package drillerWeapons;

import guiPieces.ButtonIcons.modIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class EPC_RegularShot extends EPC {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public EPC_RegularShot() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public EPC_RegularShot(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public EPC_RegularShot(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "EPC (Regular Shots)";
	}
	
	@Override
	public EPC_RegularShot clone() {
		return new EPC_RegularShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "EPC_RegularShot";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public StatsRow[] getStats() {
		boolean coolingRateModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 4;
		
		StatsRow[] toReturn = new StatsRow[8];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), modIcons.directDamage, selectedTier1 == 0 || selectedOverclock == 3);
		
		toReturn[1] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getRegularShotVelocity()), modIcons.projectileVelocity, selectedTier2 == 1, selectedTier2 == 1);
		
		boolean heatPerShotModified = selectedTier5 == 1 || selectedOverclock == 2 || selectedOverclock == 3;
		toReturn[2] = new StatsRow("Heat/Shot:", getHeatPerRegularShot(), modIcons.blank, heatPerShotModified);
		
		toReturn[3] = new StatsRow("Shots Fired Before Overheating:", getNumRegularShotsBeforeOverheat(), modIcons.magSize, coolingRateModified || heatPerShotModified);
		
		boolean batterySizeModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Battery Size:", getBatterySize(), modIcons.carriedAmmo,  batterySizeModified);
		
		toReturn[5] = new StatsRow("Rate of Fire:", rateOfFire, modIcons.rateOfFire, false);
		
		toReturn[6] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRateModifier()), modIcons.coolingRate, coolingRateModified);
		
		toReturn[7] = new StatsRow("Cooldown After Overheating:", getCooldownDuration(), modIcons.blank, coolingRateModified);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Because this only models the regular shots of the EPC, it will never do splash damage.
		return false;
	}

	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean weakpoint) {
		double damagePerProjectile;
		if (weakpoint && !statusEffects[1]) {
			// Because this weapon doesn't have its Accuracy handled like the other weapons, I'm choosing to just increase the damage by a weighted average.
			damagePerProjectile = increaseBulletDamageForWeakpoints(getDirectDamage());
		}
		else {
			damagePerProjectile = getDirectDamage();
		}
		
		// Frozen
		if (statusEffects[1]) {
			damagePerProjectile *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			damagePerProjectile *= UtilityInformation.IFG_Damage_Multiplier;
		}
		
		int burstSize = getNumRegularShotsBeforeOverheat();
		
		double duration;
		if (burst) {
			duration = burstSize / rateOfFire;
		}
		else {
			duration = burstSize / rateOfFire + getCooldownDuration();
		}
		
		double burnDPS = 0;
		if (selectedTier5 == 2 && !statusEffects[1]) {
			if (burst) {
				// 50% of Direct Damage from the Regular Shots gets added on as Heat Damage.
				double heatDamagePerShot = 0.5 * getDirectDamage();
				double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
				double fireDoTUptimeCoefficient = (duration - timeToIgnite) / duration;
				
				burnDPS = fireDoTUptimeCoefficient * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		return damagePerProjectile * burstSize / duration + burnDPS;
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
		// EPC has no recoil and no spread per shot, so it can effectively be considered 100% accurate
		return calculateSingleTargetDPS(false, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		// Regular shots can only hit one enemy before disappearing.
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double baseDamage = getDirectDamage() * getBatterySize();
		
		double fireDoTTotalDamage = 0;
		if (selectedTier5 == 2) {
			
			double estimatedNumEnemiesKilled = calculateFiringDuration() / averageTimeToKill();
			double heatDamagePerShot = 0.5 * getDirectDamage();
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
			double fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			fireDoTTotalDamage = fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return baseDamage + fireDoTTotalDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Regular shots can only hit one enemy before disappearing.
		return 1;
	}

	@Override
	public double calculateFiringDuration() {
		int burstSize = getNumRegularShotsBeforeOverheat();
		double timeToFireBurst = burstSize / rateOfFire;
		// Choosing not to use Weapon.numMagazines since the "burst" size isn't adding to total ammo count like normal bullets in a mag do.
		double numBursts = (double) getBatterySize() / (double) burstSize;
		return numBursts * timeToFireBurst + numReloads(getBatterySize(), burstSize) * getCooldownDuration();
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// TODO: should this be increased by Weakpoint bonus?
		double dmgPerShot = getDirectDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			0.5 * getDirectDamage(),  // Kinetic
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0.5 * getDirectDamage()  // Electric
		};
		
		double[] areaDamage = {
			0,  // Explosive
			0,  // Fire
			0,  // Frost
			0  // Electric
		};
		
		double burnDmg = 0;
		if (selectedTier5 == 2) {
			burnDmg = calculateAverageDoTDamagePerEnemy(EnemyInformation.averageTimeToIgnite(0.5 * getDirectDamage(), rateOfFire), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		}
		double[] DoTDamage = {
			burnDmg,  // Fire
			0,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, 0.0, 0.0, 0.0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// EPC doesn't have any utility
		// EPC regular shots also cannot break Light Armor plates
		return 0;
	}
	
	@Override
	public double damagePerMagazine() {
		double baseDamage = getNumRegularShotsBeforeOverheat() * getDirectDamage();
		double fireDoTDamage = 0;
		if (selectedTier5 == 2) {
			double heatDamagePerShot = 0.5 * getDirectDamage();
			double timeToIgnite = EnemyInformation.averageTimeToIgnite(heatDamagePerShot, rateOfFire);
			fireDoTDamage = calculateAverageDoTDamagePerEnemy(timeToIgnite, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
		}
		return baseDamage + fireDoTDamage;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getNumRegularShotsBeforeOverheat() / rateOfFire;
	}
}
