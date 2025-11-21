package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();

        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Test epic");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getAllEpicTasks();
        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getName());
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 2", "Desc");
        manager.createEpicTask(epic);
        int id = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics?id=" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic e = gson.fromJson(response.body(), Epic.class);
        assertEquals("Epic 2", e.getName());
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        manager.createEpicTask(new Epic("E1", "D1"));
        manager.createEpicTask(new Epic("E2", "D2"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);

        assertEquals(2, epics.length);
    }

    @Test
    void testDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("ToDelete", "Desc");
        manager.createEpicTask(epic);
        int id = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics?id=" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllEpicTasks().isEmpty());
    }

    @Test
    void testGetSubtasksOfEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with subtasks", "Desc");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc2", Status.DONE,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] subs = gson.fromJson(response.body(), Subtask[].class);

        assertEquals(2, subs.length);
        assertEquals("Sub1", subs[0].getName());
        assertEquals("Sub2", subs[1].getName());
    }
}