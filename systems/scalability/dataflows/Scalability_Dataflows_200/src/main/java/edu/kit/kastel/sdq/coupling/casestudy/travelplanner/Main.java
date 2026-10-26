package edu.kit.kastel.sdq.coupling.casestudy.travelplanner;

import java.util.ArrayList;
import java.util.Collection;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.Airline;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.CreditCardCenter;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.FlightStorage;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.TravelAgency;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.TravelPlanner;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components.UserInterface;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.Flight;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.FlightOffer;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.PassengerDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.RequestData;

public class Main {

	private static UserInterface ui;

	public static void main(String[] args) {
		
		ArrayList<HasFooMethod> storages = new ArrayList<>();
		ArrayList<HasFooMethod> agencies = new ArrayList<>();
		ArrayList<HasFooMethod> planners = new ArrayList<>();

		for (int i = 1; i <= 101; i++) {
			String fromData = "from-" + System.currentTimeMillis();
			String toData = "from-" + System.currentTimeMillis();

			FlightStorage storage = new FlightStorage();
			storages.add(storage);
			storage.componentId = "_ID_FS_" + i;
			
			storage.addFlight(new Flight(fromData, toData, "today", 0, 0));
			Airline airline = new Airline(null, 0, storage);
			
			TravelAgency travelAgency = new TravelAgency(airline);
			agencies.add(travelAgency);
			travelAgency.componentId = "_ID_TA_" + i;
			
			airline.setCommission(travelAgency);
			CreditCardCenter creditCardCenter = new CreditCardCenter(null);
			
			TravelPlanner planner = new TravelPlanner(travelAgency, creditCardCenter, airline,
					new PassengerDetails("Max", "Templateman", 20));
			planners.add(planner);
			planner.componentId = "_ID_TP_" + i;
			
			ui = new UserInterface(planner, creditCardCenter, planner);
			creditCardCenter.setDeclassificationConfirmation(ui);
			//startSzenario(new RequestData(toData, "today", "Max Templatemann"));
		}
		
		storages.get(0).callFoo(agencies);
	}

	private static void startSzenario(RequestData requestData) {

		Collection<FlightOffer> offers = ui.getFlightOffers(requestData);
		ui.bookSelected(offers.iterator().next());		
	}
}
