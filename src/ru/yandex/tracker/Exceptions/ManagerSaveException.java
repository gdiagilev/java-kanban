package ru.yandex.tracker.Exceptions;

public class ManagerSaveException extends RuntimeException{
    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
