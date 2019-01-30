package ru.filippov.GUI.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.skins.JFXTextFieldSkin;
import com.jfoenix.skins.ValidationPane;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.neat4j.core.AIConfig;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.core.NEATLoader;
import ru.filippov.GUI.windows.DataPreparatorDialogue;
import ru.filippov.utils.CsControl;
import ru.filippov.GUI.windows.AlertWindow;
import ru.filippov.GUI.windows.NewProjectDialogue;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class MainController {



    @FXML private Menu file;
    @FXML private MenuItem newProject;
    @FXML private MenuItem openProject;
    @FXML private MenuItem saveProject;
    @FXML private MenuItem exit;
    @FXML private Menu settings;
    @FXML private MenuItem languages;
    @FXML private Menu help;
    @FXML private MenuItem about;
    @FXML private Tooltip newProjectTooltip;
    @FXML private Tooltip openProjectTooltip;
    @FXML private Label currentProjectLabel;
    @FXML private TextField currentProjectTextField;

    @FXML private Label neatOptionsLabel;
    private String neatOptionNoProjects;
    private String neatOptionGASettings;


    @FXML private TitledPane GASettingsTitledPane;
    @FXML private JFXTextField mutationProbabilityTextField;
    @FXML private JFXTextField crossoverProbabilityTextField;
    @FXML private JFXTextField addLinkProbabilityTextField;
    @FXML private JFXTextField addNodeProbabilityTextField;
    @FXML private JFXTextField mutateBiasProbabilityTextField;
    @FXML private JFXTextField toggleLinkProbabilityTextField;
    @FXML private JFXTextField weightReplaceProbabilityTextField;
    @FXML private TitledPane neatSpecificTitledPane;
    @FXML private JFXTextField generatorSeedTextField;
    @FXML private JFXButton generateSeedButton;
    @FXML private JFXTextField excessCoefficientTextField;
    @FXML private JFXTextField disjointCoefficientTextField;
    @FXML private JFXTextField weightCoefficientTextField;
    @FXML private TitledPane speciationControlTitledPane;
    @FXML private JFXTextField thresholdCompabilityTextField;
    @FXML private JFXTextField changeCompabilityTextField;
    @FXML private JFXTextField specieCountTextField;
    @FXML private JFXTextField survivalThresholdTextField;
    @FXML private JFXTextField specieAgeThresholdTextField;
    @FXML private JFXTextField specieYouthThresholdTextField;
    @FXML private JFXTextField specieOldPenaltyTextField;
    @FXML private JFXTextField specieYouthBoostTextField;
    @FXML private JFXTextField specieFitnessMaxTextField;
    @FXML private TitledPane networkControlTitledPane;
    @FXML private JFXTextField inputNodesTextField;
    @FXML private JFXTextField outputNodesTextField;
    @FXML private JFXTextField maxPertrubTextField;
    @FXML private JFXTextField maxBiasPertrubTextField;
    @FXML private JFXToggleButton featureSelectionToogle;
    @FXML private JFXToggleButton reccurencyAllowedToogle;
    @FXML private TitledPane extinctionControlTitledPane;
    @FXML private JFXToggleButton eleEventsToogle;
    @FXML private JFXTextField eleSurvivalCountTextField;
    @FXML private JFXTextField eleEventTimeTextField;
    @FXML private TitledPane epochControlTitledPane;
    @FXML private JFXToggleButton keepBestEverToogle;
    @FXML private JFXTextField extraFeatureCountTextField;
    @FXML private JFXTextField popSizeTextField;
    @FXML private JFXTextField numberEpochsTextField;

    @FXML private ScrollPane parametresPane;
    @FXML private TabPane infoTabPane;

    @FXML private Tab datasetsTab;
    @FXML private ChoiceBox<String> datasetChoiceBox;
    @FXML
    private ScrollPane dataSetsScrollPane;
    @FXML
    private TitledPane trainTitledPane;
    @FXML
    private TableView<List<Double>> trainTableView;
    @FXML
    private TitledPane testTitledPane;
    @FXML
    private TableView<List<Double>> testTableView;

    @FXML
    private Tab trainigTab;

    @FXML
    private Tab testingTab;

    @FXML
    private Button startTrainingButton;



    private AIConfig originalProjectConfig;
    private AIConfig runnableProjectConfig;

    ResourceBundle resourceBundle;
    Locale locale;
    Scene scene;
    List<List<Double>> trainDataSet;
    List<List<Double>> testDataSet;
    private File projectFile;

    public void init() {
        this.scene = this.currentProjectLabel.getParent().getScene();

        loadLanguage(Locale.getDefault());
        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator("It cant be empty");
        addReqieredFieldValidator(this.mutationProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.mutationProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.crossoverProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.addLinkProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.addNodeProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.mutateBiasProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.toggleLinkProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.weightReplaceProbabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.excessCoefficientTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.disjointCoefficientTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.weightCoefficientTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.thresholdCompabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.changeCompabilityTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.survivalThresholdTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieAgeThresholdTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieYouthThresholdTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieOldPenaltyTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieYouthBoostTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.specieFitnessMaxTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.inputNodesTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.outputNodesTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.maxPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.maxBiasPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleSurvivalCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleEventTimeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.extraFeatureCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.popSizeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.numberEpochsTextField, requiredFieldValidator);









        this.inputNodesTextField.setDisable(true);
        this.outputNodesTextField.setDisable(true);

        this.eleEventsToogle.selectedProperty().addListener((observable, oldValue, newValue) ->{
            if(eleEventsToogle.isSelected()){
                eleEventTimeTextField.setVisible(true);
                eleSurvivalCountTextField.setVisible(true);
            } else {
                eleEventTimeTextField.setVisible(false);
                eleSurvivalCountTextField.setVisible(false);
            }
        });
        this.eleEventsToogle.selectedProperty().setValue(false);
        eleEventTimeTextField.setVisible(false);
        eleSurvivalCountTextField.setVisible(false);

        //TODO uncomment
        if (this.originalProjectConfig == null){
            this.parametresPane.setVisible(false);
            this.infoTabPane.setVisible(false);
            this.neatOptionsLabel.setText(resourceBundle.getString("NO_OPEN_PROJECTS"));
        }

        this.dataSetsScrollPane.setVisible(false);

        this.datasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                loadDataset(newValue);
                this.dataSetsScrollPane.setVisible(true);
                //this.originalProjectConfig.updateConfig("AI.SOURCE", newValue+"BestNetwork_temp.ser");
                //this.originalProjectConfig.updateConfig("SAVE.LOCATION", newValue+"BestNetwork_temp.ser");
                this.runnableProjectConfig.updateConfig("AI.SOURCE", newValue+"BestNetwork_temp.ser");
                this.runnableProjectConfig.updateConfig("SAVE.LOCATION", newValue+"BestNetwork_temp.ser");
                trainigTab.setDisable(false);
                testingTab.setDisable(false);
            }
        });

        trainigTab.setDisable(true);
        testingTab.setDisable(true);




    }

    private void loadDataset(String datasetName){
        String pathToDatasetDirectory = this.currentProjectTextField.getText() + "\\datasets\\" + datasetName;
        String trainDataSetName = pathToDatasetDirectory + "\\" +datasetName+"@train.dataset";
        String testDataSetName = pathToDatasetDirectory + "\\" +datasetName+"@test.dataset";
        try {
            /*Read training dataset file*/
            BufferedReader reader = new BufferedReader(new FileReader(new File(trainDataSetName)));
            StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(),";");
            this.inputNodesTextField.setText(stringTokenizer.nextToken()); //get number of inputs
            this.outputNodesTextField.setText(stringTokenizer.nextToken()); // get number of outputs
            stringTokenizer = new StringTokenizer(reader.readLine(),";");
            /*Prepare headers of table's columns*/
            int tokens = stringTokenizer.countTokens();
            for (int i = 0; i < tokens; i++){
                TableColumn tableColumn = new TableColumn();
                tableColumn.setText(stringTokenizer.nextToken());
                tableColumn.setPrefWidth(65);
                final int index = i;
                tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
                {
                    return new SimpleObjectProperty<Double>((p.getValue().get(index)));
                });
                this.trainTableView.getColumns().add(tableColumn);
            }
            /*Read dataset values*/
            trainDataSet = new ArrayList<>(50);
            String line = reader.readLine();
            while (line != null) {
                stringTokenizer = new StringTokenizer(line,";");
                List<Double> row = new ArrayList<>();
                while(stringTokenizer.hasMoreTokens()){
                    row.add(Double.valueOf(stringTokenizer.nextToken()));
                }
                trainDataSet.add(row);
                line = reader.readLine();
            }
            /*Put read data into tableview*/
            ObservableList<List<Double>> observableList = FXCollections.observableArrayList();
                    observableList.addAll(trainDataSet);
            this.trainTableView.setItems(observableList);
            /*Reading of training dataset is over*/


            /*Read testing dataset file*/
            reader = new BufferedReader(new FileReader(new File(testDataSetName)));
            stringTokenizer = new StringTokenizer(reader.readLine(),";");
            stringTokenizer = new StringTokenizer(reader.readLine(),";");
            /*Prepare headers of table's columns*/
            tokens = stringTokenizer.countTokens();
            for (int i = 0; i < tokens; i++){
                TableColumn tableColumn = new TableColumn();
                tableColumn.setText(stringTokenizer.nextToken());
                tableColumn.setPrefWidth(65);
                final int index = i;
                tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
                {
                    return new SimpleObjectProperty<Double>((p.getValue().get(index)));
                });
                this.testTableView.getColumns().add(tableColumn);
            }
            /*Read dataset values*/
            testDataSet = new ArrayList<>(50);
            line = reader.readLine();
            while (line != null) {
                stringTokenizer = new StringTokenizer(line,";");
                List<Double> row = new ArrayList<>();
                while(stringTokenizer.hasMoreTokens()){
                    row.add(Double.valueOf(stringTokenizer.nextToken()));
                }
                row.add(null);
                testDataSet.add(row);
                line = reader.readLine();
            }
            /*Put read data into tableview*/
            ObservableList<List<Double>> observableList1 = FXCollections.observableArrayList();
            observableList1.addAll(testDataSet);
            this.testTableView.setItems(observableList1);
            /*Reading of training dataset is over*/

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadLanguage(Locale locale) {
        switch (locale.getLanguage()){
            case "ru":
                resourceBundle = ResourceBundle.getBundle("properties.languages.language", CsControl.Cp1251);
                break;
            default:
                resourceBundle = ResourceBundle.getBundle("properties.languages.language", locale);
                break;
        }
        //resourceBundle = ResourceBundle.getBundle("properties.languages.language", locale);
        settings.setText(resourceBundle.getString("SETTINGS"));
        file.setText(resourceBundle.getString("FILE"));
        help.setText(resourceBundle.getString("HELP"));
        newProject.setText(resourceBundle.getString("NEW_PROJECT"));
        openProject.setText(resourceBundle.getString("OPEN_PROJECT"));
        saveProject.setText(resourceBundle.getString("SAVE_PROJECT"));
        exit.setText(resourceBundle.getString("EXIT"));
        languages.setText(resourceBundle.getString("LANGUAGES"));
        about.setText(resourceBundle.getString("ABOUT"));
        newProjectTooltip.setText(resourceBundle.getString("NEW_PROJECT"));
        openProjectTooltip.setText(resourceBundle.getString("OPEN_PROJECT"));
        currentProjectLabel.setText(resourceBundle.getString("CURRENT_PROJECT"));
        //neatOptionsLabel.setText(resourceBundle.getString("NEAT_OPTIONS"));
        GASettingsTitledPane.setText(resourceBundle.getString("GA_SETTINGS"));
        neatOptionGASettings = resourceBundle.getString("NEAT_OPTIONS");
        neatOptionNoProjects = resourceBundle.getString("NO_OPEN_PROJECTS");
        mutationProbabilityTextField.setPromptText(resourceBundle.getString("MUTATION_PROBABILITY"));
        crossoverProbabilityTextField.setPromptText(resourceBundle.getString("CROSSOVER_PROBABILITY"));
        addLinkProbabilityTextField.setPromptText(resourceBundle.getString("ADD_LINK_PROBABILITY"));
        addNodeProbabilityTextField.setPromptText(resourceBundle.getString("ADD_NODE_PROBABILITY"));
        mutateBiasProbabilityTextField.setPromptText(resourceBundle.getString("MUTATE_BIAS_PROBABILITY"));
        toggleLinkProbabilityTextField.setPromptText(resourceBundle.getString("TOGGLE_LINK_PROBABILITY"));
        weightReplaceProbabilityTextField.setPromptText(resourceBundle.getString("WEIGHT_REPLACED_PROBABILITY"));


        AlertWindow.setLanguage(resourceBundle);
        NewProjectDialogue.getInstance(this.scene).setLanguage(resourceBundle);



    }

    private AIConfig loadConfig(String path){
        return new NEATLoader().loadConfig(path);
    }

    @FXML
    private void openProject(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()+"\\projects\\"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("NEAT file", "*.neat"));
        fileChooser.setTitle(resourceBundle.getString("CHOOSE_PROJECT"));
        File projectFile = fileChooser.showOpenDialog(this.scene.getWindow());
        openProject(projectFile);
    }


    private void openProject(File projectFile) {
        if(projectFile != null){
            this.originalProjectConfig = this.loadConfig(projectFile.getPath());
            this.runnableProjectConfig = new NEATConfig((NEATConfig) this.originalProjectConfig);
            this.currentProjectTextField.setText(projectFile.getParent());
            this.neatOptionsLabel.setText(neatOptionGASettings);
            parametresPane.setVisible(true);
            infoTabPane.setVisible(true);
            this.projectFile = projectFile;
            fillDataSetChoiceBox(originalProjectConfig);
            fillFieldsUsingOriginalConfig();
        }
    }

    private void fillDataSetChoiceBox(AIConfig sourceConfig) {
        this.datasetChoiceBox.getItems().clear();
        StringTokenizer stringTokenizer = new StringTokenizer(sourceConfig.configElement("ALLOWED.DATASETS"),";");
        while (stringTokenizer.hasMoreTokens()) {
            this.datasetChoiceBox.getItems().add(stringTokenizer.nextToken());
        }
    }

    @FXML
    private void createNewProject(ActionEvent actionEvent){
        NewProjectDialogue dialogue = NewProjectDialogue.getInstance(this.scene);
        dialogue.show();
        if(dialogue.getProjectName().length() != 0 ){
            try {
                this.currentProjectTextField.setText(dialogue.getProjectLocation()+dialogue.getProjectName());
                this.originalProjectConfig = this.loadConfig(this.currentProjectTextField.getText()+"\\"+dialogue.getProjectName()+".neat");
                /*this.originalProjectConfig.updateConfig("AI.SOURCE", dialogue.getProjectName()+"BestNetwork.ser");
                this.originalProjectConfig.updateConfig("SAVE.LOCATION", dialogue.getProjectName()+"BestNetwork.ser");*/
                this.neatOptionsLabel.setText(neatOptionGASettings);
                parametresPane.setVisible(true);
                infoTabPane.setVisible(true);
            } catch (Exception ex){
                AlertWindow.getAlert(ex.getMessage()).show();
            }
        }
    }

    private <T extends TextField & IFXLabelFloatControl> void validationPaneFormatter(
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
        jfxTextField
                .textProperty()
                .addListener((observable, oldValue, newValue) -> jfxTextField.validate());
    }

    private <T extends TextField & IFXLabelFloatControl> void addReqieredFieldValidator(
            T jfxTextField, RequiredFieldValidator requiredFieldValidator ) {
        jfxTextField.getValidators().add(requiredFieldValidator);
        jfxTextField.getValidators().add(requiredFieldValidator);
        jfxTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                jfxTextField.validate();
            }
        });
        validationPaneFormatter(jfxTextField);

    }

    @FXML
    private void saveConfig(){
        try {
            initRunnableConfigUsingGUI();
            runnableProjectConfig.saveConfig(this.projectFile);
        } catch (IOException e) {
            AlertWindow.getAlert("CANT_SAVE_FILE").show();
        }
    }

    private void initRunnableConfigUsingGUI() {
         runnableProjectConfig.updateConfig("PROBABILITY.MUTATION", mutationProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.CROSSOVER", crossoverProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.ADDLINK", addLinkProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.ADDNODE", addNodeProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.MUTATEBIAS", mutateBiasProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.TOGGLELINK", toggleLinkProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("PROBABILITY.WEIGHT.REPLACED", weightReplaceProbabilityTextField.getText());
         runnableProjectConfig.updateConfig("GENERATOR.SEED", generatorSeedTextField.getText());
         runnableProjectConfig.updateConfig("EXCESS.COEFFICIENT", excessCoefficientTextField.getText());
         runnableProjectConfig.updateConfig("DISJOINT.COEFFICIENT", disjointCoefficientTextField.getText());
         runnableProjectConfig.updateConfig("WEIGHT.COEFFICIENT", weightCoefficientTextField.getText());
         runnableProjectConfig.updateConfig("COMPATABILITY.THRESHOLD", thresholdCompabilityTextField.getText());
         runnableProjectConfig.updateConfig("COMPATABILITY.CHANGE", changeCompabilityTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.COUNT", specieCountTextField.getText());
         runnableProjectConfig.updateConfig("SURVIVAL.THRESHOLD", survivalThresholdTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.AGE.THRESHOLD", specieAgeThresholdTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.YOUTH.THRESHOLD", specieYouthThresholdTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.OLD.PENALTY", specieOldPenaltyTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.YOUTH.BOOST", specieYouthBoostTextField.getText());
         runnableProjectConfig.updateConfig("SPECIE.FITNESS.MAX", specieFitnessMaxTextField.getText());
         runnableProjectConfig.updateConfig("INPUT.NODES", inputNodesTextField.getText());
         runnableProjectConfig.updateConfig("OUTPUT.NODES", outputNodesTextField.getText());
         runnableProjectConfig.updateConfig("MAX.PERTURB", maxPertrubTextField.getText());
         runnableProjectConfig.updateConfig("MAX.BIAS.PERTURB", maxBiasPertrubTextField.getText());
         runnableProjectConfig.updateConfig("FEATURE.SELECTION", String.valueOf(featureSelectionToogle.isSelected()));
         runnableProjectConfig.updateConfig("RECURRENCY.ALLOWED", String.valueOf(reccurencyAllowedToogle.isSelected()));
         runnableProjectConfig.updateConfig("ELE.EVENTS", String.valueOf(eleEventsToogle.isSelected()));
         runnableProjectConfig.updateConfig("ELE.SURVIVAL.COUNT", eleSurvivalCountTextField.getText());
         runnableProjectConfig.updateConfig("ELE.EVENT.TIME", eleEventTimeTextField.getText());
         runnableProjectConfig.updateConfig("KEEP.BEST.EVER", String.valueOf(keepBestEverToogle.isSelected()));
         runnableProjectConfig.updateConfig("EXTRA.FEATURE.COUNT", extraFeatureCountTextField.getText());
         runnableProjectConfig.updateConfig("POP.SIZE", popSizeTextField.getText());
         runnableProjectConfig.updateConfig("NUMBER.EPOCHS", numberEpochsTextField.getText());
    }
    
    private void fillFieldsUsingOriginalConfig(){
        mutationProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.MUTATION"));
        crossoverProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.CROSSOVER"));
        addLinkProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.ADDLINK"));
        addNodeProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.ADDNODE"));
        mutateBiasProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.MUTATEBIAS"));
        toggleLinkProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.TOGGLELINK"));
        weightReplaceProbabilityTextField.setText(originalProjectConfig.configElement("PROBABILITY.WEIGHT.REPLACED"));
        generatorSeedTextField.setText(originalProjectConfig.configElement("GENERATOR.SEED"));
        excessCoefficientTextField.setText(originalProjectConfig.configElement("EXCESS.COEFFICIENT"));
        disjointCoefficientTextField.setText(originalProjectConfig.configElement("DISJOINT.COEFFICIENT"));
        weightCoefficientTextField.setText(originalProjectConfig.configElement("WEIGHT.COEFFICIENT"));
        thresholdCompabilityTextField.setText(originalProjectConfig.configElement("COMPATABILITY.THRESHOLD"));
        changeCompabilityTextField.setText(originalProjectConfig.configElement("COMPATABILITY.CHANGE"));
        specieCountTextField.setText(originalProjectConfig.configElement("SPECIE.COUNT"));
        survivalThresholdTextField.setText(originalProjectConfig.configElement("SURVIVAL.THRESHOLD"));
        specieAgeThresholdTextField.setText(originalProjectConfig.configElement("SPECIE.AGE.THRESHOLD"));
        specieYouthThresholdTextField.setText(originalProjectConfig.configElement("SPECIE.YOUTH.THRESHOLD"));
        specieOldPenaltyTextField.setText(originalProjectConfig.configElement("SPECIE.OLD.PENALTY"));
        specieYouthBoostTextField.setText(originalProjectConfig.configElement("SPECIE.YOUTH.BOOST"));
        specieFitnessMaxTextField.setText(originalProjectConfig.configElement("SPECIE.FITNESS.MAX"));
        maxPertrubTextField.setText(originalProjectConfig.configElement("MAX.PERTURB"));
        maxBiasPertrubTextField.setText(originalProjectConfig.configElement("MAX.BIAS.PERTURB"));
        featureSelectionToogle.setSelected(Boolean.parseBoolean(originalProjectConfig.configElement("FEATURE.SELECTION")));
        reccurencyAllowedToogle.setSelected(Boolean.parseBoolean(originalProjectConfig.configElement("RECURRENCY.ALLOWED")));
        eleEventsToogle.setSelected(Boolean.parseBoolean(originalProjectConfig.configElement("ELE.EVENTS")));
        eleSurvivalCountTextField.setText(originalProjectConfig.configElement("ELE.SURVIVAL.COUNT"));
        eleEventTimeTextField.setText(originalProjectConfig.configElement("ELE.EVENT.TIME"));
        keepBestEverToogle.setSelected(Boolean.parseBoolean(originalProjectConfig.configElement("KEEP.BEST.EVER")));
        extraFeatureCountTextField.setText(originalProjectConfig.configElement("EXTRA.FEATURE.COUNT"));
        popSizeTextField.setText(originalProjectConfig.configElement("POP.SIZE"));
        numberEpochsTextField.setText(originalProjectConfig.configElement("NUMBER.EPOCHS"));
    }
    
    @FXML
    private void prepareNewData(ActionEvent actionEvent) {
        DataPreparatorDialogue.getInstance(this.scene).setProjectPath(this.currentProjectTextField.getText()).show();
        String nameOfDataSet = DataPreparatorDialogue.getInstance(this.scene).getNameOfDataSet();
        if( nameOfDataSet != null ){
            if(nameOfDataSet.length() != 0){
                this.runnableProjectConfig.updateConfig("ALLOWED.DATASETS", this.runnableProjectConfig.configElement("ALLOWED.DATASETS")+";"+nameOfDataSet);
                this.originalProjectConfig.updateConfig("ALLOWED.DATASETS", this.originalProjectConfig.configElement("ALLOWED.DATASETS")+";"+nameOfDataSet);
                try {
                    this.originalProjectConfig.saveConfig(this.projectFile);
                } catch (IOException e) {
                    AlertWindow.getAlert("Не удалось сохранить новый конфиг");
                }
                fillDataSetChoiceBox(this.runnableProjectConfig);
            }
        }
    }

    public void generateNewSeed() {
        generatorSeedTextField.setText(String.valueOf(System.currentTimeMillis()));
    }


    public void trainModel(ActionEvent actionEvent) {
    }
}
