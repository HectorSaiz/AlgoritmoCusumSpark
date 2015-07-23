package es.uji.view;

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

/**
 * Created by root on 23/07/15.
 */
public class VentanaPrincipalController extends Controller{

    final ToggleGroup group = new ToggleGroup();
    private ObservableList<ScatterChart.Series<Integer,Double>> data = FXCollections.observableArrayList();

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
        dataChart.setData(data);
    }

    public void setTwitterCusum(){
        //mainApp.setTwitter(topicTextField.getText());
    }

    public void setPoissonCusum(){
        mainApp.setPoisson();
    }

    public void start(){
        mainApp.startCusum();
        while(true){
            try{
                Thread.sleep(1000);
                updateChart();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    private void updateChart(){
        ScatterChart.Series<Integer,Double> mySerie = new ScatterChart.Series();
        List<Double> cusumData = cusum.getData();
        for ( Double elem : cusumData ){
            mySerie.getData().add(new ScatterChart.Data(cusumData.indexOf(elem),elem));
        }
        data.add(mySerie);

    }
}
