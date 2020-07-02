package engineerWeapons;

import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class BreachCutter_Projectile extends BreachCutter {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public BreachCutter_Projectile() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public BreachCutter_Projectile(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public BreachCutter_Projectile(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "Breach Cutter Projectile";
	}
	
	@Override
	public BreachCutter_Projectile clone() {
		return new BreachCutter_Projectile(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "BC_Projectile";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[10];
		
		toReturn[0] = new StatsRow("Burst Damage on First Impact:", getImpactDamage(), selectedOverclock == 5);
		
		boolean dmgPerTickModified = selectedTier2 == 1 || selectedOverclock == 2 || selectedOverclock == 5 || selectedOverclock == 6;
		toReturn[1] = new StatsRow("Damage per Tick:", getDamagePerTick(), dmgPerTickModified);
		
		toReturn[2] = new StatsRow("Damage Ticks per Second:", damageTickRate, false);
		
		toReturn[3] = new StatsRow("Projectile Width:", getProjectileWidth(), selectedTier2 == 2 || selectedTier3 == 1);
		
		toReturn[4] = new StatsRow("Projectile Velocity (m/sec):", getProjectileVelocity(), selectedOverclock == 5);
		
		toReturn[5] = new StatsRow("Delay Before Opening:", getDelayBeforeOpening(), selectedTier3 == 0);
		
		boolean lifetimeModified = selectedTier1 == 0 || selectedOverclock == 2 || selectedOverclock == 5;
		toReturn[6] = new StatsRow("Projectile Lifetime (sec):", getProjectileLifetime(), lifetimeModified);
		
		toReturn[7] = new StatsRow("In-Game Listed DPS:", getDamagePerTick() * damageTickRate, dmgPerTickModified);
		
		toReturn[8] = new StatsRow("Avg Damage per Projectile to Single Grunt:", calculateAverageDamagePerGrunt(true, true, false, true), false);
		
		double intersectionTime;
		if (selectedOverclock == 5) {
			intersectionTime = calculateAverageGruntIntersectionTimePerSpinningDeathProjectile();
		}
		else {
			intersectionTime = calculateGruntIntersectionTimePerRegularProjectile();
		}
		toReturn[9] = new StatsRow("Estimated Seconds of Intersection per Grunt:", intersectionTime, false);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		// Breach Cutter sometimes deals Splash damage for Explosive Goodbye
		// TODO: in the current model, this splash damage doesn't get used. I'm unsure if I want to keep this.
		return selectedTier5 == 0;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// According to Elythnwaen, Explosive Goodbye does 40 Explosive Damage in a 3m radius, 2m Full Damage radius. 
		// No listed falloff percentage, so I'm just going to use the default 0.25
		// TODO: in the current model, this AoE Efficiency isn't used. I'm unsure if I want to keep this.
		aoeEfficiency = calculateAverageAreaDamage(3, 2, 0.25);
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean primaryTarget, boolean weakpoint) {
		double intersectionTime;
		if (selectedOverclock == 5) {
			intersectionTime = calculateAverageGruntIntersectionTimePerSpinningDeathProjectile();
		}
		else {
			intersectionTime = calculateGruntIntersectionTimePerRegularProjectile();
		}
		
		double damagePerProjectileToSingleGrunt = calculateAverageDamagePerGrunt(false, primaryTarget, weakpoint, false);
		
		return damagePerProjectileToSingleGrunt / intersectionTime;
	}
	
	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(true, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(true, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(true, true);
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return calculateSingleTargetDPS( false, false);
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return calculateMaxNumTargets() * calculateAverageDamagePerGrunt(true, true, false, true);
	}

	@Override
	public double calculateFiringDuration() {
		return getDelayBeforeOpening() + getProjectileLifetime();
	}
}
