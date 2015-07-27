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

import java.util.*;

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
    final ScatterChart.Series<Integer,Double> dataSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gaSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> paSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> gbSerie = new XYChart.Series();
    final LineChart.Series<Integer,Double> pbSerie = new XYChart.Series();
    private Stack<Double> rawData = new Stack<>();
    private Stack<Double> rawGA = new Stack<>();
    private Stack<Double> rawPA = new Stack<>();
    private Stack<Double> rawGB = new Stack<>();
    private Stack<Double> rawPB = new Stack<>();
    private static int alarm = -1;
    private static int inicio = 0;
    private static double cambio = 0;

    @FXML
    private RadioButton simulationButton;
    @FXML
    private RadioButton twitterButton;
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
    private Button fase2Button;
    @FXML
    private Button fase3Button;
    @FXML
    private TextField cambioField;

    @FXML
    private void initialize(){
        fase2Button.setDisable(true);
        startButton.setDisable(true);
        twitterButton.setDisable(true);
        simulationButton.setToggleGroup(group);
        twitterButton.setToggleGroup(group);
        dataSerie.setName("tweets");
        dataSerie.setData(data);
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
        decFunChart.getData().addAll(gaSerie, gbSerie);

        alarmLabel.setText(""+alarm);




        topicTextField.setOnKeyReleased(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent keyEvent){
                if (topicTextField.getText() != ""){
                    twitterButton.setDisable(false);
                }
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        updateCharts();
                    }
                });
            }
        }, 0, 1000);

    }

    public void setTwitterCusum(){
        mainApp.setTwitter(topicTextField.getText());
        startButton.setDisable(false);
    }

    public void setPoissonCusum(){
        mainApp.setPoisson();
        startButton.setDisable(false);
    }

    public void start(){
        mainApp.startCusum();
        fase2Button.setDisable(false);
    }

    public void startFase2(){
        zonaIntercambio.setFase(2);
        inicio = data.size();
    }

    public void startFase3(){
        cusum.setMedio(Integer.parseInt(cambioField.getText()));
        zonaIntercambio.setFase(3);
    }

    private void updateCharts(){
        if (!rawData.isEmpty()){
            data.add(new XYChart.Data(data.size(), rawData.pop()));
        }
        if (!rawGA.isEmpty()){
            ga.add(new XYChart.Data(ga.size() +inicio, rawGA.pop()));
        }
        if (!rawGB.isEmpty()){
            gb.add(new XYChart.Data(gb.size() +inicio, rawGB.pop()));
        }
        if (!rawPA.isEmpty()){
            pa.add(new XYChart.Data(pa.size() +inicio, rawPA.pop()));
        }
        if (!rawPB.isEmpty()){
            pb.add(new XYChart.Data(pb.size() +inicio, rawPB.pop()));
        }
//        try {
//            data.add(new XYChart.Data(data.size(), rawData.pop()));
//        } catch (EmptyStackException e) {
////            System.out.println("No ha iniciado");
//        }

//        for (int i = 0; i < rawData.size(); i++) {
//            data.add(new XYChart.Data(i, rawData.get(i)));
//        }

//        for ( Double elem : rawData){
//            data.add(new XYChart.Data(rawData.indexOf(elem), elem));
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
    }

    public void updateCambio(double cambio){
        this.cambio = cambio;
    }
}
