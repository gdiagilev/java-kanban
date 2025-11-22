package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        epic = new Epic("Эпик для субтаска", "Описание эпика");
        subtask = new Subtask(epic.getId(), "Субтаск 1", "Описание субтаска", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(45));
    }

    @Test
    void shouldCreateSubtaskCorrectly() {
        assertEquals("Субтаск 1", subtask.getName());
        assertEquals("Описание субтаска", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(epic.getId(), subtask.getEpicId());
        assertNotNull(subtask.getStartTime());
        assertEquals(Duration.ofMinutes(45), subtask.getDuration());
        assertEquals(subtask.getStartTime().plus(subtask.getDuration()), subtask.getEndTime());
    }

    @Test
    void shouldUpdateSubtaskFields() {
        subtask.setName("Новое имя");
        subtask.setDescription("Новое описание");
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setDuration(Duration.ofHours(1));

        assertEquals("Новое имя", subtask.getName());
        assertEquals("Новое описание", subtask.getDescription());
        assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        assertEquals(Duration.ofHours(1), subtask.getDuration());
    }
}