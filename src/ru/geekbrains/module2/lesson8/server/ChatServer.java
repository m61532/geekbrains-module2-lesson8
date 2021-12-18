package ru.geekbrains.module2.lesson8.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final AuthService authService;
    private List<ClientHandler> clients;

    public ChatServer() {
        this.authService = new SimpleAuthService();
        this.clients = new ArrayList<>();
    }

    public void run (){
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this).run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void subscribe(ClientHandler newClient) {
        clients.add(newClient);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void privateMessage(String sender, String receiver, String message) {
        for (ClientHandler client : clients) {
            if (sender.equals(client.getNick())) {
                client.sendMessage(sender + "<private to " + receiver + ">/: " + message);
            }
            if (receiver.equals(client.getNick())) {
                client.sendMessage(receiver + "/<private>: " + message);
            }
        }
    }
}
