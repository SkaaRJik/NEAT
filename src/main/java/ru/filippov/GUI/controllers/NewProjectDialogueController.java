package ru.filippov.GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.filippov.GUI.windows.AlertWindow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class NewProjectDialogueController {
    @FXML
    private Label projectNameLabel;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Label projectLocationLabel;
    @FXML
    private TextField projectLocationTextField;
    @FXML
    private Button finishButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button browseButton;

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
        /*this.projectNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/

    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.projectNameLabel.setText(resourceBundle.getString("PROJECT_NAME"));
        this.projectLocationLabel.setText(resourceBundle.getString("PROJECT_LOCATION"));
        this.finishButton.setText(resourceBundle.getString("FINISH_BUTTON"));
        this.cancelButton.setText(resourceBundle.getString("CANCEL_BUTTON"));
        this.directoryChooser.setTitle(resourceBundle.getString("CHOOSE_DIRECTORY"));
        this.resourceBundle = resourceBundle;
    }

    public void finish() {
        if (this.projectNameTextField.getText().length() == 0) {
            AlertWindow.createAlertWindow("\"" + this.projectNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_BE_EMPTY")).show();
            return;
        }
        File projectDirectory = new File(this.projectLocationTextField.getText() + "\\" + this.projectNameTextField.getText());
        if (projectDirectory.exists()) {
            AlertWindow.createAlertWindow(this.resourceBundle.getString("ALREADY_EXISTS")).show();
        } else {

            projectFile = new File(projectDirectory.getAbsolutePath()+"/"+projectNameTextField.getText()+".prj");
            projectFile.getParentFile().mkdirs();
            try{
                FileWriter fileWriter = new FileWriter(projectFile, false);

                fileWriter.write("PROJECT_NAME:"+this.projectNameTextField.getText()+".prj\n");
                fileWriter.append("LAST_OPENED_DATASET:\n");
                fileWriter.append("TRAIN_SET:\n");
                fileWriter.append("TEST_SET:\n");
                fileWriter.append("TRAINED_MODEL:\n");

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex){
                AlertWindow.createAlertWindow("Can't save project").show();
                projectFile = null;
            }

            this.stage.close();
        }
    }

    public String getProjectLocation(){
        return this.projectLocationTextField.getText();
    }

    public File getProjectFile(){
        if(this.projectNameTextField.getText().length() == 0 || this.projectNameTextField.getText().isEmpty()) return null;
        return projectFile;
    }

    public void cancel() {
        this.stage.close();
    }

    public void browse() {
        File directory = new File(this.projectLocationTextField.getText());
        if(!directory.exists())
            directory.mkdirs();
        this.directoryChooser.setInitialDirectory(directory);
        File dir = this.directoryChooser.showDialog(this.stage);
        if (dir != null) {
            this.projectLocationTextField.setText(dir.getAbsolutePath());
        }
    }

    public void refreshFields(){
        projectFile = null;
        this.projectLocationTextField.setText(Paths.get("").toAbsolutePath().toString()+"/projects/");
        this.projectNameTextField.setText("");
    }
}
