package es.uji.view;

import es.uji.Main;
import es.uji.cusumSpark.CusumSpark;

/**
 * Clase controlador
 * @author Alberto y Héctor
 *
 */
public abstract class Controller {
	
	protected Main mainApp;         // Main
	protected CusumSpark cusum;
	
	/**
	 * Setter del main y getor de fuentes
	 * @param mainApp
	 */
	public void setMain(Main mainApp){
		this.mainApp = mainApp;
	}

	public void setCusum(CusumSpark cusum) { this.cusum = cusum; }
	
}