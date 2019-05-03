package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.SaveDialogueController;
import ru.filippov.utils.CsControl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SaveDialogue {

    private static SaveDialogue instance;




    private Stage stage;
    private SaveDialogueController controller;

    public SaveDialogue(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/saveDialogue.fxml"));
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


    public static SaveDialogue getInstance(Scene parent) {
        if(instance == null) instance = new SaveDialogue(parent);
        instance.controller.refreshFields();
        return instance;
    }

    public void show() {
        setLanguage();
        this.stage.showAndWait();
    }



    public String getNameOfNewFile(){
        return this.controller.getName();
    }

    public File getNewFile(){
        return this.controller.getNewFile();
    }

    public String getName(){
        return this.controller.getName();
    }

    public void setLabel(String label){
        instance.controller.setLabel(label);
    }

    public void setFileDirectory(String projectPath) {
        instance.controller.serPathToSave(projectPath);
    }

    public void setExtension(String extension) {
        instance.controller.setExtension(extension);
    }
}
