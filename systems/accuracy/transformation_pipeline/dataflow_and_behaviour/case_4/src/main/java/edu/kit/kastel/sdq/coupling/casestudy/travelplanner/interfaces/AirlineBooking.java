package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;

public interface AirlineBooking {

	//ccd_decl: UserAirline
    public boolean bookFlightOffer(int offerId, CreditCardDetails ccd_decl, PassengerDetails passengerDetails);
}
