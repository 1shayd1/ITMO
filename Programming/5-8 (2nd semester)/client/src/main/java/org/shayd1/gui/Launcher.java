package org.shayd1.gui;

import org.shayd1.gui.localization.LocaleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        LocaleManager.getInstance().setLocale(LocaleManager.RU);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle(LocaleManager.getInstance().get("app.title"));
        primaryStage.setScene(new Scene(root, 420, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}