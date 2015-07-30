package es.uji.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 23/07/15.
 */
public class VentanaPrincipalController extends Controller {

    final ToggleGroup group = new ToggleGroup();
    private ObservableList<XYChart.Data<Integer,Double>> data = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> ga = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> pa = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> gb = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> pb = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Integer,Double>> dataThreshold = FXCollections.observableArrayList();
    final ScatterChart.Series<Integer,Double> dataSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gaSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> paSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gbSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> pbSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> thresholdSerie = new XYChart.Series();
    private Stack<Double> rawData = new Stack<>();
    private Stack<Double> rawGA = new Stack<>();
    private Stack<Double> rawPA = new Stack<>();
    private Stack<Double> rawGB = new Stack<>();
    private Stack<Double> rawPB = new Stack<>();
    private int alarm = 0;
    private int inicio = 0;
    private int cambio = 0;
    private boolean valuesChanged = false;
    private boolean toClear = false;
    private int delay = 60;
    private boolean started = false;

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
    private ScatterChart<Integer, Double> dataChart;
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
    private void initialize(){
        fase3Button.setDisable(true);
        cambioField.setDisable(true);
        startButton.setDisable(true);
        twitterButton.setDisable(true);
        simulationButton.setToggleGroup(group);
        twitterButton.setToggleGroup(group);
        dataSerie.setName("tweets");
        dataSerie.setData(data);
        thresholdSerie.setData(dataThreshold);
        dataChart.getData().add(dataSerie);


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
        dataThreshold.add(new XYChart.Data(ga.size() + inicio, 10));
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
                Platform.runLater(() ->{
                    if (started){
                        if (delay == 0){
                            zonaIntercambio.setFase(2);
                            inicio = data.size();
                            cambioField.setDisable(false);
                            started = false;
                        } else{
                            delay--;
                        }
                    }
                    if (toClear){
                        Platform.runLater(() ->{
                            decFunChart.setAnimated(false);
                            cumSumChart.setAnimated(false);
                            gaSerie.getData().clear();
                            gbSerie.getData().clear();
                            paSerie.getData().clear();
                            pbSerie.getData().clear();
                            decFunChart.setAnimated(true);
                            cumSumChart.setAnimated(true);
                        });
                        toClear = false;
                    }
                    updateCharts();
                    if (valuesChanged){
                        alarmLabel.setText("Alarma detectada en: " + alarm);
                        cambioLabel.setText("Punto de cambio detectado en: " + cambio);
                        valuesChanged = false;
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
        inicio = data.size();
        toClear = true;
    }

    private void updateCharts(){
        if (!rawData.isEmpty()){
            data.add(new XYChart.Data(data.size(), rawData.pop()));
        }
        if (!rawGA.isEmpty()){
            dataThreshold.add(new XYChart.Data(ga.size() + inicio, 10));
            if (dataThreshold.size() == 3) {
                dataThreshold.remove(1);
            }
        }
        if (!rawGA.isEmpty()){
            ga.add(new XYChart.Data(ga.size()+inicio, rawGA.pop()));
        }
        if (!rawGB.isEmpty()){
            gb.add(new XYChart.Data(gb.size()+inicio, rawGB.pop()));
        }
        if (!rawPA.isEmpty()){
            pa.add(new XYChart.Data(pa.size()+inicio, rawPA.pop()));
        }
        if (!rawPB.isEmpty()){
            pb.add(new XYChart.Data(pb.size()+inicio, rawPB.pop()));
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
        valuesChanged = true;
    }

    public void updateCambio(int cambio){
        this.cambio = cambio;
        valuesChanged = true;
    }
}
