import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.HistoryManager;
import ru.yandex.tracker.Service.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();

        task1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task1.setId(1);

        task2 = new Task("Task 2", "Desc 2", Status.IN_PROGRESS, LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(30));
        task2.setId(2);

        task3 = new Task("Task 3", "Desc 3", Status.DONE, LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(45));
        task3.setId(3);
    }

    @Test
    void shouldAddAndReturnHistoryInCorrectOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 задачи");
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Повторяющиеся задачи не должны дублироваться в истории");
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void shouldRemoveTasksFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления должно остаться только 1 задание");
        assertEquals(task2, history.get(0));
    }
}