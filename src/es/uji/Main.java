package es.uji;

import java.io.IOException;

import es.uji.cusumSpark.CusumSpark;
import es.uji.fuentesDatos.FuenteDatosTwitter;
import es.uji.fuentesDatos.ZonaIntercambioEventos;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import es.uji.view.Controller;

/**
 * Main que arranca la interfaz grafica
 * @author Alberto y Héctor
 *
 */
public class Main extends Application {

    private Stage primaryStage;   // Escenario principal
    private Parent rootLayout;    // layoutThread cusum, twitter;
    private Thread cusum, datos;
    private CusumSpark cusumSpark;
    private Runnable fuenteDatos;
    private final ZonaIntercambioEventos zonaIntercambio = new ZonaIntercambioEventos();


    /**
     * Constructor
     */
    public Main(){
        super();
    }

    /**
     * Metodo start
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            this.primaryStage.setTitle("CUSUM GUI");
            setRootLayout( "view/VentanaPrincipal.fxml" );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setter para root layout
     */
    public void setRootLayout(String escenaFxml){
        final Main m = this;
        final String escenaFxmlFinal = escenaFxml;

        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Main.class.getResource(escenaFxmlFinal) );
                    rootLayout = loader.load();

                    Controller controller = loader.getController();
                    if ( controller != null ) {
                        controller.setMain( m );
                        controller.setCusum( cusumSpark );
                    }

                    Scene scene = new Scene(rootLayout);
                    primaryStage.setScene(scene);
                    primaryStage.setMaximized(true);
                    primaryStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setTwitter(String[] args){
        fuenteDatos = new FuenteDatosTwitter( args, zonaIntercambio );
        datos = new Thread( fuenteDatos );
        datos.start();
        cusumSpark = new CusumSpark( "twitter", zonaIntercambio );
    }

    public void setPoisson(){
        cusumSpark = new CusumSpark( zonaIntercambio );
    }

    public void startCusum(){
        cusum = new Thread(cusumSpark);
        cusum.start();
        try {
            cusum.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}