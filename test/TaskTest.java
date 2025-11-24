import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Task 1", "Description", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
    }

    @Test
    void shouldCreateTaskCorrectly() {
        assertEquals("Task 1", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
        assertNotNull(task.getStartTime());
        assertEquals(Duration.ofHours(1), task.getDuration());
        assertEquals(task.getStartTime().plus(task.getDuration()), task.getEndTime());
    }

    @Test
    void shouldUpdateFields() {
        task.setName("Updated");
        task.setDescription("Updated Desc");
        task.setStatus(Status.DONE);
        task.setDuration(Duration.ofMinutes(90));

        assertEquals("Updated", task.getName());
        assertEquals("Updated Desc", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
        assertEquals(Duration.ofMinutes(90), task.getDuration());
    }
}