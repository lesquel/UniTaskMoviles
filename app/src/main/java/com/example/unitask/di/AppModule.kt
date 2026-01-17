package com.example.unitask.di

import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.unitask.data.repository.RoomSubjectRepository
import com.example.unitask.data.repository.RoomTaskRepository
import com.example.unitask.data.repository.SampleData
import com.example.unitask.data.repository.SharedPrefsNotificationRepository
import com.example.unitask.data.repository.SharedPrefsRewardRepository
import com.example.unitask.data.room.UniTaskDatabase
import com.example.unitask.domain.repository.NotificationRepository
import com.example.unitask.domain.repository.RewardRepository
import com.example.unitask.domain.repository.SubjectRepository
import com.example.unitask.domain.repository.TaskRepository
import com.example.unitask.domain.usecase.AddSubjectUseCase
import com.example.unitask.domain.usecase.AddTaskUseCase
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.CancelAlarmUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.DeleteSubjectUseCase
import com.example.unitask.domain.usecase.EditSubjectUseCase
import com.example.unitask.domain.usecase.GetAllNotificationsUseCase
import com.example.unitask.domain.usecase.GetAllTasksUseCase
import com.example.unitask.domain.usecase.GetLevelUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetTaskByIdUseCase
import com.example.unitask.domain.usecase.GetUrgentTasksUseCase
import com.example.unitask.domain.usecase.GetXpUseCase
import com.example.unitask.domain.usecase.ScheduleAlarmUseCase
import com.example.unitask.domain.usecase.UpdateTaskUseCase
import com.example.unitask.notifications.AlarmManagerWrapper
import com.example.unitask.notifications.AlarmScheduler
import com.example.unitask.notifications.RealAlarmManagerWrapper
import com.example.unitask.presentation.viewmodel.AddTaskViewModel
import com.example.unitask.presentation.viewmodel.AlarmViewModel
import com.example.unitask.presentation.viewmodel.DashboardViewModel
import com.example.unitask.presentation.viewmodel.RewardsViewModel
import com.example.unitask.presentation.viewmodel.SubjectsViewModel
import java.time.LocalDateTime

/**
 * Simple manual dependency container. Replace with Hilt/Dagger when ready.
 */
object AppModule {

    private var _database: UniTaskDatabase? = null

    // Helper to access DB safely
    private val database: UniTaskDatabase
        get() = _database ?: throw IllegalStateException("AppModule not configured: call configureAppModule(context)")

    // Data sources
    private val subjectRepository: SubjectRepository by lazy {
        RoomSubjectRepository(database.subjectDao)
    }

    private val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(database.taskDao)
    }

    // Use cases
    private val getAllTasksUseCase: GetAllTasksUseCase by lazy {
        GetAllTasksUseCase(taskRepository)
    }

    private val getUrgentTasksUseCase: GetUrgentTasksUseCase by lazy {
        GetUrgentTasksUseCase(taskRepository)
    }

    private val addTaskUseCase: AddTaskUseCase by lazy {
        AddTaskUseCase(taskRepository, subjectRepository)
    }

    private val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(taskRepository)
    }

    private val getTaskByIdUseCase: GetTaskByIdUseCase by lazy {
        GetTaskByIdUseCase(taskRepository)
    }

    private val completeTaskUseCase: CompleteTaskUseCase by lazy {
        CompleteTaskUseCase(taskRepository)
    }

    private val getSubjectsUseCase: GetSubjectsUseCase by lazy {
        GetSubjectsUseCase(subjectRepository)
    }

    private val addSubjectUseCase: AddSubjectUseCase by lazy {
        AddSubjectUseCase(subjectRepository)
    }

    private val editSubjectUseCase: EditSubjectUseCase by lazy {
        EditSubjectUseCase(subjectRepository)
    }

    private val deleteSubjectUseCase: DeleteSubjectUseCase by lazy {
        DeleteSubjectUseCase(subjectRepository, taskRepository)
    }

    private var _alarmScheduler: AlarmScheduler? = null
    private var _notificationRepository: NotificationRepository? = null
    private var _rewardRepository: RewardRepository? = null
    private var _appContext: Context? = null

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val rewardRepo = provideRewardRepository()
            val notificationRepo = provideNotificationRepository()
            val awardXpUseCase = AwardXpUseCase(rewardRepo)
            val getAllNotificationsUseCase = GetAllNotificationsUseCase(notificationRepo)
            DashboardViewModel(
                getAllTasksUseCase = getAllTasksUseCase,
                getUrgentTasksUseCase = getUrgentTasksUseCase,
                getSubjectsUseCase = getSubjectsUseCase,
                getAllNotificationsUseCase = getAllNotificationsUseCase,
                completeTaskUseCase = completeTaskUseCase,
                awardXpUseCase = awardXpUseCase
            )
        }
        initializer {
            val notificationRepo = provideNotificationRepository()
            val scheduleUseCase = ScheduleAlarmUseCase(notificationRepo)
            val cancelUseCase = CancelAlarmUseCase(notificationRepo)
            val getAllNotificationsUseCase = GetAllNotificationsUseCase(notificationRepo)
            AlarmViewModel(
                scheduleUseCase,
                cancelUseCase,
                getAllNotificationsUseCase
            )
        }
        initializer {
            val rewardRepo = provideRewardRepository()
            RewardsViewModel(
                awardXpUseCase = AwardXpUseCase(rewardRepo),
                getXpUseCase = GetXpUseCase(rewardRepo),
                getLevelUseCase = GetLevelUseCase(rewardRepo)
            )
        }
        initializer {
            SubjectsViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addSubjectUseCase = addSubjectUseCase,
                editSubjectUseCase = editSubjectUseCase,
                deleteSubjectUseCase = deleteSubjectUseCase
            )
        }
    }

    fun addTaskViewModelFactory(initialTaskId: String?): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AddTaskViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addTaskUseCase = addTaskUseCase,
                updateTaskUseCase = updateTaskUseCase,
                getTaskByIdUseCase = getTaskByIdUseCase,
                nowProvider = { LocalDateTime.now() },
                initialTaskId = initialTaskId
            )
        }
    }

    fun configureAppModule(context: Context) {
        _appContext = context.applicationContext

        // Initialize Room Database
        if (_database == null) {
            _database = Room.databaseBuilder(
                context.applicationContext,
                UniTaskDatabase::class.java,
                "unitask_database"
            ).build()
        }

        // Lazily create shared services so they survive the app lifecycle.
        if (_alarmScheduler == null) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val wrapper: AlarmManagerWrapper = RealAlarmManagerWrapper(alarmManager)
            _alarmScheduler = AlarmScheduler(wrapper)
        }
        if (_notificationRepository == null) {
            val ctx = _appContext
                ?: throw IllegalStateException("AppModule context not set")
            val scheduler = _alarmScheduler
                ?: throw IllegalStateException("AlarmScheduler not initialized")
            _notificationRepository = SharedPrefsNotificationRepository(ctx, scheduler)
        }
        if (_rewardRepository == null) {
            val ctx = _appContext
                ?: throw IllegalStateException("AppModule context not set")
            _rewardRepository = SharedPrefsRewardRepository(ctx)
        }
    }

    fun provideNotificationRepository(): NotificationRepository {
        return _notificationRepository
            ?: throw IllegalStateException("AppModule not configured: call configureAppModule(context)")
    }

    fun provideRewardRepository(): RewardRepository {
        return _rewardRepository
            ?: throw IllegalStateException("AppModule not configured: call configureAppModule(context)")
    }
}
