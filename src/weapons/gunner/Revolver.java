package weapons.gunner;

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
import spreadCurves.RevolverCurve;
import utilities.MathUtils;
import weapons.Weapon;

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
	
	private double revolverHomebrewPowderCoefficient = 1.375;
	
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
		directDamage = 60.0;
		carriedAmmo = 24;
		magazineSize = 4;
		rateOfFire = 2.0;  // bullets per second
		reloadTime = 2.0;  // seconds
		stunChance = 0.5;
		stunDuration = 1.5;  // seconds
		weakpointBonus = 0.25;
		
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
		tier2[0] = new Mod("Increased Caliber Rounds", "+10 Direct Damage", modIcons.directDamage, 2, 0);
		tier2[1] = new Mod("Floating Barrel", "-80% Spread per Shot, x0.75 Recoil", modIcons.recoil, 2, 1);
		tier2[2] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 2, 2);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Super Blowthrough Rounds", "+3 Penetrations", modIcons.blowthrough, 3, 0);
		tier3[1] = new Mod("Explosive Rounds", "+30 Area Damage in a 1.5m radius, x0.5 Direct Damage", modIcons.addedExplosion, 3, 1);
		tier3[2] = new Mod("Hollow-Point Bullets", "+35% Weakpoint Bonus", modIcons.weakpointBonus, 3, 2);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Expanded Ammo Bags", "+12 Max Ammo", modIcons.carriedAmmo, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+10 Direct Damage", modIcons.directDamage, 4, 1);
		
		tier5 = new Mod[2];
		tier5[0] = new Mod("Dead-Eye", "No aim penalty while moving", modIcons.baseSpread, 5, 0);
		// It looks like whenever this procs for the main target, all splash targets get it too, instead of RNG/enemy.
		tier5[1] = new Mod("Neurotoxin Coating", "50% chance to inflict Neurotoxin DoT on all enemies hit by the Revolver. "
				+ "Neurotoxin does an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + " Poison Damage per Second", modIcons.neurotoxin, 5, 1);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakspot has a 75% chance to ricochet into a nearby enemy within 5m.", overclockIcons.ricochet, 0);
		overclocks[1] = new Overclock(Overclock.classification.balanced, "Homebrew Powder", "Anywhere from x0.75 - x2 damage per shot, averaged to x" + revolverHomebrewPowderCoefficient, overclockIcons.homebrewPowder, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Volatile Bullets", "x4 Direct and Area Damage to Burning targets, -10 Direct Damage", overclockIcons.heatDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Six Shooter", "+2 Magazine Size, +6 Max Ammo, +2 Rate of Fire, x1.5 Base Spread, +0.5 Reload Time", overclockIcons.magSize, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Elephant Rounds", "x2 Direct Damage, -1 Mag Size, -12 Max Ammo, +0.5 Reload Time, x0.5 Base Spread, +71% Spread per Shot, x1.5 Max Bloom, x1.5 Recoil, +3.5 Mass", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Magic Bullets", "All bullets that impact terrain automatically ricochet to nearby enemies within 5m (effectively raising accuracy to 100%). +8 Max Ammo, -20 Direct Damage", overclockIcons.ricochet, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
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
			toReturn += 10.0;
		}
		if (selectedTier4 == 1) {
			toReturn += 10.0;
		}
		
		if (selectedOverclock == 2) {
			toReturn -= 10.0;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 20.0;
		}
			
		// Then do multiplicative bonuses
		if (selectedTier3 == 1) {
			toReturn *= 0.5;
		}
		if (selectedOverclock == 1) {
			toReturn *= revolverHomebrewPowderCoefficient;
		}
		else if (selectedOverclock == 4) {
			toReturn *= 2.0;
		}
		return toReturn;
	}
	private double getAreaDamage() {
		double toReturn = 0;
		
		if (selectedTier3 == 1) {
			toReturn = 30.0;
		}
		
		if (selectedOverclock == 1) {
			toReturn *= revolverHomebrewPowderCoefficient;
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
			toReturn += 12;
		}
		if (selectedTier4 == 0) {
			toReturn += 12;
		}
		
		if (selectedOverclock == 3) {
			toReturn += 6;
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
		if (selectedOverclock == 0 || selectedOverclock == 5) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		if (selectedTier3 == 2) {
			toReturn += 0.35;
		}
		return toReturn;
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
		StatsRow[] toReturn = new StatsRow[17];
		
		boolean directDamageModified = selectedTier2 == 0 || selectedTier3 == 1 || selectedTier4 == 1 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		boolean explosiveEquipped = selectedTier3 == 1;
		toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), modIcons.areaDamage, explosiveEquipped || selectedOverclock == 1, explosiveEquipped);
		
		toReturn[2] = new StatsRow("AoE Radius:", getAoERadius(), modIcons.aoeRadius, explosiveEquipped, explosiveEquipped);
		
		toReturn[3] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, selectedOverclock == 3 || selectedOverclock == 4);
		
		boolean carriedAmmoModified = selectedTier2 == 2 || selectedTier4 == 0 || (selectedOverclock > 2 && selectedOverclock < 6);
		toReturn[4] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, selectedOverclock == 3);
		
		toReturn[6] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 0 || selectedOverclock == 3 || selectedOverclock == 4);
		
		toReturn[7] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier3 == 2);
		
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(stunChance), modIcons.homebrewPowder, false);
		
		toReturn[9] = new StatsRow("Stun Duration:", stunDuration, modIcons.stun, false);
		
		toReturn[10] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[11] = new StatsRow("Weakpoint Chain Hit Chance:", convertDoubleToPercentage(0.75), modIcons.homebrewPowder, selectedOverclock == 0, selectedOverclock == 0);
		
		boolean canRicochet = selectedOverclock == 0 || selectedOverclock == 5;
		toReturn[12] = new StatsRow("Max Ricochets:", getMaxRicochets(), modIcons.ricochet, canRicochet, canRicochet);
		
		boolean baseSpreadModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 4;
		toReturn[13] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		boolean spreadPerShotModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[14] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, spreadPerShotModified, spreadPerShotModified);
		
		toReturn[15] = new StatsRow("Max Bloom:", convertDoubleToPercentage(getMaxBloom()), modIcons.baseSpread, selectedOverclock == 4, selectedOverclock == 4);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 4;
		toReturn[15] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		toReturn[16] = new StatsRow("Recoil Mass:", getMass(), modIcons.recoil, selectedOverclock == 4, selectedOverclock == 4);
		
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
	
	@Override
	protected void setAoEEfficiency() {
		if (selectedTier3 == 1) {
			aoeEfficiency = calculateAverageAreaDamage(getAoERadius(), 0.75, 0.5);
		}
		else {
			aoeEfficiency = new double[3];
		}
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

	@Override
	public double calculateAdditionalTargetDPS() {
		/*
			There are 8 combinations of ways for the Revolver to hit an additional target, based on various combinations of
			the Overclocks "Chain Hit" and "Magic Bullets", and the Tier 3 Mods "Super Blowthrough Rounds" and "Explosive Rounds"
		*/
		double sustainedAdditionalDPS;
		double directDamage = getDirectDamage();
		double areaDamage = 0;
		if (selectedTier3 == 1) {
			areaDamage = getAreaDamage() * aoeEfficiency[1];
		}
		double magazineSize = getMagazineSize();
		double timeToFireMagazineAndReload = (magazineSize / getCustomRoF()) + getReloadTime();
		
		// If Super Blowthrough Rounds is equipped, then the ricochets from either "Chain Hit" or "Magic Bullets" won't affect the additional DPS
		if (selectedTier3 == 0) {
			// Because Super Blowthrough Rounds are just the same damage to another enemy behind the primary target (or from a ricochet), return Ideal Sustained DPS
			sustainedAdditionalDPS = magazineSize * directDamage / timeToFireMagazineAndReload;
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 1 && selectedOverclock != 5) {
			// Explosive Rounds are just the Area Damage, so I have to re-model the sustained DPS formula here
			sustainedAdditionalDPS = magazineSize * areaDamage / timeToFireMagazineAndReload;
		}
		
		// Only "Chain Hit" OR "Chain Hit" + Explosive Rounds
		else if (selectedOverclock == 0 && selectedTier3 != 0) {
			// If "Chain Hit" is equipped, 75% of bullets that hit a weakpoint will ricochet to nearby enemies.
			// Making the assumption that the ricochet won't hit another weakpoint, and will just do normal damage.
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double numBulletsRicochetPerMagazine = Math.round(ricochetProbability * magazineSize);
			
			sustainedAdditionalDPS = numBulletsRicochetPerMagazine * (directDamage + areaDamage) / timeToFireMagazineAndReload;
		}
		
		// Only "Magic Bullets"
		else if (selectedOverclock == 5 && selectedTier3 != 0 && selectedTier3 != 0) {
			// "Magic Bullets" mean that any bullet that MISSES the primary target will try to automatically ricochet to a nearby enemy.
			// This can be modeled by returning (1 - Accuracy) * Ideal Sustained DPS
			sustainedAdditionalDPS = (1.0 - getGeneralAccuracy() / 100.0) * magazineSize * directDamage / timeToFireMagazineAndReload;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// This combination is the hardest to model: when a missed bullet ricochets, it still deals an explosion of damage on the ground before redirecting to the new target. This means that if you shoot the ground next to an
			// enemy with this combination, they'll take the Area Damage, followed by the Direct + Area Damage of the bullet after it redirects.
			sustainedAdditionalDPS = magazineSize * (directDamage + 2 * areaDamage) / timeToFireMagazineAndReload;
		}
		else {
			// This means that none of the four mods/OCs that hit additional targets are equipped, therefore it cannot do Additional Target DPS.
			return 0;
		}
		
		if (selectedTier5 == 1) {
			sustainedAdditionalDPS += DoTInformation.Neuro_DPS;
		}
		
		return sustainedAdditionalDPS;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double multitargetDirectDamageMultiplier = calculateBlowthroughDamageMultiplier(getMaxPenetrations());
		
		// OC "Chain Hit" adds a 75% chance to ricochet to another enemy within 5m if it hits a Weakpoint
		// OC "Magic Bullets" only ricochets off of terrain, so it doesn't directly increase the Direct Damage dealt.
		if (selectedOverclock == 0) {
			multitargetDirectDamageMultiplier += 0.75 * getWeakpointAccuracy() / 100.0;
		}
		
		double damagePerBullet = getDirectDamage();
		if (selectedTier3 == 1) {
			damagePerBullet += getAreaDamage() * aoeEfficiency[2] * aoeEfficiency[1];
		}
		
		double baseTotalDamage = (getMagazineSize() + getCarriedAmmo()) * damagePerBullet * multitargetDirectDamageMultiplier;
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedTier5 == 1) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.5) / getCustomRoF();
			double neurotoxinDoTTotalDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = calculateMaxNumTargets() * (calculateFiringDuration() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTTotalDamagePerEnemy * estimatedNumEnemiesKilled;
		}

		return baseTotalDamage + neurotoxinDoTTotalDamage;
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
			if (selectedOverclock == 0) {
				// Only Chain Hit can bounce to another target from a current one. Magic Bullets only bounces off of terrain, so it doesn't increase the Blowthrough count.
				toReturn += getMaxRicochets();
			}
			return toReturn;
		}
		
		// Only Explosive
		else if (selectedTier3 == 1 && selectedOverclock != 0 && selectedOverclock != 5) {
			// From my limited testing, it appears that the full damage radius == full radius, so the efficiency will be 100%
			return (int) aoeEfficiency[2];
		}
		
		// Only "Chain Hit"
		else if (selectedOverclock == 0 && selectedTier3 != 0 && selectedTier3 != 1) {
			return 1 + getMaxRicochets();
		}
		
		// "Chain Hit" + Explosive
		else if (selectedOverclock == 0 && selectedTier3 == 1) {
			// Because the second hit is guaranteed to hit another primary target, this is 2*numTargets - overlap
			// I'm guessing that of the 8 Glyphid Grunts, about 3 would be hit by both explosions.
			return (int) (2 * aoeEfficiency[2]) - 3;
		}
		
		// "Magic Bullets" + Explosive
		else if (selectedOverclock == 5 && selectedTier3 == 1) {
			// Because the bullet has to first MISS a target, but the ricochet explodes, this is effectively (2*numTargets - 1) - overlap so that the primary target doesn't get double-counted
			// I'm choosing to model the overlapping Grunts as 5 instead of 3, because it's likely that the bullet lands near the center target that it ricochets to so more of the Grunts would 
			// be hit by both explosions.
			return (int) (2 * aoeEfficiency[2] - 1) - 5;
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
															getWeakpointBonus(), 1.0, getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDirectDamage() + getAreaDamage()) * UtilityInformation.ArmorBreak_Utility;
		
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
		// This is copy/pasted from MultiTargetDamage, but only uses MagSize instead of MagSize + CarriedAmmo
		double multitargetDirectDamageMultiplier = calculateBlowthroughDamageMultiplier(getMaxPenetrations());
		
		// OC "Chain Hit" adds a 75% chance to ricochet to another enemy within 5m if it hits a Weakpoint
		// OC "Magic Bullets" only ricochets off of terrain, so it doesn't directly increase the Direct Damage dealt.
		if (selectedOverclock == 0) {
			multitargetDirectDamageMultiplier += 0.75 * getWeakpointAccuracy() / 100.0;
		}
		
		double damagePerBullet = getDirectDamage();
		if (selectedTier3 == 1) {
			damagePerBullet += getAreaDamage() * aoeEfficiency[2] * aoeEfficiency[1];
		}
		
		double baseTotalDamage = getMagazineSize() * damagePerBullet * multitargetDirectDamageMultiplier;
		
		double neurotoxinDoTTotalDamage = 0;
		if (selectedTier5 == 1) {
			double timeBeforeNeuroProc = MathUtils.meanRolls(0.5) / getCustomRoF();
			double neurotoxinDoTTotalDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeNeuroProc, DoTInformation.Neuro_SecsDuration, DoTInformation.Neuro_DPS);
			
			double estimatedNumEnemiesKilled = calculateMaxNumTargets() * (timeToFireMagazine() / averageTimeToKill());
			
			neurotoxinDoTTotalDamage = neurotoxinDoTTotalDamagePerEnemy * estimatedNumEnemiesKilled;
		}

		return baseTotalDamage + neurotoxinDoTTotalDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage(), 1, getAreaDamage(), 1.0, getWeakpointBonus(), getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
