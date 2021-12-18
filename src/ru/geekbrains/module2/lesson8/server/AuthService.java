package ru.geekbrains.module2.lesson8.server;

public interface AuthService {
    String getNickByLoginAndPassword(String login, String password);
}
