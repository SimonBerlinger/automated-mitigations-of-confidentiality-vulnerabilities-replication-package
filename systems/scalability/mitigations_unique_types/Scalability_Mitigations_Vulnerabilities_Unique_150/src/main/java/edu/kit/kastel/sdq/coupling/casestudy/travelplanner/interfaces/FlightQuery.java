package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;

public interface FlightQuery {

    public Collection<FlightOffer> getFlightOffers(RequestData requestData);
}
