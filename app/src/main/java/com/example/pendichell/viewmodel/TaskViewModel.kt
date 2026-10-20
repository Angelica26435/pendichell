package com.example.pendichell.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pendichell.data.*
import com.example.pendichell.receiver.AlarmReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).taskDao()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()

    fun addTask(title: String, description: String?, dueDate: Long, priority: String) {
        viewModelScope.launch {
            val task = TaskEntity(title = title, description = description, dueDate = dueDate, priority = priority)
            val id = dao.insertTask(task)
            scheduleAlarm(id, title, description, dueDate)
        }
    }

    fun updateTaskDetails(task: TaskEntity, title: String, description: String?, dueDate: Long, priority: String) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority
            )
            dao.updateTask(updated)
            cancelAlarm(task.id)
            if (!updated.isCompleted) {
                scheduleAlarm(task.id, title, description, dueDate)
            }
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updated)
            if (updated.isCompleted) {
                cancelAlarm(task.id)
            } else {
                scheduleAlarm(task.id, task.title, task.description, task.dueDate)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.deleteTask(task)
            cancelAlarm(task.id)
        }
    }

    fun getSubtasks(taskId: Long): Flow<List<SubtaskEntity>> = dao.getSubtasksForTask(taskId)

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            dao.insertSubtask(SubtaskEntity(taskId = taskId, title = title))
        }
    }

    fun toggleSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            dao.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted))
        }
    }

    private fun scheduleAlarm(taskId: Long, title: String, desc: String?, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_TASK_ID", taskId)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_DESC", desc)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {}
    }

    private fun cancelAlarm(taskId: Long) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
