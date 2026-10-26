package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;

public interface Declassification {

    public CreditCardDetails releaseCCD(int airlineId);
}
