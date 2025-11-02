package ru.yandex.tracker;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        Subtask subtask1 = new Subtask(3, "Подзадача 1", "Подзадача 1 эпика 1", Status.NEW);
        Subtask subtask2 = new Subtask(3, "Подзадача 2", "Подзадача 2 эпика 1", Status.NEW);
        Subtask subtask3 = new Subtask(4, "Подзадача 1", "Подзадача 1 эпика 2", Status.NEW);

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpicTask(epic1);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createEpicTask(epic2);
        manager.createSubtask(subtask3);

        System.out.println("Созданные задачи:");
        System.out.println(manager.getAllTasks());
        System.out.println("Созданные эпики:");
        System.out.println(manager.getAllEpicTasks());
        System.out.println("Созданные подзадачи:");
        System.out.println(manager.getAllSubtasks());

        // ✅ Имитация просмотра задач пользователем
        System.out.println("\nПросматриваем задачи...");
        manager.getTask(task1.getId());
        manager.getEpicTask(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getEpicTask(epic2.getId());
        manager.getSubtask(subtask3.getId());

        // ✅ Теперь можно вывести историю просмотров
        System.out.println("\nИстория просмотров задач:");
        System.out.println(manager.getHistory());

        // ... (остальной код без изменений)
    }
}