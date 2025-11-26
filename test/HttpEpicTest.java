import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.HttpServer.DurationAdapter;
import ru.yandex.tracker.HttpServer.LocalDateTimeAdapter;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpEpicTest {

    private TaskManager manager;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        client = HttpClient.newHttpClient();
    }

    @Test
    void testCreateAndGetEpic() throws Exception {
        Epic epic = new Epic("Epic1", "EpicDesc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(1));
        manager.createEpicTask(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics?id=" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic returnedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(epic.getName(), returnedEpic.getName());
        assertEquals(epic.getSubtasks().size(), returnedEpic.getSubtasks().size());
    }

    @Test
    void testGetSubtasksOfEpic() throws Exception {
        Epic epic = new Epic("Epic2", "EpicDesc2", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(1));
        manager.createEpicTask(epic);

        Subtask s1 = new Subtask("S1", "D1", Status.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask s2 = new Subtask("S2", "D2", Status.NEW, epic.getId(),
                s1.getEndTime().plusMinutes(1), Duration.ofMinutes(15));

        manager.createSubtask(s1);
        manager.createSubtask(s2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] returnedSubs = gson.fromJson(response.body(), Subtask[].class);

        assertEquals(2, returnedSubs.length);
        assertEquals("S1", returnedSubs[0].getName());
        assertEquals("S2", returnedSubs[1].getName());
    }
}