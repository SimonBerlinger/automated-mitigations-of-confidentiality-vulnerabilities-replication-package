package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.Declassification;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.DeclassificationConfirmation;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightBooking;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightQuery;

public class UserInterface implements FlightQuery, DeclassificationConfirmation, FlightBooking {
	
	public String componentId = "_mz-sgKpJEe6aj_SHkSg6ow";

    public FlightQuery flightquery;

    public Declassification declassification;

    public FlightBooking flightbooking;
    
    private AirlineLogger logger;

    public UserInterface(FlightQuery flightquery, Declassification declassification, FlightBooking flightbooking, AirlineLogger logger) {
        this.flightquery = flightquery;
        this.declassification = declassification;
        this.flightbooking = flightbooking;
        this.logger = logger;
    }

    @Override
    public Collection<FlightOffer> getFlightOffers(RequestData requestData) {
    	logger.foo("hello");
        return flightquery.getFlightOffers(requestData);
    }

    @Override
    public boolean confirmRelease(CreditCardDetails ccd, int airlineId) {
        return true;
    }

    @Override
    public boolean bookSelected(FlightOffer flightOffer) {
        return flightbooking.bookSelected(flightOffer);
    }

    public void setFlightQuery(FlightQuery query) {
        this.flightquery = query;
    }

    public void setFlightBooking(FlightBooking flightBooking) {
        this.flightbooking = flightBooking;
    }
}
