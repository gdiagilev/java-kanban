import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {

    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Большой эпик", "Описание эпика");
    }

    @Test
    void shouldSetStatusNewWhenAllSubtasksNew() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW);
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW);
        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateEpicStatus();
        assertEquals(Status.NEW, epic.getStatus(), "Если все подзадачи NEW — эпик тоже NEW");
    }

    @Test
    void shouldSetStatusDoneWhenAllSubtasksDone() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.DONE);
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.DONE);
        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateEpicStatus();
        assertEquals(Status.DONE, epic.getStatus(), "Если все подзадачи DONE — эпик DONE");
    }

    @Test
    void shouldSetStatusInProgressWhenMixedNewAndDone() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW);
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.DONE);
        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateEpicStatus();
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Если подзадачи NEW и DONE — эпик IN_PROGRESS");
    }

    @Test
    void shouldSetStatusInProgressWhenAnySubtaskInProgress() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.IN_PROGRESS);
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW);
        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateEpicStatus();
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Если хотя бы одна подзадача IN_PROGRESS — эпик IN_PROGRESS");
    }

    @Test
    void shouldCalculateStartEndAndDurationCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW);
        s1.setStartTime(now);
        s1.setDuration(Duration.ofMinutes(30));

        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW);
        s2.setStartTime(now.plusHours(1));
        s2.setDuration(Duration.ofHours(2));

        epic.addSubtask(s1);
        epic.addSubtask(s2);
        epic.updateEpicTime();

        assertEquals(now, epic.getStartTime(), "Время начала эпика — самое раннее из подзадач");

        LocalDateTime expectedEnd = s2.getEndTime(); // последняя по времени подзадача
        assertEquals(expectedEnd, epic.getEndTime(), "Время окончания эпика — самое позднее из подзадач");

        Duration expectedDuration = s1.getDuration().plus(s2.getDuration());
        assertTrue(expectedDuration.equals(epic.getDuration()),
                "Продолжительность эпика должна быть суммой подзадач");
    }
}