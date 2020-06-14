package engineerWeapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.ButtonIcons.modIcons;
import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;

public class BreachCutter extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	// Shortcut constructor to get baseline data
	public BreachCutter() {
		this(-1, -1, -1, -1, -1, -1);
	}
	
	// Shortcut constructor to quickly get statistics about a specific build
	public BreachCutter(String combination) {
		this(-1, -1, -1, -1, -1, -1);
		buildFromCombination(combination);
	}
	
	public BreachCutter(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
		fullName = "Breach Cutter";
		weaponPic = WeaponPictures.breachCutter;
		
		// Base stats, before mods or overclocks alter them:
		
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
		tier1[0] = new Mod("Prolonged Power Generation", "+1.5 Projectile Lifetime", modIcons.duration, 1, 0);
		tier1[1] = new Mod("High Capacity Magazine", "+2 Clip Size", modIcons.magSize, 1, 1);
		
		tier2 = new Mod[3];
		tier2[0] = new Mod("Expanded Ammo Bags", "+8 Max Ammo", modIcons.carriedAmmo, 2, 0);
		tier2[1] = new Mod("Condensed Plasma", "+175 Beam DPS", modIcons.directDamage, 2, 1);
		tier2[2] = new Mod("Loosened Node Cohesion", "+1m Plasma Beam Width", modIcons.aoeRadius, 2, 2);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Quick Deploy", "-0.2 Plasma Expansion Delay", modIcons.duration, 3, 0);
		tier3[1] = new Mod("Loosened Node Cohesion", "+1m Plasma Beam Width", modIcons.aoeRadius, 3, 1);
		
		tier4 = new Mod[2];
		tier4[0] = new Mod("Armor Breaking", "+200% Armor Breaking", modIcons.armorBreaking, 4, 0);
		tier4[1] = new Mod("Disruptive Frequency Tuning", "+100% Stun Chance, 3 sec Stun duration", modIcons.stun, 4, 1);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Explosive Goodbye", "40 Damage in a small AoE around the line when it expires or another one gets fired, and leaves behind Persistent Plasma", modIcons.addedExplosion, 5, 0);
		tier5[1] = new Mod("Plasma Trail", "Leaves behind a Persistent Plasma field for 4.6 seconds along the entire length of the line's lifetime", modIcons.areaDamage, 5, 1);
		tier5[2] = new Mod("Triple Split Line", "Adds a line above and below the primary projectile (multiple lines hitting doesn't increase DPS)", modIcons.aoeRadius, 5, 2);
		
		overclocks = new Overclock[7];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Light-Weight Cases", "+4 Max Ammo, -0.2 Reload Time", overclockIcons.carriedAmmo, 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Roll Control", "Holding down the trigger after the line leaves the gun activates a remote connection with on the release of the trigger causes the line to stop rolling.", overclockIcons.directDamage, 1);
		overclocks[2] = new Overclock(Overclock.classification.clean, "Stronger Plasma Current", "+50 Beam DPS, +0.5 Projectile Lifetime", overclockIcons.directDamage, 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Return to Sender", "Holding down the trigger after line leaves the gun activates a remote connection with on the release of the trigger causes "
				+ "the line to change direction and move back towards the gun. Additionally, -4 Max Ammo", overclockIcons.directDamage, 3);
		overclocks[4] = new Overclock(Overclock.classification.balanced, "High Voltage Crossover", "100% chance to electrocute enemies, which deals 16 DPS for 4 seconds. In exchange, -2 Magazine Size.", overclockIcons.electricity, 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Spinning Death", "Spinning Death, x2.5 Projectile Lifetime, x0.2 Beam DPS, x0.5 Max Ammo x0.25 Magazine Size", overclockIcons.directDamage, 5);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Inferno", "Ignites most enemies immediately in exchange for -175 Beam DPS, -4 Max Ammo, and x0.25 Armor Breaking", overclockIcons.directDamage, 5);
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
				System.out.println("Breach Cutter's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Breach Cutter's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[3] == 'C') {
				System.out.println("Breach Cutter's fourth tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-7 or a hyphen");
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
				case 'C': {
					selectedTier2 = 2;
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
	public BreachCutter clone() {
		return new BreachCutter(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Engineer";
	}
	public String getSimpleName() {
		return "BreachCutter";
	}
	public int getDwarfClassID() {
		return DatabaseConstants.engineerCharacterID;
	}
	public int getWeaponID() {
		return DatabaseConstants.breachCutterGunsID;
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[1];
		
		toReturn[0] = new StatsRow("DPS:", 0, false);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}

	@Override
	public double calculateIdealBurstDPS() {
		return 0;
	}

	@Override
	public double calculateIdealSustainedDPS() {
		return 0;
	}
	
	@Override
	public double sustainedWeakpointDPS() {
		return 0;
	}

	@Override
	public double sustainedWeakpointAccuracyDPS() {
		return 0;
	}

	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		return 0;
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		return 0;
	}

	@Override
	public int calculateMaxNumTargets() {
		return 0;
	}

	@Override
	public double calculateFiringDuration() {
		return 0;
	}
	
	@Override
	protected double averageDamageToKillEnemy() {
		return 0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		return 0;
	}
	
	@Override
	public int breakpoints() {
		return 0;
	}

	@Override
	public double utilityScore() {
		return 0;
	}
	
	@Override
	public double damagePerMagazine() {
		return 0;
	}
	
	@Override
	public double timeToFireMagazine() {
		return 0;
	}
	
	@Override
	public ArrayList<String> exportModsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.modsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Tier 1
		toReturn.add(String.format(rowFormat, 1, tier1[0].getLetterRepresentation(), tier1[0].getName(), 1000, 0, 0, 20, 0, 0, 0, tier1[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 1, tier1[1].getLetterRepresentation(), tier1[1].getName(), 1000, 0, 20, 0, 0, 0, 0, tier1[1].getText(), ""));
		
		// Tier 2
		toReturn.add(String.format(rowFormat, 2, tier2[0].getLetterRepresentation(), tier2[0].getName(), 1800, 0, 18, 12, 0, 0, 0, tier2[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[1].getLetterRepresentation(), tier2[1].getName(), 1800, 0, 0, 18, 0, 12, 0, tier2[1].getText(), ""));
		toReturn.add(String.format(rowFormat, 2, tier2[2].getLetterRepresentation(), tier2[2].getName(), 1800, 12, 0, 0, 18, 0, 0, tier2[2].getText(), ""));
		
		// Tier 3
		toReturn.add(String.format(rowFormat, 3, tier3[0].getLetterRepresentation(), tier3[0].getName(), 2200, 0, 0, 20, 0, 30, 0, tier3[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 3, tier3[1].getLetterRepresentation(), tier3[1].getName(), 2200, 20, 30, 0, 0, 0, 0, tier3[1].getText(), ""));
		
		// Tier 4
		toReturn.add(String.format(rowFormat, 4, tier4[0].getLetterRepresentation(), tier4[0].getName(), 3800, 0, 25, 15, 36, 0, 0, tier4[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 4, tier4[1].getLetterRepresentation(), tier4[1].getName(), 3800, 25, 0, 15, 0, 36, 0, tier4[1].getText(), ""));
		
		// Tier 5
		toReturn.add(String.format(rowFormat, 5, tier5[0].getLetterRepresentation(), tier5[0].getName(), 4400, 60, 0, 0, 40, 0, 110, tier5[0].getText(), ""));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 110, 40, 0, 60, 0, 0, tier5[1].getText(), ""));
		toReturn.add(String.format(rowFormat, 5, tier5[1].getLetterRepresentation(), tier5[1].getName(), 4400, 0, 0, 40, 0, 110, 60, tier5[1].getText(), ""));
		
		return toReturn;
	}
	@Override
	public ArrayList<String> exportOCsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		String rowFormat = String.format("INSERT INTO `%s` VALUES (NULL, %d, %d, ", DatabaseConstants.OCsTableName, getDwarfClassID(), getWeaponID());
		rowFormat += "'%s', %s, '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', " + DatabaseConstants.patchNumberID + ");";
		
		// Credits, Magnite, Bismor, Umanite, Croppa, Enor Pearl, Jadiz
		// Clean
		toReturn.add(String.format(rowFormat, "Clean", overclocks[0].getShortcutRepresentation(), overclocks[0].getName(), 8700, 0, 130, 0, 100, 0, 80, overclocks[0].getText(), ""));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8150, 80, 0, 135, 95, 0, 0, overclocks[1].getText(), ""));
		toReturn.add(String.format(rowFormat, "Clean", overclocks[1].getShortcutRepresentation(), overclocks[1].getName(), 8650, 75, 0, 0, 100, 0, 140, overclocks[1].getText(), ""));
		
		// Balanced
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7950, 0, 140, 80, 0, 100, 0, overclocks[2].getText(), ""));
		toReturn.add(String.format(rowFormat, "Balanced", overclocks[2].getShortcutRepresentation(), overclocks[2].getName(), 7300, 0, 75, 120, 95, 0, 0, overclocks[2].getText(), ""));
		
		// Unstable
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[3].getShortcutRepresentation(), overclocks[3].getName(), 8250, 100, 120, 0, 0, 80, 0, overclocks[3].getText(), ""));
		toReturn.add(String.format(rowFormat, "Unstable", overclocks[4].getShortcutRepresentation(), overclocks[4].getName(), 7550, 135, 0, 0, 70, 0, 90, overclocks[4].getText(), ""));
		
		return toReturn;
	}
}
