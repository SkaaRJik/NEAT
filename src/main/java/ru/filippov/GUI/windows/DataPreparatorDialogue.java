package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.DataPreparatorDialogueController;
import ru.filippov.utils.CsControl;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class DataPreparatorDialogue {
    private static DataPreparatorDialogue instance;

    private Stage stage;
    private DataPreparatorDialogueController controller;

    private ResourceBundle resourceBundle;

    public DataPreparatorDialogue(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/dataPreparator.fxml"));
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
        this.stage.initOwner(parent.getWindow());
        controller = loader.getController();

        controller.init();
    }

    public void setLanguageUsingDefaultLocale(){
        switch (Locale.getDefault().getLanguage()){
            case "ru":
                resourceBundle = ResourceBundle.getBundle("properties.languages.language", CsControl.Cp1251);
                break;
            default:
                resourceBundle = ResourceBundle.getBundle("properties.languages.language", Locale.getDefault());
                break;
        }
        //this.stage.setTitle(resourceBundle.getString("NEW_DATA"));

        instance.controller.changeLanguage(resourceBundle);
    }


    public static DataPreparatorDialogue getInstance(Scene parent) {
        if(instance == null) instance = new DataPreparatorDialogue(parent);

        return instance;
    }

    public DataPreparatorDialogue setCurrentDatasetFolder(String datasetFolderName){
        controller.setCurrentDatasetFolder(datasetFolderName);
        return instance;
    }

    public void show() {
        instance.controller.clearNamesOfDatasets();
        instance.stage.showAndWait();
    }

    public String getNameOfTrainingSet(){
        return instance.controller.getNameOfTrainingSet();
    }
    public String getNameOfTestSet(){ return instance.controller.getNameOfTestSet(); }
}
