package gunnerWeapons;

import utilities.MathUtils;

public class Revolver_Snipe extends Revolver {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/

	// Shortcut constructor to get baseline data
	public Revolver_Snipe() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Revolver_Snipe(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Revolver_Snipe(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "\"Bulldog\" Heavy Revolver (Slow RoF)";
	}
	
	@Override
	public Revolver_Snipe clone() {
		return new Revolver_Snipe(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "Revolver_Snipe";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	@Override
	protected double getRateOfFire() {
		return calculateAccurateRoF(getMaxRateOfFire());
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	/*
		I'm writing this method specifically because I know that the Revolver is never fired at max RoF -- it's used by the community as a sniper side-arm.
		
		I'm a bit worried that this is counter-intuitive in comparison to how the rest of the weapons are modeled, but I think this is a better approximation for how this weapon gets used in-game.
	*/
	private double calculateAccurateRoF(double maxRoF) {
		// Variables copied from estimatedAccuracy() to reverse-calculate the slow RoF needed for high accuracy
		double spreadPerShot = getSpreadPerShotValue();
		double spreadRecoverySpeed = 6.0;
		
		double recoilPitch = 130 * getRecoil();
		double recoilYaw = 10 * getRecoil();
		double mass = getMass();
		double springStiffness = 65;
		
		double v = Math.hypot(recoilPitch, recoilYaw);
		double w = Math.sqrt(springStiffness / mass);
		
		// These numbers are chosen arbitrarily.
		double desiredIncreaseInSpread = 2.5;
		double desiredIncreaseInRecoil = 3.0;
		
		double timeToRecoverSpread = (spreadPerShot - desiredIncreaseInSpread) / spreadRecoverySpeed;
		// This technically goes beyond the [-0.1, -0.001] range for this method, but I can't really be bothered to expand it beyond 20 segments...
		double timeToRecoverRecoil = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-w * desiredIncreaseInRecoil / v) / w;
		
		double longerTime = Math.max(timeToRecoverSpread, timeToRecoverRecoil);
		
		return Math.min(1.0 / longerTime, maxRoF);
	}
}
