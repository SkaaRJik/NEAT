package ru.filippov.GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.neat4j.neat.core.DefaultConfig;
import ru.filippov.GUI.windows.AlertWindow;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class NewDatasetDialogueController {
    @FXML
    private Label datasettNameLabel;
    @FXML
    private TextField datasetNameTextField;

    @FXML
    private Button finishButton;
    @FXML
    private Button cancelButton;

    private File projectFile;
    private Stage stage;

    final DirectoryChooser directoryChooser = new DirectoryChooser();

    ResourceBundle resourceBundle;


    public void init(){
        refreshFields();
        this.stage = ((Stage) this.cancelButton.getScene().getWindow());
        stage.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()){
                case ENTER:
                    finish();
                    //Stop letting it do anything else
                    keyEvent.consume();
                    break;
                case ESCAPE:
                    cancel();
                    keyEvent.consume();
                    break;
            }

        });
        /*this.datasetNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/

    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.datasettNameLabel.setText(resourceBundle.getString("DATASET_NAME"));
        this.finishButton.setText(resourceBundle.getString("FINISH_BUTTON"));
        this.cancelButton.setText(resourceBundle.getString("CANCEL_BUTTON"));
        this.directoryChooser.setTitle(resourceBundle.getString("CHOOSE_DIRECTORY"));
        this.resourceBundle = resourceBundle;
    }

    public void finish() {
        if (this.datasetNameTextField.getText().length() == 0) {
            AlertWindow.createAlertWindow("\"" + this.datasettNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_BE_EMPTY")).show();
            return;
        }
        File newDatasetFolder = new File(projectFile.getParent() + "\\datasets\\" + this.datasetNameTextField.getText());
        if (newDatasetFolder.exists()) {
            AlertWindow.createAlertWindow(this.resourceBundle.getString("ALREADY_EXISTS")).show();
        } else {

            File neatFile = new File(newDatasetFolder.getAbsolutePath()+"\\"+this.datasetNameTextField.getText()+".neat");

            if(!newDatasetFolder.getParentFile().exists()) newDatasetFolder.getParentFile().mkdir();
            if(!newDatasetFolder.exists()) newDatasetFolder.mkdir();

            FileWriter writer = null;
            try{
                writer = new FileWriter(neatFile, false);
                writer.write("");

                Iterator<Map.Entry<String, String>> iterator = DefaultConfig.getDefaultConfig().getMap().entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, String> pair = iterator.next();
                    writer.append(pair.getKey()+"="+pair.getValue()+"\n");
                }

                projectFile = neatFile;
            } catch (FileNotFoundException e) {
                projectFile = null;
                e.printStackTrace();
            } catch (IOException e) {
                projectFile = null;
                e.printStackTrace();
            } finally {
                if(writer!=null){
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.stage.close();
        }
    }

    public File getNewDatasetFolder(){
        if(this.datasetNameTextField.getText().length() == 0 || this.datasetNameTextField.getText().isEmpty()) return null;
        return projectFile;
    }

    public void cancel() {
        this.stage.close();
    }


    public void refreshFields(){
        projectFile = null;
        this.datasetNameTextField.setText("");
    }

    public void setCurrentProject(File file) {
        this.projectFile = file;
    }
}
