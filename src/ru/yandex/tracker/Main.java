package ru.yandex.tracker;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Обычные задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        manager.createTask(task1);
        manager.createTask(task2);

        // Эпики
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpicTask(epic1);
        manager.createEpicTask(epic2);

        // Подзадачи без пересечения
        Subtask sub1 = new Subtask(epic1.getId(), "Подзадача 1", "Подзадача 1 эпика 1",
                Status.NEW, LocalDateTime.now().plusMinutes(10), Duration.ofMinutes(20));
        Subtask sub2 = new Subtask(epic1.getId(), "Подзадача 2", "Подзадача 2 эпика 1",
                Status.NEW, sub1.getEndTime().plusMinutes(5), Duration.ofMinutes(30));
        Subtask sub3 = new Subtask(epic2.getId(), "Подзадача 1", "Подзадача 1 эпика 2",
                Status.NEW, sub2.getEndTime().plusMinutes(5), Duration.ofMinutes(25));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);
        manager.createSubtask(sub3);

        // Вывод всех задач
        System.out.println("\nВсе задачи:");
        manager.getAllTasks().forEach(System.out::println);
        System.out.println("\nЭпики:");
        manager.getAllEpicTasks().forEach(System.out::println);
        System.out.println("\nПодзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        // Просмотр и история
        manager.getTask(task1.getId());
        manager.getEpicTask(epic1.getId());
        manager.getSubtask(sub1.getId());
        manager.getEpicTask(epic2.getId());
        manager.getSubtask(sub3.getId());

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(System.out::println);

        // Статусы эпиков
        System.out.println("\nСтатусы эпиков:");
        manager.getAllEpicTasks().forEach(epic ->
                System.out.println(epic.getName() + " : " + epic.getStatus()));

        // Приоритетный список задач
        System.out.println("\nПриоритетный список задач:");
        manager.getPrioritizedTasks().forEach(System.out::println);
    }
}