package es.uji.view;

import es.uji.Main;
import es.uji.cusumSpark.CusumSpark;
import es.uji.fuentesDatos.ZonaIntercambioEventos;

/**
 * Clase controlador
 * @author Alberto y Héctor
 *
 */
public abstract class Controller {
	
	protected Main mainApp;         // Main
	protected ZonaIntercambioEventos zonaIntercambio;
	protected CusumSpark cusum;
	
	/**
	 * Setter del main y getor de fuentes
	 * @param mainApp
	 */
	public void setMain(Main mainApp){
		this.mainApp = mainApp;
	}

	public void setZonaIntercambio(ZonaIntercambioEventos zonaIntercambio){ this.zonaIntercambio = zonaIntercambio; }

	public void setCusum(CusumSpark cusum){
		this.cusum = cusum;
	}

	public abstract void update(double data);

	public abstract void updateDecisions(double pa, double ga, double pb, double gb);

	public abstract void updateAlarma(int alarma);

	public abstract void updateCambio(double cambio);
	
}