package ru.yandex.tracker.Exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException() {
        super("Ошибка при сохранении данных");
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
