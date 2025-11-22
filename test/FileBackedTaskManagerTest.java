import ru.yandex.tracker.Service.FileBackedTasksManager;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    protected FileBackedTasksManager createManager() {
        Path path = Paths.get("test_data.csv");
        return new FileBackedTasksManager(path);
    }
}