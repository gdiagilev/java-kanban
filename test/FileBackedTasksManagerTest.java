import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.tracker.Service.FileBackedTasksManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private Path testFile;

    @BeforeEach
    void setUpTestFile() throws IOException {
        testFile = Files.createTempFile("kanban_test_", ".csv");
    }

    @AfterEach
    void clean() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Override
    protected FileBackedTasksManager createManager() {
        return new FileBackedTasksManager(testFile);
    }
}