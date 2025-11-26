package ru.yandex.tracker;

import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusCalculationTest {

    private InMemoryTaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic 1",
                "Test epic",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );
        manager.createEpicTask(epic);
    }

    @Test
    void shouldReturnNewWhenAllSubtasksAreNew() {
        Subtask sub1 = new Subtask(
                epic.getId(),
                "Sub1",
                "D1",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(10)
        );
        manager.createSubtask(sub1);
        assertEquals(Status.NEW, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnInProgressWhenSubtasksHaveDifferentStatuses() {
        Subtask sub1 = new Subtask(
                epic.getId(),
                "Sub1",
                "D1",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(10)
        );
        Subtask sub2 = new Subtask(
                epic.getId(),
                "Sub2",
                "D2",
                Status.DONE,
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)
        );

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnDoneWhenAllSubtasksAreDone() {
        Subtask sub1 = new Subtask(
                epic.getId(),
                "Sub1",
                "D1",
                Status.DONE,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(10)
        );
        Subtask sub2 = new Subtask(
                epic.getId(),
                "Sub2",
                "D2",
                Status.DONE,
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(15)
        );

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.DONE, manager.getEpicTask(epic.getId()).getStatus());
    }
}
