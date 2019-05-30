package ru.filippov.utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.poi.xwpf.usermodel.*;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.nn.core.functions.ActivationFunctionFinder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TrainReporter {

    static final CTHMerge hMergeStart = CTHMerge.Factory.newInstance();
    static final CTHMerge hMergeCont = CTHMerge.Factory.newInstance();
    static final CTVMerge vMergeStart = CTVMerge.Factory.newInstance();
    static final CTVMerge vMergeCont = CTVMerge.Factory.newInstance();


    List<List<Chromosome>> bestChromosomes;
    List<AIConfig> configs;
    DataKeeper dataKeeper;
    List<BufferedImage> images;
    ResourceBundle resourceBundle;




    static {
        hMergeStart.setVal(STMerge.RESTART);
        hMergeCont.setVal(STMerge.CONTINUE);
        vMergeStart.setVal(STMerge.RESTART);
        vMergeCont.setVal(STMerge.CONTINUE);
    }

    public List<List<Chromosome>> getBestChromosomes() {
        return bestChromosomes;
    }

    public TrainReporter(DataKeeper dataKeeper, ResourceBundle resource) {
        bestChromosomes = new ArrayList<>();
        configs = new ArrayList<>();
        images = new ArrayList<>();
        this.dataKeeper = dataKeeper;
        this.resourceBundle = resource;
    }

    public void addBestChromosomesSet(List<Chromosome> chromosomeList){
        this.bestChromosomes.add(chromosomeList);
    }

    public void addConfig(AIConfig config){
        this.configs.add(config);
    }

    public boolean createReport(File destination, XWPFDocument document)  {
        if (destination != null) {
            if(document == null)
                document = new XWPFDocument();
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(destination);
                writeReport(document);
                document.write(out);
                out.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    public void writeReport(XWPFDocument document) throws IOException, InvalidFormatException {
        createDocParagraph(document, "Отчет по тренировке сети", ParagraphAlignment.CENTER);

        createDocParagraph(document, "Входов сети : " + this.dataKeeper.getInputs() + " Выходов : " + this.dataKeeper.getOutputs(), ParagraphAlignment.LEFT);


        createDocParagraph(document, "Тренировочный набор", ParagraphAlignment.LEFT);
        List<List<Double>> dataToWrite = dataKeeper.getTrainData();
        IntegerProperty legendIndex = new SimpleIntegerProperty(0);


        XWPFTable table = document.createTable(dataToWrite.size()+1, dataToWrite.get(0).size()+1);
        writeDataIntoTable(table, dataToWrite, legendIndex);

        dataToWrite = dataKeeper.getTestData();
        if(dataToWrite != null) {
            createDocParagraph(document, "Тестирующий набор", ParagraphAlignment.LEFT);

            table = document.createTable(dataToWrite.size() + 1, dataToWrite.get(0).size() + 1);
            writeDataIntoTable(table, dataToWrite, legendIndex);
        }


        createDocParagraph(document, "Ошибки", ParagraphAlignment.LEFT);
        createTotalTableWithErrors(document);

        createDocParagraph(document, "Значения", ParagraphAlignment.LEFT);
        createTableWithOutputs(document);



        for (int i = 0; i < this.configs.size(); i++) {



            document.createParagraph().createRun().addBreak(BreakType.PAGE);
            createDocParagraph(document, "Параметры тренировки №" + (i+1), ParagraphAlignment.CENTER);
            if(images.size() != 0) {
                writeBriefing(document, configs.get(i), bestChromosomes.get(i), images.get(i));
            } else {
                writeBriefing(document, configs.get(i), bestChromosomes.get(i), null);
            }
            createDocParagraph(document, "", ParagraphAlignment.LEFT);
            writeAIConfig(document, configs.get(i));
            createDocParagraph(document, "", ParagraphAlignment.LEFT);
            createVerticalTableWithErrors(document, bestChromosomes.get(i));


        }

    }

    private void writeBriefing(XWPFDocument document, AIConfig config, List<Chromosome> listBestChromosomes, BufferedImage bufferedImage) throws IOException, InvalidFormatException {
        XWPFTable table = document.createTable(8, 4);

        table.getRow(0).getCell(0).setText("Слой");
        table.getRow(1).getCell(0).setText("Входной");
        table.getRow(2).getCell(0).setText("Скрытые");
        table.getRow(3).getCell(0).setText("Выходной");

        Chromosome lastBestChromosome = listBestChromosomes.get(listBestChromosomes.size()-1);


        table.getRow(0).getCell(1).setText("Активац. функция");
        writeActivationIntoTable(table, ActivationFunctionFinder.getConcatFunctions(config.configElement("INPUT.ACTIVATIONFUNCTIONS")), 1);
        writeActivationIntoTable(table, ActivationFunctionFinder.getConcatFunctions(config.configElement("HIDDEN.ACTIVATIONFUNCTIONS")), 2);
        writeActivationIntoTable(table, ActivationFunctionFinder.getConcatFunctions(config.configElement("OUTPUT.ACTIVATIONFUNCTIONS")), 3);
       

        table.getRow(0).getCell(2).setText("Ошибка обучения");
        table.getRow(1).getCell(2).getCTTc().addNewTcPr();
        table.getRow(1).getCell(2).getCTTc().getTcPr().setVMerge(vMergeStart);
        table.getRow(2).getCell(2).getCTTc().addNewTcPr();
        table.getRow(2).getCell(2).getCTTc().getTcPr().setVMerge(vMergeCont);
        table.getRow(3).getCell(2).getCTTc().addNewTcPr();
        table.getRow(3).getCell(2).getCTTc().getTcPr().setVMerge(vMergeCont);
        table.getRow(1).getCell(2).setText(String.valueOf(lastBestChromosome.getTrainError()).replace(".",","));
        table.getRow(1).getCell(2).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);


        table.getRow(0).getCell(3).setText("Ошибка тестирования");
        table.getRow(1).getCell(3).getCTTc().addNewTcPr();
        table.getRow(1).getCell(3).getCTTc().getTcPr().setVMerge(vMergeStart);
        table.getRow(2).getCell(3).getCTTc().addNewTcPr();
        table.getRow(2).getCell(3).getCTTc().getTcPr().setVMerge(vMergeCont);
        table.getRow(3).getCell(3).getCTTc().addNewTcPr();
        table.getRow(3).getCell(3).getCTTc().getTcPr().setVMerge(vMergeCont);
        table.getRow(1).getCell(3).setText(String.valueOf(lastBestChromosome.getValidationError()).replace(".",","));
        table.getRow(1).getCell(3).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);


        createMergedRow(table, 4, "График");
        createMergedRow(table, 5, null);
        createMergedRow(table, 6, "Топология");
        createMergedRow(table, 7, null);
        if(bufferedImage!=null)
        writePictureOfTopology(table.getRow(7).getCell(0).getParagraphs().get(0), bufferedImage);
















    }

    private void createMergedRow(XWPFTable table, int rowInd, String text) {
        table.getRow(rowInd).getCell(0).getCTTc().addNewTcPr();
        table.getRow(rowInd).getCell(0).getCTTc().getTcPr().setHMerge(hMergeStart);
        table.getRow(rowInd).getCell(1).getCTTc().addNewTcPr();
        table.getRow(rowInd).getCell(1).getCTTc().getTcPr().setHMerge(hMergeCont);
        table.getRow(rowInd).getCell(2).getCTTc().addNewTcPr();
        table.getRow(rowInd).getCell(2).getCTTc().getTcPr().setHMerge(hMergeCont);
        table.getRow(rowInd).getCell(3).getCTTc().addNewTcPr();
        table.getRow(rowInd).getCell(3).getCTTc().getTcPr().setHMerge(hMergeCont);
        if(text!=null)
            table.getRow(rowInd).getCell(0).setText(text);
        table.getRow(rowInd).getCell(0).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
    }

    private void writeActivationIntoTable(XWPFTable table, String activations, int pos) {
        table.getRow(pos).getCell(1).setText(activations);
    }

    private void writePictureOfTopology(XWPFParagraph paragraph, BufferedImage image) throws IOException, InvalidFormatException {

        XWPFRun paragraphConfig = paragraph.createRun();

        ByteArrayOutputStream os = new ByteArrayOutputStream();


        ImageIO.write(image, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        paragraphConfig.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, "Input 0", Units.toEMU(500), Units.toEMU(this.images.get(0).getHeight()));
    }

    private void writeDataIntoTable(XWPFTable table, List<List<Double>> dataToWrite, IntegerProperty legendStartIndex) {
        XWPFTableRow tableRowOne;
        tableRowOne = table.getRow(0);
        List<String> headersForTableView = dataKeeper.getHeadersForTableView();
        for (int i = 0; i < headersForTableView.size(); i++) {
            String suffix = "\n ( Легенда )";
            if(i > 0) {
                suffix = (i) <= dataKeeper.getInputs() ? "\n ( Вход )" : "\n( Выход )";
            }
            tableRowOne.getCell(i).setText(headersForTableView.get(i)+suffix);
        }

        for (int i = 0; i < dataToWrite.size(); i++) {
            tableRowOne = table.getRow(i+1);

            for (int j = 0; j < dataToWrite.get(i).size(); j++) {
                if(j==0) {
                    tableRowOne.getCell(0).setText(String.valueOf(dataKeeper.getLegend().get(legendStartIndex.getValue())).replace(".", ","));
                    legendStartIndex.setValue(legendStartIndex.getValue() + 1);
                }
                    tableRowOne.getCell(j+1).setText(String.valueOf(dataToWrite.get(i).get(j)).replace(".", ","));
            }
        }
    }


    protected XWPFParagraph createDocParagraph(XWPFDocument docxModel, String text, ParagraphAlignment alignment){
        XWPFParagraph bodyParagraph = docxModel.createParagraph();
        XWPFRun paragraphConfig = bodyParagraph.createRun();
        paragraphConfig.setFontSize(14);
        bodyParagraph.setAlignment(alignment);
        // HEX цвет без решетки #
        paragraphConfig.setText( text  );
        return bodyParagraph;
    }

    protected void writeAIConfig(XWPFDocument docxDoc, AIConfig config){
        createDocParagraph(docxDoc, resourceBundle.getString("GA_SETTINGS"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc,  resourceBundle.getString("GENERATOR_SEED") + " : " + config.configElement("GENERATOR.SEED"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("MUTATION_PROBABILITY") + " : " + config.configElement("PROBABILITY.MUTATION"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("CROSSOVER_PROBABILITY") + " : " + config.configElement("PROBABILITY.CROSSOVER"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("ADD_LINK_PROBABILITY") + " : " + config.configElement("PROBABILITY.ADDLINK"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("ADD_NODE_PROBABILITY") + " : " + config.configElement("PROBABILITY.ADDNODE"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("NEW_ACTIVATION_FUNCTION_PROBABILITY") + " : " + config.configElement("PROBABILITY.NEWACTIVATIONFUNCTION"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("MUTATE_BIAS_PROBABILITY") + " : " + config.configElement("PROBABILITY.MUTATEBIAS"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("TOGGLE_LINK_PROBABILITY") + " : " +config.configElement("PROBABILITY.TOGGLELINK"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("WEIGHT_REPLACED_PROBABILITY") + " : " + config.configElement("PROBABILITY.WEIGHT.REPLACED"), ParagraphAlignment.LEFT);
    
       
        createDocParagraph(docxDoc, resourceBundle.getString("ACTIVATION_FUNCTION") , ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("OUTPUT_ACTIVATION_FUNCTIONS") + " : " + ActivationFunctionFinder.getConcatFunctions(config.configElement("INPUT.ACTIVATIONFUNCTIONS")) , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("INPUT_ACTIVATION_FUNCTIONS") + " : " + ActivationFunctionFinder.getConcatFunctions(config.configElement("HIDDEN.ACTIVATIONFUNCTIONS")) , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("HIDDEN_ACTIVATION_FUNCTIONS") + " : " + ActivationFunctionFinder.getConcatFunctions(config.configElement("OUTPUT.ACTIVATIONFUNCTIONS")) , ParagraphAlignment.LEFT);
        
        createDocParagraph(docxDoc, resourceBundle.getString("EPOCH_CONTROL"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("POP_SIZE") + " : " + config.configElement("POP.SIZE") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("NUMBER_EPOCHS") + " : " + config.configElement("NUMBER.EPOCHS") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("TERMINATION_VALUE_TOGGLE") + " : " + config.configElement("TERMINATION.VALUE.TOGGLE") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("TERMINATION_VALUE") + " : " + config.configElement("TERMINATION.VALUE") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("KEEP_BEST_EVER") + " : " +  config.configElement("KEEP.BEST.EVER"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("EXTRA_FEATURE_COUNT") + " : " + config.configElement("EXTRA.FEATURE.COUNT") , ParagraphAlignment.LEFT);

        createDocParagraph(docxDoc, resourceBundle.getString("NETWORK_CONTROL"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("INPUT_NODES")  + " : " + config.configElement("INPUT.NODES") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("OUTPUT_NODES")  + " : " + config.configElement("OUTPUT.NODES") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("MAX_WEIGHT_PERTURB")  + " : " +  config.configElement("MAX.PERTURB") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("MAX_BIAS_PERTURB")  + " : " + config.configElement("MAX.BIAS.PERTURB") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("FEATURE_SELECTION")  + " : " + config.configElement("FEATURE.SELECTION") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("RECURRENCY_ALLOWED")  + " : " + config.configElement("RECURRENCY.ALLOWED") , ParagraphAlignment.LEFT);

        createDocParagraph(docxDoc, resourceBundle.getString("NEAT_SPECIFIC"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("EXCESS_COEFFICIENT")  + " : " + config.configElement("EXCESS.COEFFICIENT") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("DISJOINT_COEFFICIENT")  + " : " + config.configElement("DISJOINT.COEFFICIENT") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("WEIGHT_COEFFICIENT")  + " : " + config.configElement("WEIGHT.COEFFICIENT") , ParagraphAlignment.LEFT);
        
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIATION_CONTROL"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("THRESHOLD_COMPABILITY")  + " : " + config.configElement("COMPATABILITY.THRESHOLD") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("CHANGE_COMPABILITY")  + " : " + config.configElement("COMPATABILITY.CHANGE") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIE_COUNT")  + " : " + config.configElement("SPECIE.COUNT") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SURVIVAL_THRESHOLD")  + " : " + config.configElement("SURVIVAL.THRESHOLD") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIE_AGE_THRESHOLD")  + " : " + config.configElement("SPECIE.AGE.THRESHOLD") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIE_YOUTH_THRESHOLD")  + " : " + config.configElement("SPECIE.YOUTH.THRESHOLD") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIE_OLD_PENALTY")  + " : " + config.configElement("SPECIE.OLD.PENALTY") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("SPECIE_YOUTH_BOOST")  + " : " + config.configElement("SPECIE.YOUTH.BOOST") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("FITNESS_MAX")  + " : " + config.configElement("SPECIE.FITNESS.MAX") , ParagraphAlignment.LEFT);




        createDocParagraph(docxDoc, resourceBundle.getString("EXTINCTION_CONTROL"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc, resourceBundle.getString("EXTINCTION_EVENT")  + " : " + config.configElement("ELE.EVENTS") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("EXTINCTION_SURVIVAL_COUNT")  + " : " + config.configElement("ELE.SURVIVAL.COUNT") , ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc, resourceBundle.getString("EXTINCTION_EVENT_TIME")  + " : " + config.configElement("ELE.EVENT.TIME") , ParagraphAlignment.LEFT);




        



        
        
    }

   

    protected void createTotalTableWithErrors(XWPFDocument docxDoc){
        XWPFTable table = docxDoc.createTable(3, this.bestChromosomes.size()+1);
        table.getRow(0).getCell(0).setText("Вид ошибки");
        table.getRow(1).getCell(0).setText("Ошибка тренировки");
        table.getRow(2).getCell(0).setText("Ошибка тестирования");


        Chromosome best;
        for (int i = 0; i < this.bestChromosomes.size(); i++) {
            best = this.bestChromosomes.get(i).get(this.bestChromosomes.get(i).size()-1);
            table.getRow(0).getCell(i+1).setText("Тренировка № " + (i + 1));
            table.getRow(1).getCell(i+1).setText(String.valueOf(best.getTrainError()).replace(".", ","));
            table.getRow(2).getCell(i+1).setText(best.getValidationError() != null ? String.valueOf(best.getValidationError()).replace(".", ",") : "");
        }
    }


    protected void createVerticalTableWithErrors(XWPFDocument docxDoc, List<Chromosome> chromosomes){
        XWPFTable table = docxDoc.createTable(chromosomes.size()+1, 3);
        table.getRow(0).getCell(0).setText("Эпоха");
        table.getRow(0).getCell(1).setText("Ошибка тренировки");
        table.getRow(0).getCell(2).setText("Ошибка тестирования");

        for (int i = 0; i < chromosomes.size(); i++) {
            table.getRow(i+1).getCell(0).setText(String.valueOf(i + 1));
            table.getRow(i+1).getCell(1).setText(String.valueOf(chromosomes.get(i).getTrainError()).replace(".", ","));
            table.getRow(i+1).getCell(2).setText(chromosomes.get(i).getValidationError() != null ? String.valueOf(chromosomes.get(i).getValidationError()).replace(".", ",") : "");
        }
    }



    protected void createTableWithOutputs(XWPFDocument docxDoc){



        int col = 1 + (dataKeeper.getOutputs()*(this.bestChromosomes.size()+1));
        XWPFTable table = docxDoc.createTable(this.dataKeeper.getData().size()+2, col);

        for (int i = 0; i < dataKeeper.getLegend().size(); i++) {
            if(i == 0) {
                table.getRow(0).getCell(0).setText(dataKeeper.getLegendHeader());
            }
            table.getRow(i+2).getCell(0).setText(String.valueOf(dataKeeper.getLegend().get(i)).replace(".", ","));
        }

        List<List<Double>> data = dataKeeper.getData();

        int k = 1;
        List<Double> output;
        Chromosome best;
        List<List<Double>> columns = new ArrayList<>(col-1);

        for (int i = 0; i < dataKeeper.getOutputs(); i++) {

            for (int j = 0; j < this.bestChromosomes.size(); j++) {
                best = this.bestChromosomes.get(j).get(this.bestChromosomes.get(j).size()-1);
                output = new ArrayList<>(data.size());
                for (int l = 0; l < best.getOutputValues().size(); l++) {
                    output.add(best.getOutputValues().get(l).get(i));
                }
                columns.add(output);
            }

            output = new ArrayList<>(data.size());
            k = this.dataKeeper.getInputs()+i;
            for(int j = 0; j < data.size(); j++){
                output.add(data.get(j).get(k));
            }
            columns.add(output);

        }

        CTHMerge mergeCont;
        k = 0;
        for (int i = 0; i < table.getRow(0).getTableICells().size()-1; i++) {
            if(i % (this.bestChromosomes.size()+1) == 0){
                mergeCont = hMergeStart;
                table.getRow(0).getCell(i+1).setText(this.dataKeeper.getHeaders().get(this.dataKeeper.getInputs()+k));
                k++;
            } else {
                mergeCont = hMergeCont;
            }
            table.getRow(0).getCell(i+1).getCTTc().addNewTcPr();
            table.getRow(0).getCell(i+1).getCTTc().getTcPr().setHMerge(mergeCont);

        }
        table.getRow(0).getTableICells().size();



        k = 1;
        int bInd = 1;
        //int headerIndex = this.dataKeeper.getInputs();
        for (int i = 0; i < columns.size(); i++) {
            //table.getRow(0).getCell(i+1).setText(this.dataKeeper.getHeaders().get(headerIndex));
            //table.getRow(0).getCell(i+1).getCTTc().getTcPr().setHMerge(hMerge);
            for (int j = 0; j < columns.get(i).size(); j++) {
                table.getRow(j+2).getCell(i+1).setText(String.valueOf(columns.get(i).get(j)).replace(".", ","));
            }
            if(bInd % (this.bestChromosomes.size()+1) != 0){
                table.getRow(1).getCell(k++).setText("Тренировка № " + bInd++);
            }else {
                table.getRow(1).getCell(k++).setText("Ожидаемое");
                //hMerge = CTHMerge.Factory.newInstance();
                //hMerge.setVal(STMerge.RESTART);
                //headerIndex++;
            }
        }











    }

    public void addTopologyImage(BufferedImage snapshot) {
        this.images.add(snapshot);
    }
}
