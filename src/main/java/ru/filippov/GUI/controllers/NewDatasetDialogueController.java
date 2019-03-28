package ru.filippov.GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.filippov.GUI.windows.AlertWindow;

import java.io.*;
import java.util.ResourceBundle;

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

            File datasetsFolder = new File(newDatasetFolder.getParent());
            if(!datasetsFolder.exists()) datasetsFolder.mkdir();

            newDatasetFolder.mkdir();
            File defaultFile = new File(getClass().getClassLoader().getResource("NEATfiles/default.neat").getPath());
            File neatFile = new File(newDatasetFolder.getAbsolutePath()+"/"+this.datasetNameTextField.getText()+".neat");
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(defaultFile);
                os = new FileOutputStream(neatFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                projectFile = newDatasetFolder;
            } catch (FileNotFoundException e) {
                projectFile = null;
                e.printStackTrace();
            } catch (IOException e) {
                projectFile = null;
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
