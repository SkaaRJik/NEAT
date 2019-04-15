package ru.filippov.GUI.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.neat4j.neat.core.DefaultConfig;
import ru.filippov.GUI.windows.AlertWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class SaveModelDialogueController {
    @FXML
    private Label modelNameLabel;
    @FXML
    private TextField modelNameTextField;

    @FXML
    private Button finishButton;
    @FXML
    private Button cancelButton;

    private File model;
    private Stage stage;

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
        /*this.modelNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/

    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
        Platform.runLater(() -> {
            this.modelNameLabel.setText(resourceBundle.getString("MODEL_NAME"));
            this.finishButton.setText(resourceBundle.getString("FINISH_BUTTON"));
            this.cancelButton.setText(resourceBundle.getString("CANCEL_BUTTON"));
        });

    }

    public void finish() {
        if (this.modelNameTextField.getText().length() == 0) {
            AlertWindow.createAlertWindow("\"" + this.modelNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_BE_EMPTY")).show();
            return;
        }
        try {
            Files.copy(this.model.toPath(), new File(this.model.getParent()+"\\"+this.modelNameTextField.getText()+".ser").toPath());
        }
        catch (FileAlreadyExistsException ex) {
            ex.printStackTrace();
            AlertWindow.createAlertWindow("\"" + this.modelNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("ALREADY_EXISTS")).show();
            return;

        } catch (IOException e) {
            e.printStackTrace();
            AlertWindow.createAlertWindow("\"" + this.modelNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_SAVE")).show();
        }
        this.stage.close();

    }

    public String getName(){
         return this.modelNameTextField.getText();

    }

    public void cancel() {
        this.stage.close();
    }


    public void refreshFields(){
        this.model = null;
        this.modelNameTextField.setText("");
    }

    public void setModelToSave(File file) {
        this.model = file;
    }
}
