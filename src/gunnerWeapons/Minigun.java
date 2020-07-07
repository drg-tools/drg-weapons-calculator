package gunnerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.ConditionalArrayList;
import utilities.MathUtils;

/*
	Extracted via UUU:
	Time between Burning Hell ticks: 0.25
*/

public class Minigun extends Weapon {

	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private double stunChancePerPellet;
	private int stunDuration;
	private int maxAmmo;
	private double maxHeat;
	private double heatPerSecond;
	private double coolingRate;
	private int rateOfFire;
	private double spinupTime;
	private int spindownTime;
	private double movespeedWhileFiring;
	private double secondsBeforeHotBullets;
	private int cooldownAfterOverheat;
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public Minigun() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build without having to type a lot of enumerated variables
	public Minigun(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public Minigun(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "\"Lead Storm\" Powered Minigun";
		weaponPic = WeaponPictures.minigun;
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 10;
		stunChancePerPellet = 0.3;
		stunDuration = 1;
		maxAmmo = 2400; // equal to 1200 pellets
		// MikeGSG confirmed that 9.5 is the max Heat, and the default Heat gain rate is 1 Heat/sec, so this translates into 9.5 seconds of firing before Overheat.
		maxHeat = 9.5;
		heatPerSecond = 1.0;
		coolingRate = 1.5;
		rateOfFire = 30;  // equal to 15 pellets/sec
		spinupTime = 0.7;
		spindownTime = 3;  // seconds for the barrels to stop spinning -- does not affect the stability
		movespeedWhileFiring = 0.5;
		secondsBeforeHotBullets = 3.17805;  // See explanation in calculateIgnitionTime() 
		cooldownAfterOverheat = 10;
		
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
		tier1 = new Mod[3];
		tier1[0] = new Mod("Magnetic Refrigeration", "+1.5 Cooling Rate", modIcons.coolingRate, 1, 0);
		tier1[1] = new Mod("Improved Motor", "+4 Rate of Fire", modIcons.rateOfFire, 1, 1);
		tier1[2] = new Mod("Improved Platform Stability", "x0.2 Base Spread", modIcons.baseSpread, 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Oversized Drum", "+600 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("High Velocity Rounds", "+2 Damage per Pellet", modIcons.directDamage, 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Hardened Rounds", "+200% Armor Breaking", modIcons.armorBreaking, 3, 0);
		tier3[1] = new Mod("Stun Duration", "+1 second Stun duration", modIcons.stun, 3, 1);
		tier3[2] = new Mod("Blowthrough Rounds", "+1 Penetration", modIcons.blowthrough, 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Variable Chamber Pressure", "+15% Damage per Pellet after reaching Base Spread", modIcons.directDamage, 4, 0);
		tier4[1] = new Mod("Lighter Barrel Assembly", "-0.4 seconds spinup time", modIcons.chargeSpeed, 4, 1);
		tier4[2] = new Mod("Magnetic Bearings", "+3 seconds spindown time", modIcons.special, 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Aggressive Venting", "After overheating, deal 60 Heat Damage and 100% chance to apply Fear to all enemies within a 10m radius", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Cold As The Grave", "Every kill subtracts 0.8 Heat from the Heat Meter (maxes at 9.5 Heat) and thus increases the firing duration before overheating", modIcons.coolingRate, 5, 1);
		tier5[2] = new Mod("Hot Bullets", "After the Heat Meter turns red, 50% of the Damage per Pellet gets added as Heat Damage", modIcons.heatDamage, 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "A Little More Oomph!", "+1 Damage per Pellet, -0.2 spinup time", overclockIcons.directDamage, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thinned Drum Walls", "+300 Max Ammo, +0.5 Cooling Rate", overclockIcons.coolingRate, 1);
		// Burning Hell info comes straight from MikeGSG -- thanks Mike!
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Burning Hell", "While firing, the Minigun deals 20 Area Damage per second and 80 Heat per Second in a cone 5m in front of the muzzle. +50% heat accumulation in the "
				+ "weapon's heat meter, which translates to 2/3 the firing period", overclockIcons.heatDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Compact Feed Mechanism", "+800 Max Ammo, -4 Rate of Fire", overclockIcons.carriedAmmo, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Exhaust Vectoring", "+2 Damage per Pellet, x2.5 Base Spread", overclockIcons.directDamage, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Bullet Hell", "50% chance for bullets that impact an enemy or terrain to ricochet into another enemy. -3 Damage per Pellet, x6 Base Spread", overclockIcons.ricochet, 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Lead Storm", "+4 Damage per Pellet, x0 Movespeed while using, and the Minigun cannot stun enemies anymore.", overclockIcons.directDamage, 6);
	}
	
	@Override
	public void buildFromCombination(String combination) {
		boolean codeIsValid = true;
		char[] symbols = combination.toCharArray();
		if (combination.length() != 6) {
			System.out.println(combination + " does not have 6 characters, which makes it invalid");
			codeIsValid = false;
		}
		else {
			List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
			for (int i = 0; i < 5; i ++) {
				if (!validModSymbols.contains(symbols[i])) {
					System.out.println("Symbol #" + (i+1) + ", " + symbols[i] + ", is not a capital letter between A-C or a hyphen");
					codeIsValid = false;
				}
			}
			if (symbols[1] == 'C') {
				System.out.println("Minigun's second tier of mods only has two choices, so 'C' is an invalid choice.");
				codeIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
				codeIsValid = false;
			}
		}
		
		if (codeIsValid) {
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
				case 'C': {
					setSelectedModAtTier(1, 2, false);
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
				case 'C': {
					setSelectedModAtTier(4, 2, false);
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
				case 'C': {
					setSelectedModAtTier(5, 2, false);
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
				case '7': {
					setSelectedOverclock(6, false);
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
	public Minigun clone() {
		return new Minigun(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Gunner";
	}
	public String getSimpleName() {
		return "Minigun";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.gunnerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.minigunGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/

	public int getDamagePerPellet() {
		int toReturn = damagePerPellet;
		if (selectedTier2 == 1) {
			toReturn += 2;
		}
		if (selectedOverclock == 0) {
			toReturn += 1;
		}
		else if (selectedOverclock == 4) {
			toReturn += 2;
		}
		else if (selectedOverclock == 5) {
			toReturn -= 3;
		}
		else if (selectedOverclock == 6) {
			toReturn += 4;
		}
		return toReturn;
	}
	private double getStunChancePerPellet() {
		double toReturn = stunChancePerPellet;
		
		if (selectedOverclock == 6) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		if (selectedTier3 == 1) {
			toReturn += 1;
		}
		
		if (selectedOverclock == 6) {
			toReturn *= 0;
		}
		
		return toReturn;
	}
	private int getMaxAmmo() {
		int toReturn = maxAmmo;
		if (selectedTier2 == 0) {
			toReturn += 600;
		}
		if (selectedOverclock == 1) {
			toReturn += 300;
		}
		else if (selectedOverclock == 3) {
			toReturn += 800;
		}
		return toReturn;
	}
	private double getHeatPerSecond() {
		double toReturn = heatPerSecond;
		if (selectedOverclock == 2) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private double getCoolingRate() {
		double toReturn = coolingRate;
		if (selectedTier1 == 0) {
			toReturn += 1.5;
		}
		if (selectedOverclock == 1) {
			toReturn += 0.5;
		}
		return toReturn;
	}
	private int getRateOfFire() {
		int toReturn = rateOfFire;
		if (selectedTier1 == 1) {
			toReturn += 4;
		}
		if (selectedOverclock == 3) {
			toReturn -= 4;
		}
		return toReturn;
	}
	private double getSpinupTime() {
		double toReturn = spinupTime;
		if (selectedTier4 == 1) {
			toReturn -= 0.4;
		}
		if (selectedOverclock == 0) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private int getSpindownTime() {
		int toReturn = spindownTime;
		if (selectedTier4 == 2) {
			toReturn += 3;
		}
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double modifier = movespeedWhileFiring;
		if (selectedOverclock == 6) {
			modifier *= 0;
		}
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private double getBaseSpread() {
		double toReturn = 1.0;
		if (selectedTier1 == 2) {
			toReturn *= 0.2;
		}
		if (selectedOverclock == 4) {
			toReturn *= 2.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 6.0;
		}
		return toReturn;
	}
	private double getArmorBreaking() {
		if (selectedTier3 == 0) {
			return 3.0;
		}
		else {
			return 1.0;
		}
	}
	private int getNumberOfPenetrations() {
		if (selectedTier3 == 2) {
			return 1;
		}
		else {
			return 0;
		}
	}
	private int getNumberOfRicochets() {
		if (selectedOverclock == 5) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	private int numPelletsFiredTilMaxAccuracy() {
		double RoF = getRateOfFire() / 2.0;
		double exactAnswer = RoF * 3.0 / (0.2 * RoF - 1.0);
		return (int) Math.floor(exactAnswer);
	}
	private double variableChamberPressureMultiplier() {
		double numPelletsFiredBeforeOverheat = calculateMaxNumPelletsFiredWithoutOverheating();
		double pelletsFiredWhileNotStabilized = numPelletsFiredTilMaxAccuracy();
		return (pelletsFiredWhileNotStabilized + 1.15*(numPelletsFiredBeforeOverheat - pelletsFiredWhileNotStabilized)) / numPelletsFiredBeforeOverheat;
	}
	private double calculateFiringPeriod() {
		double heatPerSecond = getHeatPerSecond();
		double RoF = getRateOfFire();
		double firingPeriod = maxHeat / heatPerSecond;
		
		// Cold as the Grave removes 0.8 Heat from the Minigun's meter every time that the Minigun gets the killing blow on an enemy.
		if (selectedTier5 == 1) {
			double heatRemovedPerKill = 0.8;
			
			// This is a quick-and-dirty way to guess what the Ideal Burst DPS will be when it's all said and done without calculating Firing Period and causing an infinite loop.
			double estimatedBurstDPS = getDamagePerPellet() * RoF / 2.0;
			if (selectedTier4 == 0) {
				// Slight overestimation
				estimatedBurstDPS *= 1.13;
			}
			if (selectedOverclock == 2) {
				// Slight overestimation
				estimatedBurstDPS += 0.95 * DoTInformation.Burn_DPS;
				
				// To account for the increased Heat Gain Rate from Burning Hell, I'm proportionally scaling down the Heat removed per kill
				heatRemovedPerKill = heatRemovedPerKill / heatPerSecond;
			}
			
			// I'm choosing to model CatG with the incorrect "guessed" Spawn Rates vector because it produced very believable results.
			// Using the "exact" Spawn Rates made this model CatG WAY too strongly.
			double estimatedBurstTTK = EnemyInformation.averageHealthPool(false) / estimatedBurstDPS;
			double timeAddedByCATG = (firingPeriod / estimatedBurstTTK) * heatRemovedPerKill;
			firingPeriod += timeAddedByCATG;
			
			// I'm using this while loop to model how getting a kill enables the Minigun to get more kills without Overheating, but also the diminishing returns. If I modeled it correctly,
			// then this will be less and less effective as the average healthpool goes up with Difficulty Scaling
			double maxFiringPeriod = getMaxAmmo() / RoF;
			while (timeAddedByCATG > heatRemovedPerKill) {
				timeAddedByCATG = (timeAddedByCATG / estimatedBurstTTK) * heatRemovedPerKill;
				firingPeriod += timeAddedByCATG;
				
				// On Haz1 1Player, this is an infinite while loop. As such, I'm adding a second exit condition: it can't return a firing period longer than it takes to fire all ammo.
				if (firingPeriod > maxFiringPeriod) {
					firingPeriod = maxFiringPeriod;
					break;
				}
			}
		}
		
		return firingPeriod;
	}
	private double calculateMaxNumPelletsFiredWithoutOverheating() {
		// Strangely, the RoF does NOT affect the heat/sec gain?
		return Math.floor(calculateFiringPeriod() * getRateOfFire() / 2.0);
	}
	private double calculateCooldownPeriod() {
		// This equation took a while to figure out, and it's still just an approximation. A very close approximation, but an approximation nonetheless.
		return 9.5 / getCoolingRate() + getCoolingRate() / 9;
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[19];
		
		boolean damageModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Direct Damage per Pellet:", getDamagePerPellet(), modIcons.directDamage, damageModified);
		
		toReturn[1] = new StatsRow("Ammo Consumed per Pellet:", 2, modIcons.blank, false);
		
		toReturn[2] = new StatsRow("Ammo Spent Until Stabilized:", numPelletsFiredTilMaxAccuracy() * 2, modIcons.special, selectedTier1 == 1 || selectedOverclock == 3);
		
		toReturn[3] = new StatsRow("Max Duration of Firing Without Overheating:", calculateFiringPeriod(), modIcons.hourglass, selectedTier5 == 1 || selectedOverclock == 2);
		
		boolean pelletsPerBurstModified = selectedTier5 == 1 || selectedOverclock == 2 || selectedTier1 == 1 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Max Num Pellets Fired per Burst:", calculateMaxNumPelletsFiredWithoutOverheating(), modIcons.magSize, pelletsPerBurstModified);
		
		boolean ammoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[5] = new StatsRow("Max Ammo:", getMaxAmmo(), modIcons.carriedAmmo, ammoModified);
		
		toReturn[6] = new StatsRow("Rate of Fire (Ammo/Sec):", getRateOfFire(), modIcons.rateOfFire, selectedTier1 == 1 || selectedOverclock == 3);
		
		toReturn[7] = new StatsRow("Cooling Rate:", getCoolingRate(), modIcons.coolingRate, selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Max Cooldown Without Overheating:", calculateCooldownPeriod(), modIcons.hourglass, selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[9] = new StatsRow("Cooldown After Overheat:", cooldownAfterOverheat, modIcons.duration, false);
		
		toReturn[10] = new StatsRow("Spinup Time:", getSpinupTime(), modIcons.chargeSpeed, selectedTier4 == 1 || selectedOverclock == 0);
		
		toReturn[11] = new StatsRow("Spindown Time:", getSpindownTime(), modIcons.special, selectedTier4 == 2);
		
		toReturn[12] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreaking()), modIcons.armorBreaking, selectedTier3 == 0, selectedTier3 == 0);
		
		toReturn[13] = new StatsRow("Max Penetrations:", getNumberOfPenetrations(), modIcons.blowthrough, selectedTier3 == 2, selectedTier3 == 2);
		
		toReturn[14] = new StatsRow("Max Ricochets:", getNumberOfRicochets(), modIcons.ricochet, selectedOverclock == 5, selectedOverclock == 5);
		
		toReturn[15] = new StatsRow("Stun Chance per Pellet:", convertDoubleToPercentage(getStunChancePerPellet()), modIcons.homebrewPowder, selectedOverclock == 6);
		
		toReturn[16] = new StatsRow("Stun Duration:", getStunDuration(), modIcons.stun, selectedTier3 == 1 || selectedOverclock == 6);
		
		boolean baseSpreadModified = selectedTier1 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[17] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), modIcons.baseSpread, baseSpreadModified, baseSpreadModified);
		
		toReturn[18] = new StatsRow("Movement Speed While Using: (m/sec)", getMovespeedWhileFiring(), modIcons.movespeed, selectedOverclock == 6);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	private double calculateIgnitionTime(boolean accuracy) {
		double burningHellHeatPerSec = 80;
		
		double generalAccuracy;
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false);
		}
		else {
			generalAccuracy = 1.0;
		}
		
		// Special case: the overclock Bullet Hell gives every bullet a 50% chance to ricochet into nearby enemies after impacting terrain or an enemy
		if (selectedOverclock == 5 && accuracy) {
			// Never let it be above 1.0 probability to hit a target.
			generalAccuracy = Math.min(generalAccuracy + 0.5, 1.0);
		}
		
		/*
			MikeGSG shared with me that the Heat Meter displayed on the Minigun is the output of UE4 function that maps the current Heat value [0, 9.5]
			to [0, 100]. It doesn't use a proper equation, but I found a polynomial approximation:
			
				17x - 0.256x^2 - 0.0449x^3
			
			Additionally, according to UUU, Hot Bullets activates when the output value of the Heat Meter is > 50. Using WolframAlpha to solve for what Heat Value: 3.17805 Heat
				GetAll GatlingGun HotShellsTemperatureRequired
				GetAll GatlingHotShellsBonusUpgrade TemperatureRequired
			
			Unless Burning Hell is equipped, that's functionally the time before bullets have Heat Damage added to them.
		*/
		double timeBeforeHotBullets = secondsBeforeHotBullets;
		
		// Hot Bullets only
		if (selectedTier5 == 2 && selectedOverclock != 2) {
			// Hot Bullets adds 50% of of each pellet's Direct Damage as Heat Damage while the Heat Meter on the Minigun is red.
			// I'm choosing to reduce the heatPerPellet by the Accuracy of the gun to imitate when pellets miss the target
			double heatPerPellet = ((double) getDamagePerPellet()) * generalAccuracy / 2.0;
			double RoF = getRateOfFire() / 2.0;
			return timeBeforeHotBullets + EnemyInformation.averageTimeToIgnite(heatPerPellet, RoF);
		}
		// Burning Hell only
		else if (selectedTier5 != 2 && selectedOverclock == 2) {
			// Burning Hell burns everything within 5m in a 20 degree arc in front of you at a rate of 80 heat/sec
			return EnemyInformation.averageTimeToIgnite(burningHellHeatPerSec);
		}
		// Both Hot Bullets AND Burning Hell
		else if (selectedTier5 == 2 && selectedOverclock == 2) {
			// Because Burning Hell reduces the Firing Period from 9.5 sec to 6.33 sec, this means that Hot Bullets gets activated sooner too
			double heatGain = getHeatPerSecond();
			double firingPeriod = maxHeat / heatGain;
			timeBeforeHotBullets /= heatGain;
			double timeAfterHotBullets = firingPeriod - timeBeforeHotBullets;
			
			double heatPerPellet = ((double) getDamagePerPellet()) * generalAccuracy / 2.0;
			double RoF = getRateOfFire() / 2.0;
			double avgHeatPerSec = (timeBeforeHotBullets * burningHellHeatPerSec + timeAfterHotBullets * (heatPerPellet * RoF + burningHellHeatPerSec)) / firingPeriod;
			return EnemyInformation.averageTimeToIgnite(avgHeatPerSec);
		}
		// Neither are equipped.
		else {
			return -1;
		}
	}
	
	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean accuracy, boolean weakpoint) {
		double generalAccuracy, shortDuration, longDuration, directWeakpointDamage;
		
		if (accuracy) {
			generalAccuracy = estimatedAccuracy(false) / 100.0;
		}
		else {
			generalAccuracy = 1.0;
		}
		
		// Special case: the overclock Bullet Hell gives every bullet a 50% chance to ricochet into nearby enemies after impacting terrain or an enemy
		if (selectedOverclock == 5 && accuracy) {
			// Never let it be above 1.0 probability to hit a target.
			generalAccuracy = Math.min(generalAccuracy + 0.5, 1.0);
		}
		
		double burstSize = calculateMaxNumPelletsFiredWithoutOverheating();
		
		if (burst) {
			shortDuration = 2.0 * burstSize / getRateOfFire();
			longDuration = shortDuration;
			// I've considered adding the spinup time to the burst duration, but seeing it in the metrics was very counter-intuitive -- it made the burst DPS not the intuitively expected 150.
			// longDuration = getSpinupTime() + calculateFiringPeriod();
		}
		else {
			double firingPeriod = calculateFiringPeriod();
			double cooldownPeriod = calculateCooldownPeriod();
			
			shortDuration = firingPeriod + cooldownPeriod;
			
			double spindown = getSpindownTime();
			double spinup;
			if (cooldownPeriod < spindown) {
				double fractionOfSpinupNeeded = 1.0 - (cooldownPeriod / spindown);
				spinup = fractionOfSpinupNeeded * getSpinupTime();
			}
			else {
				spinup = getSpinupTime();
			}
			
			longDuration = firingPeriod + cooldownPeriod + spinup;
		}
		
		double directDamage = getDamagePerPellet();
		
		// Frozen
		if (statusEffects[1]) {
			directDamage *= UtilityInformation.Frozen_Damage_Multiplier;
		}
		// IFG Grenade
		if (statusEffects[3]) {
			directDamage *= UtilityInformation.IFG_Damage_Multiplier;
		}
		if (selectedTier4 == 0) {
			directDamage *= variableChamberPressureMultiplier();
		}
		
		double weakpointAccuracy;
		if (weakpoint && !statusEffects[1]) {
			weakpointAccuracy = estimatedAccuracy(true) / 100.0;
			directWeakpointDamage = increaseBulletDamageForWeakpoints2(directDamage);
		}
		else {
			weakpointAccuracy = 0.0;
			directWeakpointDamage = directDamage;
		}
		
		double burnDPS = 0;
		if ((selectedTier5 == 2 || selectedOverclock == 2) && !statusEffects[1]) {
			if (burst) {
				double ignitionTime = calculateIgnitionTime(accuracy);
				double burnDoTUptime = (shortDuration - ignitionTime) / shortDuration;
				burnDPS = burnDoTUptime * DoTInformation.Burn_DPS;
			}
			else {
				burnDPS = DoTInformation.Burn_DPS;
			}
		}
		
		double burningHellAreaDPS = 0;
		if (selectedOverclock == 2) {
			burningHellAreaDPS = 20;
		}
		
		int pelletsThatHitWeakpoint = (int) Math.round(burstSize * weakpointAccuracy);
		int pelletsThatHitTarget = (int) Math.round(burstSize * generalAccuracy) - pelletsThatHitWeakpoint;
		
		return (pelletsThatHitWeakpoint * directWeakpointDamage + pelletsThatHitTarget * directDamage) / longDuration + burningHellAreaDPS + burnDPS;
	}
	
	private double calculateDamagePerBurst(boolean weakpointBonus) {
		/* 
			The length of the burst is determined by the heat accumulated. Each burst duration should stop just shy of 
			overheating the minigun so that it doesn't have the overheat cooldown penalty imposed.
			
			TODO: I'd like to refactor out this method if at all possible
		*/
		double numPelletsFiredBeforeOverheat = calculateMaxNumPelletsFiredWithoutOverheating();
		double damageMultiplier = 1.0;
		if (selectedTier4 == 0) {
			damageMultiplier = variableChamberPressureMultiplier();
		}
		
		if (weakpointBonus) {
			return numPelletsFiredBeforeOverheat * increaseBulletDamageForWeakpoints((double) getDamagePerPellet()) * damageMultiplier;
		}
		else {
			return numPelletsFiredBeforeOverheat * (double) getDamagePerPellet() * damageMultiplier;
		}
	}
	
	@Override
	public double calculateIdealBurstDPS() {
		return calculateSingleTargetDPS(true, false, false);
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return calculateSingleTargetDPS(false, false, false);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return calculateSingleTargetDPS(false, false, true);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return calculateSingleTargetDPS(false, true, true);
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		double idealSustained = calculateIdealSustainedDPS();
		
		if (selectedTier3 == 2) {
			// Blowthrough Rounds are just the same DPS, with Burn DPS already added if Burning Hell or Hot Bullets is already equipped
			return idealSustained;
		}
		else if (selectedOverclock == 2) {
			// Burning Hell does 20 Area Damage per second in addition to lighting things on fire at a rate of 80 Heat/sec
			return 20 + DoTInformation.Burn_DPS;
		}
		else if (selectedOverclock == 5) {
			// Bullet Hell has a 50% chance to ricochet
			return 0.5 * idealSustained;
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		int numTargets = calculateMaxNumTargets();
		double numPelletsFiredBeforeOverheat = calculateMaxNumPelletsFiredWithoutOverheating();
		double numberOfBursts = (double) getMaxAmmo() / (2.0 * numPelletsFiredBeforeOverheat);
		double totalDamage = numberOfBursts * calculateDamagePerBurst(false) * numTargets;
		
		double burningHellAoEDamage = 0;
		
		double fireDoTTotalDamage = 0;
		double heatGainPerSec = getHeatPerSecond();
		double timeBeforeHotBullets = secondsBeforeHotBullets / heatGainPerSec;
		double defaultFiringPeriod = maxHeat / heatGainPerSec;
		double timeAfterHotBullets = defaultFiringPeriod - timeBeforeHotBullets;
		double timeBeforeFireProc, fireDoTDamagePerEnemy, estimatedNumEnemiesKilled;
		// Because of how Hot Bullets' ignition time is calculated, it returns (3.17 + the ignition time). As a result, it would end up subtracting from the total damage.
		if (selectedTier5 == 2 && selectedOverclock != 2) {
			timeBeforeFireProc = calculateIgnitionTime(false) - timeBeforeHotBullets;
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			// Because Hot Bullets only starts igniting enemies after 4 seconds, reduce this damage by the uptime coefficient.
			fireDoTDamagePerEnemy *= (timeAfterHotBullets / defaultFiringPeriod);
			
			estimatedNumEnemiesKilled = numTargets * (calculateFiringDuration() / averageTimeToKill());
			
			fireDoTTotalDamage += fireDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		// Burning Hell, on the other hand, works great with this. Even with Hot Bullets stacked on top of it, it doesn't do negative damage.
		else if (selectedOverclock == 2) {
			timeBeforeFireProc = calculateIgnitionTime(false);
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(timeBeforeFireProc, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			// Arbitrarily using 4 targets hit in the AoE in front of the muzzle, no real math behind it.
			int numTargetsHitByBurningHellAoE = 4;
			fireDoTTotalDamage += numberOfBursts * defaultFiringPeriod * fireDoTDamagePerEnemy * numTargetsHitByBurningHellAoE;
			
			// Additionally, model the 20 Area Damage per second dealt by Burning Hell
			burningHellAoEDamage = numberOfBursts * defaultFiringPeriod * 20 * numTargetsHitByBurningHellAoE;
		}
		
		// According to MikeGSG, AV does 60 Heat Damage in a 6m radius that falls off to 15 Heat Damage at 10m. It also inflicts 10 Fear on all enemies within that 10m radius.
		if (selectedTier5 == 0) {
			// I'm choosing to model Aggressive Venting as Fire DoT max damage without affecting DPS stats, since the 10 sec cooldown penalty would TANK all of those stats.
			// Additionally, I'm choosing to not combine its burst of 60 Heat Damage with the Heat/sec dealt by Hot Bullets or Burning Hell. It gets its own section, all to itself.
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(10, 6, 15.0/60.0);
			double percentageOfEnemiesIgnitedByAV = EnemyInformation.percentageEnemiesIgnitedBySingleBurstOfHeat(60 * aggressiveVentingAoeEfficiency[1]);
			double numGlyphidsHitByHeatBurst = aggressiveVentingAoeEfficiency[2];
			int numTimesAVcanTrigger = (int) Math.floor(numberOfBursts);
			fireDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			
			fireDoTTotalDamage += numTimesAVcanTrigger * (percentageOfEnemiesIgnitedByAV * numGlyphidsHitByHeatBurst) * fireDoTDamagePerEnemy;
		}
		
		return totalDamage + fireDoTTotalDamage + burningHellAoEDamage;
	}

	@Override
	public int calculateMaxNumTargets() {
		// Because a ricochet from Bullet Hell consumes the penetration from Blowthrough Rounds, they don't stack together (unless BT Rounds gets buffed to do more than 1 penetration).
		if (selectedTier3 == 2 || selectedOverclock == 5) {
			return 2;
		}
		else {
			return 1;
		}
	}

	@Override
	public double calculateFiringDuration() {
		int ammoSpentBeforeOverheat = (int) (2.0 * calculateMaxNumPelletsFiredWithoutOverheating());
		int maxAmmo = getMaxAmmo();
		
		double numberOfBursts = ((double) maxAmmo) / ((double) ammoSpentBeforeOverheat);
		
		int numberOfCooldowns = 0;
		if (maxAmmo % ammoSpentBeforeOverheat == 0) {
			numberOfCooldowns = (int) (numberOfBursts - 1.0);
		}
		else {
			numberOfCooldowns = Math.floorDiv(maxAmmo, ammoSpentBeforeOverheat);
		}
		
		return (numberOfBursts * calculateFiringPeriod()) + (numberOfCooldowns * calculateCooldownPeriod());
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		// TODO: Should this and all other weapons increase based on the weighted average, or just straight increase due to having Accuracy modeled now?
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet());
		return Math.ceil(EnemyInformation.averageHealthPool() / dmgPerShot) * dmgPerShot;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// I'm choosing to model Minigun as if it has no recoil. Although it does, its so negligible that it would have no effect.
		double unchangingBaseSpread = 61;
		double changingBaseSpread = 68 * getBaseSpread();
		
		// I measured Spread Variance, and then used the 0.2/1.0/3.0 ratio that MikeGSG provided to reverse-engineer what the Spread per Shot and Spread Recovery Speed are
		double spreadVariance = 334;
		double spreadPerShot = spreadVariance / 15.0;
		double spreadRecoverySpeed = spreadVariance / 3.0;
		double effectiveRoF = getRateOfFire() / 2.0;
		
		// Using some cheeky negative values, I can bend AccuracyEstimator.calculateCircularAccuracy() for this method.
		double cheekyBaseSpread = unchangingBaseSpread + changingBaseSpread + spreadVariance;
		int cheekyMagSize = (int) calculateMaxNumPelletsFiredWithoutOverheating();
		double[] cheekyModifiers = {
			1.0,   // Base Spread
			-1.0,  // Spread per Shot
			-1.0,  // Spread Recovery Speed
			-1.0,  // Spread Variance
			0.0    // Recoil
		};
		
		return AccuracyEstimator.calculateCircularAccuracy(weakpointAccuracy, false, effectiveRoF, cheekyMagSize, 1, 
				cheekyBaseSpread, 0, spreadVariance, spreadPerShot, spreadRecoverySpeed, 0, 0.5, 1.0, cheekyModifiers);
		
		/* 
			I'm keeping this old model here for posterity's sake. TODO: After I re-do a lot of the Accuracy value tests, delete this block comment.
		
		// Because it's being modeled without recoil, and its crosshair gets smaller as it fires, I'm making a quick-and-dirty estimate here instead of using AccuracyEstimator. 
		double spreadVariance = 334;
		double spreadPerShot = 16.7;
		double spreadRecoverySpeed = 95.42857143;
		
		double baseSpread = unchangingBaseSpread + changingBaseSpread;
		double maxSpread = baseSpread + spreadVariance;
		
		// Adapted from AccuracyEstimator
		// Because this is modeled without recoil, there are only two options: one where the crosshair is larger than the target, and one where it's <=.
		double sumOfAllProbabilities = 0.0;
		double targetRadius;
		if (weakpointAccuracy) {
			targetRadius = 0.2;
		}
		else {
			targetRadius = 0.4;
		}
		int numPelletsFired = (int) calculateMaxNumPelletsFiredWithoutOverheating();
		int numPelletsUntilStable = numPelletsFiredTilMaxAccuracy();
		double currentSpreadRadius;
		for (int i = 0; i < numPelletsUntilStable; i++) {
			currentSpreadRadius = AccuracyEstimator.convertSpreadPixelsToMeters(maxSpread - i*spreadPerShot, false);
			
			if (currentSpreadRadius > targetRadius) {
				sumOfAllProbabilities += Math.pow((targetRadius / currentSpreadRadius), 2);
			}
			else {
				sumOfAllProbabilities += 1.0;
			}
		}
		
		// Because only the first 20 shots have an accuracy penalty, the rest can be modeled with simple multiplication
		int numPelletsFiredAfterStable = numPelletsFired - numPelletsUntilStable;
		currentSpreadRadius = AccuracyEstimator.convertSpreadPixelsToMeters(baseSpread, false);
		if (currentSpreadRadius > targetRadius) {
			sumOfAllProbabilities += numPelletsFiredAfterStable * Math.pow((targetRadius / currentSpreadRadius), 2);
		}
		else {
			sumOfAllProbabilities += numPelletsFiredAfterStable;
		}
		
		return sumOfAllProbabilities / numPelletsFired * 100.0;
		*/
	}
	
	@Override
	public int breakpoints() {
		double dmgPerPellet = getDamagePerPellet();
		if (selectedTier4 == 0) {
			dmgPerPellet *= variableChamberPressureMultiplier();
		}
		double[] directDamage = {
			dmgPerPellet,  // Kinetic
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
		
		double burnDmg = 0;
		// Because Hot Bullets takes almost 4 seconds to start working, I'm choosing to not model when Burning Hell and Hot Bullets are combined.
		// I'm also choosing to model Burning Hell's 20 Area Damage per second as another Fire DoT
		if (selectedOverclock == 2) {
			burnDmg = calculateAverageDoTDamagePerEnemy(calculateIgnitionTime(false), DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
			burnDmg += calculateAverageDoTDamagePerEnemy(0, DoTInformation.Burn_SecsDuration, 20);
		}
		else if (selectedTier5 == 2) {
			// To model the fact that this won't start igniting enemies until 4 seconds of firing, I'm choosing to only use 1/4 of the Burn DoT damage
			// This is not in any way an accurate representation, since every enemy except Bulks, Breeders, and Nexuses would die before they started Burning.
			burnDmg = 0.25 * calculateAverageDoTDamagePerEnemy(calculateIgnitionTime(false) - 4, DoTInformation.Burn_SecsDuration, DoTInformation.Burn_DPS);
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
		// OC "Lead Storm" reduces Gunner's movement speed
		utilityScores[0] = (getMovespeedWhileFiring() - MathUtils.round(movespeedWhileFiring * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// Light Armor Breaking probability
		utilityScores[2] = calculateProbabilityToBreakLightArmor(getDamagePerPellet(), getArmorBreaking()) * UtilityInformation.ArmorBreak_Utility;
		
		// Mod Tier 5 "Aggressive Venting" induces Fear in a 10m radius (while also dealing 60 Heat Damage)
		if (selectedTier5 == 0) {
			// This returns 135 Grunts with radus 10, so I'm choosing to reduce the number by the AoE Efficiency (about 76%) which brings the number feared down to 103.
			// That still might be too high?
			double[] aggressiveVentingAoeEfficiency = calculateAverageAreaDamage(10, 6, 0.25);
			int numGlyphidsFeared = (int) Math.round(aggressiveVentingAoeEfficiency[1] * aggressiveVentingAoeEfficiency[2]);
			utilityScores[4] = numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
		}
		else {
			utilityScores[4] = 0;
		}
		
		// Innate stun = 30% chance, 1 sec duration (duration improved by Mod Tier 3 "Stun Duration")
		utilityScores[5] = getStunChancePerPellet() * calculateMaxNumTargets() * getStunDuration() * UtilityInformation.Stun_Utility;
		
		return MathUtils.sum(utilityScores);
	}
	
	@Override
	public double damagePerMagazine() {
		return calculateDamagePerBurst(false);
	}
	
	@Override
	public double timeToFireMagazine() {
		return calculateFiringPeriod();
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL(boolean exportAllMods) {
		ConditionalArrayList<String> toReturn = new ConditionalArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', '%s', '%s', " + DatabaseConstants.patchNumberID + ");\n";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1200, 0, 0, 0, 25, 0, 0, tier1[0].getText(true), "{ \"reload\": { \"name\": \"Cooling Rate\", \"value\": 1.5 } }", "Icon_Upgrade_TemperatureCoolDown", "Cooling"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1200, 0, 0, 0, 0, 25, 0, tier1[1].getText(true), "{ \"rate\": { \"name\": \"Rate of Fire\", \"value\": 4 } }", "Icon_Upgrade_FireRate", "Rate of Fire"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 1, tier1[2].getLetterRepresentation(), tier1[2].getName(), 1200, 0, 25, 0, 0, 0, 0, tier1[2].getText(true), "{ \"ex3\": { \"name\": \"Base Spread\", \"value\": 0.2, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_Accuracy", "Accuracy"),
				exportAllMods || false);
		
		// Tier 2
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 2000, 0, 15, 0, 0, 24, 0, tier2[0].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 600 } }", "Icon_Upgrade_Ammo", "Total Ammo"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 2000, 0, 0, 0, 24, 0, 15, tier2[1].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2 } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		
		// Tier 3
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2800, 0, 0, 0, 35, 0, 50, tier3[0].getText(true), "{ \"ex4\": { \"name\": \"Armor Breaking\", \"value\": 200, \"percent\": true } }", "Icon_Upgrade_ArmorBreaking", "Armor Breaking"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2800, 35, 0, 50, 0, 0, 0, tier3[1].getText(true), "{ \"ex11\": { \"name\": \"Stun Duration\", \"value\": 1 } }", "Icon_Upgrade_Stun", "Stun"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 3, tier3[2].getLetterRepresentation(), tier3[2].getName(), 2800, 50, 0, 0, 0, 35, 0, tier3[2].getText(true), "{ \"ex6\": { \"name\": \"Max Penetrations\", \"value\": 1 } }", "Icon_Upgrade_BulletPenetration", "Blow Through"),
				exportAllMods || false);
		
		// Tier 4
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 4800, 0, 0, 0, 72, 50, 48, tier4[0].getText(true), "{ \"ex7\": { \"name\": \"Max Stabilization Damage Bonus\", \"value\": 15, \"percent\": true } }", "Icon_Upgrade_DamageGeneral", "Damage"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 4800, 72, 48, 50, 0, 0, 0, tier4[1].getText(true), "{ \"ex1\": { \"name\": \"Spinup Time\", \"value\": 0.4, \"subtract\": true } }", "Icon_Upgrade_ChargeUp", "Charge Speed"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 4, tier4[2].getLetterRepresentation(), tier4[2].getName(), 4800, 48, 50, 0, 72, 0, 0, tier4[2].getText(true), "{ \"ex2\": { \"name\": \"Spindown Time\", \"value\": 3 } }", "Icon_Upgrade_Special", "Special"),
				exportAllMods || false);
		
		// Tier 5
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 5600, 0, 140, 0, 64, 70, 0, tier5[0].getText(true), "{ \"ex8\": { \"name\": \"Critical Overheat\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Explosion", "Explosion"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 5600, 64, 0, 0, 0, 70, 140, tier5[1].getText(true), "{ \"ex9\": { \"name\": \"Heat Removed on Kill\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_TemperatureCoolDown", "Cooling"),
				exportAllMods || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, 5, tier5[2].getLetterRepresentation(), tier5[2].getName(), 5600, 70, 0, 64, 0, 0, 140, tier5[2].getText(true), "{ \"ex10\": { \"name\": \"Hot Bullets\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Heat", "Heat"),
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
				String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8700, 95, 120, 75, 0, 0, 0, overclocks[0].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 1 }, "
				+ "\"ex1\": { \"name\": \"Spinup Time\", \"value\": 0.2, \"subtract\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 7650, 0, 0, 0, 75, 125, 95, overclocks[1].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 300 }, "
				+ "\"reload\": { \"name\": \"Cooling Rate\", \"value\": 0.5 } }", "Icon_Upgrade_TemperatureCoolDown"),
				exportAllOCs || false);
		
		// Balanced
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 8700, 140, 0, 65, 110, 0, 0, overclocks[2].getText(true), "{ \"ex13\": { \"name\": \"Burning Hell\", \"value\": 1, \"boolean\": true }, "
				+ "\"ex14\": { \"name\": \"Heat Generation\", \"value\": 50, \"percent\": true } }", "Icon_Upgrade_Heat"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 7450, 130, 70, 0, 95, 0, 0, overclocks[3].getText(true), "{ \"ammo\": { \"name\": \"Max Ammo\", \"value\": 800 }, "
				+ "\"rate\": { \"name\": \"Rate of Fire\", \"value\": 4, \"subtract\": true } }", "Icon_Upgrade_Ammo"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Balanced", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7400, 65, 140, 0, 95, 0, 0, overclocks[4].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 2 },  "
				+ "\"ex3\": { \"name\": \"Base Spread\", \"value\": 2.5, \"percent\": true, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		// Unstable
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[5].getShortcutRepresentation(), overclocks[5].getName(), 7600, 140, 0, 75, 0, 105, 0, overclocks[5].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 3, \"subtract\": true }, "
				+ "\"ex3\": { \"name\": \"Base Spread\", \"value\": 6, \"percent\": true, \"multiply\": true }, \"ex15\": { \"name\": \"Bullet Hell\", \"value\": 1, \"boolean\": true } }", "Icon_Upgrade_Ricoshet"),
				exportAllOCs || false);
		toReturn.conditionalAdd(
				String.format(rowFormat, "Unstable", overclocks[6].getShortcutRepresentation(), overclocks[6].getName(), 8800, 65, 0, 0, 0, 130, 100, overclocks[6].getText(true), "{ \"dmg\": { \"name\": \"Damage\", \"value\": 4 }, "
				+ "\"ex12\": { \"name\": \"Movement Speed While Using\", \"value\": 0, \"percent\": true, \"multiply\": true }, \"ex5\": { \"name\": \"Stun Chance\", \"value\": 0, \"percent\": true, \"multiply\": true }, "
				+ "\"ex11\": { \"name\": \"Stun Duration\", \"value\": 0, \"multiply\": true } }", "Icon_Upgrade_DamageGeneral"),
				exportAllOCs || false);
		
		return toReturn;
	}
}