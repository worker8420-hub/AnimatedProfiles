package com.animatedprofiles.app
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="profiles")
data class AnimatedProfileEntity(@PrimaryKey val id:String,val displayName:String,val phone:String,val mediaPath:String,val mediaType:String,val createdAt:Long)
@Dao interface AnimatedProfileDao {
 @Query("SELECT * FROM profiles ORDER BY displayName COLLATE NOCASE") fun observeAll():Flow<List<AnimatedProfileEntity>>
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(profile:AnimatedProfileEntity)
}
@Database(entities=[AnimatedProfileEntity::class],version=1,exportSchema=false)
abstract class ProfileDatabase:RoomDatabase(){
 abstract fun profiles():AnimatedProfileDao
 companion object {
  @Volatile private var INSTANCE:ProfileDatabase?=null
  fun get(context:Context):ProfileDatabase=INSTANCE?: synchronized(this){
   INSTANCE?:Room.databaseBuilder(context.applicationContext,ProfileDatabase::class.java,"animated_profiles.db").build().also{INSTANCE=it}
  }
 }
}
