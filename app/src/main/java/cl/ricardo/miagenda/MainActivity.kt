package cl.ricardo.miagenda
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MiAgendaApp(AppDatabase.get(this).dao())}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MiAgendaApp(dao:AgendaDao){
 val ws by dao.workspaces().collectAsStateWithLifecycle(emptyList()); val tasks by dao.tasks().collectAsStateWithLifecycle(emptyList()); var tab by remember{mutableIntStateOf(0)}
 LaunchedEffect(ws){if(ws.isEmpty()) listOf("Hospital","GameOverTV","Climarte","RLB").forEach{dao.addWorkspace(Workspace(name=it))}}
 MaterialTheme{Scaffold(topBar={TopAppBar(title={Column{Text("MiAgenda");Text("Tu jornada, organizada",style=MaterialTheme.typography.labelMedium)}})},bottomBar={NavigationBar{
  listOf("Hoy" to Icons.Outlined.Today,"Trabajos" to Icons.Outlined.WorkOutline,"Hospital" to Icons.Outlined.LocalHospital,"Agenda" to Icons.Outlined.CalendarMonth).forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.second,null)},label={Text(p.first)})}
 }}){p->Box(Modifier.padding(p).fillMaxSize()){when(tab){0->Today(tasks);1->Works(ws);2->Hospital();else->Agenda()}}}}
}
@Composable fun Today(tasks:List<Task>)=LazyColumn(Modifier.fillMaxSize().padding(20.dp)){item{Text("Próximos",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(12.dp))};if(tasks.isEmpty())item{Text("No tienes pendientes. Usa esta vista para organizar tu jornada.")}else items(tasks){HorizontalDivider();ListItem(headlineContent={Text(it.title)},supportingContent={Text(it.category)})}}
@Composable fun Works(ws:List<Workspace>)=LazyColumn(Modifier.fillMaxSize().padding(20.dp)){item{Text("Trabajos",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(12.dp))};items(ws){HorizontalDivider();ListItem(headlineContent={Text(it.name)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}}
@Composable fun Hospital(){val m=listOf("Agenda y reuniones","Renovación de contratos","Horas extras","Charlas","Inspecciones y levantamientos","Cambio / habilitación de contenedores","Inventario por servicio");LazyColumn(Modifier.fillMaxSize().padding(20.dp)){item{Text("Hospital",style=MaterialTheme.typography.headlineSmall);Text("Gestión administrativa y operativa");Spacer(Modifier.height(12.dp))};items(m){HorizontalDivider();ListItem(headlineContent={Text(it)},trailingContent={Icon(Icons.Outlined.ChevronRight,null)})}}}
@Composable fun Agenda()=Column(Modifier.padding(20.dp)){Text("Agenda",style=MaterialTheme.typography.headlineSmall);Text("Calendario, vencimientos y recordatorios.")}