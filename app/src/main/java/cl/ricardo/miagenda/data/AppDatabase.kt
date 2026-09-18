package cl.ricardo.miagenda.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface AgendaDao {
 @Query("SELECT * FROM workspaces WHERE archived=0 ORDER BY name") fun workspaces():Flow<List<Workspace>>
 @Insert suspend fun addWorkspace(v:Workspace):Long
 @Query("SELECT * FROM workspaces WHERE name=:name LIMIT 1") suspend fun findWorkspace(name:String):Workspace?
 @Query("SELECT * FROM tasks ORDER BY CASE WHEN status='PENDING' THEN 0 ELSE 1 END,dueAt") fun tasks():Flow<List<Task>>
 @Insert suspend fun addTask(v:Task):Long
 @Update suspend fun updateTask(v:Task)
 @Delete suspend fun deleteTask(v:Task)
 @Query("SELECT * FROM hospital_records ORDER BY date DESC") fun hospitalRecords():Flow<List<HospitalRecord>>
 @Insert suspend fun addHospitalRecord(v:HospitalRecord):Long
 @Update suspend fun updateHospitalRecord(v:HospitalRecord)
 @Delete suspend fun deleteHospitalRecord(v:HospitalRecord)
 @Query("SELECT * FROM containers WHERE active=1 ORDER BY service,location") fun containers():Flow<List<ContainerAsset>>
 @Insert suspend fun addContainer(v:ContainerAsset):Long
 @Update suspend fun updateContainer(v:ContainerAsset)
 @Delete suspend fun deleteContainer(v:ContainerAsset)
}
@Database(entities=[Workspace::class,Task::class,HospitalRecord::class,ContainerAsset::class],version=1,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){
 abstract fun dao():AgendaDao
 companion object{
  @Volatile private var INSTANCE:AppDatabase?=null
  fun get(c:android.content.Context):AppDatabase=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"miagenda.db").build().also{INSTANCE=it}}
  fun closeInstance(){synchronized(this){INSTANCE?.close();INSTANCE=null}}
 }
}