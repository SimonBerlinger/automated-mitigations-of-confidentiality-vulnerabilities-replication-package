package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.Flight;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightStoring;

public class FlightStorage implements FlightStoring {
	
	public String componentId = "_Vt1ToKo4Ee6aj_SHkSg6ow";

    private Set<Flight> flights = new HashSet<>();

    @Override
    public void addToFlight(int flightId, PassengerDetails details) {
    	
    	Flight potentialFlight = null;
    	
    	for(Flight toSearch : flights) {
    		if(flightId == toSearch.getFlightId()) {
    			potentialFlight = toSearch;
    		}
    	}
    	
    	potentialFlight.addPassenger(details);
    }

    @Override
    public Collection<Flight> retrieveFlights(int airlineId) {
        return flights.stream().filter(flight -> flight.getAirlineId() == airlineId).collect(Collectors.toList());
    }

	@Override
	public void addFlight(Flight flight) {
		flights.add(flight);
		AirlineLogger.logStoring.storeLog("hello");
	}
}
