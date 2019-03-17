package ru.filippov.GUI.windows;

import java.util.ResourceBundle;

public class AlertWindow {
    private static javafx.scene.control.Alert alert;
    private static String error;
    public static void setLanguage(ResourceBundle resourceBundle){
        error = resourceBundle.getString("ERROR");
    }
    public static javafx.scene.control.Alert createAlertWindow(String message){
        if(alert == null) {
            alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(error);
            alert.setHeaderText(null);
        }
        alert.setContentText(message);
        return alert;
    }
}
