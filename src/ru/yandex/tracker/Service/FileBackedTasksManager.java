package ru.yandex.tracker.Service;

import java.nio.file.Path;

public class FileBackedTasksManager extends FileBackedTaskManager {

    public FileBackedTasksManager(Path file) {
        super(file);
    }

}