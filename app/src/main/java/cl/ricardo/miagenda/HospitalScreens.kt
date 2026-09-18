package cl.ricardo.miagenda
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ricardo.miagenda.data.*
import kotlinx.coroutines.launch
import java.util.Date

@Composable fun HospitalHub(dao:AgendaDao){
 val records by dao.hospitalRecords().collectAsStateWithLifecycle(emptyList());val containers by dao.containers().collectAsStateWithLifecycle(emptyList())
 var section by remember{mutableStateOf<String?>(null)}
 if(section!=null){HospitalSection(section!!,records,containers,dao){section=null};return}
 val modules=listOf("Renovación de contratos" to "CONTRACT","Charlas" to "TALK","Inspecciones y levantamientos" to "INSPECTION","Cambio / habilitación de contenedores" to "CONTAINER_CHANGE","Inventario por servicio" to "INVENTORY")
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){
 item{Text("Hospital",style=MaterialTheme.typography.headlineSmall);Text("Gestión administrativa y operativa",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(16.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Metric("Registros",records.size);Metric("Contenedores",containers.sumOf{it.quantity});Metric("Pendientes",records.count{it.status!="DONE"})};Spacer(Modifier.height(16.dp))}
 items(modules){m->HorizontalDivider();ListItem(Modifier.clickable{section=m.second},headlineContent={Text(m.first)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}
 }}
@Composable private fun Metric(label:String,n:Int){Column{Text(n.toString(),style=MaterialTheme.typography.titleLarge);Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun HospitalSection(type:String,records:List<HospitalRecord>,containers:List<ContainerAsset>,dao:AgendaDao,back:()->Unit){
 val scope=rememberCoroutineScope();var add by remember{mutableStateOf(false)}
 val title=when(type){"CONTRACT"->"Contratos";"TALK"->"Charlas";"INSPECTION"->"Inspecciones";"CONTAINER_CHANGE"->"Cambios de contenedor";else->"Inventario por servicio"}
 Column(Modifier.fillMaxSize().padding(20.dp)){Row{IconButton(onClick=back){Icon(Icons.Outlined.ArrowBack,null)};Column{Text(title,style=MaterialTheme.typography.headlineSmall);Text("Hospital",color=MaterialTheme.colorScheme.onSurfaceVariant)}};Spacer(Modifier.height(12.dp))
 if(type=="INVENTORY") InventoryList(containers) else {Button(onClick={add=true}){Icon(Icons.Outlined.Add,null);Spacer(Modifier.width(6.dp));Text("Agregar")};Spacer(Modifier.height(8.dp));LazyColumn{items(records.filter{it.type==type}){r->HorizontalDivider();ListItem(headlineContent={Text(r.title)},supportingContent={Text(r.service+(if(r.location.isNotBlank())" · "+r.location else "")+" · "+fmt.format(Date(r.date)))},trailingContent={Text(r.status)})}}}}
 if(add){if(type=="CONTAINER_CHANGE") ContainerDialog({add=false}){service,loc,kind,liters,color,qty,notes->scope.launch{dao.addContainer(ContainerAsset(service=service,location=loc,kind=kind,capacityLiters=liters,color=color,quantity=qty,notes=notes));dao.addHospitalRecord(HospitalRecord(type=type,service=service,location=loc,date=System.currentTimeMillis(),title="Habilitación: "+kind,detail=notes))};add=false} else SmartRecordDialog(type,{add=false}){service,loc,name,detail,date->scope.launch{dao.addHospitalRecord(HospitalRecord(type=type,service=service,location=loc,date=date,title=name,detail=detail))};add=false}}
 }}
@Composable fun InventoryList(data:List<ContainerAsset>){var filter by remember{mutableStateOf("")};OutlinedTextField(filter,{filter=it},Modifier.fillMaxWidth(),label={Text("Filtrar por servicio")},leadingIcon={Icon(Icons.Outlined.Search,null)});Spacer(Modifier.height(8.dp));val filtered=data.filter{filter.isBlank()||it.service.contains(filter,true)};LazyColumn{items(filtered){item->HorizontalDivider();ListItem(headlineContent={Text(item.quantity.toString()+" × "+item.kind+(item.capacityLiters?.let{" · "+it+" L"}?:""))},supportingContent={Text(item.service+" · "+item.location+" · "+item.color+(if(item.support)" · con soporte" else ""))})}}}
@Composable fun SmartRecordDialog(type:String,close:()->Unit,save:(String,String,String,String,Long)->Unit){
 var service by remember{mutableStateOf("")};var location by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var detail by remember{mutableStateOf("")};var date by remember{mutableLongStateOf(System.currentTimeMillis())};val context=androidx.compose.ui.platform.LocalContext.current
 val labels=when(type){"CONTRACT"->listOf("Servicio / unidad","Cargo o ubicación","Nombre del funcionario","Tipo de contrato / observaciones");"TALK"->listOf("Servicio clínico","Lugar","Tema de la charla","Asistentes / observaciones");else->listOf("Servicio clínico","Sala / box / ubicación","Tipo de inspección","Hallazgos, responsable y seguimiento")}
 AlertDialog(onDismissRequest=close,title={Text(when(type){"CONTRACT"->"Nueva renovación";"TALK"->"Nueva charla";else->"Nueva inspección"})},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  OutlinedTextField(service,{service=it},label={Text(labels[0])});OutlinedTextField(location,{location=it},label={Text(labels[1])});OutlinedTextField(name,{name=it},label={Text(labels[2])});OutlinedTextField(detail,{detail=it},label={Text(labels[3])},minLines=2)
  OutlinedButton(onClick={val cal=java.util.Calendar.getInstance().apply{timeInMillis=date};android.app.DatePickerDialog(context,{_,y,m,d->date=java.util.Calendar.getInstance().apply{set(y,m,d,9,0,0)}.timeInMillis},cal.get(java.util.Calendar.YEAR),cal.get(java.util.Calendar.MONTH),cal.get(java.util.Calendar.DAY_OF_MONTH)).show()}){Icon(Icons.Outlined.CalendarMonth,null);Spacer(Modifier.width(6.dp));Text((if(type=="CONTRACT")"Vencimiento: " else "Fecha: ")+fmt.format(java.util.Date(date)))}
 }},confirmButton={Button(enabled=service.isNotBlank()&&name.isNotBlank(),onClick={save(service,location,name,detail,date)}){Text("Guardar")}},dismissButton={TextButton(onClick=close){Text("Cancelar")}})
}
@Composable fun ContainerDialog(close:()->Unit,save:(String,String,String,Double?,String,Int,String)->Unit){var s by remember{mutableStateOf("")};var l by remember{mutableStateOf("")};var k by remember{mutableStateOf("")};var cap by remember{mutableStateOf("")};var color by remember{mutableStateOf("")};var q by remember{mutableStateOf("1")};var notes by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Habilitar contenedor")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){OutlinedTextField(s,{s=it},label={Text("Servicio")});OutlinedTextField(l,{l=it},label={Text("Sala / box / ubicación")});OutlinedTextField(k,{k=it},label={Text("Tipo de contenedor")});OutlinedTextField(cap,{cap=it},label={Text("Capacidad (L)")});OutlinedTextField(color,{color=it},label={Text("Color")});OutlinedTextField(q,{q=it},label={Text("Cantidad")});OutlinedTextField(notes,{notes=it},label={Text("Observaciones")})}},confirmButton={Button(enabled=s.isNotBlank()&&l.isNotBlank()&&k.isNotBlank(),onClick={save(s,l,k,cap.toDoubleOrNull(),color,q.toIntOrNull()?:1,notes)}){Text("Guardar")}},dismissButton={TextButton(onClick=close){Text("Cancelar")}})}
