package gunnerWeapons;

import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;

public class BurstPistol extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int directDamage;
	private int burstSize;
	private double delayBetweenBulletsDuringBurst;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double armorBreakChance;
	private double spreadPerShot;
	private double recoil;
	private double weakpointBonusDamage;
	private int burstBonusDamage;
	private int burstStunDuration;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public BurstPistol() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public BurstPistol(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public BurstPistol(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "BRT7 Burst Fire Gun";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 14;
		burstSize = 3;
		delayBetweenBulletsDuringBurst = 0.15;  // TODO: Need to do more testing to confirm this.
		carriedAmmo = 144;
		magazineSize = 18;
		rateOfFire = 2.5;
		reloadTime = 2.2;
		armorBreakChance = 0.7;
		spreadPerShot = 1.0;
		recoil = 1.0;
		weakpointBonusDamage = 0.0;
		burstBonusDamage = 0;
		burstStunDuration = 0;
		
		initializeModsAndOverclocks();
		// Grab initial values before customizing mods and overclocks
		setBaselineStats();
		
		// Selected Mods
		selectedTier1 = mod1;
		selectedTier2 = mod2;
		selectedTier3 = mod3;
		selectedTier4 = mod4;
		selectedTier5 = mod5;
		
		// Overclock slot
		selectedOverclock = overclock;
	}
	
	@Override
	protected void initializeModsAndOverclocks() {
		tier1 = new Mod[3];
		tier1[0] = new Mod("", "", 1, 0);
		tier1[1] = new Mod("", "", 1, 1);
		tier1[2] = new Mod("", "", 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("", "", 2, 0);
		tier2[1] = new Mod("", "", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("", "", 3, 0);
		tier3[1] = new Mod("", "", 3, 1);
		tier3[2] = new Mod("", "", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("", "", 4, 0);
		tier4[1] = new Mod("", "", 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("", "", 5, 0, false);
		tier5[1] = new Mod("", "", 5, 1, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "", "", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "", "", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "", "", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "", "", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "", "", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "", "", 5, false);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Weapon clone() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public StatsRow[] getStats() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double calculateBurstDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateSustainedDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int calculateMaxNumTargets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateFiringDuration() {
		// TODO Auto-generated method stub
		return 0;
	}
}
