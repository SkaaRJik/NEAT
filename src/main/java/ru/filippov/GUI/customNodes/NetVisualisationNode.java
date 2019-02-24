package ru.filippov.GUI.customNodes;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.neat4j.neat.ga.core.Chromosome;


public class NetVisualisationNode extends BorderPane {

    Canvas canvas;

    public NetVisualisationNode(){

        // Create panel
        this.canvas = new Canvas(this.getPrefWidth(), this.getPrefHeight());

        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

        /*canvas.getChildren().add(new Circle(100, 100, 10));
        canvas.getChildren().add(new Circle(200, 200, 20));*/
        this.setTop(new TextField("WOW"));
        this.setCenter(canvas);

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

       /* canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());*/



        DragAndDropConfigurator dragAndDropConfigurator = new DragAndDropConfigurator(canvas);
        /*scene.addEventFilter( MouseEvent.MOUSE_PRESSED, dragAndDropConfigurator.getOnMousePressedEventHandler());
        scene.addEventFilter( MouseEvent.MOUSE_DRAGGED, dragAndDropConfigurator.getOnMouseDraggedEventHandler());*/
        this.addEventFilter( MouseEvent.MOUSE_PRESSED, dragAndDropConfigurator.getOnMousePressedEventHandler());
        this.addEventFilter( MouseEvent.MOUSE_DRAGGED, dragAndDropConfigurator.getOnMouseDraggedEventHandler());

        this.draw(graphicsContext2D);
    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.YELLOWGREEN);
        gc.setStroke(Color.YELLOW);
        gc.fillOval(10, 10, 10, 10);

    }

}
