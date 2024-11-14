package java.yandex.tracker.Model;

import Model.Task;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" + "\n" +
                "EpicId=" + epicId + "," +
                "java.yandex.practicum.Service.Status=" + super.getStatus() + "," +
                "Id=" + super.getId() + "," +
                "Name=" + super.getName() + "," +
                "Description=" + super.getDescription() + "," +
                "}" + "\n";
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getMainId() {
        return epicId;
    }
}