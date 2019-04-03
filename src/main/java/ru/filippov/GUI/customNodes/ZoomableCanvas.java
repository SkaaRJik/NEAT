package ru.filippov.GUI.customNodes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;

public abstract class ZoomableCanvas extends Canvas {
    private class DragContext {

        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;

        double finalPositionX;
        double finalPositionY;

        double mouseXPos;
        double mouseYPos;

    }

    private DragContext sceneDragContext;
    private SimpleDoubleProperty zoom = new SimpleDoubleProperty(1.0);



    public ZoomableCanvas() {
        this(0, 0);
    }

    public ZoomableCanvas(double width, double height) {
        super(width, height);
        this.getStyleClass().add("visualisation-container");
        sceneDragContext = new DragContext();
        this.setOnScroll(zoomHandler);

        this.setOnMouseMoved(event -> {

            sceneDragContext.mouseXPos = event.getX();
            sceneDragContext.mouseYPos = event.getY();

        });

        this.setOnMousePressed(event -> {
            ZoomableCanvas canvas = (ZoomableCanvas) event.getSource();
            // right mouse button => panning
            if( !event.isPrimaryButtonDown())
                return;

            sceneDragContext.mouseAnchorX = event.getX();
            sceneDragContext.mouseAnchorY = event.getY();

            Affine transform = canvas.getGraphicsContext2D().getTransform();


            sceneDragContext.translateAnchorX = transform.getTx();
            sceneDragContext.translateAnchorY = transform.getTy();;
            event.consume();
        });
        this.setOnMouseDragged(event -> {

            if( !event.isPrimaryButtonDown())
                return;


            sceneDragContext.finalPositionX = event.getX();
            sceneDragContext.finalPositionY = event.getY();
            redraw(false);
            event.consume();
        });



        this.zoomProperty().addListener(o -> redraw(true));

    }

    protected EventHandler<ScrollEvent> zoomHandler = new EventHandler<ScrollEvent>() {
        @Override
        public void handle(ScrollEvent event) {
            ZoomableCanvas zcanvas = (ZoomableCanvas) event.getSource();
            GraphicsContext gc = zcanvas.getGraphicsContext2D();
            Affine affine = gc.getTransform();
            double zoom = affine.getMxx() + event.getDeltaY() / 800;
            /*if (zoom <= 0.1) {
                zoom = 0.1;
            }
            if (zoom >= 5.0) {
                zoom = 5.0;
            }*/
            if(zoom <= 0) zoom = 0;
            sceneDragContext.mouseXPos = event.getX();
            sceneDragContext.mouseYPos = event.getY();
            zcanvas.setZoom(zoom);
            zcanvas.redraw(true);
            event.consume();
        }
    };

    public void setZoom(double value) {
        if (value != getZoom()) {
            this.zoom.set(value);
            redraw(true);
        }
    }

    public double getZoom() {
        return zoom.get();
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public void clean() {
        GraphicsContext gc = this.getGraphicsContext2D();
        Canvas canvas = gc.getCanvas();
        Affine affine = gc.getTransform();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(affine);
    }

    private void redraw(boolean zoom) {
        GraphicsContext gc = this.getGraphicsContext2D();
        if (gc == null) return;

        // Чистим:
        Canvas canvas = gc.getCanvas();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, w, h);

        double z = getZoom();
        if(zoom)
            gc.setTransform(z, 0, 0, z, (sceneDragContext.mouseXPos - sceneDragContext.mouseXPos * z)/2, (sceneDragContext.mouseYPos - sceneDragContext.mouseYPos * z)/2);
        else
            gc.setTransform(z, 0, 0, z, sceneDragContext.translateAnchorX + sceneDragContext.finalPositionX - sceneDragContext.mouseAnchorX, sceneDragContext.translateAnchorY + sceneDragContext.finalPositionY - sceneDragContext.mouseAnchorY);

        // Рисуем:
        paint(gc);
    }



   /* // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D convertCoordinates(Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();


        return new Point2D(
                this.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }*/

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    public abstract void paint(GraphicsContext gc);

}
