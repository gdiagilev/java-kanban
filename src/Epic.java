import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask task) {
        if (!subtasks.contains(task)) {
            subtasks.add(task);
        } else {
            System.out.println("Задача уже существует.");
        }
    }

    public void removeSubtask(Subtask task) {
        if (subtasks == null) {
            System.out.println("Список задач пуст.");
        } else {
            if (subtasks.contains(task)) {
                subtasks.remove(task);
            } else {
                System.out.println("Задача не найдена.");
            }
        }
    }

    public void removeAllSubtasks() {
        if (subtasks == null) {
            System.out.println("Список задач пуст.");
        } else {
            subtasks.clear();
        }
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask getSubtask(int id) {
        Subtask subtask = null;
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).getId() == id) {
                subtask = subtasks.get(i);
                break;
            }
        }
        return subtask;
    }

    @Override
    public String toString() {
        return "Epic{" + "\n" +
                "id=" + getId() + "\n" +
                "status=" + getStatus() + "\n" +
                "name='" + getName() + "\n" +
                "description='" + getDescription() + "\n" +
                "subtask=" + subtasks + "\n" +
                "}";
    }
}
