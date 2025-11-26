import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.tracker.HttpServer.DurationAdapter;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.HttpServer.LocalDateTimeAdapter;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseTestServer {

    protected TaskManager manager;
    protected HttpTaskServer server;
    protected Gson gson;

    @BeforeEach
    void startServer() throws IOException {
        manager = new InMemoryTaskManager();

        // ⚡ Регистрируем адаптеры для LocalDateTime и Duration
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting()
                .create();

        server = new HttpTaskServer(manager, gson);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }
}