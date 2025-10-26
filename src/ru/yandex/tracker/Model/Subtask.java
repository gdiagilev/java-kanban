package ru.yandex.tracker.Model;

public class Subtask extends Task {
    private int epicId;

    // Конструктор для создания новой подзадачи (id создаётся менеджером)
    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    // Конструктор для восстановления из файла (id задаётся вручную)
    public Subtask(int epicId, String name, String description, Status status, int id) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}
