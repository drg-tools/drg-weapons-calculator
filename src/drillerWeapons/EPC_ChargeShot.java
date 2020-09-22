package drillerWeapons;

import guiPieces.ButtonIcons.modIcons;
import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class EPC_ChargeShot extends EPC {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public EPC_ChargeShot() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public EPC_ChargeShot(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public EPC_ChargeShot(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		super(mod1, mod2, mod3, mod4, mod5, overclock);
		fullName = "EPC (Charged Shots)";
	}
	
	@Override
	public EPC_ChargeShot clone() {
		return new EPC_ChargeShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getSimpleName() {
		return "EPC_ChargeShot";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	@Override
	public StatsRow[] getStats() {
		boolean coolingRateModified = selectedTier3 == 2 || selectedOverclock == 1 || selectedOverclock == 4;
		
		StatsRow[] toReturn = new StatsRow[11];
		
		boolean chargedDirectDamageModified = selectedTier1 == 2 || selectedTier2 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[0] = new StatsRow("Direct Damage:", getChargedDirectDamage(), modIcons.directDamage, chargedDirectDamageModified);
		
		boolean chargedAreaDamageModified = selectedTier1 == 2 || selectedTier2 == 2 || selectedTier5 == 0 || selectedTier5 == 1 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Area Damage:", getChargedAreaDamage(), modIcons.areaDamage, chargedAreaDamageModified);
		
		boolean radiusModified = selectedTier2 == 0 || selectedTier5 == 0 || selectedOverclock == 4;
		toReturn[2] = new StatsRow("AoE Radius:", aoeEfficiency[0], modIcons.aoeRadius, radiusModified);
		
		boolean windupModified = selectedTier3 == 1 || selectedTier5 == 0 || selectedOverclock == 0 || selectedOverclock == 2;
		toReturn[3] = new StatsRow("Charged Shot Windup:", getChargedShotWindup(), modIcons.chargeSpeed, windupModified);
		
		toReturn[4] = new StatsRow("Heat/Sec While Charged:", getHeatPerSecondWhileCharged(), modIcons.blank, selectedTier4 == 0 || selectedOverclock == 1);
		
		toReturn[5] = new StatsRow("Seconds Charged Shot can be Held Before Overheating:", getSecondsBeforeOverheatWhileCharged(), modIcons.hourglass, selectedTier4 == 0 || selectedOverclock == 1);
		
		boolean ammoPerShotModified = selectedTier3 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedTier5 == 1;
		toReturn[6] = new StatsRow("Ammo/Charged Shot:", getAmmoPerChargedShot(), modIcons.fuel, ammoPerShotModified);
		
		boolean batterySizeModified = selectedTier1 == 1 || selectedTier4 == 1 || selectedOverclock == 0 || selectedOverclock == 3;
		toReturn[7] = new StatsRow("Battery Size:", getBatterySize(), modIcons.carriedAmmo, batterySizeModified);
		
		// This is equivalent to "Did either the time to charge a shot or the time to cool down after a shot change?"
		//boolean RoFModified = selectedTier3 == 1 || selectedTier3 == 2 || selectedTier5 == 0 || (selectedOverclock > -1 && selectedOverclock < 3) || selectedOverclock == 4;
		boolean RoFModified = windupModified || selectedTier5 == 1 || coolingRateModified;
		toReturn[8] = new StatsRow("Rate of Fire:", getRateOfFire(), modIcons.rateOfFire, RoFModified);
		
		toReturn[9] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRateModifier()), modIcons.coolingRate, coolingRateModified);
		
		toReturn[10] = new StatsRow("Cooldown After Overheating:", getCooldownDuration(), modIcons.hourglass, coolingRateModified || selectedTier5 == 1);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		// Because this only models the charged shots of the EPC, it will always do splash damage.
		return true;
	}
	
	@Override
	protected void setAoEEfficiency() {
		// Special case: Thin Containment Field
		if (selectedTier5 == 1) {
			aoeEfficiency = calculateAverageAreaDamage(3.0, 3.0, 1.0);
		}
		else {
			// According to Elythnwaen, EPC has a 1.25m full damage radius, and 33% damage falloff at full radius
			aoeEfficiency = calculateAverageAreaDamage(getChargedAoERadius(), 1.25, 0.33);
		}
	}

	// Single-target calculations
	private double calculateSingleTargetDPS() {
		/*
			Much like the Grenade Launcher, the DPS of this gun is modeled by the damage done per projectile divided by the time to fire another projectile.
			Because this is modeling the most efficient DPS, it will model releasing the charged shot as soon as it's available, instead of holding it until it automatically overheats.
			This means that the mods and OCs that affect heat gain while charged won't affect DPS.
			
			Additionally, the burst dps == sustained dps == sustained weakpoint dps == sustained weakpoint + accuracy dps because the charged shots' direct damage don't deal weakpoint damage, 
			the accuracy is ignored because it's manually aimed, and the magSize is effectively 1 due to the overheat mechanic.
		*/
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			return getChargedDirectDamage() * getRateOfFire();
		}
		
		double baseDPS = (getChargedDirectDamage() + getChargedAreaDamage()) * getRateOfFire();
		
		if (selectedOverclock == 5) {
			return baseDPS + DoTInformation.Plasma_DPS;
		}
		else {
			return baseDPS;
		}
	}
	
	@Override
	public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
		return calculateSingleTargetDPS();
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			return getChargedDirectDamage() * getRateOfFire();
		}
		
		if (selectedOverclock == 5) {
			return getChargedAreaDamage() * aoeEfficiency[1] * getRateOfFire() + DoTInformation.Plasma_DPS;
		}
		else {
			return getChargedAreaDamage() * aoeEfficiency[1] * getRateOfFire();
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numberOfChargedShots = (int) Math.ceil(getBatterySize() / getAmmoPerChargedShot());
		
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double directDamage = getChargedDirectDamage();
			double numTargetsHitPerShot = calculateMaxNumTargets();
			return numberOfChargedShots * directDamage * numTargetsHitPerShot;
		}
		
		double baseDamage = numberOfChargedShots * (getChargedDirectDamage() + getChargedAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2]);
		if (selectedOverclock == 5) {
			/*
				Since Persistent Plasma is a DoT that last 6 seconds, but doesn't guarantee to hit every target for that full 6 seconds, 
				I'm choosing to have its total damage be equal to how the DoT DPS times firing duration times the max num targets divided by 3.
				The divide by 3 is to simulate the fact that the enemies are not stationary within the DoT field, and will move out of it before 
				the duration expires.
			*/
			double persistentPlasmaDamage = DoTInformation.Plasma_DPS * calculateFiringDuration() * aoeEfficiency[2] / 3.0;
			return baseDamage + persistentPlasmaDamage;
		}
		else {
			return baseDamage;
		}
	}

	@Override
	public int calculateMaxNumTargets() {
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double numTargetsHitSimultaneously = aoeEfficiency[2];
			// This is an arbitrary number to multiply how many targets the Flying Nightmare projectile will hit along its path. In all liklihood this is probably incorrect, but Flying Nightmare is horrendous to try to model.
			double distanceTraveledTargetMultiplier = 3;
			return (int) Math.round(numTargetsHitSimultaneously * distanceTraveledTargetMultiplier);
		}
		else {
			return (int) aoeEfficiency[2];
		}
	}

	@Override
	public double calculateFiringDuration() {
		double firingInterval = getChargedShotWindup() + getCooldownDuration();
		int numChargedShots = (int) Math.ceil(getBatterySize() / getAmmoPerChargedShot());
		return numChargedShots * firingInterval;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		double dmgPerShot = getChargedDirectDamage() + getChargedAreaDamage();
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}
	
	@Override
	public int breakpoints() {
		double[] directDamage = {
			0,  // Kinetic
			0,  // Explosive
			0.5 * getChargedDirectDamage(),  // Fire
			0,  // Frost
			0.5 * getChargedDirectDamage()  // Electric
		};
		
		double[] areaDamage = {
			0,  // Kinetic
			0.5 * getChargedAreaDamage(),  // Explosive
			0,  // Fire
			0,  // Frost
			0.5 * getChargedAreaDamage(),  // Electric
		};
		
		double persistentPlasmaDamage = 0;
		if (selectedOverclock == 5) {
			persistentPlasmaDamage = calculateAverageDoTDamagePerEnemy(0, 7.6, DoTInformation.Plasma_DPS);
		}
		double[] DoTDamage = {
			0,  // Fire
			persistentPlasmaDamage,  // Electric
			0,  // Poison
			0  // Radiation
		};
		
		breakpoints = EnemyInformation.calculateBreakpoints(directDamage, areaDamage, DoTDamage, -1.0, 0.0, 0.0, statusEffects[1], statusEffects[3], selectedTier5 == 0);
		return MathUtils.sum(breakpoints);
	}

	@Override
	public double utilityScore() {
		// Light Armor Breaking probability
		// EPC charged shot's AoE damage is 50% Explosive, so it does have a chance to break Light Armor plates
		// Additionally, to average out this probability to break all Light Armor plates inside the AoE, multiply it by its AoE Efficiency coefficient, too.
		utilityScores[2] = calculateProbabilityToBreakLightArmor(aoeEfficiency[1] * 0.5 * getChargedAreaDamage()) * UtilityInformation.ArmorBreak_Utility;
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		// Instead of damage per mag, this will be damage per Charged Shot
		if (selectedTier5 == 0) {
			// Special case: Flying Nightmare does the Charged Direct Damage to any enemies it passes through, but it no longer explodes for its Area Damage upon impact. As a result, it also cannot proc Persistent Plasma
			double directDamage = getChargedDirectDamage();
			double numTargetsHitPerShot = calculateMaxNumTargets();
			return directDamage * numTargetsHitPerShot;
		}
		else {
			return getChargedDirectDamage() + getChargedAreaDamage() * aoeEfficiency[1] * aoeEfficiency[2];
		}
	}
	
	@Override
	public double damageWastedByArmor() {
		// The charged shots of the EPC need to have more research done before I model them like the other bullet-based weapons.
		return 0;
	}
	
	@Override
	public double timeToFireMagazine() {
		return getChargedShotWindup();
	}
}
