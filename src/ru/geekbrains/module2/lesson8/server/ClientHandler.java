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
    private String nick;
    private static final long TEMPORARY_USER_LIVING_TIME = 120000;
    private static HashSet<String> temporaryUsers = new HashSet<>();
    private boolean timeToLeave;

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
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate() {
        timeToLeave = false;
        while (true) {
            try {
                final String newMessage = dataInputStream.readUTF();
                if (newMessage.equals("/authTemp")) {
                    this.nick = getUserTemporaryNick();
                    sendMessage("/AuthOk " + nick);
                    server.subscribe(this);
                    temporaryUsers.add(nick);
                    server.broadcast("Пользователь " + nick + " зашел в чат");
                    timerCountdown();
                    break;
                }
                if (newMessage.equals("/auth")) {
                    final String[] loginAndPassword = newMessage.split(" ");
                    final String login = loginAndPassword[1];
                    final String password = loginAndPassword[2];
                    final String nick = server.getAuthService().getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/AuthOk " + nick);
                        this.nick = nick;
                        server.subscribe(this);
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        break;
                    } else {
                        sendMessage("Неверные логин и пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() {
        try {
            while (true) {
                if (dataInputStream.available() > 0) {
                    final String message = dataInputStream.readUTF();
                    if ("/end".equals(message)) {
                        chatExit();
                        break;
                    }
                    if (message.startsWith("/w")) {
                        String[] s = message.split(" ");
                        final String receiver = s[1];
                        final String privateMessage = message.substring(receiver.length() + 9);
                        server.privateMessage(nick, receiver, privateMessage);
                    } else {
                        server.broadcast(nick + "/: " + message);
                    }
                }
                if (timeToLeave){
                    chatExit();
                    temporaryUsers.remove(nick);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void timerCountdown() {
        sendMessage("Временная авторизация доступна в течении 120с");
        new Thread(() -> {
            try {
                Thread.sleep(TEMPORARY_USER_LIVING_TIME);
                timeToLeave = true;
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
        for (String temporaryUserNicks : temporaryUsers) {
            if (temporaryUserNicks.equals("TempUser" + i)) {
                i++;
                continue;
            }
        }
        return "TempUser" + i;
    }

    private void chatExit() {
        sendMessage("/end");
        closeConnection();
        server.broadcast("Пользователь " + nick + " вышел из чата");
    }

    private void closeConnection() {
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
        server.unsubscribe(this);
    }
}
