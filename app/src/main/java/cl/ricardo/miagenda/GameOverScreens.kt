package cl.ricardo.miagenda
import android.app.DatePickerDialog
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
import java.util.*

@Composable fun GameOverHub(dao:AgendaDao){
 val clients by dao.iptvClients().collectAsStateWithLifecycle(emptyList());val scope=rememberCoroutineScope();val context=LocalContext.current;var add by remember{mutableStateOf(false)};var search by remember{mutableStateOf("")}
 val data=clients.filter{search.isBlank()||it.name.contains(search,true)||it.phone.contains(search)}
 Column(Modifier.fillMaxSize().padding(20.dp)){Text("GameOverTV",style=MaterialTheme.typography.headlineSmall);Text("Clientes, renovaciones y vencimientos",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));OutlinedTextField(search,{search=it},Modifier.fillMaxWidth(),label={Text("Buscar cliente")},leadingIcon={Icon(Icons.Outlined.Search,null)});Spacer(Modifier.height(8.dp));Button(onClick={add=true}){Icon(Icons.Outlined.PersonAdd,null);Spacer(Modifier.width(6.dp));Text("Nuevo cliente")};Spacer(Modifier.height(8.dp));LazyColumn{items(data){c->HorizontalDivider();ListItem(headlineContent={Text(c.name)},supportingContent={Text(c.plan+" · vence "+fmt.format(Date(c.expiresAt))+(if(c.phone.isNotBlank())+" · "+c.phone else ""))},trailingContent={Text(if(c.expiresAt<System.currentTimeMillis())"Vencido" else "Activo")})}}}
 if(add) ClientDialog({add=false}){name,phone,plan,amount,expires,notes->scope.launch{dao.addIptvClient(IptvClient(name=name,phone=phone,plan=plan,amount=amount,expiresAt=expires,notes=notes));val ws=dao.findWorkspace("GameOverTV");if(ws!=null){val id=dao.addTask(Task(workspaceId=ws.id,category="Renovación IPTV",title="Renovar: "+name,detail=phone+" · "+plan,dueAt=expires));ReminderWorker.scheduleSequence(context,id,"Renovación GameOverTV: "+name,expires,listOf(10080,2880,1440,120))}};add=false}
}
@Composable fun ClientDialog(close:()->Unit,save:(String,String,String,Int,Long,String)->Unit){var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var plan by remember{mutableStateOf("Mensual")};var amount by remember{mutableStateOf("")};var expires by remember{mutableLongStateOf(System.currentTimeMillis()+30*86400000L)};var notes by remember{mutableStateOf("")};val context=LocalContext.current;AlertDialog(onDismissRequest=close,title={Text("Nuevo cliente")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(name,{name=it},label={Text("Nombre")});OutlinedTextField(phone,{phone=it},label={Text("WhatsApp / teléfono")});OutlinedTextField(plan,{plan=it},label={Text("Plan")});OutlinedTextField(amount,{amount=it},label={Text("Monto")});OutlinedButton(onClick={val c=Calendar.getInstance().apply{timeInMillis=expires};DatePickerDialog(context,{_,y,m,d->expires=Calendar.getInstance().apply{set(y,m,d,9,0,0)}.timeInMillis},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}){Text("Vence: "+fmt.format(Date(expires)))};OutlinedTextField(notes,{notes=it},label={Text("Notas")})}},confirmButton={Button(enabled=name.isNotBlank(),onClick={save(name,phone,plan,amount.toIntOrNull()?:0,expires,notes)}){Text("Guardar")}},dismissButton={TextButton(onClick=close){Text("Cancelar")}})}
