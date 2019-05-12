package ru.filippov.GUI.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.log4j.Logger;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;

import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.functions.ActivationFunctionFinder;
import org.neat4j.neat.nn.core.functions.LinearFunction;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;
import ru.filippov.GUI.customNodes.TreeCellIContextMenu;
import ru.filippov.GUI.customNodes.TreeItemContextMenu;
import ru.filippov.GUI.customNodes.ZoomPane;
import ru.filippov.GUI.windows.*;
import ru.filippov.prediction.WindowPrediction;
import ru.filippov.utils.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class MainController {


    private TrainReporter trainReporter;



    static class ProjectFileDescriptor{


        public enum TYPE{
            PROJECT,
            TRAINING_SET,
            NEAT_CONFIG,
            TRAINED_MODEL,
            TRAINING_FOLDER,
            MODEL_FOLDER
        }

        private TYPE type;
        private String name;
        private String directoryPath;
        private String extension;
        private Node graphic;

        public ProjectFileDescriptor(TYPE type, String directoryPath, String nameString, String extension) {
            this.type = type;

            MaterialIcon projectIcon = MaterialIcon.FOLDER_OPEN;
            OctIcon neatIcon = OctIcon.GEAR;
            OctIcon trainIcon = OctIcon.BEAKER;
            MaterialDesignIcon testDatasetIcon = MaterialDesignIcon.CHART_AREASPLINE;
            MaterialDesignIcon trainedModelIcon = MaterialDesignIcon.VECTOR_POLYGON;

            switch (type){
                case TRAINED_MODEL:
                    graphic  = new MaterialDesignIconView(trainedModelIcon);
                    break;
                case MODEL_FOLDER:
                    graphic  = new MaterialDesignIconView(trainedModelIcon);
                    break;
                case PROJECT:
                    graphic  = new MaterialIconView(projectIcon);
                    break;
                case NEAT_CONFIG:
                    graphic = new OctIconView(neatIcon);
                    break;
                case TRAINING_SET:
                    graphic = new OctIconView(trainIcon);
                    break;
                case TRAINING_FOLDER:
                    graphic = new OctIconView(trainIcon);
                    break;
            }



            this.name = nameString;
            this.directoryPath = directoryPath;
            this.extension = extension;
        }

        public TYPE getType() {
            return type;
        }

        public void setType(TYPE type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDirectoryPath() {
            return directoryPath;
        }

        public void setDirectoryPath(String directoryPath) {
            this.directoryPath = directoryPath;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        @Override
        public String toString() {
            return name;
        }

        public Node getGraphic() {
            return graphic;
        }

        public void setGraphic(Node graphic) {
            this.graphic = graphic;
        }

        public File getAsFile() {
            return new File(this.getFullPath());
        }

        public String getFullPath(){
            return this.directoryPath+"\\"+this.name+"."+this.extension;
        }

    }




    Logger logger = Logger.getLogger(MainController.class);

    private static final double SPEED_OF_SLIDE_MENU = 2;
    private static final double MAX_WIDTH_NEAT_MENU = 300;
    private static final double MIN_WIDTH_NEAT_MENU = 20;
    private static final double MAX_WIDTH_PROJECT_MENU = 270;
    private static final double MIN_WIDTH_PROJECT_MENU = 20;





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


    private ContextMenu projectContextMenu;
    private ContextMenu NEATContext;
    private ContextMenu dataContextMenu;
    private ContextMenu folderContextMenu;

    @FXML
    private SplitPane projectSplitPane;
    @FXML
    private BorderPane projectBorderPane;
    @FXML
    private MaterialDesignIconView openProjectMenuIcon;
    @FXML
    private Label projectLabel;
    @FXML
    private ToggleButton pinProjectMenuButton;
    @FXML
    private MaterialDesignIconView pinProjectMenuIcon;
    @FXML private TreeView<ProjectFileDescriptor> projectTreeView;




    @FXML private SplitPane neatSplitPane;
    
    
    @FXML private Label neatOptionsLabel;
    private Label noActiveProjectLabel;



    private Timeline openNEATMenu, closeNEATMenu;
    private Timeline openProjectMenu, closeProjectMenu;


    @FXML private MaterialDesignIconView openNEATMenuIcon;
    @FXML private BorderPane neatMenuBorderPane;
    private VBox neatMenuBorderPaneContainer;
    private HBox neatMenuHeader;
    private JFXButton saveNeatButton;
    private Label neatNameLabel;

    private ScrollPane parametresScrollPane;
    private VBox titlesPaneContainer;
    private TitledPane GASettingsTitledPane;
    private JFXTextField mutationProbabilityTextField;
    private JFXTextField crossoverProbabilityTextField;
    private JFXTextField addLinkProbabilityTextField;
    private JFXTextField addNodeProbabilityTextField;
    private JFXTextField mutateBiasProbabilityTextField;
    private JFXTextField newActivationFunctionProbabilityTextField;
    private JFXTextField toggleLinkProbabilityTextField;
    private JFXTextField weightReplaceProbabilityTextField;
    private TitledPane neatSpecificTitledPane;
    private JFXTextField generatorSeedTextField;
    private JFXButton generateSeedButton;
    private JFXTextField excessCoefficientTextField;
    private JFXTextField disjointCoefficientTextField;
    private JFXTextField weightCoefficientTextField;

    private TitledPane activationFunctionsChooser;
    private Accordion activationFunctionAccordion;
    private TitledPane inputActivationFunctionsChooser;
    private TitledPane hiddenActivationFunctionsChooser;
    private TitledPane outputActivationFunctionsChooser;



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
    private JFXTextField maxWeightPertrubTextField;
    private JFXTextField maxBiasPertrubTextField;
    private JFXToggleButton featureSelectionToggle;
    private JFXToggleButton reccurencyAllowedToggle;
    private TitledPane extinctionControlTitledPane;
    private JFXToggleButton eleEventsToggle;
    private JFXTextField eleSurvivalCountTextField;
    private JFXTextField eleEventTimeTextField;
    private TitledPane epochControlTitledPane;
    private JFXToggleButton keepBestEverToggle;
    private JFXTextField extraFeatureCountTextField;
    private JFXTextField popSizeTextField;
    private JFXTextField numberEpochsTextField;
    private JFXToggleButton terminationValueToggle;
    private JFXTextField terminationValueTextField;

    
    @FXML private TabPane infoTabPane;

    @FXML private ChoiceBox<ProjectFileDescriptor> trainDatasetChoiceBox;
    @FXML
    private ScrollPane dataSetsScrollPane;
    @FXML
    private TitledPane trainTitledPane;
    @FXML
    private TableView<List<Double>> trainTableView;



    @FXML private Tab trainigTab;
    @FXML private JFXTextField lastErrorTextField;
    @FXML
    private JFXTextField currentEpochTextField;

    @FXML
    private JFXButton saveReport;


    @FXML private VBox trainVBox;
    @FXML private ProgressBar trainingProgressBar;

    @FXML
    private BorderPane trainHeaderBorderPane;
    @FXML
    private HBox colorLegendHBox;
    @FXML
    private JFXTextField trainPercentageTextField;
    @FXML
    private JFXButton confirmTrainPercantageButton;



    private Thread trainThread;

    @FXML
    private LineChart<Number, Number> trainErrorChart;
    @FXML private JFXButton trainErrorChartClearButton;
    @FXML
    private LineChart<Number, Number> testErrorChart;
    @FXML private JFXButton testErrorChartClearButton;
    @FXML private LineChart<Number, Number> trainValueGraphicChart;
    @FXML private JFXButton trainValueGraphicChartClearButton;
    @FXML
    private BorderPane netVisualizationBorderPane;
    @FXML
    private Pane drawablePane;
    //private ZoomableCanvas netVisualisationCanvas;
    private AdvancedNetVisualisator netVisualisator;


    @FXML
    private Tab predictionTab;

    @FXML
    private ChoiceBox<ProjectFileDescriptor> trainedModelsChoiceBox;

    @FXML
    private Tooltip openTrainedModelTooltip;

    @FXML
    private ChoiceBox<ProjectFileDescriptor> predictionDatasetChoiceBox;

    @FXML
    private Label choosePredictionDatasetLabel;

    @FXML
    private Tooltip openTrainedModelTooltip1;

    @FXML
    private JFXTextField windowSizeTextField;

    @FXML
    private JFXTextField yearPredictionTextField;

    @FXML
    private JFXButton startPredictionButton;

    @FXML
    private VBox predictionVBox;

    @FXML
    private TitledPane datasetTitledPane;

    @FXML
    private TableView<List<Double>> predictionDatasetTableView;

    @FXML
    private TitledPane mainPredictionTitledPane;

    @FXML
    private JFXTextField predictionErrorTextField;

    @FXML
    private LineChart<Number, Number> predictionChart;


    @FXML JFXTextField testErrorTextField;

    private SimpleObjectProperty<DataKeeper> predictionDataSet =   new SimpleObjectProperty<>(null);;


    @FXML private Button startTrainingButton;
    @FXML private ToggleButton pinButton;


    @FXML
    private MaterialDesignIconView pinIcon;

    private SimpleObjectProperty<Integer> trainEndIndex =   new SimpleObjectProperty<>(null);;


    private AIConfig currentNEATConfig;
    private boolean isNEATConfigSaved;
    private SimpleObjectProperty<TreeItem<ProjectFileDescriptor>> currentNeatConfigFile = new SimpleObjectProperty<>(null);

    private File tempDirectory;

    ResourceBundle resourceBundle;
    Locale locale;
    Scene scene;
    SimpleObjectProperty<DataKeeper> trainSetSimpleObjectProperty = new SimpleObjectProperty<>(null);

    List<ProjectFileDescriptor> trainSets;
    List<ProjectFileDescriptor> testSets;

    int trainingCount = 0;

    List<Map<String, Node>> dynamicPredictionNodes = new ArrayList<Map<String, Node>>(10);

    SimpleObjectProperty<WindowPrediction> windowPrediction = new SimpleObjectProperty<>(null);


    public void init() {
        this.scene = this.currentProjectLabel.getParent().getScene();


        this.scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            this.drawablePane.setMaxWidth(newValue.doubleValue());

            this.netVisualisator.visualiseNet(drawablePane);
        });



        new File(Paths.get("").toAbsolutePath().toString()+"\\projects").mkdir();
        this.tempDirectory =NEATTrainingForJavaFX.TEMP_DIRECTORY_PATH;


        this.scene.getWindow().setOnCloseRequest(event -> {
            Arrays.stream(this.tempDirectory.listFiles()).forEach(file1 -> file1.delete());
        });

        trainTitledPane.setText(null);





        JFXUtils.addValidator(trainPercentageTextField, new NumberValidator("Промежуток [0.0-100.0]", 0 , 100));


        

        confirmTrainPercantageButton.disableProperty().bind(
                trainPercentageTextField.textProperty().isEmpty().or(
                        trainPercentageTextField.getValidators().get(0).hasErrorsProperty()
                ).or(trainSetSimpleObjectProperty.isNull())
        );
        trainPercentageTextField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(trainPercentageTextField.validate()){
                    confirmTrainPercantage(null);
                }
            }
        });
        startTrainingButton.disableProperty().bind(trainEndIndex.isNull().or(trainSetSimpleObjectProperty.isNull()));



        this.trainingCount = 0;

        this.noActiveProjectLabel = new Label();

        this.parametresScrollPane = new ScrollPane();
        this.parametresScrollPane.setFitToWidth(true);
        this.titlesPaneContainer = new VBox();

        /*this.projectTreeView.addEventHandler(MouseEvent.ANY, event -> {
            if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                    ((TreeView<ProjectFileDescriptor>)event.getSource()).getSelectionModel().getSelectedItem().setExpanded(true);
                }

                event.consume();
            }
        });*/

        projectTreeView.setCellFactory(param -> {
            TreeCell<ProjectFileDescriptor> treeCell = new TreeCellIContextMenu();

            treeCell.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {

                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
                    ProjectFileDescriptor value = treeCell.getTreeItem().getValue();


                    switch (value.getType()){
                        case NEAT_CONFIG:
                            openNEATFile(treeCell.getTreeItem());
                            break;
                        case TRAINED_MODEL:
                            if(currentNeatConfigFile == null || currentNEATConfig == null){
                                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getParent());
                            }
                            this.infoTabPane.getSelectionModel().select(predictionTab);
                            if(!trainedModelsChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                                this.trainedModelsChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            }
                            this.trainedModelsChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            logger.debug("projectTreeView double click : projectTreeView " + this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getFullPath() );
                            logger.debug("projectTreeView double click : model " + this.trainedModelsChoiceBox.getSelectionModel().getSelectedItem().getFullPath() );
                            break;
                        case TRAINING_SET:
                            if(currentNeatConfigFile == null || currentNEATConfig == null ){
                                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getParent());
                            }

                            this.infoTabPane.getSelectionModel().select(trainigTab);

                            if(!trainDatasetChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                                this.trainDatasetChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            }
                            this.trainDatasetChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());

                            logger.debug("projectTreeView double click : projectTreeView " + this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getFullPath() );
                            logger.debug("projectTreeView double click : trainDatasetChoiceBox " + this.trainDatasetChoiceBox.getSelectionModel().getSelectedItem().getFullPath() );
                            break;
                    }
                    e.consume();
                }
            });
            return treeCell;
        });


        final PseudoClass train = PseudoClass.getPseudoClass("train");
        final PseudoClass test = PseudoClass.getPseudoClass("test");



        this.colorLegendHBox.visibleProperty().bind(this.trainEndIndex.isNotNull());




        this.trainTableView.setRowFactory(param -> {



            /*Percentage selection with button*/
            final TableRow<List<Double>> row = new TableRow<List<Double>>(){

                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);
                    if(trainEndIndex.getValue() != null){
                        if(i>=0){
                            pseudoClassStateChanged(train, i < trainEndIndex.getValue());
                            pseudoClassStateChanged(test, i >= trainEndIndex.getValue());
                        }
                    }

                }
            };

            trainEndIndex.addListener((observable, oldValue, newValue) -> {
                if(newValue!=null){
                    row.pseudoClassStateChanged(train, row.getIndex() < newValue);
                    row.pseudoClassStateChanged(test, row.getIndex() >= newValue);
                }
            });




            return row;
        });



        VBox tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.GASettingsTitledPane = new TitledPane();

        this.generatorSeedTextField = new JFXTextField();
        this.generateSeedButton = new JFXButton();
        this.generateSeedButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.REFRESH));
        this.generateSeedButton.setOnAction(event -> {
            this.generateNewSeed();
        });
        HBox hBox = new HBox(20, this.generatorSeedTextField, generateSeedButton);

        this.mutationProbabilityTextField = new JFXTextField();
        this.crossoverProbabilityTextField = new JFXTextField();
        this.addLinkProbabilityTextField = new JFXTextField();
        this.addNodeProbabilityTextField = new JFXTextField();
        this.mutateBiasProbabilityTextField = new JFXTextField();
        this.newActivationFunctionProbabilityTextField = new JFXTextField();
        this.toggleLinkProbabilityTextField = new JFXTextField();
        this.weightReplaceProbabilityTextField = new JFXTextField();
        this.mutationProbabilityTextField.setLabelFloat(true);
        this.crossoverProbabilityTextField.setLabelFloat(true);
        this.addLinkProbabilityTextField.setLabelFloat(true);
        this.addNodeProbabilityTextField.setLabelFloat(true);
        this.mutateBiasProbabilityTextField.setLabelFloat(true);
        this.newActivationFunctionProbabilityTextField.setLabelFloat(true);
        this.toggleLinkProbabilityTextField.setLabelFloat(true);
        this.weightReplaceProbabilityTextField.setLabelFloat(true);

        this.mutationProbabilityTextField.setAlignment(Pos.CENTER);
        this.crossoverProbabilityTextField.setAlignment(Pos.CENTER);
        this.addLinkProbabilityTextField.setAlignment(Pos.CENTER);
        this.addNodeProbabilityTextField.setAlignment(Pos.CENTER);
        this.mutateBiasProbabilityTextField.setAlignment(Pos.CENTER);
        this.newActivationFunctionProbabilityTextField.setAlignment(Pos.CENTER);
        this.toggleLinkProbabilityTextField.setAlignment(Pos.CENTER);
        this.weightReplaceProbabilityTextField.setAlignment(Pos.CENTER);

        tempVbox.getChildren().addAll(
                hBox,
            this.mutationProbabilityTextField,
            this.crossoverProbabilityTextField,
            this.addLinkProbabilityTextField,
            this.addNodeProbabilityTextField,
            this.mutateBiasProbabilityTextField,
            this.newActivationFunctionProbabilityTextField,
            this.toggleLinkProbabilityTextField,
            this.weightReplaceProbabilityTextField
        );
        GASettingsTitledPane.setContent(tempVbox);

        JFXToggleButton tempTButton;
        this.activationFunctionsChooser = new TitledPane();
        this.inputActivationFunctionsChooser = new TitledPane();
        tempVbox=new VBox();
        tempTButton = new JFXToggleButton();
        tempTButton.setText(SigmoidFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(TanhFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(LinearFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        this.inputActivationFunctionsChooser.setContent(tempVbox);

        this.hiddenActivationFunctionsChooser = new TitledPane();
        tempVbox=new VBox();
        tempTButton = new JFXToggleButton();
        tempTButton.setText(SigmoidFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(TanhFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(LinearFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        this.hiddenActivationFunctionsChooser.setContent(tempVbox);

        this.outputActivationFunctionsChooser = new TitledPane();
        tempVbox=new VBox();
        tempTButton = new JFXToggleButton();
        tempTButton.setText(SigmoidFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(TanhFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        tempTButton = new JFXToggleButton();
        tempTButton.setText(LinearFunction.getStaticFunctionName());
        tempVbox.getChildren().add(tempTButton);
        this.outputActivationFunctionsChooser.setContent(tempVbox);

        this.activationFunctionAccordion = new Accordion(this.inputActivationFunctionsChooser,
                this.hiddenActivationFunctionsChooser,
                this.outputActivationFunctionsChooser);
        tempVbox = new VBox(new Label("Разрешить использовать:"), this.activationFunctionAccordion);
        this.activationFunctionsChooser.setContent(tempVbox);









                tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.neatSpecificTitledPane = new TitledPane();


        this.excessCoefficientTextField = new JFXTextField();
        this.disjointCoefficientTextField = new JFXTextField();
        this.weightCoefficientTextField = new JFXTextField();
        /*this.excessCoefficientTextField.getStyleClass().add("text-field-with-tooltip");
        this.disjointCoefficientTextField.getStyleClass().add("text-field-with-tooltip");
        this.weightCoefficientTextField.getStyleClass().add("text-field-with-tooltip");*/
        /*this.excessCoefficientTextField.promptTextProperty().sty*/

        this.generatorSeedTextField.setLabelFloat(true);
        this.excessCoefficientTextField.setLabelFloat(true);
        this.disjointCoefficientTextField.setLabelFloat(true);
        this.weightCoefficientTextField.setLabelFloat(true);
        Tooltip tempTooltip = new Tooltip();
        JFXUtils.TooltipConfigurator.setDelay(tempTooltip, 100);
        this.excessCoefficientTextField.setTooltip(tempTooltip);
        this.disjointCoefficientTextField.setTooltip(tempTooltip);
        this.weightCoefficientTextField.setTooltip(tempTooltip);
        tempVbox.getChildren().addAll(
                this.excessCoefficientTextField,
                this.disjointCoefficientTextField,
                this.weightCoefficientTextField
        );
        this.neatSpecificTitledPane.setContent(tempVbox);
        this.neatSpecificTitledPane.setTooltip(new Tooltip(""));

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
        this.maxWeightPertrubTextField = new JFXTextField();
        this.maxBiasPertrubTextField = new JFXTextField();
        this.featureSelectionToggle = new JFXToggleButton();
        this.reccurencyAllowedToggle = new JFXToggleButton();
        this.inputNodesTextField.setLabelFloat(true);
        this.outputNodesTextField.setLabelFloat(true);
        this.maxWeightPertrubTextField.setLabelFloat(true);
        this.maxBiasPertrubTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.inputNodesTextField,
                this.outputNodesTextField,
                this.maxWeightPertrubTextField,
                this.maxBiasPertrubTextField,
                this.featureSelectionToggle,
                this.reccurencyAllowedToggle
        );
        this.networkControlTitledPane.setContent(tempVbox);


        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.extinctionControlTitledPane = new TitledPane();
        this.eleEventsToggle = new JFXToggleButton();
        this.eleSurvivalCountTextField = new JFXTextField();
        this.eleEventTimeTextField = new JFXTextField();
        this.eleSurvivalCountTextField.setLabelFloat(true);
        this.eleEventTimeTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.eleEventsToggle,
                this.eleSurvivalCountTextField,
                this.eleEventTimeTextField
        );
        this.extinctionControlTitledPane.setContent(tempVbox);

        this.eleEventsToggle.selectedProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue){
                eleEventTimeTextField.setVisible(true);
                eleSurvivalCountTextField.setVisible(true);
            } else {
                eleEventTimeTextField.setVisible(false);
                eleSurvivalCountTextField.setVisible(false);
            }
        });
        this.eleEventsToggle.selectedProperty().setValue(false);
        eleEventTimeTextField.setVisible(false);
        eleSurvivalCountTextField.setVisible(false);




        tempVbox = new VBox();
        tempVbox.setSpacing(25);
        this.epochControlTitledPane = new TitledPane();
        this.keepBestEverToggle = new JFXToggleButton();
        this.extraFeatureCountTextField = new JFXTextField();
        this.popSizeTextField = new JFXTextField();
        this.numberEpochsTextField = new JFXTextField();
        this.terminationValueToggle = new JFXToggleButton();
        this.terminationValueToggle.setTooltip(new Tooltip());
        this.terminationValueTextField = new JFXTextField();
        this.terminationValueTextField.setTooltip(new Tooltip());
        this.extraFeatureCountTextField.setLabelFloat(true);
        this.popSizeTextField.setLabelFloat(true);
        this.numberEpochsTextField.setLabelFloat(true);
        this.terminationValueTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.keepBestEverToggle,
                this.extraFeatureCountTextField,
                this.popSizeTextField,
                this.numberEpochsTextField,
                this.terminationValueToggle,
                this.terminationValueTextField
        );
        this.epochControlTitledPane.setContent(tempVbox);
        this.terminationValueToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                this.terminationValueTextField.setVisible(true);
            } else {
                this.terminationValueTextField.setVisible(false);
            }
        });
        this.terminationValueToggle.setSelected(false);
        this.terminationValueTextField.setVisible(false);

        this.titlesPaneContainer.getChildren().addAll(
                this.GASettingsTitledPane,
                this.neatSpecificTitledPane,
                this.speciationControlTitledPane,
                this.networkControlTitledPane,
                this.activationFunctionsChooser,
                this.extinctionControlTitledPane,
                this.epochControlTitledPane
        );
        parametresScrollPane.setContent(this.titlesPaneContainer);

        this.neatMenuBorderPane.setCenter(noActiveProjectLabel);
        this.neatMenuBorderPane.setMinWidth(MIN_WIDTH_NEAT_MENU);
        this.neatMenuBorderPane.setPrefWidth(MIN_WIDTH_NEAT_MENU);
        this.neatMenuBorderPane.setMaxWidth(MIN_WIDTH_NEAT_MENU);

        saveNeatButton = new JFXButton();
        saveNeatButton.setOnAction(event -> {
            saveConfig();
        });

        saveNeatButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.SAVE));
        neatNameLabel = new Label("");
        neatMenuHeader = new HBox(neatNameLabel, saveNeatButton);
        neatMenuHeader.setAlignment(Pos.CENTER);
        neatMenuHeader.setSpacing(10);
        neatMenuBorderPaneContainer = new VBox( this.neatMenuHeader, this.parametresScrollPane);
        neatMenuBorderPaneContainer.setAlignment(Pos.CENTER);
        neatMenuBorderPaneContainer.setSpacing(10);


        this.neatSplitPane.getDividers().get(0).positionProperty().setValue(1);

        openNEATMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED_OF_SLIDE_MENU), event -> menuSlide(openNEATMenu,neatSplitPane, neatMenuBorderPane,1, -1))
        );
        openNEATMenu.setCycleCount(Timeline.INDEFINITE);

        closeNEATMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED_OF_SLIDE_MENU), event -> menuSlide(closeNEATMenu, neatSplitPane, neatMenuBorderPane,-1, -1))
        );
        closeNEATMenu.setCycleCount(Timeline.INDEFINITE);
        neatMenuBorderPane.getCenter().setVisible(false);

        enableSlideMenu(openNEATMenuIcon, pinButton,neatMenuBorderPane, MAX_WIDTH_NEAT_MENU, closeNEATMenu, openNEATMenu);
        neatMenuBorderPane.disableProperty().bind(this.currentNeatConfigFile.isNull());



        setPinButtonAction(pinButton, neatMenuBorderPane, MAX_WIDTH_NEAT_MENU, MIN_WIDTH_NEAT_MENU);

        this.projectBorderPane.setCenter(noActiveProjectLabel);
        this.projectBorderPane.setMinWidth(MIN_WIDTH_PROJECT_MENU);
        this.projectBorderPane.setPrefWidth(MIN_WIDTH_PROJECT_MENU);
        this.projectBorderPane.setMaxWidth(MIN_WIDTH_PROJECT_MENU);

        openProjectMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED_OF_SLIDE_MENU), event -> menuSlide(openProjectMenu,projectSplitPane, projectBorderPane,1, 1))
        );
        openProjectMenu.setCycleCount(Timeline.INDEFINITE);

        closeProjectMenu = new Timeline(
                new KeyFrame(Duration.millis(SPEED_OF_SLIDE_MENU), event -> menuSlide(closeProjectMenu, projectSplitPane, projectBorderPane,-1, 1))
        );
        closeProjectMenu.setCycleCount(Timeline.INDEFINITE);
        projectBorderPane.getCenter().setVisible(false);

        enableSlideMenu(openProjectMenuIcon, pinProjectMenuButton,projectBorderPane, MAX_WIDTH_PROJECT_MENU, closeProjectMenu, openProjectMenu);
        setPinButtonAction(pinProjectMenuButton, projectBorderPane, MAX_WIDTH_PROJECT_MENU, MIN_WIDTH_PROJECT_MENU);

        initContextMenues();

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
        addReqieredFieldValidator(this.maxWeightPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.maxBiasPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleSurvivalCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleEventTimeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.extraFeatureCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.popSizeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.numberEpochsTextField, requiredFieldValidator);*/






        startPredictionButton.disableProperty().bind(this.predictionDataSet.isNull());


        this.inputNodesTextField.setDisable(true);
        this.outputNodesTextField.setDisable(true);



        if (this.currentNEATConfig == null){
            this.parametresScrollPane.setVisible(false);
            this.infoTabPane.setVisible(false);
        }



        this.trainDatasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                try {
                    DataKeeper trainDataSet = DataKeeper.loadDataset(newValue.getAsFile());
                    this.inputNodesTextField.setText(String.valueOf(trainDataSet.getInputs())); //get number of inputs
                    this.outputNodesTextField.setText(String.valueOf(trainDataSet.getOutputs())); // get number of outputs
                    this.trainEndIndex.set(null);
                    this.trainPercentageTextField.setText(null);
                    trainReporter = new TrainReporter(trainDataSet, resourceBundle);

                    this.fillTableViewWithData(this.trainTableView, trainDataSet);


                    trainErrorChart.getData().clear();
                    trainValueGraphicChart.getData().clear();
                    drawablePane.getChildren().clear();
                    saveReport.setDisable(true);
                    trainingCount = 0;
                    this.trainSetSimpleObjectProperty.setValue(trainDataSet);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        /*this.testDatasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                this.testDataSet = loadDataset(newValue.getAsFile());

                this.currentNEATConfig.updateConfig("TEST.INPUTS", this.testDataSet.getInputs());
                this.currentNEATConfig.updateConfig("TEST.OUTPUTS", this.testDataSet.getOutputs());


                this.fillTableViewWithData(this.testTableView, this.testDataSet.getHeadersForTableView(), this.testDataSet.getDataForTableView());
                this.testValueChart.getData().clear();
                if(trainedModelsChoiceBox.getSelectionModel().getSelectedItem() != null) {
                    this.runTestButton.setDisable(false);
                }
            } else {
                if(trainedModelsChoiceBox.getSelectionModel().getSelectedItem() == null)
                    this.runTestButton.setDisable(true);
            }
        });*/
        /*this.trainedModelsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                this.testValueChart.getData().clear();
                if(testDatasetChoiceBox.getSelectionModel().getSelectedItem() != null) {
                    this.runTestButton.setDisable(false);
                }
            } else {
                if(testDatasetChoiceBox.getSelectionModel().getSelectedItem() == null)
                    this.runTestButton.setDisable(true);
            }
        });*/




        //trainigTab.setDisable(true);
        //testingTab.setDisable(true);
        this.trainingProgressBar = new ProgressBar(0);
        this.trainingProgressBar.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY){
                if(!this.trainigTab.isDisable())
                    this.infoTabPane.getSelectionModel().select(this.trainigTab);
            }
        });



        trainigTab.setGraphic(new BorderPane(trainingProgressBar,null,null,null, null));


        trainErrorChartClearButton.setOnAction(event -> {
            this.trainErrorChart.getData().clear();
            this.trainingCount = 0;
        });
        testErrorChartClearButton.setOnAction(event -> {
            this.testErrorChart.getData().clear();
        });


        trainValueGraphicChartClearButton.setOnAction(event -> {
            this.trainValueGraphicChart.getData().clear();
        });

        /*this.testValueChartClearButton.setOnAction(event -> {
            this.testValueChart.getData().clear();
        });*/


        //Panning works via either secondary (right) mouse or primary with ctrl held down
        configureChart(this.trainErrorChart);
        configureChart(this.testErrorChart);
        configureChart(this.trainValueGraphicChart);
        configureChart(this.predictionChart);
        //configureChart(this.testValueChart);
        trainValueGraphicChart.getXAxis().setAutoRanging( false );
        trainValueGraphicChart.getYAxis().setAutoRanging( true );
        /*testValueChart.getXAxis().setAutoRanging( false );
        testValueChart.getYAxis().setAutoRanging( true );*/



        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                neatSplitPane.lookupAll(".split-pane-divider").stream()
                        .forEach(div ->  div.setMouseTransparent(true) );
                projectSplitPane.lookupAll(".split-pane-divider").stream()
                        .forEach(div ->  div.setMouseTransparent(true) );
                /*netVisualisationCanvas.setWidth(netVisualizationBorderPane.getWidth()-2);
                netVisualisationCanvas.setHeight(netVisualizationBorderPane.getHeight());
                netVisualisationCanvas.widthProperty().bind(netVisualizationBorderPane.widthProperty());
                netVisualisationCanvas.heightProperty().bind(netVisualizationBorderPane.heightProperty());*/
            }
        });

        /*trainTitledPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                trainHeaderBorderPane.setMaxWidth(trainTitledPane.getWidth());
                trainHeaderBorderPane.setPrefWidth(trainTitledPane.getWidth()-40);
            }
        });*/
        predictionDatasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                this.loadPredictionData(newValue);
            }
        });



        this.netVisualizationBorderPane.setCenter(ZoomPane.createZoomPane(this.drawablePane));

        /*this.netVisualisationCanvas = new ZoomableCanvas(300, 300) {
            @Override
            public void paint(GraphicsContext gc) {
                netVisualisator.visualiseNet(netVisualisationCanvas);
            }
        };*/
        this.netVisualisator = new AdvancedNetVisualisator();
        //this.netVisualizationBorderPane.setCenter(this.netVisualisationCanvas);

       /* this.netVisualisationCanvas.widthProperty().bind(this.netVisualizationBorderPane.widthProperty());
        this.netVisualisationCanvas.heightProperty().bind(this.netVisualizationBorderPane.heightProperty());*/



       /* JFXUtils.CanvasConfigurator.setNetVisualisator(netVisualisator);
        JFXUtils.CanvasConfigurator.setZoomOnCanvas(this.netVisualisationCanvas);
        JFXUtils.CanvasConfigurator.setDragableCanvas(this.netVisualisationCanvas, this.netVisualizationBorderPane);

*/

    }

    private void configureChart(LineChart chart) {
        ChartPanManager panner = new ChartPanManager( chart );
        panner.setMouseFilter( new EventHandler<MouseEvent>() {
            @Override
            public void handle( MouseEvent mouseEvent ) {
                if ( mouseEvent.getButton() == MouseButton.PRIMARY ||
                        ( mouseEvent.getButton() == MouseButton.SECONDARY &&
                                mouseEvent.isShortcutDown() ) ) {
                    //let it through
                } else {
                    mouseEvent.consume();
                }
            }
        } );
        panner.start();

        //Zooming works only via primary mouse button without ctrl held down
        JFXChartUtil.setupZooming( chart, new EventHandler<MouseEvent>() {
            @Override
            public void handle( MouseEvent mouseEvent ) {
                if ( mouseEvent.getButton() != MouseButton.SECONDARY ||
                        mouseEvent.isShortcutDown() )
                    mouseEvent.consume();
            }
        } );

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler( chart );
    }

    private void setPinButtonAction(ToggleButton pinMenuButton, BorderPane menuBorderPane, double maxWidthMenu, double minWidthMenu) {
        pinMenuButton.setText(null);
        pinMenuButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                pinMenuButton.getGraphic().setRotate(45);
            } else {
                pinMenuButton.getGraphic().setRotate(0);
            }
        });
        /*pinMenuButton.setOnAction(new EventHandler<ActionEvent>() {
                                  boolean isAlwaysOpened = false;
                                  @Override
                                  public void handle(ActionEvent event) {
                                      isAlwaysOpened = !isAlwaysOpened;
                                      if(isAlwaysOpened) {
                                          menuBorderPane.setMaxWidth(maxWidthMenu);
                                          menuBorderPane.setPrefWidth(maxWidthMenu);
                                          pinMenuButton.getGraphic().setRotate(45);
                                          menuBorderPane.setOnMouseEntered(null);
                                          menuBorderPane.setOnMouseExited(null);

                                      } else {
                                          pinMenuButton.getGraphic().setRotate(0);
                                          menuBorderPane.setMaxWidth(minWidthMenu);
                                          menuBorderPane.setPrefWidth(minWidthMenu);
                                          enableSlideMenu(openNEATMenuIcon, menuBorderPane, maxWidthMenu, closeNEATMenu, openNEATMenu);
                                      }
                                  }
                              }
        );*/
    }


    private void enableSlideMenu(MaterialDesignIconView openMenuIcon, ToggleButton pinButton,BorderPane menuBorderPane, double maxWidth, Timeline closeMenu, Timeline openMenu) {
        RotateTransition iconRotateTransition = new RotateTransition(Duration.millis(500), openMenuIcon);
        iconRotateTransition.setFromAngle(0);
        iconRotateTransition.setToAngle(180);
        iconRotateTransition.setAutoReverse(true);

        menuBorderPane.setOnMouseEntered(evt -> {
            if(menuBorderPane.isDisable()) return;
            menuBorderPane.setMinWidth(menuBorderPane.getMinWidth()-0.1);
            menuBorderPane.setMaxWidth(maxWidth);
            menuBorderPane.getCenter().setVisible(true);
            //neatOptionsLabel.setVisible(true);
            iconRotateTransition.setRate(1);
            iconRotateTransition.play();
            closeMenu.stop(); openMenu.play();
        });
        menuBorderPane.setOnMouseExited(evt -> {
            if(menuBorderPane.isDisable()) return;
            if(pinButton.isSelected()) return;
            menuBorderPane.setMinWidth(menuBorderPane.getMinWidth()+0.1);
            menuBorderPane.setMaxWidth(menuBorderPane.getMinWidth()+0.2);
            menuBorderPane.getCenter().setVisible(false);
            iconRotateTransition.setRate(-1);
            iconRotateTransition.play();
            openMenu.stop(); closeMenu.play();
        });
    }





    void fillTableViewWithData(TableView<List<Double>> tableView, DataKeeper data){


        tableView.getColumns().clear();
        tableView.getItems().clear();
        /*Prepare headers of table's columns*/


        List<String> headers = data.getHeadersForTableView();




        for (int i = 0; i < headers.size(); i++){
            TableColumn tableColumn = new TableColumn();
            tableColumn.setText(headers.get(i));
            tableColumn.setPrefWidth(65);
            final int index = i;
            tableColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures<List<Double>, Double>, ObservableValue<Double>>) p ->
            {
                return new SimpleObjectProperty<Double>((p.getValue().get(index)));
            });

            tableView.getColumns().add(tableColumn);
        }

        /*Put read data into tableview*/
         ObservableList<List<Double>> observableList = FXCollections.observableArrayList();
                    observableList.addAll(data.getDataForTableView());
         tableView.setItems(observableList);

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
        this.newActivationFunctionProbabilityTextField.setPromptText(resourceBundle.getString("NEW_ACTIVATION_FUNCTION_PROBABILITY"));
        mutateBiasProbabilityTextField.setPromptText(resourceBundle.getString("MUTATE_BIAS_PROBABILITY"));
        toggleLinkProbabilityTextField.setPromptText(resourceBundle.getString("TOGGLE_LINK_PROBABILITY"));
        weightReplaceProbabilityTextField.setPromptText(resourceBundle.getString("WEIGHT_REPLACED_PROBABILITY"));

        this.neatSpecificTitledPane.setText(resourceBundle.getString("NEAT_SPECIFIC"));
        this.neatSpecificTitledPane.getTooltip().setText(resourceBundle.getString("NICHING_DESCRIPTION"));
        this.generatorSeedTextField.setPromptText(resourceBundle.getString("GENERATOR_SEED"));
        this.excessCoefficientTextField.setPromptText(resourceBundle.getString("EXCESS_COEFFICIENT"));
        this.disjointCoefficientTextField.setPromptText(resourceBundle.getString("DISJOINT_COEFFICIENT"));
        this.weightCoefficientTextField.setPromptText(resourceBundle.getString("WEIGHT_COEFFICIENT"));
        this.excessCoefficientTextField.getTooltip().setText(resourceBundle.getString("NICHING_TOOLTIP"));

        this.speciationControlTitledPane.setText(resourceBundle.getString("SPECIATION_CONTROL"));
        this.thresholdCompabilityTextField.setPromptText(resourceBundle.getString("THRESHOLD_COMPABILITY"));
        this.changeCompabilityTextField.setPromptText(resourceBundle.getString("CHANGE_COMPABILITY"));
        this.specieCountTextField.setPromptText(resourceBundle.getString("SPECIE_COUNT"));
        this.survivalThresholdTextField.setPromptText(resourceBundle.getString("SURVIVAL_THRESHOLD"));
        this.specieAgeThresholdTextField.setPromptText(resourceBundle.getString("SPECIE_AGE_THRESHOLD"));
        this.specieYouthThresholdTextField.setPromptText(resourceBundle.getString("SPECIE_YOUTH_THRESHOLD"));
        this.specieOldPenaltyTextField.setPromptText(resourceBundle.getString("SPECIE_OLD_PENALTY"));
        this.specieYouthBoostTextField.setPromptText(resourceBundle.getString("SPECIE_YOUTH_BOOST"));
        this.specieFitnessMaxTextField.setPromptText(resourceBundle.getString("FITNESS_MAX"));

        this.networkControlTitledPane.setText(resourceBundle.getString("NETWORK_CONTROL"));
        this.inputNodesTextField.setPromptText(resourceBundle.getString("INPUT_NODES"));
        this.outputNodesTextField.setPromptText(resourceBundle.getString("OUTPUT_NODES"));
        this.maxWeightPertrubTextField.setPromptText(resourceBundle.getString("MAX_WEIGHT_PERTURB"));
        this.maxBiasPertrubTextField.setPromptText(resourceBundle.getString("MAX_BIAS_PERTURB"));
        this.featureSelectionToggle.setText(resourceBundle.getString("FEATURE_SELECTION"));
        this.reccurencyAllowedToggle.setText(resourceBundle.getString("RECURRENCY_ALLOWED"));

        this.extinctionControlTitledPane.setText(resourceBundle.getString("EXTINCTION_CONTROL"));
        this.eleEventsToggle.setText(resourceBundle.getString("EXTINCTION_EVENT"));
        this.eleSurvivalCountTextField.setPromptText(resourceBundle.getString("EXTINCTION_SURVIVAL_COUNT"));
        this.eleEventTimeTextField.setPromptText(resourceBundle.getString("EXTINCTION_EVENT_TIME"));

        this.epochControlTitledPane.setText(resourceBundle.getString("EPOCH_CONTROL"));
        this.keepBestEverToggle.setText(resourceBundle.getString("KEEP_BEST_EVER"));
        this.extraFeatureCountTextField.setPromptText(resourceBundle.getString("EXTRA_FEATURE_COUNT"));
        this.popSizeTextField.setPromptText(resourceBundle.getString("POP_SIZE"));
        this.numberEpochsTextField.setPromptText(resourceBundle.getString("NUMBER_EPOCHS"));
        this.terminationValueToggle.setText(resourceBundle.getString("TERMINATION_VALUE_TOGGLE"));
        this.terminationValueToggle.getTooltip().setText(resourceBundle.getString("TERMINATION_VALUE_TOOLTIP"));
        this.terminationValueTextField.setPromptText(resourceBundle.getString("TERMINATION_VALUE"));
        this.terminationValueTextField.getTooltip().setText(resourceBundle.getString("TERMINATION_VALUE_TOOLTIP"));


        this.activationFunctionsChooser.setText(resourceBundle.getString("ACTIVATION_FUNCTION"));
        ((Label)((VBox)this.activationFunctionsChooser.getContent()).getChildren().get(0)).setText((resourceBundle.getString("ALLOWED_TO_USE")));
        this.outputActivationFunctionsChooser.setText(resourceBundle.getString("OUTPUT_ACTIVATION_FUNCTIONS"));
        this.inputActivationFunctionsChooser.setText(resourceBundle.getString("INPUT_ACTIVATION_FUNCTIONS"));
        this.hiddenActivationFunctionsChooser.setText(resourceBundle.getString("HIDDEN_ACTIVATION_FUNCTIONS"));




        AlertWindow.setLanguage(resourceBundle);
        NewProjectDialogue.getInstance(this.scene).setLanguage(resourceBundle);
        NewDatasetDialogue.getInstance(this.scene).setLanguage(resourceBundle);



    }

    private AIConfig loadConfig(String path){
        return new NEATLoader().loadConfig(path);
    }

    @FXML
    private void openProject(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()+"\\projects\\"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project file", "*.prj"));
        fileChooser.setTitle(resourceBundle.getString("CHOOSE_PROJECT"));
        File projectFile = fileChooser.showOpenDialog(this.scene.getWindow());
        if (projectFile != null){
            try {
                readProjectFile(projectFile);

            } catch (FileNotFoundException e) {
                AlertWindow.createAlertWindow("Can't open the file").show();
            } catch (IOException e) {
                AlertWindow.createAlertWindow("Can't read the file").show();
            }
        }

    }

    private void readProjectFile(File projectFile) throws IOException {

        this.currentProjectTextField.setText(projectFile.getAbsolutePath());



        /*ProjectFileDescriptor projectFileDescriptor = null;
        if(stringTokenizer.hasMoreTokens()){
            token = stringTokenizer.nextToken();
            if(token.equals("PROJECT_NAME")){
                token = stringTokenizer.nextToken();
                String[] fileName = token.split("[.]");
                projectFileDescriptor =  new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.PROJECT, projectFile.getParent() ,fileName[0], fileName[1]);

                rootProject = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), projectContextMenu);
            }
            else {
                throw new IOException();
            }
        }
        this.projectTreeView.setRoot(rootProject);
        rootProject.setExpanded(true);

        String lastOpenedProject = "";
        stringTokenizer = new StringTokenizer(reader.readLine(),":");

        if(stringTokenizer.nextToken().equals("LAST_OPENED_DATASET")){
            if(stringTokenizer.hasMoreTokens()){
                lastOpenedProject = stringTokenizer.nextToken();
            }
        } else {
            throw new IOException();
        }

        this.projectBorderPane.setCenter(this.projectTreeView);

        TreeItem<ProjectFileDescriptor> treeItem;
        String line;*/






        /*String pathToCurrentDataset = "";

        while((line=reader.readLine())!=null){
            stringTokenizer = new StringTokenizer(line,":");
            switch (stringTokenizer.nextToken()){
                case "DATASET_NAME":
                    if(stringTokenizer.hasMoreTokens()){
                        token = stringTokenizer.nextToken();
                        String[] fileName = token.split("[.]");
                        pathToCurrentDataset = projectFile.getParent()+"\\datasets\\"+fileName[0]+"\\";
                        projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.NEAT_CONFIG, pathToCurrentDataset ,fileName[0], fileName[1]);
                        treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), NEATContext);
                        rootProject.getChildren().add(treeItem);

                        if(lastOpenedProject.equals(treeItem.getValue().getName()+"."+treeItem.getValue().getExtension())){
                            projectTreeView.getSelectionModel().select(treeItem);
                        }
                    }
                    break;
                case "TRAIN_SET":
                    if(stringTokenizer.hasMoreTokens()){
                        token = stringTokenizer.nextToken();

                        String[] fileName = token.split("[.]");
                        projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_SET, pathToCurrentDataset ,fileName[0], fileName[1]);
                        treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu);
                        rootProject.getChildren().get(rootProject.getChildren().size()-1).getChildren().add(treeItem);
                    }
                    break;
                case "TEST_SET":
                    if(stringTokenizer.hasMoreTokens()){
                        token = stringTokenizer.nextToken();
                        String[] fileName = token.split("[.]");

                        projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TEST_SET, pathToCurrentDataset ,fileName[0], fileName[1]);
                        treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu);
                        rootProject.getChildren().get(rootProject.getChildren().size()-1).getChildren().add(treeItem);
                    }
                    break;
                case "TRAINED_MODEL":
                    if(stringTokenizer.hasMoreTokens()){
                        token = stringTokenizer.nextToken();
                        String[] fileName = token.split("[.]");
                        projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINED_MODEL, pathToCurrentDataset ,fileName[0], fileName[1]);
                        treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu);
                        rootProject.getChildren().get(rootProject.getChildren().size()-1).getChildren().add(treeItem);
                    }
                    break;
            }
        }

        this.currentProjectTextField.setText(projectFile.getAbsolutePath());
        if(projectTreeView.getSelectionModel().getSelectedItem()!=null) {
            openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getValue());
        }*/

        this.updateTreeView(projectFile);
    }



    void initContextMenues(){
        projectContextMenu = new ContextMenu();
        configureContextMenu(projectContextMenu);

        MenuItem newDatasetFolder = new MenuItem("New dataset folder");
        newDatasetFolder.setOnAction(event -> {
            createNewDatasetFolder(event);
        });

        MenuItem deleteNEAT = new MenuItem("Delete");
        deleteNEAT.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete File");
            alert.setHeaderText("Are you sure want to remove this?");
            alert.setContentText(projectTreeView.getSelectionModel().getSelectedItem().getValue().getName()+"."+projectTreeView.getSelectionModel().getSelectedItem().getValue().getExtension());
            // option != null.
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get() != null) {
                if (option.get() == ButtonType.OK) {
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setTitle("Information");
                    if(deleteDatasetFolder(projectTreeView.getSelectionModel().getSelectedItem().getValue())){
                        alert1.setContentText("Dataset was removed successfully!");
                        this.projectTreeView.getRoot().getChildren().remove(this.projectTreeView.getSelectionModel().getSelectedItem());
                        saveProject();
                    } else {
                        alert1.setContentText("Can't remove!");
                    }
                    alert1.showAndWait();
                }
            }

        });


        MenuItem loadNEAT = new MenuItem("Load");
        loadNEAT.setOnAction(event -> {
            //openNEATFile(projectTreeView.getSelectionModel().getSelectedItem());

            Node n = projectTreeView.getSelectionModel().getSelectedItem().getGraphic();
            Event.fireEvent(n ,new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.PRIMARY, 2,
                    false, false, false, false, false,
                    false, false, false, true, true, null));
        });

        projectContextMenu.getItems().addAll(
                newDatasetFolder
        );

        MenuItem newDatasetItem = new MenuItem("Prepare new data");
        newDatasetItem.setOnAction(event -> {
            prepareNewData(event);
        });


        NEATContext = new ContextMenu();
        configureContextMenu(NEATContext);
        NEATContext.getItems().addAll(loadNEAT, new SeparatorMenuItem(), newDatasetItem, new SeparatorMenuItem(), deleteNEAT);



        MenuItem loadDataItem = new MenuItem("Load");
        loadDataItem.setOnAction(event -> {
            if(currentNeatConfigFile == null  || currentNEATConfig == null ){
                //openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent());
                logger.debug("loadDataItem ContextMenu Load : currentNeatConfigFile == null  || currentNEATConfig == null " );
                Node n = projectTreeView.getSelectionModel().getSelectedItem().getGraphic();
                Event.fireEvent(n ,new MouseEvent(MouseEvent.MOUSE_CLICKED,
                        n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.PRIMARY, 2,
                        false, false, false, false, false,
                        false, false, false, true, true, null));

            }
            ProjectFileDescriptor projectFileDescriptor = projectTreeView.getSelectionModel().getSelectedItem().getValue();
            switch (projectFileDescriptor.getType()){
                case TRAINED_MODEL:
                    this.infoTabPane.getSelectionModel().select(predictionTab);
                    if(!trainedModelsChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                        this.trainedModelsChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    }
                    this.trainedModelsChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    break;
                case TRAINING_SET:
                    this.infoTabPane.getSelectionModel().select(trainigTab);
                    if(!trainDatasetChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                        this.trainDatasetChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    }
                    this.trainDatasetChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    break;
            }
        });

        MenuItem deleteDataItem = new MenuItem("Delete");

        deleteDataItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete File");
            alert.setHeaderText("Are you sure want to remove this?");
            alert.setContentText(projectTreeView.getSelectionModel().getSelectedItem().getValue().getName()+"."+projectTreeView.getSelectionModel().getSelectedItem().getValue().getExtension());
            // option != null.
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get() != null) {
                if (option.get() == ButtonType.OK) {
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setTitle("Information");
                    TreeItem<ProjectFileDescriptor> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
                    if(deleteData(selectedItem.getValue())){
                        alert1.setContentText(selectedItem.getValue().getName() + " was removed successfully!");
                        this.projectTreeView.getSelectionModel().getSelectedItem().getParent().getChildren().remove(this.projectTreeView.getSelectionModel().getSelectedItem());
                        saveProject();
                    } else {
                        alert1.setContentText("Can't remove!");
                    }
                    alert1.showAndWait();
                }
            }

        });

        MenuItem viewDataItem = new MenuItem("View");
        viewDataItem.setOnAction(event -> {
            ProjectFileDescriptor projectFileDescriptor = this.projectTreeView.getSelectionModel().getSelectedItem().getValue();
            switch (projectFileDescriptor.type){
                case TRAINING_SET:
                    this.viewDataInNewWindow(projectFileDescriptor);
                    break;
                case TRAINED_MODEL:
                    this.viewNetTopologyInNewWindow(projectTreeView.getSelectionModel().getSelectedItem());
                    break;
            }

        });


        this.dataContextMenu = new ContextMenu();
        configureContextMenu(this.dataContextMenu);
        this.dataContextMenu.getItems().addAll(viewDataItem, loadDataItem, new SeparatorMenuItem(), deleteDataItem);

        this.folderContextMenu = new ContextMenu();
        configureContextMenu(this.folderContextMenu);

        MenuItem create = new MenuItem("New data");
        create.setOnAction(event -> {prepareNewData(event);});

        this.folderContextMenu.getItems().add(create);





    }

    private void viewNetTopologyInNewWindow(TreeItem<ProjectFileDescriptor> treeItem) {
        ViewNetWindow viewNetWindow = ViewNetWindow.getInstance(this.scene, treeItem.getValue().getName());
        viewNetWindow.show();
        this.netVisualisator.setNetToVisualise(treeItem.getValue().getAsFile(), this.loadConfig(treeItem.getParent().getParent().getValue().getFullPath()));
        this.netVisualisator.visualiseNet(viewNetWindow.getPaneToDraw());
    }

    private void configureContextMenu(ContextMenu contextMenu) {

        MenuItem save = new MenuItem("Save");

        save.setOnAction(event -> saveModel(projectTreeView.getSelectionModel().getSelectedItem()));




        contextMenu.setOnShowing(event -> {
            pinProjectMenuButton.setSelected(true);
                    switch (this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getType()){
                        case TRAINED_MODEL:
                            contextMenu.getItems().add(1, save);
                            break;
                    }
            }
        );

        contextMenu.setOnHiding(event -> {
            pinProjectMenuButton.setSelected(false);
            switch (this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getType()){
                case TRAINED_MODEL:
                    contextMenu.getItems().remove(save);
                    break;
            }
        });
    }

    private boolean deleteDatasetFolder(ProjectFileDescriptor value) {
        File file = value.getAsFile().getParentFile();
        Arrays.stream(file.listFiles()).forEach(file1 -> file1.delete());
        return file.delete();
    }

    private boolean deleteData(ProjectFileDescriptor value){
        return value.getAsFile().delete();
    }

    private void openNEATFile(TreeItem<ProjectFileDescriptor> projectFileDescriptor) {


        if(!isNEATConfigSaved && currentNEATConfig != null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Open new NEAT config");
            alert.setHeaderText("Yout last NEAT config was modified and not saved");
            alert.setContentText("Would you like to save it?");


            ButtonType save = new ButtonType("Save");
            ButtonType notSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel");

            alert.getButtonTypes().clear();

            alert.getButtonTypes().addAll(save, notSave, cancel);

            // option != null.
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get() != null) {
                if (option.get() == save) {
                    saveConfig();
                } else if (option.get() == cancel){
                    return;
                }
            }

        }

        File projectFile = projectFileDescriptor.getValue().getAsFile();
        if(projectFile != null){

            this.clearAllInfoElements();
            this.clearGUIConfig();
            this.trainDatasetChoiceBox.getItems().clear();
            this.trainedModelsChoiceBox.getItems().clear();

            this.currentNEATConfig = this.loadConfig(projectFile.getAbsolutePath());
            logger.debug("openNEATFile() : Current NEAT Config = " + this.currentNEATConfig);
            //this.currentProjectTextField.setText(projectFile.getParent());
            //this.neatOptionsLabel.setText(neatOptionGASettings);



            this.parametresScrollPane.setVisible(true);
            neatMenuBorderPaneContainer.setVisible(false);
            this.neatMenuBorderPane.setCenter(neatMenuBorderPaneContainer);
            this.infoTabPane.setVisible(true);


            /*TreeItem<ProjectFileDescriptor> treeItem = this.projectTreeView.getRoot().getChildren().stream().filter(projectFileDescriptorTreeItem -> {
                return projectFileDescriptorTreeItem.getValue() == projectFileDescriptor;
            }).findAny().orElse(null);*/



            this.currentNeatConfigFile.setValue(projectFileDescriptor);
            logger.debug("Current NEAT Config Tree item= " +  this.currentNeatConfigFile.getValue().getValue().getFullPath());
            fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TRAINING_SET, projectFileDescriptor.getChildren().stream()
                    .filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TRAINING_FOLDER).findFirst().orElse(null), trainDatasetChoiceBox);
            fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TRAINING_SET, projectFileDescriptor.getChildren().stream()
                    .filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TRAINING_FOLDER).findFirst().orElse(null), predictionDatasetChoiceBox);
            //fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TEST_SET, projectFileDescriptor.getChildren().stream().filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TESTING_FOLDER).findFirst().orElse(null), testDatasetChoiceBox);
            fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TRAINED_MODEL, projectFileDescriptor.getChildren().stream().filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.MODEL_FOLDER).findFirst().orElse(null), trainedModelsChoiceBox);

            fillFieldsUsingAIConfig(this.currentNEATConfig);

            this.neatNameLabel.setText("Current NEAT Config : " + this.currentNeatConfigFile.getValue().getValue().getName());

            isNEATConfigSaved = true;

        }
    }

    private void fillChoiceBoxWithData(ProjectFileDescriptor.TYPE type, TreeItem<ProjectFileDescriptor> treeItem, ChoiceBox<ProjectFileDescriptor> choiceBox) {
        if (treeItem == null) return;
        choiceBox.getItems().clear();
        treeItem.getChildren().stream().forEach(treeItem1 -> {
            if(treeItem1.getValue().getType() == type) choiceBox.getItems().add(treeItem1.getValue());
        });
        choiceBox.getSelectionModel().selectFirst();
        if(choiceBox.getSelectionModel().getSelectedItem()!=null) {
            logger.debug("fillChoiceBoxWithData() : Current " + type + " : " + choiceBox.getSelectionModel().getSelectedItem().getFullPath());
        }
    }

    private void clearAllInfoElements() {

        trainTableView.getItems().clear();
        trainTableView.getColumns().clear();
        //testTableView.getItems().clear();
        //testTableView.getColumns().clear();
        trainErrorChart.getData().clear();
        testErrorChart.getData().clear();
        trainValueGraphicChart.getData().clear();
    }

    private void clearGUIConfig(){
        mutationProbabilityTextField.setText("");
        crossoverProbabilityTextField.setText("");
        addLinkProbabilityTextField.setText("");
        addNodeProbabilityTextField.setText("");
        mutateBiasProbabilityTextField.setText("");
        newActivationFunctionProbabilityTextField.setText("");
        toggleLinkProbabilityTextField.setText("");
        weightReplaceProbabilityTextField.setText("");
        generatorSeedTextField.setText("");
        excessCoefficientTextField.setText("");
        disjointCoefficientTextField.setText("");
        weightCoefficientTextField.setText("");
        thresholdCompabilityTextField.setText("");
        changeCompabilityTextField.setText("");
        specieCountTextField.setText("");
        survivalThresholdTextField.setText("");
        specieAgeThresholdTextField.setText("");
        specieYouthThresholdTextField.setText("");
        specieOldPenaltyTextField.setText("");
        specieYouthBoostTextField.setText("");
        specieFitnessMaxTextField.setText("");
        maxWeightPertrubTextField.setText("");
        maxBiasPertrubTextField.setText("");
        featureSelectionToggle.setSelected(false);
        reccurencyAllowedToggle.setSelected(false);
        eleEventsToggle.setSelected(false);
        eleSurvivalCountTextField.setText("");
        eleEventTimeTextField.setText("");
        keepBestEverToggle.setSelected(false);
        extraFeatureCountTextField.setText("");
        popSizeTextField.setText("");
        numberEpochsTextField.setText("");
        terminationValueToggle.setSelected(false);
        terminationValueTextField.setText("");


        for(TitledPane titledPane : activationFunctionAccordion.getPanes()){
            VBox vBox = (VBox)titledPane.getContent();
            for (Node toggleButton : vBox.getChildren()){
                JFXToggleButton jfxToggleButton = ((JFXToggleButton) toggleButton);
                jfxToggleButton.setSelected(false);

                }
        }

    }


    @FXML
    private void createNewProject(ActionEvent actionEvent){

        if(projectTreeView.getRoot() != null){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Creating new project");
            alert.setHeaderText("You tried to create new project");
            alert.setContentText("Would you like to save all changes in current project?");
            // option != null.

            ButtonType save = new ButtonType("Save");
            ButtonType notSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel");

            alert.getButtonTypes().clear();

            alert.getButtonTypes().addAll(save, notSave, cancel);

            // option != null.
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get() != null) {
                if (option.get() == save) {
                    saveConfig();
                    saveProject();
                } else if (option.get() == cancel){
                    return;
                }
            }

        }

        NewProjectDialogue dialogue = NewProjectDialogue.getInstance(this.scene);
        dialogue.show();
        if(dialogue.getProjectFile() != null ){
            try {
                readProjectFile(dialogue.getProjectFile());
            } catch (Exception ex){
                AlertWindow.createAlertWindow(ex.getMessage()).show();
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void createNewDatasetFolder(ActionEvent actionEvent){
        NewDatasetDialogue dialogue = NewDatasetDialogue.getInstance(this.scene);
        dialogue.setCurrentProject(this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getAsFile());
        dialogue.show();
        if (dialogue.getNewDatasetFolder() != null){
            /*ProjectFileDescriptor projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.NEAT_CONFIG, dialogue.getNewDatasetFolder().getParent()+"\\", dialogue.getNewDatasetFolder().getName().split("[.]")[0], "neat");
            TreeItem<ProjectFileDescriptor> treeItem = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), NEATContext);
            this.projectTreeView.getRoot().getChildren().add(treeItem);*/
            this.updateTreeView(this.projectTreeView.getRoot().getValue().getAsFile());
        }


    }


    private void saveModel(TreeItem<ProjectFileDescriptor> model){

        SaveDialogue saveDialogue = SaveDialogue.getInstance(this.scene);
        //saveDialogue.setModelToSave(model.getValue().getAsFile());
        saveDialogue.show();

        if(saveDialogue.getNameOfNewFile().length() != 0){
            ProjectFileDescriptor value = model.getValue();
            ProjectFileDescriptor newModel = new ProjectFileDescriptor(value.getType(), value.getDirectoryPath(), saveDialogue.getNameOfNewFile(), value.getExtension());

            TreeItemContextMenu<ProjectFileDescriptor> treeItemContextMenu = new TreeItemContextMenu<>(newModel, newModel.getGraphic(), dataContextMenu);
            model.getParent().getChildren().add(treeItemContextMenu);
        }







    }

    private void saveProject() {
        //TODO refactor it
        /*ProjectFileDescriptor projectFileDescriptor = this.projectTreeView.getRoot().getValue();
        try {

            FileWriter fileWriter = new FileWriter(projectFileDescriptor.getAsFile(), false);

            fileWriter.write("PROJECT_NAME:"+this.projectTreeView.getRoot().getValue().getName()+"."+this.projectTreeView.getRoot().getValue().getExtension()+"\n");
            fileWriter.append("LAST_OPENED_DATASET:");
            if(currentNeatConfigFile != null) {
                fileWriter.append(currentNeatConfigFile.getValue().getName() + "." + currentNeatConfigFile.getValue().getExtension()+"\n");
            } else {
                fileWriter.append("\n");
            }

            for(TreeItem<ProjectFileDescriptor> dataset : this.projectTreeView.getRoot().getChildren()){
                fileWriter.append("DATASET_NAME:"+ dataset.getValue().getName()+"."+dataset.getValue().getExtension()+"\n");
                for(TreeItem<ProjectFileDescriptor> item : dataset.getChildren()){
                    switch (item.getValue().getType()){
                        case TRAINING_SET:
                            fileWriter.append("TRAIN_SET:"+ item.getValue().getName()+"."+item.getValue().getExtension()+"\n");
                            break;
                        case TEST_SET:
                            fileWriter.append("TEST_SET:"+ item.getValue().getName()+"."+item.getValue().getExtension()+"\n");
                            break;
                        case TRAINED_MODEL:
                            if(!item.getValue().getName().contains("_last_best"))
                                fileWriter.append("TRAINED_MODEL:"+ item.getValue().getName()+"."+item.getValue().getExtension()+"\n");
                            break;
                    }
                }
            }
            fileWriter.flush();
            fileWriter.close();



        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }




    @FXML
    private void saveConfig(){
        try {
            initNEATConfigUsingGUI(this.currentNEATConfig);
            currentNEATConfig.saveConfig(this.currentNeatConfigFile.getValue().getValue().getAsFile());
            isNEATConfigSaved = true;
        } catch (IOException e) {
            AlertWindow.createAlertWindow("CANT_SAVE_FILE").show();
            isNEATConfigSaved = false;
        }
    }

    private void initNEATConfigUsingGUI(AIConfig NEATConfig) {
         NEATConfig.updateConfig("PROBABILITY.MUTATION", mutationProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.CROSSOVER", crossoverProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.ADDLINK", addLinkProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.ADDNODE", addNodeProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.MUTATEBIAS", mutateBiasProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.TOGGLELINK", toggleLinkProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.NEWACTIVATIONFUNCTION", newActivationFunctionProbabilityTextField.getText());
         NEATConfig.updateConfig("PROBABILITY.WEIGHT.REPLACED", weightReplaceProbabilityTextField.getText());
         NEATConfig.updateConfig("GENERATOR.SEED", generatorSeedTextField.getText());
         NEATConfig.updateConfig("EXCESS.COEFFICIENT", excessCoefficientTextField.getText());
         NEATConfig.updateConfig("DISJOINT.COEFFICIENT", disjointCoefficientTextField.getText());
         NEATConfig.updateConfig("WEIGHT.COEFFICIENT", weightCoefficientTextField.getText());
         NEATConfig.updateConfig("COMPATABILITY.THRESHOLD", thresholdCompabilityTextField.getText());
         NEATConfig.updateConfig("COMPATABILITY.CHANGE", changeCompabilityTextField.getText());
         NEATConfig.updateConfig("SPECIE.COUNT", specieCountTextField.getText());
         NEATConfig.updateConfig("SURVIVAL.THRESHOLD", survivalThresholdTextField.getText());
         NEATConfig.updateConfig("SPECIE.AGE.THRESHOLD", specieAgeThresholdTextField.getText());
         NEATConfig.updateConfig("SPECIE.YOUTH.THRESHOLD", specieYouthThresholdTextField.getText());
         NEATConfig.updateConfig("SPECIE.OLD.PENALTY", specieOldPenaltyTextField.getText());
         NEATConfig.updateConfig("SPECIE.YOUTH.BOOST", specieYouthBoostTextField.getText());
         NEATConfig.updateConfig("SPECIE.FITNESS.MAX", specieFitnessMaxTextField.getText());
         NEATConfig.updateConfig("INPUT.NODES", inputNodesTextField.getText());
         NEATConfig.updateConfig("OUTPUT.NODES", outputNodesTextField.getText());
         NEATConfig.updateConfig("MAX.PERTURB", maxWeightPertrubTextField.getText());
         NEATConfig.updateConfig("MAX.BIAS.PERTURB", maxBiasPertrubTextField.getText());
         NEATConfig.updateConfig("FEATURE.SELECTION", String.valueOf(featureSelectionToggle.isSelected()));
         NEATConfig.updateConfig("RECURRENCY.ALLOWED", String.valueOf(reccurencyAllowedToggle.isSelected()));
         NEATConfig.updateConfig("ELE.EVENTS", String.valueOf(eleEventsToggle.isSelected()));
         NEATConfig.updateConfig("ELE.SURVIVAL.COUNT", eleSurvivalCountTextField.getText());
         NEATConfig.updateConfig("ELE.EVENT.TIME", eleEventTimeTextField.getText());
         NEATConfig.updateConfig("KEEP.BEST.EVER", String.valueOf(keepBestEverToggle.isSelected()));
         NEATConfig.updateConfig("EXTRA.FEATURE.COUNT", extraFeatureCountTextField.getText());
         NEATConfig.updateConfig("POP.SIZE", popSizeTextField.getText());
         NEATConfig.updateConfig("NUMBER.EPOCHS", numberEpochsTextField.getText());
         NEATConfig.updateConfig("TERMINATION.VALUE.TOGGLE", String.valueOf(terminationValueToggle.isSelected()));
         NEATConfig.updateConfig("TERMINATION.VALUE", terminationValueTextField.getText());


        TitledPane titledPane = null;
        JFXToggleButton jfxToggleButton = null;
        for (int i = 0; i < activationFunctionAccordion.getPanes().size(); i++) {
            titledPane = activationFunctionAccordion.getPanes().get(i);


            VBox vBox = (VBox)titledPane.getContent();
            List<String> functions = null;

            StringBuilder stringBuilder = new StringBuilder();

            for (int j = 0; j < vBox.getChildren().size(); j++) {
                jfxToggleButton = ((JFXToggleButton) vBox.getChildren().get(j));
                if(jfxToggleButton.isSelected()) {
                    stringBuilder.append(ActivationFunctionFinder.getFunctionClassNameByName(jfxToggleButton.getText()));
                    stringBuilder.append(";");
                }
            }
            switch (i){
                case 0:
                    NEATConfig.updateConfig("INPUT.ACTIVATIONFUNCTIONS", stringBuilder.toString());
                    break;
                case 1:
                    NEATConfig.updateConfig("HIDDEN.ACTIVATIONFUNCTIONS", stringBuilder.toString());
                    break;
                case 2:
                    NEATConfig.updateConfig("OUTPUT.ACTIVATIONFUNCTIONS", stringBuilder.toString());
                    break;
            }





        }

         //runnableNEATConfig.updateConfig("");
    }

    private void fillFieldsUsingAIConfig(AIConfig NEATConfig){
        mutationProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.MUTATION"));
        crossoverProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.CROSSOVER"));
        addLinkProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.ADDLINK"));
        addNodeProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.ADDNODE"));
        mutateBiasProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.MUTATEBIAS"));
        newActivationFunctionProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.NEWACTIVATIONFUNCTION"));
        toggleLinkProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.TOGGLELINK"));
        weightReplaceProbabilityTextField.setText(NEATConfig.configElement("PROBABILITY.WEIGHT.REPLACED"));
        generatorSeedTextField.setText(NEATConfig.configElement("GENERATOR.SEED"));
        excessCoefficientTextField.setText(NEATConfig.configElement("EXCESS.COEFFICIENT"));
        disjointCoefficientTextField.setText(NEATConfig.configElement("DISJOINT.COEFFICIENT"));
        weightCoefficientTextField.setText(NEATConfig.configElement("WEIGHT.COEFFICIENT"));
        thresholdCompabilityTextField.setText(NEATConfig.configElement("COMPATABILITY.THRESHOLD"));
        changeCompabilityTextField.setText(NEATConfig.configElement("COMPATABILITY.CHANGE"));
        specieCountTextField.setText(NEATConfig.configElement("SPECIE.COUNT"));
        survivalThresholdTextField.setText(NEATConfig.configElement("SURVIVAL.THRESHOLD"));
        specieAgeThresholdTextField.setText(NEATConfig.configElement("SPECIE.AGE.THRESHOLD"));
        specieYouthThresholdTextField.setText(NEATConfig.configElement("SPECIE.YOUTH.THRESHOLD"));
        specieOldPenaltyTextField.setText(NEATConfig.configElement("SPECIE.OLD.PENALTY"));
        specieYouthBoostTextField.setText(NEATConfig.configElement("SPECIE.YOUTH.BOOST"));
        specieFitnessMaxTextField.setText(NEATConfig.configElement("SPECIE.FITNESS.MAX"));
        maxWeightPertrubTextField.setText(NEATConfig.configElement("MAX.PERTURB"));
        maxBiasPertrubTextField.setText(NEATConfig.configElement("MAX.BIAS.PERTURB"));
        featureSelectionToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("FEATURE.SELECTION")));
        reccurencyAllowedToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("RECURRENCY.ALLOWED")));
        eleEventsToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("ELE.EVENTS")));
        eleSurvivalCountTextField.setText(NEATConfig.configElement("ELE.SURVIVAL.COUNT"));
        eleEventTimeTextField.setText(NEATConfig.configElement("ELE.EVENT.TIME"));
        keepBestEverToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("KEEP.BEST.EVER")));
        extraFeatureCountTextField.setText(NEATConfig.configElement("EXTRA.FEATURE.COUNT"));
        popSizeTextField.setText(NEATConfig.configElement("POP.SIZE"));
        numberEpochsTextField.setText(NEATConfig.configElement("NUMBER.EPOCHS"));
        terminationValueToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("TERMINATION.VALUE.TOGGLE")));
        terminationValueTextField.setText(NEATConfig.configElement("TERMINATION.VALUE"));

        int i = 0;
        for(TitledPane titledPane : activationFunctionAccordion.getPanes()){
            VBox vBox = (VBox)titledPane.getContent();
            List<String> functions = null;
            switch (i){
                case 0:
                    functions = ((NEATConfig) NEATConfig).getActivationFunctionsByElementKey("INPUT.ACTIVATIONFUNCTIONS");
                    break;
                case 1:
                    functions = ((NEATConfig) NEATConfig).getActivationFunctionsByElementKey("HIDDEN.ACTIVATIONFUNCTIONS");
                    break;
                case 2:
                    functions = ((NEATConfig) NEATConfig).getActivationFunctionsByElementKey("OUTPUT.ACTIVATIONFUNCTIONS");
                    break;
            }

            for (Node toggleButton : vBox.getChildren()){
                JFXToggleButton jfxToggleButton = ((JFXToggleButton) toggleButton);
                for (String classFunction : functions){
                    try {
                        if(jfxToggleButton.getText().equalsIgnoreCase(((ActivationFunction)Class.forName(classFunction).newInstance()).getFunctionName())){
                         jfxToggleButton.setSelected(true);
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            i++;

        }


    }

    private void updateTreeView(File projectFile){



        TreeItem<ProjectFileDescriptor> treeItem;
        String[] fileName = projectFile.getName().split("[.]");
        ProjectFileDescriptor projectFileDescriptor =  new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.PROJECT, projectFile.getParent() ,fileName[0], fileName[1]);
        treeItem = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), projectContextMenu);
        this.projectTreeView.setRoot(treeItem);
        treeItem.setExpanded(true);

        this.projectBorderPane.setCenter(this.projectTreeView);
        /*BufferedReader reader = new BufferedReader(new FileReader(projectFile));
        StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(),":");
        String token;

        TreeItem<ProjectFileDescriptor> lastSelected = null;

        TreeItem<ProjectFileDescriptor> rootProject = new TreeItemContextMenu<ProjectFileDescriptor>();*/

        File datasetsFolder = new File(projectFile.getParent()+"\\datasets\\");
        File[] datasets = datasetsFolder.listFiles();
        if(datasets == null) return;
        File[] files = null;


        TreeItem<ProjectFileDescriptor> trainTreeItem;
        TreeItem<ProjectFileDescriptor> trainenModelTreeItem;
        StringBuilder fileNameBuilder;
        for (int i = 0; i < datasets.length; i++) {
            files = datasets[i].listFiles();
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_FOLDER, null ,"Training data", "");
            trainTreeItem = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), folderContextMenu);
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.MODEL_FOLDER, null ,"Models", "");
            trainenModelTreeItem = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic());

            for (int j = 0; j < files.length; j++) {
                if (files[j].isDirectory()) continue;
                fileNameBuilder = new StringBuilder();

                fileName = files[j].getName().split("[.]");
                for (int k = 0; k < fileName.length-1; k++) {
                    fileNameBuilder.append(fileName[k]);
                }










                try {
                    switch (fileName[fileName.length-1]) {
                        case "neat":
                            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.NEAT_CONFIG, files[j].getParent(), fileNameBuilder.toString(), fileName[1]);
                            treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), NEATContext);
                            treeItem.getChildren().addAll(trainTreeItem, trainenModelTreeItem);
                            this.projectTreeView.getRoot().getChildren().add(treeItem);
                            break;
                        case "trd":
                            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_SET, files[j].getParent(), fileNameBuilder.toString(), fileName[1]);
                            treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu);
                            trainTreeItem.getChildren().add(treeItem);
                            break;
                        case "ser":
                            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINED_MODEL, files[j].getParent(), fileNameBuilder.toString(), fileName[1]);
                            treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu);
                            trainenModelTreeItem.getChildren().add(treeItem);
                            break;
                    }
                } catch (ArrayIndexOutOfBoundsException ex){
                    ex.printStackTrace();
                    continue;
                }

            }

        }

    }


    @FXML
    private void prepareNewData(ActionEvent actionEvent) {
        //DataPreparatorDialogue.getInstance(this.scene).setCurrentDatasetFolder(this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getDirectoryPath()).show();
        //this.updateTreeView(new File(this.currentProjectTextField.getText()));

        TreeItem<ProjectFileDescriptor> parentProject = null;
        switch (this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getType()){
            case NEAT_CONFIG:
                parentProject = this.projectTreeView.getSelectionModel().getSelectedItem();
                break;
            case TRAINING_FOLDER:
                parentProject = this.projectTreeView.getSelectionModel().getSelectedItem().getParent();
                break;
            default:
                parentProject = this.currentNeatConfigFile.getValue();
                break;
        }
        NewDataPreparatorDialogue instance = NewDataPreparatorDialogue.getInstance(this.scene);
        instance.setCurrentDatasetFolder(parentProject.getValue().getDirectoryPath()).show();

        if(instance.getName()!=null){
            ProjectFileDescriptor projectFileDescriptor;
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_SET, parentProject.getValue().getDirectoryPath(), instance.getName(), "trd");
            //this.projectTreeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            parentProject.getChildren().stream().filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TRAINING_FOLDER).findFirst().orElse(null).getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            this.trainDatasetChoiceBox.getItems().add(projectFileDescriptor);
            //this.trainDatasetChoiceBox.getSelectionModel().select(projectFileDescriptor);
        }
        /*String nameOfDataSet = NewDataPreparatorDialogue.getInstance(this.scene);
        ProjectFileDescriptor projectFileDescriptor;
        if(nameOfDataSet.length() != 0){
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_SET, parentProject.getValue().getDirectoryPath(), nameOfDataSet, "trd");
            //this.projectTreeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            parentProject.getChildren().stream().filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TRAINING_FOLDER).findFirst().orElse(null).getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            this.trainDatasetChoiceBox.getItems().add(projectFileDescriptor);
            //this.trainDatasetChoiceBox.getSelectionModel().select(projectFileDescriptor);
        }
        nameOfDataSet = DataPreparatorDialogue.getInstance(this.scene).getNameOfTestSet();
        if(nameOfDataSet.length() != 0){
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TEST_SET,parentProject.getValue().getDirectoryPath(), nameOfDataSet, "ted");
            parentProject.getChildren().stream().filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.TESTING_FOLDER).findFirst().orElse(null).getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            //this.projectTreeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            this.testDatasetChoiceBox.getItems().add(projectFileDescriptor);
            //this.testDatasetChoiceBox.getSelectionModel().select(projectFileDescriptor);
        }*/
    }

    public void generateNewSeed() {
        generatorSeedTextField.setText(String.valueOf(System.currentTimeMillis()));
    }


    public void trainModel(ActionEvent actionEvent) {
        //trainigTab.setContent(new SideBar(30, new TextField()));
        if(trainThread != null) {
            if (trainThread.isAlive()) {
                trainThread.interrupt();
                logger.debug("trainModel() : Train thread was interrupted");
            }
        }

        DataKeeper trainDataSet = this.trainSetSimpleObjectProperty.getValue();
        trainDataSet.setTrainIndexEnd(this.trainEndIndex.getValue());

        AIConfig config = new NEATConfig((NEATConfig) this.currentNEATConfig);
        this.initNEATConfigUsingGUI(config);

        this.trainingProgressBar.progressProperty().unbind();
        this.trainingProgressBar.progressProperty().setValue(0);

        logger.debug("trainModel() : Work with NEAT Config = " + config);

        this.saveReport.setDisable(true);

        try {

            //config.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+trainDataSetChoiceBox.getValue()+"\\"+trainDataSetChoiceBox.getValue()+"@test_temp.dataset");
            NEATTrainingForJavaFX neatTrainingForJavaFX = new NEATTrainingForJavaFX();
            neatTrainingForJavaFX.initialise(config, trainDataSet, this.currentNeatConfigFile.getValue().getValue().getDirectoryPath()+"\\"+trainDatasetChoiceBox.getValue().getName()+"_last_best.ser");
           /* this.currentEpochTextField.textProperty().unbind();
            this.currentEpochTextField.textProperty().bind(neatTrainingForJavaFX.getCurrentEpochProperty());
            this.lastErrorTextField.textProperty().unbind();
            this.lastErrorTextField.textProperty().bind(neatTrainingForJavaFX.lastErrorProperty());
            this.testErrorTextField.textProperty().unbind();
            this.testErrorTextField.textProperty().bind(neatTrainingForJavaFX.lastValidationErrorProperty());
            this.testErrorTextField.visibleProperty().unbind();
            this.testErrorTextField.visibleProperty().bind(neatTrainingForJavaFX.lastValidationErrorProperty().isNotEqualTo("null"));*/

            testErrorTextField.visibleProperty().bind(testErrorTextField.textProperty().isNotEqualTo("null"));

            logger.debug("trainModel() : name of current dataset " + this.trainDatasetChoiceBox.getValue().getName());

            XYChart.Data<Number, Number> dataChart;

            if(this.trainValueGraphicChart.getData().isEmpty()){
                //double tick = trainDataSet.getLegend().stream().mapToDouble(value -> {return value;}).sum() / trainDataSet.getLegend().size();
                double tick = (trainDataSet.getLegend().get(trainDataSet.getLegend().size()-1) - trainDataSet.getLegend().get(0)) / (trainDataSet.getLegend().size()-1);
                ((NumberAxis) trainValueGraphicChart.getXAxis()).setTickUnit(tick);
                //((NumberAxis)trainValueGraphicChart.getXAxis()).setTickUnit(trainDataSet.legend.get(1)-trainDataSet.legend.get(0));
                ((NumberAxis) trainValueGraphicChart.getXAxis()).setLowerBound(trainDataSet.getLegend().get(0)-((NumberAxis) trainValueGraphicChart.getXAxis()).getTickUnit());
                ((NumberAxis) trainValueGraphicChart.getXAxis()).setUpperBound(trainDataSet.getLegend().get(trainDataSet.getLegend().size()-1)+((NumberAxis) trainValueGraphicChart.getXAxis()).getTickUnit());
                trainValueGraphicChart.getXAxis().setLabel(trainDataSet.getLegendHeader());
                XYChart.Series expectedOutputDataXYChart = null;
                for (int i = trainDataSet.getInputs(); i < trainDataSet.getInputs()+trainDataSet.getOutputs(); i++) {


                    expectedOutputDataXYChart = new XYChart.Series();
                    this.trainValueGraphicChart.getData().add(expectedOutputDataXYChart);
                    expectedOutputDataXYChart.setName("Факт. " + trainDataSet.getHeaders().get(i));
                    for (int j = 0; j < trainDataSet.getData().size(); j++) {
                        dataChart = new XYChart.Data<>(trainDataSet.getLegend().get(j), trainDataSet.getData().get(j).get(i));
                        dataChart.setNode(new StackPane());
                        expectedOutputDataXYChart.getData().add(dataChart);
                        Tooltip.install(dataChart.getNode(), new Tooltip(String.valueOf(dataChart.getYValue())));
                    }
                }
            }







            XYChart.Series trainErrorSeries = new XYChart.Series();
            trainErrorSeries.setName("Fitness of the " + ++this.trainingCount + " run");
            this.trainErrorChart.getData().add(trainErrorSeries);

            XYChart.Series testErrorSeries = new XYChart.Series();
            testErrorSeries.setName("Loss of the " + this.trainingCount + " run");
            this.testErrorChart.getData().add(testErrorSeries);

            List<XYChart.Series<Number, Number>> outputValues = new ArrayList<>(trainDataSet.getOutputs());
            List<List<Tooltip>> seriesTooltip = new ArrayList<>(trainDataSet.getOutputs());

            for (int i = 0; i < trainDataSet.getOutputs(); i++) {
                outputValues.add(new XYChart.Series<>());
                seriesTooltip.add(new ArrayList<>(trainDataSet.getData().size()));
                this.trainValueGraphicChart.getData().add(outputValues.get(i));
                for (int j = 0; j < trainDataSet.getData().size(); j++) {

                    dataChart = new XYChart.Data<>(trainDataSet.getLegend().get(j), 0);
                    dataChart.setNode(new StackPane());
                    seriesTooltip.get(i).add(new Tooltip(String.valueOf(0)));
                    Tooltip.install(dataChart.getNode(), seriesTooltip.get(i).get(j));
                    outputValues.get(i).getData().add(dataChart);
                }
                outputValues.get(i).setName(this.trainingCount + ". " + trainDataSet.getHeaders().get(trainDataSet.getInputs()+i));
            }



            /*XYChart.Series outputValuesSeries = new XYChart.Series();
            outputValuesSeries.setName(this.trainingCount + ". " + this.trainTableView.getColumns().get(this.trainTableView.getColumns().size()-1).getText());
            this.trainValueGraphicChart.getData().add(outputValuesSeries);*/



            /*Platform.runLater(() -> {
                *//**//*trainErrorSeries.getNode().lookup(".chart-series-line"). setStyle("-fx-stroke: "+colour[0]+";");
                Node[] nodes = trainErrorChart.lookupAll(".chart-line-symbol").toArray(new Node[0]);
                nodes[nodes.length-1].setStyle("-fx-background-color: "+ colour[0] +", white;");
                for (int i = 0; i <outputValuesSeries.length ; i++) {
                    outputValuesSeries[i].getNode().lookup(".chart-series-line"). setStyle("-fx-stroke: "+colour[i]+";");
                    nodes = trainValueGraphicChart.lookupAll(".chart-line-symbol").toArray(new Node[0]);
                    nodes[nodes.length-1].setStyle("-fx-background-color: "+ colour[i] +", white;");
                }*//**//*

            });*/

            neatTrainingForJavaFX.getBestChromosomeProperty().addListener(new ChangeListener<Chromosome>() {
                @Override
                public void changed(ObservableValue<? extends Chromosome> observable, Chromosome oldValue, Chromosome newValue) {
                    if(newValue == null){
                        neatTrainingForJavaFX.getBestChromosomeProperty().removeListener(this);
                    } else {
                        Chromosome chromosome = newValue;
                        int currentEpoch = neatTrainingForJavaFX.getCurrentEpoch();
                        Platform.runLater(() -> {

                            trainErrorSeries.getData().add(createXYData(currentEpoch, chromosome.getTrainError() ,trainErrorSeries.getChart().getXAxis().getLabel(), trainErrorSeries.getChart().getYAxis().getLabel()));
                            if (chromosome.getValidationError() != null) {
                                testErrorSeries.getData().add(createXYData(currentEpoch, chromosome.getValidationError(),testErrorSeries.getChart().getXAxis().getLabel(), testErrorSeries.getChart().getYAxis().getLabel()));
                            }

                            for (int i = 0; i < chromosome.getOutputs(); i++) {

                                for (int j = 0; j < trainDataSet.getData().size(); j++) {


                                    outputValues.get(i).getData().get(j).setYValue(chromosome.getOutputValues().get(j).get(i));
                                    seriesTooltip.get(i).get(j).setText(outputValues.get(i).getName()+"\n"+outputValues.get(i).getChart().getXAxis().getLabel() + ": " + trainDataSet.getLegend().get(j) +"\n" + outputValues.get(i).getChart().getYAxis().getLabel() +": " + chromosome.getOutputValues().get(j).get(i));

                                    //Tooltip.install(outputValues.get(i).getNode(), new Tooltip(String.valueOf(chromosome.getOutputValues().get(j).get(i))));
                            /*dataChart = new XYChart.Data<>(trainDataSet.getLegend().get(i), 0);
                            dataChart.setNode(new StackPane());
                            Tooltip.install(dataChart.getNode(), new Tooltip(String.valueOf(0)));
                            outputValues.get(i).getData().add(dataChart);*/
                                }
                            }

                            lastErrorTextField.setText(String.valueOf(chromosome.getTrainError()));
                            testErrorTextField.setText(String.valueOf(chromosome.getValidationError()));
                            currentEpochTextField.setText(String.valueOf(currentEpoch));



                            /*if (neatTrainingForJavaFX.isIsEnded())
                                neatTrainingForJavaFX.getBestEverChromosomesList().removeListener(this);*/



                    /*outputValuesSeries.getData().clear();


                    List<List<Double>> outputs = bestChromo.getOutputValues();
                    AtomicInteger counter = new AtomicInteger();
                    for(List<Double> output : outputs) {
                        output.stream().forEach(value -> {
                            Double fromLegend = trainDataSet.getLegend().get(counter.getAndIncrement());
                            XYChart.Data<Number, Number> data = new XYChart.Data<>(fromLegend, value);
                            data.setNode(new StackPane());
                            Tooltip.install(data.getNode(), new Tooltip(String.valueOf(value)));
                            outputValuesSeries.getData().add(data);
                        });
                    }*/

                        });
                    }
                }
                                                                          }
            );




            /*neatTrainingForJavaFX.statusProperty().addListener(observable -> {
                Platform.runLater(() -> {
                    int n = neatTrainingForJavaFX.getBestEverChromosomes().size();
                        Chromosome bestChromo = neatTrainingForJavaFX.getBestEverChromosomes().get(n - 1);
                        try {
                            netVisualisator.setNetToVisualise(bestChromo, config);
                            netVisualisator.visualiseNet(this.drawablePane);
                        } catch (InitialisationFailedException e) {
                            e.printStackTrace();
                        }
                });
            });*/
            this.trainingProgressBar.progressProperty().bind(neatTrainingForJavaFX.statusProperty());


            final TreeItem<ProjectFileDescriptor> currentTreeItem = this.currentNeatConfigFile.getValue();
            final String currentDataSetName = this.trainDatasetChoiceBox.getValue().getName();

            neatTrainingForJavaFX.isEndedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        Platform.runLater(() -> {
                            try {
                                netVisualisator.setNetToVisualise(neatTrainingForJavaFX.getBestEverChromosomes().get(neatTrainingForJavaFX.getBestEverChromosomes().size() - 1), config);
                                netVisualisator.visualiseNet(drawablePane);

                            } catch (InitialisationFailedException e) {
                                e.printStackTrace();
                            }
                            trainingProgressBar.progressProperty().unbind();
                            trainReporter.addConfig(config);

                            trainReporter.addBestChromosomesSet(neatTrainingForJavaFX.getBestEverChromosomes());
                            SnapshotParameters snapshotParameters = new SnapshotParameters();
                            snapshotParameters.setFill(Color.web("#1E1E1E"));
                            trainReporter.addTopologyImage(SwingFXUtils.fromFXImage(drawablePane.snapshot(snapshotParameters, null), null));
                            saveReport.setDisable(false);

                            TreeItem<ProjectFileDescriptor> folder = currentTreeItem.getChildren()
                                    .parallelStream()
                                    .filter(treeItem -> treeItem.getValue().getType() == ProjectFileDescriptor.TYPE.MODEL_FOLDER)
                                    .findFirst()
                                    .orElse(null);

                            ProjectFileDescriptor projectFileDescriptor = folder.getChildren()
                                    .parallelStream().map(treeItem -> treeItem.getValue())
                                    .filter(projectFileDescriptor1 -> projectFileDescriptor1.getType() == ProjectFileDescriptor.TYPE.TRAINED_MODEL
                                            && projectFileDescriptor1.getName().equals(currentDataSetName + "_last_best"))
                                    .findFirst()
                                    .orElse(null);


                            if (projectFileDescriptor != null) {
                                if(!trainedModelsChoiceBox.getItems().contains(projectFileDescriptor)) {
                                    trainedModelsChoiceBox.getItems().add(projectFileDescriptor);
                                }
                                trainedModelsChoiceBox.getSelectionModel().select(projectFileDescriptor);
                            } else {
                                projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINED_MODEL, currentTreeItem.getValue().getDirectoryPath(), currentDataSetName + "_last_best", "ser");
                                folder.getChildren().add(new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), MainController.this.dataContextMenu));
                                trainedModelsChoiceBox.getItems().add(projectFileDescriptor);
                                trainedModelsChoiceBox.getSelectionModel().select(projectFileDescriptor);
                            }
                            neatTrainingForJavaFX.isEndedProperty().removeListener(this);



                        });
                    }
                }
            });

            this.trainThread = new Thread(neatTrainingForJavaFX);
            this.trainThread.start();



        } catch (IOException e) {
            AlertWindow.createAlertWindow("Can't open file!\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
            if(!trainReporter.getBestChromosomes().isEmpty()) saveReport.setDisable(false);
        } catch (InitialisationFailedException e) {
            AlertWindow.createAlertWindow("Initialisation failed!\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
            if(!trainReporter.getBestChromosomes().isEmpty()) saveReport.setDisable(false);
        }


    }

    private XYChart.Data<Number, Number> createXYData(Number xValue, Double yValue, String xLabel, String yLabel) {
        XYChart.Data<Number, Number> xyData = new XYChart.Data<>(xValue, yValue);
        xyData.setNode(new StackPane());

        Tooltip.install(xyData.getNode(), new Tooltip(xLabel +": " + xValue +"\n" + yLabel+": " + yValue));
        return xyData;
    }

    public void testModel(ActionEvent actionEvent) {

        /*if(testThread != null) {
            if (testThread.isAlive()) {
                testThread.interrupt();
                logger.debug("testModel() : Test thread was interrupted");
            }
        }
        this.testingProgressBar.progressProperty().bind(new SimpleObjectProperty<Double>((double) 0));
        logger.debug("testModel() : Current test data " + testDatasetChoiceBox.getSelectionModel().getSelectedItem().getFullPath());
        this.initNEATConfigUsingGUI(currentNEATConfig);
        this.currentNEATConfig.updateConfig("TEST.DATA", tempDirectory.getAbsolutePath()+"\\"+UUID.randomUUID()+"."+testDatasetChoiceBox.getSelectionModel().getSelectedItem().getExtension());
        logger.debug("testModel() : Current temp test data " + currentNEATConfig.configElement("TEST.DATA"));
        try {
            this.saveTempDataSet(this.currentNEATConfig.configElement("TEST.DATA"), this.testDataSet);
            this.currentNEATConfig.updateConfig("AI.SOURCE", this.trainedModelsChoiceBox.getValue().getFullPath());
            logger.debug("testModel() : Current model " + this.currentNEATConfig.configElement("AI.SOURCE"));

            NEATPredictionEngineForJavaFX neatPredictionEngineForJavaFX = new NEATPredictionEngineForJavaFX();
            neatPredictionEngineForJavaFX.initialise(this.currentNEATConfig);

            if(this.testValueChart.getData().isEmpty()){
                //double tick = this.testDataSet.getLegend().stream().mapToDouble(value -> {return value;}).sum() / this.testDataSet.getLegend().size();
                double tick = (this.testDataSet.getLegend().get(this.testDataSet.getLegend().size()-1) - this.testDataSet.getLegend().get(0)) / (this.testDataSet.getLegend().size()-1);
                ((NumberAxis) testValueChart.getXAxis()).setTickUnit(tick);
                //((NumberAxis)testValueGraphicChart.getXAxis()).setTickUnit(this.testDataSet.legend.get(1)-this.testDataSet.legend.get(0));
                ((NumberAxis) testValueChart.getXAxis()).setLowerBound(this.testDataSet.getLegend().get(0)-((NumberAxis) testValueChart.getXAxis()).getTickUnit());
                ((NumberAxis) testValueChart.getXAxis()).setUpperBound(this.testDataSet.getLegend().get(this.testDataSet.getLegend().size()-1)+((NumberAxis) testValueChart.getXAxis()).getTickUnit());
                testValueChart.getXAxis().setLabel(this.testDataSet.getLegendHeader());
                XYChart.Series expectedOutputDataXYChart = null;
                for (int i = 0; i < Integer.parseInt(this.outputNodesTextField.getText()); i++) {

                    TableColumn tableColumn = this.testTableView.getColumns().get(this.testTableView.getColumns().size()-1-i);
                    expectedOutputDataXYChart = new XYChart.Series();
                    this.testValueChart.getData().add(expectedOutputDataXYChart);
                    expectedOutputDataXYChart.setName(tableColumn.getText() + " (Факт)");
                    for (int j = 0; j < this.testTableView.getItems().size(); j++) {
                        if(tableColumn.getCellData(j) != null) {
                            XYChart.Data integerObjectData = new XYChart.Data<>(testDataSet.getLegend().get(j), tableColumn.getCellData(j));
                            integerObjectData.setNode(new StackPane());
                            expectedOutputDataXYChart.getData().add(integerObjectData);
                            Tooltip.install(integerObjectData.getNode(), new Tooltip(String.valueOf(tableColumn.getCellData(j))));
                        }
                    }
                }
            }

            XYChart.Series outputValuesSeries = new XYChart.Series();
            outputValuesSeries.setName(this.testTableView.getColumns().get(this.testTableView.getColumns().size()-1).getText());
            this.testValueChart.getData().add(outputValuesSeries);
            AtomicInteger counter = new AtomicInteger(0);
            neatPredictionEngineForJavaFX.getOutsProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(()->{
                    *//*List<Double> outputs = newValue.get(newValue.size() - 1);*//*
                    if(newValue!=null) {
                        counter.set(0);
                        for (List<Double> output : newValue) {
                            output.stream().forEach(value -> {
                                Double fromLegend = testDataSet.getLegend().get(counter.getAndIncrement());
                                XYChart.Data<Number, Number> data = new XYChart.Data<>(fromLegend, value);
                                data.setNode(new StackPane());
                                Tooltip.install(data.getNode(), new Tooltip(String.valueOf(value)));
                                outputValuesSeries.getData().add(data);
                            });
                        }
                    }
                });
            });

            ChangeListener<Double> neatErrorTestingListener = getListener();

            neatPredictionEngineForJavaFX.getErrorProperty().addListener(neatErrorTestingListener);

            neatPredictionEngineForJavaFX.getIsFinished().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    neatPredictionEngineForJavaFX.getErrorProperty().removeListener(neatErrorTestingListener);
                }
            });

            this.testThread = new Thread(neatPredictionEngineForJavaFX);
            this.testThread.start();
            

        } catch (IOException e) {
            AlertWindow.createAlertWindow("Can't open file!\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            AlertWindow.createAlertWindow("Initialisation failed!\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            AlertWindow.createAlertWindow("Несовместимость модели и тестируемой выборки\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
        }*/


    }




    public void showMenu(ActionEvent actionEvent) {

    }

    private void menuSlide(Timeline timeline, SplitPane splitPane, BorderPane menuBorderPane ,int i, int direction) {

        double pos = splitPane.getDividers().get(0).getPosition();
        double width = menuBorderPane.getWidth();


        if(pos > 1.0) {
            splitPane.setDividerPositions(1.0);
            timeline.stop();

        }
        else if (width == menuBorderPane.getMinWidth()) {

            splitPane.setDividerPositions(pos+direction*0.001f);
            timeline.stop();

        }
        else splitPane.setDividerPositions(pos + direction *  ((double)i)/100);
    }





    public void openTrainedModels(ActionEvent actionEvent) {
    }


    public void viewDataInNewWindow(ProjectFileDescriptor projectFileDescriptor){
        DataKeeper dataKeeper = null;
        try {
            dataKeeper = DataKeeper.loadDataset(projectFileDescriptor.getAsFile());
        } catch (IOException e) {
            e.printStackTrace();
            AlertWindow.createAlertWindow("Невозможно открыть файл : " + projectFileDescriptor.getFullPath());
        }

        ViewDataWindow viewDataWindow = ViewDataWindow.getInstance(this.scene, projectFileDescriptor.getType()+" "+projectFileDescriptor.getName());

        fillTableViewWithData(viewDataWindow.getTableView(), dataKeeper);
        viewDataWindow.show();
    }

    @FXML
    private void confirmTrainPercantage(ActionEvent event){
        try{
            if(!trainTitledPane.isExpanded()) trainTitledPane.setExpanded(true);
            double trainPercentage = Double.parseDouble(this.trainPercentageTextField.getText().replace(",", "."));
            this.trainEndIndex.set((int) Math.round( ((double) this.trainSetSimpleObjectProperty.getValue().getData().size()) / 100 * trainPercentage));
            this.trainSetSimpleObjectProperty.getValue().setTrainIndexEnd(this.trainEndIndex.getValue());
        } catch (NumberFormatException ex){
            ex.printStackTrace();
            AlertWindow.createAlertWindow("Невереый формат числа! Введите число в диапозоне от 0.0 - 100.0").showAndWait();
        }
    }

    public void saveReport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");

        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("DOC files (*.docx)", "*.docx"));//Расширение));//Расширение);

        fileChooser.setInitialDirectory(Paths.get(".").toAbsolutePath().toFile());
        File file = fileChooser.showSaveDialog(this.scene.getWindow());//Указываем текущую сцену
        if(file!=null) {

            if(this.trainReporter.createReport(file)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Сохранение");

                // Header Text: null
                alert.setHeaderText(null);
                alert.setContentText("Сохранение прошло успешно!");

                alert.showAndWait();
            } else {
                AlertWindow.createAlertWindow("Произошла ошибка во время сохранения.\n Закройте файл и повторите попытку.").show();
            }
        }


    }

    private void createInputPredictionTitledPane(String inputLabel){

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(predictionDataSet.getValue().getLegendHeader());
        yAxis.setLabel("Значение");
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);
        this.configureChart(lineChart);
        lineChart.setTitle(inputLabel);

        JFXTextField error = new JFXTextField();
        error.setLabelFloat(true);
        error.setPromptText("Ошибка обучения");


        JFXTextField valError = new JFXTextField();
        valError.setLabelFloat(true);
        valError.setPromptText("Ошибка тестирования");


        VBox vBox = new VBox(new HBox(error, valError), lineChart);
        ((HBox)vBox.getChildren().get(0)).setSpacing(5);

        TitledPane titledPane = new TitledPane(inputLabel, vBox);
        ProgressBar progressBar = new ProgressBar(0);
        JFXButton retrainButton = new JFXButton("Перетренировать");

        titledPane.setGraphic(new BorderPane(progressBar,null, retrainButton, null, null));
        predictionVBox.getChildren().add(titledPane);

        Map<String, Node> map = new HashMap<>();

        map.put("valError" , valError);
        map.put("progressBar", progressBar);
        map.put("retrainButton", retrainButton);
        map.put("lineChart", lineChart);
        map.put("error", error);
        map.put("titledPane", titledPane);
        this.dynamicPredictionNodes.add(map);


    }

    @FXML
    private void openData(ActionEvent event) {
    }


    @FXML
    private void startPrediction(ActionEvent event) {

        AIConfig config = new NEATConfig();
        this.initNEATConfigUsingGUI(config);

        DataKeeper predictionData = this.predictionDataSet.getValue();
        int windowSize = Integer.parseInt(windowSizeTextField.getText());
        int yearPrediction = Integer.parseInt(yearPredictionTextField.getText());
        WindowPrediction windowPrediction = this.windowPrediction.getValue();

        try {
            windowPrediction.initialise(predictionData, windowSize, yearPrediction, config);
            //this.setChartsRange();
            for (int i = 0; i < this.predictionDataSet.getValue().getInputs(); i++) {
                updateTitledPane(i, predictionData, yearPrediction);
            }

            windowPrediction.trainIsFinishedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                    if(newValue){
                        predict(predictionData, windowPrediction);


                        windowPrediction.trainIsFinishedProperty().removeListener(this);
                    }
                }
            });

            Thread thread = new Thread(windowPrediction);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }






    }

    private void predict(DataKeeper predictionData, WindowPrediction windowPrediction){
        AIConfig config = new NEATConfig();
        initNEATConfigUsingGUI(config);

        config.updateConfig("AI.SOURCE", trainedModelsChoiceBox.getValue().getFullPath());


        try {
            windowPrediction.predict(config);

            List<List<Double>> outputData = windowPrediction.getOutputData();

            Double xValue;
            Double yValue;
            Tooltip tooltip;
            Double start = predictionData.getLegend().get(0);
            List<Tooltip> seriesTooltip = new ArrayList<>(predictionData.getData().size() + Integer.parseInt(yearPredictionTextField.getText()));
            XYChart.Series<Number, Number> outputSeries = new XYChart.Series<>();
            predictionInputTime.set(predictionData.getInputs(), predictionInputTime.get(predictionData.getInputs())+1);
            outputSeries.setName(predictionInputTime.get(predictionData.getInputs())+". " + predictionData.getHeaders().get(predictionData.getInputs()));
            Double tick = ((NumberAxis) predictionChart.getXAxis()).getTickUnit();
            for (int j = 0; j < outputData.size(); j++) {
                xValue = start;
                yValue = outputData.get(j).get(0);
                //yValue = newValue.getData().get(j).get(newValue.getInputs());
                outputSeries.getData().add(new XYChart.Data<>(xValue, yValue));
                outputSeries.getData().get(j).setNode(new StackPane());
                tooltip = new Tooltip(predictionChart.getXAxis().getLabel()+": " + xValue + "\n" + predictionChart.getYAxis().getLabel() + ": " + yValue);
                seriesTooltip.add(tooltip);
                Tooltip.install(outputSeries.getData().get(j).getNode(), tooltip);
                start+=tick;
            }
            Platform.runLater(()->{
                predictionErrorTextField.setText(String.valueOf(windowPrediction.getPredictionError()));
                predictionChart.getData().add(outputSeries);
            });




        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void setChartsRange() {

        double year = Double.parseDouble(yearPredictionTextField.getText());
        double tick = (predictionDataSet.getValue().getLegend().get(predictionDataSet.getValue().getLegend().size()-1) - predictionDataSet.getValue().getLegend().get(0)) / (predictionDataSet.getValue().getLegend().size() - 1);
        double xMin = predictionDataSet.getValue().getLegend().get(0) - tick;
        double xMax = predictionDataSet.getValue().getLegend().get(predictionDataSet.getValue().getLegend().size()-1) + year + tick;

        LineChart lineChart = null;
        for (Map<String, Node> map : this.dynamicPredictionNodes) {
            lineChart = (LineChart) map.get("lineChart");
            ((NumberAxis) lineChart.getXAxis()).setAutoRanging(false);
            ((NumberAxis) lineChart.getXAxis()).setTickUnit(tick);
            ((NumberAxis) lineChart.getXAxis()).setLowerBound(xMin);
            ((NumberAxis) lineChart.getXAxis()).setUpperBound(xMax);
        }

        lineChart = this.predictionChart;
        ((NumberAxis) lineChart.getXAxis()).setAutoRanging(false);
        ((NumberAxis) lineChart.getXAxis()).setTickUnit(tick);
        ((NumberAxis) lineChart.getXAxis()).setLowerBound(xMin);
        ((NumberAxis) lineChart.getXAxis()).setUpperBound(xMax);



    }

    private void retrain(int i){
        AIConfig config = new NEATConfig();
        this.initNEATConfigUsingGUI(config);
        WindowPrediction windowPrediction = this.windowPrediction.getValue();
        windowPrediction.retrain(i, config);
        NEATTrainingForJavaFX trainer = windowPrediction.getTrainer(i);
        List<Tooltip> seriesTooltip = new ArrayList<>(windowPrediction.getDataKeeper().getData().size() + Integer.parseInt(yearPredictionTextField.getText()));
        XYChart.Series<Number, Number> outputSeries = new XYChart.Series<>();
        predictionInputTime.set(i, predictionInputTime.get(i)+1);
        outputSeries.setName(predictionInputTime.get(i)+". " + windowPrediction.getDataKeeper().getHeaders().get(i));
        LineChart lineChart = (LineChart) dynamicPredictionNodes.get(i).get("lineChart");
        Double tick = ((NumberAxis) lineChart.getXAxis()).getTickUnit();
        trainer.isEndedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if(newValue) {
                    predict(windowPrediction.getDataKeeper(), windowPrediction);
                    Double xValue;
                    Double yValue;
                    Tooltip tooltip;
                    Double start = trainer.getDataKeeper().getLegend().get(0);

                    for (int j = 0; j < trainer.getDataKeeper().getData().size(); j++) {
                        xValue = start;
                        yValue = trainer.getBestChromosomeProperty().getValue().getOutputValues().get(j).get(0);
                        //yValue = newValue.getData().get(j).get(newValue.getInputs());
                        outputSeries.getData().add(new XYChart.Data<>(xValue, yValue));
                        outputSeries.getData().get(j).setNode(new StackPane());
                        tooltip = new Tooltip(lineChart.getXAxis().getLabel() + ": " + xValue + "\n" + lineChart.getYAxis().getLabel() + ": " + yValue);
                        seriesTooltip.add(tooltip);
                        Tooltip.install(outputSeries.getData().get(j).getNode(), tooltip);
                        start += tick;
                    }

                    Platform.runLater(() -> {
                        lineChart.getData().add(outputSeries);
                    });
                    trainer.isEndedProperty().removeListener(this);
                }
            }
        });






    }



    List<Integer> predictionInputTime;
    private void updateTitledPane(int i, DataKeeper predictionData, int yearPrediction){
        Map<String, Node> map = this.dynamicPredictionNodes.get(i);
        NEATTrainingForJavaFX trainer = this.windowPrediction.getValue().getTrainer(i);
        ProgressBar progressBar = (ProgressBar) map.get("progressBar");
        LineChart<Number,Number> lineChart = (LineChart<Number, Number>) map.get("lineChart");
        JFXTextField error = (JFXTextField) map.get("error");
        JFXTextField valError = (JFXTextField) map.get("valError");
        JFXButton retrainButton = (JFXButton) map.get("retrainButton");

        error.setText("0");
        retrainButton.disableProperty().unbind();
        retrainButton.disableProperty().bind(windowPrediction.getValue().trainIsFinishedProperty().isEqualTo(false));
        retrainButton.setOnAction(event -> {
            retrain(i);



        });


        //error.textProperty().bind(trainer.lastErrorProperty());
        if(lineChart.getData().isEmpty()) {
            List<XYChart.Data<Number, Number>> inputs = new ArrayList<>(predictionData.getData().size());
            for (int j = 0; j < predictionData.getData().size(); j++) {
                inputs.add(createXYData(predictionData.getLegend().get(j), predictionData.getData().get(j).get(i), predictionData.getLegendHeader(), predictionData.getHeaders().get(i)));
            }
            XYChart.Series<Number, Number> series = new XYChart.Series<>("Факт. " + predictionData.getHeaders().get(i), FXCollections.observableArrayList(inputs));
            lineChart.getData().add(series);
            lineChart.getYAxis().setLabel("Значения");
            lineChart.getXAxis().setLabel(predictionData.getLegendHeader());
        }


        progressBar.progressProperty().bind(trainer.statusProperty());



        /*trainer.getDataKeeperProperty().addListener(new ChangeListener<DataKeeper>() {



            @Override
            public void changed(ObservableValue<? extends DataKeeper> observable, DataKeeper oldValue, DataKeeper newValue) {
                Double xValue;
                Double yValue;
                Tooltip tooltip;
                for (int j = 0; j < newValue.getData().size(); j++) {
                    xValue = newValue.getLegend().get(j);
                    yValue = 0.0;
                    //yValue = newValue.getData().get(j).get(newValue.getInputs());
                    outputSeries.getData().add(new XYChart.Data<>(xValue, yValue));
                    outputSeries.getData().get(j).setNode(new StackPane());
                    tooltip = new Tooltip(lineChart.getXAxis().getLabel()+": " + xValue + "\n" + lineChart.getYAxis().getLabel() + ": " + yValue);
                    seriesTooltip.add(tooltip);
                    Tooltip.install(outputSeries.getData().get(j).getNode(), tooltip);
                }
                Platform.runLater(()->{
                    lineChart.getData().add(outputSeries);
                });
                if(trainer.isIsEnded()) trainer.getDataKeeperProperty().removeListener(this);
            }
        });*/

        trainer.getBestChromosomeProperty().addListener(new ChangeListener<Chromosome>() {
            @Override
            public void changed(ObservableValue<? extends Chromosome> observable, Chromosome oldValue, Chromosome newValue) {
                if(newValue != null){
                    String err = String.valueOf(trainer.getLastTrainError());
                    String verr = String.valueOf(trainer.getLastValidationError());
                    Platform.runLater(()->{
                        error.setText(err);
                        valError.setText(verr);
                    });


                    if(trainer.isIsEnded()) trainer.getBestChromosomeProperty().removeListener(this);
                }

            }
        });

        List<Tooltip> seriesTooltip = new ArrayList<>(predictionData.getData().size() + Integer.parseInt(yearPredictionTextField.getText()));
        XYChart.Series<Number, Number> outputSeries = new XYChart.Series<>();
        predictionInputTime.set(i, predictionInputTime.get(i)+1);
        outputSeries.setName(predictionInputTime.get(i)+". " + predictionData.getHeaders().get(i));
        Double tick = ((NumberAxis) lineChart.getXAxis()).getTickUnit();
        trainer.isEndedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {

                    Double xValue;
                    Double yValue;
                    Tooltip tooltip;
                    Double start = trainer.getDataKeeper().getLegend().get(0);

                    for (int j = 0; j < trainer.getDataKeeper().getData().size(); j++) {
                        xValue = start;
                        yValue = trainer.getBestChromosomeProperty().getValue().getOutputValues().get(j).get(0);
                        //yValue = newValue.getData().get(j).get(newValue.getInputs());
                        outputSeries.getData().add(new XYChart.Data<>(xValue, yValue));
                        outputSeries.getData().get(j).setNode(new StackPane());
                        tooltip = new Tooltip(lineChart.getXAxis().getLabel() + ": " + xValue + "\n" + lineChart.getYAxis().getLabel() + ": " + yValue);
                        seriesTooltip.add(tooltip);
                        Tooltip.install(outputSeries.getData().get(j).getNode(), tooltip);
                        start += tick;
                    }
                    Platform.runLater(() -> {
                        lineChart.getData().add(outputSeries);
                    });
                    trainer.isEndedProperty().removeListener(this);
                }
            }
        });

        windowPrediction.getValue().getInputPredictionEndedProperty(i).addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    List<Double> predictedInputs = windowPrediction.getValue().getPredictedInputs(i);

                        Double xValue;
                        Double yValue;
                        Tooltip tooltip;
                        XYChart.Data<Number, Number> xyChart;
                        List<XYChart.Data<Number, Number>> predictedOuts = new ArrayList<>(predictedInputs.size());
                        Double start = (Double) outputSeries.getData().get(outputSeries.getData().size()-1).getXValue()+tick;
                        for (int j = 0; j < predictedInputs.size(); j++) {
                            xValue = start;
                            yValue = predictedInputs.get(j);
                            xyChart = new XYChart.Data<>(xValue, yValue);
                            //yValue = newValue.getData().get(j).get(newValue.getInputs());
                            predictedOuts.add(xyChart);
                            xyChart.setNode(new StackPane());
                            tooltip = new Tooltip(lineChart.getXAxis().getLabel()+": " + xValue + "\n" + lineChart.getYAxis().getLabel() + ": " + yValue);
                            seriesTooltip.add(tooltip);
                            Tooltip.install(xyChart.getNode(), tooltip);
                            start+=tick;
                        }
                    Platform.runLater(()->{
                        outputSeries.getData().addAll(predictedOuts);
                    });


                    windowPrediction.getValue().getInputPredictionEndedProperty(i).removeListener(this);
                }
            }
        });

        /*for (int i = 0; i < trainDataSet.getOutputs(); i++) {
            outputValues.add(new XYChart.Series<>());
            seriesTooltip.add(new ArrayList<>(trainDataSet.getData().size()));
            this.trainValueGraphicChart.getData().add(outputValues.get(i));
            for (int j = 0; j < trainDataSet.getData().size(); j++) {

                dataChart = new XYChart.Data<>(trainDataSet.getLegend().get(j), 0);
                dataChart.setNode(new StackPane());
                seriesTooltip.get(i).add(new Tooltip(String.valueOf(0)));
                Tooltip.install(dataChart.getNode(), seriesTooltip.get(i).get(j));
                outputValues.get(i).getData().add(dataChart);
            }
            outputValues.get(i).setName(this.trainingCount + ". " + trainDataSet.getHeaders().get(trainDataSet.getInputs()+i));
        }*/



        //

        TitledPane titledPane = (TitledPane) map.get("titledPane");
        titledPane.setVisible(true);

    }

    private void loadPredictionData(ProjectFileDescriptor data){
        try {
            this.predictionDataSet.setValue(DataKeeper.loadDataset(data.getAsFile()));
            this.windowPrediction.setValue(new WindowPrediction());
            this.fillTableViewWithData(predictionDatasetTableView, this.predictionDataSet.getValue());
            predictionInputTime = new ArrayList<>(predictionDataSet.getValue().getOutputs()+predictionDataSet.getValue().getInputs());
            for (int i = 0; i < predictionDataSet.getValue().getOutputs()+predictionDataSet.getValue().getInputs(); i++) {
                predictionInputTime.add(0);
            }

            predictionErrorTextField.setText("0");
            predictionChart.getData().clear();

            Map<String, Node> map;
            for (int i = 0; i < this.dynamicPredictionNodes.size(); i++) {
                map = this.dynamicPredictionNodes.get(i);
                map.get("titledPane").setVisible(false);
                ((TitledPane) map.get("titledPane")).setText(this.predictionDataSet.getValue().getHeaders().get(i));
                ((LineChart) map.get("lineChart")).setTitle(this.predictionDataSet.getValue().getHeaders().get(i));
                ((LineChart) map.get("lineChart")).getData().clear();
                ((ProgressBar) map.get("progressBar")).progressProperty().unbind();
                ((ProgressBar) map.get("progressBar")).progressProperty().setValue(0);
                ((JFXTextField) map.get("error")).textProperty().unbind();
                ((JFXTextField) map.get("error")).setText("0");
                ((JFXTextField) map.get("valError")).textProperty().unbind();
                ((JFXTextField) map.get("valError")).setText("0");
            }



            while (this.dynamicPredictionNodes.size() < this.predictionDataSet.getValue().getInputs()){
                this.createInputPredictionTitledPane(this.predictionDataSet.getValue().getHeaders().get(this.dynamicPredictionNodes.size()));
                ((TitledPane) this.dynamicPredictionNodes.get(this.dynamicPredictionNodes.size()-1).get("titledPane")).setVisible(false);
            }

            setChartsRange();

            List<XYChart.Data<Number, Number>> inputs = new ArrayList<>(this.predictionDataSet.getValue().getData().size());
            for (int j = 0; j < this.predictionDataSet.getValue().getData().size(); j++) {
                inputs.add(createXYData(this.predictionDataSet.getValue().getLegend().get(j), this.predictionDataSet.getValue().getData().get(j).get(this.predictionDataSet.getValue().getInputs()), this.predictionDataSet.getValue().getLegendHeader(), this.predictionDataSet.getValue().getHeaders().get(this.predictionDataSet.getValue().getInputs())));
            }
            XYChart.Series<Number, Number> series = new XYChart.Series<>("Факт. " + this.predictionDataSet.getValue().getHeaders().get(this.predictionDataSet.getValue().getInputs()), FXCollections.observableArrayList(inputs));
            predictionChart.getData().add(series);
            predictionChart.getYAxis().setLabel("Значения");
            predictionChart.getXAxis().setLabel(this.predictionDataSet.getValue().getLegendHeader());


        } catch (IOException e) {
            e.printStackTrace();
            AlertWindow.createAlertWindow("Не удалось загрузить файл \n" + data.getFullPath()).show();
        }
    }


}
