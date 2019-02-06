package ru.filippov.GUI.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.controls.base.IFXLabelFloatControl;
import com.jfoenix.skins.JFXTextFieldSkin;
import com.jfoenix.skins.ValidationPane;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.animation.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.log4j.Logger;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
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

    Logger logger = Logger.getLogger(MainController.class);

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

    @FXML
    private SplitPane splitPane;
    
    
    @FXML private Label neatOptionsLabel;
    private Label noActiveProjectLabel;



    private Timeline openMenu, closeMenu;
    private static final double SPEED = 2;

    @FXML private MaterialDesignIconView iconView;
    @FXML private BorderPane menuBorderPane;
    private ScrollPane parametresScrollPane;
    private VBox titlesPaneContainer;
    private TitledPane GASettingsTitledPane;
    private JFXTextField mutationProbabilityTextField;
    private JFXTextField crossoverProbabilityTextField;
    private JFXTextField addLinkProbabilityTextField;
    private JFXTextField addNodeProbabilityTextField;
    private JFXTextField mutateBiasProbabilityTextField;
    private JFXTextField toggleLinkProbabilityTextField;
    private JFXTextField weightReplaceProbabilityTextField;
    private TitledPane neatSpecificTitledPane;
    private JFXTextField generatorSeedTextField;
    private JFXButton generateSeedButton;
    private JFXTextField excessCoefficientTextField;
    private JFXTextField disjointCoefficientTextField;
    private JFXTextField weightCoefficientTextField;
    private TitledPane speciationControlTitledPane;
    private JFXTextField thresholdCompabilityTextField;
    private JFXTextField changeCompabilityTextField;
    private JFXTextField specieCountTextField;
    private JFXTextField survivalThresholdTextField;
    private JFXTextField specieAgeThresholdTextField;
    private JFXTextField specieYouthThresholdTextField;
    private JFXTextField specieOldPenaltyTextField;
    private JFXTextField specieYouthBoostTextField;
    private JFXTextField specieFitnessMaxTextField;
    private TitledPane networkControlTitledPane;
    private JFXTextField inputNodesTextField;
    private JFXTextField outputNodesTextField;
    private JFXTextField maxPertrubTextField;
    private JFXTextField maxBiasPertrubTextField;
    private JFXToggleButton featureSelectionToogle;
    private JFXToggleButton reccurencyAllowedToogle;
    private TitledPane extinctionControlTitledPane;
    private JFXToggleButton eleEventsToogle;
    private JFXTextField eleSurvivalCountTextField;
    private JFXTextField eleEventTimeTextField;
    private TitledPane epochControlTitledPane;
    private JFXToggleButton keepBestEverToogle;
    private JFXTextField extraFeatureCountTextField;
    private JFXTextField popSizeTextField;
    private JFXTextField numberEpochsTextField;

    
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
    @FXML private BorderPane trainBorderPane;
    @FXML private ProgressBar trainingProgressBar;
    @FXML private ProgressBar testingProgressBar;


    @FXML
    private Tab testingTab;

    @FXML
    private Button startTrainingButton;

    @FXML
    private MaterialDesignIconView openMenuIcon;


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


        this.noActiveProjectLabel = new Label();

        this.parametresScrollPane = new ScrollPane();
        this.parametresScrollPane.setFitToWidth(true);
        this.titlesPaneContainer = new VBox();

        VBox tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.GASettingsTitledPane = new TitledPane();
        this.mutationProbabilityTextField = new JFXTextField();
        this.crossoverProbabilityTextField = new JFXTextField();
        this.addLinkProbabilityTextField = new JFXTextField();
        this.addNodeProbabilityTextField = new JFXTextField();
        this.mutateBiasProbabilityTextField = new JFXTextField();
        this.toggleLinkProbabilityTextField = new JFXTextField();
        this.weightReplaceProbabilityTextField = new JFXTextField();
        this.mutationProbabilityTextField.setLabelFloat(true);
        this.crossoverProbabilityTextField.setLabelFloat(true);
        this.addLinkProbabilityTextField.setLabelFloat(true);
        this.addNodeProbabilityTextField.setLabelFloat(true);
        this.mutateBiasProbabilityTextField.setLabelFloat(true);
        this.toggleLinkProbabilityTextField.setLabelFloat(true);
        this.weightReplaceProbabilityTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
            this.mutationProbabilityTextField,
            this.crossoverProbabilityTextField,
            this.addLinkProbabilityTextField,
            this.addNodeProbabilityTextField,
            this.mutateBiasProbabilityTextField,
            this.toggleLinkProbabilityTextField,
            this.weightReplaceProbabilityTextField
        );
        GASettingsTitledPane.setContent(tempVbox);

        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.neatSpecificTitledPane = new TitledPane();

        this.generatorSeedTextField = new JFXTextField();
        this.generateSeedButton = new JFXButton();
        this.generateSeedButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.REFRESH));
        this.generateSeedButton.setOnAction(event -> {
            this.generateNewSeed();
        });
        HBox hBox = new HBox(20, this.generatorSeedTextField, generateSeedButton);
        this.excessCoefficientTextField = new JFXTextField();
        this.disjointCoefficientTextField = new JFXTextField();
        this.weightCoefficientTextField = new JFXTextField();
        
        this.generatorSeedTextField.setLabelFloat(true);
        this.excessCoefficientTextField.setLabelFloat(true);
        this.disjointCoefficientTextField.setLabelFloat(true);
        this.weightCoefficientTextField.setLabelFloat(true);

        tempVbox.getChildren().addAll(
                hBox,
                this.excessCoefficientTextField,
                this.disjointCoefficientTextField,
                this.weightCoefficientTextField
        );
        this.neatSpecificTitledPane.setContent(tempVbox);

        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.speciationControlTitledPane = new TitledPane();
        this.thresholdCompabilityTextField = new JFXTextField();
        this.changeCompabilityTextField = new JFXTextField();
        this.specieCountTextField = new JFXTextField();
        this.survivalThresholdTextField = new JFXTextField();
        this.specieAgeThresholdTextField = new JFXTextField();
        this.specieYouthThresholdTextField = new JFXTextField();
        this.specieOldPenaltyTextField = new JFXTextField();
        this.specieYouthBoostTextField = new JFXTextField();
        this.specieFitnessMaxTextField = new JFXTextField();
        this.thresholdCompabilityTextField.setLabelFloat(true);
        this.changeCompabilityTextField.setLabelFloat(true);
        this.specieCountTextField.setLabelFloat(true);
        this.survivalThresholdTextField.setLabelFloat(true);
        this.specieAgeThresholdTextField.setLabelFloat(true);
        this.specieYouthThresholdTextField.setLabelFloat(true);
        this.specieOldPenaltyTextField.setLabelFloat(true);
        this.specieYouthBoostTextField.setLabelFloat(true);
        this.specieFitnessMaxTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.thresholdCompabilityTextField,
                this.changeCompabilityTextField,
                this.specieCountTextField,
                this.survivalThresholdTextField,
                this.specieAgeThresholdTextField,
                this.specieYouthThresholdTextField,
                this.specieOldPenaltyTextField,
                this.specieYouthBoostTextField,
                this.specieFitnessMaxTextField     
        );
        this.speciationControlTitledPane.setContent(tempVbox);

        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.networkControlTitledPane = new TitledPane();
        this.inputNodesTextField = new JFXTextField();
        this.outputNodesTextField = new JFXTextField();
        this.maxPertrubTextField = new JFXTextField();
        this.maxBiasPertrubTextField = new JFXTextField();
        this.featureSelectionToogle = new JFXToggleButton();
        this.reccurencyAllowedToogle = new JFXToggleButton();
        this.inputNodesTextField.setLabelFloat(true);
        this.outputNodesTextField.setLabelFloat(true);
        this.maxPertrubTextField.setLabelFloat(true);
        this.maxBiasPertrubTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.inputNodesTextField,
                this.outputNodesTextField,
                this.maxPertrubTextField,
                this.maxBiasPertrubTextField,
                this.featureSelectionToogle,
                this.reccurencyAllowedToogle
        );
        this.networkControlTitledPane.setContent(tempVbox);


        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.extinctionControlTitledPane = new TitledPane();
        this.eleEventsToogle = new JFXToggleButton();
        this.eleSurvivalCountTextField = new JFXTextField();
        this.eleEventTimeTextField = new JFXTextField();
        tempVbox.getChildren().addAll(
                this.eleEventsToogle,
                this.eleSurvivalCountTextField,
                this.eleEventTimeTextField
        );
        this.extinctionControlTitledPane.setContent(tempVbox);

        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.epochControlTitledPane = new TitledPane();
        this.keepBestEverToogle = new JFXToggleButton();
        this.extraFeatureCountTextField = new JFXTextField();
        this.popSizeTextField = new JFXTextField();
        this.numberEpochsTextField = new JFXTextField();
        tempVbox.getChildren().addAll(
                this.keepBestEverToogle,
                this.extraFeatureCountTextField,
                this.popSizeTextField,
                this.numberEpochsTextField
        );
        this.epochControlTitledPane.setContent(tempVbox);


        this.titlesPaneContainer.getChildren().addAll(
                this.GASettingsTitledPane,
                this.neatSpecificTitledPane,
                this.speciationControlTitledPane,
                this.networkControlTitledPane,
                this.extinctionControlTitledPane,
                this.epochControlTitledPane
        );
        parametresScrollPane.setContent(this.titlesPaneContainer);

        this.menuBorderPane.setCenter(noActiveProjectLabel);
        this.menuBorderPane.setMinWidth(20);
        this.menuBorderPane.setPrefWidth(300);
        this.menuBorderPane.setMaxWidth(300);

        this.splitPane.getDividers().get(0).positionProperty().setValue(0.02);

        openMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED), event -> menuSlide(openMenu,1))
        );
        openMenu.setCycleCount(Timeline.INDEFINITE);

        closeMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED), event -> menuSlide(closeMenu, -1))
        );
        closeMenu.setCycleCount(Timeline.INDEFINITE);
        menuBorderPane.getCenter().setVisible(false);

        loadLanguage(Locale.getDefault());
        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator("It cant be empty");
        /*addReqieredFieldValidator(this.mutationProbabilityTextField, requiredFieldValidator);
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
        addReqieredFieldValidator(this.numberEpochsTextField, requiredFieldValidator);*/



        RotateTransition iconRotateTransition = new RotateTransition(Duration.millis(500), iconView);
        iconRotateTransition.setFromAngle(0);
        iconRotateTransition.setToAngle(180);
        iconRotateTransition.setAutoReverse(true);

        menuBorderPane.setOnMouseEntered(evt -> {
            menuBorderPane.setMinWidth(menuBorderPane.getMinWidth()-0.1);
            menuBorderPane.setMaxWidth(300);
            menuBorderPane.getCenter().setVisible(true);
            //neatOptionsLabel.setVisible(true);
            iconRotateTransition.setRate(1);
            iconRotateTransition.play();
            closeMenu.stop(); openMenu.play();
        });
        menuBorderPane.setOnMouseExited(evt -> {
            menuBorderPane.setMinWidth(menuBorderPane.getMinWidth()+0.1);
            menuBorderPane.setMaxWidth(menuBorderPane.getMinWidth()+0.2);
            menuBorderPane.getCenter().setVisible(false);
            menuBorderPane.getCenter().setVisible(false);
            iconRotateTransition.setRate(-1);
            iconRotateTransition.play();
            openMenu.stop(); closeMenu.play();
        });






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

        if (this.originalProjectConfig == null){
            this.parametresScrollPane.setVisible(false);
            this.infoTabPane.setVisible(false);
            this.dataSetsScrollPane.setVisible(false);
        }


        this.datasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                loadDataset(newValue);
                this.dataSetsScrollPane.setVisible(true);
                //this.originalProjectConfig.updateConfig("AI.SOURCE", newValue+"BestNetwork_temp.ser");
                //this.originalProjectConfig.updateConfig("SAVE.LOCATION", newValue+"BestNetwork_temp.ser");

                this.runnableProjectConfig.updateConfig("SAVE.LOCATION", newValue+"BestNetwork_temp.ser");
                if(new File(this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork.ser").exists()){
                    this.runnableProjectConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork.ser");
                    this.runnableProjectConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"@test.dataset");
                    logger.debug(this.runnableProjectConfig.configElement("AI.SOURCE"));
                    logger.debug(this.runnableProjectConfig.configElement("INPUT.DATA"));
                    this.testingTab.setDisable(false);
                } else if(new File(this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser").exists()){
                    this.runnableProjectConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"@test.dataset");
                    this.runnableProjectConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser");
                    logger.debug(this.runnableProjectConfig.configElement("AI.SOURCE"));
                    logger.debug(this.runnableProjectConfig.configElement("INPUT.DATA"));
                    this.testingTab.setDisable(false);
                } else {
                    this.runnableProjectConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser");
                    logger.debug(this.runnableProjectConfig.configElement("AI.SOURCE"));
                    this.testingTab.setDisable(true);
                }
                /*trainigTab.setDisable(false);
                testingTab.setDisable(false);*/
                this.startTrainingButton.setDisable(false);
            } else {
                this.startTrainingButton.setDisable(true);
            }
        });

        trainigTab.setDisable(true);
        testingTab.setDisable(true);
        this.trainingProgressBar = new ProgressBar(0);
        this.testingProgressBar = new ProgressBar(0);
        trainigTab.setGraphic(new BorderPane(trainingProgressBar,null,null,null, null));
        testingTab.setGraphic(new BorderPane(testingProgressBar,null,null,null, null));




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
        neatOptionsLabel.setText(resourceBundle.getString("NEAT_OPTIONS"));
        noActiveProjectLabel.setText(resourceBundle.getString("NO_OPEN_PROJECTS"));
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
            //this.neatOptionsLabel.setText(neatOptionGASettings);
            this.menuBorderPane.setCenter(this.parametresScrollPane);
            this.infoTabPane.setVisible(true);
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
        if(dialogue.getProjectFile() != null ){
            try {
                this.openProject(dialogue.getProjectFile());
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
        //trainigTab.setContent(new SideBar(30, new TextField()));
        initRunnableConfigUsingGUI();
        this.runnableProjectConfig.updateConfig("TRAINING.SET", this.currentProjectTextField.getText()+"\\datasets\\"+this.datasetChoiceBox.getValue()+"\\"+this.datasetChoiceBox.getValue()+"@train_temp.dataset");

        try {
            this.saveTempDataSet(this.runnableProjectConfig.configElement("TRAINING.SET"));
            logger.debug(this.runnableProjectConfig.configElement("TRAINING.SET"));
            //TODO replace Test set init
            this.runnableProjectConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+this.datasetChoiceBox.getValue()+"\\"+this.datasetChoiceBox.getValue()+"@test_temp.dataset");
            trainigTab.setDisable(false);
            infoTabPane.getSelectionModel().select(trainigTab);
            NEATTrainingForJavaFX neatTrainingForJavaFX = new NEATTrainingForJavaFX();
            //TODO refresh SpecieCounter
            neatTrainingForJavaFX.initialise(runnableProjectConfig);
            neatTrainingForJavaFX.statusProperty().addListener(observable -> {});
            this.trainingProgressBar.progressProperty().bind(neatTrainingForJavaFX.statusProperty());
            final Thread thread = new Thread(neatTrainingForJavaFX);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }


    }

    public void showMenu(ActionEvent actionEvent) {

    }

    private void menuSlide(Timeline timeline, int i) {
        //0.3373729476153245
        double pos = splitPane.getDividers().get(0).getPosition();
        double width = menuBorderPane.getWidth();


        if(pos > 1.0) {
            splitPane.setDividerPositions(1.0);
            timeline.stop();

        }
        else if (width == menuBorderPane.getMinWidth()) {

            splitPane.setDividerPositions(pos+0.001f);
            timeline.stop();

        }
        else splitPane.setDividerPositions(pos +  ((double)i)/100);
    }

    public void initVisibleElements() {
        splitPane.lookupAll(".split-pane-divider").stream()
                .forEach(div ->  div.setMouseTransparent(true) );
    }

    public File saveTempDataSet(String dataSetPath) throws IOException {
        File file = new File(dataSetPath);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
        double value;
        for(List<Double> list : this.trainDataSet){
            for (int i = 0; i < list.size(); i++) {
                value = list.get(i);
                bufferedWriter.write(String.valueOf(value));
                if(i != list.size()-1) bufferedWriter.write(";");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
        bufferedWriter.close();
        return file;
    }
}
