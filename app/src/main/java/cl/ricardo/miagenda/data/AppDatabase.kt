package cl.ricardo.miagenda.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface AgendaDao {
 @Query("SELECT * FROM workspaces WHERE archived=0 ORDER BY name") fun workspaces():Flow<List<Workspace>>
 @Insert suspend fun addWorkspace(v:Workspace):Long
 @Query("SELECT * FROM tasks ORDER BY CASE WHEN status='PENDING' THEN 0 ELSE 1 END,dueAt") fun tasks():Flow<List<Task>>
 @Insert suspend fun addTask(v:Task):Long
 @Update suspend fun updateTask(v:Task)
 @Query("SELECT * FROM hospital_records ORDER BY date DESC") fun hospitalRecords():Flow<List<HospitalRecord>>
 @Insert suspend fun addHospitalRecord(v:HospitalRecord):Long
 @Query("SELECT * FROM containers WHERE active=1 ORDER BY service,location") fun containers():Flow<List<ContainerAsset>>
 @Insert suspend fun addContainer(v:ContainerAsset):Long
}
@Database(entities=[Workspace::class,Task::class,HospitalRecord::class,ContainerAsset::class],version=1,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){
 abstract fun dao():AgendaDao
 companion object{@Volatile private var INSTANCE:AppDatabase?=null
 fun get(c:android.content.Context)=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(c.applicationContext,AppDatabase::class.java,"miagenda.db").build().also{INSTANCE=it}}}
}