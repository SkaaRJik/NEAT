package ru.filippov.utils;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNeuron;
import org.neat4j.neat.core.NEATNodeGene;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.nn.core.Synapse;
import ru.filippov.GUI.customNodes.ZoomableCanvas;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AdvancedNetVisualisator {
    private static final Logger cat = Logger.getLogger(AdvancedNetVisualisator.class);
    private NEATNeuralNet net;
    private static final int X_OFFSET = 50;
    private static final int Y_OFFSET = 30;
    private static final int R_OFFSET = 10;
    private static final int F_OFFSET = 20;
    private static double N_SIZE_W = 0;
    private static double N_SIZE_H = 0;

    Map<Integer, DisplayNeuron> neuronsPositions;
    Map<String, DisplayNeuron> connectionsPositions;

    //private DisplayNeuron[] displayNeurons;
    //private  DisplayNeuron[] displaySources;




    public void visualiseNet(Pane pane){
        if (this.net == null ) return;
        //GraphicsContext gc = canvas.getGraphicsContext2D();

        pane.getChildren().clear();

        DisplayNeuron from;
        DisplayNeuron to;
        double canvasW = ((StackPane)pane.getParent()).getWidth();
        double canvasH = ((StackPane)pane.getParent()).getHeight();
        Region  rectangle;
        Text text = null;
        //gc.setFill(Color.BLACK);
        //this.drawNeurons(gc);
        Line line = null;
        Synapse[] connections = net.getConnections();
        /*Path path = null;
        MoveTo moveFrom = null;
        LineTo quadTo = null;*/

        PseudoClass fromInputToHidden = PseudoClass.getPseudoClass("input-hidden");
        PseudoClass fromInputToOutput = PseudoClass.getPseudoClass("input-output");
        PseudoClass fromHiddenToHidden = PseudoClass.getPseudoClass("hidden-hidden");
        PseudoClass fromHiddenToOutput = PseudoClass.getPseudoClass("hidden-output");

        for (int i = 0; i < connections.length; i++) {

                from = this.neuronsPositions.get(connections[i].getFrom().getID());
                to = this.neuronsPositions.get(connections[i].getTo().getID());

                line = new Line(from.x * canvasW + N_SIZE_W, from.y * canvasH + N_SIZE_H / 2, to.x * canvasW, to.y * canvasH + N_SIZE_H / 2);
                if(!connections[i].isEnabled()) {
                    line.getStrokeDashArray().addAll(25d, 10d);
                }
                if(from.neuron.neuronType() == NEATNodeGene.TYPE.INPUT && to.neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN)
                line.getStyleClass().add("visualisation-content-input-node");
                /*line.getPseudoClassStates().add(new PseudoClass() {

                })*/

                //moveFrom = new MoveTo(from.x * canvasW + N_SIZE_W, from.y * canvasH + N_SIZE_H / 2);
                //quadTo = new LineTo(to.x * canvasW, to.y * canvasH + N_SIZE_H / 2);

                //path.getElements().addAll(moveFrom, quadTo);

                Tooltip.install(line, new Tooltip(connections[i].toString()));

                pane.getChildren().add(line);





        }


        Path path = null;

        for(DisplayNeuron neuron : this.neuronsPositions.values()){
            rectangle = new Region();
            rectangle.setLayoutX(neuron.x()*canvasW);
            rectangle.setLayoutY(neuron.y()*canvasH);
            rectangle.setMaxWidth(N_SIZE_W);
            rectangle.setMaxHeight(N_SIZE_H);
            rectangle.setPrefWidth(N_SIZE_W);
            rectangle.setPrefHeight(N_SIZE_H);

            /*rectangle.setFill(Color.TRANSPARENT);
            rectangle.setArcHeight(25);
            rectangle.setArcWidth(25);*/
            if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.INPUT) {

                rectangle.getStyleClass().add("visualisation-content-input-node");

                text = new Text(neuron.neuron().getLabel());
                text.setX(neuron.x()*canvasW - text.getLayoutBounds().getWidth()-N_SIZE_W);
                text.setY(neuron.y()*canvasH+N_SIZE_H-8);
                text.getStyleClass().add("visualisation-content-input-text");

                line = new Line(text.getX() + text.getLayoutBounds().getWidth(), text.getY() , rectangle.getLayoutX(), rectangle.getLayoutY() + N_SIZE_H/2);
                line.getStyleClass().add("visualisation-content-input-node");
                pane.getChildren().addAll(line, text);
                /*path = new Path();



                path.setStroke(Color.WHITE);

                moveFrom = new MoveTo(text.getX() + text.getLayoutBounds().getWidth(), text.getY()  );
                quadTo = new LineTo(rectangle.getLayoutX(), rectangle.getLayoutY() + N_SIZE_H/2);

                path.getElements().addAll(moveFrom, quadTo);

                pane.getChildren().addAll(path, text);*/


                //gc.setStroke(Color.MAGENTA);
                //gc.strokeText(neuron.neuron().getLabel(), neuron.x*canvasW - N_SIZE_W * 3, neuron.y()*canvasH+N_SIZE_H-4);
            } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.HIDDEN) {
                /*rectangle.setStroke(Color.web("#00A08A"));
                rectangle.setFill(Color.web("#5DD0C0"));*/
                rectangle.getStyleClass().add("visualisation-content-hidden-node");

            } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.OUTPUT) {

                /*rectangle.setStroke(Color.web("#D0006E"));
                rectangle.setFill(Color.web("#E868AB"));*/
                rectangle.getStyleClass().add("visualisation-content-output-node");




                text = new Text(neuron.neuron().getLabel());
                text.setX(neuron.x()*canvasW+N_SIZE_W*2);
                text.setY(neuron.y()*canvasH+N_SIZE_H-8);
                text.getStyleClass().add("visualisation-content-output-text");
                line = new Line(text.getX(), text.getY() , rectangle.getLayoutX(), rectangle.getLayoutY() + N_SIZE_H/2);

                line.getStyleClass().add("visualisation-content-output-node");

                pane.getChildren().addAll(line, text);

            } else {
                //rectangle.setStroke(Color.BLACK);
                //gc.setStroke(Color.BLACK);
            }

            Tooltip tooltip = new Tooltip(neuron.neuron.toString());


            text = new Text(neuron.neuron().getActivationFunction().getFunctionName());
            text.setX(neuron.x()*canvasW+(N_SIZE_W/2 - text.getLayoutBounds().getWidth()/2));
            text.setY(neuron.y()*canvasH+N_SIZE_H-8);
            Tooltip.install(rectangle, tooltip);
            Tooltip.install(text, tooltip);



            pane.getChildren().addAll(rectangle, text);



            //gc.strokeRoundRect(neuron.x()*canvasW, neuron.y()*canvasH, N_SIZE_W, N_SIZE_H,5,5);
            //gc.setStroke(Color.BLACK);
            //gc.strokeText(neuron.neuron().getActivationFunction().getFunctionName(), neuron.x()*canvasW+4, neuron.y()*canvasH+N_SIZE_H-4);
        }


        // now create the links
        /*for (int i = 0; i < displayNeurons.length; i++) {
            from = displayNeurons[i];
            if (from != null) {
                displaySources = this.findDisplaySources(displayNeurons, from);
                for (int j = 0; j < displaySources.length; j++) {
                    this.drawLink(from, displaySources[j], gc);
                }
                this.drawNeuron(from, gc);
            }
        }*/
        /*gc.setFill(Color.LIGHTGREEN);
        gc.fillOval(60, 10, 180, 180);
        gc.setFill(Color.WHITE);
        gc.fillOval(100, 50, 100, 100);*/
    }

    public void visualiseNet(Canvas canvas){
        if (this.net == null ) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();


        if (canvas instanceof ZoomableCanvas) ((ZoomableCanvas) canvas).clean();
        DisplayNeuron from;
        DisplayNeuron to;
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();
        Rectangle rectangle;
        gc.setFill(Color.BLACK);
        //this.drawNeurons(gc);
        for(DisplayNeuron neuron : this.neuronsPositions.values()){
            rectangle = new Rectangle(neuron.x()*canvasW, neuron.y()*canvasH, N_SIZE_W, N_SIZE_H);

            if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.INPUT) {
                rectangle.setStroke(Color.MAGENTA);

                //gc.setStroke(Color.MAGENTA);
                //gc.strokeText(neuron.neuron().getLabel(), neuron.x*canvasW - N_SIZE_W * 3, neuron.y()*canvasH+N_SIZE_H-4);
            } else if (neuron.neuron().sourceNeurons().size() == 0) {
                rectangle.setStroke(Color.GREEN);
                //gc.setStroke(Color.GREEN);
            } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.OUTPUT) {
                gc.setStroke(Color.ORANGE);
                //gc.setStroke(Color.ORANGE);
            } else {
                gc.setStroke(Color.BLACK);
                //gc.setStroke(Color.BLACK);
            }



            //gc.strokeRoundRect(neuron.x()*canvasW, neuron.y()*canvasH, N_SIZE_W, N_SIZE_H,5,5);
            //gc.setStroke(Color.BLACK);
            gc.strokeText(neuron.neuron().getActivationFunction().getFunctionName(), neuron.x()*canvasW+4, neuron.y()*canvasH+N_SIZE_H-4);
        }


        // now create the links
        /*for (int i = 0; i < displayNeurons.length; i++) {
            from = displayNeurons[i];
            if (from != null) {
                displaySources = this.findDisplaySources(displayNeurons, from);
                for (int j = 0; j < displaySources.length; j++) {
                    this.drawLink(from, displaySources[j], gc);
                }
                this.drawNeuron(from, gc);
            }
        }*/
        /*gc.setFill(Color.LIGHTGREEN);
        gc.fillOval(60, 10, 180, 180);
        gc.setFill(Color.WHITE);
        gc.fillOval(100, 50, 100, 100);*/
    }

    private void drawNeurons(GraphicsContext gc) {


    }


    public void setNetToVisualise(NEATNeuralNet net){
        this.net = net;
        prepareNetToVisualise();
    }

    public void setNetToVisualise(Chromosome chromosome, AIConfig config) throws InitialisationFailedException {
        net = initNet(config, chromosome);
        prepareNetToVisualise();
    }


    public AdvancedNetVisualisator() {

    }

    private List<List<NEATNeuron>> analyseNeuronStructure() {
        int maxDepth = 1;
        int maxWidth = 0;
        int i;
        int row = 0;
        int col = 0;
        NEATNeuron[] neurons = this.net.neurons();
        List<List<NEATNeuron>> neuronStructure;
        NEATNeuron neuron;

        // will only need the first few entries, but htis will cope with wierd structures
        int[] nDepthWidth = new int[neurons.length];
        int inputs = this.net.netDescriptor().numInputs();

        for (i = 0; i < neurons.length; i++) {
            if (neurons[i].neuronDepth() >= 0 && neurons[i].neuronType() != NEATNodeGene.TYPE.INPUT) {
                if (neurons[i].neuronType() == NEATNodeGene.TYPE.OUTPUT) {
                    nDepthWidth[0]++;
                } else if (neurons[i].neuronType() == NEATNodeGene.TYPE.HIDDEN) {
                    if (neurons[i].neuronDepth() > (maxDepth - 1)) {
                        maxDepth = neurons[i].neuronDepth() + 1;
                    }
                    nDepthWidth[neurons[i].neuronDepth()]++;
                }
                if (nDepthWidth[neurons[i].neuronDepth()] > maxWidth) {
                    maxWidth = nDepthWidth[neurons[i].neuronDepth()];
                }
            }
        }
        // and one for the inputs
        maxDepth++;
        // ensure array is wide enough
        if (inputs > maxWidth) {
            maxWidth = inputs;
        }
        neuronStructure = new ArrayList<>(maxDepth);
        for (int j = 0; j < maxDepth; j++) {
            neuronStructure.add(new ArrayList<>(maxWidth));
        }


        nDepthWidth = new int[neurons.length];

        for (i = 0; i < neurons.length; i++) {
            neuron = neurons[i];
            if (neuron.neuronDepth() >= 0) {
                if(neuron.neuronType() == NEATNodeGene.TYPE.INPUT){
                    row = 0;
                } else {
                    row = maxDepth - 1 - neuron.neuronDepth();
                }

                neuronStructure.get(row).add(neuron);
            }
        }

        return (neuronStructure);
    }


    class DisplayNeuron {
        private NEATNeuron neuron;
        private float x;
        private float y;

        public DisplayNeuron(NEATNeuron neuron, float x, float y) {
            this.neuron = neuron;
            this.x = x;
            this.y = y;
        }

        public float x() {
            return (this.x);
        }

        public float y() {
            return (this.y);
        }

        public NEATNeuron neuron() {
            return (this.neuron);
        }
    }

    class DisplayConnection{
        private Synapse synapse;
        List<Point> points;

        public DisplayConnection(Synapse synapse) {
            this.synapse = synapse;
            this.points = new ArrayList<>(5);
        }

        public Synapse getSynapse() {
            return synapse;
        }

        public void setSynapse(Synapse synapse) {
            this.synapse = synapse;
        }

        public List<Point> getPoints() {
            return points;
        }

        public void setPoints(List<Point> points) {
            this.points = points;
        }
    }



    private void prepareNetToVisualise(){

        List<List<NEATNeuron>> structure = this.analyseNeuronStructure();
        NEATNeuron neuron;

        Synapse[] connections = this.net.getConnections();


        this.neuronsPositions = new HashMap<Integer, DisplayNeuron>(this.net.neurons().length);
        this.connectionsPositions = new HashMap<String, DisplayNeuron>(this.net.getConnections().length);




        int connectionSizeBulb = 6;

        float offsetY;
        float offsetX = 0.20f;

        float currentX = 0f;
        float currentY = 0f;

        Arrays.stream(this.net.neurons()).forEach(neatNeuron -> {
            Text text = new Text(neatNeuron.getActivationFunction().getFunctionName());
            N_SIZE_W = Double.max(text.getLayoutBounds().getWidth()+20, N_SIZE_W);
            N_SIZE_H = Double.max(text.getLayoutBounds().getHeight()+4, N_SIZE_H);
        });

        for (List<NEATNeuron> layer : structure){
            currentX += offsetX;
            offsetY = 1.0f / (layer.size()+1);
            currentY = offsetY;
            for (NEATNeuron neatNeuron : layer){
                neuronsPositions.put(neatNeuron.id(), new DisplayNeuron(neatNeuron, currentX, currentY));
                currentY += offsetY;
            }
        }






        //offsetX = ((float) (canvasW)) / (structure.length);
        //lastX = (int) (offsetX - nodeSize/2);






        /*//Set<Integer> isRectdrawn = new HashSet<>(topology.getTotalNeurons());
        for(Set<Neuron> layer : topology.getLayers()){
            offsetY = ((float) (imageSize)) / (layer.size()+1);
            lastY = (int) (offsetY) - nodeSize/2;
                int y = (int) (lastY + AdvancedRandom.nextFloat(-1f, 1f) * (offsetY/2));
                neuronsPositions.put(neuron.getID(), new Point(lastX, y));
                g.fillOval(lastX, y , nodeSize, nodeSize);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(neuron.getID()+1), lastX, y);
                for(Synapse inputSynapses : neuron.getInputConnections()){
                    if(!inputSynapses.isEnabled()) continue;
                    Point point = neuronsPositions.get(inputSynapses.getFromNeuronID());
                    g.drawLine(point.x+nodeSize, point.y+nodeSize/2, lastX, y+nodeSize/2);
                    if(!isRectdrawn.contains(neuron.getID())){
                        g.fillRect(lastX-cubeSize/2, y+nodeSize/2-cubeSize/2, cubeSize, cubeSize);
                        isRectdrawn.add(neuron.getID());
                    }
                }
                lastY += offsetY;
            }
            lastX += offsetX;
        }*/



        /*displayNeurons = new DisplayNeuron[structure.length * structure[0].length];

        ArrayList structureList = new ArrayList();
        ArrayList rowList;

        for (row = 0; row < structure.length; row++) {
            rowList = new ArrayList();
            for (col = 0; col < structure[0].length; col++) {
                neuron = structure[row][col];
                if (neuron != null) {
                    rowList.add(neuron);
                }
            }
            structureList.add(rowList);
        }

        for (row = 0; row < structureList.size(); row++) {
            rowList = (ArrayList)structureList.get(row);
            for (col = 0; col < rowList.size(); col++) {
                neuron = (NEATNeuron)rowList.get(col);
                displayNeurons[(row * structure[0].length) + col] = new DisplayNeuron(neuron, (int)Math.round((this.canvasW / (rowList.size() + 1)) * (col + 1)), (int)Math.round(((this.canvasH / structureList.size()) - (2 * F_OFFSET)) * (row + 1)));
            }
        }*/
    }


    public NEATNeuralNet initNet(AIConfig config, Chromosome chromo) throws InitialisationFailedException {
        NeuralNet net = null;
        // need to create a nn based on this chromo.
        net = this.createNet(config);
        ((NEATNetDescriptor)(net.netDescriptor())).updateStructure(chromo);
        ((NEATNeuralNet)net).updateNetStructure();
        return (NEATNeuralNet) net;
    }

    public NeuralNet createNet(AIConfig config) throws InitialisationFailedException {
        NEATNetManager netManager = new NEATNetManager();
        netManager.initialise(config);
        return ((NEATNeuralNet)netManager.managedNet());
    }

    private DisplayNeuron[] findDisplaySources(DisplayNeuron[] displayNeurons, DisplayNeuron from) {
        ArrayList sourceNeurons = from.neuron().sourceNeurons();
        DisplayNeuron[] targets = new DisplayNeuron[sourceNeurons.size()];
        int i;

        for (i = 0; i < targets.length; i++) {
            targets[i] = this.findTarget(displayNeurons, ((NEATNeuron)sourceNeurons.get(i)).id());
        }

        return (targets);
    }

    private DisplayNeuron findTarget(DisplayNeuron[] displayNeurons, int id) {
        int i = 0;
        boolean found = false;
        DisplayNeuron target = null;

        while (i < displayNeurons.length && ! found) {
            if (displayNeurons[i] != null) {
                if (displayNeurons[i].neuron().id() == id) {
                    target = displayNeurons[i];
                    found = true;
                }
            }
            i++;
        }

        return (target);
    }




    /*private void drawNeuron(DisplayNeuron neuron, GraphicsContext g) {
        if (neuron.neuron().neuronType() == NEATNodeGene.INPUT) {
            g.setFill(Color.MAGENTA);
        } else if (neuron.neuron().sourceNeurons().size() == 0) {
            g.setFill(Color.GREEN);
        } else if (neuron.neuron().neuronType() == NEATNodeGene.OUTPUT) {
            g.setFill(Color.ORANGE);
        } else {
            g.setFill(Color.BLACK);
        }
        g.fillRoundRect(neuron.x(), neuron.y(), N_SIZE, N_SIZE,5,5);
        g.setFill(Color.WHITE);
        g.strokeText("id=" + neuron.neuron().id() + "("+neuron.neuron().getActivationFunction().getFunctionName()+")", neuron.x(), neuron.y());
    }

    private void drawLink(DisplayNeuron from, DisplayNeuron to, GraphicsContext g) {
        if (from.neuron().id() == to.neuron().id()) {
            g.setFill(Color.BLUE);
            g.strokeLine(from.x() + N_SIZE, from.y() + R_OFFSET, to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET);
            g.strokeLine(to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET, to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET + N_SIZE);
            g.strokeLine(to.x() + N_SIZE + R_OFFSET, to.y() + R_OFFSET + N_SIZE, to.x() + R_OFFSET, to.y() + R_OFFSET + N_SIZE);
            g.strokeLine(to.x() + R_OFFSET, to.y() + R_OFFSET + N_SIZE, to.x() + R_OFFSET, to.y());
        } else if (from.neuron().neuronDepth() >= to.neuron().neuronDepth()) {
            g.setFill(Color.YELLOW);
            g.strokeLine(to.x(), to.y(), to.x() - R_OFFSET, to.y() - R_OFFSET);
            g.strokeLine(to.x() - R_OFFSET, to.y() - R_OFFSET, to.x() - R_OFFSET, to.y() + R_OFFSET);
            g.strokeLine(to.x() - R_OFFSET, to.y() + R_OFFSET, from.x(), from.y());
        } else {
            g.setFill(Color.RED);
            g.strokeLine(to.x() + (N_SIZE / 2), to.y() + (N_SIZE / 2), to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2));
            g.strokeLine(to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2), to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2) - F_OFFSET);
            g.strokeLine(to.x() + (N_SIZE / 2) - F_OFFSET, to.y() + (N_SIZE / 2) - F_OFFSET, from.x() + (N_SIZE / 2), from.y() + (N_SIZE / 2));
        }
    }*/

}
