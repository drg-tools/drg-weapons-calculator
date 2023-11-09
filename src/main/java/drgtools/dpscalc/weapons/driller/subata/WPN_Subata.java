package drgtools.dpscalc.weapons.driller.subata;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.modIcons;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons.overclockIcons;
import drgtools.dpscalc.modelPieces.UtilityInformation;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.modelPieces.accuracy.CircularHitscanAccuracyEstimator;
import drgtools.dpscalc.modelPieces.accuracy.RecoilSettings;
import drgtools.dpscalc.modelPieces.accuracy.SpreadSettings;
import drgtools.dpscalc.modelPieces.damage.*;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.MaterialFlag;
import drgtools.dpscalc.modelPieces.damage.DamageFlags.RicochetFlag;
import drgtools.dpscalc.modelPieces.statusEffects.PushSTEComponent;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.modelPieces.statusEffects.commonSTEs.STE_OnFire;
import drgtools.dpscalc.weapons.Weapon;

public class WPN_Subata extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private int carriedAmmo;
	private int magazineSize;
	private double rateOfFire;
	private double reloadTime;
	private double weakpointBonus;
	private double armorBreaking;

	// Damage Components
	private DamageComponent damagePerHitscan;
	private DamageComponent NCTC;
	private DamageComponent explosiveReload;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public WPN_Subata() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public WPN_Subata(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public WPN_Subata(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Subata 120";
		weaponPic = WeaponPictures.subata;
		customizableRoF = true;
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 12;
		carriedAmmo = 144;
		magazineSize = 12;
		rateOfFire = 8.0;
		reloadTime = 1.9;
		weakpointBonus = 0.25;
		// Subata has a hidden 50% Armor Breaking penalty (credit to Elythnwaen for pointing this out to me)
		armorBreaking = 0.5;
		
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
		tier1[0] = new Mod("Improved Alignment", "x0 Base Spread", modIcons.baseSpread, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+4 Magazine Size", modIcons.magSize, 1, 1);

		tier2 = new Mod[2];
		tier2[0] = new Mod("Expanded Ammo Bags", "+48 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Improved Propellant", "+3 Direct Damage", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Recoil Compensator", "-33% Spread per Shot, x0.5 Recoil", modIcons.baseSpread, 3, 0);
		tier3[1] = new Mod("2-Round Burst", "Changes the Subata to fire in bursts of 2 bullets at 20 RoF during the burst. " +
				"If both bullets hit an enemy, the second bullet gets +350% Armor Breaking. Additionally, -0.25 Mass and -4 Rate of Fire. ", modIcons.rateOfFire, 3, 1);
		tier3[2] = new Mod("Quickfire Ejector", "-0.6 Reload Time", modIcons.reloadSpeed, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Hollow-Point Bullets", "+75% Weakpoint Bonus", modIcons.weakpointBonus, 4, 0);
		tier4[1] = new Mod("High Velocity Rounds", "+3 Direct Damage", modIcons.directDamage, 4, 1);
		tier4[2] = new Mod("Expanded Ammo Bags", "+48 Max Ammo", modIcons.carriedAmmo, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Volatile Bullets", "When shooting Burning enemies, 50% of the Direct Damage gets added as Fire-element & Heat " +
				"(extends the Burn duration).", modIcons.heatDamage, 5, 0);
		tier5[1] = new Mod("Blowthrough Rounds", "+1 Blowthrough", modIcons.blowthrough, 5, 1);
		tier5[2] = new Mod("Neuro-Corrosive Toxic Catalyst ", "Shooting an enemy that is afflicted with Corrosion, Sludge Puddle, " +
				"or Neurotoxin will apply 2 Status Effects. The first one lasts 5 seconds, and each bullet that hits will increase the DPS by +3.2, " +
				"up to a maximum of 30 stacks (96 DPS). After its duration expires, you must apply a new instance and start stacking it again. The " +
				"second Status Effect lasts 4.5 seconds, and its duration can be refreshed. If an enemy dies while the second one is active, then " +
				"they will explode and damage nearby enemies proportional to the number of active stacks of the first effect. The explosion does " +
				"(60 + 20*stacks) Typeless Damage, and the radius is (0.5 + SqRt[stacks]) meters. ", modIcons.special, 5, 2, false);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Chain Hit", "Any shot that hits a weakpoint has a 75% chance " +
				"to ricochet into a nearby enemy within 10m.", overclockIcons.ricochet, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Homebrew Powder", "Anywhere from x0.8 - x1.4 damage per shot, " +
				"averaged to x" + homebrewPowderCoefficient, overclockIcons.homebrewPowder, 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Oversized Magazine", "+10 Magazine Size, +0.5 Reload Time", overclockIcons.magSize, 2);
		overclocks[3] = new Overclock(Overclock.classification.unstable, "Automatic Fire", "Changes the Subata from semi-automatic to " +
				"fully automatic, +2 Rate of Fire, +100% Base Spread, x2.5 Recoil", overclockIcons.rateOfFire, 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Explosive Reload", "Bullets that deal damage to an enemy's " +
				"healthbar leave behind a detonator that deals 42 Internal Damage to the enemy upon reloading. If reloading can kill an enemy, an " +
				"icon will appear next to their healthbar and they'll be slowed by 90% for 2 seconds. In exchange: x0.5 Magazine Size and x0.5 Max Ammo", overclockIcons.specialReload, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Tranquilizer Rounds", "Every bullet has a 50% chance to stun an " +
				"enemy for 6 seconds, and a separate 50% chance to slow the enemy by 50% for 4 seconds. In exchange, -4 Magazine Size and x0.75 Rate of Fire.", overclockIcons.stun, 5);
		
		// This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
		modsAndOCsInitialized = true;
	}
	
	@Override
	public WPN_Subata clone() {
		return new WPN_Subata(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Driller";
	}
	public String getSimpleName() {
		return "Subata";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.drillerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.subataGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public boolean isRofCustomizable() {
		// I'm choosing to disable RoF customization when the user equips T3.B "2 Round Burst" or OC "Automatic Fire", for obvious reasons.
		if (selectedTier3 == 1 || selectedOverclock == 3) {
			return false;
		}
		else {
			return customizableRoF;
		}
	}
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		if (selectedTier2 == 1) {
			toReturn += 3;
		}
		if (selectedTier4 == 1) {
			toReturn += 3;
		}
		
		return toReturn;
	}
	private int getExplosiveReloadDamage() {
		// Equipping the Overclock "Explosive Reload" leaves a detonator inside enemies that does 42 Internal Damage per Bullet that deals damage to an enemy upon reloading the Subata
		if (selectedOverclock == 4) {
			return 42;
		}
		else { 
			return 0;
		}
	}
	private int getCarriedAmmo() {
		int toReturn = carriedAmmo;
		
		if (selectedTier2 == 0) {
			toReturn += 48;
		}
		if (selectedTier4 == 2) {
			toReturn += 48;
		}
		
		if (selectedOverclock == 4) {
			toReturn /= 2;
		}
		
		return toReturn;
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier1 == 1) {
			toReturn += 4;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 10;
		}
		else if (selectedOverclock == 4) {
			toReturn /= 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 4;
		}
		
		return toReturn;
	}
	@Override
	public double getRateOfFire() {
		double toReturn = rateOfFire;

		if (selectedTier3 == 1) {
			toReturn -= 4.0;
		}

		if (selectedOverclock == 3) {
			toReturn += 2.0;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.75;
		}
		
		return toReturn;
	}
	private boolean hasBurstFire() {
		return selectedTier3 == 1;
	}
	private int getBurstSize() {
		if (hasBurstFire()) {
			return 2;
		}
		else {
			return 0;
		}
	}
	private double getBurstInterval() {
		if (hasBurstFire()) {
			// 2-Round Burst is 20 RoF
			return 0.05;
		}
		else {
			return 0;
		}
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier3 == 2) {
			toReturn -= 0.6;
		}
		
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		
		return toReturn;
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 0) {
			toReturn += 0.75;
		}
		
		return toReturn;
	}
	private int getNumBlowthroughs() {
		if (selectedTier5 == 1) {
			return 1;
		}
		else {
			return 0;
		}
	}
	// TODO: this method might not be needed anymore?
	private int getMaxRicochets() {
		// According to GreyHound, this ricochet searches for enemies within 10m
		if (selectedOverclock == 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn += 1.0;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 0) {
			toReturn *= 0.0;
		}
		
		return toReturn;
	}
	private double getSpreadPerShot() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn -= 0.33;
		}
		
		return toReturn;
	}
	private double getSpreadPerShotValue() {
		if (selectedTier3 == 0) {
			return 1.0;
		}
		else {
			return 1.5;
		}
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier3 == 0) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 2.5;
		}
		
		return toReturn;
	}
	private double getRecoilMass() {
		double toReturn = 1.0;

		if (selectedTier3 == 1) {
			toReturn -= 0.25;
		}

		return toReturn;
	}
	private double getStunChance() {
		if (selectedOverclock == 5) {
			return 0.5;
		}
		else {
			return 0;
		}
	}
	private int getStunDuration() {
		if (selectedOverclock == 5) {
			return 6;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public double getRecommendedRateOfFire() {
		return Math.min(getRateOfFire(), 6);
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[15];
		
		boolean directDamageModified = selectedTier2 == 1 || selectedTier3 == 0 || selectedTier4 == 1 || selectedOverclock == 1;
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, directDamageModified);
		
		// This stat only applies to OC "Explosive Reload"
		toReturn[1] = new StatsRow("Explosive Reload Damage:", getExplosiveReloadDamage(), modIcons.areaDamage, selectedOverclock == 4, selectedOverclock == 4);
		
		boolean magSizeModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), modIcons.magSize, magSizeModified);
		
		boolean carriedAmmoModified = selectedTier2 == 0 || selectedTier3 == 2 || selectedOverclock == 4;
		toReturn[3] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		toReturn[4] = new StatsRow("Rate of Fire:", getCustomRoF(), modIcons.rateOfFire, selectedOverclock == 3 || selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier1 == 2 || selectedOverclock == 2);
		
		toReturn[6] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 0);
		
		// Display Subata's hidden 50% armor break penalty
		toReturn[7] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(armorBreaking), modIcons.armorBreaking, false);
		
		// These two stats only apply to OC "Tranquilizer Rounds"
		boolean tranqRoundsEquipped = selectedOverclock == 5;
		toReturn[8] = new StatsRow("Stun Chance:", convertDoubleToPercentage(getStunChance()), modIcons.homebrewPowder, tranqRoundsEquipped, tranqRoundsEquipped);
		
		toReturn[9] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, tranqRoundsEquipped, tranqRoundsEquipped);
		
		boolean chainHitEquipped = selectedOverclock == 0;
		toReturn[10] = new StatsRow("Weakpoint Chain Hit Chance:", convertDoubleToPercentage(0.75), modIcons.homebrewPowder, chainHitEquipped, chainHitEquipped);
		toReturn[11] = new StatsRow("Max Ricochets:", getMaxRicochets(), modIcons.ricochet, chainHitEquipped, chainHitEquipped);
		
		boolean baseSpreadModified = selectedTier1 == 0 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[13] = new StatsRow("Spread per Shot:", convertDoubleToPercentage(getSpreadPerShot()), modIcons.baseSpread, selectedTier3 == 1, selectedTier3 == 1);
		
		boolean recoilModified = selectedOverclock == 3 || selectedTier3 == 1;
		toReturn[14] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		return toReturn;
	}

	/****************************************************************************************
 	* Rebuild Methods
 	****************************************************************************************/

	@Override
	protected void rebuildAccuracyEstimator() {
		// In the parsed game files, I can see that Subata does technically have a Spread Curve.
		// However, it appears to just be a 1:1 match of having no curve at all, so I'm choosing not to implement it.
		SpreadSettings spread = new SpreadSettings(
			1.5 * getBaseSpread(),
			1.5 * getBaseSpread(),
			getSpreadPerShotValue(),
			7.5,
			3.0,
			0.5
		);

		RecoilSettings recoil = new RecoilSettings(
			30.0 * getRecoil(),
			10.0 * getRecoil(),
			getRecoilMass(),
			60.0
		);

		if (hasBurstFire()) {
			accEstimator = new CircularHitscanAccuracyEstimator(getRateOfFire(), getMagazineSize(), getBurstSize(), getBurstInterval(), spread, recoil);
		}
		else {
			accEstimator = new CircularHitscanAccuracyEstimator(getRateOfFire(), getMagazineSize(), 1, 0, spread, recoil);
		}
	}

	@Override
	protected void rebuildDamageComponents() {
		double avgRoF = getRateOfFire();

		damagePerHitscan = new DamageComponent(
			getDirectDamage(),
			DamageElement.kinetic,
			armorBreaking,
			1.0,
			null
		);
		damagePerHitscan.setWeakpointBonus(getWeakpointBonus());

		// T3.B "2-Round Burst"
		if (selectedTier3 == 1) {
			// If both bullets hit, the 2nd one does +350% AB. To model that, I'm going to multiply the +3.5 bonus by General Accuracy^2
			// The logic is that the probability to hit both shots is equal to the probability to hit each shot, twice.
			// This requires that the AccuracyEstimator needs to be rebuilt BEFORE DamageComponents!
			double enhancedAB = (2*armorBreaking + 3.5 * Math.pow(getGeneralAccuracy(),2)) / 2.0;
			damagePerHitscan.setArmorBreaking(enhancedAB);

			avgRoF = calculateAverageBurstRoF(avgRoF, getBurstSize(), getBurstInterval());
		}

		// Homebrew Powder must be applied before other DamageConversions in order to model the game engine's shenanigans
		if (selectedOverclock == 1) {
			damagePerHitscan.applyHomebrewDamage(0.8, 1.4);
		}

		// Volatile Bullets
		if (selectedTier5 == 0) {
			StatusEffect[] triggers = {new STE_OnFire()};
			DamageConversion volatileBulletsDC = new DamageConversion(0.5, true, DamageElement.fireAndHeat);
			damagePerHitscan.addConditionalDamageConversion(new ConditionalDamageConversion(triggers, volatileBulletsDC));
		}
		// Blowthrough Rounds
		else if (selectedTier5 == 1) {
			damagePerHitscan.setNumBlowthroughs(getNumBlowthroughs());
		}
		// Neuro-Corrosive Toxic Catalyst
		else if (selectedTier5 == 2) {
			// TODO: implement NCTC
			// NCTC = new DamageComponent();
		}

		// Chain Hit
		if (selectedOverclock == 0) {
			damagePerHitscan.setRicochet(getWeakpointAccuracy(), 0.75, RicochetFlag.onlyCreatureWeakpoints, 10);
		}
		// Tranquilizer Rounds
		else if (selectedOverclock == 5) {
			damagePerHitscan.setStun(false, getStunChance(), getStunDuration());
			damagePerHitscan.addStatusEffectApplied(new PushSTEComponent(avgRoF, 0.5, new STE_TranqSlowdown()));
		}

		// Explosive Reload
		if (selectedOverclock == 4) {
			explosiveReload = new DamageComponent(
				getExplosiveReloadDamage(),
				DamageElement.internal,
				false,
				false,
				false,
				false,
				true,
				0.0,
				1.0,
				null
			);

			dmgInstance = new DamageInstance(damagePerHitscan, new DamageComponent[]{explosiveReload});
		}
		else {
			dmgInstance = new DamageInstance(damagePerHitscan);
		}

		damageComponentsInitialized = true;
	}

	@Override
	public boolean currentlyDealsRadialDamage() {
		return false;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double generalAccuracy = 1.0;
		double weakpointAccuracy = 0.0;

		double directDamage = damagePerHitscan.calculateComplicatedDamageDealtPerHit(targetDummy, MaterialFlag.normalFlesh);
		double directWeakpointDamage = directDamage;
		double explosiveReloadDamage = 0;

		double duration;
		double magSize = getMagazineSize();

		if (accuracy) {
			generalAccuracy = getGeneralAccuracy();
		}

		if (burst) {
			duration = magSize / getCustomRoF();
		}
		else {
			duration = (magSize / getCustomRoF()) + getReloadTime();
		}

		if (selectedOverclock == 4) {
			explosiveReloadDamage = explosiveReload.calculateComplicatedDamageDealtPerHit(targetDummy, MaterialFlag.normalFlesh);
		}
		
		// Damage wasted by Armor
		if (armorWasting && !targetDummy.currentlyFrozen()) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			directDamage *= armorWaste;
		}

		if (weakpoint && !targetDummy.currentlyFrozen()) {
			weakpointAccuracy = getWeakpointAccuracy();
			directWeakpointDamage = damagePerHitscan.calculateComplicatedDamageDealtPerHit(targetDummy, MaterialFlag.weakpoint);
		}

		return (
			weakpointAccuracy * directWeakpointDamage +
			(generalAccuracy - weakpointAccuracy) * directDamage +
			generalAccuracy * explosiveReloadDamage
		) * magSize / duration;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		double directDamage = damagePerHitscan.calculateComplicatedDamageDealtPerHit(targetDummy, MaterialFlag.normalFlesh);
		double secondaryTargetMultiplier = damagePerHitscan.calculateAverageSecondaryTargetDamageMultiplier();

		// Special case: T5.B Blowthrough Rounds + Explosive Reload
		double explosiveReloadDamage = 0;
		if (selectedTier5 == 1 && selectedOverclock == 4) {
			explosiveReloadDamage = explosiveReload.calculateComplicatedDamageDealtPerHit(targetDummy, MaterialFlag.normalFlesh);
		}

		double magSize = getMagazineSize();
		double timeToFireMagazineAndReload = (magSize / getCustomRoF()) + getReloadTime();
		return secondaryTargetMultiplier * (directDamage + explosiveReloadDamage) * magSize / timeToFireMagazineAndReload;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		if (selectedOverclock == 0) {
			// Chain Hit
			double ricochetProbability = 0.75 * getWeakpointAccuracy() / 100.0;
			double totalNumRicochets = Math.round(ricochetProbability * (getMagazineSize() + getCarriedAmmo()));
			
			return (getMagazineSize() + getCarriedAmmo() + totalNumRicochets) * getDirectDamage();
		}
		else {
			// Because the OCs Chain Hit and Explosive Reload are mutually exclusive, the Area Damage only needs to be called here.
			return (getMagazineSize() + getCarriedAmmo()) * (getDirectDamage() + getExplosiveReloadDamage());
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		// Explosive Reload cannot hit any targets not already hit by damagePerHitscan, because damagePerHitscan is what applies ER.
		return damagePerHitscan.calculateTheoreticalMaxNumberOfEnemiesHitSimultaneously();
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
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage(), getWeakpointBonus()) + getExplosiveReloadDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(dmgInstance);
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return accEstimator.estimateAccuracy(weakpointAccuracy);
	}
	
	@Override
	public int breakpoints() {
		breakpoints = EnemyInformation.calculateBreakpoints(dmgInstance, getRateOfFire(), statusEffects[3], statusEffects[1]);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		// The Area damage from Explosive Reload doesn't affect the chance to break the Light Armor plates since it's not part of the initial projectile
		utilityScores[2] = calculateProbabilityToBreakLightArmor(damagePerHitscan) * UtilityInformation.ArmorBreak_Utility;

		// TODO: Tranq Rounds' slow
		// Tranq rounds = 50% chance to stun, 6 second stun
		if (selectedOverclock == 5) {
			utilityScores[5] = getStunChance() * getStunDuration() * UtilityInformation.Stun_Utility;
		}
		else {
			utilityScores[5] = 0;
		}
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double averageTimeToCauterize() {
		return -1;
	}
	
	@Override
	public double damagePerMagazine() {
		return getMagazineSize() * (getDirectDamage() + getExplosiveReloadDamage());
	}
	
	@Override
	public double timeToFireMagazine() {
		return getMagazineSize() / getCustomRoF();
	}
	
	@Override
	public double damageWastedByArmor() {
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(dmgInstance, getGeneralAccuracy(), getWeakpointAccuracy());
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
