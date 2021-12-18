package ru.geekbrains.module2.lesson8;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Controller controller;

    public ChatClient(Controller controller) {
        this.controller = controller;
        openConnection();
    }

    public void openConnection() {
        try {
            socket = new Socket("localhost", 8189);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while (true) {
                    final String authMessage;
                    try {
                        authMessage = dataInputStream.readUTF();
                        if (authMessage.startsWith("/AuthOk")) {
                            final String[] split = authMessage.split(" ");
                            final String nick = split[1];
                            controller.addMessage("Успешная авторизация под ником " + nick);
                            controller.setAuth(true);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (true) {
                    try {
                        final String incomingMessage = dataInputStream.readUTF();
                        if ("/end".equals(incomingMessage)) {
                            closeConnection();
                            controller.setAuth(false);
                            break;
                        }
                        controller.addMessage(incomingMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String messageToSend) {
        try {
            dataOutputStream.writeUTF(messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
