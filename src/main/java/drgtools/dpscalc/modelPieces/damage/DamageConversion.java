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

	// TODO: these prettyPrints might not be necessary?
	public String prettyPrint(){
		return prettyPrint(0);
	}
	public String prettyPrint(int indentLevel) {
		String indent = "    ";
		String toReturn = "";

		if (addInsteadOfConvert) {
			toReturn += indent.repeat(indentLevel) + "Adds " + percentageToConvert * 100.0 + "% of the Base Element as " + DamageElements.prettyPrint(elementConvertedTo) + " (both Damage and RadialDamage)\n";
		}
		else {
			toReturn += indent.repeat(indentLevel) + "Converts " + percentageToConvert * 100.0 + "% of the Base Element to " + DamageElements.prettyPrint(elementConvertedTo) + " (both Damage and RadialDamage)\n";
		}

		return toReturn;
	}
}
