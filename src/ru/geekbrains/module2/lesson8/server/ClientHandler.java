package ru.geekbrains.module2.lesson8.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final static long TEMPORARY_USER_LIVING_TIME = 120000;
    private String nick;
    private static HashSet<String> temporaryUsers = new HashSet<>();
    private boolean tempAuthEnabled = false;

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            new Thread(() -> {
                try {
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String message = dataInputStream.readUTF();
                System.out.println(message);
                if ("/end".equals(message)) {
                    chatExit();
                    break;
                }
                if (message.startsWith("/auth")) {
                    authenticate(message);
                    continue;
                }
                if (message.startsWith("/w")) {
                    privateMessage(message);
                } else {
                    server.broadcast(nick + "/: " + message);
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) e.printStackTrace();
            System.out.println("Socked closed");
        } finally {
            closeConnection();
        }
    }

    private void authenticate(String authMessage) {
        if (authMessage.startsWith("/authTemp")) {
            nick = getUserTemporaryNick();
            temporaryUsers.add(nick);
            server.subscribe(this);
            tempAuthEnabled = true;
            sendMessage("/AuthTempOk " + nick);
            authCountdown();
            sendMessage("Временная аутентификация доступна в течении 120с");
            server.broadcast("Пользователь " + nick + " вошел в чат");
        } else {
            final String[] words = authMessage.split(" ");
            final String login = words[1];
            final String password = words[2];
            final String newNick = server.getAuthService().getNickByLoginAndPassword(login, password);
            if (newNick == null) {
                sendMessage("Неверный логин и пароль");
                return;
            }
            if (server.isNickBusy(newNick)) {
                sendMessage("Данный пользователь уже находится в чате");
            } else {
                if (tempAuthEnabled) {
                    server.broadcast("Временный пользователь " + nick + " вошёл в чат как " + newNick);
                    temporaryUsers.remove(nick);
                    tempAuthEnabled = false;
                } else {
                    server.subscribe(this);
                    server.broadcast("Пользователь " + newNick + " вошел в чат");
                }
                nick = newNick;
                sendMessage("/AuthOk " + nick);
            }
        }
    }

    private void privateMessage(String privateMessage) {
        final String[] words = privateMessage.split(" ");
        server.privateMessage(nick, words[1], privateMessage.substring(words[1].length() + 3));
    }

    protected void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authCountdown() {
        new Thread(() -> {
            try {
                Thread.sleep(TEMPORARY_USER_LIVING_TIME);
                if (!socket.isClosed() && tempAuthEnabled) {
                    chatExit();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getNick() {
        return nick;
    }

    private String getUserTemporaryNick() {
        int i = 1;
        for (String ignored : temporaryUsers) {
            if (temporaryUsers.contains("TempUser" + i)) {
                i++;
            }
        }
        return "TempUser" + i;
    }

    private void chatExit() {
        sendMessage("/end");
        temporaryUsers.remove(nick);
        closeConnection();
        server.broadcast("Пользователь " + nick + " вышел из чата");
    }

    private void closeConnection() {
        server.unsubscribe(this);
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
