import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicStatusCalculationTest {
    private InMemoryTaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic("Epic 1", "Test epic");
        manager.createEpicTask(epic);
    }

    @Test
    void shouldReturnNewWhenAllSubtasksAreNew() {
        LocalDateTime now = LocalDateTime.now();
        Subtask sub1 = new Subtask(epic.getId(), "sub1", "desc1", Status.NEW, now, Duration.ofMinutes(30));
        Subtask sub2 = new Subtask(epic.getId(), "sub2", "desc2", Status.NEW, now.plusHours(1), Duration.ofMinutes(45));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpicTask(epic.getId()).getStatus(), "Все подзадачи — NEW, значит эпик должен быть NEW");
    }

    @Test
    void shouldReturnDoneWhenAllSubtasksAreDone() {
        LocalDateTime now = LocalDateTime.now();
        Subtask sub1 = new Subtask(epic.getId(), "sub1", "desc1", Status.DONE, now, Duration.ofMinutes(30));
        Subtask sub2 = new Subtask(epic.getId(), "sub2", "desc2", Status.DONE, now.plusHours(1), Duration.ofMinutes(45));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.DONE, manager.getEpicTask(epic.getId()).getStatus(), "Все подзадачи — DONE, значит эпик должен быть DONE");
    }

    @Test
    void shouldReturnInProgressWhenSubtasksHaveDifferentStatuses() {
        LocalDateTime now = LocalDateTime.now();
        Subtask sub1 = new Subtask(epic.getId(), "sub1", "desc1", Status.NEW, now, Duration.ofMinutes(30));
        Subtask sub2 = new Subtask(epic.getId(), "sub2", "desc2", Status.DONE, now.plusHours(1), Duration.ofMinutes(45));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus(), "NEW + DONE => эпик должен быть IN_PROGRESS");
    }

    @Test
    void shouldReturnInProgressWhenSubtasksAreInProgress() {
        LocalDateTime now = LocalDateTime.now();
        Subtask sub1 = new Subtask(epic.getId(), "sub1", "desc1", Status.IN_PROGRESS, now, Duration.ofMinutes(30));
        Subtask sub2 = new Subtask(epic.getId(), "sub2", "desc2", Status.IN_PROGRESS, now.plusHours(1), Duration.ofMinutes(45));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus(), "Все подзадачи — IN_PROGRESS => эпик должен быть IN_PROGRESS");
    }
}