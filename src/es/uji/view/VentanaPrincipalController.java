package es.uji.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 23/07/15.
 */
public class VentanaPrincipalController extends Controller {

    final ToggleGroup group = new ToggleGroup();
    private ObservableList<XYChart.Data<Integer,Double>> data = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> dataCambio = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> ga = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> pa = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> gb = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> pb = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> dataThreshold = FXCollections.observableArrayList();
    final LineChart.Series<Integer,Double> dataSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> dataCambioSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gaSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> paSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gbSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> pbSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> thresholdSerie = new XYChart.Series();
    private Queue<Double> rawData = new LinkedList<>();
    private Queue<Double> rawGA = new LinkedList<>();
    private Queue<Double> rawPA = new LinkedList<>();
    private Queue<Double> rawGB = new LinkedList<>();
    private Queue<Double> rawPB = new LinkedList<>();
    private int alarm = 0;
    private int cambio = 0;
    private boolean cambioDetectado = false;
    private int delay = 60;
    private boolean started = false;
    private boolean updateFirstDecisions = false;

    @FXML
    private RadioButton simulationButton;
    @FXML
    private RadioButton twitterButton;
    @FXML
    private TextField dataBaseTextField;
    @FXML
    private TextField tableTextField;
    @FXML
    private TextField topicTextField;
    @FXML
    private Button startButton;
    @FXML
    private LineChart<Integer, Double> dataChart;
    @FXML
    private Label alarmLabel;
    @FXML
    private LineChart<Integer, Double> cumSumChart;
    @FXML
    private LineChart<Integer, Double> decFunChart;
    @FXML
    private Button fase3Button;
    @FXML
    private TextField cambioField;
    @FXML
    private Label cambioLabel;
    @FXML
    private TextField retrasoField;
    @FXML
    private NumberAxis decFunXAxis;
    @FXML
    private NumberAxis cumSumXAxis;
    @FXML
    private Label segActualLabel;

    @FXML
    private void initialize(){
        decFunXAxis.setForceZeroInRange(false);
        cumSumXAxis.setForceZeroInRange(false);
        fase3Button.setDisable(true);
        cambioField.setDisable(true);
        startButton.setDisable(true);
        twitterButton.setDisable(true);
        simulationButton.setToggleGroup(group);
        twitterButton.setToggleGroup(group);
        dataSerie.setName("Tweets/segundo");
        dataSerie.setData(data);
        dataCambioSerie.setName("Puntos de cambio");
        dataCambioSerie.setData(dataCambio);
        thresholdSerie.setData(dataThreshold);
        dataChart.getData().addAll(dataSerie, dataCambioSerie);


        paSerie.setName("After");
        paSerie.setData(pa);
        pbSerie.setName("Before");
        pbSerie.setData(pb);
        cumSumChart.getData().addAll(paSerie, pbSerie);

        gaSerie.setName("After");
        gaSerie.setData(ga);
        gbSerie.setName("Before");
        gbSerie.setData(gb);
        thresholdSerie.setName("Threshold");
        thresholdSerie.setData(dataThreshold);
        decFunChart.getData().addAll(gaSerie, gbSerie, thresholdSerie);

        topicTextField.setOnKeyReleased(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent keyEvent) {
                if ( !topicTextField.getText().equals("") && !dataBaseTextField.getText().equals("") && !tableTextField.getText().equals("")) {
                    System.out.println(tableTextField.getText());
                    twitterButton.setDisable(false);
                }
            }
        });

        dataBaseTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if ( !topicTextField.getText().equals("") && !dataBaseTextField.getText().equals("") && !tableTextField.getText().equals("")) {
                    twitterButton.setDisable(false);
                }
            }
        });

        tableTextField.setOnKeyReleased(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent keyEvent){
                if ( !topicTextField.getText().equals("") && !dataBaseTextField.getText().equals("") && !tableTextField.getText().equals("")){
                    twitterButton.setDisable(false);
                }
            }
        });

        cambioField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                try {
                    Integer.parseInt(cambioField.getText());
                    fase3Button.setDisable(false);
                } catch (NumberFormatException e) {
                    fase3Button.setDisable(true);
                }
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    if (started) {
                        if (delay == 0) {
                            zonaIntercambio.setFase(2);
                            cambioField.setDisable(false);
                            started = false;
                        } else {
                            delay--;
                        }
                    }
                    if (updateFirstDecisions) {
                        Platform.runLater(() ->{
                            decFunChart.setAnimated(false);
                            cumSumChart.setAnimated(false);
                            gaSerie.getData().clear();
                            gbSerie.getData().clear();
                            paSerie.getData().clear();
                            pbSerie.getData().clear();
                            thresholdSerie.getData().clear();
                            decFunChart.setAnimated(true);
                            cumSumChart.setAnimated(true);
                            dataThreshold.add(new XYChart.Data(ga.size() + cambio, 10));
                            while (!rawGA.isEmpty()) {
                                ga.add(new XYChart.Data(ga.size() + cambio, rawGA.poll()));
                            }
                            while (!rawGB.isEmpty()) {
                                gb.add(new XYChart.Data(gb.size()+cambio, rawGB.poll()));
                            }
                            while (!rawPA.isEmpty()) {
                                pa.add(new XYChart.Data(pa.size()+cambio, rawPA.poll()));
                            }
                            while (!rawPB.isEmpty()) {
                                pb.add(new XYChart.Data(pb.size()+cambio, rawPB.poll()));
                            }
                            dataThreshold.add(new XYChart.Data(ga.size() + cambio, 10));
                        });
                        updateFirstDecisions = false;
                    }
                    Platform.runLater(() ->
                        updateCharts()
                    );
                    if (cambioDetectado) {
                        alarmLabel.setText("Alarma detectada en: " + alarm);
                        cambioLabel.setText("Punto de cambio detectado en: " + cambio);
                        dataCambio.add(new XYChart.Data(cambio, dataSerie.getData().get(cambio).getYValue()));
                        cambioDetectado = false;
                    }
                });
            }
        }, 0, 1000);

    }

    public void setTwitterCusum(){
        mainApp.setTwitter(dataBaseTextField.getText(), tableTextField.getText(), topicTextField.getText());
        startButton.setDisable(false);
    }

    public void setPoissonCusum(){
        mainApp.setPoisson();
        startButton.setDisable(false);
    }

    public void start(){
        try{
            delay = Integer.parseInt(retrasoField.getText());
        }catch (Exception e){ }
        mainApp.startCusum();
        started = true;
    }

    public void startFase3(){
        cusum.setMedio(Integer.parseInt(cambioField.getText()));
        zonaIntercambio.setFase(3);
    }

    private void updateCharts(){
        if (!rawData.isEmpty()) {
            segActualLabel.setText("Segundo actual: "+data.size());
            data.add(new XYChart.Data(data.size(), rawData.poll()));
        }
        if (!rawGA.isEmpty()){
            dataThreshold.add(new XYChart.Data(ga.size() + cambio, 10));
            if (dataThreshold.size() == 3) {
                dataThreshold.remove(1);
            }
        }
        if (!rawGA.isEmpty()){
            ga.add(new XYChart.Data(ga.size()+cambio, rawGA.poll()));
        }
        if (!rawGB.isEmpty()){
            gb.add(new XYChart.Data(gb.size()+cambio, rawGB.poll()));
        }
        if (!rawPA.isEmpty()){
            pa.add(new XYChart.Data(pa.size()+cambio, rawPA.poll()));
        }
        if (!rawPB.isEmpty()){
            pb.add(new XYChart.Data(pb.size()+cambio, rawPB.poll()));
        }
//        try {
//            data.add(new XYChart.Data(data.size(), rawData.pop()));
//        } catch (EmptyStackException e) {
////            System.out.println("No ha iniciado");
//        }

    }

    public void update(double data){
        rawData.add(data);
    }

    public void updateDecisions(double pa, double ga, double pb, double gb){
        rawGA.add(ga);
        rawGB.add(gb);
        rawPA.add(pa);
        rawPB.add(pb);
    }

    public void updateAlarma(int alarma){
        alarm = alarma;
        cambioDetectado = true;
    }

    public void updateCambio(int cambio){
        this.cambio = cambio;
        cambioDetectado = true;
    }

    public void updateFirstDecisions(List<Double> pa,List<Double> ga, List<Double> pb,List<Double> gb){
        rawGA.addAll(ga);
        rawGB.addAll(gb);
        rawPA.addAll(pa);
        rawPB.addAll(pb);
        updateFirstDecisions = true;
    }


    @FXML
    private void handleSettings() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.getDialogPane().setStyle(" -fx-max-width:600px; -fx-max-height: 300px; -fx-pref-width: 600px; -fx-pref-height: 300px;");
        dialog.setResizable(true);
        dialog.setTitle("Configuración");
        dialog.setHeaderText("Elige una fuente de datos e introduce los datos.");

        // Set the icon (must be included in the project).
        dialog.setGraphic(new ImageView(this.getClass().getResource("settings.png").toString()));

        // Set the radio button to select simulation or twitter data.
        RadioButton rSimulation, rTwitter;
        Label textoRadioButtons;
        ToggleGroup group;
        HBox Hradio;
        GridPane grid = new GridPane();

        rSimulation=new RadioButton("Datos simulados");
        rSimulation.setUserData("simulacion");
        rTwitter=new RadioButton("Datos de Twitter");
        rTwitter.setUserData("twitter");

        //create group for radio buttons
        group=new ToggleGroup();
        rSimulation.setToggleGroup(group);
        rTwitter.setToggleGroup(group);
        // Escuchador de los radio button
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {

                    if (group.getSelectedToggle().getUserData().toString() == "twitter") {
                        Platform.runLater(() -> camposTwitter(dialog, grid));
                    } else {
                        Platform.runLater(() -> camposSimulacion(dialog, grid));
                    }

                    System.out.println(group.getSelectedToggle().getUserData().toString() == "twitter");
                }
            }
        });


        //put radio buttons into hbox
        Hradio=new HBox(20, rSimulation, rTwitter);
        Hradio.setPadding(new Insets(10));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        grid.setHgap(40);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 200, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
//
//        grid.add(new Label("Username:"), 0, 0);
//        grid.add(username, 1, 0);
//        grid.add(new Label("Password:"), 0, 1);
//        grid.add(password, 1, 1);
        grid.add(rSimulation, 0, 0);
        grid.add(rTwitter, 1, 0);
//        grid.addRow(0, rSimulation, rTwitter);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node : grid.getChildren()) {
                if (node.getClass().equals(TextField.class)){
                    TextField f = (TextField) node;
                    System.out.println(f.getText().isEmpty());
                }
//                System.out.println(node.getClass().equals(TextField.class));
            }

            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
        });
    }


    private void camposTwitter (Dialog<Pair<String, String>> dialog, GridPane grid) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                TextField topico = new TextField();
                topico.setPromptText("Tópico");

                TextField nombreBBDD = new TextField();
                nombreBBDD.setPromptText("Nombre BBDD");

                TextField nombreTabla = new TextField();
                nombreTabla.setPromptText("Nombre de la tabla");

                TextField retrasoInicial = new TextField();
                retrasoInicial.setPromptText("Retraso inicial");

//
//        grid.add(new Label("Username:"), 0, 0);
//        grid.add(username, 1, 0);
                grid.add(new Label("Tópico: "), 0, 1);
                grid.add(topico, 1, 1);
                grid.add(new Label("Nombre BBDD: "), 0, 2);
                grid.add(nombreBBDD, 1, 2);
                grid.add(new Label("Nombre de la tabla: "), 0, 3);
                grid.add(nombreTabla, 1, 3);
                grid.add(new Label("Retraso inicial: "), 0, 4);
                grid.add(retrasoInicial, 1, 4);
//                dialog.getDialogPane().setContent(grid);
            }
        });


    }


    private void camposSimulacion (Dialog<Pair<String, String>> dialog, GridPane grid) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                TextField lon = new TextField();
                lon.setPromptText("lon");

                ToggleGroup groupSimulacion;
                HBox Hradio;
                RadioButton R1, R3, R5;
                int[] betas;

                R1=new RadioButton("1 Punto cambio");
                R1.setUserData("1 Punto");
                R3=new RadioButton("3 Puntos de cambio");
                R3.setUserData("3 Puntos");
                R5=new RadioButton("5 Puntos de cambio");
                R5.setUserData("5 Puntos");

                //create group for radio buttons
                groupSimulacion=new ToggleGroup();
                R1.setToggleGroup(groupSimulacion);
                R3.setToggleGroup(groupSimulacion);
                R5.setToggleGroup(groupSimulacion);
                // Escuchador de los radio button
                groupSimulacion.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    public void changed(ObservableValue<? extends Toggle> ov,
                                        Toggle old_toggle, Toggle new_toggle) {
                        if (groupSimulacion.getSelectedToggle() != null) {

                            if (groupSimulacion.getSelectedToggle().getUserData().toString() == "1 Punto") {

                            } else {
                                Platform.runLater(() -> camposSimulacion(dialog, grid));
                            }

                        }
                    }
                });

                if (groupSimulacion.getSelectedToggle() != null)
                    groupSimulacion.getUserData().toString();
                //put radio buttons into hbox
                Hradio=new HBox(20, R1, R3, R5);
                Hradio.setPadding(new Insets(10));
//
//        grid.add(new Label("Username:"), 0, 0);
//        grid.add(username, 1, 0);
                grid.add(new Label("Longitus datos regresión: "), 0, 1);
                grid.add(lon, 1, 1);
                grid.add(R1, 0, 2);
                grid.add(R3, 1, 2);
                grid.add(R5, 2, 2);

            }
        });


    }


}
