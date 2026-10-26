package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;


import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.Main;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "_5gt3EBRrEey_turSZ8PcJQ";
  
    private LogStoring logStoring;

    public AirlineLogger() {
       logStoring = new LoggingDB();
    }

    @Override
    public void log(Object entry) {
    	logStoring.storeLog(entry);
    	Main.fs.foo("text");
    }    
}
