package gunnerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;

public class Revolver extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int areaDamage;
	private double aoeRadius;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	private int maxPenetrations;
	private double weakpointBonus;
	private double baseSpread;
	private double spreadPerShot;
	private double recoil;
	
	private int numberOfTargets;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/

	// Shortcut constructor to get baseline data
	public Revolver() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Revolver(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Revolver(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Bulldog\" Heavy Revolver";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 50.0;
		areaDamage = 0;
		aoeRadius = 0.0;  // meters
		carriedAmmo = 28;
		magazineSize = 4;
		rateOfFire = 2.0;  // bullets per second
		reloadTime = 2.0;  // seconds
		stunChance = 0.5;
		stunDuration = 1.5;  // seconds
		maxPenetrations = 0;
		weakpointBonus = 0.15;
		baseSpread = 1.0;
		spreadPerShot = 1.0;
		recoil = 1.0;
		
		numberOfTargets = 1;
		
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
		tier1 = new Mod[2];
		tier1[0] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 1, 0);
		tier1[1] = new Mod("Perfect Weight Balance", "Improved Accuracy", 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 2, 0);
		tier2[1] = new Mod("Floating Barrel", "Sweet, sweet optimization. We called in a few friends and managed to significantly improve the stability of this gun.", 2, 1);
		tier2[2] = new Mod("Expanded Ammo Bags", "Expanded Ammo Bags", 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Super Blowthrough Rounds", "Shaped projectiles capable to over-penetrate targets with a mininal loss of energy. In other words: Fire straight through several enemies at once!", 3, 0);
		tier3[1] = new Mod("Explosive Rounds", "Bullet detonates creating a radius of damage but deals less direct damage.", 3, 1);
		tier3[2] = new Mod("Hollow-Point Bullets", "Hit 'em where it hurts! Literally! We've upped the damage you'll be able to do to any creature's fleshy bits. You're welcome.", 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Dead-Eye", "No aim penalty while moving", 5, 0, false);
		tier5[1] = new Mod("Glyphid Neurotoxin Coating", "Chance to poison your target. Affected creatures move slower and take damage over time.", 5, 1, false);  // It looks like whenever this procs for the main target, all splash targets get it too, instead of RNG/enemy.
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "More damage on average but it's a bit inconsistent.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a chnace to ricochet into a nearby enemy.", 1, false);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Feather Trigger", "Less weight means you can squeeze out more bullets faster than you can say \"Recoil\" but the stability of the weapon is reduced.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Five Shooter", "An updated casing profile lets you squeeze one more round into the cylinder and increases the maximum rate of fire, but all that filling and drilling has compromised the pure damage output of the weapon.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Elephant Rounds", "Heavy tweaking has made it possible to use modified autocannon rounds in the revolver! The damage is crazy but so is the recoil and you can't carry very many rounds.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Magic Bullets", "Smaller bouncy bullets ricochet off hard surfaces and hit nearby enemies like magic and you can carry a few more due to their compact size. However the overall damage of the weapon is reduced.", 5);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean combinationIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			combinationIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					combinationIsValid = false;
				}
			}
			if (symbols[0] == 'C') {
				System.out.println("Revolver's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Revolver's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("Revolver's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
				combinationIsValid = false;
			}
		}
		
		if (combinationIsValid) {
			switch (symbols[0]) {
				case '-': {
					selectedTier1 = -1;
					break;
				}
				case 'A': {
					selectedTier1 = 0;
					break;
				}
				case 'B': {
					selectedTier1 = 1;
					break;
				}
			}
			
			switch (symbols[1]) {
				case '-': {
					selectedTier2 = -1;
					break;
				}
				case 'A': {
					selectedTier2 = 0;
					break;
				}
				case 'B': {
					selectedTier2 = 1;
					break;
				}
				case 'C': {
					selectedTier2 = 2;
					break;
				}
			}
			
			switch (symbols[2]) {
				case '-': {
					selectedTier3 = -1;
					break;
				}
				case 'A': {
					selectedTier3 = 0;
					break;
				}
				case 'B': {
					selectedTier3 = 1;
					break;
				}
				case 'C': {
					selectedTier3 = 2;
					break;
				}
			}
			
			switch (symbols[3]) {
				case '-': {
					selectedTier4 = -1;
					break;
				}
				case 'A': {
					selectedTier4 = 0;
					break;
				}
				case 'B': {
					selectedTier4 = 1;
					break;
				}
			}
			
			switch (symbols[4]) {
				case '-': {
					selectedTier5 = -1;
					break;
				}
				case 'A': {
					selectedTier5 = 0;
					break;
				}
				case 'B': {
					selectedTier5 = 1;
					break;
				}
			}
			
			switch (symbols[5]) {
				case '-': {
					selectedOverclock = -1;
					break;
				}
				case '1': {
					selectedOverclock = 0;
					break;
				}
				case '2': {
					selectedOverclock = 1;
					break;
				}
				case '3': {
					selectedOverclock = 2;
					break;
				}
				case '4': {
					selectedOverclock = 3;
					break;
				}
				case '5': {
					selectedOverclock = 4;
					break;
				}
				case '6': {
					selectedOverclock = 5;
					break;
				}
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Revolver clone() {
		return new Revolver(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private double getDirectDamage() {
		double toReturn = directDamage;
		// Start by adding flat damage bonuses
		if (selectedTier2 == 0) {
			toReturn += 15.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 15.0;
		}
		if (selectedOverclock == 5) {
			toReturn -= 20.0;
		}
			
		// Then do multiplicative bonuses
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		if (selectedOverclock == 0) {
			// Since this ranges from 80% to 140% damage, I'll just average it out to 110%.
			toReturn *= 1.1;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 2.0;
		}
		return toReturn;
	}
	private int getAreaDamage() {
		int toReturn = areaDamage;
		if (selectedTier3 == 1) {
			toReturn += 30;
		}
		return toReturn;
	}
	private double getAoERadius() {
		double toReturn = aoeRadius;
		if (selectedTier3 == 1) {
			toReturn += 1.5;
		}
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier2 == 2) {
			toReturn += 12;
		}
		if (selectedTier4 == 0) {
			toReturn += 12;
		}
		if (selectedOverclock == 3) {
			toReturn += 5;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 12;
		}
		else if (selectedOverclock == 5) {
			toReturn += 8;
		}
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedOverclock == 3) {
			toReturn += 1;
		}
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		if (selectedOverclock == 2) {
			toReturn += 4.0;
		}
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		if (selectedTier1 == 0) {
			toReturn -= 0.4;
		}
		return toReturn;
	}
	private int getMaxPenetrations() {
		int toReturn = maxPenetrations;
		if (selectedTier3 == 0) {
			toReturn += 3;
		}
		return toReturn;
	}
	private int getMaxRicochets() {
		if (selectedOverclock == 1 || selectedOverclock == 5) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		if (selectedTier3 == 2) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		if (selectedTier1 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 0.15;
		}
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = spreadPerShot;
		if (selectedTier2 == 1) {
			toReturn -= 0.8;
		}
		if (selectedOverclock == 4) {
			toReturn += 1.0;
		}
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = recoil;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.75;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 1.5;
		}
		else if (selectedOverclock == 4) {
			toReturn += 1.5;
		}
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || (selectedOverclock > 2 && selectedOverclock < 6);
		toReturn[0] = new StatsRow("Damage:", "" + getDirectDamage(), directDamageModified);
		
		toReturn[1] = new StatsRow("Area Damage:", "" + getAreaDamage(), selectedTier3 == 1);
		
		toReturn[2] = new StatsRow("Effect Radius:", "" + getAoERadius(), selectedTier3 == 1);
		
		toReturn[3] = new StatsRow("Magazine Size:", "" + getMagazineSize(), selectedOverclock == 3);
		
		boolean carriedAmmoModified = selectedTier2 == 2 || selectedTier4 == 0 || selectedOverclock > 2 && selectedOverclock < 6;
		toReturn[4] = new StatsRow("Max Ammo:", "" + getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", "" + getRateOfFire(), selectedOverclock == 2);
		
		toReturn[6] = new StatsRow("Reload Time:", "" + getReloadTime(), selectedTier1 == 0);
		
		toReturn[7] = new StatsRow("Stun chance:", convertDoubleToPercentage(stunChance), false);
		
		toReturn[8] = new StatsRow("Stun duration:", "" + stunDuration, false);
		
		toReturn[9] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedTier1 == 1);
		
		toReturn[10] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), selectedTier2 == 1 || selectedOverclock == 4);
		
		toReturn[11] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), selectedTier2 == 1 || selectedOverclock == 2 || selectedOverclock == 4);
		
		toReturn[12] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier3 == 2);
		
		toReturn[13] = new StatsRow("Max Penetrations:", "" + getMaxPenetrations(), selectedTier3 == 0);
		
		toReturn[14] = new StatsRow("Max Ricochets:", "" + getMaxRicochets(), selectedOverclock == 1 || selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return selectedTier3 == 1;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		if (weakpointBonus) {
			return (increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + numberOfTargets * getAreaDamage()) * getMagazineSize();
		}
		else {
			return (getDirectDamage() + numberOfTargets * getAreaDamage()) * getMagazineSize();
		}
	}

	@Override
	public double calculateIdealBurstDPS() {
		double timeToFireMagazine = (double) getMagazineSize() / getRateOfFire();
		return calculateDamagePerMagazine(false) / timeToFireMagazine;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		return calculateDamagePerMagazine(false) / timeToFireMagazineAndReload;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		return calculateDamagePerMagazine(true) / timeToFireMagazineAndReload;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier3 == 0) {
			return  calculateIdealSustainedDPS();
		}
		else if (selectedTier3 == 1) {
			int oldNumTargets = numberOfTargets;
			numberOfTargets = 3;
			double threeTargetsDPS = calculateIdealSustainedDPS();
			numberOfTargets = 2;
			double twoTargetsDPS = calculateIdealSustainedDPS();
			numberOfTargets = oldNumTargets;
			return threeTargetsDPS - twoTargetsDPS;
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int oldNumTargets = numberOfTargets;
		
		// Set how many targets you expect will be hit per bullet here.
		numberOfTargets = calculateMaxNumTargets();
		double damagePerMagazine = calculateDamagePerMagazine(false);
		// Don't forget to add the magazine that you start out with, in addition to the carried ammo
		double numberOfMagazines = ((double) getCarriedAmmo()) / ((double) getMagazineSize()) + 1.0;
		
		if (selectedTier3 == 0) {
			damagePerMagazine *= numberOfTargets;
		}
		
		numberOfTargets = oldNumTargets;
		return damagePerMagazine * numberOfMagazines;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier3 == 1) {
			return calculateNumGlyphidsInRadius(getAoERadius());
		}
		else {
			// Even though two Overclocks let this weapon ricochet, their shots have to miss in order to ricochet.
			// As a result, the ricochets don't increase the max number of targets.
			return 1 + getMaxPenetrations();
		}
	}

	@Override
	public double calculateFiringDuration() {
		double magSize = (double) getMagazineSize();
		// Don't forget to add the magazine that you start out with, in addition to the carried ammo
		double numberOfMagazines = (((double) getCarriedAmmo()) / magSize) + 1.0;
		double timeToFireMagazine = magSize / getRateOfFire();
		// There are one fewer reloads than there are magazines to fire
		return numberOfMagazines * timeToFireMagazine + (numberOfMagazines - 1.0) * getReloadTime();
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage();
		double overkill = EnemyInformation.averageHealthPool() % dmgPerShot;
		return overkill / dmgPerShot * 100.0;
	}

	@Override
	public double estimatedAccuracy() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double utilityScore() {
		// TODO Auto-generated method stub
		return 0;
	}
}
