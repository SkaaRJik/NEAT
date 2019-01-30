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
import org.apache.log4j.Logger;
import org.neat4j.neat.data.normaliser.DataScaler;
import org.neat4j.neat.data.normaliser.LinearScaler;
import org.neat4j.neat.data.normaliser.NonLinearScaler;
import org.neat4j.neat.nn.core.functions.ArctgFunction;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;
import ru.filippov.GUI.windows.AlertWindow;
import ru.filippov.utils.Validator;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class DataPreparatorDialogueController {

    private static final Logger log = Logger.getLogger(DataPreparatorDialogueController.class);




    @FXML
    private TabPane tabPane;

    @FXML
    private Tab loadDataTab;

    @FXML
    private TextField fileTextField;

    @FXML
    private JFXComboBox<String> encodingChoiceBox;

    @FXML
    private TextArea dataTextArea;

    @FXML
    private JFXTextField decimalSeparatorTextField;

    @FXML
    private JFXTextField dataSeparatorTextField;

    @FXML
    private CheckBox containsHeadersCheckBox;






    @FXML
    private Tab selectUsableDataTab;

    @FXML private TableView<List<Double>> selectUsableDataTableView;



    @FXML
    private Tab normaliseDataTab;

    @FXML
    private Label chooseNormaliseMethodLabel;

    @FXML
    private ChoiceBox<String> chooseNormaliseMethodChoiceBox;

    @FXML
    private Label chooseActivationFunctionLabel;

    @FXML
    private ChoiceBox<String> chooseActivationFunctionChoiceBox;

    @FXML
    private Button runNormaliseButton;

    @FXML
    private Accordion normaliseDataAccordion;

    @FXML
    private TableView<List<Double>> normalisedDataTableView;

    @FXML
    private BarChart<Number, Number> normaliseStatisticBarChart;

    @FXML
    private Tab selectTrainingDataTab;

    @FXML
    private TableView<List<Double>> selectTrainingDataTableView;

    @FXML
    private Label trainingSetPercentageLabel;

    @FXML
    private TextField trainingSetPercentageTextField;

    @FXML
    private CheckBox manualSelectionCheckBox;

    @FXML
    private Label testingSetPercentageLabel;

    @FXML
    private TextField testingSetPercentageTextField;



    @FXML
    private Label trainingSetLegendLabel;

    @FXML
    private Label testingSetLegendLabel;

    @FXML
    private Label trainingAndTestingSetLegendLabel;

    @FXML
    private Button confirmPercantageSetButton;



    @FXML
    private Tab setDataSetNameTable;

    @FXML
    private JFXTextField dataSetNameTextField;

    @FXML
    private Label dataSetsHeaderLabel;

    @FXML
    private TitledPane normalizedTrainDataTitledPane;

    @FXML
    private TableView<List<Double>> normalisedTrainDataTableView;

    @FXML
    private TitledPane normalizedTestDataTitledPane;

    @FXML
    private TableView<List<Double>> normalisedTestDataTableView;

    @FXML
    private JFXButton previousButton;

    @FXML
    private JFXButton nextButton;

    @FXML
    private JFXButton cancelButton;

    private Stage stage;
    ResourceBundle resourceBundle;

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

        this.encodingChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
            if(this.dataTextArea.getText().length() != 0 && this.fileTextField.getText().length() != 0) {
                this.dataTextArea.setText("");
                try {
                    this.lines = loadData(new File(this.fileTextField.getText()));
                    this.lines.stream().forEach(s -> this.dataTextArea.appendText(s+"\n"));
                    fillSeparators();
                } catch (UnsupportedEncodingException e){
                    this.dataTextArea.setText(this.unsupportedEncoding);
                    e.printStackTrace();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            checkLoadDataTab1ToGoNext();
        });

        this.stage.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()){
                case ENTER:
                    if(!this.nextButton.isDisable())
                        nextStep();
                    //Stop letting it do anything else
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

        this.dataTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(this.dataTextArea.getText().length() != 0) {
                fillSeparators();
                checkLoadDataTab1ToGoNext();
            }
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

        chooseActivationFunctionChoiceBox.setItems(FXCollections.observableArrayList("sigmoid(x)", "th(x)", "arctg(x)"));
        //selectTrainingDataTableView.setPlaceholder(new BorderPane(new CheckBox("Ручная выборка")));


        selectTrainingDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /*trainingSetLegendLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("train"), true);
        testingSetLegendLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("test"), true);
        trainingAndTestingSetLegendLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("train-and-test"), true);*/



        selectTrainingDataTableView.setRowFactory( tableView2 -> {

            PseudoClass train = PseudoClass.getPseudoClass("train");
            PseudoClass test = PseudoClass.getPseudoClass("test");
            PseudoClass trainAndTest = PseudoClass.getPseudoClass("train-and-test");
            /*Percentage selection with button*/
            final TableRow<List<Double>> row = new TableRow<List<Double>>(){

                @Override
                public void updateItem(List<Double> item, boolean empty){

                    if (item != null) {
                        int index = usedData.indexOf(item);
                        this.pseudoClassStateChanged(train, trainSetIndexes.contains(index));
                        this.pseudoClassStateChanged(test, testSetIndexes.contains(index));
                        this.pseudoClassStateChanged(trainAndTest, trainSetIndexes.contains(index) && testSetIndexes.contains(index));
                    } else {
                        this.pseudoClassStateChanged(train, false);
                        this.pseudoClassStateChanged(test, false);
                        this.pseudoClassStateChanged(trainAndTest, false);
                    }
                    if(trainSetIndexes.isEmpty() || testSetIndexes.isEmpty()){
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
            if(!Validator.isOnlyNumbers(newValue)){
                trainingSetPercentageTextField.setText(oldValue);
            } else {
                newValue = newValue.replace(",",".");
                try {
                    String tempVal = newValue;
                    if (tempVal.charAt(tempVal.length()-1) == '.') tempVal+='0';
                    double value = Double.valueOf(tempVal);
                    if (value>100) throw new NumberFormatException();

                    String testingValue = (value >= 1) ? String.format("%.0f", 100 - value) : String.format("%.4f", 1.0 - value);
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
            if(!Validator.isOnlyNumbers(newValue)){
                testingSetPercentageTextField.setText(oldValue);
            } else {
                newValue = newValue.replace(",",".");
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

        dataSetNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                nextButton.setDisable(true);
            } else{
                if(!Validator.allowedNameOfFile(newValue)) nextButton.setDisable(true);
                else{
                    nextButton.setDisable(false);
                }
            }
        });
    }
    @FXML
    private void cancel() {
        this.stage.close();
    }

    @FXML
    void nextStep() {
        int i = this.tabPane.getSelectionModel().getSelectedIndex();
        if( this.tabPane.getSelectionModel().getSelectedItem() == loadDataTab) {
            //TODO Check List lines, if user just copy-paste his data - List is empty
            checkDecimalSeparator = "Не найден разделитель \"" + decimalSeparatorTextField.getText() + "\"!\nПожалуйста, перепроверрьте данные";
            checkDatalSeparator = "Не найден разделитель \"" + dataSeparatorTextField.getText() + "\"!\nПожалуйста, перепроверрьте данные";;
            if (!dataTextArea.getText().contains(decimalSeparatorTextField.getText())){
                AlertWindow.getAlert(checkDecimalSeparator).show();
                return;
            }
            if (!dataTextArea.getText().contains(dataSeparatorTextField.getText())){
                AlertWindow.getAlert(checkDatalSeparator).show();
                return;
            }
            try {
                firstStep();
            } catch (NumberFormatException e) {
                AlertWindow.getAlert(cantProcessData).show();
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
        File file = new File(this.projectPath+this.dataSetNameTextField.getText());
        if(file.exists()){
            AlertWindow.getAlert(dataSetAlreadyExists).show();
        } else {
            file.mkdir();
            File dataFile = new File(file.getAbsolutePath()+"\\"+this.dataSetNameTextField.getText()+"@train.dataset");
            try {
                this.writeDataIntoFile(dataFile, this.normalisedTrainDataTableView);
                dataFile = new File(file.getAbsolutePath()+"\\"+this.dataSetNameTextField.getText()+"@test.dataset");
                this.writeDataIntoFile(dataFile, this.normalisedTestDataTableView);
            } catch (IOException e) {
                AlertWindow.getAlert(e.getMessage()).show();
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(success);
            alert.setHeaderText(dataWasCreated);
            alert.setContentText(useChoiceBox);
            alert.show();
        }


    }

    private void fourthStep() {
        normalisedTrainDataTableView.getColumns().clear();
        normalisedTrainDataTableView.getItems().clear();
        normalisedTestDataTableView.getColumns().clear();
        normalisedTestDataTableView.getItems().clear();

        ObservableList<TableColumn<List<Double>, ?>> columns = FXCollections.observableList(normalisedDataTableView.getColumns());


        normalisedTrainDataTableView.getColumns().addAll(columns);
        columns = FXCollections.observableList(normalisedDataTableView.getColumns());
        normalisedTestDataTableView.getColumns().addAll(columns);
        ObservableList<List<Double>> normalisedTrainData = FXCollections.observableArrayList();
        ObservableList<List<Double>> normalisedTestData = FXCollections.observableArrayList();
        for(int index : trainSetIndexes){
            normalisedTrainData.add(normalisedUsedData.get(index));
        }
        for(int index : testSetIndexes) {
            normalisedTestData.add(normalisedUsedData.get(index));
        }

        normalisedTrainDataTableView.getItems().addAll(normalisedTrainData);
        normalisedTestDataTableView.getItems().addAll(normalisedTestData);
        normalizedTrainDataTitledPane.setExpanded(true);
        nextButton.setDisable(true);

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

        List<Double> row;
        List<Double> dataForTable = new ArrayList<>(usedData.size());

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

        TableColumn<List<Double>, ?> column;
        ArrayList<TableColumn<List<Double>, ?>> outputTableColumns = new ArrayList<>(this.selectUsableDataTableView.getColumns().size());
        int counter = 0;
        for( int i = 0 ; i < selectUsableDataTableView.getColumns().size() ; i++){
            column = selectUsableDataTableView.getColumns().get(i);
            if(((ChoiceBox)((BorderPane) column.getGraphic()).getCenter()).getValue().equals("Unused")){
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
        normaliseDataAccordion.setVisible(false);
        nextButton.setDisable(true);

    }

    private void firstStep() throws NumberFormatException{
        this.dataSeparatorTextField.setVisible(false);
        this.decimalSeparatorTextField.setVisible(false);
        this.containsHeadersCheckBox.setVisible(false);

        StringTokenizer stringTokenizer = new StringTokenizer(lines.get(0), dataSeparatorTextField.getText());
        TableColumn tableColumn;
        int tokens = stringTokenizer.countTokens();
        this.selectUsableDataTableView.getColumns().clear();
        this.selectUsableDataTableView.getItems().clear();

        for (int j = 0; j < tokens; j++) {
            tableColumn = new TableColumn();
            tableColumn.setSortable(false);
            ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Input", "Output", "Unused"));
            choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    switch (oldValue) {
                        case "Input":
                            this.inputs--;
                            break;
                        case "Output":
                            this.outputs--;
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
                    default:
                        break;
                }
                if (this.inputs == 0 || this.outputs == 0) {
                    this.nextButton.setDisable(true);
                } else {
                    this.nextButton.setDisable(false);
                }
            });
            if (j == tokens-1) choiceBox.getSelectionModel().select("Output");
            else choiceBox.getSelectionModel().select("Input");
            Label headerLabel = null;

            if(containsHeadersCheckBox.isSelected()){
                headerLabel = new Label(stringTokenizer.nextToken());
            } else {
                headerLabel = new Label(String.valueOf(j+1));
            }
            BorderPane borderPane = new BorderPane(choiceBox, headerLabel, null, null, null);
            tableColumn.setGraphic(borderPane);
            int finalJ = j;
            tableColumn.setPrefWidth(65);
            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
            {
                return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
            });
            selectUsableDataTableView.getColumns().add(tableColumn);
        }
        int startIndex = 0;
        if (containsHeadersCheckBox.isSelected()) startIndex = 1;
        List<List<Double>> data = new ArrayList<>(lines.size());
        List<Double> row;
        for (int j = startIndex; j < lines.size(); j++) {
            if (!decimalSeparatorTextField.getText().contains(".")) lines.set(j, lines.get(j).replaceAll(decimalSeparatorTextField.getText(), "."));
            stringTokenizer = new StringTokenizer(lines.get(j), dataSeparatorTextField.getText());
            tokens = stringTokenizer.countTokens();
            row = new ArrayList<>(tokens);
            for (int k = 0; k < tokens; k++) {
                row.add(Double.valueOf(stringTokenizer.nextToken()));
            }
            data.add(row);
        }
        ObservableList<List<Double>> observableList = FXCollections.observableArrayList();
        observableList.addAll(data);
        this.selectUsableDataTableView.setItems(observableList);
    }

    @FXML
    void previousStep(ActionEvent event) {
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
        this.nextButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.tabPane.getSelectionModel().select(0);
        this.fileTextField.setText("");
        this.dataTextArea.setText("");
        this.nextButton.setDisable(true);
        this.dataSetNameTextField.setText("");
    }


    public void loadData(ActionEvent actionEvent) {
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
                    this.lines = loadData(dataFile);
                    this.dataTextArea.setText("");
                this.lines.stream().forEach(s -> this.dataTextArea.appendText(s+"\n"));


                boolean hasNumbers = false;
                char currentChar;
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.get(i).length(); j++) {
                        currentChar = lines.get(i).charAt(j);
                        if (Character.isDigit(currentChar)) {
                            hasNumbers = true;
                            break;
                        }
                        if (i == 0) containsHeadersCheckBox.setSelected(true);
                        if(j == 3) break;
                    }
                    if (hasNumbers) break;
                    if (i == 3) {
                        AlertWindow.getAlert(this.thereWasntNumeric).show();
                        this.nextButton.setDisable(true);
                        return;
                    }
                }







            } catch (UnsupportedEncodingException ex){
                this.dataTextArea.setText(unsupportedEncoding);
            }
            catch (IOException e) {
                this.dataTextArea.setText(e.getMessage());
                e.printStackTrace();
                // log error
            }
        }

    }

    private void fillSeparators() {
        this.decimalSeparatorTextField.setDisable(false);
        this.dataSeparatorTextField.setDisable(false);
        switch (Locale.getDefault().getLanguage()){
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

    public List<String> loadData(File dataFile) throws IOException {
        List<String> lines = new ArrayList<>(30);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(dataFile), this.encodingChoiceBox.getSelectionModel().getSelectedItem()));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private void checkLoadDataTab1ToGoNext(){
        if(this.tabPane.getSelectionModel().getSelectedIndex() == 0) {
            nextButton.setDisable(true);
            if (dataTextArea.getText().length() == 0) return;
            if (dataTextArea.getText().equals(this.unsupportedEncoding)) return;
            if (decimalSeparatorTextField.getText().length() == 0) return;
            if (dataSeparatorTextField.getText().length() == 0) return;
            nextButton.setDisable(false);
        }
    }

    @FXML
    void normaliseData(ActionEvent event) {
        normaliseDataAccordion.setVisible(true);
        DataScaler dataScaler;
        switch (chooseNormaliseMethodChoiceBox.getValue()){
            case "Линейный":
                dataScaler = new LinearScaler();
                normalisedUsedData = dataScaler.normalize(usedData);

                break;
            case "Нелинейный":
                switch (chooseActivationFunctionChoiceBox.getValue()) {
                    case "sigmoid(x)":
                        dataScaler = new NonLinearScaler(new SigmoidFunction());
                        normalisedUsedData = dataScaler.normalize(usedData);
                        break;
                    case "th(x)":
                        dataScaler = new NonLinearScaler(new TanhFunction());
                        normalisedUsedData = dataScaler.normalize(usedData);
                        break;
                    case "arctg(x)":
                        dataScaler = new NonLinearScaler(new ArctgFunction());
                        normalisedUsedData = dataScaler.normalize(usedData);
                        break;
                }
                break;
            case "Выбеливание входов":
                break;
            default:
                break;
        }
        if(normalisedUsedData != null) {
            normalisedDataTableView.setItems(FXCollections.observableArrayList(normalisedUsedData));
            normaliseStatisticBarChart.getData().clear();
            XYChart.Series dataSeries1 = new XYChart.Series();
            dataSeries1.setName("Частота распределения");
            float temp;
            for (float i = 0.0f; i < 1.0f; i+=0.1f) {

                if(i == 0.0f){
                    temp = i;
                } else {
                    temp = i+0.01f;
                }
                dataSeries1.getData().add(new XYChart.Data<String, Float>(String.valueOf(String.format("%.2f",temp))+"-"+String.valueOf(String.format("%.2f",i+0.1f)), 0f));
            }
            XYChart.Data<String, Float> xychart;
            temp = 0;
            for (List<Double> list : normalisedUsedData){
                for (Double val : list) {
                    for (float i = 0.0f; i < 1.0f; i+=0.1f) {
                        if(val >= i && val < i+0.1f){
                            temp++;
                            xychart = (XYChart.Data<String, Float>) dataSeries1.getData().get((int) (i*10));
                            xychart.setYValue(xychart.getYValue()+1);
                            break;
                        }
                    }
                }
            }
            for (Object chart : dataSeries1.getData()){
                xychart = ((XYChart.Data<String, Float>) chart);
                xychart.setYValue(xychart.getYValue()/temp);
            }
            normaliseStatisticBarChart.setBarGap(0);
            normaliseStatisticBarChart.getData().add(dataSeries1);
            normaliseDataAccordion.getPanes().get(normaliseDataAccordion.getPanes().size()-1).setExpanded(true);
            nextButton.setDisable(false);
        }
    }

    @FXML
    void confirmPercentage(ActionEvent event) {
        clearSelectionsOfTestingTableView();

        double percent = Double.parseDouble(this.trainingSetPercentageTextField.getText());
        percent = percent > 1 ? percent / 100 : percent;
        trainSize = (int) (Math.floor(usedData.size() * percent));
        percent = Double.parseDouble(this.testingSetPercentageTextField .getText());
        percent = percent > 1 ? percent / 100 : percent;
        testSize = (int) (Math.floor(usedData.size() * percent));

        //put indexes
        for (int i = 0 ; i < trainSize; i++){
            trainSetIndexes.add(i);
        }

        for (int i = usedData.size()-1 ; i >= usedData.size()-1-testSize; i--){
            testSetIndexes.add(i);
        }
        this.selectTrainingDataTableView.refresh();
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath + "\\datasets\\";
    }


    boolean writeDataIntoFile(File dest, TableView<List<Double>> tableView) throws IOException{

        BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
        writer.write(this.inputs+";"+this.outputs+"\n");
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


    public String getNameOfDataSet() {
        return this.dataSetNameTextField.getText();
    }
}
