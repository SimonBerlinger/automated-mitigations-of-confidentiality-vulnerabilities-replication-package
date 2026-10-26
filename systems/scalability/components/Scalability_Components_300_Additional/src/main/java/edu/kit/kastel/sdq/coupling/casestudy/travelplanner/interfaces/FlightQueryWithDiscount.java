package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.DiscountDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;

public interface FlightQueryWithDiscount {

	//discount: Airline;TravelAgency
    public Collection<FlightOffer> getFlightOffers(RequestData requestData, DiscountDetails discount);
}
