package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;

public interface DeclassificationConfirmation {

    public boolean confirmRelease(CreditCardDetails ccd, int airlineId);
}
