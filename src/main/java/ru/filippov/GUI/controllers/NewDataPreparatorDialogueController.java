package ru.filippov.GUI.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.data.normaliser.DataScaler;
import org.neat4j.neat.data.normaliser.LinearScaler;
import org.neat4j.neat.data.normaliser.NonLinearScaler;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;
import ru.filippov.GUI.windows.AlertWindow;
import ru.filippov.GUI.windows.SaveDialogue;
import ru.filippov.utils.Validator;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NewDataPreparatorDialogueController{

    private static final Logger log = Logger.getLogger(NewDataPreparatorDialogueController.class);

    ListProperty<List<String>> loadDataProperty;


    @FXML
    private TabPane tabPane;
    @FXML    private Tab loadDataTab;
    private TableView<List<Double>> dataFromExcelTableView = new TableView<>();
    @FXML    private BorderPane loadDataBorderPane;
    @FXML    private TextField fileTextField;
    @FXML    private JFXComboBox<String> encodingChoiceBox;
    @FXML    private CheckBox containsHeadersCheckBox;
    @FXML    private Label dataTextAreaError;

    @FXML    private Tab selectUsableDataTab;
    @FXML    private TableView<List<String>> selectUsableDataTableView;

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

    @FXML    private JFXButton previousButton;
    @FXML    private JFXButton nextButton;
    @FXML    private JFXButton cancelButton;

    Workbook workBook = null;
    private String file;


    enum Mode {
        XLSX,
        CSV
    }

    SimpleObjectProperty<Mode> currentLoadMode = new SimpleObjectProperty<>();

    private Stage stage;
    ResourceBundle resourceBundle;




    int inputs = 0;
    int outputs = 0;
    List<String> lines;
    List<List<Double>> usedData;
    DataKeeper normalisedUsedData;

    int trainSize = 0;
    int testSize = 0;

    String projectPath;

    private String save = "Сохранить";
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
    String legendLabel;
    List<Double> legend;



    ChangeListener<String> encoderListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if(newValue!=null){
                if(newValue.length()!=0) {
                    if(fileTextField.getText().length()!=0) {
                        if(fileTextField.getText().length() != 0) {
                            try {
                                fillTableViewWithData(readCSVData(new File(fileTextField.getText()),false));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };

    ChangeListener<Number> xlsxListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if(newValue!=null) {
                fillTableViewWithData(loadXLSXSheet(newValue, workBook, true));
            }
        }
    };


    public void init() {
        this.stage = ((Stage) this.tabPane.getScene().getWindow());

        initLoadDataTab();

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
            if(newValue == loadDataTab){
                containsHeadersCheckBox.setVisible(true);
            } else {
                containsHeadersCheckBox.setVisible(false);
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


        selectUsableDataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        selectUsableDataTableView.setRowFactory( tableView2 -> {

            final PseudoClass nullInRow = PseudoClass.getPseudoClass("null-row");
            /*Percentage selection with button*/
            final TableRow<List<String>> row = new TableRow<List<String>>(){
                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);
                    if(i<0 || i>=this.getTableView().getItems().size()) return;
                    String s1 = this.getTableView().getItems().get(i).stream().filter(s -> {
                        return s.matches("[\\w, ]");
                    }).findFirst().orElse(null);

                    boolean activate = s1==null ? false : true;

                    this.pseudoClassStateChanged(nullInRow, this.getTableView().getItems().get(i).contains("null") || activate);
                    if(this.getPseudoClassStates().contains(nullInRow)){
                        dataTextAreaError.setVisible(true);
                        dataTextAreaError.setText("Недопустимые значения!");
                        nextButton.setDisable(true);
                    }
                }
            };

            return row;
        });

        selectUsableDataTableView.setEditable(true);

        MenuItem deleteRow = new MenuItem("Удалить ряд");
        deleteRow.setOnAction(event -> {
            selectUsableDataTableView.getItems().remove(selectUsableDataTableView.getSelectionModel().getSelectedItem());
        });

        selectUsableDataTableView.setContextMenu(new ContextMenu(deleteRow));

        containsHeadersCheckBox.selectedProperty().addListener(observable -> {
            try {
                switch (currentLoadMode.getValue()) {
                    case CSV:
                        fillTableViewWithData(readCSVData(new File(this.fileTextField.getText()), false));
                        break;
                    case XLSX:
                        fillTableViewWithData(loadXLSXSheet(encodingChoiceBox.getSelectionModel().getSelectedIndex(), workBook, false));
                        break;
                }
            } catch (IOException ex){
                ex.printStackTrace();
                AlertWindow.createAlertWindow(ex.getMessage()).showAndWait();
            }
        });

    }

    private void initLoadDataTab() {

        this.currentLoadMode.addListener((observable, oldValue, newValue) -> {
            if(!newValue.equals(oldValue)){
                switch (newValue){
                    case CSV:
                        this.encodingChoiceBox.getSelectionModel().selectedIndexProperty().removeListener(this.xlsxListener);
                        this.encodingChoiceBox.getItems().clear();
                        this.encodingChoiceBox.getItems().addAll("UTF-8", "cp1251");
                        switch (Locale.getDefault().getLanguage()){
                            case "ru":
                                this.encodingChoiceBox.getSelectionModel().select("cp1251");
                                break;
                            default:
                                this.encodingChoiceBox.getSelectionModel().select("UTF-8");
                                break;
                        }
                        this.encodingChoiceBox.getSelectionModel().selectedItemProperty().addListener(this.encoderListener);
                        break;
                    case XLSX:
                        encodingChoiceBox.getSelectionModel().selectedItemProperty().removeListener(this.encoderListener);
                        encodingChoiceBox.getItems().clear();
                        for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
                            encodingChoiceBox.getItems().add(workBook.getSheetName(i));
                        }

                        encodingChoiceBox.getSelectionModel().selectedIndexProperty().addListener(this.xlsxListener);
                        break;
                }
            }
        });


        this.fileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!= null){
                if(!newValue.isEmpty()){
                    this.encodingChoiceBox.setVisible(true);
                }
                else {
                    this.encodingChoiceBox.setVisible(false);
                }
            } else {
                this.encodingChoiceBox.setVisible(false);
            }
        });

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






    @FXML    void nextStep() {
        int i = this.tabPane.getSelectionModel().getSelectedIndex();
        /*if( this.tabPane.getSelectionModel().getSelectedItem() == loadDataTab) {
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
                e.printStackTrace();
                AlertWindow.createAlertWindow(cantProcessData).show();
                return;
            }
        }*/
        boolean goNext = false;
        if(this.tabPane.getSelectionModel().getSelectedItem() == loadDataTab) {
            goNext = firstStep();
        }
        if(this.tabPane.getSelectionModel().getSelectedItem() == normaliseDataTab) {
            if(finishStep()){
                this.stage.close();
                return;
            }
        }


        if(i+1 < tabPane.getTabs().size() && goNext) {
            this.tabPane.getSelectionModel().select(i+1);
            this.tabPane.getTabs().get(i+1).setDisable(false);
            this.previousButton.setDisable(false);
        }
    }

    public String getFileName() {
        return this.file;
    }

    private boolean finishStep() {

        SaveDialogue instance = SaveDialogue.getInstance(this.stage.getScene());
        instance.setLabel("Имя датасета");
        instance.setFileDirectory(this.projectPath);
        instance.setExtension("trd");
        instance.show();
        try {
            this.writeDataIntoFile(instance.getNewFile());
            this.file = instance.getName();
        } catch (IOException ex){
            ex.printStackTrace();
            AlertWindow.createAlertWindow("Не удалось записать в файл:\n"+instance.getNewFile().getAbsolutePath());
            return false;
        }
        return true;
    }


    private boolean firstStep() {
        this.normalisedDataTableView.getColumns().clear();
        this.normalisedDataTableView.getItems().clear();

        this.usedData = new ArrayList<>(this.selectUsableDataTableView.getItems().size());
        List<String> headers = new ArrayList<>(this.selectUsableDataTableView.getColumns().size());

        ObservableList<List<String>> items = this.selectUsableDataTableView.getItems();
        this.legend = new ArrayList<>(items.size());
        List<Integer> outs = new ArrayList<>(this.selectUsableDataTableView.getColumns().size());
        boolean rowsAdded = false;
        int indexI = 0, indexJ = 0;
        try {
            for (int i = 0; i < this.selectUsableDataTableView.getColumns().size(); i++) {
                if(((ChoiceBox)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getCenter()).getValue().equals("Unused")) continue;
                if(((ChoiceBox)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getCenter()).getValue().equals("Legend")) {
                    legendLabel = (((TextField)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getTop()).getText());
                    for(int j = 0; j < items.size() ; j++){
                        legend.add(Double.valueOf(items.get(j).get(i)));
                    }
                    continue;
                }
                if(((ChoiceBox)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getCenter()).getValue().equals("Output")) {
                    outs.add(i);
                    continue;
                }
                indexJ = i;
                for (int j = 0; j < items.size(); j++) {
                    indexI = j;
                    if(!rowsAdded){
                        usedData.add(new ArrayList<>(items.size()));
                    }
                    usedData.get(j).add(Double.valueOf(items.get(j).get(i)));
                }
                headers.add(((TextField)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getTop()).getText());
                rowsAdded = true;
            }
            for(int i : outs){
                indexJ = i;
                for (int j = 0; j < items.size(); j++) {
                    indexI = j;
                    usedData.get(j).add(Double.valueOf(items.get(j).get(i)));
                }
                headers.add(((TextField)((BorderPane) selectUsableDataTableView.getColumns().get(i).getGraphic()).getTop()).getText());
            }
        } catch (NumberFormatException ex){
            log.error(ex.getMessage());
            if(((ChoiceBox)((BorderPane) selectUsableDataTableView.getColumns().get(indexI).getGraphic()).getCenter()).getValue().equals("Legend")) {
                AlertWindow.createAlertWindow("Столбец Legend должен содержать только цифры!").showAndWait();
            } else {
                AlertWindow.createAlertWindow("Произошла ошибка при обработке данных\n Строка : " + (indexI + 1) + " Столбец : " + (indexJ + 1)).showAndWait();
            }
            tabPane.getSelectionModel().select(loadDataTab);
            return false;
        }




        int counter = 0;

        for( int i = 0 ; i < usedData.get(0).size(); i++){
            TableColumn<List<Double>, Double> newColumn = new TableColumn<List<Double>, Double>(headers.get(i));
            int finalJ = i;
            newColumn.setSortable(false);
            newColumn.setPrefWidth(65);
            newColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
            {
                return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
            });
            this.normalisedDataTableView.getColumns().add(newColumn);
        }






        /*List<Double> item;
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
*/
        normaliseDataAccordion.setVisible(false);
        nextButton.setDisable(true);
        return true;
    }

    private void fillTableViewWithData(List<List<String>> values) throws NumberFormatException{



        TableColumn tableColumn;
        this.inputs = 0;
        this.outputs = 0;

        this.selectUsableDataTableView.getColumns().clear();
        this.selectUsableDataTableView.getItems().clear();

        this.legendIsSelected = false;
        for (int i = 0; i < values.get(0).size(); i++) {
            tableColumn = new TableColumn();
            tableColumn.setSortable(false);
            ChoiceBox<String> choiceBox = this.createInputOutputChoiceBox();
            if( i == 0 ) choiceBox.getSelectionModel().select("Legend");
            if (i == values.get(0).size() - 1) choiceBox.getSelectionModel().select("Output");
            TextField headerLabel = new TextField(containsHeadersCheckBox.isSelected() ? values.get(0).get(i) : String.valueOf(i));
            BorderPane borderPane = new BorderPane(choiceBox, headerLabel, null, null, null);
            tableColumn.setGraphic(borderPane);
            int finalJ = i;
            tableColumn.setPrefWidth(65);
            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
            {
                return new SimpleObjectProperty<Double>((p.getValue().get(finalJ)));
            });
            tableColumn.setCellFactory(TextFieldTableCell.<Double> forTableColumn());
            tableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<List<Double>, Double>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<List<Double>, Double> event) {
                    TablePosition tablePosition = event.getTablePosition();
                    int row = tablePosition.getRow();
                    event.getTableView().getItems().get(row).set(finalJ, event.getNewValue());
                }
            });
            selectUsableDataTableView.getColumns().add(tableColumn);
        }




        if(containsHeadersCheckBox.isSelected()){
            values.remove(0);
        }
        ObservableList<List<String>> observableList = FXCollections.observableArrayList();
        observableList.addAll(values);
        this.loadDataProperty = new SimpleListProperty<>(observableList);
        loadDataProperty.addListener((observable, oldValue, newValue) -> {
           boolean hasNull = false;

           for (List<String> strings : newValue){
               for (int j = 0 ; j < strings.size(); j++){
                   hasNull = StringUtils.containsIgnoreCase(strings.get(j), "null");

                   if(hasNull && !(((ChoiceBox)((BorderPane) selectUsableDataTableView.getColumns().get(j).getGraphic()).getCenter()).getValue().equals("Unused"))) break;
               }
               if(hasNull) break;
           }


            if(hasNull) {
                dataTextAreaError.setText("Недопустимые значения!");
                dataTextAreaError.setVisible(true);
                nextButton.setDisable(true);
            } else {
                dataTextAreaError.setVisible(false);
                nextButton.setDisable(false);
            }
        });



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
        if(i-1 >= 0) {
            tabPane.getSelectionModel().select(i-1);
            if(i==this.tabPane.getTabs().size()-1) this.nextButton.setText(this.next);
        }
    }

    public void changeLanguage(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;

    }

    public void clearNamesOfDatasets(){
        this.tabPane.getSelectionModel().select(loadDataTab);
    }

    public void refresh() {

        this.inputs = 0;
        this.outputs = 0;
        this.legendIsSelected = false;
        this.file = null;

        this.selectUsableDataTableView.getItems().clear();
        this.selectUsableDataTableView.getColumns().clear();

        this.previousButton.setDisable(true);
        this.tabPane.getSelectionModel().select(0);
        this.fileTextField.setText("");

        this.nextButton.setDisable(true);




    }


    public void loadData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()+"\\projects\\"));
        fileChooser.getExtensionFilters().addAll(//
                new FileChooser.ExtensionFilter("csv", "*.csv"),
                new FileChooser.ExtensionFilter("Excel", "*.xls*"),
                new FileChooser.ExtensionFilter("txt", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")); //
        fileChooser.setTitle(this.chooseFile);
        File dataFile = fileChooser.showOpenDialog(this.stage);

        if(dataFile != null){
            //lines = Files.lines(Paths.get(dataFile.getAbsolutePath()), StandardCharsets.UTF_8).collect(Collectors.toList());
            this.fileTextField.setText(dataFile.getAbsolutePath());

            try {
                if(dataFile.getName().split("[.]")[1].contains("xls")){
                    readXLSXfile(dataFile);
                }
                else {
                    this.currentLoadMode.set(Mode.CSV);
                    this.fillTableViewWithData(readCSVData(dataFile, true));
                    encodingChoiceBox.setDisable(false);

                }
            } catch (IOException e) {
                e.printStackTrace();
                // log error
            }
        }

    }



    public List<List<String>> readCSVData(File dataFile, boolean initHeaders) throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(dataFile), this.encodingChoiceBox.getSelectionModel().getSelectedItem()));

        String line;



        List<List<String>> result = new ArrayList<>();
        List<String> row = null;
        String[] split;
        boolean readingHeader = initHeaders;
        while ((line = reader.readLine()) != null) {
            row = new ArrayList<>();
            if(readingHeader){
                Matcher matcher = Pattern.compile("[\\D]*[;]").matcher(line);
                containsHeadersCheckBox.setSelected(matcher.find());
                readingHeader = false;
            }
            split = line.split("[;]");
            for (int i = 0; i < split.length; i++) {
                if(split[i].matches("[\\d]*[,]?[\\dEe]*")){
                        split[i] = split[i].replaceAll(",", ".");
                }
                row.add(split[i]);
            }
            if(!row.isEmpty())
                result.add(row);
        }
        reader.close();
        return result;
    }

    public void readXLSXfile(File dataFile){
        //инициализируем потоки
        String result = "";

        try {

            this.workBook = WorkbookFactory.create(dataFile);
            //разбираем первый лист входного файла на объектную модель

            currentLoadMode.set(Mode.XLSX);
            encodingChoiceBox.getSelectionModel().select(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<List<String>> loadXLSXSheet(Number newValue, Workbook workBook, boolean initHeaders) {



        Sheet sheet = workBook.getSheetAt((Integer) newValue);
        Iterator<Row> it = sheet.iterator();
        org.apache.poi.ss.usermodel.Cell cell;
        Row row;
        List<List<String>> result = new ArrayList<>(sheet.getLastRowNum());
        List<String> rowString = null;
        if(initHeaders) {
            this.containsHeadersCheckBox.setSelected(false);
        }
        for(int i = 0; it.hasNext(); i++ ){

            row = it.next();
            rowString = new ArrayList<>(row.getLastCellNum());
            Iterator<org.apache.poi.ss.usermodel.Cell> cells = row.iterator();
            while(cells.hasNext()) {
                cell = cells.next();
                //перебираем возможные типы ячеек
                switch (cell.getCellType()) {
                    case STRING:
                        if(i == 0){
                            if(initHeaders) {
                                this.containsHeadersCheckBox.setSelected(true);
                            }
                        }
                        rowString.add(cell.getStringCellValue().replaceAll("\n", " "));
                        break;
                    case NUMERIC:
                    case FORMULA:
                        String value = String.valueOf(cell.getNumericCellValue());
                        value = value.contains(",") ? value.replace(",", ".") : value;
                        rowString.add(value);
                        break;
                    case BLANK:
                        rowString.add("null");
                        break;
                    default:
                        rowString.add("null");

                        break;
                }
            }
            if(i>0){
                if(result.get(0).size() == rowString.size()){
                    result.add(rowString);
                }
            } else {
                result.add(rowString);
            }

        }
        return result;
    }






    @FXML    void normaliseData(ActionEvent event) {
        normaliseDataAccordion.setVisible(true);
        DataScaler dataScaler;
        try {
            switch (chooseNormaliseMethodChoiceBox.getValue()) {
                case "Линейный":
                    //dataScaler = new LinearScalerGlobalValues();
                    dataScaler = new LinearScaler();
                    normalisedUsedData = dataScaler.normalise(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));

                    //normalisedUsedData = dataScaler.normalise(usedData);

                    break;
                case "Нелинейный":
                    switch (chooseActivationFunctionChoiceBox.getValue()) {
                        case "sigmoid(x)":
                            dataScaler = new NonLinearScaler(new SigmoidFunction());
                            normalisedUsedData = dataScaler.normalise(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));
                            break;
                        case "tanh(x)":
                            dataScaler = new NonLinearScaler(new TanhFunction());

                            normalisedUsedData = dataScaler.normalise(usedData, Double.parseDouble(minRangeTextField.getText()), Double.parseDouble(maxRangeTextField.getText()));
                            break;
                    }
                    break;
                case "Выбеливание входов":
                    break;
                default:
                    break;
            }


            if (normalisedUsedData != null) {
                normalisedDataTableView.setItems(FXCollections.observableArrayList(normalisedUsedData.getData()));
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
                for (List<Double> list : normalisedUsedData.getData()) {
                    for (Double val : list) {
                        for (float i = minVal; i < maxVal-step; i += step) {
                            if (val != null) {
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

    public void setCurrentDatasetFolder(String datasetFolderName) {
        this.projectPath = datasetFolderName;
    }

    boolean writeDataIntoFile(File dest) throws IOException {

        this.normalisedUsedData.setInputs(this.inputs);
        this.normalisedUsedData.setOutputs(this.outputs);
        this.normalisedUsedData.setLegendHeader(this.legendLabel);
        this.normalisedUsedData.setLegend(this.legend);
        this.normalisedUsedData.setHeaders(normalisedDataTableView.getColumns().stream().map(listTableColumn -> {
            return listTableColumn.getText();
        }).collect(Collectors.toList()));
        DataKeeper denormalise = this.normalisedUsedData.denormalise();
        return this.normalisedUsedData.writeDataIntoFile(dest);

    }


}
