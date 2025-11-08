import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldCreateAndGetTaskById() {
        Task task = new Task(
                "Task 1",
                "Description 1",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );
        manager.createTask(task);
        Task saved = manager.getTask(task.getId());
        assertNotNull(saved);
        assertEquals(task, saved);
    }

    @Test
    void shouldReturnNewStatusWhenAllSubtasksAreNew() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.NEW, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnDoneStatusWhenAllSubtasksAreDone() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc", Status.DONE,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc", Status.DONE,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.DONE, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnInProgressStatusWhenSubtasksNewAndDone() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc", Status.DONE,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnInProgressStatusWhenAllSubtasksInProgress() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc", Status.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc", Status.IN_PROGRESS,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTasksOverlap() {
        Task task1 = new Task(
                "Task 1",
                "Desc 1",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );

        Task task2 = new Task(
                "Task 2",
                "Desc 2",
                Status.NEW,
                LocalDateTime.now().plusMinutes(15),
                Duration.ofMinutes(30)
        );

        manager.createTask(task1);
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2),
                "Ожидается исключение при пересечении времён");
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldAddAndRetrieveHistory() {
        Task task = new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
        manager.createTask(task);
        manager.getTask(task.getId());
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldNotDuplicateHistoryEntries() {
        Task task = new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
        manager.createTask(task);
        manager.getTask(task.getId());
        manager.getTask(task.getId());
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }
}