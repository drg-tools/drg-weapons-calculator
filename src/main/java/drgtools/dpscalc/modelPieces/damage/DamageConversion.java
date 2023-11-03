package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;

public class DamageConversion {
	private double percentageToConvert;
	private boolean addInsteadOfConvert;
	private DamageElement elementConvertedTo;
	// Shortcut constructor for common use-cases.
	public DamageConversion(DamageElement cnvrt) {
		this(0.5, false, cnvrt);
	}
	public DamageConversion(double percentage, boolean add, DamageElement dmgCnvrt) {
		percentageToConvert = percentage;
		addInsteadOfConvert = add;
		elementConvertedTo = dmgCnvrt;
	}
	
	public double getPercentage() {
		return percentageToConvert;
	}
	public boolean convertsInsteadOfAdds() {
		return !addInsteadOfConvert;
	}
	public DamageElement getConvertedElement() {
		return elementConvertedTo;
	}

}
