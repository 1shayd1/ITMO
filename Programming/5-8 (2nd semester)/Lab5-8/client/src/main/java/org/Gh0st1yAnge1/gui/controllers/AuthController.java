package org.Gh0st1yAnge1.gui.controllers;

import org.Gh0st1yAnge1.gui.localization.LocaleManager;
import org.Gh0st1yAnge1.gui.network.ServerConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.Gh0st1yAnge1.request_and_response.Response;

public class AuthController {

    @FXML private Label          lblTitle;
    @FXML private Label          lblLogin;
    @FXML private Label          lblPassword;
    @FXML private TextField      tfLogin;
    @FXML private PasswordField  pfPassword;
    @FXML private Button          btnLogin;
    @FXML private Button          btnRegister;
    @FXML private Label          lblMessage;
    @FXML private ComboBox<String> cbLanguage;
    @FXML private Label          lblLanguage;

    private final LocaleManager lm         = LocaleManager.getInstance();
    private final ServerConnection connection = new ServerConnection();
    private String lastErrorKey = null;

    @FXML
    public void initialize() {
        cbLanguage.setItems(FXCollections.observableArrayList(
                LocaleManager.SUPPORTED.stream()
                        .map(LocaleManager::displayName)
                        .toList()
        ));
        cbLanguage.getSelectionModel().select(
                LocaleManager.SUPPORTED.indexOf(lm.getCurrent())
        );
        cbLanguage.setOnAction(e -> {
            int idx = cbLanguage.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                lm.setLocale(LocaleManager.SUPPORTED.get(idx));
                applyLocale();
            }
        });

        btnLogin.setOnAction(e -> onLogin());
        btnRegister.setOnAction(e -> onRegister());
        applyLocale();
    }

    private void applyLocale() {
        lblTitle.setText(lm.get("auth.title"));
        lblLogin.setText(lm.get("auth.label.login"));
        lblPassword.setText(lm.get("auth.label.password"));
        lblLanguage.setText(lm.get("auth.label.language"));
        btnLogin.setText(lm.get("auth.btn.login"));
        btnRegister.setText(lm.get("auth.btn.register"));
        tfLogin.setPromptText(lm.get("auth.prompt.login"));
        pfPassword.setPromptText(lm.get("auth.prompt.password"));
        if (lastErrorKey != null) {
            lblMessage.setText(lm.get(lastErrorKey));
        }
    }

    private void onLogin() {
        String login = tfLogin.getText().trim();
        String pass  = pfPassword.getText();
        if (login.isEmpty() || pass.isEmpty()) {
            showError("auth.error.empty");
            return;
        }
        setDisabled(true);
        new Thread(() -> {
            try {
                if (!connection.isConnected()) connection.connect();
                Response resp = connection.login(login, pass);
                Platform.runLater(() -> {
                    setDisabled(false);
                    if (resp.success()) {
                        connection.setCredentials(login, pass);

                        long userId = -1;
                        try {
                            userId = Long.parseLong(resp.message());
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse userId, using -1");
                        }

                        openMainWindow(login, userId);

                    } else {
                        String serverMsg = resp.message();
                        if (serverMsg != null && (serverMsg.contains("Invalid login") || serverMsg.contains("password"))) {
                            showError("auth.error.invalid");
                        } else {
                            showError("auth.error.invalid");
                        }
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setDisabled(false);
                    showError("error.connection");
                });
            }
        }, "auth-thread").start();
    }

    private void onRegister() {
        String login = tfLogin.getText().trim();
        String pass  = pfPassword.getText();
        if (login.isEmpty() || pass.isEmpty()) {
            showError("auth.error.empty");
            return;
        }
        setDisabled(true);
        new Thread(() -> {
            try {
                if (!connection.isConnected()) connection.connect();
                Response resp = connection.register(login, pass);
                Platform.runLater(() -> {
                    setDisabled(false);

                    if (resp.success()) {
                        connection.setCredentials(login, pass);

                        long userId = -1;
                        try {
                            userId = Long.parseLong(resp.message());
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse userId, using -1");
                        }
                        showSuccess("auth.success.register");
                        openMainWindow(login, userId);

                    } else {
                        String serverMsg = resp.message();
                        if (serverMsg != null && (serverMsg.contains("already exists") || serverMsg.contains("taken") || serverMsg.contains("существует"))) {
                            showError("auth.error.exists");
                        } else {
                            showError("auth.error.failed");
                        }
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setDisabled(false);
                    showError("error.connection");
                });
            }
        }, "register-thread").start();
    }

    private void openMainWindow(String login, long userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            try {
                java.lang.reflect.Method setOwnerMethod = controller.getClass().getMethod("setMyOwnerId", long.class);
                java.lang.reflect.Method initMethod = controller.getClass().getMethod("init", ServerConnection.class, String.class);

                setOwnerMethod.invoke(controller, userId);
                initMethod.invoke(controller, connection, login);
            } catch (Exception e) {
                System.err.println("Ошибка линковки контроллеров: " + e.getMessage());
            }

            Stage stage = new Stage();
            stage.setTitle(lm.get("app.title"));
            stage.setScene(new Scene(root, 1200, 750));
            try {
                java.lang.reflect.Method shutdownMethod = controller.getClass().getMethod("shutdown");
                stage.setOnCloseRequest(e -> {
                    try { shutdownMethod.invoke(controller); } catch (Exception ignored) {}
                });
            } catch (NoSuchMethodException ignored) {}

            stage.show();
            ((Stage) btnLogin.getScene().getWindow()).close();
        } catch (Exception ex) {
            showError("auth.error.failed");
            ex.printStackTrace();
        }
    }
    private void showError(String key) {
        this.lastErrorKey = key;
        lblMessage.getStyleClass().remove("auth-success-message");
        if (!lblMessage.getStyleClass().contains("auth-error-message")) {
            lblMessage.getStyleClass().add("auth-error-message");
        }
        lblMessage.setText(lm.get(key));
    }

    private void showSuccess(String key) {
        this.lastErrorKey = key;
        lblMessage.getStyleClass().remove("auth-error-message");
        if (!lblMessage.getStyleClass().contains("auth-success-message")) {
            lblMessage.getStyleClass().add("auth-success-message");
        }
        lblMessage.setText(lm.get(key));
    }

    private void setDisabled(boolean v) {
        tfLogin.setDisable(v);
        pfPassword.setDisable(v);
        btnLogin.setDisable(v);
        btnRegister.setDisable(v);
    }
}