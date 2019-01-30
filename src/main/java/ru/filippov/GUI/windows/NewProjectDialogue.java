package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.NewProjectDialogueController;

import java.io.IOException;
import java.util.ResourceBundle;

public class NewProjectDialogue {

    private static NewProjectDialogue instance;




    private Stage stage;
    private NewProjectDialogueController controller;

    public NewProjectDialogue(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/newProjectDialogue.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stage = new Stage();

        Scene scene = new Scene(root);
        //scene.getStylesheets().add((getClass().getClassLoader().getResource("css/light.css")).toExternalForm());
        this.stage.setScene(scene);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setResizable(false);
        this.stage.initOwner(parent.getWindow());
        controller = loader.getController();

        controller.init();
    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.stage.setTitle(resourceBundle.getString("NEW_PROJECT"));
        instance.controller.setLanguage(resourceBundle);
    }


    public static NewProjectDialogue getInstance(Scene parent) {
        if(instance == null) instance = new NewProjectDialogue(parent);
        instance.controller.refreshFields();
        return instance;
    }

    public void show() {
        this.stage.showAndWait();
    }

    public String getProjectLocation(){
        return this.controller.getProjectLocation();
    }

    public String getProjectName(){
        return this.controller.getProjectName();
    }


}
