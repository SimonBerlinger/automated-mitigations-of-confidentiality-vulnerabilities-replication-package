package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.ArrayList;
import java.util.Collection;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.DiscountDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.Flight;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineBooking;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.Commission;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightQueryWithDiscount;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightStoring;

public class Airline implements FlightQueryWithDiscount, AirlineBooking {
	
	public String componentId = "_HRAO0MOAEeWst9mTsticNA";

    public Commission commission;

    public AirlineLogger airlinelogging1;
    public AirlineLogger airlinelogging2;

    private Collection<FlightOffer> flightoffers;

    private final int airlineId;

    public FlightStoring flightstoring;

    private int lastOfferId = 0;

    public Airline(Commission commission, int airlineId, FlightStoring flightstoring) {
        this.commission = commission;
        this.airlinelogging1 = new AirlineLogger();
        airlinelogging1.componentId = "ID_AL_1";
        this.airlinelogging2 = new AirlineLogger();
        airlinelogging2.componentId = "ID_AL_2";
        this.airlineId = airlineId;
        this.flightstoring = flightstoring;
        flightoffers = new ArrayList<FlightOffer>();
        flightstoring.addFlight(new Flight("Frankfurt", "New York", "Today", airlineId, 1));
    }

    @Override
    public Collection<FlightOffer> getFlightOffers(RequestData requestData, DiscountDetails discount) {
        //airlinelogging.log(requestData.toString());
    	//Flow: (Airline::getFlightOffers.details (P) -> AirlineLogger::log.entry[DiscountDetails] (P), TravelAgencyAirline -> Support, Multiple, Effect)
    	airlinelogging1.log(discount);
    	airlinelogging2.log(discount);
        Collection<FlightOffer> fittingOffers = new ArrayList<>();
        Collection<Flight> flights = flightstoring.retrieveFlights(airlineId);
        for (Flight flight : flights) {
            if (flight.getTo().equals(requestData.destination) && flight.getDate().equals(requestData.date)) {
            	FlightOffer flightOffer = new FlightOffer(lastOfferId++, airlineId, 10, flight.getFlightId());
                flightoffers.add(flightOffer);
                fittingOffers.add(flightOffer);
            }
        }
        return fittingOffers;
    }

    @Override
    public boolean bookFlightOffer(int offerId, CreditCardDetails ccd_decl, PassengerDetails passengerDetails) {
    	//Flow: (ccd_decl (P) -> log.entry (P), UserAirline -> Support, MultipleFlows, Effect)  
        airlinelogging1.log(ccd_decl);
        airlinelogging2.log(ccd_decl);
        processBooking(offerId, passengerDetails);
        commission.payCommission();
        return true;
    }

    public void setCommission(Commission commission) {
        this.commission = commission;
    }

    private void processBooking(int offerId, PassengerDetails passengerDetails) {
        for (FlightOffer offer : flightoffers) {
            if (offer.id == offerId) {
            	// Flow: (Airline::processBooking.passengerDetails (P) -> FlightStorage::addToFlight.details (P), User -> Airline, Single, NoEffect)
                flightstoring.addToFlight(offer.flightId, passengerDetails);
            }
        }
    }
}
