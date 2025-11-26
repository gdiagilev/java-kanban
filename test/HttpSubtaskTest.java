package ru.yandex.tracker;

import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpSubtaskTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic1",
                "Description Epic",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(0)
        );
        manager.createEpicTask(epic);
    }

    @Test
    void shouldCreateSubtaskCorrectly() {
        Subtask subtask = new Subtask(
                epic.getId(),
                "Sub1",
                "SubDesc",
                Status.NEW,
                LocalDateTime.now().plusMinutes(5),
                Duration.ofMinutes(15)
        );

        manager.createSubtask(subtask);

        Subtask saved = manager.getSubtask(subtask.getId());
        assertNotNull(saved);
        assertEquals("Sub1", saved.getName());
        assertEquals(epic.getId(), saved.getEpicId());
    }

    @Test
    void shouldGetSubtasksOfEpic() {
        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "D1", Status.NEW,
                LocalDateTime.now().plusMinutes(1), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "D2", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(5));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getSubtasksOfEpic(epic.getId()).size());
    }
}