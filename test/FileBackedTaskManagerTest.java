import ru.yandex.tracker.Service.FileBackedTaskManager;
import java.io.File;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createManager() {
        return new FileBackedTaskManager(new File("test_data.csv"));
    }
}