package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes;

public class PassengerDetails {

	private String firstName;
	private String lastName;
	private int age;
	
	public PassengerDetails(String firstName, String lastName, int age) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public int getAge() {
		return age;
	}
}
