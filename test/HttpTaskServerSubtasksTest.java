import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerSubtasksTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );
        manager.createEpicTask(epic);
    }

    @Test
    void shouldCreateSubtaskCorrectly() {
        Subtask sub = new Subtask(
                epic.getId(),
                "Sub1",
                "SubDesc",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(30)
        );

        manager.createSubtask(sub);

        Subtask retrieved = manager.getSubtask(sub.getId());
        assertNotNull(retrieved);
        assertEquals(sub.getName(), retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }
}