package ru.geekbrains.module2.lesson8.server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    static int tempUserCounter = 0;

    private final List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("login" + i, "password" + i, "nick" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            System.out.println(login + " " + user.login + " " + login.equals(user.login));
            System.out.println(password + " " + user.password + " " + password.equals(user.password));
            if (login.equals(user.login) & password.equals(user.password)) return user.nick;
        }
        return null;
    }

    private static class UserData {
        private final String login;
        private final String password;
        private final String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }
}
