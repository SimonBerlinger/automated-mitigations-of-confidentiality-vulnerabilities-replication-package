package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import java.util.Collection;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.AirlineBooking;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.Declassification;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightBooking;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.FlightQuery;

public class TravelPlanner implements FlightQuery, FlightBooking {

	public String componentId = "_2pBlYMN-EeWst9mTsticNA";

	public FlightQuery flightquery;

	public Declassification declassification;

	public AirlineBooking airlinebooking;

	private PassengerDetails passengerdetails;

	public TravelPlanner(FlightQuery flightquery, Declassification declassification, AirlineBooking airlinebooking,
			PassengerDetails passengerdetails) {
		this.flightquery = flightquery;
		this.declassification = declassification;
		this.airlinebooking = airlinebooking;
		this.passengerdetails = passengerdetails;
	}

	@Override
	public Collection<FlightOffer> getFlightOffers(RequestData requestData) {
		return flightquery.getFlightOffers(requestData);
	}

	@Override
	public boolean bookSelected(FlightOffer flightOffer) {
		// Flow by missing declassification: (CreditCardCenter.UserDetails (V) ->
		// Airline::bookFlightOffer.ccd_decl (P), User -> UserAirline, Single, Effect
		// (?))
		CreditCardDetails ccd_decl = declassification.releaseCCD(flightOffer.airlineId);

		// Startpoint for flow TravelPlanner.PassengerDetails ->
		// FlightStorage::addToFlight.details
		// Story: PassengerDetails may be used to calculate costs, but must not be
		// stored
		PassengerDetails details = null;

		if (this.passengerdetails != null) {
			details = this.passengerdetails;
		} else {
			details = new PassengerDetails("Max", "Templateman", 20);
		}
		foo("text");
		return airlinebooking.bookFlightOffer(flightOffer.id, ccd_decl, this.passengerdetails);
	}

	public void foo() {

	}

	public void foo(String text) {

	}
	
	public String bar() {
		return "text";
	}
}
