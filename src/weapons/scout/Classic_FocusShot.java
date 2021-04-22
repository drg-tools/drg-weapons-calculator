package weapons.scout;

import guiPieces.customButtons.ButtonIcons.modIcons;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class Classic_FocusShot extends Classic {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
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
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "M1000 Classic (Focused Shots)";
	}
	
	@Override
	public Classic_FocusShot clone() {
		return new Classic_FocusShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "Classic_FocusShot";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	protected int getCarriedAmmo() {
		// Divide by 2 to account for firing two ammo per focused shot
		// Use ceiling function because you can do a fully-charged Focus Shot with only 1 ammo if there's no ammo left in the clip
		return (int) Math.ceil(super.getCarriedAmmo() / 2.0);
	}
	@Override
	protected int getMagazineSize() {
		// Divide by 2 to account for firing two ammo per focused shot
		return super.getMagazineSize() / 2;
	}
	@Override
	public double getRateOfFire() {
		double delayBetweenShots = 1 / rateOfFire;
		if (selectedOverclock == 3) {
			// Hipster's +3 RoF translates to a shorter delay between focused shots.
			delayBetweenShots = 1 / 7.0;
		}
		// Because the max RoF will never be achieved with Focus Shots, instead model the RoF as the inverse of the Focus Duration
		return 1.0 / (delayBetweenShots + getFocusDuration());
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[14];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedOverclock == 3 || selectedTier1 == 1);
		
		boolean multiplierModified = selectedTier3 == 0 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Focused Shot Multiplier:", convertDoubleToPercentage(getFocusedShotMultiplier()), modIcons.directDamage, multiplierModified);
		
		double delayBetweenShots = 1 / rateOfFire;
		if (selectedOverclock == 3) {
			// Hipster's +3 RoF translates to a shorter delay between focused shots.
			delayBetweenShots = 1 / 7.0;
		}
		toReturn[2] = new StatsRow("Delay Between Focused Shots:", delayBetweenShots, modIcons.duration, selectedOverclock == 3);
		
		toReturn[3] = new StatsRow("Focus Shot Charge-up Duration:", getFocusDuration(), modIcons.chargeSpeed, selectedTier2 == 0 || selectedOverclock == 2 || selectedOverclock == 5);
		
		toReturn[4] = new StatsRow("Clip Size:", getMagazineSize(), modIcons.magSize, selectedTier3 == 1 || selectedOverclock == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), modIcons.carriedAmmo, carriedAmmoModified);
		
		boolean RoFmodified = selectedTier2 == 0 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, RoFmodified);
		
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), modIcons.reloadSpeed, selectedTier5 == 2 || selectedOverclock == 1 || selectedOverclock ==  2);
		
		toReturn[8] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), modIcons.weakpointBonus, selectedTier4 == 1);
		
		toReturn[9] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier2 == 2);
		
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, selectedTier5 == 0, selectedTier5 == 0);
		
		toReturn[11] = new StatsRow("Max Penetrations:", getMaxPenetrations(), modIcons.blowthrough, selectedTier4 == 0, selectedTier4 == 0);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), modIcons.recoil, recoilModified, recoilModified);
		
		toReturn[13] = new StatsRow("Movespeed While Focusing: (m/sec)", getMovespeedWhileFocusing(), modIcons.movespeed, selectedOverclock == 2 || selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	// Single-target calculations
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		double duration;
		if (burst) {
			duration = ((double) getMagazineSize()) / getRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage() * getFocusedShotMultiplier();
		
		// Damage wasted by Armor
		if (armorWasting && !statusEffects[1]) {
			double armorWaste = 1.0 - MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]);
			directDamage *= armorWaste;
		}
		
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
	public double calculateMaxMultiTargetDamage() {
		double totalDamageDealt = calculateBlowthroughDamageMultiplier(getMaxPenetrations()) * (getMagazineSize() + getCarriedAmmo()) * getDirectDamage() * getFocusedShotMultiplier();
		
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
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage() * getFocusedShotMultiplier(), getWeakpointBonus());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public double averageOverkill() {
		overkillPercentages = EnemyInformation.overkillPerCreature(getDirectDamage() * getFocusedShotMultiplier());
		return MathUtils.vectorDotProduct(overkillPercentages[0], overkillPercentages[1]);
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Manually aimed
		return -1.0;
	}
	
	@Override
	public int breakpoints() {
		// Both Direct and Area Damage can have 5 damage elements in this order: Kinetic, Explosive, Fire, Frost, Electric
		double[] directDamage = new double[5];
		directDamage[0] = getDirectDamage() * getFocusedShotMultiplier();  // Kinetic
		
		double[] areaDamage = new double[5];
		
		// DoTs are in this order: Electrocute, Neurotoxin, Persistent Plasma, and Radiation
		double[] dot_dps = new double[4];
		double[] dot_duration = new double[4];
		double[] dot_probability = new double[4];
		
		if (selectedOverclock == 4) {
			dot_dps[0] = DoTInformation.Electro_DPS;
			dot_duration[0] = 4.0;
			dot_probability[0] = 1.0;
		}
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, dot_dps, dot_duration, dot_probability, 
															getWeakpointBonus(), getArmorBreaking(), getRateOfFire(), 0.0, 0.0, 
															statusEffects[1], statusEffects[3], false, false);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// OC "Active Stability System" removes the movespeed penalty while Focusing
		utilityScores[0] = (getMovespeedWhileFocusing() - MathUtils.round(movespeedWhileFocusing * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// GreyHound tells me that OC "Hoverclock" slows your velocity by 80% for 1.5 seconds
		if (selectedOverclock == 0) {
			// Duration divided by the movespeed multiplier; 1.5 * 5 = 7.5
			utilityScores[0] += 1.5 / (1.0 - 0.8);
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
		
		// T5.B "Precision Terror" does 2.5 Fear in a 4m radius
		if (selectedTier5 == 1) {
			double probabilityToHitWeakpoint = EnemyInformation.probabilityBulletWillHitWeakpoint();
			int numGlyphidsFeared = calculateNumGlyphidsInRadius(4.0);
			double probabilityToFear = calculateFearProcProbability(2.5);
			// Although it is technically possible to electrocute a Feared enemy with Electrocuting Focus Shots and Blowthrough Rounds, it's so unlikely to happen that I'm choosing not to model that overlap.
			utilityScores[4] = probabilityToHitWeakpoint * probabilityToFear * numGlyphidsFeared * EnemyInformation.averageFearDuration() * UtilityInformation.Fear_Utility;
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
	public double damageWastedByArmor() {
		double weakpointAccuracy = EnemyInformation.probabilityBulletWillHitWeakpoint() * 100.0;
		damageWastedByArmorPerCreature = EnemyInformation.percentageDamageWastedByArmor(getDirectDamage() * getFocusedShotMultiplier(), 1, 0.0, getArmorBreaking(), getWeakpointBonus(), 100.0, weakpointAccuracy);
		return 100 * MathUtils.vectorDotProduct(damageWastedByArmorPerCreature[0], damageWastedByArmorPerCreature[1]) / MathUtils.sum(damageWastedByArmorPerCreature[0]);
	}
}
