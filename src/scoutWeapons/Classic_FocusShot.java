package scoutWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.GuiConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

/*
	The way MikeGSG explained it to me, the M1000 waits 0.2 seconds before beginning a Focused Shot to prevent it from "jittering".
	Once it starts charging, by default it only takes 0.8 seconds to fully charge, for what feels like a 1 second period to the user.
	The 0.8 duration is what gets affected by Charge Speed, not the 0.2 sec delay. Additionally, the crosshair animation doesn't begin
	until a minimum charge has been gained, so by the time the crosshair starts moving the Focus Shot has already been charging for a short time.
*/

public class Classic_FocusShot extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double focusedShotMultiplier;
	private double carriedAmmo;
	private int magazineSize;
	// private double rateOfFire;
	private double reloadTime;
	private double delayBeforeFocusing;
	private double focusDuration;
	private double movespeedWhileFocusing;
	private double weakpointBonus;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Classic_FocusShot() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public Classic_FocusShot(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Classic_FocusShot(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "M1000 Classic (Focused Shots)";
		weaponPic = WeaponPictures.classic;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 50;
		focusedShotMultiplier = 2.0;
		carriedAmmo = 96;
		magazineSize = 8;
		// rateOfFire = 4.0;
		reloadTime = 2.5;
		delayBeforeFocusing = 0.2;  // seconds
		focusDuration = 0.8;  // seconds.
		movespeedWhileFocusing = 0.3;
		weakpointBonus = 0.1;
		armorBreaking = 0.3;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+32 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Increased Caliber Rounds", "x1.2 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Fast-Charging Coils", "x1.6 Focus Speed", modIcons.chargeSpeed, 2, 0);
		tier2[1] = new Mod("Better Weight Balance", "x0.8 Spread per Shot, x0.5 Recoil", modIcons.recoil, 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Killer Focus", "+25% Focused Shot Multiplier", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Extended Clip", "+6 Magazine Size", modIcons.magSize, 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+25% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		tier4[2] = new Mod("Hardened Rounds", "+220% Armor Breaking", modIcons.armorBreaking, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Hitting Where it Hurts", "Focused shots Stun enemies for 3 seconds", modIcons.stun, 5, 0);
		tier5[1] = new Mod("Precision Terror", "Killing an enemy with a focused shot to a weakspot will inflict Fear on enemies within 3.5m of the kill", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Killing Machine", "Manually reloading within 1 second after a kill reduces reload time by 0.75 seconds", modIcons.reloadSpeed, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Hoverclock", "Your movement slows down for a few seconds while using focus mode in the air.", overclockIcons.hoverclock, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Minimal Clips", "+16 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Active Stability System", "No movement penalty while Focusing, -25% Focused Shot Multiplier", overclockIcons.movespeed, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Hipster", "+3 Rate of Fire, x1.75 Max Ammo, x0.85 Spread per Shot, +75% Spread Recovery Speed, x0.5 Recoil, x0.6 Direct Damage", overclockIcons.baseSpread, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electrocuting Focus Shots", "Focused Shots apply an Electrocute DoT which does "
				+ "an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per Second for 4 seconds, -25% Focused Shot Multiplier", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Supercooling Chamber", "+125% Focused Shot Multiplier, x0.635 Max Ammo, x0.5 Focus Speed, no movement while focusing", overclockIcons.directDamage, 5);
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
				System.out.println("Classic's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Classic's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Classic's third tier of mods only has two choices, so 'C' is an invalid choice.");
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
	public Classic_FocusShot clone() {
		return new Classic_FocusShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Classic_FocusShot";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.scoutCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.classicGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn -= 20;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 1) {
			toReturn *= 1.2;
		}
		
		return toReturn;
	}
	private double getFocusedShotMultiplier() {
		double toReturn = focusedShotMultiplier;
		
		// Additive bonuses first
		if (selectedTier3 == 0) {
			toReturn += 0.25;
		}
		
		if (selectedOverclock == 2 || selectedOverclock == 4) {
			toReturn -= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn += 1.25;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 32;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 16;
		}
		else if (selectedOverclock == 3) {
			toReturn += 72;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.635;
		}
		
		// Divide by 2 to account for firing two ammo per focused shot
		// Use ceiling function because you can do a fully-charged Focus Shot with only 1 ammo if there's no ammo left in the clip
		return (int) Math.ceil(toReturn / 2.0);
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 6;
		}
		
		// Divide by 2 to account for firing two ammo per focused shot
		return toReturn / 2;
	}
	private double getRateOfFire() {
		// Because the max RoF will never be achieved with Focus Shots, instead model the RoF as the inverse of the Focus Duration
		return 1.0 / (delayBeforeFocusing + getFocusDuration());
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier5 == 2) {
			// "Killing Machine": if you manually reload within 1 second after a kill, the reload time is reduced by approximately 0.75 seconds.
			// Because Sustained DPS uses this ReloadTime method, I'm choosing to use the Ideal Burst DPS as a quick-and-dirty estimate how often a kill gets scored 
			// so that this doesn't infinitely loop.
			double killingMachineManualReloadWindow = 1.0;
			double killingMachineReloadReduction = 0.75;
			double burstTTK = EnemyInformation.averageHealthPool() / calculateIdealBurstDPS();
			// Don't let a high Burst DPS increase this beyond a 100% uptime
			double killingMachineUptimeCoefficient = Math.min(killingMachineManualReloadWindow / burstTTK, 1.0);
			double effectiveReloadReduction = killingMachineUptimeCoefficient * killingMachineReloadReduction;
			
			toReturn -= effectiveReloadReduction;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getFocusDuration() {
		double focusSpeedCoefficient = 1.0;
		if (selectedTier2 == 0) {
			focusSpeedCoefficient *= 1.6;
		}
		
		if (selectedOverclock == 5) {
			focusSpeedCoefficient *= 0.5;
		}
		
		return focusDuration / focusSpeedCoefficient;
	}
	private double getMovespeedWhileFocusing() {
		double modifier = movespeedWhileFocusing;
		
		if (selectedOverclock == 2) {
			modifier += 0.7;
		}
		else if (selectedOverclock == 5) {
			modifier *= 0;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	private double getArmorBreaking() {
		double toReturn = armorBreaking;
		
		if (selectedTier4 == 2) {
			toReturn += 2.2;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		if (selectedTier5 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[14];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), selectedOverclock == 3 || selectedTier1 == 1);
		
		boolean multiplierModified = selectedTier3 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Focused Shot Multiplier:", convertDoubleToPercentage(getFocusedShotMultiplier()), multiplierModified);
		
		toReturn[2] = new StatsRow("Delay Before Focusing:", delayBeforeFocusing, false);
		
		toReturn[3] = new StatsRow("Focus Shot Charge-up Duration:", getFocusDuration(), selectedTier2 == 0 || selectedOverclock == 5);
		
		toReturn[4] = new StatsRow("Clip Size:", getMagazineSize(), selectedTier3 == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 0 || selectedOverclock == 5);
		
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), selectedTier5 == 2 || selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 1);
		
		toReturn[9] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), selectedTier4 == 2);
		
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier5 == 0, selectedTier5 == 0);
		
		toReturn[11] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0, selectedTier4 == 0);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
		toReturn[13] = new StatsRow("Movespeed While Focusing: (m/sec)", getMovespeedWhileFocusing(), selectedOverclock == 2 || selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}

	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean weakpoint) {
		double duration;
		if (burst) {
			duration = ((double) getMagazineSize()) / getRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage() * getFocusedShotMultiplier();
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		if (weakpoint && !statusEffects[1]) {
			directDamage = increaseBulletDamageForWeakpoints(directDamage, getWeakpointBonus());
		}
		
		double electroDPS = 0;
		if (selectedOverclock == 4) {
			// Because they get Electrocuted immediately, it has 100% uptime.
			electroDPS = DoTInformation.Electro_DPS;
		}
		
		return (directDamage * getMagazineSize()) / duration + electroDPS;
	}

	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false);
	}

	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true);
	}
	
	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateIdealSustainedDPS();
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamageDealt = calculateMaxNumTargets() * (getMagazineSize() + getCarriedAmmo()) * getDirectDamage() * getFocusedShotMultiplier();
		
		double electrocuteDoTTotalDamage = 0;
		if (selectedOverclock == 4) {
			// OC "Electrocuting Focus Shots" has an increased duration of 4 seconds
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Electro_DPS);
			
			double estimatedNumEnemiesKilled = calculateMaxNumTargets() * (calculateFiringDuration() / averageTimeToKill());
			
			electrocuteDoTTotalDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return totalDamageDealt + electrocuteDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getMaxPenetrations();
	}

	@Override
	public double calculateFiringDuration() {
		int magSize = getMagazineSize();
		int carriedAmmo = getCarriedAmmo();
		double timeToFireMagazine = ((double) magSize) / getRateOfFire();
		return numMagazines(carriedAmmo, magSize) * timeToFireMagazine + numReloads(carriedAmmo, magSize) * getReloadTime();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage() * getFocusedShotMultiplier(), getWeakpointBonus());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Manually aimed
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			getDirectDamage() * getFocusedShotMultiplier(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double[] areaDamage = {
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double electroDmg = 0;
		if (selectedOverclock == 4) {
			// OC "Electrocuting Focus Shots" has an increased duration of 4 seconds
			electroDmg = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Electro_DPS);
		}
		double[] DoTDamage = {
			0,  // Fire
			electroDmg,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, getWeakpointBonus(), 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// OC "Active Stability System" removes the movespeed penalty while Focusing
		utilityScores[0] = (getMovespeedWhileFocusing() - MathUtils.round(movespeedWhileFocusing * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// OC "Hoverclock" gives a 2 second cap to Scout's vertical movement speed (guess: 0.5 m/sec?), but after that 2sec ends original velocity is restored
		if (selectedOverclock == 0) {
			// Because the vertical movespeed cap of +- 0.5 m/sec can be used to negate fall damage from infinite height, there's not really a 
			// way to give this OC a numerical value. For now, I'm just gonna call it 10 and move on.
			utilityScores[0] += 10;
		}
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() * getFocusedShotMultiplier(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electrocuting Focus Shots" = 100% chance to electrocute on focused shots
		if (selectedOverclock == 4) {
			// OC "Electrocuting Focus Shots" has an increased duration of 4 seconds
			utilityScores[3] = calculateMaxNumTargets() * 4 * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// According to MikeGSG, Mod Tier 5 "Precision Terror" does 1 Fear in a 3.5m radius
		if (selectedTier5 == 1) {
			double probabilityToHitWeakpoint = EnemyInformation.probabilityBulletWillHitWeakpoint();
			double uptimeCoefficient = Math.min(UtilityInformation.Fear_Duration / averageTimeToKill(), 1);
			int numGlyphidsFeared = 22;  // calculateNumGlyphidsInRadius(3.5);
			utilityScores[4] = probabilityToHitWeakpoint * uptimeCoefficient * numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Mod Tier 5 "Hitting Where it Hurts" = 100% chance for 3 sec stun
		if (selectedTier5 == 0) {
			utilityScores[5] = calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		double bulletDamage = getDirectDamage() * getFocusedShotMultiplier() * getMagazineSize();
		
		double electrocuteDamage = 0;
		if (selectedOverclock == 4) {
			// OC "Electrocuting Focus Shots" has 100% chance to proc and an increased duration of 4 seconds
			electrocuteDamage = calculateAverageDoTDamagePerEnemy(0, 4, DoTInformation.Electro_DPS);
		}
		
		return (bulletDamage + electrocuteDamage) * calculateMaxNumTargets();
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 32 } }", "Icon_Upgrade_Ammo"));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 10 } }", "Icon_Upgrade_DamageGeneral"));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 0, 0, 24, 15, 0, tier2[0].getText(true), "{ \"ex1\": { \"name\": \"Focus Speed\", \"value\": 60, \"percent\": true } }", "Icon_Upgrade_ChargeUp"));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 24, 0, 15, 0, 0, tier2[1].getText(true), "{ \"ex3\": { \"name\": \"Recoil\", \"value\": 0.5, \"multiply\": true } }", "Icon_Upgrade_Recoil"));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 50, 0, 0, 0, 35, 0, tier3[0].getText(true), "{ \"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_DamageGeneral"));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 0, 0, 0, 0, 35, 50, tier3[1].getText(true), "{ \"clip\": { \"name\": \"Clip Size\", \"value\": 6 } }", "Icon_Upgrade_ClipSize"));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 48, 0, 0, 0, 50, 72, tier4[0].getText(true), "{ \"ex4\": { \"name\": \"Max Penetrations\", \"value\": 3 } }", "Icon_Upgrade_BulletPenetration"));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 0, 72, 0, 48, 50, 0, tier4[1].getText(true), "{ \"ex5\": { \"name\": \"Weakpoint Damage\", \"value\": 25, \"percent\": true } }", "Icon_Upgrade_Weakspot"));
		toReturn.add(String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 0, 0, 0, 48, 50, 72, tier4[2].getText(true), "{ \"ex6\": { \"name\": \"Armor Breaking\", \"value\": 220, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking"));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 64, 70, 0, 140, 0, 0, tier5[0].getText(true), "{ \"ex7\": { \"name\": \"Focused Shot Stun Chance\", \"value\": 100, \"percent\": true } }", "Icon_Upgrade_Stun"));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 70, 140, 0, 0, 0, tier5[1].getText(true), "{ \"ex10\": { \"name\": \"Focus Shot Kill AoE Fear\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_ScareEnemies"));
		toReturn.add(String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 70, 0, 64, 0, 140, 0, tier5[2].getText(true), "{ \"ex10\": { \"name\": \"Focus Shot Kill AoE Fear\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Speed"));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 7350, 0, 105, 0, 135, 0, 65, overclocks[0].getText(true), "{ \"ex11\": { \"name\": \"Focus Shot Hover\", \"value\": 1, \"boolean\": true } }", "Icon_Overclock_Slowdown"));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8200, 75, 0, 0, 0, 95, 130, overclocks[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 16 }, \"reload\": { \"name\": \"Reload Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_Ammo"));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8150, 70, 90, 135, 0, 0, 0, overclocks[2].getText(true), "{ \"ex8\": { \"name\": \"Focus Mode Movement Speed\", \"value\": 70, \"percent\": true }, "
				+ "\"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_MovementSpeed"));
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8900, 0, 0, 80, 125, 105, 0, overclocks[3].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 1.75, \"multiply\": true }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 3 }, \"ex3\": { \"name\": \"Recoil\", \"value\": 0.5, \"multiply\": true }, \"dmg\": { \"name\": \"Damage\", \"value\": 0.6, \"multiply\": true } }", "Icon_Upgrade_Aim"));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 8850, 0, 120, 75, 95, 0, 0, overclocks[4].getText(true), "{ \"ex12\": { \"name\": \"Electrocuting Focus Shots\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 25, \"percent\": true, \"subtract\": true } }", "Icon_Upgrade_Electricity"));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 8500, 70, 0, 0, 0, 90, 130, overclocks[5].getText(true), "{ \"ex2\": { \"name\": \"Focused Shot Damage Bonus\", \"value\": 125, \"percent\": true }, "
				+ "\"ammo\": { \"name\": \"Max Ammo\", \"value\": 0.635, \"multiply\": true }, \"ex1\": { \"name\": \"Focus Speed\", \"value\": 0.5, \"percent\": true, \"multiply\": true }, "
				+ "\"ex8\": { \"name\": \"Focus Mode Movement Speed\", \"value\": 0, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"));
		
		return toReturn;
	}
}
