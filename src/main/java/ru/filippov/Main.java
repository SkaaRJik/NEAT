package ru.filippov;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.MainController;

public class Main extends Application {
    FXMLLoader fxmlLoader;
    @Override
    public void start(Stage primaryStage) throws Exception{
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/main.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 600));
        MainController controller = fxmlLoader.getController();

        controller.init();
        primaryStage.show();
        controller.initVisibleElements();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
