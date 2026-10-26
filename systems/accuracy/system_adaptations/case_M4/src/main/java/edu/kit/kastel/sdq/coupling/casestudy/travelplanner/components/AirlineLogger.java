package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineLogging;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;

public class AirlineLogger implements AirlineLogging {

	public String componentId = "_5gt3EBRrEey_turSZ8PcJQ";

	public static LogStoring logStoring = new LoggingDB();;

	public AirlineLogger() {

	}

	@Override
	public void log(Object entry) {
		logStoring.storeLog(entry);
	}
}
