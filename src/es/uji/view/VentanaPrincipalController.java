package es.uji.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
    final ScatterChart.Series<Integer,Double> mySerie = new XYChart.Series();
    private Stack<Double> rawData = new Stack<>();

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
    private void initialize(){
        startButton.setDisable(true);
        twitterButton.setDisable(true);
        simulationButton.setToggleGroup(group);
        twitterButton.setToggleGroup(group);
        mySerie.setName("tweets");
        mySerie.setData(data);
        dataChart.getData().add(mySerie);

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
                        updateChart();
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
    }

    private void updateChart(){

        try {
            data.add(new XYChart.Data(data.size(), rawData.pop()));
        } catch (EmptyStackException e) {
//            System.out.println("No ha iniciado");
        }

//        for (int i = 0; i < rawData.size(); i++) {
//            data.add(new XYChart.Data(i, rawData.get(i)));
//        }

//        for ( Double elem : rawData){
//            data.add(new XYChart.Data(rawData.indexOf(elem), elem));
//        }
    }

    public void update(Double data){
        rawData.add(data);
    }
}
