package scoutWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Boomstick extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private int numberOfPellets;
	private int frontalConeDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	private int maxPenetrations;
	private double armorBreakChance;
	private double baseSpread;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Boomstick() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Boomstick(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Boomstick(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Jury-Rigged Boomstick";
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 12;
		numberOfPellets = 8;
		frontalConeDamage = 20;
		carriedAmmo = 24;
		magazineSize = 2;
		rateOfFire = 1.5;
		reloadTime = 2.0;
		stunChance = 0.3;
		stunDuration = 2.5;
		maxPenetrations = 0;
		armorBreakChance = 1.0;
		baseSpread = 1.0;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 1, 0);
		tier1[1] = new Mod("Double-Sized Buckshot", "Bigger and heavier handcrafted specialist dwarf buckshot. Accept no substitute.", 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Double Trigger", "Tweaked trigger mechanism allows you to unload both barrels in quick succession dealing massive damage to anything in front of you.", 2, 0);
		tier2[1] = new Mod("Quickfire Ejector", "Experience, training, and a couple of under-the-table design \"adjustments\" means your gun can be reloaded significantly faster.", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Stun Duration", "Stunned enemies are incapacitated for a longer period of time.", 3, 0);
		tier3[1] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 3, 1);
		tier3[2] = new Mod("High Capacity Shells", "It took some creating thinking, but we finally found out how to pack more buckshot into each shell. Just... Handle with care, they're liable to take your eye out.", 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "Shaped projectiles designed to over-penetrate targets with a minimal loss of energy. In other words: Fire straight through several enemies at once!", 4, 0);
		tier4[1] = new Mod("Tungsten Coated Buckshot", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those big buggers like butter. What could be finer?", 4, 1);
		tier4[2] = new Mod("Improved Blast Wave", "The shockwave from the blast deals extra damage to any enemies unlucky enough to be in the area extending 4m infront of you.", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Auto Reload", "Reloads automatically when unequipped for more than 5 seconds", 5, 0, false);
		tier5[1] = new Mod("Fear The Boomstick", "Chance to scare nearby creatures whenever you shoot", 5, 1, false);
		tier5[2] = new Mod("White Phosphorous Shells", "Convert some of the damage to fire damage", 5, 2, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Compact Shells", "You can carry a few more of these compact shells in your pockets and they are a bit faster to reload with.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Double Barrel", "Unload both barrels at once, no regrets.", 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Special Powder", "Less like gunpowder and more like rocketfuel, this mixture gives a hell of a kick that you can use to get places.", 2, false);
		overclocks[3] = new Overclock(Overclock.classification.clean, "Stuffed Shells", "With a bit of patience and some luck you can get one more pellet and a few more grains of powder into each shell without affecting the gun's performance or losing an eye in the process.", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Shaped Shells", "Specially shaped shells result in a tighter shot but the number of pellets is reduced.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Jumbo Shells", "These large shells pack a lot more charge for a big increase in damage but they also take up more space so total ammo is limited.", 5);
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
				System.out.println("Boomstick's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Boomstick's second tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public Boomstick clone() {
		return new Boomstick(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		
		if (selectedTier1 == 1) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1 || selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 5) {
			toReturn += 8;
		}
		
		return toReturn;
	}
	private int getNumberOfPelletsPerShot() {
		int toReturn = numberOfPellets;
		
		if (selectedTier3 == 2) {
			toReturn += 3;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= 2;
		}
		else if (selectedOverclock == 3) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 2;
		}
		
		return toReturn;
	}
	private int getBlastwaveDamage() {
		// Hits enemies within 4m in front of Scout
		// Area damage, instead of direct damage. Bulks + Dreads resist it.
		int toReturn = frontalConeDamage;
		
		if (selectedTier4 == 2) {
			toReturn += 20;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedOverclock == 1) {
			toReturn -= 1;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 8;
		}
		if (selectedTier3 == 1) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 6;
		}
		else if (selectedOverclock == 1) {
			// For the math of Double Barrel to work out correctly, the Carried Ammo should be halved since it fires 2 ammo per shot.
			toReturn /= 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 10;
		}
		
		return toReturn;
	}
	private double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedTier2 == 0) {
			toReturn += 7.5;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 5) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getStunDuration() {
		double toReturn = stunDuration;
		
		if (selectedTier3 == 0) {
			toReturn += 2.5;
		}
		
		return toReturn;
	}
	private int getMaxPenetrations() {
		int toReturn = maxPenetrations;
		
		if (selectedTier4 == 0) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		
		if (selectedTier4 == 1) {
			toReturn += 3.0;
		}
		
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		
		if (selectedOverclock == 4) {
			toReturn -= 0.35;
		}
		
		return toReturn;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[12];
		
		boolean damageModified = selectedTier1 == 1 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Damage Per Pellet:", getDamagePerPellet(), damageModified);
		
		boolean pelletsModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[1] = new StatsRow("Number of Pellets/Shot:", getNumberOfPelletsPerShot(), pelletsModified);
		
		toReturn[2] = new StatsRow("Blastwave Damage:", getBlastwaveDamage(), selectedTier4 == 2);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), selectedOverclock == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedTier3 == 1 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 0);
		
		boolean reloadTimeModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), reloadTimeModified);
		
		toReturn[7] = new StatsRow("Stun Chance:", convertDoubleToPercentage(stunChance), false);
		
		toReturn[8] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier3 == 0);
		
		toReturn[9] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0);
		
		toReturn[10] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier4 == 1);
		
		toReturn[11] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Technically the Blastwave is Area Damage, but this flag is for spherical Area Damage like Grenade Launcher or Autocannon.
		return false;
	}
	
	// Single-target calculations
	private double calculateDamagePerMagazine(boolean weakpointBonus) {
		double damagePerShot;
		if (weakpointBonus) {
			damagePerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet() * getNumberOfPelletsPerShot()) + getBlastwaveDamage();
			return (double) damagePerShot * getMagazineSize();
		}
		else {
			damagePerShot = getDamagePerPellet() * getNumberOfPelletsPerShot() + getBlastwaveDamage();
			return (double) damagePerShot * getMagazineSize();
		}
	}

	@Override
	public double calculateIdealBurstDPS() {
		int magSize = getMagazineSize();
		
		if (magSize > 1) {
			double timeToFireMagazine = ((double) getMagazineSize()) / getRateOfFire();
			return calculateDamagePerMagazine(false) / timeToFireMagazine;
		}
		else {
			return calculateDamagePerMagazine(false) / getReloadTime();
		}
	}

	@Override
	public double calculateIdealSustainedDPS() {
		int magSize = getMagazineSize();
		
		if (magSize > 1) {
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			return calculateDamagePerMagazine(false) / timeToFireMagazineAndReload;
		}
		else {
			return calculateDamagePerMagazine(false) / getReloadTime();
		}
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		int magSize = getMagazineSize();
		
		if (magSize > 1) {
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			return calculateDamagePerMagazine(true) / timeToFireMagazineAndReload;
		}
		else {
			return calculateDamagePerMagazine(true) / getReloadTime();
		}
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		int magSize = getMagazineSize();
		double secondaryDamage;
		if (selectedTier4 == 0) {
			secondaryDamage = calculateDamagePerMagazine(false);
		}
		else {
			secondaryDamage = getBlastwaveDamage();
		}
		if (magSize > 1) {
			double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
			return secondaryDamage / timeToFireMagazineAndReload;
		}
		else {
			return secondaryDamage / getReloadTime();
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		// The frontal blastwave is a 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
		int gruntsHitByBlastwave = 4;
		int damagePerShot = getDamagePerPellet() * getNumberOfPelletsPerShot() + gruntsHitByBlastwave * getBlastwaveDamage();
		return (getMagazineSize() + getCarriedAmmo()) * damagePerShot * calculateMaxNumTargets();
	}

	@Override
	public int calculateMaxNumTargets() {
		int gruntsHitByPellets;
		if (selectedOverclock == 4) {
			// Since the base spread gets reduced by 35%, assume that each shot's pellets can only hit one grunt without blowthrough.
			gruntsHitByPellets = 1;
		}
		else {
			// If Shaped Shells is not selected, then assume the pellets are wide enough to hit 2 grunts.
			gruntsHitByPellets = 2;
		}
		return gruntsHitByPellets * (1 + getMaxPenetrations());
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		
		if (magSize > 1) {
			int carriedAmmo = getCarriedAmmo();
			double timeToFireMagazine = ((double) magSize) / getRateOfFire();
			return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
		}
		else {
			// Since each shot gets fired instantly and there's only one shot in the magazine, the rate of fire isn't applicable. Simply add up all the reload times.
			return getCarriedAmmo() * getReloadTime();
		}
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet() * getNumberOfPelletsPerShot());
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
		// OC "Special Powder" gives a lot of Mobility (7.8m vertical per shot, 13m horizontal per shot)
		if (selectedOverclock == 2) {
			// Multiply by 2 for mobility per shot
			utilityScores[0] = 2 * (0.5 * 7.8 + 0.5 * 13) * UtilityInformation.BlastJump_Utility;
		}
		else {
			utilityScores[0] = 0;
		}
		
		// Armor Breaking bonuses
		utilityScores[2] = (getArmorBreakChance() - 1) * calculateMaxNumTargets() * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Fear the Boomstick" = 50% chance to Fear in same blast cone as the Blastwave damage
		if (selectedTier5 == 1) {
			// 20 degree isosceles triangle, 4m height; 1.41m base. 4 grunts can be hit in a 1-2-1 stack.
			int gruntsHitByBlastwave = 4;
			utilityScores[4] = 0.5 * gruntsHitByBlastwave * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate Stun = 30% chance for 2.5 sec (improved by Mod Tier 3 "Stun Duration")
		utilityScores[5] = stunChance * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
}
