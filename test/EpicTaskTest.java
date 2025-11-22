package ru.yandex.tracker;

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
    void shouldBeNewIfAllSubtasksNew() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateStatus();

        assertEquals(Status.NEW, epic.getStatus(), "Эпик должен быть NEW если все подзадачи NEW");
    }

    @Test
    void shouldBeDoneIfAllSubtasksDone() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.DONE,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateStatus();

        assertEquals(Status.DONE, epic.getStatus(), "Эпик должен быть DONE если все подзадачи DONE");
    }

    @Test
    void shouldBeInProgressIfMixedNewAndDone() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateStatus();

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Эпик должен быть IN_PROGRESS если подзадачи NEW и DONE");
    }

    @Test
    void shouldBeInProgressIfAnySubtaskInProgress() {
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateStatus();

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Эпик должен быть IN_PROGRESS если хотя бы одна подзадача IN_PROGRESS");
    }

    @Test
    void shouldCalculateStartEndAndDuration() {
        LocalDateTime now = LocalDateTime.now();
        Subtask s1 = new Subtask(epic.getId(), "S1", "Desc", Status.NEW, now, Duration.ofMinutes(30));
        Subtask s2 = new Subtask(epic.getId(), "S2", "Desc", Status.NEW, now.plusHours(1), Duration.ofHours(2));

        epic.addSubtask(s1);
        epic.addSubtask(s2);

        epic.updateEpicTime();

        assertEquals(now, epic.getStartTime(), "Время начала эпика — самое раннее из подзадач");
        assertEquals(s2.getEndTime(), epic.getEndTime(), "Время окончания эпика — самое позднее из подзадач");
        assertEquals(s1.getDuration().plus(s2.getDuration()), epic.getDuration(), "Продолжительность эпика — сумма длительностей подзадач");
    }
}