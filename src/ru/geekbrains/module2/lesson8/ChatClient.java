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
    }

    public void openConnection() {
        try {
            socket = new Socket("localhost", 8189);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            openConnection();
            new Thread(() -> {
                readMessage();
            }).start();
        } catch (Exception e) {
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

    private void readMessage() {
        try {
            while (true) {
                final String message = dataInputStream.readUTF();
                System.out.println(message);
                if (message.equals("/end")) {
                    chatWindowClose();
                    break;
                }
                if (message.startsWith("/Auth")) {
                    authenticateHandle(message);
                } else {
                    controller.addMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void chatWindowClose() {
        controller.setAuth(false);
        controller.setTempAuth(false);
    }

    private void authenticateHandle(String message) {
        final String[] strings = message.split(" ");
        if (message.startsWith("/AuthTemp")) {
            controller.setTempAuth(true);
            controller.addMessage("Успешная временная аутентификация под ником " + strings[1]);
        } else {
            controller.setTempAuth(false);
            controller.setAuth(true);
            controller.addMessage("Успешная аутентификация под ником " + strings[1]);
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
