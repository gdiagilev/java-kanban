public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Subtask subtask1 = new Subtask(3, "Подзадача 1", "Подзадача 1 эпика 1", Status.NEW);
        Subtask subtask2 = new Subtask(3, "Подзадача 2", "Подзадача 2 эпика 1", Status.NEW);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        Subtask subtask3 = new Subtask(4, "Подзадача 1", "Подзадача 1 эпика 2", Status.NEW);

        TaskManager manager = new TaskManager();
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpicTask(epic1);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);
        manager.createEpicTask(epic2);

        System.out.println("Созданные задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Созданные эпики:");
        System.out.println(manager.getAllEpicTasks());
        System.out.println("Созданные подзадачи:");
        System.out.println(manager.getAllSubtasks());

        task1.update("Задача 1.1", "Описание задачи 1.1", Status.IN_PROGRESS);
        task2.update("Задача 2.1", "Описание задачи 2.1", Status.DONE);
        manager.updateTask(task1);
        manager.updateTask(task2);
        System.out.println("Обновленные задачи:");
        System.out.println(manager.getAllTasks());

        subtask1.update("Подзадача 1.1", "Подзадача эпика 1.1", Status.IN_PROGRESS);
        subtask2.update("Подзадача 2.1", "Подзадача эпика 1.1", Status.DONE);
        subtask3.update("Подзадача 3.1", "Подзадача эпика 1.1", Status.IN_PROGRESS);

        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        manager.updateSubtask(subtask3);
        System.out.println("getAllSubTasksByEpicId");
        System.out.println(manager.getAllSubTasksByEpicId(epic1.getId()));
        manager.deleteAllSubTasksInEpic(3);
        System.out.println("deleteAllSubTasksInEpic");
        System.out.println(manager.getAllSubTasksByEpicId(epic1.getId()));

        System.out.println("Обновлённые задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Обновлённые эпики:");
        System.out.println(manager.getAllEpicTasks());
        System.out.println("Обновлённые подзадачи:");
        System.out.println(manager.getAllSubtasks());

        manager.deleteTask(task1);
        manager.deleteEpicTask(epic2);
        manager.deleteSubtask(subtask3);

        System.out.println("Финальные задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Финальные эпики:");
        System.out.println(manager.getAllEpicTasks());
        System.out.println("Финальные подзадачи:");
        System.out.println(manager.getAllSubtasks());

        manager.deleteAllSubtasks();
        System.out.println("В результате удаления подзадач:");
        System.out.println(manager.getAllEpicTasks());
        System.out.println(manager.getAllSubtasks());
    }
}