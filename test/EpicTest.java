import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic 1", "Desc Epic");
    }

    @Test
    void shouldCreateEpicCorrectly() {
        assertEquals("Epic 1", epic.getName());
        assertEquals("Desc Epic", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
    }
}