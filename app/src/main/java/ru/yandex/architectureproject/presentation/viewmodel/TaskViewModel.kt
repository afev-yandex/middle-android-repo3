package ru.yandex.architectureproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.yandex.architectureproject.domain.AddTaskUseCase
import ru.yandex.architectureproject.domain.CompleteTaskUseCase
import ru.yandex.architectureproject.domain.DeleteTaskUseCase
import ru.yandex.architectureproject.domain.GetAllTasksUseCase
import ru.yandex.architectureproject.domain.IncompleteTaskUseCase
import ru.yandex.architectureproject.presentation.state.TaskAction
import ru.yandex.architectureproject.presentation.state.TaskState

class TaskViewModel(
    private val addTaskUseCase: AddTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val incompleteTaskUseCase: IncompleteTaskUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow<TaskState>(TaskState.Loading)
    val state: StateFlow<TaskState> = _state.asStateFlow()
    val completeJobs: MutableMap<Int, Job?> = mutableMapOf()

    init {
        reduce(TaskAction.LoadTasks)
    }

    fun reduce(action: TaskAction) {
        // TODO: Здесь должна быть обработка действий
        viewModelScope.launch {
            when (action) {

                TaskAction.LoadTasks -> loadTasks()

                is TaskAction.AddTask -> addTaskUseCase(action.task)

                is TaskAction.DeleteTask -> {
                    completeJobs.remove(action.id)?.cancel() // чтобы избежать удаления удаленного
                    deleteTaskUseCase(action.id)
                }

                is TaskAction.UpdateTaskStatus -> {

                    if (action.isDone) {

                        completeJobs[action.id]?.cancel()

                        val job = launch { // отдельная задача
                            try {
                                completeTaskUseCase(action.id)
                            } finally {
                                completeJobs.remove(action.id)
                            }
                        }

                        completeJobs[action.id] = job

                    } else {

                        completeJobs.remove(action.id)?.cancel()
                        incompleteTaskUseCase(action.id)

                    }

                } // if

            } // when
        }
    }

    private suspend fun loadTasks() {
        withContext(ioDispatcher) {
            getAllTasksUseCase()
                .distinctUntilChanged()
                .onStart { _state.value = TaskState.Loading }
                .catch { e -> _state.value = TaskState.Error(e.message ?: "Ошибка загрузки") }
                .collect { tasks -> _state.value = TaskState.Loaded(tasks) }
        }
    }
}
