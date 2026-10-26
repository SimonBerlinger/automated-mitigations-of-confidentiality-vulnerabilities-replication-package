package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.Flight;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;

public interface FlightStoring {

	//details: Airline
    public void addToFlight(int flightId, PassengerDetails details);

    public Collection<Flight> retrieveFlights(int airlineId);
    
    public void addFlight(Flight flight);
}
