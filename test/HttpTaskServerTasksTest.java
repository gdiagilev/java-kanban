import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTasksTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );
        manager.createEpicTask(epic);
    }

    @Test
    void shouldCreateSubtasksAndCheckTime() {
        Subtask sub1 = new Subtask(
                epic.getId(),
                "Sub1",
                "D1",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(15)
        );
        Subtask sub2 = new Subtask(
                epic.getId(),
                "Sub2",
                "D2",
                Status.NEW,
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(10)
        );

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getSubtasksOfEpic(epic.getId()).size());
    }

    @Test
    void shouldCreateTaskAndCheckPrioritizedTasksOrder() {
        Task t1 = new Task(
                "T1",
                "Desc1",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );

        Task t2 = new Task(
                "T2",
                "Desc2",
                Status.NEW,
                t1.getEndTime().plusMinutes(1),
                Duration.ofMinutes(15)
        );

        manager.createTask(t1);
        manager.createTask(t2);

        assertEquals(t1.getId(), manager.getPrioritizedTasks().get(0).getId());
        assertEquals(t2.getId(), manager.getPrioritizedTasks().get(1).getId());
    }
}