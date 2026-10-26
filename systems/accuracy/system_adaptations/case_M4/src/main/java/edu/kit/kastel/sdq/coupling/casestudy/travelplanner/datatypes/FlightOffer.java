package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes;


public class FlightOffer {

    public final int id;

    public final int airlineId;

    public int cost;

    public CreditCardDetails paymentDetails;
    
    public final int flightId;

    public FlightOffer(int id, int airlineId, int cost, int flightId) {
        this.id = id;
        this.airlineId = airlineId;
        this.cost = cost;
        this.flightId = flightId;
    }

    public void addPaymentDetails(CreditCardDetails details) {
        this.paymentDetails = details;
    }
}
