package com.animatedprofiles.app
import java.io.*
import java.util.Properties
import java.util.zip.*
import org.junit.Assert.*
import org.junit.Test
class ProfilePackageCodecTest {
 @Test fun exportedProfileCanBeImported(){
  val media=byteArrayOf(1,2,3);val archive=ByteArrayOutputStream()
  ProfilePackageCodec.export(ProfilePackageCodec.Profile("p1","Ana","+34123","mp4"),ByteArrayInputStream(media),archive)
  val result=ProfilePackageCodec.import(ByteArrayInputStream(archive.toByteArray()))
  assertEquals("Ana",result.profile.name);assertEquals("+34123",result.profile.phone);assertArrayEquals(media,result.media)
 }
 @Test fun rejectsEmptyMedia(){
  assertThrows(IllegalArgumentException::class.java){ProfilePackageCodec.export(ProfilePackageCodec.Profile("p","Empty","","mp4"),ByteArrayInputStream(byteArrayOf()),ByteArrayOutputStream())}
 }
 @Test fun rejectsPathTraversalId(){
  val out=ByteArrayOutputStream()
  ZipOutputStream(out).use{z->val p=Properties().apply{setProperty("version","1");setProperty("id","../escape");setProperty("name","Bad");setProperty("phone","");setProperty("mediaExtension","mp4")};z.putNextEntry(ZipEntry("metadata.properties"));p.store(z,null);z.closeEntry();z.putNextEntry(ZipEntry("media/video.mp4"));z.write(byteArrayOf(1));z.closeEntry()}
  assertThrows(IllegalArgumentException::class.java){ProfilePackageCodec.import(ByteArrayInputStream(out.toByteArray()))}
 }
}
