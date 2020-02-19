package engineerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class SMG extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double electrocutionDoTChance;
	private int directDamage;
	private int electricDamage;
	private int magazineSize;
	private int carriedAmmo;
	private double rateOfFire;
	private double reloadTime;
	private double baseSpread;
	private double weakpointBonus;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public SMG() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public SMG(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public SMG(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Stubby\" Voltaic SMG";
		
		// Base stats, before mods or overclocks alter them:
		electrocutionDoTChance = 0.1;
		// Electrocution DoTs do not stack; it only refreshes the duration.
		directDamage = 6;
		electricDamage = 2; 
		// Added onto the direct damage of each bullet; does not affect DoT damage. Affected by weakpoint bonuses and elemental weaknesses/resistances 
		// Dreadnaughts resist 60% electric damage, Huuli Hoarders take 80% extra electric damage.
		magazineSize = 30;
		carriedAmmo = 420;
		rateOfFire = 11.0;
		reloadTime = 2.0;
		baseSpread = 1.0;
		weakpointBonus = 0.0;
		
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
		tier1[0] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 1, 0);
		tier1[1] = new Mod("Upgraded Capacitors", "Better chance to electrocute target", 1, 1);
		tier1[2] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 1, 2);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("High Capacity Magazine", "The good thing about clips, magazines, ammo drums, fuel tanks... You can always get bigger variants.", 2, 0);
		tier2[1] = new Mod("Floating Barrel", "Sweet, sweet optimization. We called in a few friends and managed to significantly improve the stability of this gun.", 2, 1);
		tier2[2] = new Mod("Improved Gas System", "We overclocked your gun. It fires faster. Don't ask. Just enjoy. Also probably don't tell Management, please.", 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hollow-Point Bullets", "Hit 'em where it hurts! Literally! We've updated the damage you'll be able to do to any creatures fleshy bits. You're welcome.", 4, 0);
		tier4[1] = new Mod("Larger Capacitors", "Better chance to electrocute target", 4, 1);
		tier4[2] = new Mod("Overcharged Rounds", "More electric damage", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Conductive Bullets", "More damage to targets that are in an electric field.", 5, 0);
		tier5[1] = new Mod("Magazine Capacity Tweak", "Greatly increased magazine capacity", 5, 1);
		tier5[2] = new Mod("Electric Arc", "Chance for electrocution to arc from one target to another", 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Super-Slim Rounds", "Same power but in a smaller package giving slightly better accuracay and letting you fit a few more rounds in each mag.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Well Oiled Machine", "When you need a little more sustained damage.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "EM Refire Booster", "Use the electron circuit of the SMG to boost its fire rate and damage but the accuracy suffers as a result.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Light-Weight Rounds", "They don't hit quite as hard, and can't handle fast fire rates but you sure can carry a lot more of them!", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Turret Arc", "Use the gemini turrests as nodes in an electric arc. Zap! The downside is less ammo and a slower rate of fire.", 4, false);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Turret EM Discharge", "Use a turret as the epicenter of an electric explosion! The bullet modifications unfortunately also lower the direct damage and electrocution chance.", 5, false);
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
			if (symbols[2] == 'C') {
				System.out.println("SMG's third tier of mods only has two choices, so 'C' is an invalid choice.");
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
				case 'C': {
					selectedTier1 = 2;
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
				case 'C': {
					selectedTier4 = 2;
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
				case 'C': {
					selectedTier5 = 2;
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
	public SMG clone() {
		return new SMG(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getElectrocutionDoTChance() {
		double toReturn = electrocutionDoTChance;
		
		if (selectedTier1 == 1) {
			toReturn += 0.1;
		}
		
		if (selectedTier4 == 1) {
			toReturn += 0.1;
		}
		
		if (selectedOverclock == 5) {
			toReturn -= 0.05;
		}
		
		return toReturn;
	}
	private int getDirectDamage() {
		int toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 2;
		}
		
		if (selectedTier3 == 0) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 1;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		
		return toReturn;
	}
	private int getElectricDamage() {
		int toReturn = electricDamage;
		
		if (selectedTier4 == 2) {
			toReturn += 2;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 2;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier2 == 0) {
			toReturn += 10;
		}
		
		if (selectedTier5 == 1) {
			toReturn += 20;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 5;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 2) {
			toReturn += 120;
		}
		
		if (selectedTier3 == 1) {
			toReturn += 120;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 180;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 120;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 2) {
			toReturn += 3.0;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 2) {
			toReturn += 4.0;
		}
		else if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn -= 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.4;
		}

		if (selectedOverclock == 0) {
			toReturn *= 0.8;
		}
		else if (selectedOverclock == 2) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.3;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[13];
		
		boolean DoTChanceModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Electrocution DoT Chance:", convertDoubleToPercentage(getElectrocutionDoTChance()), DoTChanceModified);
		toReturn[1] = new StatsRow("Electrocution DoT Dmg/Tick:", DoTInformation.Electro_DmgPerTick, false);
		toReturn[2] = new StatsRow("Electrocution DoT Ticks/Sec:", DoTInformation.Electro_TicksPerSec, false);
		toReturn[3] = new StatsRow("Electrocution DoT Duration:", DoTInformation.Electro_SecsDuration, false);
		double electrocuteTotalDamage = DoTInformation.Electro_DmgPerTick * DoTInformation.Electro_TicksPerSec * DoTInformation.Electro_SecsDuration;
		toReturn[4] = new StatsRow("Electrocution DoT Total Dmg:", electrocuteTotalDamage, false);
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		toReturn[6] = new StatsRow("Electric Damage:", getElectricDamage(), selectedTier4 == 2 || selectedOverclock == 2);
		
		boolean magSizeModified = selectedTier2 == 0 || selectedTier5 == 1 || selectedOverclock == 0;
		toReturn[7] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier1 == 2 || selectedTier3 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[8] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		boolean RoFModified = selectedTier2 == 2 || (selectedOverclock > 0 && selectedOverclock < 5);
		toReturn[9] = new StatsRow("Rate of Fire:", getRateOfFire(), RoFModified);
		
		toReturn[10] = new StatsRow("Reload Time:", getReloadTime(), selectedOverclock == 1);
		
		boolean baseSpreadModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[11] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified);
		
		toReturn[12] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 0);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	private double calculateDamagePerBullet(boolean weakpointBonus) {
		double directDamage = getDirectDamage();
		
		if (selectedTier5 == 0) {
			// To model a 30% physical damage increase to electrocuted targets, average out how many bullets/mag that would get the buff after a DoT proc, and then spread that bonus across every bullet.
			double DoTChance = getElectrocutionDoTChance();
			double meanBulletsFiredBeforeProc = Math.round(1.0 / DoTChance);
			double numBulletsFiredAfterProc = getMagazineSize() - meanBulletsFiredBeforeProc;
			
			directDamage *= (meanBulletsFiredBeforeProc + numBulletsFiredAfterProc * 1.3) / getMagazineSize();
		}
		
		// According to the wiki, Electric damage gets bonus from Weakpoints too
		double totalDamage = directDamage + getElectricDamage();
		if (weakpointBonus) {
			return increaseBulletDamageForWeakpoints(totalDamage, getWeakpointBonus());
		}
		else {
			return totalDamage;
		}
	}
	
	private double calculateDirectDamagePerMagazine(boolean weakpointBonus) {
		return calculateDamagePerBullet(weakpointBonus) * getMagazineSize();
	}
	
	private double calculateBurstElectrocutionDoTDPS() {
		/*
			When DoTs stack, like in BL2, the formula is PelletsPerSec * DoTDuration * DoTChance * DoTDmgPerSec.
			However, in DRG, once a DoT is applied it can only have its duration refreshed.
		*/
		double DoTChance = getElectrocutionDoTChance();
		double meanBulletsFiredBeforeProc = Math.round(1.0 / DoTChance);
		double numBulletsFiredAfterProc = getMagazineSize() - meanBulletsFiredBeforeProc;
		double secBeforeProc = meanBulletsFiredBeforeProc / getRateOfFire();
		double secAfterProc = numBulletsFiredAfterProc / getRateOfFire();
		
		return (DoTInformation.Electro_DPS * secAfterProc) / (secBeforeProc + secAfterProc);
	}

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}

	@Override
	public double calculateIdealBurstDPS() {
		// First calculate the direct damage DPS of the bullets, then add the DoT DPS on top.
		double timeToFireMagazine = ((double) getMagazineSize()) / getRateOfFire();
		double directDPS = calculateDirectDamagePerMagazine(false) / timeToFireMagazine;
		
		return directDPS + calculateBurstElectrocutionDoTDPS();
	}

	@Override
	public double calculateIdealSustainedDPS() {
		// First calculate the direct damage DPS of the bullets, then add the DoT DPS on top.
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		double directDPS = calculateDirectDamagePerMagazine(false) / timeToFireMagazineAndReload;
		
		// Due to high fire rate of the gun, it can be modeled as always having an Electrocute DoT up for sustained DPS.
		return directDPS + DoTInformation.Electro_DPS;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		// First calculate the direct damage DPS of the bullets, then add the DoT DPS on top.
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		double directDPS = calculateDirectDamagePerMagazine(true) / timeToFireMagazineAndReload;
		
		// Due to high fire rate of the gun, it can be modeled as always having an Electrocute DoT up for sustained DPS.
		return directDPS + DoTInformation.Electro_DPS;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier5 == 2) {
			// TODO: this formula is incorrect. With high RoF, this can exceed the normal DoT DPS.
			// Average it out to bullets/sec number of chances to apply the primary dot every second times 25% chance to apply DoT to secondary target times the DoT DPS
			return getRateOfFire() * getElectrocutionDoTChance() * 0.25 * DoTInformation.Electro_DPS;
		}
		else {
			return 0.0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// First, how much direct damage can be dealt without DoT calculations. Second, add the DoTs on the primary targets. Third, if necessary, add the secondary target DoTs.
		double totalDamage = 0;
		totalDamage += calculateDamagePerBullet(false) * (getMagazineSize() + getCarriedAmmo());
		
		/* 
			There's no good way to model RNG-based mechanics' max damage, such as the Electrocute DoT. I'm choosing
			to model it as how much DPS it does per magazine times how many seconds it takes to fire every bullet. 
			This value should always be less than the full DoT DPS times firing duration.
		*/
		totalDamage += calculateBurstElectrocutionDoTDPS() * calculateFiringDuration();
		
		if (selectedTier5 == 2) {
			totalDamage += calculateAdditionalTargetDPS() * calculateFiringDuration();
		}
		
		return totalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier5 == 2) {
			return 2;
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = calculateDamagePerBullet(true);
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double utilityScore() {
		
		// Innate ability to Electrocute applies an 80% slow to enemies (proc chance increased/decreased by mods and OCs)
		utilityScores[3] = getElectrocutionDoTChance() * calculateMaxNumTargets() * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}
