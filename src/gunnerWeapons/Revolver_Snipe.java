package gunnerWeapons;

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
		double spreadPerShot = 129;
		double spreadRecoverySpeed = 109.1390954;
		double recoilPerShot = 155;
		
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 6.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 1.0;
		
		// Elephant Rounds significantly reduces the recoil speeds in addition to increasing recoil per shot
		double SpSModifier = getSpreadPerShot();
		if (selectedOverclock == 4) {
			
			if (selectedTier2 != 1) {
				// And if Floating Barrel isn't equipped, then the Spread per Shot takes it to Max Spread on first shot for some reason?
				spreadPerShot = 389;
				SpSModifier = 1.0;
				
			}
			
			recoilUpInterval = 16.0 / 60.0;
			recoilDownInterval = 140.0 / 60.0;
		}
		
		// These numbers are chosen arbitrarily.
		double desiredIncreaseInSpread = 52;
		double desiredIncreaseInRecoil = 62;
		
		double timeToRecoverSpread = (spreadPerShot * SpSModifier - desiredIncreaseInSpread) / (spreadRecoverySpeed * getSpreadRecoverySpeed());
		double timeToRecoverRecoil = recoilUpInterval + (recoilPerShot * getRecoil() - desiredIncreaseInRecoil) * recoilDownInterval / (recoilPerShot * getRecoil());
		
		double longerTime = Math.max(timeToRecoverSpread, timeToRecoverRecoil);
		
		return Math.min(1.0 / longerTime, maxRoF);
	}
}
