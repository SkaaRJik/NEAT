package ru.filippov.GUI.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.ResourceBundle;

public class ViewDataWindowController {

    private Stage stage;

    @FXML
    TableView<List<Double>> tableView;

    ResourceBundle resourceBundle;


    public void init(){
        Platform.runLater(() -> {
            this.stage = ((Stage) this.tableView.getScene().getWindow());
            stage.getScene().setOnKeyPressed(keyEvent -> {
                switch (keyEvent.getCode()){
                    case ESCAPE:
                        this.stage.close();
                        keyEvent.consume();
                        break;
                }

            });
        });

        /*this.projectNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/

    }

    public void setLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
    }

    public TableView<List<Double>> getTableView(){
        return tableView;
    }

    public void refresh(){
        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
    }
}
