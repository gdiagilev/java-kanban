import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.tracker.Service.FileBackedTasksManager;
import ru.yandex.tracker.Service.TaskManager;
import ru.yandex.tracker.TaskManagerTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTasksManagerTest extends TaskManagerTest {

    private Path testFile;

    @BeforeEach
    void setUpTestFile() throws IOException {
        testFile = Files.createTempFile("kanban_test_", ".csv");
    }

    @AfterEach
    void cleanTestFile() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Override
    protected TaskManager createManager() {
        return new FileBackedTasksManager(testFile);
    }
}