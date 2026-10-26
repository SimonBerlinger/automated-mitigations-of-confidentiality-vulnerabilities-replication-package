package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;


import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "_5gt3EBRrEey_turSZ8PcJQ";
  
    private LoggingDB logStoring1;
    private LoggingDB logStoring2;

    public AirlineLogger() {
       logStoring1 = new LoggingDB();
       logStoring1.componentId = "ID_LDB_1";
       logStoring2 = new LoggingDB();
       logStoring2.componentId = "ID_LDB_2";
    }

    @Override
    public void log(Object entry) {
    	logStoring1.storeLog(entry);
    	logStoring2.storeLog(entry);
    }    
}
