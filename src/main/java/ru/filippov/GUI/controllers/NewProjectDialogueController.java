package ru.filippov.GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.filippov.GUI.windows.AlertWindow;


import java.io.*;
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
            AlertWindow.getAlert("\"" + this.projectNameLabel.getText() + "\"" + " - " + this.resourceBundle.getString("CANT_BE_EMPTY")).show();
            return;
        }
        File file = new File(this.projectLocationTextField.getText() + "\\" + this.projectNameTextField.getText());
        if (file.exists()) {
            AlertWindow.getAlert(this.resourceBundle.getString("ALREADY_EXISTS")).show();
        } else {
            file.mkdir();
            File defaultFile = new File(getClass().getClassLoader().getResource("NEATfiles/default.neat").getPath());
            projectFile = new File(file.getPath()+"/"+this.projectNameTextField.getText()+".neat");
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(defaultFile);
                os = new FileOutputStream(projectFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
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
            directory.mkdir();
        this.directoryChooser.setInitialDirectory(directory);
        File dir = this.directoryChooser.showDialog(this.stage);
        if (dir != null) {
            this.projectLocationTextField.setText(dir.getAbsolutePath());
        }
    }

    public void refreshFields(){
        projectFile = null;
        this.projectLocationTextField.setText(Paths.get("").toAbsolutePath().toString()+"\\projects\\");
        this.projectNameTextField.setText("");
    }
}
