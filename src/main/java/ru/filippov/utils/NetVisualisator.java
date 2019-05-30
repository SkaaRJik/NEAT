package ru.filippov.utils;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.*;
import org.neat4j.neat.core.control.NEATNetManager;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.nn.core.Synapse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class NetVisualisator {
    private static final Logger cat = Logger.getLogger(NetVisualisator.class);
    private NEATNeuralNet net;
    private static final int X_OFFSET = 50;
    private static final int Y_OFFSET = 30;
    private static final int R_OFFSET = 10;
    private static final int F_OFFSET = 20;
    private static double N_SIZE_W = 0;
    private static double N_SIZE_H = 0;

    PseudoClass fromInputToHidden = PseudoClass.getPseudoClass("input-hidden-connection");
    PseudoClass fromInputToOutput = PseudoClass.getPseudoClass("input-output-connection");
    PseudoClass fromHiddenToHidden = PseudoClass.getPseudoClass("hidden-hidden-connection");
    PseudoClass fromHiddenToOutput = PseudoClass.getPseudoClass("hidden-output-connection");
    PseudoClass inputNode = PseudoClass.getPseudoClass("input-node");
    PseudoClass outputNode = PseudoClass.getPseudoClass("output-node");
    PseudoClass hiddenNode = PseudoClass.getPseudoClass("hidden-node");
    PseudoClass active = PseudoClass.getPseudoClass("active");
    PseudoClass inputText = PseudoClass.getPseudoClass("input-text");
    PseudoClass outputText = PseudoClass.getPseudoClass("output-text");


    Map<Integer, DisplayNeuron> neuronsPositions;
    Map<String, Line> connectionsPositions;
    Map<Region, DisplayNeuron> rectangleDisplayNeuronHashMap;



    public void visualiseNet(Pane pane) {
        if (this.net == null) return;

        double canvasW =  pane.getWidth();
        double canvasH =  pane.getHeight();

        pane.getChildren().clear();
        this.drawLinks(pane, canvasW, canvasH);
        drawNeurons(pane, connectionsPositions, canvasW, canvasH);

    }

    private void drawLinks(Pane pane, double canvasW, double canvasH) {


        Synapse[] connections = this.net.getConnections();

        if(connectionsPositions != null){
            connectionsPositions.clear();
        } else {
            connectionsPositions = new HashMap<>(connections.length);
        }



        DisplayNeuron from;
        DisplayNeuron to;
        Line line;
        for (int i = 0; i < connections.length; i++) {

            from = this.neuronsPositions.get(connections[i].getFrom().getID());
            to = this.neuronsPositions.get(connections[i].getTo().getID());
            if(from == null || to == null) continue;
            line = new Line(from.x * canvasW + N_SIZE_W, from.y * canvasH + N_SIZE_H / 2, to.x * canvasW, to.y * canvasH + N_SIZE_H / 2);

            connectionsPositions.put(connections[i].getFrom().getID() + "_" + connections[i].getTo().getID(), line);


            if (!connections[i].isEnabled()) {
                line.getStrokeDashArray().addAll(25d, 10d);
            }

            line.getStyleClass().add("visualisation-item");
            if (from.neuron.neuronType() == NEATNodeGene.TYPE.INPUT && to.neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN) {
                line.pseudoClassStateChanged(fromInputToHidden, true);
            }
            if (from.neuron.neuronType() == NEATNodeGene.TYPE.INPUT && to.neuron.neuronType() == NEATNodeGene.TYPE.OUTPUT) {
                line.pseudoClassStateChanged(fromInputToOutput, true);
            }
            if (from.neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN && to.neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN) {
                line.pseudoClassStateChanged(fromHiddenToHidden, true);
            }
            if (from.neuron.neuronType() == NEATNodeGene.TYPE.HIDDEN && to.neuron.neuronType() == NEATNodeGene.TYPE.OUTPUT) {
                line.pseudoClassStateChanged(fromHiddenToOutput, true);
            }
            Tooltip.install(line, new Tooltip(connections[i].toString()));
            pane.getChildren().add(line);
        }

    }

    private void drawNeurons(Pane pane, Map<String, Line> connectionsPositions,  double canvasW, double canvasH) {

        Pane rectangle;
        if(rectangleDisplayNeuronHashMap==null) {
            rectangleDisplayNeuronHashMap = new HashMap<>(this.net.getNeurons().size());
        } else {
            rectangleDisplayNeuronHashMap.clear();
        }
        Text text = null;
        Line line = null;





        for (DisplayNeuron neuron : this.neuronsPositions.values()) {
            rectangle = new Pane();

            rectangleDisplayNeuronHashMap.put(rectangle, neuron);

            rectangle.setLayoutX(neuron.x() * canvasW);
            rectangle.setLayoutY(neuron.y() * canvasH);
            rectangle.setMaxWidth(N_SIZE_W);
            rectangle.setMaxHeight(N_SIZE_H);
            rectangle.setPrefWidth(N_SIZE_W);
            rectangle.setPrefHeight(N_SIZE_H);

            rectangle.setOnMouseClicked(new EventHandler<MouseEvent>() {
                boolean isActive = false;

                @Override
                public void handle(MouseEvent event) {
                    this.isActive = !isActive;
                    ((Region) event.getSource()).pseudoClassStateChanged(active, isActive);

                    DisplayNeuron mainNeuron = rectangleDisplayNeuronHashMap.get((Region) event.getSource());
                    String synapseId = "";
                    for (NEATNeuron neatNeuron : mainNeuron.neuron.sourceNeurons()) {
                        synapseId = neatNeuron.getID() + "_" + mainNeuron.neuron.id();
                        activateSynapse(synapseId);
                    }

                    for (Synapse synapse : mainNeuron.neuron().getOutSynapses()) {
                        synapseId = mainNeuron.neuron.id() + "_" + synapse.getTo().getID();
                        activateSynapse(synapseId);
                    }
                    ((Region) event.getSource()).toFront();

                }

                private void activateSynapse(String synapseId) {
                    if (connectionsPositions.containsKey(synapseId)) {
                        connectionsPositions.get(synapseId).pseudoClassStateChanged(active, isActive);
                        if (isActive) {
                            connectionsPositions.get(synapseId).toFront();
                        } else {
                            connectionsPositions.get(synapseId).toBack();
                        }
                    }
                }
            });


            if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.INPUT) {

                rectangle.getStyleClass().add("visualisation-item");
                rectangle.pseudoClassStateChanged(inputNode, true);


                text = new Text(neuron.neuron().getLabel());
                text.setX(neuron.x() * canvasW - text.getLayoutBounds().getWidth() - N_SIZE_W);
                text.setY(neuron.y() * canvasH + N_SIZE_H - 8);
                text.getStyleClass().add("visualisation-item");
                text.pseudoClassStateChanged(inputText, true);


                line = new Line(text.getX() + text.getLayoutBounds().getWidth(), rectangle.getLayoutY() + N_SIZE_H / 2, rectangle.getLayoutX(), rectangle.getLayoutY() + N_SIZE_H / 2);
                line.getStyleClass().add("visualisation-item");
                line.pseudoClassStateChanged(inputNode, true);

                pane.getChildren().addAll(line, text);

            } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.HIDDEN) {

                rectangle.getStyleClass().add("visualisation-item");
                rectangle.pseudoClassStateChanged(hiddenNode, true);

            } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.OUTPUT) {

                rectangle.getStyleClass().add("visualisation-item");
                rectangle.pseudoClassStateChanged(outputNode, true);

                text = new Text(neuron.neuron().getLabel());
                text.setX(neuron.x() * canvasW + N_SIZE_W * 2);
                text.setY(neuron.y() * canvasH + N_SIZE_H - 8);
                text.getStyleClass().add("visualisation-item");
                text.pseudoClassStateChanged(outputText, true);

                line = new Line(text.getX(), rectangle.getLayoutY() + N_SIZE_H / 2, rectangle.getLayoutX() + N_SIZE_W, rectangle.getLayoutY() + N_SIZE_H / 2);
                line.getStyleClass().add("visualisation-item");
                line.pseudoClassStateChanged(outputNode, true);

                pane.getChildren().addAll(line, text);

            } else {
                //rectangle.setStroke(Color.BLACK);
                //gc.setStroke(Color.BLACK);
            }

            Tooltip tooltip = new Tooltip(neuron.neuron.toString());


            text = new Text(neuron.neuron().getActivationFunction().getFunctionName());
            //text.setX(neuron.x() * canvasW + (N_SIZE_W / 2 - text.getLayoutBounds().getWidth() / 2));
            //text.setY(neuron.y() * canvasH + N_SIZE_H - 8);
            text.setX((N_SIZE_W / 2 - text.getLayoutBounds().getWidth() / 2));
            text.setY(N_SIZE_H - 8);
            Tooltip.install(rectangle, tooltip);
            Tooltip.install(text, tooltip);

            rectangle.getChildren().add(text);

            pane.getChildren().addAll(rectangle);
        }

    }

    public void setNetToVisualise(File fileNet){
        try {
            Chromosome chromo = (Chromosome)NEATChromosome.readObject(fileNet.getAbsolutePath());
            // need to create a nn based on this chromo.

            //NeuralNet net = createNet(config);
            this.net = (NEATNeuralNet) NEATNeuralNet.createNet(chromo);
            prepareNetToVisualise();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }

    }


    public void setNetToVisualise(NEATNeuralNet net){
        this.net = net;
        prepareNetToVisualise();
    }

    public void setNetToVisualise(Chromosome chromosome, AIConfig config) throws InitialisationFailedException {
        net = NEATNeuralNet.createNet(config, chromosome);
        prepareNetToVisualise();
    }


    public NetVisualisator() {

    }

    private List<List<NEATNeuron>> analyseNeuronStructure() {
        int maxDepth = 1;
        int maxWidth = 0;
        int i;
        int row = 0;
        int col = 0;
        List<NEATNeuron> neurons = this.net.getNeurons();

        NEATNeuron neuron;

        // will only need the first few entries, but htis will cope with wierd structures
        int[] nDepthWidth = new int[neurons.size()];
        int inputs = this.net.netDescriptor().numInputs();

        for (i = 0; i < neurons.size(); i++) {
            if (neurons.get(i).neuronDepth() >= 0 && neurons.get(i).neuronType() != NEATNodeGene.TYPE.INPUT) {
                if (neurons.get(i).neuronType() == NEATNodeGene.TYPE.OUTPUT) {
                    nDepthWidth[0]++;
                } else if (neurons.get(i).neuronType() == NEATNodeGene.TYPE.HIDDEN) {
                    if (neurons.get(i).neuronDepth() > (maxDepth - 1)) {
                        maxDepth = neurons.get(i).neuronDepth() + 1;
                    }
                    nDepthWidth[neurons.get(i).neuronDepth()]++;
                }
                if (nDepthWidth[neurons.get(i).neuronDepth()] > maxWidth) {
                    maxWidth = nDepthWidth[neurons.get(i).neuronDepth()];
                }
            }
        }
        // and one for the inputs
        maxDepth++;
        // ensure array is wide enough
        if (inputs > maxWidth) {
            maxWidth = inputs;
        }

        List<List<NEATNeuron>> neuronStructure = new ArrayList<>(maxDepth);
        for (int j = 0; j < maxDepth; j++) {
            neuronStructure.add(new ArrayList<>(maxWidth));
        }


        nDepthWidth = new int[neurons.size()];

        for (i = 0; i < neurons.size(); i++) {
            neuron = neurons.get(i);
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


   static class DisplayNeuron {
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

        this.neuronsPositions = new HashMap<Integer, DisplayNeuron>(this.net.getNeurons().size());
        int connectionSizeBulb = 6;

        float offsetY;
        float offsetX = 0.20f;

        float currentX = 0f;
        float currentY = 0f;

        this.net.getNeurons().stream().forEach(neatNeuron -> {
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
    }


    /*public NEATNeuralNet initNet(AIConfig config, Chromosome chromo) throws InitialisationFailedException {
        NeuralNet net = null;
        // need to create a nn based on this chromo.
        net = this.createNet(config);
        ((NEATNetDescriptor)(net.netDescriptor())).updateStructure(chromo);
        ((NEATNeuralNet)net).updateNetStructure();
        return (NEATNeuralNet) net;
    }

    public NeuralNet createNet(AIConfig config) throws InitialisationFailedException {
        NEATNetManager netManager = new NEATNetManager();
        netManager.initialise(config, false);
        return ((NEATNeuralNet)netManager.managedNet());
    }*/






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