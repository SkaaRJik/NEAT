package ru.filippov.GUI.customNodes;

import javafx.scene.canvas.Canvas;

public class ResizableCanvas extends Canvas {
    @Override
    public double prefWidth(double height) {
        return 200;
    }

    @Override
    public double prefHeight(double width) {
        return 200;
    }
}
