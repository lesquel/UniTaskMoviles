package com.example.unitask.di

import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.unitask.data.repository.InMemorySubjectRepository
import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.data.repository.SampleData
import com.example.unitask.data.repository.SharedPrefsNotificationRepository
import com.example.unitask.data.repository.SharedPrefsRewardRepository
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
import com.example.unitask.domain.usecase.GetUrgentTasksUseCase
import com.example.unitask.domain.usecase.GetXpUseCase
import com.example.unitask.domain.usecase.ScheduleAlarmUseCase
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

    // Data sources
    private val subjectRepository: SubjectRepository by lazy {
        InMemorySubjectRepository(SampleData.subjects())
    }

    private val taskRepository: TaskRepository by lazy {
        InMemoryTaskRepository(SampleData.tasks())
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
            val awardXpUseCase = AwardXpUseCase(rewardRepo)
            DashboardViewModel(
                getAllTasksUseCase = getAllTasksUseCase,
                getUrgentTasksUseCase = getUrgentTasksUseCase,
                getSubjectsUseCase = getSubjectsUseCase,
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
                scheduleUseCase = scheduleUseCase,
                cancelUseCase = cancelUseCase,
                getAllNotificationsUseCase = getAllNotificationsUseCase
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
            AddTaskViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addTaskUseCase = addTaskUseCase,
                nowProvider = { LocalDateTime.now() }
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

    fun configureAppModule(context: Context) {
        _appContext = context.applicationContext
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
