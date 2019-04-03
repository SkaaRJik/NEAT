package ru.filippov.utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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
import ru.filippov.GUI.customNodes.ZoomableCanvas;

import java.util.ArrayList;

public class NetVisualisator {



    private static final Logger cat = Logger.getLogger(NetVisualisator.class);
    private NEATNeuralNet net;
    private static final int X_OFFSET = 50;
    private static final int Y_OFFSET = 30;
    private static final int R_OFFSET = 10;
    private static final int F_OFFSET = 20;
    private static final int N_SIZE = 15;

    private DisplayNeuron[] displayNeurons;
    private  DisplayNeuron[] displaySources;

    double canvasW;
    double canvasH;

    public void visualiseNet(Canvas canvas){
        if (this.net == null ) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();


        if (canvas instanceof ZoomableCanvas) ((ZoomableCanvas) canvas).clean();
        DisplayNeuron from;
        DisplayNeuron to;

        // now create the links
        for (int i = 0; i < displayNeurons.length; i++) {
            from = displayNeurons[i];
            if (from != null) {
                displaySources = this.findDisplaySources(displayNeurons, from);
                for (int j = 0; j < displaySources.length; j++) {
                    this.drawLink(from, displaySources[j], gc);
                }
                this.drawNeuron(from, gc);
            }
        }
        /*gc.setFill(Color.LIGHTGREEN);
        gc.fillOval(60, 10, 180, 180);
        gc.setFill(Color.WHITE);
        gc.fillOval(100, 50, 100, 100);*/
    }



    public void setNetToVisualise(NEATNeuralNet net){
        this.net = net;
        prepareNet();
    }

    public void setNetToVisualise(Chromosome chromosome, AIConfig config) throws InitialisationFailedException {
        net = initNet(config, chromosome);
        prepareNet();
    }


    public NetVisualisator(Canvas canvas) {
        canvasW = canvas.getWidth();
        canvasH = canvas.getHeight();
    }

    private NEATNeuron[][] analyseNeuronStructure() {
        int maxDepth = 1;
        int maxWidth = 0;
        int i;
        int row = 0;
        int col = 0;
        NEATNeuron[] neurons = this.net.neurons();
        NEATNeuron[][] neuronStructure;
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
        neuronStructure = new NEATNeuron[maxDepth][maxWidth];
        nDepthWidth = new int[neurons.length];

        for (i = 0; i < neurons.length; i++) {
            neuron = neurons[i];
            if (neuron.neuronDepth() >= 0) {
                if (neuron.neuronType() == NEATNodeGene.TYPE.INPUT) {
                    row = maxDepth - 1;
                } else {
                    row = neuron.neuronDepth();
                }
                col = nDepthWidth[row];
                neuronStructure[row][col] = neuron;
                nDepthWidth[row]++;
            }
        }

        return (neuronStructure);
    }


    class DisplayNeuron {
        private NEATNeuron neuron;
        private int x;
        private int y;

        public DisplayNeuron(NEATNeuron neuron, int x, int y) {
            this.neuron = neuron;
            this.x = x;
            this.y = y;
        }

        public int x() {
            return (this.x);
        }

        public int y() {
            return (this.y);
        }

        public NEATNeuron neuron() {
            return (this.neuron);
        }
    }



    private void prepareNet(){
        int row = 0;
        int col = 0;
        NEATNeuron[][] structure = this.analyseNeuronStructure();
        NEATNeuron neuron;

        displayNeurons = new DisplayNeuron[structure.length * structure[0].length];

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
        }
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




    private void drawNeuron(DisplayNeuron neuron, GraphicsContext g) {
        if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.INPUT) {
            g.setFill(Color.MAGENTA);
        } else if (neuron.neuron().sourceNeurons().size() == 0) {
            g.setFill(Color.GREEN);
        } else if (neuron.neuron().neuronType() == NEATNodeGene.TYPE.OUTPUT) {
            g.setFill(Color.ORANGE);
        } else {
            g.setFill(Color.BLACK);
        }
        g.fillRoundRect(neuron.x(), neuron.y(), N_SIZE, N_SIZE,5,5);
        g.setFill(Color.WHITE);
        g.strokeText("id=" + neuron.neuron().id() + "("+neuron.neuron.getActivationFunction().getFunctionName()+")", neuron.x(), neuron.y());
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
    }

}
