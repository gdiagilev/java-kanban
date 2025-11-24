import ru.yandex.tracker.Service.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}