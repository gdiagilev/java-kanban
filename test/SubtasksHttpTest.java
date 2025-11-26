import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtasksHttpTest extends BaseTestServer {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void testCreateAndGetSubtask() throws Exception {
        Epic epic = new Epic("Epic1", "Desc");
        manager.createEpicTask(epic);

        Subtask sub = new Subtask(epic.getId(), "Subtask1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        String json = gson.toJson(sub);

        HttpRequest post = HttpRequest.newBuilder(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> postResp = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/subtasks?id=1")).GET().build();
        HttpResponse<String> getResp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Subtask result = gson.fromJson(getResp.body(), Subtask.class);
        assertEquals("Subtask1", result.getName());
    }

    @Test
    void testGetSubtasksOfEpic() throws Exception {
        Epic epic = new Epic("Epic1", "Desc");
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(epic.getId(), "Sub1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask sub2 = new Subtask(epic.getId(), "Sub2", "Desc", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(20));
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/epics/subtasks?id=" + epic.getId()))
                .GET().build();
        HttpResponse<String> resp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Subtask[] subs = gson.fromJson(resp.body(), Subtask[].class);
        assertEquals(2, subs.length);
    }
}