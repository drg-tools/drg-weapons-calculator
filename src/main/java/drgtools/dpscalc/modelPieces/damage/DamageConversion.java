package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;

public class DamageConversion {
	private double percentageToConvert;
	private boolean addInsteadOfConvert;
	private boolean convertsToDamage;
	private DamageElement damageConvertedToDamage;
	private boolean convertsToTemperature;
	private TemperatureElement damageConvertedToTemperature;
	
	// Shortcut constructors for common use-cases.
	public DamageConversion(DamageElement cnvrt) {
		this(0.5, false, true, cnvrt, false, null);
	}
	public DamageConversion(TemperatureElement cnvrt) {
		this(0.5, false, false, null, true, cnvrt);
	}
	public DamageConversion(double percentage, boolean add, DamageElement cnvrt) {
		this(percentage, add, true, cnvrt, false, null);
	}
	public DamageConversion(double percentage, boolean add, TemperatureElement cnvrt) {
		this(percentage, add, false, null, true, cnvrt);
	}
	public DamageConversion(double percentage, boolean add, DamageElement dmgCnvrt, TemperatureElement tempCnvrt) {
		this(percentage, add, true, dmgCnvrt, true, tempCnvrt);
	}
	
	private DamageConversion(double percentage, boolean add, boolean damage, DamageElement dmgCnvrt, boolean temperature, TemperatureElement tempCnvrt) {
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
	public DamageElement getDamageElement() {
		return damageConvertedToDamage;
	}
	public boolean convertsToTemperature() {
		return convertsToTemperature;
	}
	public TemperatureElement getTemperatureElement() {
		return damageConvertedToTemperature;
	}
}
