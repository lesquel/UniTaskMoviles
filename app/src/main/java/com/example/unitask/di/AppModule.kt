package com.example.unitask.di

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.unitask.data.repository.RoomSubjectRepository
import com.example.unitask.data.repository.RoomTaskRepository
import com.example.unitask.data.repository.RoomUserRepository
import com.example.unitask.data.repository.SharedPrefsNotificationRepository
import com.example.unitask.data.repository.SharedPrefsRewardRepository
import com.example.unitask.data.room.UniTaskDatabase
import com.example.unitask.data.supabase.SupabaseClientProvider
import com.example.unitask.data.supabase.SupabaseSubjectRepository
import com.example.unitask.data.supabase.SupabaseTaskRepository
import com.example.unitask.data.supabase.SupabaseUserRepository
import com.example.unitask.domain.repository.NotificationRepository
import com.example.unitask.domain.repository.RewardRepository
import com.example.unitask.domain.repository.SubjectRepository
import com.example.unitask.domain.repository.TaskRepository
import com.example.unitask.domain.repository.UserRepository
import com.example.unitask.domain.usecase.AddSubjectUseCase
import com.example.unitask.domain.usecase.AddTaskUseCase
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.CancelAlarmUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.DeleteSubjectUseCase
import com.example.unitask.domain.usecase.DeleteTaskUseCase
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
import com.example.unitask.presentation.viewmodel.AuthViewModel
import com.example.unitask.presentation.viewmodel.DashboardViewModel
import com.example.unitask.presentation.viewmodel.LeaderboardViewModel
import com.example.unitask.presentation.viewmodel.ProfileViewModel
import com.example.unitask.presentation.viewmodel.RewardsViewModel
import com.example.unitask.presentation.viewmodel.SubjectsViewModel
import java.time.LocalDateTime

/**
 * Contenedor de dependencias manual.
 * Usa Room como almacenamiento local con opción de sincronización con Supabase.
 */
object AppModule {

    private var _database: UniTaskDatabase? = null
    private var _appContext: Context? = null
    
    // Flag para saber si usamos Supabase o Room
    private var _useSupabase = false

    // Repositorios - pueden ser Supabase o Room
    private var _taskRepository: TaskRepository? = null
    private var _subjectRepository: SubjectRepository? = null
    private var _userRepository: UserRepository? = null
    
    // Otros repositorios
    private var _alarmScheduler: AlarmScheduler? = null
    private var _notificationRepository: NotificationRepository? = null
    private var _rewardRepository: RewardRepository? = null

    // Helper to access DB safely
    private val database: UniTaskDatabase
        get() = _database ?: throw IllegalStateException("AppModule not configured")

    // Repositorios
    private val taskRepository: TaskRepository
        get() = _taskRepository ?: throw IllegalStateException("TaskRepository not configured")
    
    private val subjectRepository: SubjectRepository
        get() = _subjectRepository ?: throw IllegalStateException("SubjectRepository not configured")
    
    private val userRepository: UserRepository
        get() = _userRepository ?: throw IllegalStateException("UserRepository not configured")

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
    
    private val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(taskRepository)
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

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val rewardRepo = provideRewardRepository()
            val notificationRepo = provideNotificationRepository()
            val userRepo = provideUserRepository()
            val awardXpUseCase = AwardXpUseCase(rewardRepo)
            val getAllNotificationsUseCase = GetAllNotificationsUseCase(notificationRepo)
            DashboardViewModel(
                getAllTasksUseCase = getAllTasksUseCase,
                getUrgentTasksUseCase = getUrgentTasksUseCase,
                getSubjectsUseCase = getSubjectsUseCase,
                getAllNotificationsUseCase = getAllNotificationsUseCase,
                completeTaskUseCase = completeTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                awardXpUseCase = awardXpUseCase,
                userRepository = userRepo
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
                deleteSubjectUseCase = deleteSubjectUseCase,
                userRepository = provideUserRepository()
            )
        }
    }

    fun addTaskViewModelFactory(initialTaskId: String?): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val notificationRepo = provideNotificationRepository()
            val userRepo = provideUserRepository()
            AddTaskViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addTaskUseCase = addTaskUseCase,
                updateTaskUseCase = updateTaskUseCase,
                getTaskByIdUseCase = getTaskByIdUseCase,
                scheduleAlarmUseCase = ScheduleAlarmUseCase(notificationRepo),
                userRepository = userRepo,
                nowProvider = { LocalDateTime.now() },
                initialTaskId = initialTaskId
            )
        }
    }

    fun authViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AuthViewModel(
                userRepository = provideUserRepository()
            )
        }
    }

    fun profileViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProfileViewModel(
                userRepository = provideUserRepository()
            )
        }
    }

    fun leaderboardViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LeaderboardViewModel(
                userRepository = provideUserRepository()
            )
        }
    }

    fun configureAppModule(context: Context) {
        _appContext = context.applicationContext
        
        Log.d("AppModule", "Configurando AppModule...")

        // Initialize Room Database primero (siempre necesario)
        if (_database == null) {
            try {
                _database = Room.databaseBuilder(
                    context.applicationContext,
                    UniTaskDatabase::class.java,
                    "unitask_database"
                )
                .fallbackToDestructiveMigration() // Reset DB si hay problemas de migración
                .build()
                Log.d("AppModule", "Room Database inicializada correctamente")
            } catch (e: Exception) {
                Log.e("AppModule", "Error inicializando Room Database", e)
            }
        }

        // Por ahora SIEMPRE usar Room - Supabase deshabilitado temporalmente
        // hasta que se resuelvan los problemas de dependencias
        _useSupabase = false
        Log.d("AppModule", "Usando Room (almacenamiento local)")
        
        // Configurar repositorios de Room
        configureRoomRepositories()
        Log.d("AppModule", "Repositorios de Room configurados")

        // Inicializar servicios de alarmas
        if (_alarmScheduler == null) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val wrapper: AlarmManagerWrapper = RealAlarmManagerWrapper(context.applicationContext, alarmManager)
                _alarmScheduler = AlarmScheduler(wrapper)
            } catch (e: Exception) {
                Log.e("AppModule", "Error inicializando AlarmScheduler", e)
            }
        }
        
        if (_notificationRepository == null && _appContext != null && _alarmScheduler != null) {
            _notificationRepository = SharedPrefsNotificationRepository(_appContext!!, _alarmScheduler!!)
        }
        
        if (_rewardRepository == null && _appContext != null) {
            _rewardRepository = SharedPrefsRewardRepository(_appContext!!)
        }
        
        Log.d("AppModule", "AppModule configurado exitosamente (Supabase: $_useSupabase)")
    }
    
    private fun configureSupabaseRepositories() {
        // User Repository
        if (_userRepository == null) {
            _userRepository = SupabaseUserRepository()
        }
        
        // Task Repository
        if (_taskRepository == null) {
            _taskRepository = SupabaseTaskRepository()
        }
        
        // Subject Repository
        if (_subjectRepository == null) {
            val supabaseUserRepo = _userRepository as? SupabaseUserRepository
            _subjectRepository = SupabaseSubjectRepository(
                getCurrentUserId = { supabaseUserRepo?.getCurrentUser()?.id }
            )
        }
    }
    
    private fun configureRoomRepositories() {
        val db = _database ?: return
        
        // User Repository
        if (_userRepository == null) {
            _userRepository = RoomUserRepository(db.userDao, db.subjectDao)
        }
        
        // Task Repository
        if (_taskRepository == null) {
            _taskRepository = RoomTaskRepository(db.taskDao)
        }
        
        // Subject Repository  
        if (_subjectRepository == null) {
            _subjectRepository = RoomSubjectRepository(db.subjectDao)
        }
    }

    fun provideNotificationRepository(): NotificationRepository {
        return _notificationRepository
            ?: throw IllegalStateException("AppModule not configured")
    }

    fun provideRewardRepository(): RewardRepository {
        return _rewardRepository
            ?: throw IllegalStateException("AppModule not configured")
    }

    fun provideUserRepository(): UserRepository {
        return _userRepository
            ?: throw IllegalStateException("AppModule not configured")
    }
    
    fun provideTaskRepository(): TaskRepository {
        return _taskRepository
            ?: throw IllegalStateException("AppModule not configured")
    }
    
    fun provideSubjectRepository(): SubjectRepository {
        return _subjectRepository
            ?: throw IllegalStateException("AppModule not configured")
    }
    
    /**
     * Indica si estamos usando Supabase o almacenamiento local.
     */
    fun isUsingSupabase(): Boolean = _useSupabase
    
    /**
     * Refresca todos los datos desde Supabase (si está habilitado).
     */
    suspend fun refreshAllData() {
        if (!_useSupabase) return
        
        Log.d("AppModule", "Refreshing all data from Supabase...")
        try {
            (_taskRepository as? SupabaseTaskRepository)?.refreshAllTasks()
            (_subjectRepository as? SupabaseSubjectRepository)?.refreshSubjects()
        } catch (e: Exception) {
            Log.e("AppModule", "Error refreshing data from Supabase", e)
        }
    }
}
