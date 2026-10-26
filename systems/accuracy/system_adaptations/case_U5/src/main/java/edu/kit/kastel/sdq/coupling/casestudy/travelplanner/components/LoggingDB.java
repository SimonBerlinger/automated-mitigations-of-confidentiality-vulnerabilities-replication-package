package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.Collection;
import java.util.HashSet;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.LogStoring;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.*;
public class LoggingDB implements LogStoring {

	public String componentId = "_ik_1gKpHEe6aj_SHkSg6ow";
	
	private Collection<String> logEntries = new HashSet<String>();

	@Override
	public void storeLog(Object entry) {
		logEntries.add("Booked with:" + entry);
		Main.fs.foo("text");
	}

}
