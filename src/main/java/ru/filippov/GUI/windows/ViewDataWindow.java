package ru.filippov.GUI.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import ru.filippov.GUI.controllers.ViewDataWindowController;
import ru.filippov.utils.CsControl;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ViewDataWindow {
    private static ViewDataWindow instance;

    private Stage stage;
    private ViewDataWindowController controller;

    private ResourceBundle resourceBundle;

    public ViewDataWindow(Scene parent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/viewData.fxml"));
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
        this.stage.setMinWidth(600);
        this.stage.setMinHeight(400);

        this.stage.initOwner(parent.getWindow());
        controller = loader.getController();

        //controller.init();
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

        instance.controller.setLanguage(resourceBundle);
    }


    public static ViewDataWindow getInstance(Scene parent, String title) {
        if(instance == null) instance = new ViewDataWindow(parent);
        instance.controller.refresh();
        instance.stage.setTitle(title);
        return instance;
    }



    public void show() {
        instance.stage.show();
    }

    public TableView<List<Double>> getTableView(){
        return instance.controller.getTableView();
    }
}
