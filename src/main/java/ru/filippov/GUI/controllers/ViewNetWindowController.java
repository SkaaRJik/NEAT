package ru.filippov.GUI.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.filippov.GUI.customNodes.ZoomPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewNetWindowController implements Initializable {
    @FXML
    Pane viewPane;


    @FXML
    BorderPane viewPaneContainer;


    ResourceBundle resourceBundle;
    private Stage stage;

    public void setLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            this.stage = ((Stage) this.viewPaneContainer.getScene().getWindow());
            /*stage.getScene().setOnKeyPressed(keyEvent -> {
                switch (keyEvent.getCode()){
                    case ESCAPE:
                        this.stage.close();
                        keyEvent.consume();
                        break;
                }*/
            this.viewPaneContainer.setCenter(ZoomPane.createZoomPane(viewPane));
            this.stage.widthProperty().addListener((observable, oldValue, newValue) -> {
                this.viewPaneContainer.setMaxWidth(newValue.doubleValue());
                this.viewPaneContainer.setPrefWidth(newValue.doubleValue());
            });
            this.stage.heightProperty().addListener((observable, oldValue, newValue) -> {
                this.viewPaneContainer.setMaxHeight(newValue.doubleValue());
                this.viewPaneContainer.setMaxHeight(newValue.doubleValue());
            });
        });


        /*this.projectNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.projectLocationTextField.getText().
        });*/
    }

    public Pane getViewPane() {
        return viewPane;
    }
}
