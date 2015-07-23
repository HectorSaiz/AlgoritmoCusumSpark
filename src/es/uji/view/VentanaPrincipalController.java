package es.uji.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 23/07/15.
 */
public class VentanaPrincipalController extends Controller{

    final ToggleGroup group = new ToggleGroup();
    private ObservableList<ScatterChart.Series<Integer,Double>> data = FXCollections.observableArrayList();
    private ScatterChart.Series<Integer,Double> mySerie = new ScatterChart.Series();
    private List<Double> rawData = new ArrayList<>();

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
        simulationButton.setToggleGroup(group);
        twitterButton.setToggleGroup(group);
        data.add(mySerie);
        dataChart.setData(data);
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        //System.out.println("Hola");
                        updateChart();
                    }
                });
            }
        }, 0, 1000);

    }

    public void setTwitterCusum(){
        //mainApp.setTwitter(topicTextField.getText());
    }

    public void setPoissonCusum(){
        mainApp.setPoisson();
    }

    public void start(){
        mainApp.startCusum();
    }

    private void updateChart(){
        mySerie.getData().clear();
        for ( Double elem : rawData){
            mySerie.getData().add(new ScatterChart.Data(rawData.indexOf(elem), elem));
        }
    }

    public void update(Double data){
        rawData.add(data);
    }
}
