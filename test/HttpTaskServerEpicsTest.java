import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class HttpTaskServerEpicsTest {

    private HttpTaskServer server;
    private InMemoryTaskManager manager;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();

        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new ru.yandex.tracker.HttpServer.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new ru.yandex.tracker.HttpServer.DurationAdapter())
                .serializeNulls()
                .create();

        server = new HttpTaskServer(manager, gson);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc Epic");
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getAllEpicTasks();
        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getName());
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        manager.createEpicTask(new Epic("Epic 1", "Desc 1"));
        manager.createEpicTask(new Epic("Epic 2", "Desc 2"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
    }

    @Test
    void testGetSubtasksOfEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc2", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }
}