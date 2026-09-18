package cl.ricardo.miagenda
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class ReminderWorker(c:Context,p:WorkerParameters):Worker(c,p){
 override fun doWork():Result{
  val title=inputData.getString("title")?:"Tarea pendiente"
  val nm=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  nm.createNotificationChannel(NotificationChannel("agenda","Recordatorios",NotificationManager.IMPORTANCE_DEFAULT))
  if(android.os.Build.VERSION.SDK_INT<33||applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
   NotificationManagerCompat.from(applicationContext).notify(inputData.getInt("id",1),NotificationCompat.Builder(applicationContext,"agenda").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("MiAgenda").setContentText(title).setAutoCancel(true).build())
  return Result.success()
 }
 companion object{
  fun schedule(c:Context,id:Long,title:String,due:Long,minutes:Int){scheduleOne(c,id,title,due,minutes)}
  fun scheduleSequence(c:Context,id:Long,title:String,due:Long,minutes:List<Int>){
   minutes.distinct().filter{due-System.currentTimeMillis()>TimeUnit.MINUTES.toMillis(it.toLong())}.forEach{scheduleOne(c,id,title,due,it)}
  }
  private fun scheduleOne(c:Context,id:Long,title:String,due:Long,minutes:Int){
   val delay=(due-System.currentTimeMillis()-TimeUnit.MINUTES.toMillis(minutes.toLong())).coerceAtLeast(0)
   val label=when{minutes>=1440->"${minutes/1440} días";minutes>=60->"${minutes/60} horas";else->"$minutes min"}
   WorkManager.getInstance(c).enqueueUniqueWork("task_$id_$minutes",ExistingWorkPolicy.REPLACE,OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay,TimeUnit.MILLISECONDS).setInputData(workDataOf("id" to (id.toInt()+minutes),"title" to "$title · vence en $label")).build())
  }
 }
}