package ru;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import ru.filippov.GUI.customNodes.AnimatedZoomOperator;
import ru.filippov.GUI.customNodes.NodeGestures;
import ru.filippov.GUI.customNodes.SceneGestures;


/**
 * An application with a zoomable and pannable canvas.
 */
public class ZoomAndScrollApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();


        // Create panel
        Canvas canvas = new Canvas(200, 200);
        GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

        /*canvas.getChildren().add(new Circle(100, 100, 10));
        canvas.getChildren().add(new Circle(200, 200, 20));*/

        root.setCenter(canvas);
        root.setTop(new TextField("WOW"));
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



        // create scene which can be dragged and zoomed
        Scene scene = new Scene(root, 1024, 768);

        SceneGestures sceneGestures = new SceneGestures(canvas);
        scene.addEventFilter( MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter( MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());


        stage.setScene(scene);
        stage.show();

        this.draw(graphicsContext2D);

    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.YELLOWGREEN);
        gc.setStroke(Color.YELLOW);
        gc.fillOval(10, 10, 10, 10);
    }
}