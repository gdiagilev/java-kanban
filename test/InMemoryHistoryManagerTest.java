import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.HistoryManager;
import ru.yandex.tracker.Service.Managers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();

        task1 = new Task("Task 1", "Description 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));

        task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS,
                LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(30));

        task3 = new Task("Task 3", "Description 3", Status.DONE,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(45));
    }

    @Test
    void shouldAddAndReturnHistoryInCorrectOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три задачи");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2");
        assertEquals(task3, history.get(2), "Третья задача должна быть task3");
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История не должна содержать дубликаты");
        assertEquals(task2, history.get(0), "После повторного добавления task1 должна переместиться в конец");
        assertEquals(task1, history.get(1), "Task1 должна быть последней");
    }

    @Test
    void shouldRemoveTasksFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления должно остаться 1 задание");
        assertEquals(task2, history.get(0), "В истории должна остаться только task2");
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после инициализации");
    }

    @Test
    void shouldNotThrowExceptionOnRemovingFromEmptyHistory() {
        assertDoesNotThrow(() -> historyManager.remove(999), "Удаление несуществующей задачи не должно вызывать ошибку");
    }
}