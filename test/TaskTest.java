package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Задача 1", "Описание задачи", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
    }

    @Test
    void shouldCreateTaskCorrectly() {
        assertEquals("Задача 1", task.getName());
        assertEquals("Описание задачи", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
        assertNotNull(task.getStartTime());
        assertEquals(Duration.ofHours(1), task.getDuration());
        assertEquals(task.getStartTime().plus(task.getDuration()), task.getEndTime());
    }

    @Test
    void shouldUpdateTaskFields() {
        task.setName("Новое имя");
        task.setDescription("Новое описание");
        task.setStatus(Status.DONE);
        task.setDuration(Duration.ofMinutes(90));

        assertEquals("Новое имя", task.getName());
        assertEquals("Новое описание", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
        assertEquals(Duration.ofMinutes(90), task.getDuration());
    }
}