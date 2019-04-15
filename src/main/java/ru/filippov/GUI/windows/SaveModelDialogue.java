package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.NewDatasetDialogueController;
import ru.filippov.GUI.controllers.SaveModelDialogueController;
import ru.filippov.utils.CsControl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SaveModelDialogue {

    private static SaveModelDialogue instance;




    private Stage stage;
    private SaveModelDialogueController controller;

    public SaveModelDialogue(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/saveModelDialogue.fxml"));
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

    public void setLanguage(){

        ResourceBundle resourceBundleNew =null;
        switch (Locale.getDefault().getLanguage()){
            case "ru":
                resourceBundleNew = ResourceBundle.getBundle("properties.languages.language", CsControl.Cp1251);
                break;
            default:
                resourceBundleNew = ResourceBundle.getBundle("properties.languages.language", Locale.getDefault());
                break;
        }


        this.stage.setTitle(resourceBundleNew.getString("SAVE_MODEL"));
        instance.controller.setLanguage(resourceBundleNew);
    }


    public static SaveModelDialogue getInstance(Scene parent) {
        if(instance == null) instance = new SaveModelDialogue(parent);
        instance.controller.refreshFields();
        return instance;
    }

    public void show() {
        setLanguage();
        this.stage.showAndWait();
    }



    public String getNameOfNewModel(){
        return this.controller.getName();
    }


    public void setModelToSave(File file) {
        this.controller.setModelToSave(file);
    }
}
