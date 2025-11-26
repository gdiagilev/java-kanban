package ru.yandex.tracker;

import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest {

    private FileBackedTasksManager manager;
    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("tasks", ".csv");
        manager = new FileBackedTasksManager(tempFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldSaveAndLoadTask() {
        Task t = new Task(
                "Task1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );

        manager.createTask(t);

        // Создаём новый менеджер и загружаем из файла
        FileBackedTasksManager loadedManager = new FileBackedTasksManager(tempFile);
        Task loadedTask = loadedManager.getTask(t.getId());

        assertNotNull(loadedTask);
        assertEquals(t.getName(), loadedTask.getName());
    }

    @Test
    void shouldSaveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );
        manager.createEpicTask(epic);

        Subtask sub = new Subtask(
                epic.getId(),
                "Sub1",
                "SubDesc",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(15)
        );
        manager.createSubtask(sub);

        FileBackedTasksManager loadedManager = new FileBackedTasksManager(tempFile);
        Epic loadedEpic = loadedManager.getEpicTask(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(1, loadedManager.getSubtasksOfEpic(epic.getId()).size());
    }
}
