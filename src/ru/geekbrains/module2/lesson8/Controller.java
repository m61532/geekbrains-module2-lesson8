package ru.geekbrains.module2.lesson8;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class Controller {
    public HBox chatTextFieldWithButtons;
    public TextField chatTextField;
    public TextArea chatTextArea;
    public ChatClient client;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox loginBox;

    public void sendMessage(ActionEvent actionEvent) {
        Object source = (Button) actionEvent.getSource();
        final String messageToSend = chatTextField.getText().trim();
        if (messageToSend.isEmpty()) {
            return;
        }
        chatTextField.clear();
        //chatTextArea.appendText("You: " + messageToSend + "\n");
        chatTextField.requestFocus();
        client.sendMessage(messageToSend);
    }

    public void addMessage(String message) {
        chatTextArea.appendText(message + "\n");
    }

    public void clearTextField() {
        chatTextField.clear();
    }

    public void bthAuthClick(ActionEvent actionEvent) {
        client = new ChatClient(this);
        client.sendMessage("/auth " + loginField.getText() + " " + passwordField.getText());
    }

    public void bthTempAuthClick(ActionEvent actionEvent) {
        client = new ChatClient(this);
        client.sendMessage("/authTemp");
    }

    public void setAuth(boolean success){
        loginBox.setVisible(!success);
        chatTextField.setVisible(success);
        chatTextArea.setVisible(success);
        chatTextFieldWithButtons.setVisible(success);
    }
}
