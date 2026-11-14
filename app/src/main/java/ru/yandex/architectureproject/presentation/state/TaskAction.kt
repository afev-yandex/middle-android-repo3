package ru.yandex.architectureproject.presentation.state

sealed class TaskAction {
    // TODO: Здесь должны быть действия (загрузка заданий, добавление задания, обновление статуса задания (сделано/не сделано) и удаление задания)
    data object LoadTasks: TaskAction()
    class AddTask(val task: String): TaskAction()
    class UpdateTaskStatus(val id: Int, val isDone: Boolean): TaskAction()
    class DeleteTask(val id: Int): TaskAction()
}
