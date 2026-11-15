package ru.yandex.architectureproject.domain

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import ru.yandex.architectureproject.data.repository.TaskRepository
import kotlin.time.Duration.Companion.seconds

class CompleteTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: Int) {
        repository.completeTask(taskId)
        // TODO: Здесь будет автоудаление задачи
        delay(10.seconds)
        currentCoroutineContext().ensureActive() // должен бросить CancellationException, если job отменен
        repository.deleteTask(taskId)
    }
}
