package edu.kit.kastel.sdq.coupling.casestudy.travelplanner.components;

import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.datatypes.CreditCardDetails;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.Declassification;
import edu.kit.kastel.sdq.coupling.casestudy.travelplanner.interfaces.DeclassificationConfirmation;

public class CreditCardCenter implements Declassification {
	
	public String componentId = "_XOyEsMOBEeWst9mTsticNA";

    public DeclassificationConfirmation declassificationconfirmation;

    private CreditCardDetails ccd;

    public CreditCardCenter(DeclassificationConfirmation declassificationconfirmation) {
        this.declassificationconfirmation = declassificationconfirmation;
    }

    @Override
    public CreditCardDetails releaseCCD(int airlineId) {
        if (declassificationconfirmation.confirmRelease(ccd, airlineId)) {
            return ccd;
        } else {
            return null;
        }
    }

    public void setDetails(CreditCardDetails ccd) {
        this.ccd = ccd;
    }

    public void setDeclassificationConfirmation(DeclassificationConfirmation confirmation) {
        this.declassificationconfirmation = confirmation;
    }
}
