package com.animatedprofiles.app
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import android.view.ViewGroup
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.animatedprofiles.app.databinding.ActivityMainBinding
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity:AppCompatActivity(){
 private lateinit var binding:ActivityMainBinding
 private val dao by lazy{ProfileDatabase.get(this).profiles()}
 private var selectedVideo:Uri?=null
 private val pickVideo=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){selectedVideo=uri;binding.selectedVideoText.text="Archivo seleccionado: ${uri.lastPathSegment?:"archivo"}"}}
 private val exportFile=registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){uri->if(uri!=null)exportSelectedProfile(uri)}
 private val importFile=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)importProfile(uri)}
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState);binding=ActivityMainBinding.inflate(layoutInflater);setContentView(binding.root)
  if(intent?.action==Intent.ACTION_SEND||intent?.action==Intent.ACTION_VIEW){val incoming=intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM);if(incoming!=null)importProfile(incoming)}
  binding.selectVideoButton.setOnClickListener{pickVideo.launch(arrayOf("video/mp4","image/gif"))}
  binding.exportButton.setOnClickListener{if(selectedVideo==null)toast("Selecciona primero un vídeo MP4 o GIF.")else exportFile.launch("perfil-animado.wprofile")}
  binding.importButton.setOnClickListener{importFile.launch(arrayOf("application/zip","application/octet-stream","*/*"))}
  lifecycleScope.launch{dao.observeAll().collectLatest{renderProfiles(it)}}
 }
 private fun renderProfiles(profiles:List<AnimatedProfileEntity>){
  binding.profilesContainer.removeAllViews()
  if(profiles.isEmpty()){binding.profilesContainer.addView(TextView(this).apply{text="Todavía no hay perfiles. Crea uno o importa el archivo que te envíe tu amigo.";textSize=14f;setPadding(8,8,8,8)});return}
  profiles.forEach{profile->
   val card=com.google.android.material.card.MaterialCardView(this).apply{radius=18f;cardElevation=2f;setCardBackgroundColor(android.graphics.Color.WHITE);layoutParams=ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply{bottomMargin=12}}
   val content=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL;setPadding(18,14,18,14)}
   content.addView(TextView(this).apply{text=profile.displayName;textSize=17f;setTextColor(android.graphics.Color.rgb(33,26,43));setTypeface(null,android.graphics.Typeface.BOLD)})
   content.addView(TextView(this).apply{text="${profile.phone.ifBlank{"Sin teléfono asociado"}} · ${profile.mediaType.uppercase()}";textSize=13f;setTextColor(android.graphics.Color.DKGRAY);setPadding(0,4,0,10)})
   content.addView(com.google.android.material.button.MaterialButton(this).apply{text="Reproducir en bucle";setOnClickListener{startActivity(Intent(this@MainActivity,ProfilePlayerActivity::class.java).apply{putExtra(ProfilePlayerActivity.EXTRA_NAME,profile.displayName);putExtra(ProfilePlayerActivity.EXTRA_PATH,profile.mediaPath);putExtra(ProfilePlayerActivity.EXTRA_TYPE,profile.mediaType)})}})
   card.addView(content);binding.profilesContainer.addView(card)
  }
 }
 private fun exportSelectedProfile(destination:Uri){
  val source=selectedVideo?:return;val name=binding.nameInput.text?.toString()?.trim().orEmpty()
  if(name.isBlank()){toast("Escribe un nombre para el perfil.");return};val phone=binding.phoneInput.text?.toString()?.trim().orEmpty()
  lifecycleScope.launch{try{
   withContext(Dispatchers.IO){
    val ext=if(contentResolver.getType(source)=="image/gif"||source.toString().endsWith(".gif",true))"gif" else "mp4"
    val input=contentResolver.openInputStream(source)?:error("No se pudo leer el archivo.");val output=contentResolver.openOutputStream(destination)?:error("No se pudo crear el archivo.")
    ProfilePackageCodec.export(ProfilePackageCodec.Profile(UUID.randomUUID().toString(),name,phone,ext),input,output)
   }
   toast("Perfil exportado. Puedes compartirlo con tu amigo.")
   val share=Intent(Intent.ACTION_SEND).apply{type="application/octet-stream";putExtra(Intent.EXTRA_STREAM,destination);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
   startActivity(Intent.createChooser(share,"Compartir perfil animado"))
  }catch(error:Exception){toast(error.message?:"No se pudo exportar el perfil.")}}
 }
 private fun importProfile(uri:Uri){
  lifecycleScope.launch{try{
   val imported=withContext(Dispatchers.IO){val input=contentResolver.openInputStream(uri)?:error("No se pudo abrir el archivo.");ProfilePackageCodec.import(input)}
   withContext(Dispatchers.IO){val folder=File(filesDir,"profiles").apply{mkdirs()};val mediaFile=File(folder,"${imported.profile.id}.${imported.profile.mediaExtension}");mediaFile.writeBytes(imported.media);dao.upsert(AnimatedProfileEntity(imported.profile.id,imported.profile.name,imported.profile.phone,mediaFile.absolutePath,imported.profile.mediaExtension,System.currentTimeMillis()))}
   toast("Perfil de ${imported.profile.name} importado correctamente.")
  }catch(error:Exception){toast(error.message?:"No se pudo importar el perfil.")}}
 }
 private fun toast(message:String)=Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}
