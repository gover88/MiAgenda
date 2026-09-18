package cl.ricardo.miagenda
import android.content.Context
import android.net.Uri
import java.io.File
object BackupManager{
 fun createLocalBackup(context:Context):File{checkpoint(context);val db=context.getDatabasePath("miagenda.db");val dir=File(context.filesDir,"backups").apply{mkdirs()};val out=File(dir,"miagenda-backup.db");if(db.exists())db.copyTo(out,true);return out}
 private fun checkpoint(context:Context){runCatching{cl.ricardo.miagenda.data.AppDatabase.get(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()}}
 fun restoreFromUri(context:Context,uri:Uri):Boolean{return runCatching{
  val temp=File(context.cacheDir,"restore-check.db");context.contentResolver.openInputStream(uri)!!.use{input->temp.outputStream().use{input.copyTo(it)}}
  val header=ByteArray(16);temp.inputStream().use{it.read(header)};require(String(header,Charsets.US_ASCII).startsWith("SQLite format 3"))
  val db=context.getDatabasePath("miagenda.db");val safety=File(context.filesDir,"backups/pre-restore.db");safety.parentFile?.mkdirs();if(db.exists())db.copyTo(safety,true)
  cl.ricardo.miagenda.data.AppDatabase.closeInstance();listOf(db,File(db.path+"-wal"),File(db.path+"-shm")).forEach{if(it.exists())it.delete()};temp.copyTo(db,true);temp.delete();true
 }.getOrDefault(false)}
}