package gunnerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import spreadCurves.RevolverCurve;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

public class Revolver extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double stunChance;
	private double stunDuration;
	private double weakpointBonus;
	
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
		weaponPic = WeaponPictures.revolver;
		customizableRoF = true;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 55.0;
		carriedAmmo = 26;
		magazineSize = 4;
		rateOfFire = 2.0;  // bullets per second
		reloadTime = 2.0;  // seconds
		stunChance = 0.5;
		stunDuration = 1.5;  // seconds
		weakpointBonus = 0.1;
		
		// Override default 10m distance
		accEstimator.setDistance(9.0);
		accEstimator.setSpreadCurve(new RevolverCurve());
		
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
		tier1[0] = new Mod("Quickfire Ejector", "-0.7 Reload Time", modIcons.reloadSpeed, 1, 0);
		tier1[1] = new Mod("Perfect Weight Balance", "x0.3 Base Spread", modIcons.baseSpread, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Increased Caliber Rounds", "+13 Direct Damage", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Floating Barrel", "-80% Spread per Shot, x0.75 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Expanded Ammo Bags", "+11 Max Ammo", modIcons.carriedAmmo, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations, +10% Armor Breaking", modIcons.blowthrough, 3, 0);
		tier3[1] = new Mod("Explosive Rounds", "+30 Area Damage in a 1.5m radius, x0.55 Direct Damage", modIcons.addedExplosion, 3, 1);
		tier3[2] = new Mod("Hollow-Point Bullets", "+45% Weakpoint Bonus", modIcons.weakpointBonus, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Expanded Ammo Bags", "+11 Max Ammo", modIcons.carriedAmmo, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+13 Direct Damage", modIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Dead-Eye", "No aim penalty while moving", modIcons.baseSpread, 5, 0);
		// It looks like whenever this procs for the main target, all splash targets get it too, instead of RNG/enemy.
		tier5[1] = new Mod("Glyphid Neurotoxin Coating", "50% chance to inflict Neurotoxin DoT on all enemies hit by the Revolver. "
				+ "Neurotoxin does an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + " Poison Damage per Second", modIcons.neurotoxin, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "High Grain Powder", "+9 Direct Damage, +30% Weakpoint Bonus", overclockIcons.weakpointBonus, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a 75% chance to ricochet into a nearby enemy.", overclockIcons.ricochet, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Volatile Bullets", "x4 Damage to Burning targets, -26 Direct Damage", overclockIcons.heatDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Six Shooter", "+2 Magazine Size, +4 Max Ammo, +2 Rate of Fire, x1.5 Base Spread, +0.5 Reload Time", overclockIcons.magSize, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Elephant Rounds", "x2 Direct Damage, -1 Mag Size, -12 Max Ammo, +0.5 Reload Time, x0.5 Base Spread, +71% Spread per Shot, x1.5 Spread Variance, x1.5 Recoil, +3.5 Mass", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Magic Bullets", "All bullets that impact terrain automatically ricochet to nearby enemies (effectively raising accuracy to 100%). +4 Max Ammo, -20 Direct Damage", overclockIcons.ricochet, 5);
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
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			switch (symbols[0]) {
				case 'A': {
					setSelectedModAtTier(1, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(1, 1, false);
					break;
				}
			}
			
			switch (symbols[1]) {
				case 'A': {
					setSelectedModAtTier(2, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(2, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(2, 2, false);
					break;
				}
			}
			
			switch (symbols[2]) {
				case 'A': {
					setSelectedModAtTier(3, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(3, 1, false);
					break;
				}
				case 'C': {
					setSelectedModAtTier(3, 2, false);
					break;
				}
			}
			
			switch (symbols[3]) {
				case 'A': {
					setSelectedModAtTier(4, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(4, 1, false);
					break;
				}
			}
			
			switch (symbols[4]) {
				case 'A': {
					setSelectedModAtTier(5, 0, false);
					break;
				}
				case 'B': {
					setSelectedModAtTier(5, 1, false);
					break;
				}
			}
			
			switch (symbols[5]) {
				case '1': {
					setSelectedOverclock(0, false);
					break;
				}
				case '2': {
					setSelectedOverclock(1, false);
					break;
				}
				case '3': {
					setSelectedOverclock(2, false);
					break;
				}
				case '4': {
					setSelectedOverclock(3, false);
					break;
				}
				case '5': {
					setSelectedOverclock(4, false);
					break;
				}
				case '6': {
					setSelectedOverclock(5, false);
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
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Revolver";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.revolverGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	private double getDirectDamage() {
		double toReturn = directDamage;
		// Start by adding flat damage bonuses
		if (selectedTier2 == 0) {
			toReturn += 13.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 13.0;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 9.0;
		}
		else if (selectedOverclock == 2) {
			toReturn -= 26.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20.0;
		}
			
		// Then do multiplicative bonuses
		if (selectedTier3 == 1) {
			toReturn *= 0.55;
		}
		if (selectedOverclock == 4) {
			toReturn *= 2.0;
		}
		return toReturn;
	}
	private double getAreaDamage() {
		double toReturn = 0;
		
		if (selectedTier3 == 1) {
			toReturn = 30.0;
		}
		
		return toReturn;
		
	}
	private double getAoERadius() {
		if (selectedTier3 == 1) {
			return 1.5;
		}
		else {
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		if (selectedTier2 == 2) {
			toReturn += 11;
		}
		if (selectedTier4 == 0) {
			toReturn += 11;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 5) {
			toReturn += 4;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 12;
		}
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		if (selectedOverclock == 3) {
			toReturn += 2;
		}
		else if (selectedOverclock == 4) {
			toReturn -= 1;
		}
		return toReturn;
	}
	public double getRateOfFire() {
		double toReturn = rateOfFire;
		
		if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		
		return toReturn;
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		if (selectedTier1 == 0) {
			toReturn -= 0.7;
		}
		
		if (selectedOverclock == 3 || selectedOverclock == 4) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private int getMaxPenetrations() {
		if (selectedTier3 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private int getMaxRicochets() {
		// According to GreyHound, these ricochets search for enemies within 5m
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
			toReturn += 0.45;
		}
		
		if (selectedOverclock == 0) {
			toReturn += 0.3;
		}
		return toReturn;
	}
	protected double getArmorBreaking() {
		if (selectedTier3 == 0) {
			return 1.1;
		}
		else {
			return 1.0;
		}
	}
	protected double getBaseSpread() {
		double toReturn = 1.0;
		if (selectedTier1 == 1) {
			toReturn *= 0.3;
		}
		if (selectedOverclock == 3) {
			toReturn *= 1.5;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	protected double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.8;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 0.71;
		}
		
		return toReturn;
	}
	protected double getSpreadPerShotValue() {
		double toReturn = 7.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 5.6;
		}
		
		if (selectedOverclock == 4) {
			toReturn += 5.0;
		}
		
		return toReturn;
	}
	protected double getMaxBloom() {
		double toReturn = 1.0;
		
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	protected double getMovingSpreadPenalty() {
		// T5.A "Dead-Eye" removes this penalty
		if (selectedTier5 == 0) {
			return 0.0;
		}
		else {
			return 1.5;
		}
	}
	protected double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.75;
		}
		
		// Although the in-game stat is 250%, it's only a x1.5 multiplier on the RecoilPitch and RecoilYaw. The other 100% comes from the change in Mass.
		if (selectedOverclock == 4) {
			toReturn *= 1.5;
		}
		
		return toReturn;
	}
	protected double getMass() {
		if (selectedOverclock == 4) {
			return 5.5;
		}
		else {
			return 2.0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[19];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean explosiveEquipped = selectedTier3 == 1;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, explosiveEquipped || selectedOverclock == 0, explosiveEquipped);
		
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, explosiveEquipped, explosiveEquipped);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 3 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier2 == 2 || selectedTier4 == 0 || (selectedOverclock > 2 && selectedOverclock < 6);
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, selectedOverclock == 3);
		
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 0 || selectedOverclock == 3 || selectedOverclock == 4);
		
		toReturn[7] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier3 == 2 || selectedOverclock == 0);
		
		toReturn[8] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[9] = new StatsRow("Stun Chance:", convertDoubleToPercentage(stunChance), modIcons.homebrewPowder, false);
		
		toReturn[10] = new StatsRow("Stun Duration:", stunDuration, modIcons.stun, false);
		
		toReturn[11] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[12] = new StatsRow("Weakpoint Chain Hit Chance:", convertDoubleToPercentage(0.75), modIcons.homebrewPowder, selectedOverclock == 1, selectedOverclock == 1);
		
		boolean canRicochet = selectedOverclock == 1 || selectedOverclock == 5;
		toReturn[13] = new StatsRow("Max Ricochets:", getMaxRicochets(), modIcons.ricochet, canRicochet, canRicochet);
		
		boolean baseSpreadModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[14] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		boolean spreadPerShotModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[15] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, spreadPerShotModified, spreadPerShotModified);
		
		toReturn[16] = new StatsRow("Max Bloom:", convertDoubleToPercentage(getMaxBloom()), modIcons.baseSpread, selectedOverclock == 4, selectedOverclock == 4);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[17] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		toReturn[18] = new StatsRow("Recoil Mass:", getMass(), modIcons.recoil, selectedOverclock == 4, selectedOverclock == 4);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		// It appears that Revolver doesn't have any damage falloff within its 1.5m radius, so its AoE efficiency would be [1.5, 1.0, 5].
		// However, in order to save a few cycles every auto-calculate, I'm choosing not to implement that as it has no mathematical effect on the outputs.
		return selectedTier3 == 1;
	}
	
	/*
		I'm writing this method specifically because I know that the Revolver is never fired at max RoF -- it's used by the community as a sniper side-arm.
		
		I'm a bit worried that this is counter-intuitive in comparison to how the rest of the weapons are modeled, but I think this is a better approximation for how this weapon gets used in-game.
	*/
	@Override
	public double getRecommendedRateOfFire() {
		// Variables copied from estimatedAccuracy() to reverse-calculate the slow RoF needed for high accuracy
		double spreadPerShot = getSpreadPerShotValue();
		double spreadRecoverySpeed = 6.0;
		
		double recoilPitch = 130 * getRecoil();
		double recoilYaw = 10 * getRecoil();
		double mass = getMass();
		double springStiffness = 65;
		
		// These numbers are chosen arbitrarily.
		double desiredIncreaseInSpread = 2.5;
		double desiredIncreaseInRecoil = 3.0;
		
		double timeToRecoverSpread = (spreadPerShot - desiredIncreaseInSpread) / spreadRecoverySpeed;
		double timeToRecoverRecoil = calculateTimeToRecoverRecoil(recoilPitch, recoilYaw, mass, springStiffness, desiredIncreaseInRecoil);
		
		double longerTime = Math.max(timeToRecoverSpread, timeToRecoverRecoil);
		
		return Math.min(1.0 / longerTime, getRateOfFire());
	}
	
	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy, duration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = getGeneralAccuracy() / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		if (burst) {
			duration = ((double) getMagazineSize()) / getCustomRoF();
		}
		else {
			duration = (((double) getMagazineSize()) / getCustomRoF()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			directDamage *= armorWaste;
		}
		
		// OC Volatile Bullets deals x4 Direct and Area Damage to Burning targets
		if (selectedOverclock == 2 && statusEffects[0]) {
			directDamage *= 4.0;
			areaDamage *= 4.0;
		}
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
			areaDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = getWeakpointAccuracy() / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus(), 1.0);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		double neuroDPS = 0;
		if (selectedTier5 == 1) {
			// Neurotoxin Coating has a 50% chance to inflict the DoT
			if (burst) {
				neuroDPS = calculateRNGDoTDPSPerMagazine(0.5, DoTInformation.Neuro_DPS, getMagazineSize());
			}
			else {
				neuroDPS = DoTInformation.Neuro_DPS;
			}
		}
		
		int magSize = getMagazineSize();
		int bulletsThatHitWeakpoint = (int) Math.round(magSize * weakpointAccuracy);
		int bulletsThatHitTarget = (int) Math.round(magSize * generalAccuracy) - bulletsThatHitWeakpoint;
		
		return (bulletsThatHitWeakpoint * directWeakpointDamage + bulletsThatHitTarget * (directDamage + areaDamage)) / duration + neuroDPS;
	}
	
	private double calculateDamagePerMagazine(boolean weakpointBonus, int numTargets) {
		// TODO: I'd like to refactor this method out
		if (weakpointBonus) {
			return (increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + numTargets * getAreaDamage()) * getMagazineSize();
		}
		else {
			return (getDirectDamage() + numTargets * getAreaDamage()) * getMagazineSize();
		}
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		// TODO: I'd like to refactor this method a little.
		/*
			There are 8 combinations of ways for the Revolver to hit an additional target, based on various combinations of
			the Overclocks "Chain Hit" and "Magic Bullets", and the Tier 3 Mods "Super Blowthrough Rounds" and "Explosive Rounds"
		*/
		double sustainedAdditionalDPS;
		double directDamage = getDirectDamage();
		double areaDamage = getAreaDamage();
		double timeToFireMagazineAndReload = (((double) getMagazineSize()) / getCustomRoF()) + getReloadTime();
		
		// If Super Blowthrough Rounds is equipped, then the ricochets from either "Chain Hit" or "Magic Bullets" won't affect the additional DPS
		if (selectedTier3 == 0) {
			// Because Super Blowthrough Rounds are just the same damage to another enemy behind the primary target (or from a ricochet), return Ideal Sustained DPS
			return calculateSingleTargetDPS(false, false, false, false);
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 1 && selectedOverclock != 5) {
			// Explosive Rounds are just the Area Damage, so I have to re-model the sustained DPS formula here
			sustainedAdditionalDPS = getMagazineSize() * areaDamage / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// Only "Chain Hit" OR "Chain Hit" + Explosive Rounds
		else if (selectedOverclock == 1 && selectedTier3 != 0) {
			// If "Chain Hit" is equipped, 75% of bullets that hit a weakpoint will ricochet to nearby enemies.
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * getMagazineSize());
			
			sustainedAdditionalDPS = numBulletsRicochetPerMagazine * (directDamage + areaDamage) / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// Only "Magic Bullets"
		else if (selectedOverclock == 5 && selectedTier3 != 0 && selectedTier3 != 1) {
			// "Magic Bullets" mean that any bullet that MISSES the primary target will try to automatically ricochet to a nearby enemy.
			// This can be modeled by returning (1 - Accuracy) * Ideal Sustained DPS
			sustainedAdditionalDPS = (1.0 - getGeneralAccuracy() / 100.0) * calculateSingleTargetDPS(false, false, false, false);
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// This combination is the hardest to model: when a missed bullet ricochets, it still deals an explosion of damage on the ground before redirecting to the new target. This means that if you shoot the ground next to an
			// enemy with this combination, they'll take the Area Damage, followed by the Direct + Area Damage of the bullet after it redirects.
			sustainedAdditionalDPS = getMagazineSize() * (directDamage + 2 * areaDamage) / timeToFireMagazineAndReload;
			
			if (selectedTier5 == 1) {
				sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
			}
			
			return sustainedAdditionalDPS;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numberOfTargets = calculateMaxNumTargets();
		double damagePerMagazine = calculateDamagePerMagazine(false, numberOfTargets);
		double numberOfMagazines = numMagazines(getCarriedAmmo(), getMagazineSize());
		
		double ricochetTotalDamage = 0;
		// If Blowthrough Rounds is selected, multiply the dmg/mag times the total num targets hit
		if (selectedTier3 == 0) {
			damagePerMagazine *= numberOfTargets;
		}
		else if (selectedOverclock == 1 && selectedTier3 != 1) {
			// Only Chain Hit
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
			ricochetTotalDamage = totalNumRicochets * getDirectDamage();
		}
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedTier5 == 1) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.5) / getCustomRoF();
			double neurotoxinDoTTotalDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = numberOfTargets * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTTotalDamagePerEnemy * estimatedNumEnemiesKilled;
		}

		return damagePerMagazine * numberOfMagazines + ricochetTotalDamage + neurotoxinDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		/*
			There are 8 combinations of ways for the Revolver to hit an additional target, based on various combinations of
			the Overclocks "Chain Hit" and "Magic Bullets", and the Tier 3 Mods "Super Blowthrough Rounds" and "Explosive Rounds"
		*/
		// If Super Blowthrough Rounds is equipped, then any potential Ricochet can simply be added
		if (selectedTier3 == 0) {
			int toReturn = 1 + getMaxPenetrations();
			if (selectedOverclock == 1) {
				// Only Chain Hit can bounce to another target from a current one. Magic Bullets only bounces off of terrain, so it doesn't increase the Blowthrough count.
				toReturn += getMaxRicochets();
			}
			return toReturn;
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 1 && selectedOverclock != 5) {
			// From my limited testing, it appears that the full damage radius == full radius, so the efficiency will be 100%
			return calculateNumGlyphidsInRadius(getAoERadius());
		}
		
		// Only "Chain Hit"
		else if (selectedOverclock == 1 && selectedTier3 != 0 && selectedTier3 != 1) {
			return 2;
		}
		
		// "Chain Hit" + Explosive
		else if (selectedOverclock == 1 && selectedTier3 == 1) {
			// Because the second hit is guaranteed to hit another primary target, this is 2*numTargets - overlap
			// I'm guessing that of the 8 Glyphid Grunts, about 3 would be hit by both explosions.
			return (2 * calculateNumGlyphidsInRadius(getAoERadius())) - 3;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// Because the bullet has to first MISS a target, but the ricochet explodes, this is effectively (2*numTargets - 1) - overlap so that the primary target doesn't get double-counted
			// I'm choosing to model the overlapping Grunts as 5 instead of 3, because it's likely that the bullet lands near the center target that it ricochets to so more of the Grunts would 
			// be hit by both explosions.
			return (2 * calculateNumGlyphidsInRadius(getAoERadius()) - 1) - 5;
		}
		
		else {
			// Because Magic Bullets have to MISS in order to hit a secondary target, they don't increase the numTarget count unless Explosive Rounds is equipped
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getCustomRoF();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() + getAreaDamage());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		double baseSpread = 1.5 * getBaseSpread();
		double spreadPerShot = getSpreadPerShotValue();
		double spreadRecoverySpeed = 6.0;
		double maxBloom = 8.0 * getMaxBloom();
		double minSpreadWhileMoving = getMovingSpreadPenalty();
		
		double recoilPitch = 130.0 * getRecoil();
		double recoilYaw = 10.0 * getRecoil();
		double mass = getMass();
		double springStiffness = 65.0;
		
		return accEstimator.calculateCircularAccuracy(weakpointAccuracy, getCustomRoF(), getMagazineSize(), 1, 
				baseSpread, baseSpread, spreadPerShot, spreadRecoverySpeed, maxBloom, minSpreadWhileMoving,
				recoilPitch, recoilYaw, mass, springStiffness);
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage();  // Kinetic
		
		double[] areaDamage = new double[5];
		areaDamage[1] = getAreaDamage();  // Explosive
		
		if (selectedOverclock == 2 && statusEffects[0]) {
			directDamage[2] = 3.0 * getDirectDamage();  // Fire
			areaDamage[2] = 3.0 * getAreaDamage();  // Fire
		}
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		if (selectedTier5 == 1) {
			dot_dps[1] = DoTInformation.Neuro_DPS;
			dot_duration[1] = DoTInformation.Neuro_SecsDuration;
			dot_probability[1] = 0.5;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getAreaDamage(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Neurotoxin Slow; 50% chance
		if (selectedTier5 == 1) {
			utilityScores[3] = 0.5 * calculateMaxNumTargets() * DoTInformation.Neuro_SecsDuration * UtilityInformation.Neuro_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Innate stun; 50% chance for 1.5 sec duration
		utilityScores[5] = stunChance * calculateMaxNumTargets() * stunDuration * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		double damagePerShot;
		if (selectedTier3 == 0) {
			// Blowthrough Rounds
			damagePerShot = getDirectDamage() * (1 + getMaxPenetrations() + getMaxRicochets());
		}
		else if (selectedTier3 == 1) {
			// Explosive Rounds
			damagePerShot = getDirectDamage() + getAreaDamage() * calculateNumGlyphidsInRadius(getAoERadius());
		}
		else {
			damagePerShot = getDirectDamage();
		}
		
		return damagePerShot * getMagazineSize();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), getArmorBreaking(), getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[0].getText(true), "{ \"reload\": { \"name\": \"Reload Time\", \"value\": 0.7, \"subtract\": true } }", "Icon_Upgrade_Speed", "Reload Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 0, 0, 0, 20, 0, tier1[1].getText(true), "{ \"ex1\": { \"name\": \"Base Spread\", \"value\": 70, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 0, 0, 18, 0, 12, tier2[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 15 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 18, 0, 0, 12, 0, tier2[1].getText(true), "{ \"ex13\": { \"name\": \"Recoil\", \"value\": 0.75, \"multiply\": true }, "
				+ "\"ex2\": { \"name\": \"Spread Per Shot\", \"value\": 80, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 0, 0, 0, 0, 12, 18, tier2[2].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 12 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[0].getText(true), "{ \"ex3\": { \"name\": \"Max Penetrations\", \"value\": 3 } }", "Icon_Upgrade_BulletPenetration", "Blow Through"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 0, 0, 0, 20, 0, 30, tier3[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 0.5, \"multiply\": true }, "
				+ "\"ex5\": { \"name\": \"Area Damage\", \"value\": 30 }, \"ex9\": { \"name\": \"Effect Radius\", \"value\": 1.5 } }", "Icon_Upgrade_Explosion", "Explosion"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2200, 0, 0, 0, 30, 0, 20, tier3[2].getText(true), "{ \"ex6\": { \"name\": \"Weakpoint Damage Bonus\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Weakspot", "Weak Spot Bonus"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 15, 0, 0, 0, 36, 25, tier4[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 12 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 15, 36, 0, 0, 25, 0, tier4[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 15 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 0, 40, 110, 0, 60, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Dead-Eye\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 110, 0, 0, 40, 0, 60, tier5[1].getText(true), "{ \"ex8\": { \"name\": \"Neurotoxin Coating\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Special", "Neurotoxin"),
				exportAllMods || false);
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL(boolean exportAllOCs) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7350, 70, 0, 0, 135, 105, 0, overclocks[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": " + homebrewPowderCoefficient + ", \"multiply\": true } }", 
				"Icon_Overclock_ChangeOfHigherDamage"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7300, 120, 0, 0, 0, 80, 110, overclocks[1].getText(true), "{ \"ex11\": { \"name\": \"Chain Hit\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Ricoshet"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7350, 60, 0, 0, 130, 0, 110, overclocks[2].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 25, \"subtract\": true }, "
				+ "\"ex14\": { \"name\": \"Damage Vs Burning\", \"value\": 300, \"percent\": true } }", "Icon_Upgrade_FireRate"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7750, 100, 120, 0, 60, 0, 0, overclocks[3].getText(true), "{ \"clip\": { \"name\": \"Magazine Size\", \"value\": 2 }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 8 }, \"rate\": { \"name\": \"Rate of Fire\", \"value\": 4 }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 }, "
				+ "\"ex1\": { \"name\": \"Base Spread\", \"value\": 1.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_ClipSize"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7300, 90, 0, 65, 0, 140, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2, \"multiply\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 13, \"subtract\": true }, \"clip\": { \"name\": \"Magazine Size\", \"value\": 1, \"subtract\": true }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.5 }, "
				+ "\"ex13\": { \"name\": \"Recoil\", \"value\": 2.5, \"percent\": true, \"multiply\": true }, \"ex1\": { \"name\": \"Base Spread\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, "
				+ "\"ex2\": { \"name\": \"Spread Per Shot\", \"value\": 71, \"percent\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8750, 130, 0, 75, 105, 0, 0, overclocks[5].getText(true), "{ \"ex12\": { \"name\": \"Magic Bullets\", \"value\": 1, \"boolean\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 8 }, \"dmg\": { \"name\": \"Damage\", \"value\": 20, \"subtract\": true } }", "Icon_Upgrade_Ricoshet"),
				exportAllOCs || false);
		
		return toReturn;
	}
}
