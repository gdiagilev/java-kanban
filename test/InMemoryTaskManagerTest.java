import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.TaskManagerTest;

public class InMemoryTaskManagerTest extends TaskManagerTest {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}