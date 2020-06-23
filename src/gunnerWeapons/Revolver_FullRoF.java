package gunnerWeapons;

public class Revolver_FullRoF extends Revolver {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/

	// Shortcut constructor to get baseline data
	public Revolver_FullRoF() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Revolver_FullRoF(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Revolver_FullRoF(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "\"Bulldog\" Heavy Revolver (Full RoF)";
	}
	
	@Override
	public Revolver_FullRoF clone() {
		return new Revolver_FullRoF(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	protected double getRateOfFire() {
		return getMaxRateOfFire();
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
}
