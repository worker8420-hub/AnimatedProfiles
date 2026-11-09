package com.animatedprofiles.app
import java.io.*
import java.util.Properties
import java.util.UUID
import java.util.zip.*
object ProfilePackageCodec {
 const val MAX_MEDIA_BYTES=30*1024*1024
 private const val MAX_ARCHIVE_BYTES=MAX_MEDIA_BYTES+64*1024
 private const val METADATA_ENTRY="metadata.properties"
 private val allowedMedia=setOf("mp4","gif")
 data class Profile(val id:String,val name:String,val phone:String,val mediaExtension:String)
 data class ImportedProfile(val profile:Profile,val media:ByteArray)
 private fun readLimited(input:InputStream,limit:Int):ByteArray{
  val output=ByteArrayOutputStream();val buffer=ByteArray(8192)
  while(true){val count=input.read(buffer);if(count<0)break;require(output.size()+count<=limit){"El archivo supera el límite permitido."};output.write(buffer,0,count)}
  return output.toByteArray()
 }
 fun export(profile:Profile,media:InputStream,output:OutputStream){
  require(profile.name.isNotBlank()){"El nombre del perfil es obligatorio."}
  val extension=profile.mediaExtension.lowercase();require(extension in allowedMedia){"Formato no compatible. Usa MP4 o GIF."}
  val bytes=media.use{readLimited(it,MAX_MEDIA_BYTES)};require(bytes.isNotEmpty()){"El archivo multimedia está vacío."}
  val props=Properties().apply{
   setProperty("version","1");val safeId=profile.id.ifBlank{UUID.randomUUID().toString()}
   require(safeId.matches(Regex("[A-Za-z0-9_-]{1,64}"))){"Identificador no válido."}
   setProperty("id",safeId);setProperty("name",profile.name.trim());setProperty("phone",profile.phone.trim());setProperty("mediaExtension",extension)
  }
  ZipOutputStream(output).use{zip->zip.putNextEntry(ZipEntry(METADATA_ENTRY));props.store(zip,"Animated Profiles");zip.closeEntry();zip.putNextEntry(ZipEntry("media/video.$extension"));zip.write(bytes);zip.closeEntry()}
 }
 fun import(input:InputStream):ImportedProfile{
  val archive=input.use{readLimited(it,MAX_ARCHIVE_BYTES)}
  var metadata:ByteArray?=null;var media:ByteArray?=null;var mediaEntry:String?=null
  ZipInputStream(ByteArrayInputStream(archive)).use{zip->while(true){
   val entry=zip.nextEntry?:break
   require(!entry.isDirectory&&!entry.name.startsWith("/")&&!entry.name.contains("..")){"Ruta no válida."}
   when(entry.name){
    METADATA_ENTRY->{require(metadata==null){"Metadatos duplicados."};metadata=readLimited(zip,16*1024)}
    "media/video.mp4","media/video.gif"->{require(media==null){"Más de un vídeo."};media=readLimited(zip,MAX_MEDIA_BYTES);mediaEntry=entry.name}
    else->throw IllegalArgumentException("Elemento no compatible.")
   };zip.closeEntry()
  }}
  val meta=metadata?:throw IllegalArgumentException("Faltan los datos del perfil.")
  val bytes=media?:throw IllegalArgumentException("Falta el vídeo o GIF.")
  require(bytes.isNotEmpty()){"El vídeo está vacío."}
  val props=Properties().apply{load(ByteArrayInputStream(meta))}
  require(props.getProperty("version")=="1"){"Versión no compatible."}
  val ext=props.getProperty("mediaExtension","").lowercase();require(ext in allowedMedia){"Formato no compatible."}
  require(mediaEntry=="media/video.$ext"){"La extensión no coincide."}
  val id=props.getProperty("id","").trim();val name=props.getProperty("name","").trim()
  require(id.matches(Regex("[A-Za-z0-9_-]{1,64}"))&&name.isNotBlank()){"Datos incompletos o identificador no válido."}
  return ImportedProfile(Profile(id,name,props.getProperty("phone","").trim(),ext),bytes)
 }
}
