package edu.kit.kastel.sdq.coupling.casestudy.travelplanner;

import java.util.ArrayList;

public interface HasFooMethod {

	public String foo(String text);
	
	public default void callFoo(ArrayList<HasFooMethod> list) {
		for(HasFooMethod item: list) {
			item.foo("Text");
		}
	}
}
