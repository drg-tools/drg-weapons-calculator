package gunnerWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Minigun extends Weapon {

	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private int damagePerPellet;
	private double stunChancePerPellet;
	private int stunDuration;
	private int maxAmmo;
	private double maxHeat;
	private double heatPerPellet;
	private double coolingRate;
	private int rateOfFire;
	private double spinupTime;
	private int spindownTime;
	private double moveSpeedWhileFiring;
	private double baseSpread;
	private double armorBreakChance;
	private int bulletsFiredTilMaxStability;
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
		
		// Base stats, before mods or overclocks alter them:
		damagePerPellet = 10;
		stunChancePerPellet = 0.3;  // 30%
		stunDuration = 1;  // second
		maxAmmo = 2400; // equal to 1200 pellets
		maxHeat = 9.0;
		heatPerPellet = maxHeat*2.0/258.0;  // max heat divided by 258 ammo times 2 to count pellets. This may need to change to 256? The testing was a little ambiguous...
		coolingRate = 1.5; // heat dissipated per second; translates to 6 seconds of cooling off from max heat (without overheating)
		rateOfFire = 30;  // equal to 15 pellets/sec
		spinupTime = 0.7;  // seconds before minigun starts firing
		spindownTime = 3;  // seconds for the stability to decay from fully stabilized to no stability at all
		moveSpeedWhileFiring = 0.5;
		baseSpread = 1.0;  // effectively its accuracy
		armorBreakChance = 1.0; // it is just as effective at breaking armor per pellet as any other gun
		bulletsFiredTilMaxStability = 50;  // equals 25 pellets
		cooldownAfterOverheat = 10;  // seconds
		
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
		tier1[0] = new Mod("Magnetic Refrigeration", "Increases the cooling Rate", 1, 0);
		tier1[1] = new Mod("Improved Motor", "Increased rate of fire and faster gyro stabilization", 1, 1);
		tier1[2] = new Mod("Improved Platform Stability", "Increased Accuracy", 1, 2);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Oversized Drum", "Expanded Ammo Bags", 2, 0);
		tier2[1] = new Mod("High Velocity Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 2, 1);
		
		tier3 = new Mod[3];
		tier3[0] = new Mod("Hardened Rounds", "Improved armor breaking", 3, 0);
		tier3[1] = new Mod("Stun Duration", "Stunned enemies are incapacitated for a longer period of time.", 3, 1);
		tier3[2] = new Mod("Blowthrough Rounds", "Shaped bullets capable of passing through a target!", 3, 2);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Variable Chamber Pressure", "Damage increase when fully stabilized", 4, 0);
		tier4[1] = new Mod("Lighter Barrel Assembly", "Start killing things sooner with a shorter spinup time.", 4, 1);
		tier4[2] = new Mod("Magnetic Bearings", "Barrels keep spinning for a longer time after firing, keeping the gun stable for longer.", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Aggressive Venting", "Burn everything in a radius when the minigun overheats", 5, 0, false);
		tier5[1] = new Mod("Cold As The Grave", "Every kill cools the gun", 5, 1, false);
		tier5[2] = new Mod("Hot Bullets", "Rounds fired when the heat meter is red will burn the target", 5, 2, false);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "A Little More Oomph!", "Get the most out of each shot without compromising any of the gun's systems.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Thinned Drum Walls", "Stuff more bullets into the ammo drum by thinning the material in non-critical areas.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Burning Hell", "Turn the area just infront of the minigun into an even worse place by venting all the combustion gasses forward. However, it does overheat rather quickly.", 2, false);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Compact Feed Mechanism", "More space left for ammo at the cost of a reduced rate of fire.", 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "Exhaust Vectoring", "Increases damage at a cost to accuracy.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Bullet Hell", "Special bullets that ricochet off all surfaces and even enemies going on to hit nearby targets. However they deal less damage and are less accurate overall.", 5);
		overclocks[6] = new Overclock(Overclock.classification.unstable, "Lead Storm", "Pushing things to the limit this overclock greatly increases damage output but the kickback makes it almost impossible to move.", 6);
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
				case 'C': {
					selectedTier1 = 2;
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
				case 'C': {
					selectedTier3 = 2;
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
				case '7': {
					selectedOverclock = 6;
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
			toReturn += 5;
		}
		return toReturn;
	}
	private int getStunDuration() {
		int toReturn = stunDuration;
		if (selectedTier3 == 1) {
			toReturn += 1;
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
	private double getHeatPerPellet() {
		double toReturn = heatPerPellet;
		if (selectedOverclock == 2) {
			toReturn *= 2.5;
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
		// This value is really annoying; the imprecise nature of double values make 0.7 - 0.4 != 0.3
		// Round it to one decimal point to present the expected value.
		return MathUtils.round(toReturn, 1);
	}
	private int getSpindownTime() {
		int toReturn = spindownTime;
		if (selectedTier4 == 2) {
			toReturn += 3;
		}
		return toReturn;
	}
	private double getMovespeedWhileFiring() {
		double toReturn = moveSpeedWhileFiring;
		if (selectedOverclock == 6) {
			toReturn *= 0;
		}
		return toReturn;
	}
	private double getBaseSpread() {
		double toReturn = baseSpread;
		if (selectedTier1 == 2) {
			toReturn -= 0.8;
		}
		if (selectedOverclock == 4) {
			toReturn *= 2.5;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 6.0;
		}
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		if (selectedTier3 == 0) {
			toReturn +=  2.0;
		}
		return toReturn;
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
			// I don't know if the penetration bullet will ricochet too. For now, I'll assume that it does.
			if (selectedTier3 == 2) {
				return 2;
			}
			else {
				return 1;
			}
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[18];
		
		boolean damageModified = selectedTier2 == 1 || selectedOverclock == 0 || selectedOverclock > 3;
		toReturn[0] = new StatsRow("Damage per Pellet:", "" + getDamagePerPellet(), damageModified);
		
		toReturn[1] = new StatsRow("Ammo Spend per Pellet:", "2", false);
		
		toReturn[2] = new StatsRow("Stun Chance per Pellet:", convertDoubleToPercentage(stunChancePerPellet), false);
		
		toReturn[3] = new StatsRow("Stun Duration:", "" + getStunDuration(), selectedTier3 == 1);
		
		boolean ammoModified = selectedTier2 == 0 || selectedOverclock == 1 || selectedOverclock == 3;
		toReturn[4] = new StatsRow("Max Ammo:", "" + getMaxAmmo(), ammoModified);
		
		toReturn[5] = new StatsRow("Max Heat:", maxHeat + " *", false);
		
		toReturn[6] = new StatsRow("Heat Accumulated per Pellet:", getHeatPerPellet() + " *", selectedOverclock == 2);
		
		toReturn[7] = new StatsRow("Cooling Rate (Heat Dissipated/Sec):", "" + getCoolingRate(), selectedTier1 == 0 || selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Cooldown After Overheat:", cooldownAfterOverheat + " *", false);
		
		toReturn[9] = new StatsRow("Rate of Fire (Ammo/Sec):", "" + getRateOfFire(), selectedTier1 == 1 || selectedOverclock == 3);
		
		toReturn[10] = new StatsRow("Ammo Spent Until Stabilized:", bulletsFiredTilMaxStability + " *", false);
		
		toReturn[11] = new StatsRow("Spinup Time:", "" + getSpinupTime(), selectedTier4 == 1 || selectedOverclock == 0);
		
		toReturn[12] = new StatsRow("Spindown Time:", "" + getSpindownTime(), selectedTier4 == 2);
		
		boolean baseSpreadModified = selectedTier1 == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[13] = new StatsRow("Base Spread:", convertDoubleToPercentage(getBaseSpread()), baseSpreadModified);
		
		toReturn[14] = new StatsRow("Movement Speed While Using:", convertDoubleToPercentage(getMovespeedWhileFiring()), selectedOverclock == 6);
		
		toReturn[15] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier3 == 0);
		
		toReturn[16] = new StatsRow("Max Penetrations:", "" + getNumberOfPenetrations(), selectedTier3 == 2);
		
		toReturn[17] = new StatsRow("Max Ricochets:", "" + getNumberOfRicochets(), selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}
	
	private double calculateDamagePerBurst(boolean weakpointBonus) {
		/* 
			The length of the burst is determined by the heat accumulated. Each burst duration should stop just shy of 
			overheating the minigun so that it doesn't have the overheat cooldown penalty imposed.
		*/
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double damageMultiplier = 1.0;
		if (selectedTier4 == 0) {
			double pelletsFiredWhileNotStabilized = bulletsFiredTilMaxStability / 2.0;
			damageMultiplier = (pelletsFiredWhileNotStabilized + 1.15*(numPelletsFiredBeforeOverheat - pelletsFiredWhileNotStabilized)) / numPelletsFiredBeforeOverheat;
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
		double damagePerBurst = calculateDamagePerBurst(false);
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double burstDuration = 2.0 * numPelletsFiredBeforeOverheat / ((double) getRateOfFire());
		return damagePerBurst / burstDuration;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		double damagePerBurst = calculateDamagePerBurst(false);
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double burstDuration = 2.0 * numPelletsFiredBeforeOverheat / ((double) getRateOfFire());
		double coolOffDuration = maxHeat / getCoolingRate();
		return damagePerBurst / (burstDuration + coolOffDuration);
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		double damagePerBurst = calculateDamagePerBurst(true);
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double burstDuration = 2.0 * numPelletsFiredBeforeOverheat / ((double) getRateOfFire());
		double coolOffDuration = maxHeat / getCoolingRate();
		return damagePerBurst / (burstDuration + coolOffDuration);
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier3 == 2 || selectedOverclock == 5) {
			// This assumes that the penetrations don't have their damage reduced.
			return calculateIdealSustainedDPS();
		}
		else {
			return 0;
		}
	}
	
	private double calculateMaxSingleTargetDamage() {
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double numberOfBursts = (double) getMaxAmmo() / (2.0 * numPelletsFiredBeforeOverheat);
		return numberOfBursts * calculateDamagePerBurst(false);
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return (double) calculateMaxNumTargets() * calculateMaxSingleTargetDamage();
	}

	@Override
	public int calculateMaxNumTargets() {
		return 1 + getNumberOfPenetrations() + getNumberOfRicochets();
	}

	@Override
	public double calculateFiringDuration() {
		double numPelletsFiredBeforeOverheat = Math.floor(maxHeat / getHeatPerPellet());
		double numberOfBursts = (double) getMaxAmmo() / (2.0 * numPelletsFiredBeforeOverheat);
		double numberOfCooldowns = Math.floor(numberOfBursts) - 1.0;
		return (numberOfBursts * 2.0 * numPelletsFiredBeforeOverheat / getRateOfFire()) + (numberOfCooldowns * maxHeat / getCoolingRate());
	}

	@Override
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDamagePerPellet());
		double overkill = EnemyInformation.averageHealthPool() % dmgPerShot;
		return overkill / dmgPerShot * 100.0;
	}

	@Override
	public double estimatedAccuracy() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double utilityScore() {
		// TODO Auto-generated method stub
		return 0;
	}

}