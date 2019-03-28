package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.NewDatasetDialogueController;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class NewDatasetDialogue {

    private static NewDatasetDialogue instance;




    private Stage stage;
    private NewDatasetDialogueController controller;

    public NewDatasetDialogue(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/newDatasetDialogue.fxml"));
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


    public static NewDatasetDialogue getInstance(Scene parent) {
        if(instance == null) instance = new NewDatasetDialogue(parent);
        instance.controller.refreshFields();
        return instance;
    }

    public void show() {
        this.stage.showAndWait();
    }



    public File getNewDatasetFolder(){
        return this.controller.getNewDatasetFolder();
    }


    public void setCurrentProject(File file) {
        this.controller.setCurrentProject(file);
    }
}
