package ru.filippov.utils;

import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.skins.JFXTextFieldSkin;
import com.jfoenix.skins.ValidationPane;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.lang.reflect.Field;

public class JFXUtils {
    public static class ZoomConfigurator {
        /** Allow to zoom/scale any node with pivot at scene (x,y) coordinates.
         *
         * @param node
         * @param factor
         * @param x
         * @param y
         */
        public static void zoom(Node node, double factor, double x, double y) {
            double oldScale = node.getScaleX();
            double scale = oldScale * factor;
            if (scale < 0.05) scale = 0.05;
            if (scale > 50)  scale = 50;
            node.setScaleX(scale);
            node.setScaleY(scale);

            double  f = (scale / oldScale)-1;
            Bounds bounds = node.localToScene(node.getBoundsInLocal());
            double dx = (x - (bounds.getWidth()/2 + bounds.getMinX()));
            double dy = (y - (bounds.getHeight()/2 + bounds.getMinY()));

            node.setTranslateX(node.getTranslateX()-f*dx);
            node.setTranslateY(node.getTranslateY()-f*dy);
        }

        public static void zoom(Node node, ScrollEvent event) {
            zoom(node, Math.pow(1.01, event.getDeltaY()), event.getSceneX(), event.getSceneY());
        }
        public static void zoom(Node node, ZoomEvent event) {
            zoom(node, event.getZoomFactor(), event.getSceneX(), event.getSceneY());
        }
    }

    public static class TooltipConfigurator {
        public static void setDelay(Tooltip tooltip, int delay) {
            try {
                Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
                fieldBehavior.setAccessible(true);
                Object objBehavior = fieldBehavior.get(tooltip);

                Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
                fieldTimer.setAccessible(true);
                Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

                objTimer.getKeyFrames().clear();
                objTimer.getKeyFrames().add(new KeyFrame(new Duration(delay)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class CanvasConfigurator {


        /*private static NetVisualisator netVisualisator;

        public static void setNetVisualisator(NetVisualisator _netVisualisator){
            netVisualisator = _netVisualisator;
            if (ca)
        }

        public static class DragAndDropConfigurator {

            private class DragContext {

                double mouseAnchorX;
                double mouseAnchorY;

                double translateAnchorX;
                double translateAnchorY;

            }

            private DragContext sceneDragContext = new DragContext();

            Node canvas;

            public DragAndDropConfigurator(Node canvas) {
                this.canvas = canvas;
            }

            public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
                return onMousePressedEventHandler;
            }

            public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
                return onMouseDraggedEventHandler;
            }

            private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

                public void handle(MouseEvent event) {

                    // right mouse button => panning
                    if( !event.isPrimaryButtonDown())
                        return;

                    sceneDragContext.mouseAnchorX = event.getSceneX();
                    sceneDragContext.mouseAnchorY = event.getSceneY();

                    sceneDragContext.translateAnchorX = canvas.getTranslateX();
                    sceneDragContext.translateAnchorY = canvas.getTranslateY();

                }

            };

            private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {

                    // right mouse button => panning
                    if( !event.isPrimaryButtonDown())
                        return;

                    canvas.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
                    canvas.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

                    event.consume();
                }
            };

            public static double clamp( double value, double min, double max) {

                if( Double.compare(value, min) < 0)
                    return min;

                if( Double.compare(value, max) > 0)
                    return max;

                return value;
            }


        }


        public static class AnimatedZoomOperator {

            private Timeline timeline;

            public AnimatedZoomOperator() {
                this.timeline = new Timeline(60);
            }

            public void zoom(Node node, double factor, double x, double y) {
                // determine scale
                double oldScale = node.getScaleX();
                double scale = oldScale * factor;
                double f = (scale / oldScale) - 1;

                // determine offset that we will have to move the node
                Bounds bounds = node.localToScene(node.getBoundsInLocal());
                double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
                double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));

                // timeline that scales and moves the node
                timeline.getKeyFrames().clear();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), node.getTranslateX() - f * dx)),
                        new KeyFrame(Duration.millis(200), new KeyValue(node.translateYProperty(), node.getTranslateY() - f * dy)),
                        new KeyFrame(Duration.millis(200), new KeyValue(node.scaleXProperty(), scale)),
                        new KeyFrame(Duration.millis(200), new KeyValue(node.scaleYProperty(), scale))
                );
                timeline.play();
            }
        }

        *//*public static void setZoomOnCanvas(Canvas canvas){
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
        }*//*



        public static void setZoomOnCanvas(Canvas canvas){


            canvas.setOnScroll(new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {

                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    if (gc == null) return;
                    Affine affine = gc.getTransform();
                    double z = affine.getMxx() + event.getDeltaY() / 800;
                    if(z <= 0) z = 0;



                    // Чистим:
                    double w = canvas.getWidth();
                    double h = canvas.getHeight();
                    gc.setTransform(1, 0, 0, 1, 0, 0);
                    gc.clearRect(0, 0, w, h);



                    // Масштабирование в левый верхний угол:
                    //gc.setTransform(z, 0, 0, z, 0, 0);

                    // Масштабирование в центр canvas:
                    gc.setTransform(z, 0, 0, z, (w - w * z) / 2.0, (h - h * z) / 2.0);

                    if (netVisualisator != null) {
                        netVisualisator.visualiseNet(canvas);
                    }
                }
            });
        }


        public static void setDragableCanvas(Canvas canvas, Node parent){
            DragAndDropConfigurator dragAndDropConfigurator = new DragAndDropConfigurator(canvas);
        *//*scene.addEventFilter( MouseEvent.MOUSE_PRESSED, dragAndDropConfigurator.getOnMousePressedEventHandler());
        scene.addEventFilter( MouseEvent.MOUSE_DRAGGED, dragAndDropConfigurator.getOnMouseDraggedEventHandler());*//*
            parent.addEventFilter( MouseEvent.MOUSE_PRESSED, dragAndDropConfigurator.getOnMousePressedEventHandler());
            parent.addEventFilter( MouseEvent.MOUSE_DRAGGED, dragAndDropConfigurator.getOnMouseDraggedEventHandler());
        }*/
    }


    private static <T extends TextField & IFXLabelFloatControl> void validationPaneFormatter(
            T jfxTextField) {
        jfxTextField
                .skinProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            JFXTextFieldSkin textFieldSkin = ((JFXTextFieldSkin) newValue);
                            ObservableList childs = textFieldSkin.getChildren();
                            // Get validation pane.
                            // It's always the last child. Be careful no get per type checking -> index can change
                            // -> code will fail.
                            ValidationPane validationPane = (ValidationPane) childs.get(childs.size() - 1);
                            validationPane.setTranslateY(-32);

                            // Set validation label to the right.
                            // Again node is always first child but code can fail in future.
                            StackPane labelStackPane = (StackPane) validationPane.getChildren().get(0);
                            Label innerErrorLabel = (Label) labelStackPane.getChildren().get(0);
                            StackPane.setAlignment(innerErrorLabel, Pos.TOP_RIGHT);
                        });
        // Validate also directly on typing or better text change for not override the error label.
        /*jfxTextField
                .textProperty()
                .addListener((observable, oldValue, newValue) -> jfxTextField.validate());*/
    }

    public static  <T extends TextField & IFXLabelFloatControl> void addValidator(
            T jfxTextField, ValidatorBase validator) {
        jfxTextField.getValidators().add(validator);

        jfxTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                jfxTextField.validate();
            }
        });
        validationPaneFormatter(jfxTextField);

    }
}
