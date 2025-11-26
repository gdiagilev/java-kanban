import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest {

    protected InMemoryTaskManager manager;
    protected Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic("Epic 1", "Test epic");
        manager.createEpicTask(epic);
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Task 1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        Task fetched = manager.getTask(task.getId());
        assertNotNull(fetched);
        assertEquals(task.getName(), fetched.getName());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Task 1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        task.setName("Updated Task");
        manager.updateTask(task);

        Task fetched = manager.getTask(task.getId());
        assertEquals("Updated Task", fetched.getName());
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Task 1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);

        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic fetched = manager.getEpicTask(epic.getId());
        assertNotNull(fetched);
        assertEquals(epic.getName(), fetched.getName());
    }

    @Test
    void shouldUpdateEpic() {
        epic.setName("Updated Epic");
        manager.updateEpicTask(epic);

        Epic fetched = manager.getEpicTask(epic.getId());
        assertEquals("Updated Epic", fetched.getName());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Subtask sub = new Subtask(epic.getId(), "sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(sub);

        manager.deleteEpicTask(epic.getId());
        assertNull(manager.getEpicTask(epic.getId()));
        assertNull(manager.getSubtask(sub.getId()));
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Subtask sub = new Subtask(epic.getId(), "sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(sub);

        Subtask fetched = manager.getSubtask(sub.getId());
        assertNotNull(fetched);
        assertEquals(sub.getName(), fetched.getName());
    }

    @Test
    void shouldUpdateSubtaskAndEpicStatus() {
        Subtask sub = new Subtask(epic.getId(), "sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(sub);

        sub.setStatus(Status.DONE);
        manager.updateSubtask(sub);

        assertEquals(Status.DONE, manager.getSubtask(sub.getId()).getStatus());
        assertEquals(Status.DONE, manager.getEpicTask(epic.getId()).getStatus());
    }

    @Test
    void shouldDeleteSubtaskAndUpdateEpic() {
        Subtask sub = new Subtask(epic.getId(), "sub", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createSubtask(sub);

        manager.deleteSubtask(sub.getId());

        assertNull(manager.getSubtask(sub.getId()));
        assertEquals(Status.NEW, manager.getEpicTask(epic.getId()).getStatus());
        assertTrue(manager.getSubtasksOfEpic(epic.getId()).isEmpty());
    }

    @Test
    void historyShouldTrackAccessedTasks() {
        Task task1 = new Task("Task 1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "desc", Status.NEW,
                LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(30));
        Subtask sub = new Subtask(epic.getId(), "sub", "desc", Status.NEW,
                LocalDateTime.now().plusMinutes(80), Duration.ofMinutes(30));

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createSubtask(sub);

        manager.getTask(task1.getId());
        manager.getEpicTask(epic.getId());
        manager.getSubtask(sub.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(sub));
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        Task task1 = new Task("Task 1", "desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "desc", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(task1, prioritized.get(0));
        assertEquals(task2, prioritized.get(1));
    }

    protected abstract TaskManager createManager();
}