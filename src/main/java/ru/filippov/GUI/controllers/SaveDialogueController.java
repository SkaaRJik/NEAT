package ru.filippov.GUI.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.filippov.GUI.windows.AlertWindow;

import java.io.File;
import java.util.ResourceBundle;

public class SaveDialogueController {
    @FXML
    private Label fileNameLabel;
    @FXML
    private TextField saveNameTextField;

    @FXML
    private Button finishButton;
    @FXML
    private Button cancelButton;

    private String path;
    private Stage stage;

    ResourceBundle resourceBundle;
    private String extension;

    private File newFile = null;

    public void setExtension(String extension) {
        this.extension = extension;
    }

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
        /*this.saveNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/

        this.saveNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null){
                if (newValue.contains("/") || newValue.contains("\\") || newValue.contains(".")) {
                    saveNameTextField.setText(newValue.replaceAll("[/\\\\.]", " "));
                }

            }
        });

    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
        Platform.runLater(() -> {
            this.fileNameLabel.setText(resourceBundle.getString("MODEL_NAME"));
            this.finishButton.setText(resourceBundle.getString("FINISH_BUTTON"));
            this.cancelButton.setText(resourceBundle.getString("CANCEL_BUTTON"));
        });

    }

    public void finish() {
        if (this.saveNameTextField.getText().length() == 0) {
            AlertWindow.createAlertWindow("\"" + this.fileNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_BE_EMPTY")).show();
            return;
        }
        newFile = new File(this.path+this.saveNameTextField.getText()+"."+extension);

        newFile.getParentFile().getParentFile().mkdir();
        newFile.getParentFile().mkdir();
        if(newFile.exists()){
            AlertWindow.createAlertWindow("\"" + this.fileNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("ALREADY_EXISTS")).show();
            return;
        }


        this.stage.close();

    }

    public String getName(){
         return this.saveNameTextField.getText();

    }

    public void cancel() {
        this.stage.close();
    }


    public void refreshFields(){
        this.path = "";
        this.saveNameTextField.setText("");
        this.newFile = null;
    }

    public void serPathToSave(String path) {
        this.path = path+"\\";
    }

    public void setLabel(String label){
        this.fileNameLabel.setText(label);
    }

    public File getNewFile() {
        return newFile;
    }
}
