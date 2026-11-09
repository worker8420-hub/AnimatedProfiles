package com.animatedprofiles.app
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.animatedprofiles.app.databinding.ActivityPlayerBinding
import java.io.File
class ProfilePlayerActivity:AppCompatActivity(){
 private lateinit var binding:ActivityPlayerBinding
 private var player:ExoPlayer?=null
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState);binding=ActivityPlayerBinding.inflate(layoutInflater);setContentView(binding.root)
  val name=intent.getStringExtra(EXTRA_NAME).orEmpty();val file=File(intent.getStringExtra(EXTRA_PATH).orEmpty());val type=intent.getStringExtra(EXTRA_TYPE).orEmpty()
  if(name.isBlank()||!file.isFile||!file.canRead()){finish();return};binding.playerTitle.text=name
  if(type=="gif"){binding.videoPlayer.visibility=View.GONE;binding.gifPlayer.visibility=View.VISIBLE;Glide.with(this).asGif().load(file).into(binding.gifPlayer)}
  else{binding.gifPlayer.visibility=View.GONE;binding.videoPlayer.visibility=View.VISIBLE;player=ExoPlayer.Builder(this).build().also{exo->binding.videoPlayer.player=exo;exo.setMediaItem(MediaItem.fromUri(file.toURI().toString()));exo.repeatMode=Player.REPEAT_MODE_ONE;exo.prepare();exo.playWhenReady=true}}
 }
 override fun onStop(){binding.videoPlayer.player=null;player?.release();player=null;super.onStop()}
 companion object{const val EXTRA_NAME="profile_name";const val EXTRA_PATH="profile_path";const val EXTRA_TYPE="profile_type"}
}
