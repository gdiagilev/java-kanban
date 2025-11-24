import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic 1", "Epic Desc");
        subtask = new Subtask(epic.getId(), "Subtask 1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(45));
    }

    @Test
    void shouldCreateSubtaskCorrectly() {
        assertEquals("Subtask 1", subtask.getName());
        assertEquals("Desc", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(epic.getId(), subtask.getEpicId());
        assertNotNull(subtask.getStartTime());
        assertEquals(Duration.ofMinutes(45), subtask.getDuration());
        assertEquals(subtask.getStartTime().plus(subtask.getDuration()), subtask.getEndTime());
    }
}