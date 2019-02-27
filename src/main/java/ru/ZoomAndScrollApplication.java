package ru;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.filippov.GUI.customNodes.ZoomableCanvas;


/**
 * An application with a zoomable and pannable canvas.
 */
public class ZoomAndScrollApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.LIGHTGREEN);
        gc.fillOval(60, 10, 180, 180);
        gc.setFill(Color.WHITE);
        gc.fillOval(100, 50, 100, 100);
    }

    @Override
    public void start(Stage stage) throws Exception {

        VBox canvasContainer = new VBox();
        ZoomableCanvas zoomableCanvas1 = new ZoomableCanvas(300, 400) {
            @Override
            public void paint(GraphicsContext gc) {
                draw(gc);
            }
        };
        draw(zoomableCanvas1.getGraphicsContext2D());
        Button buttonClear = new Button("Clear");
        buttonClear.setOnAction(event -> {
            zoomableCanvas1.clean();
        });
        Button buttonPaint = new Button("Paint");
        newPaint(zoomableCanvas1, buttonPaint);
        canvasContainer.getChildren().addAll(zoomableCanvas1, new HBox(buttonClear, buttonPaint));

        ZoomableCanvas zoomableCanvas2 = new ZoomableCanvas(300, 400) {
            @Override
            public void paint(GraphicsContext gc) {
                draw(gc);
            }
        };
        draw(zoomableCanvas2.getGraphicsContext2D());
        buttonClear = new Button("Clear");
        buttonClear.setOnAction(event -> {
            zoomableCanvas2.clean();
        });
        buttonPaint = new Button("Paint");
        newPaint(zoomableCanvas2, buttonPaint);
        canvasContainer.getChildren().addAll(zoomableCanvas2, new HBox(buttonClear, buttonPaint));


        ZoomableCanvas zoomableCanvas3 = new ZoomableCanvas(300, 400) {
            @Override
            public void paint(GraphicsContext gc) {
                draw(gc);
            }
        };
        draw(zoomableCanvas3.getGraphicsContext2D());

        buttonClear = new Button("Clear");
        buttonClear.setOnAction(event -> {
            zoomableCanvas3.clean();
        });
        buttonPaint = new Button("Paint");
        newPaint(zoomableCanvas3, buttonPaint);
        canvasContainer.getChildren().addAll(zoomableCanvas3, new HBox(buttonClear, buttonPaint));

        ScrollPane root =  new ScrollPane(canvasContainer);

        Scene scene = new Scene(root, 1024, 768);

        stage.setScene(scene);
        stage.show();


    }

    private void newPaint(ZoomableCanvas zoomableCanvas, Button buttonPaint) {
        buttonPaint.setOnAction(event -> {
            GraphicsContext gc = zoomableCanvas.getGraphicsContext2D();
            zoomableCanvas.clean();

            gc.setFill(Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
            gc.fillOval(60, 10, 180, 180);
            gc.setFill(Color.WHITE);
            gc.fillOval(100, 50, 100, 100);
        });
    }
}


/*
    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        BorderPane borderPane2 = new BorderPane();
        BorderPane borderPane1 = new BorderPane();


        // Create panel
        ZoomableCanvas canvas = new ZoomableCanvas (300, 200){
            @Override
            public void paint(GraphicsContext gc) {
                // Рисуем:
                gc.setFill(Color.LIGHTGREEN);
                gc.fillOval(60, 10, 180, 180);
                gc.setFill(Color.WHITE);
                gc.fillOval(100, 50, 100, 100);
            }
        };
        canvas.paint(canvas.getGraphicsContext2D());
        //GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
       */
/* JFXUtils.CanvasConfigurator.setDragableCanvas(canvas, borderPane2);
        JFXUtils.CanvasConfigurator.setZoomOnCanvas(canvas);
        *//*
*/
/*canvas.getChildren().add(new Circle(100, 100, 10));
        canvas.getChildren().add(new Circle(200, 200, 20));*//*


        Label zoomLabel = new Label();
        zoomLabel.textProperty().bind(canvas.zoomProperty().asString());

        Slider slider = new Slider(0.8, 2.0, 1.0);
        slider.valueProperty().bindBidirectional(canvas.zoomProperty());


        borderPane1.setCenter(new LineChart<Number, Number>(new NumberAxis(), new NumberAxis()));

        borderPane2.setCenter(canvas);
        borderPane2.setTop(new TextField("WOW"));
        borderPane2.setBottom(new HBox(slider, zoomLabel));

        root.getChildren().addAll(borderPane1, borderPane2);

        // create scene which can be dragged and zoomed
        Scene scene = new Scene(root, 1024, 768);



        stage.setScene(scene);
        stage.show();

       // this.draw(graphicsContext2D);

    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.YELLOWGREEN);
        gc.setStroke(Color.YELLOW);
        gc.fillOval(10, 10, 10, 10);
    }
}*/
