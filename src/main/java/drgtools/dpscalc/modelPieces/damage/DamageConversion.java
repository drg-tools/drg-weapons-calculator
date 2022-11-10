package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.temperatureElement;

public class DamageConversion {
	private double percentageToConvert;
	private boolean addInsteadOfConvert;
	private boolean convertsToDamage;
	private damageElement damageConvertedToDamage;
	private boolean convertsToTemperature;
	private temperatureElement damageConvertedToTemperature;
	
	// Shortcut constructors for common use-cases.
	public DamageConversion(damageElement cnvrt) {
		this(0.5, false, true, cnvrt, false, null);
	}
	public DamageConversion(temperatureElement cnvrt) {
		this(0.5, false, false, null, true, cnvrt);
	}
	public DamageConversion(double percentage, boolean add, damageElement cnvrt) {
		this(percentage, add, true, cnvrt, false, null);
	}
	public DamageConversion(double percentage, boolean add, temperatureElement cnvrt) {
		this(percentage, add, false, null, true, cnvrt);
	}
	public DamageConversion(double percentage, boolean add, damageElement dmgCnvrt, temperatureElement tempCnvrt) {
		this(percentage, add, true, dmgCnvrt, true, tempCnvrt);
	}
	
	private DamageConversion(double percentage, boolean add, boolean damage, damageElement dmgCnvrt, boolean temperature, temperatureElement tempCnvrt) {
		percentageToConvert = percentage;
		addInsteadOfConvert = add;
		
		convertsToDamage = damage;
		damageConvertedToDamage = dmgCnvrt;
		
		convertsToTemperature = temperature;
		damageConvertedToTemperature = tempCnvrt;
	}
	
	public double getPercentage() {
		return percentageToConvert;
	}
	public boolean convertsInsteadOfAdds() {
		return !addInsteadOfConvert;
	}
	public boolean convertsToDamage() {
		return convertsToDamage;
	}
	public damageElement getDamageElement() {
		return damageConvertedToDamage;
	}
	public boolean convertsToTemperature() {
		return convertsToTemperature;
	}
	public temperatureElement getTemperatureElement() {
		return damageConvertedToTemperature;
	}
}
