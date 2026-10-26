package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.DiscountDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.Commission;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightQuery;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightQueryWithDiscount;

public class TravelAgency implements FlightQuery, Commission {
	
	public String componentId = "_ID_TA_1";

    public FlightQueryWithDiscount flightquerywithdiscount;

    private DiscountDetails discount;

    private String agencyName;

    public TravelAgency(FlightQueryWithDiscount flightquerywithdiscount) {
        this.flightquerywithdiscount = flightquerywithdiscount;
        agencyName = "BestTravel";
        this.discount = new DiscountDetails("1", agencyName, 2);
    }

    @Override
    public Collection<FlightOffer> getFlightOffers(RequestData requestData) {
        return flightquerywithdiscount.getFlightOffers(requestData, discount);
    }

    @Override
    public boolean payCommission() {
        return true;
    }
}
