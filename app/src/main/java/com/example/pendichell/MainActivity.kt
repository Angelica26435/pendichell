package com.example.pendichell

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pendichell.data.SubtaskEntity
import com.example.pendichell.data.TaskEntity
import com.example.pendichell.ui.theme.*
import com.example.pendichell.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            TaskTheme {
                val viewModel: TaskViewModel = viewModel()
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Función para abrir la configuración del sistema de alarmas y sonido
    fun openSystemAlarmSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Pendichell 🎀✨", 
                            color = TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ) 
                        Text(
                            "Tus pendientes más lindos 💕", 
                            color = TextDark.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    // Modificación 1: Botón para acceder a las opciones de Alarma y Sonido del Sistema
                    IconButton(onClick = { openSystemAlarmSettings() }) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Ajustes de Alarma y Sonido",
                            tint = PrimaryPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Agregar Tarea",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = BackgroundPastel
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (tasks.isEmpty()) {
                // Estado vacío rosita y femenino
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, CardBorderPink, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("🌸✨💖", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡No tienes pendientes por ahora!",
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca el botón rosa + para agregar tu primer pendiente y programar tu alarma recordatoria. 🎀",
                                color = TextDark.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(task = task, viewModel = viewModel)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, desc, priority, dueDate ->
                    viewModel.addTask(
                        title = title,
                        description = desc,
                        dueDate = dueDate,
                        priority = priority
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity, viewModel: TaskViewModel) {
    val subtasks by viewModel.getSubtasks(task.id).collectAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf(false) }

    val progress = if (subtasks.isNotEmpty()) {
        subtasks.count { it.isCompleted }.toFloat() / subtasks.size
    } else 0f

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val (priorityLabel, priorityBadgeColor, priorityTextColor) = when (task.priority) {
        "Urgente" -> Triple("💖 Urgente", PriorityUrgent, Color.White)
        "Alta" -> Triple("💕 Alta", PriorityHigh, Color.White)
        "Media" -> Triple("🌸 Media", PriorityMedium, Color.White)
        "Baja" -> Triple("🎀 Baja", PriorityLow, TextDark)
        else -> Triple("🌸 " + task.priority, PrimaryPink, Color.White)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, CardBorderPink, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { viewModel.toggleTaskCompleted(task) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryPink,
                            uncheckedColor = CardBorderPink
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = priorityBadgeColor,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = priorityLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityTextColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // HU-05: Botón para editar
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryPink)
                }

                // HU-05: Botón para eliminar
                IconButton(onClick = { viewModel.deleteTask(task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = PriorityUrgent)
                }
            }

            task.description?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = PrimaryPink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Recordatorio: ${dateFormat.format(Date(task.dueDate))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextDark.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
            }

            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = PrimaryPink,
                    trackColor = PrimaryPurple
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            SubtaskSection(taskId = task.id, subtasks = subtasks, viewModel = viewModel)
        }
    }

    if (showEditDialog) {
        EditTaskDialog(
            task = task,
            onDismiss = { showEditDialog = false },
            onSave = { title, desc, priority, dueDate ->
                viewModel.updateTaskDetails(task, title, desc, dueDate, priority)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SubtaskSection(taskId: Long, subtasks: List<SubtaskEntity>, viewModel: TaskViewModel) {
    var newSubtaskText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(start = 4.dp)) {
        subtasks.forEach { subtask ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = subtask.isCompleted,
                    onCheckedChange = { viewModel.toggleSubtask(subtask) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryPink,
                        uncheckedColor = CardBorderPink
                    )
                )
                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = TextDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                label = { Text("🌸 Agregar subpendiente") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPink,
                    unfocusedBorderColor = CardBorderPink
                )
            )
            IconButton(onClick = {
                if (newSubtaskText.isNotBlank()) {
                    viewModel.addSubtask(taskId, newSubtaskText)
                    newSubtaskText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Subpendiente", tint = PrimaryPink)
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, priority: String, dueDate: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Media") }
    var showErrorAlert by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) } }
    var selectedDueDate by remember { mutableLongStateOf(calendar.timeInMillis) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    fun showDateTimePicker() {
        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timeCal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val resultCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDueDate = resultCal.timeInMillis
                    },
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timeCal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val priorities = listOf(
        "Urgente" to PriorityUrgent,
        "Alta" to PriorityHigh,
        "Media" to PriorityMedium,
        "Baja" to PriorityLow
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Pendiente ✨💖", color = TextDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showErrorAlert) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AlertBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ¡El título es obligatorio, linda!",
                            color = AlertText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it 
                        if (it.isNotBlank()) showErrorAlert = false
                    },
                    label = { Text("Título de la actividad *") },
                    singleLine = true,
                    isError = showErrorAlert,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("¿Qué se hará? (Opcional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("📅 Fecha y Hora de la Alarma:", style = MaterialTheme.typography.labelMedium, color = TextDark, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showDateTimePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                ) {
                    Text("⏰ ${dateFormat.format(Date(selectedDueDate))}", fontWeight = FontWeight.Bold)
                }

                Text("Prioridad / Matiz Pastel:", style = MaterialTheme.typography.labelMedium, color = TextDark, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { (pName, color) ->
                        FilterChip(
                            selected = selectedPriority == pName,
                            onClick = { selectedPriority = pName },
                            label = { Text(pName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                containerColor = color.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, selectedPriority, selectedDueDate)
                    } else {
                        showErrorAlert = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar 💖", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextDark)
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditTaskDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, priority: String, dueDate: Long) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var selectedDueDate by remember { mutableLongStateOf(task.dueDate) }
    var showErrorAlert by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    fun showDateTimePicker() {
        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timeCal = Calendar.getInstance().apply { timeInMillis = selectedDueDate }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val resultCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDueDate = resultCal.timeInMillis
                    },
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timeCal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val priorities = listOf(
        "Urgente" to PriorityUrgent,
        "Alta" to PriorityHigh,
        "Media" to PriorityMedium,
        "Baja" to PriorityLow
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Pendiente ✏️💖", color = TextDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showErrorAlert) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AlertBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ¡El título es obligatorio, linda!",
                            color = AlertText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it 
                        if (it.isNotBlank()) showErrorAlert = false
                    },
                    label = { Text("Título de la actividad *") },
                    singleLine = true,
                    isError = showErrorAlert,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("¿Qué se hará? (Opcional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("📅 Fecha y Hora de la Alarma:", style = MaterialTheme.typography.labelMedium, color = TextDark, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showDateTimePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                ) {
                    Text("⏰ ${dateFormat.format(Date(selectedDueDate))}", fontWeight = FontWeight.Bold)
                }

                Text("Prioridad / Matiz Pastel:", style = MaterialTheme.typography.labelMedium, color = TextDark, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { (pName, color) ->
                        FilterChip(
                            selected = selectedPriority == pName,
                            onClick = { selectedPriority = pName },
                            label = { Text(pName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                containerColor = color.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, selectedPriority, selectedDueDate)
                    } else {
                        showErrorAlert = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios 💖", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextDark)
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
