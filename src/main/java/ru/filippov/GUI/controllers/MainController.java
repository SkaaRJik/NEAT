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
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.log4j.Logger;
/*import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.ChartZoomManager;
import org.gillius.jfxutils.chart.JFXChartUtil;*/
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.applications.train.NEATTrainingForJavaFX;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.core.NEATLoader;
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
import ru.filippov.utils.AdvancedNetVisualisator;
import ru.filippov.utils.CsControl;
import ru.filippov.utils.JFXUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {




    static class ProjectFileDescriptor{


        public enum TYPE{
            PROJECT,
            TRAINING_SET,
            TEST_SET,
            NEAT_CONFIG,
            TRAINED_MODEL
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
                case PROJECT:
                    graphic  = new MaterialIconView(projectIcon);
                    break;
                case NEAT_CONFIG:
                    graphic = new OctIconView(neatIcon);
                    break;
                case TEST_SET:
                    graphic = new MaterialDesignIconView(testDatasetIcon);
                    break;
                case TRAINING_SET:
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
            if (this.type == TYPE.PROJECT || this.type == TYPE.NEAT_CONFIG){
                return name;
            }
            return name + " ( " + this.type + " ) ";
        }

        public Node getGraphic() {
            return graphic;
        }

        public void setGraphic(Node graphic) {
            this.graphic = graphic;
        }

        public File getAsFile() {
            return new File(this.directoryPath+"\\"+this.name+"."+this.extension);
        }

        public String getFullPath(){
            return this.directoryPath+"\\"+this.name+"."+this.extension;
        }

    }

    static class DataKeeper{
        List<List<Double>> data;
        List<String> headers;
        String legendHeader;
        List<Double> legend;
        int inputs;
        int outputs;

        public DataKeeper() {

        }

        public DataKeeper(List<List<Double>> data, List<String> headers, List<Double> legend) {
            this.data = data;
            this.headers = headers;
            this.legend = legend;
        }

        public List<List<Double>> getData() {
            return data;
        }

        public void setData(List<List<Double>> data) {
            this.data = data;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public List<Double> getLegend() {
            return legend;
        }

        public void setLegend(List<Double> legend) {
            this.legend = legend;
        }

        public List<String> getHeadersForTableView(){
            List<String> newHeaders = new ArrayList<>(this.headers.size()+1);

            newHeaders.add(legendHeader);
            newHeaders.addAll(headers);
            return newHeaders;

        }

        public List<List<Double>> getDataForTableView(){
            List<List<Double>> dataForTableView = new ArrayList<>(this.data.size()+1);
            for (int i = 0 ; i < data.size(); i++) {
                List<Double> row = new ArrayList<>(data.get(i).size()+1);
                row.add(legend.get(i));
                for (int j = 0; j < data.get(i).size(); j++) {
                    row.add(data.get(i).get(j));
                }
                dataForTableView.add(row);
            }
            return dataForTableView;
        }

        public String getLegendHeader() {
            return legendHeader;
        }

        public void setLegendHeader(String legendHeader) {
            this.legendHeader = legendHeader;
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
    private ContextMenu datasetFolderContext;
    private ContextMenu dataContextMenu;

    @FXML
    private SplitPane projectSplitPane;
    @FXML
    private BorderPane projectBorderPane;
    @FXML
    private MaterialDesignIconView openProjectMenuIcon;
    @FXML
    private Label projectLabel;
    @FXML
    private JFXButton pinProjectMenuButton;
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
    private JFXTextField maxPertrubTextField;
    private JFXTextField maxBiasPertrubTextField;
    private JFXToggleButton featureSelectionToggle;
    private JFXToggleButton reccurencyAllowedToggle;
    private TitledPane extinctionControlTitledPane;
    private JFXToggleButton eleEventsToogle;
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
    @FXML private VBox trainVBox;
    @FXML private ProgressBar trainingProgressBar;




    private Thread trainThread;
    @FXML
    private LineChart<Number, Number> errorChart;
    @FXML private JFXButton errorChartRefreshButton;
    @FXML private LineChart<Number, Number> valueGraphicChart;
    @FXML private JFXButton valueGraphicChartButton;
    @FXML
    private BorderPane netVisualizationBorderPane;
    @FXML
    private Pane drawablePane;
    //private ZoomableCanvas netVisualisationCanvas;
    private AdvancedNetVisualisator netVisualisator;


    @FXML
    private TableView<List<Double>> testTableView;
    @FXML
    private Tab testingTab;
    @FXML private ProgressBar testingProgressBar;

    @FXML
    private ChoiceBox<ProjectFileDescriptor> trainedModelsChoiceBox;

    @FXML
    private Tooltip openTrainedModelTooltip;

    @FXML
    private ChoiceBox<ProjectFileDescriptor> testDatasetChoiceBox;

    @FXML
    private Tooltip openTestDatasetTooltip;

    @FXML
    private JFXButton runTestButton;



    @FXML private Button startTrainingButton;
    @FXML private JFXButton pinButton;


    @FXML
    private MaterialDesignIconView pinIcon;




    private AIConfig currentNEATConfig;
    private boolean isNEATConfigSaved;
    private ProjectFileDescriptor currentNeatConfigFile;

    private File tempDirectory;

    ResourceBundle resourceBundle;
    Locale locale;
    Scene scene;
    DataKeeper trainDataSet;
    DataKeeper testDataSet;

    List<ProjectFileDescriptor> trainSets;
    List<ProjectFileDescriptor> testSets;

    int trainingCount = 0;

    public void init() {
        this.scene = this.currentProjectLabel.getParent().getScene();




        new File(Paths.get("").toAbsolutePath().toString()+"\\projects").mkdir();
        this.tempDirectory = new File(Paths.get("").toAbsolutePath().toString()+"\\temp");
        if(!this.tempDirectory.exists()){
            this.tempDirectory.mkdir();
        }


        this.scene.getWindow().setOnCloseRequest(event -> {
            Arrays.stream(this.tempDirectory.listFiles()).forEach(file1 -> file1.delete());
        });


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

            treeCell.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {

                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
                    ProjectFileDescriptor value = treeCell.getTreeItem().getValue();


                    switch (value.getType()){
                        case NEAT_CONFIG:
                            openNEATFile(value);
                            break;
                        case TRAINED_MODEL:
                            if(currentNeatConfigFile == null){
                                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                            }
                            this.infoTabPane.getSelectionModel().select(testingTab);
                            if(!trainedModelsChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                                this.trainedModelsChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            }
                            this.trainedModelsChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            break;
                        case TEST_SET:
                            if(currentNeatConfigFile == null){
                                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                            }
                            this.infoTabPane.getSelectionModel().select(testingTab);

                            if(!testDatasetChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                                this.testDatasetChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            }
                            this.testDatasetChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            break;
                        case TRAINING_SET:
                            if(currentNeatConfigFile == null){
                                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                            }

                            this.infoTabPane.getSelectionModel().select(trainigTab);

                            if(!trainDatasetChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                                this.trainDatasetChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            }
                            this.trainDatasetChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                            break;
                    }
                    e.consume();
                }
            });
            return treeCell;
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
        this.maxPertrubTextField = new JFXTextField();
        this.maxBiasPertrubTextField = new JFXTextField();
        this.featureSelectionToggle = new JFXToggleButton();
        this.reccurencyAllowedToggle = new JFXToggleButton();
        this.inputNodesTextField.setLabelFloat(true);
        this.outputNodesTextField.setLabelFloat(true);
        this.maxPertrubTextField.setLabelFloat(true);
        this.maxBiasPertrubTextField.setLabelFloat(true);
        tempVbox.getChildren().addAll(
                this.inputNodesTextField,
                this.outputNodesTextField,
                this.maxPertrubTextField,
                this.maxBiasPertrubTextField,
                this.featureSelectionToggle,
                this.reccurencyAllowedToggle
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

        this.eleEventsToogle.selectedProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue){
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
        this.terminationValueTextField.setTooltip(new Tooltip());;
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

        enableSlideMenu(openNEATMenuIcon, neatMenuBorderPane, MAX_WIDTH_NEAT_MENU, closeNEATMenu, openNEATMenu);
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

        enableSlideMenu(openProjectMenuIcon, projectBorderPane, MAX_WIDTH_PROJECT_MENU, closeProjectMenu, openProjectMenu);
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
        addReqieredFieldValidator(this.maxPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.maxBiasPertrubTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleSurvivalCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.eleEventTimeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.extraFeatureCountTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.popSizeTextField, requiredFieldValidator);
        addReqieredFieldValidator(this.numberEpochsTextField, requiredFieldValidator);*/









        this.inputNodesTextField.setDisable(true);
        this.outputNodesTextField.setDisable(true);



        if (this.currentNEATConfig == null){
            this.parametresScrollPane.setVisible(false);
            this.infoTabPane.setVisible(false);
            this.dataSetsScrollPane.setVisible(false);
        }


        this.trainDatasetChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                this.trainDataSet = loadDataset(newValue.getAsFile(), true);
                this.fillTableViewWithData(this.trainTableView, this.trainDataSet.getHeadersForTableView(), this.trainDataSet.getDataForTableView());
                //loadDataset(newValue.getAsFile());
                this.dataSetsScrollPane.setVisible(true);
                errorChart.getData().clear();
                valueGraphicChart.getData().clear();
                drawablePane.getChildren().clear();

                trainingCount = 0;
                //this.currentNEATConfig.updateConfig("AI.SOURCE", newValue+"BestNetwork_temp.ser");
                //this.currentNEATConfig.updateConfig("SAVE.LOCATION", newValue+"BestNetwork_temp.ser");
                //TODO REFACTOR THIS SHIT
                this.currentNEATConfig.updateConfig("SAVE.LOCATION", this.currentNeatConfigFile.getDirectoryPath()+"\\"+newValue.getName()+"_last_best.ser");

                /*if(new File(this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork.ser").exists()){
                    this.runnableNEATConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork.ser");
                    this.runnableNEATConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"@test.dataset");
                    logger.debug(this.runnableNEATConfig.configElement("AI.SOURCE"));
                    logger.debug(this.runnableNEATConfig.configElement("INPUT.DATA"));
                    this.testingTab.setDisable(false);
                } else if(new File(this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser").exists()){
                    this.runnableNEATConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"@test.dataset");
                    this.runnableNEATConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser");
                    logger.debug(this.runnableNEATConfig.configElement("AI.SOURCE"));
                    logger.debug(this.runnableNEATConfig.configElement("INPUT.DATA"));
                    this.testingTab.setDisable(false);
                } else {
                    this.runnableNEATConfig.updateConfig("AI.SOURCE", this.currentProjectTextField.getText()+"\\datasets\\"+newValue+"\\"+newValue+"BestNetwork_temp.ser");
                    logger.debug(this.runnableNEATConfig.configElement("AI.SOURCE"));
                    //this.testingTab.setDisable(true);
                }*/

                /*trainigTab.setDisable(false);
                testingTab.setDisable(false);*/
                this.startTrainingButton.setDisable(false);
            } else {
                this.startTrainingButton.setDisable(true);
            }
        });

        //trainigTab.setDisable(true);
        //testingTab.setDisable(true);
        this.trainingProgressBar = new ProgressBar(0);
        this.trainingProgressBar.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY){
                if(!this.trainigTab.isDisable())
                    this.infoTabPane.getSelectionModel().select(this.trainigTab);
            }
        });

        this.testingProgressBar = new ProgressBar(0);
        this.testingProgressBar.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY){
                if(!this.testingTab.isDisable())
                    this.infoTabPane.getSelectionModel().select(this.testingTab);
            }
        });

        trainigTab.setGraphic(new BorderPane(trainingProgressBar,null,null,null, null));
        testingTab.setGraphic(new BorderPane(testingProgressBar,null,null,null, null));

        errorChartRefreshButton.setOnAction(event -> {
            this.errorChart.getData().clear();
            this.trainingCount = 0;
        });

        valueGraphicChartButton.setOnAction(event -> {
            this.valueGraphicChart.getData().clear();
        });


        //Panning works via either secondary (right) mouse or primary with ctrl held down
        configureChart(this.errorChart);
        configureChart(this.valueGraphicChart);
        valueGraphicChart.getXAxis().setAutoRanging( false );
        valueGraphicChart.getYAxis().setAutoRanging( true );



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

        netVisualizationBorderPane.setCenter(ZoomPane.createZoomPane(this.drawablePane));

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

    private void setPinButtonAction(JFXButton pinMenuButton, BorderPane menuBorderPane, double maxWidthMenu, double minWidthMenu) {
        pinMenuButton.setOnAction(new EventHandler<ActionEvent>() {
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
        );
    }


    private void enableSlideMenu(MaterialDesignIconView openMenuIcon, BorderPane menuBorderPane, double maxWidth, Timeline closeMenu, Timeline openMenu) {
        RotateTransition iconRotateTransition = new RotateTransition(Duration.millis(500), openMenuIcon);
        iconRotateTransition.setFromAngle(0);
        iconRotateTransition.setToAngle(180);
        iconRotateTransition.setAutoReverse(true);

        menuBorderPane.setOnMouseEntered(evt -> {
            menuBorderPane.setMinWidth(menuBorderPane.getMinWidth()-0.1);
            menuBorderPane.setMaxWidth(maxWidth);
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
            iconRotateTransition.setRate(-1);
            iconRotateTransition.play();
            openMenu.stop(); closeMenu.play();
        });
    }



    private DataKeeper loadDataset(File datasetName, boolean needInit){
        try {
            /*Read training dataset file*/
            DataKeeper dataKeeper = new DataKeeper();
            BufferedReader reader = new BufferedReader(new FileReader(datasetName));
            StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(),";");
            List<String> headers = new ArrayList<>();

            if(needInit) {
                this.inputNodesTextField.setText(stringTokenizer.nextToken()); //get number of inputs
                this.outputNodesTextField.setText(stringTokenizer.nextToken()); // get number of outputs
            }
            String line = reader.readLine();
            stringTokenizer = new StringTokenizer(line,":");
            if(stringTokenizer.nextToken().equals("Legend")){
                if(stringTokenizer.hasMoreTokens()) {
                    stringTokenizer = new StringTokenizer(stringTokenizer.nextToken(), ";");
                    dataKeeper.setLegendHeader(stringTokenizer.nextToken());
                    List<Double> legend = new ArrayList<>(stringTokenizer.countTokens());
                    while (stringTokenizer.hasMoreTokens()) {
                        legend.add(Double.valueOf(stringTokenizer.nextToken()));
                    }
                    dataKeeper.setLegend(legend);
                }
                line = reader.readLine();
            }


            stringTokenizer = new StringTokenizer(line,";");
            while (stringTokenizer.hasMoreTokens()){
                headers.add(stringTokenizer.nextToken());
            }
            dataKeeper.setHeaders(headers);

            /*Read dataset values*/
            List<List<Double>> tempDataSet = new ArrayList<>(50);
            line = reader.readLine();
            while (line != null) {
                stringTokenizer = new StringTokenizer(line,";");
                List<Double> row = new ArrayList<>();
                while(stringTokenizer.hasMoreTokens()){
                    row.add(Double.valueOf(stringTokenizer.nextToken()));
                }
                tempDataSet.add(row);
                line = reader.readLine();
            }

            dataKeeper.setData(tempDataSet);

            if(dataKeeper.getLegend() == null){
                dataKeeper.setLegendHeader("№");
                List<Double> legend = new ArrayList<>();
                for (double i = 0; i < dataKeeper.getData().size(); i++) {
                    legend.add(i+1);
                }
                dataKeeper.setLegend(legend);
            }


            return dataKeeper;
            /*Reading of training dataset is over*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void fillTableViewWithData(TableView<List<Double>> tableView, List<String> headers, List<List<Double>> data){


        tableView.getColumns().clear();
        tableView.getItems().clear();
        /*Prepare headers of table's columns*/

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
                    observableList.addAll(data);
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
        this.maxPertrubTextField.setPromptText(resourceBundle.getString("MAX_WEIGHT_PERTURB"));
        this.maxBiasPertrubTextField.setPromptText(resourceBundle.getString("MAX_BIAS_PERTURB"));
        this.featureSelectionToggle.setText(resourceBundle.getString("FEATURE_SELECTION"));
        this.reccurencyAllowedToggle.setText(resourceBundle.getString("RECURRENCY_ALLOWED"));

        this.extinctionControlTitledPane.setText(resourceBundle.getString("EXTINCTION_CONTROL"));
        this.eleEventsToogle.setText(resourceBundle.getString("EXTINCTION_EVENT"));
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

        this.isNEATConfigSaved = true;


        BufferedReader reader = new BufferedReader(new FileReader(projectFile));
        StringTokenizer stringTokenizer = new StringTokenizer(reader.readLine(),":");
        String token;

        TreeItem<ProjectFileDescriptor> lastSelected = null;

        TreeItem<ProjectFileDescriptor> rootProject = new TreeItemContextMenu<ProjectFileDescriptor>();






        ProjectFileDescriptor projectFileDescriptor = null;
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
        String line;



        String pathToCurrentDataset = "";

        while((line=reader.readLine())!=null){
            stringTokenizer = new StringTokenizer(line,":");
            switch (stringTokenizer.nextToken()){
                case "DATASET_NAME":
                    if(stringTokenizer.hasMoreTokens()){
                        token = stringTokenizer.nextToken();
                        String[] fileName = token.split("[.]");
                        pathToCurrentDataset = projectFile.getParent()+"\\datasets\\"+fileName[0]+"\\";
                        projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.NEAT_CONFIG, pathToCurrentDataset ,fileName[0], fileName[1]);
                        treeItem = new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), datasetFolderContext);
                        rootProject.getChildren().add(treeItem);

                        if(lastOpenedProject.equals(treeItem.getValue())){
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
            openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getValue());
        });

        projectContextMenu.getItems().addAll(
                newDatasetFolder
        );

        MenuItem newDatasetItem = new MenuItem("Prepare new data");
        newDatasetItem.setOnAction(event -> {
            prepareNewData(event);
        });


        datasetFolderContext = new ContextMenu();
        configureContextMenu(datasetFolderContext);
        datasetFolderContext.getItems().addAll(loadNEAT, new SeparatorMenuItem(), newDatasetItem, new SeparatorMenuItem(), deleteNEAT);



        MenuItem loadDataItem = new MenuItem("Load");
        loadDataItem.setOnAction(event -> {
            if(currentNeatConfigFile == null){
                openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
            }
            ProjectFileDescriptor projectFileDescriptor = projectTreeView.getSelectionModel().getSelectedItem().getValue();
            switch (projectFileDescriptor.getType()){
                case TRAINED_MODEL:
                    if(currentNeatConfigFile == null){
                        openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                    }
                    this.infoTabPane.getSelectionModel().select(testingTab);
                    if(!trainedModelsChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                        this.trainedModelsChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    }
                    this.trainedModelsChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    break;
                case TEST_SET:
                    if(currentNeatConfigFile == null){
                        openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                    }
                    this.infoTabPane.getSelectionModel().select(testingTab);

                    if(!testDatasetChoiceBox.getItems().contains(this.projectTreeView.getSelectionModel().getSelectedItem().getValue())) {
                        this.testDatasetChoiceBox.getItems().add(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    }
                    this.testDatasetChoiceBox.getSelectionModel().select(this.projectTreeView.getSelectionModel().getSelectedItem().getValue());
                    break;
                case TRAINING_SET:
                    if(currentNeatConfigFile == null){
                        openNEATFile(projectTreeView.getSelectionModel().getSelectedItem().getParent().getValue());
                    }

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
                    viewDataInNewWindow(projectFileDescriptor);
                    break;
                case TEST_SET:
                    viewDataInNewWindow(projectFileDescriptor);
                    break;
            }

        });


        this.dataContextMenu = new ContextMenu();
        configureContextMenu(this.dataContextMenu);
        this.dataContextMenu.getItems().addAll(viewDataItem, loadDataItem, new SeparatorMenuItem(), deleteDataItem);




    }

    private void configureContextMenu(ContextMenu contextMenu) {
        contextMenu.setOnShowing(event -> {
                    projectBorderPane.setMaxWidth(MAX_WIDTH_PROJECT_MENU);
                    projectBorderPane.setPrefWidth(MAX_WIDTH_PROJECT_MENU);
                    projectBorderPane.setOnMouseEntered(null);
                    projectBorderPane.setOnMouseExited(null);
            }
        );

        contextMenu.setOnHiding(event -> {
            projectBorderPane.getCenter().setVisible(false);
            projectBorderPane.setMaxWidth(MIN_WIDTH_PROJECT_MENU);
            projectBorderPane.setPrefWidth(MIN_WIDTH_PROJECT_MENU);
            enableSlideMenu(openProjectMenuIcon, projectBorderPane, MAX_WIDTH_PROJECT_MENU, closeProjectMenu, openProjectMenu);

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

    private void openNEATFile(ProjectFileDescriptor projectFileDescriptor) {


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

        File projectFile = projectFileDescriptor.getAsFile();
        if(projectFile != null){

            this.clearAllInfoElements();
            this.clearGUIConfig();
            this.testDatasetChoiceBox.getItems().clear();
            this.trainDatasetChoiceBox.getItems().clear();
            this.trainedModelsChoiceBox.getItems().clear();

            this.currentNEATConfig = this.loadConfig(projectFile.getAbsolutePath());
            //this.currentProjectTextField.setText(projectFile.getParent());
            //this.neatOptionsLabel.setText(neatOptionGASettings);
            this.neatMenuBorderPane.setCenter(this.parametresScrollPane);
            this.infoTabPane.setVisible(true);


            TreeItem<ProjectFileDescriptor> treeItem = this.projectTreeView.getRoot().getChildren().stream().filter(projectFileDescriptorTreeItem -> {
                return projectFileDescriptorTreeItem.getValue() == projectFileDescriptor;
            }).findAny().orElse(null);


            if(treeItem != null) {
                this.currentNeatConfigFile = treeItem.getValue();
                fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TRAINING_SET, treeItem, trainDatasetChoiceBox);
                fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TEST_SET, treeItem, testDatasetChoiceBox);
                fillChoiceBoxWithData(ProjectFileDescriptor.TYPE.TRAINED_MODEL, treeItem, trainedModelsChoiceBox);
            }
            fillFieldsUsingAIConfig(this.currentNEATConfig);

            isNEATConfigSaved = true;

        }
    }

    private void fillChoiceBoxWithData(ProjectFileDescriptor.TYPE type, TreeItem<ProjectFileDescriptor> treeItem, ChoiceBox<ProjectFileDescriptor> choiceBox) {
        treeItem.getChildren().stream().forEach(treeItem1 -> {
            if(treeItem1.getValue().getType() == type) choiceBox.getItems().add(treeItem1.getValue());
        });
        choiceBox.getSelectionModel().selectFirst();
    }

    private void clearAllInfoElements() {

        trainTableView.getItems().clear();
        trainTableView.getColumns().clear();
        testTableView.getItems().clear();
        testTableView.getColumns().clear();
        errorChart.getData().clear();
        valueGraphicChart.getData().clear();
        this.trainingProgressBar.progressProperty().setValue(0);
        this.testingProgressBar.progressProperty().setValue(0);
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
        maxPertrubTextField.setText("");
        maxBiasPertrubTextField.setText("");
        featureSelectionToggle.setSelected(false);
        reccurencyAllowedToggle.setSelected(false);
        eleEventsToogle.setSelected(false);
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
            }
        }
    }

    @FXML
    private void createNewDatasetFolder(ActionEvent actionEvent){
        NewDatasetDialogue dialogue = NewDatasetDialogue.getInstance(this.scene);
        dialogue.setCurrentProject(this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getAsFile());
        dialogue.show();
        if (dialogue.getNewDatasetFolder() != null){
            ProjectFileDescriptor projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.NEAT_CONFIG, dialogue.getNewDatasetFolder().getAbsolutePath()+"\\"+dialogue.getNewDatasetFolder().getName()+".neat", dialogue.getNewDatasetFolder().getName(), "neat");
            TreeItem<ProjectFileDescriptor> treeItem = new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), datasetFolderContext);
                this.projectTreeView.getRoot().getChildren().add(treeItem);
        }
        saveProject();

    }

    private void saveProject() {

        ProjectFileDescriptor projectFileDescriptor = this.projectTreeView.getRoot().getValue();
        try {

            FileWriter fileWriter = new FileWriter(projectFileDescriptor.getAsFile(), false);

            fileWriter.write("PROJECT_NAME:"+this.projectTreeView.getRoot().getValue().getName()+"."+this.projectTreeView.getRoot().getValue().getExtension()+"\n");
            fileWriter.append("LAST_OPENED_DATASET:");
            if(currentNeatConfigFile != null) {
                fileWriter.append(currentNeatConfigFile.getName() + "." + currentNeatConfigFile.getExtension()+"\n");
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
                            fileWriter.append("TRAINED_MODEL:"+ item.getValue().getName()+"."+item.getValue().getExtension()+"\n");
                            break;
                    }
                }
            }
            fileWriter.flush();
            fileWriter.close();



        } catch (IOException e) {
            e.printStackTrace();
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
            initNEATConfigUsingGUI(this.currentNEATConfig);
            currentNEATConfig.saveConfig(this.currentNeatConfigFile.getAsFile());
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
         NEATConfig.updateConfig("MAX.PERTURB", maxPertrubTextField.getText());
         NEATConfig.updateConfig("MAX.BIAS.PERTURB", maxBiasPertrubTextField.getText());
         NEATConfig.updateConfig("FEATURE.SELECTION", String.valueOf(featureSelectionToggle.isSelected()));
         NEATConfig.updateConfig("RECURRENCY.ALLOWED", String.valueOf(reccurencyAllowedToggle.isSelected()));
         NEATConfig.updateConfig("ELE.EVENTS", String.valueOf(eleEventsToogle.isSelected()));
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
                if(j!=0) stringBuilder.append(";");
                if(jfxToggleButton.isSelected())
                    stringBuilder.append(ActivationFunctionFinder.getFunctionClassNameByName(jfxToggleButton.getText()));
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
        maxPertrubTextField.setText(NEATConfig.configElement("MAX.PERTURB"));
        maxBiasPertrubTextField.setText(NEATConfig.configElement("MAX.BIAS.PERTURB"));
        featureSelectionToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("FEATURE.SELECTION")));
        reccurencyAllowedToggle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("RECURRENCY.ALLOWED")));
        eleEventsToogle.setSelected(Boolean.parseBoolean(NEATConfig.configElement("ELE.EVENTS")));
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
    
    @FXML
    private void prepareNewData(ActionEvent actionEvent) {
        DataPreparatorDialogue.getInstance(this.scene).setCurrentDatasetFolder(this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getAsFile().getParent()).show();
        String nameOfDataSet = DataPreparatorDialogue.getInstance(this.scene).getNameOfTrainingSet();
        ProjectFileDescriptor projectFileDescriptor;
        if(nameOfDataSet.length() != 0){
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINING_SET, this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getDirectoryPath(), nameOfDataSet, "trd");
            this.projectTreeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            this.trainDatasetChoiceBox.getItems().add(projectFileDescriptor);
        }
        nameOfDataSet = DataPreparatorDialogue.getInstance(this.scene).getNameOfTestSet();
        if(nameOfDataSet.length() != 0){
            projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TEST_SET, this.projectTreeView.getSelectionModel().getSelectedItem().getValue().getDirectoryPath(), nameOfDataSet, "ted");
            this.projectTreeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItemContextMenu<>(projectFileDescriptor, projectFileDescriptor.getGraphic(), dataContextMenu));
            this.testDatasetChoiceBox.getItems().add(projectFileDescriptor);
        }
        saveProject();
        /*if( nameOfDataSet != null ){
            if(nameOfDataSet.length() != 0){
                try {
                    this.currentNEATConfig.saveConfig(this.projectFile);
                } catch (IOException e) {
                    AlertWindow.createAlertWindow("Не удалось сохранить новый конфиг");
                }
            }
        }*/



    }

    public void generateNewSeed() {
        generatorSeedTextField.setText(String.valueOf(System.currentTimeMillis()));
    }


    public void trainModel(ActionEvent actionEvent) {
        //trainigTab.setContent(new SideBar(30, new TextField()));
        if(trainThread != null)
        if(trainThread.isAlive()){
            trainThread.interrupt();
        }
        initNEATConfigUsingGUI(this.currentNEATConfig);
        this.currentNEATConfig.updateConfig("TRAINING.SET", tempDirectory.getAbsolutePath()+"\\"+UUID.randomUUID()+"."+trainDatasetChoiceBox.getSelectionModel().getSelectedItem().getExtension());

        try {
            this.saveTempDataSet(this.currentNEATConfig.configElement("TRAINING.SET"));
            logger.debug(this.currentNEATConfig.configElement("TRAINING.SET"));
            //this.currentNEATConfig.updateConfig("INPUT.DATA", this.currentProjectTextField.getText()+"\\datasets\\"+this.trainDatasetChoiceBox.getValue()+"\\"+this.trainDatasetChoiceBox.getValue()+"@test_temp.dataset");
            trainigTab.setDisable(false);
            infoTabPane.getSelectionModel().select(trainigTab);



            if(this.valueGraphicChart.getData().isEmpty()){
                //double tick = this.trainDataSet.getLegend().stream().mapToDouble(value -> {return value;}).sum() / this.trainDataSet.getLegend().size();
                double tick = (this.trainDataSet.getLegend().get(this.trainDataSet.getLegend().size()-1) - this.trainDataSet.getLegend().get(0)) / (this.trainDataSet.getLegend().size()-1);
                ((NumberAxis)valueGraphicChart.getXAxis()).setTickUnit(tick);
                //((NumberAxis)valueGraphicChart.getXAxis()).setTickUnit(this.trainDataSet.legend.get(1)-this.trainDataSet.legend.get(0));
                ((NumberAxis)valueGraphicChart.getXAxis()).setLowerBound(this.trainDataSet.legend.get(0)-((NumberAxis)valueGraphicChart.getXAxis()).getTickUnit());
                ((NumberAxis)valueGraphicChart.getXAxis()).setUpperBound(this.trainDataSet.legend.get(this.trainDataSet.legend.size()-1)+((NumberAxis)valueGraphicChart.getXAxis()).getTickUnit());
                valueGraphicChart.getXAxis().setLabel(this.trainDataSet.getLegendHeader());
                XYChart.Series expectedOutputDataXYChart = null;
                for (int i = 0; i < Integer.parseInt(this.outputNodesTextField.getText()); i++) {

                    TableColumn tableColumn = this.trainTableView.getColumns().get(this.trainTableView.getColumns().size()-1-i);
                    expectedOutputDataXYChart = new XYChart.Series();
                    this.valueGraphicChart.getData().add(expectedOutputDataXYChart);
                    expectedOutputDataXYChart.setName(tableColumn.getText() + " (Факт)");
                    for (int j = 0; j < this.trainTableView.getItems().size(); j++) {
                        XYChart.Data integerObjectData = new XYChart.Data<>(trainDataSet.legend.get(j), tableColumn.getCellData(j));
                        integerObjectData.setNode(new StackPane());
                        expectedOutputDataXYChart.getData().add(integerObjectData);
                        Tooltip.install(integerObjectData.getNode(), new Tooltip(String.valueOf(tableColumn.getCellData(j))));
                    }
                }
            }



            NEATTrainingForJavaFX neatTrainingForJavaFX = new NEATTrainingForJavaFX();
            XYChart.Series errorSeries = new XYChart.Series();
            errorSeries.setName("Fitness of the " + ++this.trainingCount + " run");
            this.errorChart.getData().add(errorSeries);




            XYChart.Series outputValuesSeries = new XYChart.Series();
            outputValuesSeries.setName(this.trainingCount + ". " + this.trainTableView.getColumns().get(this.trainTableView.getColumns().size()-1).getText());
            this.valueGraphicChart.getData().add(outputValuesSeries);



           /*Platform.runLater(() -> {
                *//*errorSeries.getNode().lookup(".chart-series-line"). setStyle("-fx-stroke: "+colour[0]+";");
                Node[] nodes = errorChart.lookupAll(".chart-line-symbol").toArray(new Node[0]);
                nodes[nodes.length-1].setStyle("-fx-background-color: "+ colour[0] +", white;");
                for (int i = 0; i <outputValuesSeries.length ; i++) {
                    outputValuesSeries[i].getNode().lookup(".chart-series-line"). setStyle("-fx-stroke: "+colour[i]+";");
                    nodes = valueGraphicChart.lookupAll(".chart-line-symbol").toArray(new Node[0]);
                    nodes[nodes.length-1].setStyle("-fx-background-color: "+ colour[i] +", white;");
                }*//*

            });*/



            neatTrainingForJavaFX.initialise(currentNEATConfig);


            AtomicInteger atomicInteger = new AtomicInteger(1);
            neatTrainingForJavaFX.getBestEverChromosomesProperty().addListener((ListChangeListener<? super Chromosome>) c -> {
                Platform.runLater(() -> {
                    int n = neatTrainingForJavaFX.getBestEverChromosomes().size();
                    double fitnessValue = neatTrainingForJavaFX.getBestEverChromosomes().get(n-1).fitness();
                    XYChart.Data<Number, Number> xyData = new XYChart.Data<>(neatTrainingForJavaFX.getCurrentEpoch(), fitnessValue);
                    xyData.setNode(new StackPane());
                    lastErrorTextField.setText(String.valueOf(fitnessValue));
                    currentEpochTextField.setText(String.valueOf(neatTrainingForJavaFX.getCurrentEpoch()));
                    Tooltip.install(xyData.getNode(), new Tooltip(String.valueOf(fitnessValue)));
                    errorSeries.getData().add(xyData);
                    outputValuesSeries.getData().clear();

                    Chromosome bestChromo = neatTrainingForJavaFX.getBestEverChromosomes().get(n - 1);
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
                    }

                });
            });


            neatTrainingForJavaFX.statusProperty().addListener(observable -> {
                Platform.runLater(() -> {
                    int n = neatTrainingForJavaFX.getBestEverChromosomes().size();
                        Chromosome bestChromo = neatTrainingForJavaFX.getBestEverChromosomes().get(n - 1);
                        try {
                            //if(neatTrainingForJavaFX.statusProperty().getValue() > 0.99) {
                                netVisualisator.setNetToVisualise(bestChromo, currentNEATConfig);
                                //netVisualisator.visualiseNet(netVisualisationCanvas);
                                netVisualisator.visualiseNet(this.drawablePane);
                            //}
                            if(neatTrainingForJavaFX.statusProperty().getValue() == 1){

                            }
                        } catch (InitialisationFailedException e) {
                            e.printStackTrace();
                        }
                });
            });
            this.trainingProgressBar.progressProperty().bind(neatTrainingForJavaFX.statusProperty());
            Thread trainThread = new Thread(neatTrainingForJavaFX);
            trainThread.start();

            ProjectFileDescriptor projectFileDescriptor = null;
            for(TreeItem<ProjectFileDescriptor> treeItem : projectTreeView.getRoot().getChildren()){
                if(treeItem.getValue() == currentNeatConfigFile) {
                    for (TreeItem<ProjectFileDescriptor> children :treeItem.getChildren()){
                        if(children.getValue().getType() == ProjectFileDescriptor.TYPE.TRAINED_MODEL && children.getValue().getName().equals(this.trainDatasetChoiceBox.getSelectionModel().getSelectedItem().getName()+"_last_best")){
                            projectFileDescriptor = children.getValue();
                            break;
                        }
                    }
                    if(projectFileDescriptor != null) break;
                    projectFileDescriptor = new ProjectFileDescriptor(ProjectFileDescriptor.TYPE.TRAINED_MODEL, this.trainDatasetChoiceBox.getSelectionModel().getSelectedItem().getDirectoryPath() ,this.trainDatasetChoiceBox.getSelectionModel().getSelectedItem().getName()+"_last_best", ".ser");
                    treeItem.getChildren().add(new TreeItemContextMenu<ProjectFileDescriptor>(projectFileDescriptor, projectFileDescriptor.getGraphic(), this.dataContextMenu));

                    break;
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }


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



    public File saveTempDataSet(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
        double value;
        for (int i = 0; i < this.trainDataSet.getHeaders().size(); i++) {
            bufferedWriter.write(this.trainDataSet.getHeaders().get(i));
            if(i != this.trainDataSet.getHeaders().size() - 1) bufferedWriter.write(";");
        }
        bufferedWriter.append("\n");
        for(List<Double> list : this.trainDataSet.getData()){
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

    public void openTrainedModels(ActionEvent actionEvent) {
    }

    public void openTestDataset(ActionEvent actionEvent) {
    }

    public void viewDataInNewWindow(ProjectFileDescriptor projectFileDescriptor){
        DataKeeper dataKeeper = loadDataset(projectFileDescriptor.getAsFile(), false);

        ViewDataWindow viewDataWindow = ViewDataWindow.getInstance(this.scene, projectFileDescriptor.getType()+" "+projectFileDescriptor.getName());

        fillTableViewWithData(viewDataWindow.getTableView(), dataKeeper.getHeadersForTableView(), dataKeeper.getDataForTableView());
        viewDataWindow.show();
    }





}
