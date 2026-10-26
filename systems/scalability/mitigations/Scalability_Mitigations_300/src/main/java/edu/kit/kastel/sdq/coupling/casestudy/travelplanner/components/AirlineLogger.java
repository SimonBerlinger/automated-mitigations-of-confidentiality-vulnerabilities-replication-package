package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;


import java.util.ArrayList;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "_5gt3EBRrEey_turSZ8PcJQ";
  
    private ArrayList<LogStoring> logStoringList;

    public AirlineLogger() {
       logStoringList = new ArrayList<>();
       for(int i = 1; i <= 1200; i++) {
       LoggingDB loggingDB = new LoggingDB();
       loggingDB.componentId = "_ID_LDB_" + i;       
       logStoringList.add(loggingDB);
       }
    }

    @Override
    public void log(Object entry) {
    	for(var logging: logStoringList) {
    		logging.storeLog(entry);
    	}
    }    
}
