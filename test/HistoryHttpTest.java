import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHttpTest extends BaseTestServer {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void testHistory() throws Exception {
        Task task = new Task("Task1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);
        manager.getTask(task.getId());

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/history"))
                .GET().build();
        HttpResponse<String> resp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Task[] history = gson.fromJson(resp.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals("Task1", history[0].getName());
    }
}