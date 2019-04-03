package ru.filippov.GUI.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.neat4j.neat.data.normaliser.DataScaler;
import org.neat4j.neat.data.normaliser.LinearScaler;
import org.neat4j.neat.data.normaliser.NonLinearScaler;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;
import ru.filippov.GUI.windows.AlertWindow;
import ru.filippov.utils.Validator;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataPreparatorDialogueController {

    private static final Logger log = Logger.getLogger(DataPreparatorDialogueController.class);




    @FXML    private TabPane tabPane;
    @FXML    private Tab loadDataTab;
    @FXML    private TextField fileTextField;
    @FXML    private JFXComboBox<String> encodingChoiceBox;
    @FXML    private TextArea dataTextArea;
    @FXML    private JFXTextField decimalSeparatorTextField;
    @FXML    private JFXTextField dataSeparatorTextField;
    @FXML    private CheckBox containsHeadersCheckBox;

    @FXML    private Tab selectUsableDataTab;
    @FXML    private TableView<List<Double>> selectUsableDataTableView;

    @FXML    private Tab normaliseDataTab;
    @FXML    private Label chooseNormaliseMethodLabel;
    @FXML    private ChoiceBox<String> chooseNormaliseMethodChoiceBox;
    @FXML    private Label chooseActivationFunctionLabel;
    @FXML    private ChoiceBox<String> chooseActivationFunctionChoiceBox;
    @FXML    private JFXTextField minRangeTextField;
    @FXML    private JFXTextField maxRangeTextField;
    @FXML    private Button runNormaliseButton;
    @FXML    private Accordion normaliseDataAccordion;
    @FXML    private TableView<List<Double>> normalisedDataTableView;
    @FXML    private BarChart<Number, Number> normaliseStatisticBarChart;

    @FXML    private Tab selectTrainingDataTab;
    @FXML    private TableView<List<Double>> selectTrainingDataTableView;
    @FXML    private Label trainingSetPercentageLabel;
    @FXML    private TextField trainingSetPercentageTextField;
    @FXML    private CheckBox manualSelectionCheckBox;
    @FXML    private Label testingSetPercentageLabel;
    @FXML    private TextField testingSetPercentageTextField;
    @FXML    private Label trainingSetLegendLabel;
    @FXML    private Label testingSetLegendLabel;
    @FXML    private Label trainingAndTestingSetLegendLabel;
    @FXML    private Button confirmPercantageSetButton;



    @FXML    private Tab setDataSetNameTable;
    @FXML    private JFXTextField trainDataNameTextField;
    @FXML    private JFXTextField testDataNameTextField;

    @FXML    private Label dataSetsHeaderLabel;
    @FXML    private TitledPane normalizedTrainDataTitledPane;
    @FXML    private TableView<List<Double>> normalisedTrainDataTableView;
    @FXML    private TitledPane normalizedTestDataTitledPane;
    @FXML    private TableView<List<Double>> normalisedTestDataTableView;

    @FXML    private JFXButton previousButton;
    @FXML    private JFXButton nextButton;
    @FXML    private JFXButton cancelButton;

    private Stage stage;
    ResourceBundle resourceBundle;


    Pattern patternDelimetr = Pattern.compile("[;,. _]");

    int inputs = 0;
    int outputs = 0;
    List<String> lines;
    List<List<Double>> usedData;
    List<List<Double>> normalisedUsedData;
    Set<Integer> trainSetIndexes;
    Set<Integer> testSetIndexes;

    int trainSize = 0;
    int testSize = 0;

    String projectPath;
    String legend;


    private String stringFromFile;

    private String finish = "Завершить";
    private String next = "Далее";
    private String chooseFile = "Выберите файл";
    private String cantReadFile = "Не удалось открыть файл";
    private String thereWasntNumeric = "В файле нет чисел";
    private String unsupportedEncoding = "Неподдерживаемая кодировка!";
    private String checkDecimalSeparator;
    private String checkDatalSeparator;
    private String cantProcessData = "Не удалось обработать датасет";
    private String noInputs = "Выберите хотя бы одно входное значение";
    private String noOutputs = "Выберите хотя бы одно выходное значение";
    private String dataSetAlreadyExists = "Датасет с таким именем уже существует";
    private String success = "Success";
    private String dataWasCreated = "Data was successfully saved";
    private String useChoiceBox = "Use drop out list on main window, to select it";
    private String reloadData = "Upload your data again to apply encoding";
    private boolean legendIsSelected = false;

    public void init(){

        this.stage = ((Stage) this.tabPane.getScene().getWindow());

        this.encodingChoiceBox.getItems().addAll("UTF-8", "cp1251");
        switch (Locale.getDefault().getLanguage()){
            case "ru":
                this.encodingChoiceBox.getSelectionModel().select("cp1251");
                break;
            default:
                this.encodingChoiceBox.getSelectionModel().select("UTF-8");
                break;
        }

        this.encodingChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                if(newValue.length()!=0) {
                    if(fileTextField.getText().length()!=0) {
                        this.dataTextArea.setText(reloadData);
                        this.nextButton.setDisable(true);
                    }
                }
            }
        });

        this.dataTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(this.dataTextArea.getText().length() != 0) {
                checkLoadDataTab1ToGoNext();
                fillSeparators();
            }
        });

        this.stage.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()){
                case ENTER:
                    if(!this.encodingChoiceBox.isFocused()) {
                        if (!this.nextButton.isDisable())
                            nextStep();
                    } else {
                        loadData();
                    }
                    keyEvent.consume();
                    break;
                case ESCAPE:
                    cancel();
                    keyEvent.consume();
                    break;
            }

        });

        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int i = this.tabPane.getSelectionModel().getSelectedIndex();
            for (int j = i+1; j < this.tabPane.getTabs().size(); j++) {
                tabPane.getTabs().get(j).setDisable(true);
            }
            if(i == this.tabPane.getTabs().size()-1) this.nextButton.setText(this.finish);
            else this.nextButton.setText(next);

        });



        chooseNormaliseMethodChoiceBox.setItems(FXCollections.observableArrayList("Линейный", "Нелинейный", "Выбеливание входов"));
        chooseNormaliseMethodChoiceBox.getSelectionModel().selectFirst();
        chooseNormaliseMethodChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Нелинейный")){
                this.chooseActivationFunctionChoiceBox.setVisible(true);
                this.chooseActivationFunctionLabel.setVisible(true);
                this.chooseActivationFunctionChoiceBox.getSelectionModel().select(0);
            } else {
                this.chooseActivationFunctionChoiceBox.setVisible(false);
                this.chooseActivationFunctionLabel.setVisible(false);
            }
        });


        chooseActivationFunctionChoiceBox.setItems(FXCollections.observableArrayList(SigmoidFunction.getStaticFunctionName(), TanhFunction.getStaticFunctionName()));
        this.minRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            minRangeTextField.setText(newValue.replace(",","."));
            if (newValue.isEmpty() || !NumberUtils.isCreatable(newValue)) {
                runNormaliseButton.setDisable(true);
                nextButton.setDisable(true);
            } else {
                runNormaliseButton.setDisable(false);
            }
        });

        this.maxRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            maxRangeTextField.setText(newValue.replace(",","."));
            if (newValue.isEmpty() || !NumberUtils.isCreatable(newValue)) {
                runNormaliseButton.setDisable(true);
                nextButton.setDisable(true);
            } else {
                runNormaliseButton.setDisable(false);
            }
        });


        selectTrainingDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        selectTrainingDataTableView.setRowFactory( tableView2 -> {

            PseudoClass train = PseudoClass.getPseudoClass("train");
            PseudoClass test = PseudoClass.getPseudoClass("test");
            PseudoClass trainAndTest = PseudoClass.getPseudoClass("train-and-test");
            /*Percentage selection with button*/
            final TableRow<List<Double>> row = new TableRow<List<Double>>(){
                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);
                    if(i<0 || i>=usedData.size()) return;
                    int index = usedData.indexOf(this.getTableView().getItems().get(i));
                    this.pseudoClassStateChanged(train, trainSetIndexes.contains(index));
                    this.pseudoClassStateChanged(test, testSetIndexes.contains(index));
                    this.pseudoClassStateChanged(trainAndTest, trainSetIndexes.contains(index) && testSetIndexes.contains(index));
                    if(trainSetIndexes.isEmpty() && testSetIndexes.isEmpty()){
                        nextButton.setDisable(true);
                    } else {
                        nextButton.setDisable(false);
                    }
                }
            };

            /*Mouse selection*/
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (manualSelectionCheckBox.isSelected()) {
                        if (event.isPrimaryButtonDown()) {
                            int index = usedData.indexOf(row.getTableView().getItems().get(row.getIndex()));
                            if(trainSetIndexes.contains(index)){
                                trainSetIndexes.remove(index);
                            } else {
                                trainSetIndexes.add(index);
                            }
                            selectTrainingDataTableView.refresh();
                        } else if (event.isSecondaryButtonDown()) {
                            int index = usedData.indexOf(row.getTableView().getItems().get(row.getIndex()));
                            if(testSetIndexes.contains(index)){
                                testSetIndexes.remove(index);
                            } else {
                                testSetIndexes.add(index);
                            }
                            selectTrainingDataTableView.refresh();
                        }
                    }
                }
            });

            return row;
        });



        manualSelectionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue == true){
                trainingSetPercentageTextField.setText("");
                testingSetPercentageTextField.setText("");
                this.selectTrainingDataTableView.refresh();
                testingSetPercentageTextField.setDisable(true);
                trainingSetPercentageTextField.setDisable(true);
                confirmPercantageSetButton.setDisable(true);
                clearSelectionsOfTestingTableView();
                nextButton.setDisable(true);
            } else {
                this.selectTrainingDataTableView.refresh();
                testingSetPercentageTextField.setDisable(false);
                trainingSetPercentageTextField.setDisable(false);
                confirmPercantageSetButton.setDisable(false);
                clearSelectionsOfTestingTableView();
                nextButton.setDisable(true);
            }
        });

        trainingSetPercentageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                confirmPercantageSetButton.setDisable(true);
                nextButton.setDisable(true);
                return;
            }
            if(!NumberUtils.isCreatable(newValue)){
                trainingSetPercentageTextField.setText(oldValue);
            } else {
                newValue = newValue.replace(",",".");
                try {
                    String tempVal = newValue;
                    if (tempVal.charAt(tempVal.length()-1) == '.') tempVal+='0';
                    double value = Double.valueOf(tempVal);
                    if (value>100) throw new NumberFormatException();
                    String testingValue = String.format("%.2f", 100.0 - value);
                    //String testingValue = (value >= 1) ? String.format("%.0f", 100 - value) : String.format("%.4f", 1.0 - value);
                    testingSetPercentageTextField.setText(testingValue);
                    confirmPercantageSetButton.setDisable(false);
                } catch (NumberFormatException e){
                    trainingSetPercentageTextField.setText(oldValue);
                }
            }
        });

        this.testingSetPercentageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                confirmPercantageSetButton.setDisable(true);
                nextButton.setDisable(true);
                return;
            }
            newValue = newValue.replace(",",".");
            if(!NumberUtils.isCreatable(newValue)){
                testingSetPercentageTextField.setText(oldValue);
            } else {
                try {
                    double value = Double.valueOf(newValue);
                    if (value>100) throw new NumberFormatException();
                    if(trainingSetPercentageTextField.getText().length()!=0) {
                        confirmPercantageSetButton.setDisable(false);
                    } else {
                        confirmPercantageSetButton.setDisable(true);
                        nextButton.setDisable(true);
                    }
                } catch (NumberFormatException e){
                    testingSetPercentageTextField.setText(oldValue);
                }
            }
        });

        configureDataNameTextField(trainDataNameTextField);
        configureDataNameTextField(testDataNameTextField);
    }

    private void configureDataNameTextField(JFXTextField dataNameTextField) {
        dataNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(dataNameTextField.isVisible()) {
                if (newValue.isEmpty()) {
                    nextButton.setDisable(true);
                } else {
                    if (!Validator.allowedNameOfFile(newValue)) nextButton.setDisable(true);
                    else {
                        nextButton.setDisable(false);
                    }
                }
            }
        });
    }

    @FXML    private void cancel() {
        this.stage.close();
    }

    private List<List<Object>> getListDataFromTextArea() throws NumberFormatException{

        String textAreaString = this.dataTextArea.getText();

        String valueDelimetr = this.dataSeparatorTextField.getText() ;
        String decimalDelimetr = this.decimalSeparatorTextField.getText() ;
        if(!decimalDelimetr.equals(".")){
            if(!valueDelimetr.equals(".")){
                decimalDelimetr = ".";
            } else {
                decimalDelimetr = ".";
                valueDelimetr = ";";
            }
        }

        StringTokenizer stringTokenizer = new StringTokenizer(textAreaString, "\n");

        int tokens = stringTokenizer.countTokens();
        StringTokenizer elementTokenizer;
        List<List<Object>> valuesList;

        if(containsHeadersCheckBox.isSelected()) valuesList = new ArrayList<>(tokens);
        else valuesList = new ArrayList<>(tokens+1);
        List<Object> row;
        int countOfValues = 0;
        for (int i = 0; i < tokens; i++) {
            String tempStr = stringTokenizer.nextToken();
            if(i != 0 || !containsHeadersCheckBox.isSelected()){
                tempStr = tempStr.replaceAll(decimalSeparatorTextField.getText(), decimalDelimetr);
            }
            elementTokenizer = new StringTokenizer(tempStr, valueDelimetr);
            if(i == 0) {
                countOfValues = elementTokenizer.countTokens();
            }
            row = new ArrayList<>(countOfValues);
            if(i==0) {
                if (!containsHeadersCheckBox.isSelected()) {
                    for (int j = 0; j < countOfValues; j++) {
                        row.add(String.valueOf(j + 1));
                    }
                    valuesList.add(row);
                    row = new ArrayList<>(countOfValues);

                } else {
                    for (int j = 0; j < countOfValues; j++) {
                        row.add(elementTokenizer.nextToken());
                    }
                    valuesList.add(row);
                    continue;
                }
            }
            for (int j = 0; j < countOfValues; j++) {
                row.add(Double.parseDouble(elementTokenizer.nextToken()));
            }
            valuesList.add(row);
        }

        return valuesList;


    }




    @FXML    void nextStep() {
        int i = this.tabPane.getSelectionModel().getSelectedIndex();
        if( this.tabPane.getSelectionModel().getSelectedItem() == loadDataTab) {
            checkDecimalSeparator = "Не найден разделитель \"" + decimalSeparatorTextField.getText() + "\"!\nПожалуйста, перепроверрьте данные";
            if (!dataTextArea.getText().contains(decimalSeparatorTextField.getText())){
                checkDatalSeparator = "Не найден разделитель \"" + dataSeparatorTextField.getText() + "\"!\nПожалуйста, перепроверрьте данные";;
                AlertWindow.createAlertWindow(checkDecimalSeparator).show();
                return;
            }
            if (!dataTextArea.getText().contains(dataSeparatorTextField.getText())){
                AlertWindow.createAlertWindow(checkDatalSeparator).show();
                return;
            }
            try {
                firstStep(getListDataFromTextArea());
            } catch (NumberFormatException e) {
                AlertWindow.createAlertWindow(cantProcessData).show();
                return;
            }
        }
        if(this.tabPane.getSelectionModel().getSelectedItem() == selectUsableDataTab) {
            secondStep();
        }
        if(this.tabPane.getSelectionModel().getSelectedItem() == normaliseDataTab) {
            thirdStep();
        }
        if(this.tabPane.getSelectionModel().getSelectedItem() == selectTrainingDataTab){
            fourthStep();
        }
        if(this.tabPane.getSelectionModel().getSelectedItem() == setDataSetNameTable){
            finishStep();
        }

        if(i+1 < tabPane.getTabs().size()) {
            this.tabPane.getSelectionModel().select(i+1);
            this.tabPane.getTabs().get(i+1).setDisable(false);
            this.previousButton.setDisable(false);
        }
    }

    private void finishStep() {
        File dataFile;
        try {
            if(!trainSetIndexes.isEmpty()) {
                dataFile = new File(this.projectPath + this.trainDataNameTextField.getText() + ".trd");
                if(dataFile.exists()) {
                    AlertWindow.createAlertWindow("\"" + this.trainDataNameTextField.getText() + "\" train dataset - is already exists. \n Choose another name");
                    return;
                }
                this.writeDataIntoFile(dataFile, this.normalisedTrainDataTableView);
            }
            if(!testSetIndexes.isEmpty()) {
                dataFile = new File(this.projectPath + this.trainDataNameTextField.getText() + ".ted");
                if(dataFile.exists()) {
                    AlertWindow.createAlertWindow("\"" + this.trainDataNameTextField.getText() + "\" test dataset - is already exists. \n Choose another name");
                    return;
                }
                this.writeDataIntoFile(dataFile, this.normalisedTestDataTableView);
            }
        } catch (IOException e) {
            AlertWindow.createAlertWindow(e.getMessage()).show();
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(success);
        alert.setHeaderText(dataWasCreated);
        alert.setContentText(useChoiceBox);
        alert.show();
        this.stage.close();



    }

    private void fourthStep() {
        normalisedTrainDataTableView.getColumns().clear();
        normalisedTrainDataTableView.getItems().clear();
        normalisedTestDataTableView.getColumns().clear();
        normalisedTestDataTableView.getItems().clear();

        //ObservableList<TableColumn<List<Double>, ?>> columns = FXCollections.observableList(normalisedDataTableView.getColumns());
        TableColumn<List<Double>, ?> column;
        TableColumn<List<Double>, Double> tempColumn;
        for( int i = 0 ; i < normalisedDataTableView.getColumns().size(); i++){
            column = normalisedDataTableView.getColumns().get(i);
            int finalI = i;
            if(!trainSetIndexes.isEmpty()) {
                tempColumn = new TableColumn<List<Double>, Double>();
                tempColumn.setCellValueFactory(p -> {
                    return new SimpleObjectProperty<Double>((p.getValue().get(finalI)));
                });
                normalisedTrainDataTableView.getColumns().add(tempColumn);
                tempColumn.setText(column.getText());
            }
            if(!testSetIndexes.isEmpty()) {
                tempColumn = new TableColumn<List<Double>, Double>();
                tempColumn.setCellValueFactory(p -> {
                    return new SimpleObjectProperty<Double>((p.getValue().get(finalI)));
                });
                tempColumn.setText(column.getText());
                normalisedTestDataTableView.getColumns().add(tempColumn);
            }
        }
        if(!trainSetIndexes.isEmpty()) {
            normalizedTrainDataTitledPane.setVisible(true);
            normalizedTrainDataTitledPane.setVisible(true);
            ObservableList<List<Double>> normalisedTrainData = FXCollections.observableArrayList();
            for (int index : trainSetIndexes) {
                normalisedTrainData.add(normalisedUsedData.get(index));
            }
            normalisedTrainDataTableView.getItems().addAll(normalisedTrainData);
            nextButton.setDisable(true);
            normalizedTrainDataTitledPane.setExpanded(true);
        } else {
            trainDataNameTextField.setVisible(false);
            normalizedTrainDataTitledPane.setVisible(false);
        }

        if(!testSetIndexes.isEmpty()) {
            normalizedTestDataTitledPane.setVisible(true);
            testDataNameTextField.setVisible(true);
            ObservableList<List<Double>> normalisedTestData = FXCollections.observableArrayList();
            for (int index : testSetIndexes) {
                normalisedTestData.add(normalisedUsedData.get(index));
            }
            normalisedTestDataTableView.getItems().addAll(normalisedTestData);
            nextButton.setDisable(true);
            if(trainSetIndexes.isEmpty()){
                normalizedTestDataTitledPane.setExpanded(true);
            }
        } else {
            testDataNameTextField.setVisible(false);
            normalizedTestDataTitledPane.setVisible(false);
        }






    }

    private void thirdStep() {
        this.selectTrainingDataTableView.getColumns().clear();
        this.selectTrainingDataTableView.getItems().clear();
        this.testingSetPercentageTextField.setText("");
        this.trainingSetPercentageTextField.setText("");
        this.manualSelectionCheckBox.setSelected(false);
        ObservableList<TableColumn<List<Double>, ?>> columns = FXCollections.observableArrayList(selectUsableDataTableView.getColumns());/*new ObservableList<TableColumn<List<Double>, ?>>(selectUsableDataTableView.getItems().size());*/

        TableColumn<List<Double>, ?> column;
        ArrayList<TableColumn<List<Double>, ?>> outputTableColumns = new ArrayList<>(this.selectUsableDataTableView.getColumns().size());



        int counter = 0;
        for( int i = 0 ; i < selectUsableDataTableView.getColumns().size() ; i++){
            column = selectUsableDataTableView.getColumns().get(i);
            if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Unused")){
                //columns.add(tableColumn);
                columns.remove(column);
            }
            else if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Legend")){
                //columns.add(tableColumn);
                columns.remove(column);

            } else {
                TableColumn<List<Double>, Double> newColumn = new TableColumn<List<Double>, Double>(((Label)((BorderPane)column.getGraphic()).getTop()).getText());
                int finalJ = counter;
                newColumn.setSortable(false);
                newColumn.setPrefWidth(65);
                newColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
                {
                    return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
                });
                if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Output")){
                    outputTableColumns.add(newColumn);
                } else {
                    this.selectTrainingDataTableView.getColumns().add(newColumn);
                }
                counter++;
            }
        }
        this.selectTrainingDataTableView.getColumns().addAll(outputTableColumns);

        this.selectTrainingDataTableView.setItems(FXCollections.observableArrayList(usedData));

        this.testSetIndexes = new HashSet<>();
        this.trainSetIndexes = new HashSet<>();
        clearSelectionsOfTestingTableView();
        nextButton.setDisable(true);

    }

    private void clearSelectionsOfTestingTableView() {
        this.testSetIndexes.clear();
        this.trainSetIndexes.clear();
    }

    private void secondStep() {
        this.normalisedDataTableView.getColumns().clear();
        this.normalisedDataTableView.getItems().clear();
        this.normaliseDataAccordion.setVisible(false);

        this.usedData = new ArrayList<>(this.selectUsableDataTableView.getItems().size());
        ObservableList<TableColumn<List<Double>, ?>> columns = FXCollections.observableArrayList(selectUsableDataTableView.getColumns());/*new ObservableList<TableColumn<List<Double>, ?>>(selectUsableDataTableView.getItems().size());*/
        StringBuilder stringLegend = new StringBuilder("Legend:");
        TableColumn<List<Double>, ?> column;
        ArrayList<TableColumn<List<Double>, ?>> outputTableColumns = new ArrayList<>(this.selectUsableDataTableView.getColumns().size());
        int counter = 0;
        for( int i = 0 ; i < selectUsableDataTableView.getColumns().size() ; i++){
            column = selectUsableDataTableView.getColumns().get(i);
            if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Unused")){
                columns.remove(column);
            } else if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Legend")) {
                stringLegend.append(((Label)((BorderPane) column.getGraphic()).getTop()).getText()+";");
                for(List<Double> item : selectUsableDataTableView.getItems()){
                    stringLegend.append(column.getCellObservableValue(item).getValue()+";");
                }
                stringLegend.deleteCharAt(stringLegend.length()-1);

                columns.remove(column);

            } else{
                TableColumn<List<Double>, Double> newColumn = new TableColumn<List<Double>, Double>(((Label)((BorderPane)column.getGraphic()).getTop()).getText());
                int finalJ = counter;
                newColumn.setSortable(false);
                newColumn.setPrefWidth(65);
                newColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
                {
                    return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
                });
                if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Output")){
                    outputTableColumns.add(newColumn);
                } else {
                    this.normalisedDataTableView.getColumns().add(newColumn);
                }
                counter++;
            }
        }
        this.normalisedDataTableView.getColumns().addAll(outputTableColumns);

        List<Double> item;
        List<Double> data = null;
        for (int i = 0; i < columns.size(); i++) {
            column = columns.get(i);
            for (int j = 0 ; j < column.getTableView().getItems().size(); j++){
                item = column.getTableView().getItems().get(j);
                try{
                    usedData.get(j);
                } catch (IndexOutOfBoundsException ex){
                    usedData.add(new ArrayList<>(columns.size()));
                } catch (NullPointerException ex){
                    usedData.add(new ArrayList<>(columns.size()));
                }
                data = usedData.get(j);
                data.add((Double) column.getCellObservableValue(item).getValue());


            }
        }

        this.legend = stringLegend.toString();
        normaliseDataAccordion.setVisible(false);
        nextButton.setDisable(true);

    }

    private void firstStep(List<List<Object>> values) throws NumberFormatException{
        this.dataSeparatorTextField.setVisible(false);
        this.decimalSeparatorTextField.setVisible(false);
        this.containsHeadersCheckBox.setVisible(false);


        TableColumn tableColumn;

        this.selectUsableDataTableView.getColumns().clear();
        this.selectUsableDataTableView.getItems().clear();

        List<List<Double>> data = new ArrayList<>(values.size()-1);
        List<Double> row;

        for (int i = 0; i < values.size(); i++) {

            row = new ArrayList<Double>(values.get(i).size());
            for (int j = 0; j < values.get(i).size(); j++) {
                if(i == 0) {
                    tableColumn = new TableColumn();
                    tableColumn.setSortable(false);
                    ChoiceBox<String> choiceBox = this.createInputOutputChoiceBox();
                    if (j == values.get(0).size() - 1) choiceBox.getSelectionModel().select("Output");
                    Label headerLabel = new Label((String) values.get(0).get(j));
                    BorderPane borderPane = new BorderPane(choiceBox, headerLabel, null, null, null);
                    tableColumn.setGraphic(borderPane);
                    int finalJ = j;
                    tableColumn.setPrefWidth(65);
                    tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
                    {
                        return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
                    });
                    selectUsableDataTableView.getColumns().add(tableColumn);
                } else {
                    row.add((Double) values.get(i).get(j));
                }
            }
            if(i!=0) data.add(row);
        }
        ObservableList<List<Double>> observableList = FXCollections.observableArrayList();
        observableList.addAll(data);
        this.selectUsableDataTableView.setItems(observableList);
    }

    private ChoiceBox<String> createInputOutputChoiceBox() {
        ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Input", "Output", "Legend","Unused"));
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                switch (oldValue) {
                    case "Input":
                        this.inputs--;
                        break;
                    case "Output":
                        this.outputs--;
                        break;
                    case "Legend":
                        this.legendIsSelected = false;
                        break;
                    default:
                        break;
                }
            }
            switch (newValue){
                case "Input":
                    this.inputs++;
                    break;
                case "Output":
                    this.outputs++;
                    break;
                case "Legend":
                    if(this.legendIsSelected){
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setContentText("Only one column can be used as a legend");
                        alert.showAndWait();
                        choiceBox.getSelectionModel().select(oldValue);
                    } else {
                        this.legendIsSelected = true;
                    }
                    break;
                default:
                    break;
            }
            if (this.inputs == 0 || this.outputs == 0) {
                this.nextButton.setDisable(true);
            } else {
                this.nextButton.setDisable(false);
            }
        });
        choiceBox.getSelectionModel().select("Input");
        return choiceBox;
    }

    @FXML    void previousStep(ActionEvent event) {
        int i = this.tabPane.getSelectionModel().getSelectedIndex();
        if (this.tabPane.getSelectionModel().getSelectedItem() == loadDataTab){
            this.dataSeparatorTextField.setVisible(true);
            this.decimalSeparatorTextField.setVisible(true);
            this.containsHeadersCheckBox.setVisible(true);
        }
        if(i-1 >= 0) {
            tabPane.getSelectionModel().select(i-1);
            tabPane.getTabs().get(i).setDisable(true);
            if(i-1 <= 0) this.previousButton.setDisable(true);
            if(i==this.tabPane.getTabs().size()-1) this.nextButton.setText(this.next);
        }
    }

    public void changeLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;

    }

    public void refresh() {

        stringFromFile = "";

        this.previousButton.setDisable(true);
        this.tabPane.getSelectionModel().select(0);
        this.fileTextField.setText("");
        this.dataTextArea.setText("");
        this.nextButton.setDisable(true);
        this.trainDataNameTextField.setText("");

        this.trainDataNameTextField.setText("");
        this.testDataNameTextField.setText("");

    }


    public void loadData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()+"\\projects\\"));
        fileChooser.getExtensionFilters().addAll(//
                new FileChooser.ExtensionFilter("csv", "*.csv"),
                new FileChooser.ExtensionFilter("txt", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")); //
        fileChooser.setTitle(this.chooseFile);
        File dataFile = fileChooser.showOpenDialog(this.stage);

        if(dataFile != null){
            //lines = Files.lines(Paths.get(dataFile.getAbsolutePath()), StandardCharsets.UTF_8).collect(Collectors.toList());
            this.fileTextField.setText(dataFile.getAbsolutePath());
            this.decimalSeparatorTextField.setDisable(false);
            this.dataSeparatorTextField.setDisable(false);

            fillSeparators();
            try {
                stringFromFile = readData(dataFile);
                dataTextArea.setText(stringFromFile);
                encodingChoiceBox.setDisable(false);
                dataTextArea.requestFocus();
            } catch (IOException e) {
                this.dataTextArea.setText(e.getMessage());
                e.printStackTrace();
                // log error
            }
        }

    }

    private void fillSeparators() {
        this.decimalSeparatorTextField.setDisable(false);
        this.dataSeparatorTextField.setDisable(false);
        if(this.dataSeparatorTextField.getText().length() == 0
                && this.decimalSeparatorTextField.getText().length() == 0) {
            switch (Locale.getDefault().getLanguage()) {
                case "ru":
                    this.decimalSeparatorTextField.setText(",");
                    this.dataSeparatorTextField.setText(";");
                    break;
                default:
                    this.decimalSeparatorTextField.setText(".");
                    this.dataSeparatorTextField.setText(";");
                    break;
            }
        }

    }

    public String readData(File dataFile) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(dataFile), this.encodingChoiceBox.getSelectionModel().getSelectedItem()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        Pattern pattern = Pattern.compile("[^\\d ;,.{}()\\s\\n]?");

        boolean readingHeader = true;
        while ((line = reader.readLine()) != null) {
            if(readingHeader){
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()){
                    containsHeadersCheckBox.setSelected(matcher.group().length() != 0);
                }
                readingHeader = false;
            }
            stringBuilder.append(line+"\n");
        }
        reader.close();
        return stringBuilder.toString();

    }

    private void checkLoadDataTab1ToGoNext(){
        if(this.tabPane.getSelectionModel().getSelectedIndex() == 0) {
            nextButton.setDisable(true);
            if (dataTextArea.getText().length() == 0) return;
            if (dataTextArea.getText().contains(this.unsupportedEncoding)) return;
            if (decimalSeparatorTextField.getText().length() == 0) return;
            if (dataSeparatorTextField.getText().length() == 0) return;
            nextButton.setDisable(false);
        }
    }

    @FXML    void normaliseData(ActionEvent event) {
        normaliseDataAccordion.setVisible(true);
        DataScaler dataScaler;
        try {
            switch (chooseNormaliseMethodChoiceBox.getValue()) {
                case "Линейный":
                    dataScaler = new LinearScaler();
                    normalisedUsedData = dataScaler.normalize(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));

                    break;
                case "Нелинейный":
                    switch (chooseActivationFunctionChoiceBox.getValue()) {
                        case "sigmoid(x)":
                            dataScaler = new NonLinearScaler(new SigmoidFunction());
                            normalisedUsedData = dataScaler.normalize(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));
                            break;
                        case "tanh(x)":
                            dataScaler = new NonLinearScaler(new TanhFunction());

                            normalisedUsedData = dataScaler.normalize(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));
                            break;
                    }
                    break;
                case "Выбеливание входов":
                    break;
                default:
                    break;
            }


            if (normalisedUsedData != null) {
                normalisedDataTableView.setItems(FXCollections.observableArrayList(normalisedUsedData));
                normaliseStatisticBarChart.getData().clear();
                XYChart.Series dataSeries1 = new XYChart.Series();
                dataSeries1.setName("Частота распределения");
                XYChart.Data<String, Double> xychart;
                float temp;
                float minVal = Float.parseFloat(minRangeTextField.getText());
                float maxVal = Float.parseFloat(maxRangeTextField.getText());
                float step = (maxVal - minVal )/10;
                Map<String, XYChart.Data<String, Double>> intervals = new HashMap<>(10);
                for (float i = minVal; i < maxVal-step; i += step) {
                    String intervalID = String.valueOf(String.format("%.2f", i)) + "-" + String.valueOf(String.format("%.2f", i + step));
                    xychart = new XYChart.Data<>(intervalID, 0.0);
                    dataSeries1.getData().add(xychart);
                    intervals.put(intervalID, xychart);
                }
                normaliseStatisticBarChart.setBarGap(0);
                normaliseStatisticBarChart.getData().add(dataSeries1);


                temp = 0;
                for (List<Double> list : normalisedUsedData) {
                    for (Double val : list) {
                        for (float i = minVal; i < maxVal-step; i += step) {
                            if (val >= i && val < i + step) {
                                String intervalID = String.valueOf(String.format("%.2f", i)) + "-" + String.valueOf(String.format("%.2f", i + step));
                                xychart = intervals.get(intervalID);
                                xychart.setYValue((xychart.getYValue()) + 1.0);
                                temp++;
                                break;
                            }
                        }
                    }
                }
                for (Object chart : dataSeries1.getData()) {
                    xychart = ((XYChart.Data<String, Double>) chart);
                    xychart.setYValue(xychart.getYValue() / temp);
                }

                normaliseDataAccordion.getPanes().get(normaliseDataAccordion.getPanes().size() - 1).setExpanded(true);
                nextButton.setDisable(false);
            }


        } catch (NumberFormatException ex) {
            AlertWindow.createAlertWindow(ex.getMessage()).show();
        }
    }

    @FXML    void confirmPercentage(ActionEvent event) {
        clearSelectionsOfTestingTableView();
        String trainingSetPercentage = this.trainingSetPercentageTextField.getText().replaceAll(decimalSeparatorTextField.getText(),"." );
        String testSetPercentage = this.testingSetPercentageTextField.getText().replaceAll(decimalSeparatorTextField.getText(),"." );


        double percent = Double.parseDouble(trainingSetPercentage);
        trainSize = (int) (Math.round(usedData.size() * (percent/100)));
        percent = Double.parseDouble(testSetPercentage);
        testSize = (int) (Math.round(usedData.size() * (percent/100)));
        //if(trainSize >= usedData.size()) trainSize = usedData.size()-1;
        //put indexes
        for (int i = 0 ; i < trainSize; i++){
            trainSetIndexes.add(i);
        }



        for (int i = 0 ; i < testSize; i++){
            testSetIndexes.add(usedData.size()-1-i);
        }
        this.selectTrainingDataTableView.refresh();
    }

    public void setCurrentDatasetFolder(String datasetFolderName) {
        this.projectPath = datasetFolderName + "\\";
    }


    boolean writeDataIntoFile(File dest, TableView<List<Double>> tableView) throws IOException{

        BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
        writer.write(this.inputs+";"+this.outputs+"\n");
        writer.append(this.legend+"\n");
        for(int i = 0 ; i < tableView.getColumns().size(); i++) {
            writer.append(tableView.getColumns().get(i).getText());
            if(i != tableView.getColumns().size()-1) {
                writer.append(";");
            }
        }
        writer.append("\n");
        ObservableList<List<Double>> items = tableView.getItems();

        for(List<Double> row : items){
            for (int i = 0; i < row.size(); i++) {
                writer.append(String.valueOf(row.get(i)));
                if(i != row.size()-1) {
                    writer.append(";");
                }
            }
            writer.append("\n");
        }
        writer.flush();
        writer.close();


        return true;
    }


    public String getNameOfTrainingSet() {
        return this.trainDataNameTextField.getText();
    }
    public String getNameOfTestSet() {
        return this.trainDataNameTextField.getText();
    }
}
