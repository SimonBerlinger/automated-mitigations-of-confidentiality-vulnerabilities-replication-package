package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes;

import java.util.Collection;
import java.util.HashSet;

public class Flight {

    public Flight(String from, String to, String date, int airlineId, int flightId) {
        super();
        this.from = from;
        this.to = to;
        this.date = date;
        this.airlineId = airlineId;
        this.flightId = flightId;
    }

    private final String from;

    private final String to;

    private final String date;
    
    private final int airlineId;

    private final Collection<PassengerDetails> passengers = new HashSet<>();
    
    private final int flightId;

    public int getFlightId() {
		return flightId;
	}

	public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDate() {
        return date;
    }

    public Collection<PassengerDetails> getUsers() {
        return passengers;
    }
    
    public void addPassenger(PassengerDetails passenger) {
    	passengers.add(passenger);
    }
    
    public int getAirlineId() {
    	return airlineId;
    }
}
