package drgtools.dpscalc.weapons.scout.classic;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.DoTInformation;
import drgtools.dpscalc.modelPieces.DwarfInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.weapons.Weapon;

public abstract class Classic extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double focusedShotMultiplier;
	protected double carriedAmmo;
	protected int magazineSize;
	protected double rateOfFire;
	private double reloadTime;
	private double focusDuration;
	protected double movespeedWhileFocusing;
	private double armorBreaking;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	public Classic(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		weaponPic = WeaponPictures.classic;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 55;
		focusedShotMultiplier = 2.0;
		carriedAmmo = 96;
		magazineSize = 8;
		rateOfFire = 4.0;
		reloadTime = 2.55;
		focusDuration = 1.0 / 1.6;  // seconds
		movespeedWhileFocusing = 0.3;
		armorBreaking = 0.3;
		
		accEstimator.setSpreadCurve(new Classic_SpreadCurve());
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "+40 Max Ammo", modIcons.carriedAmmo, 1, 0);
		tier1[1] = new Mod("Increased Caliber Rounds", "+10 Direct Damage", modIcons.directDamage, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Fast-Charging Coils", "+25% Focus Speed", modIcons.chargeSpeed, 2, 0);
		tier2[1] = new Mod("Better Weight Balance", "-30% Spread per Shot, x0.8 Max Bloom, x0.5 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Hardened Rounds", "+220% Armor Breaking", modIcons.armorBreaking, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Killer Focus", "+25% Focused Shot Multiplier", modIcons.directDamage, 3, 0);
		tier3[1] = new Mod("Extended Clip", "+6 Clip Size", modIcons.magSize, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "+20% Weakpoint Bonus", modIcons.weakpointBonus, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Hitting Where it Hurts", "Focused shots Stun enemies for 3 seconds", modIcons.stun, 5, 0);
		tier5[1] = new Mod("Precision Terror", "Killing an enemy with a focused shot to a weakpoint will inflict 2.5 Fear Factor on enemies within 4m of the kill", modIcons.fear, 5, 1);
		tier5[2] = new Mod("Killing Machine", "Manually reloading within 1 second after a kill reduces reload time by 0.75 seconds", modIcons.reloadSpeed, 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Hoverclock", "While Focusing in midair, your current velocity is reduced by 80% for 1.5 seconds or until you fire/stop focusing. Getting a kill or touching the ground lets you Hover again.", overclockIcons.hoverclock, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Minimal Clips", "x1.25 Clip Size, -0.2 Reload Time", overclockIcons.magSize, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Active Stability System", "No movement penalty while Focusing, +19% Focus Speed, +0.5 Reload Time", overclockIcons.movespeed, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Hipster", "+2 Rate of Fire, x1.913 Max Ammo, -10% Spread per Shot, x0.85 Max Bloom, x0.5 Recoil, -17 Direct Damage", overclockIcons.baseSpread, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electrocuting Focus Shots", "Focused Shots apply an Electrocute DoT which does "
				+ "an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per Second and slows enemies by 80% for 6 seconds, -25% Focused Shot Multiplier ", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Supercooling Chamber", "+150% Focused Shot Multiplier, x3 Weakpoint Bonus, x0.73 Clip Size, x0.685 Max Ammo, x0.6 Focus Speed, and no movement while focusing", overclockIcons.directDamage, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	public String getDwarfClass() {
		return "Scout";
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
	
	protected double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier1 == 1) {
			toReturn += 10;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 17;
		}
		
		return toReturn;
	}
	protected double getFocusedShotMultiplier() {
		double toReturn = focusedShotMultiplier;
		
		// Additive bonuses first
		if (selectedTier3 == 0) {
			toReturn += 0.25;
		}
		
		if (selectedOverclock == 4) {
			toReturn -= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn += 1.5;
		}
		
		return toReturn;
	}
	protected int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 40;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 1.913;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.685;
		}
		
		return (int) Math.round(toReturn);
	}
	protected int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 6;
		}
		
		if (selectedOverclock == 1) {
			toReturn = (int) Math.ceil(toReturn * 1.25);
		}
		else if (selectedOverclock == 5) {
			toReturn = (int) Math.ceil(toReturn * 0.73);
		}
		
		return toReturn;
	}
	protected double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier5 == 2) {
			// "Killing Machine": if you manually reload within 1 second after a kill, the reload time is reduced by approximately 0.75 seconds.
			// Because Sustained DPS uses this ReloadTime method, I'm choosing to use the Ideal Burst DPS as a quick-and-dirty estimate how often a kill gets scored 
			// so that this doesn't infinitely loop.
			double killingMachineManualReloadWindow = 1.0;
			double killingMachineReloadReduction = 0.75;
			// Just like Gunner/Minigun/Mod/5/CatG, I'm using the incorrect "guess" spawn rates to create a more believable uptime coefficient
			double burstTTK = EnemyInformation.averageHealthPool(false) / calculateSingleTargetDPS(true, false, false, false);
			// Don't let a high Burst DPS increase this beyond a 100% uptime
			double killingMachineUptimeCoefficient = Math.min(killingMachineManualReloadWindow / burstTTK, 1.0);
			double effectiveReloadReduction = killingMachineUptimeCoefficient * killingMachineReloadReduction;
			
			toReturn -= effectiveReloadReduction;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		else if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	protected double getFocusDuration() {
		double focusSpeedCoefficient = 1.0;
		if (selectedTier2 == 0) {
			focusSpeedCoefficient += 0.25;
		}
		
		if (selectedOverclock == 2) {
			focusSpeedCoefficient += 0.19;
		}
		else if (selectedOverclock == 5) {
			focusSpeedCoefficient *= 0.6;
		}
		
		return focusDuration / focusSpeedCoefficient;
	}
	protected double getMovespeedWhileFocusing() {
		double modifier = movespeedWhileFocusing;
		
		if (selectedOverclock == 2) {
			modifier += 0.7;
		}
		else if (selectedOverclock == 5) {
			modifier *= 0;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	protected int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	protected double getWeakpointBonus() {
		double toReturn = 0.0;
		
		if (selectedTier4 == 1) {
			toReturn += 0.2;
		}

		if (selectedOverclock == 5) {
			toReturn *= 3.0;
		}
		
		return toReturn;
	}
	protected double getArmorBreaking() {
		double toReturn = armorBreaking;
		
		if (selectedTier2 == 2) {
			toReturn += 2.2;
		}
		
		return toReturn;
	}
	protected double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn -= 0.3;
		}
		
		if (selectedOverclock == 3) {
			toReturn -= 0.1;
		}
		
		return toReturn;
	}
	protected double getMaxBloom() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.8;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.85;
		}
		
		return toReturn;
	}
	protected double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	protected int getStunDuration() {
		if (selectedTier5 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsRadialDamage() {
		return false;
	}
	
	// Single-target calculations

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateSingleTargetDPS(false, false, false, false);
		}
		else {
			return 0;
		}
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
	public double timeToFireMagazine() {
		return getMagazineSize() / getRateOfFire();
	}
	
	@Override
	public double averageTimeToCauterize() {
		// Neither Hipfire nor Focused Shots can deal Temperature Damage
		return -1;
	}
}
