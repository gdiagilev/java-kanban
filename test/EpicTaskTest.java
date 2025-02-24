import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.Managers;
import ru.yandex.tracker.Service.InMemoryHistoryManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EpicTaskTest {
    Managers managers = new Managers();
    InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();

    @BeforeEach
    void init() {
        Managers managers = new Managers();
        InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();
    }

    @Test
    void addSubtask() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Assertions.assertNotNull(subtask1, "Подзадача не добавлена");
    }

    @Test
    void removeSubtask() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);

        Subtask subtask1 = new Subtask(epicTask1.getId(), "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);

        int subtaskId = subtask1.getId(); // Получаем правильный ID
        manager.deleteSubtask(subtask1);

        Assertions.assertNull(manager.getSubtask(subtaskId), "Подзадача не удалена");
    }

    @Test
    void removeAllSubtasks() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(1, "Подзадача 2",  "Подзадача эпика 2", Status.NEW);
        manager.createSubtask(subtask2);
        manager.deleteAllSubtasks();
        Assertions.assertNull(manager.getSubtask(2),   "Подзадача не удалена");
        Assertions.assertNull(manager.getSubtask(3),    "Подзадача не удалена");
    }

    @Test
    void getSubTasks() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(1, "Подзадача 2",  "Подзадача эпика 2", Status.NEW);
        manager.createSubtask(subtask2);
        List<Subtask> subTasks  = new ArrayList<>();
        subTasks.add(subtask1);
        subTasks.add(subtask2);
        List<Subtask> actualSubTasks = manager.getAllSubtasks();
        assertArrayEquals(actualSubTasks.toArray(), subTasks.toArray());
    }

    @Test
    void addAndRemoveInMemoryHistoryManager() {
        InMemoryHistoryManager historyManager = (InMemoryHistoryManager) Managers.getDefaultHistory();
        Epic task1 = new Epic("Epic1", "Task1");
        Epic task2 = new Epic("Epic2", "Task2");
        Epic task3 = new Epic("Epic3", "Task3");
        manager.createEpicTask(task1);
        manager.createEpicTask(task2);
        manager.createEpicTask(task3);

        List<Task> history = new ArrayList<>();
        history.add(task1);
        history.add(task2);
        history.add(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> historyToCompare = historyManager.getHistory();
        Assertions.assertEquals(history, historyToCompare);

        historyManager.remove(1);
        history.remove(task1);
        historyToCompare = historyManager.getHistory();
        Assertions.assertEquals(history, historyToCompare);
    }
}
