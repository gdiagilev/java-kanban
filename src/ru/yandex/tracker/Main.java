package ru.yandex.tracker;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        // Создание эпиков
        Epic epic1 = new Epic(
                "Эпик 1",
                "Описание эпика 1",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofHours(5)
        );

        Epic epic2 = new Epic(
                "Эпик 2",
                "Описание эпика 2",
                Status.NEW,
                LocalDateTime.now().plusDays(1),
                Duration.ofHours(3)
        );

        manager.createEpicTask(epic1);
        manager.createEpicTask(epic2);

        // Создание подзадач
        Subtask sub1 = new Subtask(
                epic1.getId(),
                "Подзадача 1",
                "Подзадача 1 эпика 1",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofHours(2)
        );

        Subtask sub2 = new Subtask(
                epic1.getId(),
                "Подзадача 2",
                "Подзадача 2 эпика 1",
                Status.NEW,
                LocalDateTime.now().plusHours(2),
                Duration.ofHours(1)
        );

        Subtask sub3 = new Subtask(
                epic2.getId(),
                "Подзадача 1",
                "Подзадача 1 эпика 2",
                Status.NEW,
                LocalDateTime.now().plusDays(1),
                Duration.ofHours(1)
        );

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);
        manager.createSubtask(sub3);

        // Проверка
        System.out.println("Все эпики:");
        for (Epic e : manager.getAllEpicTasks()) {
            System.out.println(e.getName() + " | Статус: " + e.getStatus());
        }

        System.out.println("\nВсе подзадачи эпика 1:");
        for (Subtask s : manager.getSubtasksOfEpic(epic1.getId())) {
            System.out.println(s.getName() + " | Статус: " + s.getStatus());
        }
    }
}