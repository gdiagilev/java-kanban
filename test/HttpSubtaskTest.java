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

public class HttpSubtaskTest {

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
    void testCreateAndGetSubtask() throws Exception {
        Epic epic = new Epic("Epic1", "EpicDesc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(1));
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(
                "Sub1", "SubDesc", Status.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(10)
        );
        manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] returned = gson.fromJson(response.body(), Subtask[].class);

        assertEquals(1, returned.length);
        assertEquals("Sub1", returned[0].getName());
        assertEquals(epic.getId(), returned[0].getEpicId());
    }
}