package ru.filippov.utils;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import ru.filippov.GUI.customNodes.AnimatedZoomOperator;
import ru.filippov.GUI.customNodes.SceneGestures;

public class NetVisualisation extends Pane {

    public NetVisualisation(Node parent){

        // Create panel
        Canvas canvas = new Canvas(200, 200);
        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
        // Create operator
        AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();

// Listen to scroll events (similarly you could listen to a button click, slider, ...)
        canvas.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double zoomFactor = 1.5;
                if (event.getDeltaY() <= 0) {
                    // zoom out
                    zoomFactor = 1 / zoomFactor;
                }
                zoomOperator.zoom(canvas, zoomFactor, event.getSceneX(), event.getSceneY());
            }
        });

        SceneGestures sceneGestures = new SceneGestures(canvas);
        parent.addEventFilter( MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        parent.addEventFilter( MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());


    }


}
