package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;


import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "ID_AL_1";
  
    public LogStoring logStoring;    

    public AirlineLogger() {
       logStoring = new LoggingDB();       
    }

    @Override
    public void log(Object entry) {
    	logStoring.storeLog(entry);
    }    
}
