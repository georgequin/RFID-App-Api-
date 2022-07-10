package sample.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("sample/login_page.fxml")));
        root.getStylesheets().add(getClass().getClassLoader().getResource("sample/mod.css").toExternalForm());
        primaryStage.setTitle("Nigerian Army University Biu");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }
//
//    @Override
//    public void init() throws Exception {
//        super.init();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
