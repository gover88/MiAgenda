package cl.ricardo.miagenda
import android.content.Context
import java.io.File
object BackupManager{
 fun createLocalBackup(context:Context):File{val db=context.getDatabasePath("miagenda.db");val dir=File(context.filesDir,"backups").apply{mkdirs()};val out=File(dir,"miagenda-backup.db");if(db.exists())db.copyTo(out,true);return out}
}