package cl.ricardo.miagenda.data
import androidx.room.*
@Entity(tableName="workspaces") data class Workspace(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val archived:Boolean=false)
@Entity(tableName="tasks",indices=[Index("workspaceId")]) data class Task(@PrimaryKey(autoGenerate=true) val id:Long=0,val workspaceId:Long,val category:String,val title:String,val detail:String="",val dueAt:Long,val status:String="PENDING",val reminderMinutes:Int=2880)
@Entity(tableName="hospital_records",indices=[Index("service")]) data class HospitalRecord(@PrimaryKey(autoGenerate=true) val id:Long=0,val type:String,val service:String,val location:String="",val date:Long,val title:String,val detail:String="",val status:String="ACTIVE",val dueAt:Long?=null,val responsible:String="")
@Entity(tableName="containers",indices=[Index("service")]) data class ContainerAsset(@PrimaryKey(autoGenerate=true) val id:Long=0,val service:String,val location:String,val kind:String,val capacityLiters:Double?,val color:String="",val quantity:Int=1,val support:Boolean=false,val active:Boolean=true,val installedAt:Long=System.currentTimeMillis(),val notes:String="")
@Entity(tableName="iptv_clients",indices=[Index("expiresAt")]) data class IptvClient(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val phone:String="",val plan:String="Mensual",val amount:Int=0,val expiresAt:Long,val status:String="ACTIVE",val notes:String="")

@Entity(tableName="iptv_renewals",indices=[Index("clientId")]) data class IptvRenewal(@PrimaryKey(autoGenerate=true) val id:Long=0,val clientId:Long,val date:Long=System.currentTimeMillis(),val previousExpiry:Long,val newExpiry:Long,val amount:Int=0,val plan:String="")
