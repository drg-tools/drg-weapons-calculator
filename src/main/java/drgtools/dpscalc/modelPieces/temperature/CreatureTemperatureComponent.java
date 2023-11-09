package drgtools.dpscalc.modelPieces.temperature;

public class CreatureTemperatureComponent {
	private double updateTime = 1.0;
	private double temperatureChangeScale = 1.0;
	private double tempShockActivationWindow = 10.0;

	// For when Temperature > 0 (warmed/heated)
	private double maxTemperature = 100.0;
	private double burnTemperature;
	private double douseTemperature;
	private double coolingRate;
	private boolean dieIfOnFire = false;
	private double onFireHeatRange;

	// For when Temperature < 0 (chilled)
	private double minTemperature;
	private double freezeTemperature;
	private double unfreezeTemperature;
	private double warmingRate;
	private double warmingCooldown;  // bypassed if the enemy gets Frozen.
	private double maxColdSlowdown = 0.25;
	private boolean dieIfFrozen = false;
	private double frozenDamageBonus = 3.0;
	
	public CreatureTemperatureComponent(double burnTemp, double douseTemp, double coolRate, double heatRange,
										double freezeTemp, double unfreezeTemp, double warmRate, double warmCD) {
		burnTemperature = burnTemp;
		douseTemperature = douseTemp;
		coolingRate = coolRate;
		onFireHeatRange = heatRange;
		minTemperature = freezeTemp;
		freezeTemperature = freezeTemp;
		unfreezeTemperature = unfreezeTemp;
		warmingRate = warmRate;
		warmingCooldown = warmCD;
	}

	public void setUpdateTime(double in) {
		updateTime = in;
	}
	public void setTempChangeScale(double in) {
		temperatureChangeScale = in;
	}
	public void setTempShockActivationWindow(double in) {
		tempShockActivationWindow = in;
	}
	public void setDieOnFire(boolean in) {
		dieIfOnFire = in;
	}
	public void setMaxColdSlowdown(double in) {
		maxColdSlowdown = in;
	}
	public void setDieFrozen(boolean in) {
		dieIfFrozen = in;
	}
	public void setFrozenBonus(double in) {
		frozenDamageBonus = in;
	}

	public double getEffectiveTempShockActivationWindow() {
		// TODO: is this affected? test on Elites maybe...
		return tempShockActivationWindow / temperatureChangeScale;
	}

	public double getEffectiveMaxTemperature() {
		return maxTemperature / temperatureChangeScale;
	}
	public double getEffectiveBurnTemperature() {
		return burnTemperature / temperatureChangeScale;
	}
	public double getEffectiveDouseTemperature() {
		return douseTemperature / temperatureChangeScale;
	}
	public double getCoolingRate() {
		return coolingRate;
	}
	public boolean diesIfOnFire() {
		return dieIfOnFire;
	}
	public double getOnFireHeatRange() {
		return onFireHeatRange;
	}

	public double getEffectiveMinTemperature() {
		return minTemperature / temperatureChangeScale;
	}
	public double getEffectiveFreezeTemperature() {
		return freezeTemperature / temperatureChangeScale;
	}
	public double getEffectiveUnfreezeTemperature() {
		return unfreezeTemperature / temperatureChangeScale;
	}
	public double getWarmingRate() {
		return warmingRate;
	}
	public double getWarmingCooldown() {
		return warmingCooldown;
	}
	public double getMaxColdSlowdown() {
		return maxColdSlowdown;
	}
	public boolean diesIfFrozen() {
		return dieIfFrozen;
	}
	public double getFrozenDamageBonus() {
		return frozenDamageBonus;
	}
}
