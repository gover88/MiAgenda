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
  ){p->Box(Modifier.padding(p).fillMaxSize()){when(tab){0->Today(tasks,ws){scope.launch{dao.updateTask(it.copy(status=if(it.status=="PENDING")"DONE" else "PENDING"))}};1->Works(ws);2->HospitalHub(dao);else->Agenda(tasks)}}}
  if(add) NewTaskDialog(ws,{add=false}){w,title,category,due,detail->scope.launch{dao.addTask(Task(workspaceId=w,category=category,title=title,detail=detail,dueAt=due))};add=false}
 }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun Today(tasks:List<Task>,workspaces:List<Workspace>,toggle:(Task)->Unit){
 var period by remember{mutableStateOf("Hoy")};var work by remember{mutableStateOf<Long?>(null)}
 val now=Calendar.getInstance();val from=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
 val to=Calendar.getInstance().apply{setTimeInMillis(from);when(period){"Hoy"->add(Calendar.DAY_OF_YEAR,1);"Semana"->add(Calendar.DAY_OF_YEAR,7);else->add(Calendar.MONTH,1)}}.timeInMillis
 val visible=tasks.filter{it.status=="PENDING"&&it.dueAt in from until to&&(work==null||it.workspaceId==work)}.sortedBy{it.dueAt}
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(vertical=18.dp)){
  item{Text("Tu jornada",style=MaterialTheme.typography.headlineSmall);Text("Filtra por período y trabajo",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(14.dp))
   Text("Período",style=MaterialTheme.typography.labelMedium);SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()){listOf("Hoy","Semana","Mes").forEachIndexed{i,v->SegmentedButton(selected=period==v,onClick={period=v},shape=SegmentedButtonDefaults.itemShape(i,3)){Text(v)}}};Spacer(Modifier.height(12.dp))
   Text("Trabajo",style=MaterialTheme.typography.labelMedium);LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(vertical=6.dp)){item{FilterChip(selected=work==null,onClick={work=null},label={Text("Todos")})};items(workspaces){w->FilterChip(selected=work==w.id,onClick={work=w.id},label={Text(w.name)})}}
   Spacer(Modifier.height(8.dp));Text(visible.size.toString()+" pendiente"+if(visible.size==1)"":"s",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(8.dp))
  }
  if(visible.isEmpty())item{Text("No tienes pendientes para estos filtros.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  else items(visible,key={it.id}){t->val wn=workspaces.firstOrNull{it.id==t.workspaceId}?.name?:"Trabajo";HorizontalDivider();ListItem(modifier=Modifier.clickable{toggle(t)},headlineContent={Text(t.title)},supportingContent={Text(wn+" · "+fmt.format(Date(t.dueAt))+" · "+t.category)},leadingContent={Icon(Icons.Outlined.RadioButtonUnchecked,null)})}
 }
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
@Composable fun WeeklySummary(tasks:List<Task>){
 val cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,0);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0)
 val start=cal.timeInMillis;cal.add(Calendar.DAY_OF_YEAR,7);val end=cal.timeInMillis
 val week=tasks.filter{it.status=="PENDING"&&it.dueAt in start until end}.sortedBy{it.dueAt}
 Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("Esta semana",style=MaterialTheme.typography.titleMedium);Text("Próximos 7 días",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(week.size.toString(),style=MaterialTheme.typography.headlineSmall)}
  if(week.isEmpty())Text("No tienes vencimientos próximos.",modifier=Modifier.padding(top=10.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)
  else week.take(4).forEach{t->HorizontalDivider(Modifier.padding(vertical=6.dp));Text(t.title,style=MaterialTheme.typography.bodyMedium);Text(fmt.format(Date(t.dueAt))+" · "+t.category,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  if(week.size>4)Text("+ "+(week.size-4)+" pendientes más",modifier=Modifier.padding(top=8.dp),style=MaterialTheme.typography.labelMedium)
 }}
}