package org.gridlab.gat.resource.gt4;

import java.util.List;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareResource;

public class GlobusSoftwareResource extends SoftwareResource{

	private String NODE_NAME;
	
	
	public GlobusSoftwareResource(){
	/*Questo costruttore dovra ricevere un elemento di JDOM
	 * che in pratica sara l'HOST, con il solo campo Operatyng System. 
	 * Da questo elemento si otterranno tutti i valori per le varibili
	 * di questa classe e per le variabili di classe di SoftwareResource.
	 * */	
	}
	
	public Reservation getReservation() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceDescription getResourceDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addMetricListener(MetricListener metricListener, Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		
	}

	public MetricEvent getMeasurement(Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public MetricDefinition getMetricDefinitionByName(String name) throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MetricDefinition> getMetricDefinitions() throws GATInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeMetricListener(MetricListener metricListener, Metric metric) throws GATInvocationException {
		// TODO Auto-generated method stub
		
	}

	public String marshal() {
		// TODO Auto-generated method stub
		return null;
	}

}