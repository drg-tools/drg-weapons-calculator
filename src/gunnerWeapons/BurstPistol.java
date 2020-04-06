package gunnerWeapons;

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

public class BurstPistol extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int burstSize;
	private double delayBetweenBulletsDuringBurst;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double armorBreakChance;
	
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
		directDamage = 20;
		burstSize = 3;
		delayBetweenBulletsDuringBurst = 0.05;
		carriedAmmo = 120;
		magazineSize = 24;
		rateOfFire = 2.5;
		reloadTime = 2.2;
		armorBreakChance = 0.5;
		
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
		tier1[0] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased", 1, 0);
		tier1[1] = new Mod("Floating Barrel", "Sweet, sweet optimization. We called in a few friends and managed to significantly improve the stability of this gun.", 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Recoil Dampener", "Quality engineering, the best laser cut parts, the tender loving care of a dedicated R&D Department. The recoil of your gun is drastically reduced.", 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 2, 1);
		tier2[2] = new Mod("Disabled Safety", "Shorter Delay between bursts", 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("High Capacity Magazine", "The good thing about clips, magazines, ammo drums, fuel tanks... You can always get bigger variants.", 3, 0);
		tier3[1] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hardened Rounds", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those big buggers like butter. What could be finer?", 4, 0);
		tier4[1] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 4, 1);
		tier4[2] = new Mod("Hollow-Point Bullets", "Hit 'em where it hurts! Literally! We've upped the damage you'll be able to do to any creatures fleshy bits. You're welcome.", 4, 2);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Burst Stun", "Stun target if all shots in a burst hit", 5, 0);
		tier5[1] = new Mod("Longer Burst", "Fire more rounds in each burst", 5, 1);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Composite Casings", "Lighter rounds that permit a shorter delay between bursts and you can carry a few more of them as well. What's not to like?", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Full Chamber Seal", "Meticulous sealing lets you get a bit more power out of each round and the attention to detail improves how easily the magazine slots in.", 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "More damage on average but it's a bit inconsistent.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Compact Mags", "You can carry even more ammo but the rate of fire needs to be toned back to avoid a jam and please take more care while reloading.", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Experimental Rounds", "A new shape to the bullet delivers a lot more damage but it's odd size means fewer rounds in the clip and a bit less ammo overall.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Electro Minelets", "After impacting terrain, these high-tech bullets convert in to electro-minelets that will electrocute anything unfortunate enough to come close. However they don't last forever and the rounds themselves take more space in the clip and deal less direct damage.", 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Micro Fletchettes", "Convert the BRT to fire small flechettes instead of slugs. Increases overall ammo and clip size as well as reducing recoil but at the cost of pure damage.", 6);
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
				System.out.println("BurstPistol's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("BurstPistol's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[4] == 'C') {
				System.out.println("BurstPistol's fifth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
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
				case '7': {
					selectedOverclock = 6;
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
	public BurstPistol clone() {
		return new BurstPistol(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "BurstPistol";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier1 == 0) {
			toReturn += 3;
		}
		if (selectedTier3 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 1;
		}
		else if (selectedOverclock == 2) {
			// Since the randomized damage of "Homebrew Powder" averages out to 10% damage increase, I'm choosing to model it as a simple 10% multiplier.
			toReturn *= 1.1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 9;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 6) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private int getBurstSize() {
		int toReturn = burstSize;
		
		if (selectedTier5 == 1) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier4 == 1) {
			toReturn += 72;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 36;
		}
		else if (selectedOverclock == 3) {
			toReturn += 72;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 36;
		}
		else if (selectedOverclock == 6) {
			toReturn += 120;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 0) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 4 || selectedOverclock == 5) {
			toReturn -= 6;
		}
		else if (selectedOverclock == 6) {
			toReturn += 24;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 2) {
			toReturn += 3.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 1.0;
		}
		else if (selectedOverclock == 3) {
			toReturn -= 1.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 3) {
			toReturn += 0.4;
		}
		
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		
		if (selectedTier4 == 0) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier1 == 1) {
			toReturn -= 0.42;
		}
		
		if (selectedOverclock == 6) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 0) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 6) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		if (selectedTier4 == 2) {
			return 0.4;
		}
		else {
			return 0;
		}
	}
	private int getBurstStunDuration() {
		if (selectedTier5 == 0) {
			return 4;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean directDamageModified = selectedTier1 == 0 || selectedTier3 == 1 || (selectedOverclock > 0 && selectedOverclock < 7 && selectedOverclock != 3);
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), directDamageModified);
		
		toReturn[1] = new StatsRow("Burst Size:", getBurstSize(), selectedTier5 == 1);
		
		boolean magSizeModified = selectedTier3 == 0 || (selectedOverclock > 3 && selectedOverclock < 7);
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), magSizeModified);
		
		boolean carriedAmmoModified = selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 6;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		boolean RoFModified = selectedTier2== 2 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Rate of Fire:", getRateOfFire(), RoFModified);
		
		boolean reloadModified = selectedTier2 == 1 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), reloadModified);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 2, selectedTier4 == 2);
		
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier4 == 0);
		
		toReturn[8] = new StatsRow("Stun Duration:", getBurstStunDuration(), selectedTier5 == 0, selectedTier5 == 0);
		
		boolean spreadPerShotModified = selectedTier1 == 1 || selectedOverclock == 6;
		toReturn[9] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), spreadPerShotModified, spreadPerShotModified);
		
		boolean recoilModified = selectedTier2 == 0 || selectedOverclock == 6;
		toReturn[10] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// This weapon can't deal splash damage
		return false;
	}
	
	// Single-target calculations
	private double calculateDamagePerBurst(boolean weakpointBonus) {
		// TODO: I'd like to refactor this method out
		if (weakpointBonus) {
			return increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) * getBurstSize();
		}
		else {
			return getDirectDamage() * getBurstSize();
		}
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		// TODO: I'd like to refactor this method out
		double damagePerBurst = calculateDamagePerBurst(weakpointBonus);
		int numBurstsPerMagazine = getMagazineSize() / getBurstSize();
		
		return damagePerBurst * numBurstsPerMagazine;
	}
	
	private double calculateTimeToFireMagazine() {
		double timeToFireBurst = (getBurstSize() - 1) * delayBetweenBulletsDuringBurst;
		double delayBetweenBursts = 1.0 / getRateOfFire();
		int numBurstsPerMagazine = getMagazineSize() / getBurstSize();
		
		return numBurstsPerMagazine * timeToFireBurst + (numBurstsPerMagazine - 1) * delayBetweenBursts;
	}
	
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = calculateTimeToFireMagazine();
		}
		else {
			duration = calculateTimeToFireMagazine() + getReloadTime();
		}
		
		double weakpointAccuracy;
		if (weakpoint) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(getDirectDamage(), getWeakpointBonus());
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = getDirectDamage();
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 5) {
			if (burst) {
				// Because the Electro Minelets don't arm for 1 second, the Burst DPS needs to be reduced by an uptime coefficient
				double electroMinesUptimeCoefficient = (duration - 1) / duration;
				electroDPS = electroMinesUptimeCoefficient * DoTInformation.Electro_DPS;
			}
			else {
				electroDPS = DoTInformation.Electro_DPS;
			}
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * getDirectDamage()) / duration + electroDPS;
	}

	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true, true);
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedOverclock == 5) {
			return DoTInformation.Electro_DPS;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		double totalDamage = numberOfMagazines * calculateDamagePerMagazine(false);
		
		if (selectedOverclock == 5) {
			double accuracy = estimatedAccuracy(false) / 100.0;
			int numBulletsThatMiss = (int) Math.ceil((1 - accuracy) * (getCarriedAmmo() + getMagazineSize()));
			// Electro Minelets only apply a 2 second DoT, instead of the full 4 seconds like other mods/OCs.
			totalDamage += numBulletsThatMiss * DoTInformation.Electro_DPS * (0.5 * DoTInformation.Electro_SecsDuration);
		}
		
		return totalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedOverclock == 5) {
			return 8;  // calculateNumGlyphidsInRadius(1.5);
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		return numMagazines(carriedAmmo, magSize) * calculateTimeToFireMagazine() + numReloads(carriedAmmo, magSize) * getReloadTime();
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = calculateDamagePerBurst(true);
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double unchangingBaseSpread = 54;
		double changingBaseSpread = 0;
		double spreadVariance = 74;
		double spreadPerShot = 21 * getSpreadPerShot();
		double spreadRecoverySpeed = 83.72401183;
		double recoilPerShot = 27 * getRecoil();
		// Fractional representation of how many seconds this gun takes to reach full recoil per shot
		double recoilUpInterval = 1.0 / 10.0;
		// Fractional representation of how many seconds this gun takes to recover fully from each shot's recoil
		double recoilDownInterval = 3.0 / 10.0;
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, getRateOfFire(), getMagazineSize(), getBurstSize(), 
				unchangingBaseSpread, changingBaseSpread, spreadVariance, spreadPerShot, spreadRecoverySpeed, 
				recoilPerShot, recoilUpInterval, recoilDownInterval);
	}

	@Override
	public double utilityScore() {
		// Armor Breaking
		// Since only the bullets get the armor break bonus, this doesn't get multiplied by max num targets since the bullets don't have Blowthrough
		utilityScores[2] = (getArmorBreakChance() - 1.0) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electro Minelets" = 100% Electrocute Chance, but only on bullets that miss... maybe (1.0 - Accuracy)?
		if (selectedOverclock == 5) {
			// Electro Minelets arm in 1 second, detonate on any enemies that come within ~1.5m, and then explode after 3 seconds. 100% chance to apply Electrocute for 2 sec.
			double probabilityBulletsMiss = 1.0 - estimatedAccuracy(false) / 100.0;
			int numGlyphidsInMineletRadius = 8;  // calculateNumGlyphidsInRadius(1.5);
			utilityScores[3] = probabilityBulletsMiss * numGlyphidsInMineletRadius * (0.5 * DoTInformation.Electro_SecsDuration) * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Mod Tier 5 "Burst Stun" = 100% chance for 4 sec stun
		if (selectedTier5 == 0) {
			utilityScores[5] = estimatedAccuracy(false) / 100.0 * getBurstStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
}
