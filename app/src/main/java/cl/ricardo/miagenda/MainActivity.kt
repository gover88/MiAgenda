package cl.ricardo.miagenda
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ricardo.miagenda.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MiAgendaApp(AppDatabase.get(this).dao())}}}
private val fmt=SimpleDateFormat("dd/MM/yyyy",Locale("es","CL"))
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MiAgendaApp(dao:AgendaDao){
 val ws by dao.workspaces().collectAsStateWithLifecycle(emptyList()); val tasks by dao.tasks().collectAsStateWithLifecycle(emptyList())
 var tab by remember{mutableIntStateOf(0)}; var add by remember{mutableStateOf(false)}; val scope=rememberCoroutineScope()
 LaunchedEffect(ws){if(ws.isEmpty()) listOf("Hospital","GameOverTV","Climarte","RLB").forEach{dao.addWorkspace(Workspace(name=it))}}
 MaterialTheme(colorScheme=lightColorScheme()){
  Scaffold(topBar={TopAppBar(title={Column{Text("MiAgenda");Text("Tu jornada, organizada",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}})},
   floatingActionButton={if(tab==0) FloatingActionButton(onClick={add=true}){Icon(Icons.Outlined.Add,"Nueva tarea")}},
   bottomBar={NavigationBar{listOf("Hoy" to Icons.Outlined.Today,"Trabajos" to Icons.Outlined.WorkOutline,"Hospital" to Icons.Outlined.LocalHospital,"Agenda" to Icons.Outlined.CalendarMonth).forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.second,null)},label={Text(p.first)})}}}
  ){p->Box(Modifier.padding(p).fillMaxSize()){when(tab){0->Today(tasks){scope.launch{dao.updateTask(it.copy(status=if(it.status=="PENDING")"DONE" else "PENDING"))}};1->Works(ws);2->HospitalHub(dao);else->Agenda(tasks)}}}
  if(add) NewTaskDialog(ws,{add=false}){w,title,category,due,detail->scope.launch{dao.addTask(Task(workspaceId=w,category=category,title=title,detail=detail,dueAt=due))};add=false}
 }
}
@Composable fun Today(tasks:List<Task>,toggle:(Task)->Unit)=LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){
 item{Text("Hoy",style=MaterialTheme.typography.headlineSmall);Text("Pendientes y próximos vencimientos",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(16.dp))}
 if(tasks.isEmpty())item{Text("No tienes pendientes. Pulsa + para crear tu primera tarea.")} else items(tasks,key={it.id}){t->HorizontalDivider();ListItem(modifier=Modifier.clickable{toggle(t)},headlineContent={Text(t.title)},supportingContent={Text(t.category+" · "+fmt.format(Date(t.dueAt)))},leadingContent={Icon(if(t.status=="DONE") Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,null)})}
}
@Composable fun Works(ws:List<Workspace>)=LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){item{Text("Trabajos",style=MaterialTheme.typography.headlineSmall);Text("Tus áreas de trabajo",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp))};items(ws){HorizontalDivider();ListItem(headlineContent={Text(it.name)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}}
@Composable fun Hospital(){val m=listOf("Agenda y reuniones","Renovación de contratos","Horas extras","Charlas","Inspecciones y levantamientos","Cambio / habilitación de contenedores","Inventario por servicio");LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){item{Text("Hospital",style=MaterialTheme.typography.headlineSmall);Text("Gestión administrativa y operativa",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp))};items(m){HorizontalDivider();ListItem(headlineContent={Text(it)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}}}
@Composable fun Agenda(tasks:List<Task>)=LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){item{Text("Agenda",style=MaterialTheme.typography.headlineSmall);Text("Vencimientos registrados",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp))};items(tasks){HorizontalDivider();ListItem(headlineContent={Text(fmt.format(Date(it.dueAt)))},supportingContent={Text(it.title)})}}
@Composable fun NewTaskDialog(ws:List<Workspace>,close:()->Unit,save:(Long,String,String,Long,String)->Unit){
 var title by remember{mutableStateOf("")};var cat by remember{mutableStateOf("General")};var detail by remember{mutableStateOf("")};var work by remember{mutableStateOf(ws.firstOrNull()?.id?:0)};var due by remember{mutableLongStateOf(System.currentTimeMillis())};val c=LocalContext.current
 AlertDialog(onDismissRequest=close,title={Text("Nueva tarea")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
  OutlinedTextField(title,{title=it},label={Text("Título")},singleLine=true)
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ws.take(4).forEach{w->FilterChip(selected=work==w.id,onClick={work=w.id},label={Text(w.name)})}}
  OutlinedTextField(cat,{cat=it},label={Text("Categoría")},singleLine=true)
  OutlinedButton(onClick={val cal=Calendar.getInstance().apply{timeInMillis=due};DatePickerDialog(c,{_,y,m,d->due=Calendar.getInstance().apply{set(y,m,d,9,0,0);set(Calendar.MILLISECOND,0)}.timeInMillis},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()}){Icon(Icons.Outlined.CalendarMonth,null);Spacer(Modifier.width(8.dp));Text(fmt.format(Date(due)))}
  OutlinedTextField(detail,{detail=it},label={Text("Notas")},minLines=2)
 }},confirmButton={Button(enabled=title.isNotBlank()&&work!=0L,onClick={save(work,title.trim(),cat.trim(),due,detail.trim())}){Text("Guardar")}},dismissButton={TextButton(onClick=close){Text("Cancelar")}})
}