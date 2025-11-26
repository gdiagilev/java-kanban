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

public class PrioritizedHttpTest extends BaseTestServer {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void testPrioritizedTasksOrder() throws Exception {
        Task t1 = new Task("T1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        Task t2 = new Task("T2", "Desc", Status.NEW, LocalDateTime.now().plusMinutes(5), Duration.ofMinutes(10));
        manager.createTask(t1);
        manager.createTask(t2);

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks/prioritized"))
                .GET().build();
        HttpResponse<String> resp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Task[] tasks = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, tasks.length);
        assertEquals("T1", tasks[0].getName()); // проверяем порядок
    }
}