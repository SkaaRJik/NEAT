package ru.filippov.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.nn.core.functions.ActivationFunctionFinder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import ru.filippov.prediction.WindowPrediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PredictionReporter{

    static final CTHMerge hMergeStart = CTHMerge.Factory.newInstance();
    static final CTHMerge hMergeCont = CTHMerge.Factory.newInstance();
    static final CTVMerge vMergeStart = CTVMerge.Factory.newInstance();
    static final CTVMerge vMergeCont = CTVMerge.Factory.newInstance();



    AIConfig config;
    DataKeeper dataKeeper;
    Integer windowSize;
    Integer yearSize;
    Integer inputs;
    ResourceBundle resourceBundle;
    private Double predictionError;
    XWPFDocument document;
    List<List<Double>> predictedGoalData;
    List<List<List<Double>>> predictedFactorData;
    WindowPrediction windowPrediction;

    static {
        hMergeStart.setVal(STMerge.RESTART);
        hMergeCont.setVal(STMerge.CONTINUE);
        vMergeStart.setVal(STMerge.RESTART);
        vMergeCont.setVal(STMerge.CONTINUE);
    }

    public PredictionReporter(){

    }

    public PredictionReporter(WindowPrediction windowPrediction, ResourceBundle resourceBundle) {
        document = new XWPFDocument();
        dataKeeper = windowPrediction.getDataKeeper();
        yearSize = windowPrediction.getYearPrediction();
        windowSize = windowPrediction.getWindowsSize();
        predictedGoalData = windowPrediction.getOutputData();
        this.inputs = dataKeeper.getInputs()+dataKeeper.getOutputs();
        predictionError = windowPrediction.getPredictionError();
        this.windowPrediction = windowPrediction;
        predictedFactorData = new ArrayList<>(dataKeeper.getInputs());
        this.resourceBundle = resourceBundle;

        //Double[][] predictedWindowDatas = windowPrediction.getPredictedWindowDatas();
        Double[][] predictedInputDatas = windowPrediction.getPredictedInputDatas();

        for (int i = 0; i < this.inputs; i++) {
            List<List<Double>> factData = new ArrayList<>(predictedInputDatas.length);

            for (int j = 0; j < predictedInputDatas.length; j++) {
                int finalJ = j;
                int finalI = i;
                factData.add(new ArrayList<Double>(1){{add(predictedInputDatas[finalJ][finalI]);}});
            }
            predictedFactorData.add(factData);

        }

    }

    public boolean report(File destination){

        if (destination != null) {

            FileOutputStream out = null;

            try {
                out = new FileOutputStream(destination);

                createDocParagraph(document, "Отчет по прогнозированию", ParagraphAlignment.CENTER);

                createDocParagraph(document, "Входов сети : " + this.dataKeeper.getInputs() + "; Выходов сети: " + this.dataKeeper.getOutputs(), ParagraphAlignment.LEFT);
                createDocParagraph(document, "Период прогнозирования : " + this.yearSize + "; Размер окна прогнозирования : " + windowSize, ParagraphAlignment.LEFT);

                createDocParagraph(document, "Пргнозируемый набор смотреть в приложении 1.", ParagraphAlignment.LEFT);


                List<List<Double>> data = dataKeeper.denormaliseData().getData();
                List<List<Double>> dataToWrite = new ArrayList<>(data.size());
                List<Double> row;
                for (int i = 0; i < data.size(); i++) {
                    dataToWrite.add(new ArrayList<>(dataKeeper.getOutputs()));

                }

                for (int i = 0; i < data.size(); i++) {
                    row = dataToWrite.get(i);
                    for (int j = dataKeeper.getInputs(); j < data.get(0).size(); j++) {
                        row.add(data.get(i).get(j));
                    }
                }
                long minutes = Math.round(windowPrediction.getTimeSpend() / 60);
                long seconds = Math.round(windowPrediction.getTimeSpend() - (minutes*60000));

                createDocParagraph(document, "Ошибка прогнозирования: " + predictionError.toString().replace(".",","), ParagraphAlignment.LEFT);
                createDocParagraph(document, String.format("Время затрачено: %d мин. %d сек.", minutes, seconds), ParagraphAlignment.LEFT);
                createDocParagraph(document, "Прогноз целевых показателей:", ParagraphAlignment.CENTER);

                List<Integer> indexes = new ArrayList<>(predictedGoalData.get(0).size());
                for (int i = dataKeeper.getInputs(); i < dataKeeper.getData().get(0).size(); i++) {
                    indexes.add(i);
                }


                List<List<Double>> denormalisedPredictedData = dataKeeper.denormaliseColumns(predictedGoalData, indexes);



                writePredictedDataIntoTable(document, denormalisedPredictedData, dataToWrite, dataKeeper.getInputs(), 0);



                for (int i = 0; i < this.inputs; i++) {
                    document.createParagraph().createRun().addBreak(BreakType.PAGE);
                    createDocParagraph(document, "Прогнозирование факторного показателя:", ParagraphAlignment.CENTER);
                    createDocParagraph(document, dataKeeper.getHeaders().get(i), ParagraphAlignment.CENTER);
                    createDocParagraph(document, String.format("Ошибка обучения: %f; Ошибка тестирования: %f", windowPrediction.getTrainer(i).getLastTrainError(), windowPrediction.getTrainer(i).getLastValidationError()), ParagraphAlignment.LEFT);
                    dataToWrite = new ArrayList<>(data.size());
                    for (int j = 0; j < data.size(); j++) {
                        dataToWrite.add(new ArrayList<>(1));

                    }
                    for (int j = 0; j < data.size(); j++) {
                        dataToWrite.get(j).add(data.get(j).get(i));
                    }
                    writePredictedDataIntoTable(document, this.predictedFactorData.get(i), dataToWrite, i, windowSize);

                    writeAIConfig(document, windowPrediction.getConfigForWindow()[i]);

                }


                document.createParagraph().createRun().addBreak(BreakType.PAGE);
                dataToWrite = dataKeeper.getData();
                createDocParagraph(document, "Приложение 1 - прогнозируемые данные", ParagraphAlignment.CENTER);
                createDocParagraph(document, "Если таблица не вмещается в документ, скопируйте данную таблицу в excel", ParagraphAlignment.LEFT);
                XWPFTable table = document.createTable(dataToWrite.size()+1, dataToWrite.get(0).size()+1);
                writeTrainDataIntoTable(table, dataToWrite);

                /*dataToWrite = dataKeeper.getTestData();
                if(dataToWrite != null) {
                    createDocParagraph(document, "Тестирующий набор", ParagraphAlignment.LEFT);

                    table = document.createTable(dataToWrite.size() + 1, dataToWrite.get(0).size() + 1);
                    writeTrainDataIntoTable(table, dataToWrite, legendIndex);
                }


                createDocParagraph(document, "Ошибки", ParagraphAlignment.LEFT);
                createTotalTableWithErrors(document);

                createDocParagraph(document, "Значения", ParagraphAlignment.LEFT);
                createTableWithOutputs(document);



                for (int i = 0; i < this.configs.size(); i++) {



                    document.createParagraph().createRun().addBreak(BreakType.PAGE);
                    createDocParagraph(document, "Параметры тренировки №" + (i+1), ParagraphAlignment.CENTER);
                    writeBriefing(document, configs.get(i), bestChromosomes.get(i), images.get(i));
                    createDocParagraph(document, "", ParagraphAlignment.LEFT);
                    writeAIConfig(document, configs.get(i));
                    createDocParagraph(document, "", ParagraphAlignment.LEFT);
                    createVerticalTableWithErrors(document, bestChromosomes.get(i));


                }*/


                document.write(out);
                out.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }  /*catch (InvalidFormatException e) {
                e.printStackTrace();
            }*/

            return true;
        }
        return false;
    }

    protected XWPFParagraph createDocParagraph(XWPFDocument docxModel, String text, ParagraphAlignment alignment){
        XWPFParagraph bodyParagraph = docxModel.createParagraph();
        XWPFRun paragraphConfig = bodyParagraph.createRun();
        paragraphConfig.setFontFamily("Times New Roman");
        //1559629738043
        paragraphConfig.setFontSize(14);
        bodyParagraph.setAlignment(alignment);
        // HEX цвет без решетки #
        paragraphConfig.setText( text  );
        return bodyParagraph;
    }


    private void writePredictedDataIntoTable(XWPFDocument docxDoc, List<List<Double>> predictedData, List<List<Double>> factData, int headerIndex, int offset){
        XWPFTable table = docxDoc.createTable( predictedData.size()+2+offset, predictedData.get(0).size()*2+1);

        table.getRow(0).getCell(0).setText(dataKeeper.getLegendHeader());

        double start = dataKeeper.getLegend().get(0);
        double tick = (dataKeeper.getLegend().get(dataKeeper.getLegend().size()-1) - dataKeeper.getLegend().get(0))/(dataKeeper.getLegend().size()-1);
        for (int i = 0; i < predictedData.size()+offset; i++) {
            table.getRow(i+2).getCell(0).setText(String.valueOf(start).replace(".",","));
            start+=tick;
        }

        int index = 2;

        int size = predictedData.size() > factData.size() ? predictedData.size() : factData.size();



        for (int i = 0; i < predictedData.get(0).size(); i++) {
            table.getRow(0).getCell(i+1).setText(dataKeeper.getHeaders().get(headerIndex+i));
            table.getRow(1).getCell(index-1).setText("Факт");
            table.getRow(1).getCell(index).setText("Нейросеть");
            for (int j = 0; j < size; j++) {
                if(j < predictedData.size())
                    table.getRow(j+2+offset).getCell(index).setText(String.valueOf(predictedData.get(j).get(i)).replace(".",","));
                if(j < factData.size())
                    table.getRow(j+2).getCell(index-1).setText(String.valueOf(factData.get(j).get(i)).replace(".",","));
            }
            index+=2;
        }



    }

    private void writeTrainDataIntoTable(XWPFTable table, List<List<Double>> dataToWrite) {
        XWPFTableRow tableRowOne;
        tableRowOne = table.getRow(0);
        List<String> headersForTableView = dataKeeper.getHeadersForTableView();
        for (int i = 0; i < headersForTableView.size(); i++) {
            String suffix = "\n ( Легенда )";
            if(i > 0) {
                suffix = (i) <= this.inputs ? "\n ( Вход )" : "\n( Выход )";
            }
            tableRowOne.getCell(i).setText(headersForTableView.get(i)+suffix);
        }

        for (int i = 0; i < dataToWrite.size(); i++) {
            tableRowOne = table.getRow(i+1);

            for (int j = 0; j < dataToWrite.get(i).size(); j++) {
                if(j==0) {
                    tableRowOne.getCell(0).setText(String.valueOf(dataKeeper.getLegend().get(i)).replace(".", ","));
                }
                tableRowOne.getCell(j+1).setText(String.valueOf(dataToWrite.get(i).get(j)).replace(".", ","));
            }
        }
    }




    protected void writeAIConfig(XWPFDocument docxDoc, AIConfig config){
        createDocParagraph(docxDoc, resourceBundle.getString("GA_SETTINGS"), ParagraphAlignment.CENTER);
        createDocParagraph(docxDoc,  resourceBundle.getString("GENERATOR_SEED") + " : " + config.configElement("GENERATOR.SEED"), ParagraphAlignment.LEFT);
        createDocParagraph(docxDoc,  resourceBundle.getString("MUTATION_PROBABILITY") + " : " + config.configElement("PROBABILITY.MUTATION"), ParagraphAlignment.LEFT);
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



    private void writePredictedOutputDataIntoTable(XWPFTable table, List<List<Double>> dataToWrite) {
        XWPFTableRow tableRowOne;
        tableRowOne = table.getRow(0);
        List<String> headersForTableView = dataKeeper.getHeadersForTableView();
        for (int i = 0; i < headersForTableView.size(); i++) {
            String suffix = "\n ( Легенда )";
            if(i > 0) {
                suffix = (i) <= this.inputs ? "\n ( Вход )" : "\n( Выход )";
            }
            tableRowOne.getCell(i).setText(headersForTableView.get(i)+suffix);
        }

        for (int i = 0; i < dataToWrite.size(); i++) {
            tableRowOne = table.getRow(i+1);

            for (int j = 0; j < dataToWrite.get(i).size(); j++) {
                if(j==0) {
                    tableRowOne.getCell(0).setText(String.valueOf(dataKeeper.getLegend().get(i)).replace(".", ","));
                }
                tableRowOne.getCell(j+1).setText(String.valueOf(dataToWrite.get(i).get(j)).replace(".", ","));
            }
        }
    }




}
