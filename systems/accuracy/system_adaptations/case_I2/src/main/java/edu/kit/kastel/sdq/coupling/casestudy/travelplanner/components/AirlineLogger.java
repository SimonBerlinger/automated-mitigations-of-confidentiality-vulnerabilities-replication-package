package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;


import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "_5gt3EBRrEey_turSZ8PcJQ";
  
    private LogStoring logStoring;
    private LogStoring logStoring2;

    public AirlineLogger() {
       logStoring = new LoggingDB();
       logStoring2 = new DifferentLoggingDB();
    }

    @Override
    public void log(Object entry) {
    	logStoring.storeLog(entry);
    	logStoring2.storeLog(entry);
    }    
}
