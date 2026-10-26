package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes;

public class RequestData {

    public String destination;

    public String date;

    public String userName;

    public RequestData(String destination, String date, String userName) {
        this.destination = destination;
        this.date = date;
        this.userName = userName;
    }

    @Override
    public String toString() {
        return String.format("Flights For User %s with destination %s at the %s", userName, destination, date);
    }
}
