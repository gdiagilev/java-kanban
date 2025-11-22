import ru.yandex.tracker.Service.FileBackedTaskManager;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        Path path = Paths.get("test_data.csv");
        return new FileBackedTaskManager(path);
    }
}