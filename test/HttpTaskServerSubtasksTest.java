package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerSubtasksTest {

    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/subtasks";

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder().serializeNulls().create();
        server = new HttpTaskServer(8080, manager, gson);
        server.start();
        client = HttpClient.newHttpClient();

        // создаём эпик, чтобы можно было создавать подзадачи
        Epic epic = new Epic("Epic 1", "Epic for subtasks");
        manager.createEpicTask(epic);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateGetAndDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(1, "Subtask 1", "Desc Subtask");
        String jsonSub = gson.toJson(subtask);

        // POST /subtasks
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSub))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        // GET /subtasks?id=1
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "?id=1"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        Subtask returned = gson.fromJson(getResponse.body(), Subtask.class);
        assertEquals("Subtask 1", returned.getName());

        // DELETE /subtasks
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        List<Subtask> subsAfterDelete = manager.getAllSubtasks();
        assertTrue(subsAfterDelete.isEmpty());
    }
}