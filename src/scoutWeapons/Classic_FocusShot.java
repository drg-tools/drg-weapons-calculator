package scoutWeapons;

import java.util.Arrays;
import java.util.List;

import modelPieces.DoTInformation;
import modelPieces.DwarfInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.UtilityInformation;
import modelPieces.Weapon;
import utilities.MathUtils;

public class Classic_FocusShot extends Weapon {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	private double directDamage;
	private double focusedShotMultiplier;
	private double carriedAmmo;
	private int magazineSize;
	// private double rateOfFire;
	private double reloadTime;
	private double delayBeforeFocusing;
	private double focusDuration;
	private double movespeedWhileFocusing;
	private double weakpointBonus;
	private double armorBreakChance;
	
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
		fullName = "M1000 Classic (Focused Shots)";
		
		// Base stats, before mods or overclocks alter them:
		directDamage = 50;
		focusedShotMultiplier = 2.0;
		carriedAmmo = 96;
		magazineSize = 8;
		// rateOfFire = 4.0;
		reloadTime = 2.5;
		delayBeforeFocusing = 0.4;  // seconds
		focusDuration = 0.6;  // seconds.
		movespeedWhileFocusing = 0.3;
		weakpointBonus = 0.1;
		armorBreakChance = 0.3;
		
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
		tier1[0] = new Mod("Expanded Ammo Bags", "You had to give up some sandwich-storage, but your total ammo capacity is increased!", 1, 0);
		tier1[1] = new Mod("Increased Caliber Rounds", "The good folk in R&D have been busy. The overall damage of your weapon is increased.", 1, 1);
		
		tier2 = new Mod[2];
		tier2[0] = new Mod("Fast-Charging Coils", "Faster focus when holding down the trigger.", 2, 0);
		tier2[1] = new Mod("Better Weight Balance", "Improved hip-shot accuracy", 2, 1);
		
		tier3 = new Mod[2];
		tier3[0] = new Mod("Killer Focus", "Greater focus damage bonus", 3, 0);
		tier3[1] = new Mod("Extended Clip", "The good thing about clips, magazines, ammo drums, fuel tanks... You can always get bigger variants.", 3, 1);
		
		tier4 = new Mod[3];
		tier4[0] = new Mod("Super Blowthrough Rounds", "Shaped projectiles designed to over-penetrate targets with a minimal loss of energy. In other words: Fire straight through several enemies at once!", 4, 0);
		tier4[1] = new Mod("Hollow-Point Bullets", "Hit 'em where it hurts! Literally! We've upped the damage you'll be able to do to any creatures fleshy bits. You're welcome.", 4, 1);
		tier4[2] = new Mod("Hardened Rounds", "We're proud of this one. Armor shredding. Tear through that high-impact plating of those big buggers like butter. What could be finer?", 4, 2);
		
		tier5 = new Mod[3];
		tier5[0] = new Mod("Hitting Where it Hurts", "Focused shots stagger the target", 5, 0);
		tier5[1] = new Mod("Precision Terror", "Killing your target with a focused shot to the weakspot will send nearby creatures fleeing with terror!", 5, 1);
		tier5[2] = new Mod("Killing Machine", "You can perform a lightning-fast reload right after killing an enemy.", 5, 2);
		
		overclocks = new Overclock[6];
		overclocks[0] = new Overclock(Overclock.classification.clean, "Hoverclock", "Your movement slows down for a few seconds while using focus mode in the air.", 0);
		overclocks[1] = new Overclock(Overclock.classification.clean, "Minimal Clips", "Make space for more ammo and speed up reloads by getting rid of dead weight on the clips.", 1);
		overclocks[2] = new Overclock(Overclock.classification.balanced, "Active Stability System", "Focus without slowing down but the power drain from the coils lowers the power of the focused shots.", 2);
		overclocks[3] = new Overclock(Overclock.classification.balanced, "Hipster", "A rebalancing of weight distribution, enlarged vents and a reshaped grip result in a rifle that is more controllable when hip-firing in quick succession but at the cost of pure damage output.", 3);
		overclocks[4] = new Overclock(Overclock.classification.unstable, "Electrocuting Focus Shots", "Embedded capacitors in a copper core carry the electric charge from the EM coils used for focus shots and will electrocute the target at the cost of a reduced focus shot damage bonus.", 4);
		overclocks[5] = new Overclock(Overclock.classification.unstable, "Supercooling Chamber", "Take the M1000'S focus mode to the extreme by supercooling the rounds before firing to improve their acceleration through the coils, but the extra coolant in the clips limits how much ammo you can bring.", 5);
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
				System.out.println("Classic's first tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[1] == 'C') {
				System.out.println("Classic's second tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			if (symbols[2] == 'C') {
				System.out.println("Classic's third tier of mods only has two choices, so 'C' is an invalid choice.");
				combinationIsValid = false;
			}
			List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '-'});
			if (!validOverclockSymbols.contains(symbols[5])) {
				System.out.println("The sixth symbol, " + symbols[5] + ", is not a number between 1-6 or a hyphen");
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
			}
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	@Override
	public Classic_FocusShot clone() {
		return new Classic_FocusShot(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
	}
	
	public String getDwarfClass() {
		return "Scout";
	}
	public String getSimpleName() {
		return "Classic_FocusShot";
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	private double getDirectDamage() {
		double toReturn = directDamage;
		
		// Additive bonuses first
		if (selectedOverclock == 3) {
			toReturn -= 20;
		}
		
		// Multiplicative bonuses last
		if (selectedTier1 == 1) {
			toReturn *= 1.2;
		}
		
		return toReturn;
	}
	private double getFocusedShotMultiplier() {
		double toReturn = focusedShotMultiplier;
		
		// Additive bonuses first
		if (selectedTier3 == 0) {
			toReturn += 0.25;
		}
		
		if (selectedOverclock == 2 || selectedOverclock == 4) {
			toReturn -= 0.25;
		}
		else if (selectedOverclock == 5) {
			toReturn += 1.25;
		}
		
		return toReturn;
	}
	private int getCarriedAmmo() {
		double toReturn = carriedAmmo;
		
		if (selectedTier1 == 0) {
			toReturn += 32;
		}
		
		if (selectedOverclock == 1) {
			toReturn += 16;
		}
		else if (selectedOverclock == 3) {
			toReturn += 72;
		}
		else if (selectedOverclock == 5) {
			toReturn *= 0.635;
		}
		
		// Divide by 2 to account for firing two ammo per focused shot
		// Use ceiling function because you can do a fully-charged Focus Shot with only 1 ammo if there's no ammo left in the clip
		return (int) Math.ceil(toReturn / 2.0);
	}
	private int getMagazineSize() {
		int toReturn = magazineSize;
		
		if (selectedTier3 == 1) {
			toReturn += 6;
		}
		
		// Divide by 2 to account for firing two ammo per focused shot
		return toReturn / 2;
	}
	private double getRateOfFire() {
		// Because the max RoF will never be achieved with Focus Shots, instead model the RoF as the inverse of the Focus Duration
		return 1.0 / (delayBeforeFocusing + getFocusDuration());
	}
	private double getReloadTime() {
		double toReturn = reloadTime;
		
		if (selectedTier5 == 2) {
			// "Killing Machine": if you manually reload within 1 second after a kill, the reload time is reduced by approximately 0.75 seconds.
			// Because Sustained DPS uses this ReloadTime method, I'm choosing to use the Ideal Burst DPS as a quick-and-dirty estimate how often a kill gets scored 
			// so that this doesn't infinitely loop.
			double killingMachineManualReloadWindow = 1.0;
			double killingMachineReloadReduction = 0.75;
			double burstTTK = EnemyInformation.averageHealthPool() / calculateIdealBurstDPS();
			// Don't let a high Burst DPS increase this beyond a 100% uptime
			double killingMachineUptimeCoefficient = Math.min(killingMachineManualReloadWindow / burstTTK, 1.0);
			double effectiveReloadReduction = killingMachineUptimeCoefficient * killingMachineReloadReduction;
			
			toReturn -= effectiveReloadReduction;
		}
		
		if (selectedOverclock == 1) {
			toReturn -= 0.2;
		}
		
		return toReturn;
	}
	private double getFocusDuration() {
		double focusSpeedCoefficient = 1.0;
		if (selectedTier2 == 0) {
			focusSpeedCoefficient *= 1.6;
		}
		
		if (selectedOverclock == 5) {
			focusSpeedCoefficient *= 0.5;
		}
		
		return focusDuration / focusSpeedCoefficient;
	}
	private double getMovespeedWhileFocusing() {
		double modifier = movespeedWhileFocusing;
		
		if (selectedOverclock == 2) {
			modifier += 0.7;
		}
		else if (selectedOverclock == 5) {
			modifier *= 0;
		}
		
		return MathUtils.round(modifier * DwarfInformation.walkSpeed, 2);
	}
	private int getMaxPenetrations() {
		if (selectedTier4 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	private double getWeakpointBonus() {
		double toReturn = weakpointBonus;
		
		if (selectedTier4 == 1) {
			toReturn += 0.25;
		}
		
		return toReturn;
	}
	private double getArmorBreakChance() {
		double toReturn = armorBreakChance;
		
		if (selectedTier4 == 2) {
			toReturn += 2.2;
		}
		
		return toReturn;
	}
	private double getRecoil() {
		double toReturn = 1.0;
		
		if (selectedTier2 == 1) {
			toReturn *= 0.5;
		}
		
		if (selectedOverclock == 3) {
			toReturn *= 0.5;
		}
		
		return toReturn;
	}
	private int getStunDuration() {
		if (selectedTier5 == 0) {
			return 3;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public StatsRow[] getStats() {
		StatsRow[] toReturn = new StatsRow[14];
		
		toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), selectedOverclock == 3 || selectedTier1 == 1);
		
		boolean multiplierModified = selectedTier3 == 0 || selectedOverclock == 2 || selectedOverclock == 4 || selectedOverclock == 5;
		toReturn[1] = new StatsRow("Focused Shot Multiplier:", convertDoubleToPercentage(getFocusedShotMultiplier()), multiplierModified);
		
		toReturn[2] = new StatsRow("Delay Before Focusing:", delayBeforeFocusing, false);
		
		toReturn[3] = new StatsRow("Focus Shot Charge-up Duration:", getFocusDuration(), selectedTier2 == 0 || selectedOverclock == 5);
		
		toReturn[4] = new StatsRow("Magazine Size:", getMagazineSize(), selectedTier3 == 1);
		
		boolean carriedAmmoModified = selectedTier1 == 0 || selectedOverclock == 1 || selectedOverclock == 3 || selectedOverclock == 5;
		toReturn[5] = new StatsRow("Max Ammo:", getCarriedAmmo(), carriedAmmoModified);
		
		toReturn[6] = new StatsRow("Rate of Fire:", getRateOfFire(), selectedTier2 == 0 || selectedOverclock == 5);
		
		toReturn[7] = new StatsRow("Reload Time:", getReloadTime(), selectedTier5 == 2 || selectedOverclock == 1);
		
		toReturn[8] = new StatsRow("Weakpoint Bonus:", "+" + convertDoubleToPercentage(getWeakpointBonus()), selectedTier4 == 1);
		
		toReturn[9] = new StatsRow("Armor Breaking:", convertDoubleToPercentage(getArmorBreakChance()), selectedTier4 == 2);
		
		toReturn[10] = new StatsRow("Stun Duration:", getStunDuration(), selectedTier5 == 0, selectedTier5 == 0);
		
		toReturn[11] = new StatsRow("Max Penetrations:", getMaxPenetrations(), selectedTier4 == 0, selectedTier4 == 0);
		
		boolean recoilModified = selectedTier2 == 1 || selectedOverclock == 3;
		toReturn[12] = new StatsRow("Recoil:", convertDoubleToPercentage(getRecoil()), recoilModified, recoilModified);
		
		toReturn[13] = new StatsRow("Movespeed While Focusing: (m/sec)", getMovespeedWhileFocusing(), selectedOverclock == 2 || selectedOverclock == 5);
		
		return toReturn;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/

	@Override
	public boolean currentlyDealsSplashDamage() {
		return false;
	}

	// Single-target calculations
	private double calculateSingleTargetDPS(boolean burst, boolean weakpoint) {
		double duration;
		if (burst) {
			duration = ((double) getMagazineSize()) / getRateOfFire();
		}
		else {
			duration = (((double) getMagazineSize()) / getRateOfFire()) + getReloadTime();
		}
		
		double directDamage = getDirectDamage() * getFocusedShotMultiplier();
		if (weakpoint) {
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
		return calculateSingleTargetDPS(false, true);
	}
	
	// Multi-target calculations
	@Override
	public double calculateAdditionalTargetDPS() {
		if (selectedTier4 == 0) {
			return calculateIdealSustainedDPS();
		}
		else {
			return 0;
		}
	}

	@Override
	public double calculateMaxMultiTargetDamage() {
		double totalDamageDealt = calculateMaxNumTargets() * (getMagazineSize() + getCarriedAmmo()) * getDirectDamage() * getFocusedShotMultiplier();
		
		double electrocuteDoTTotalDamage = 0;
		if (selectedOverclock == 4) {
			double electrocuteDoTDamagePerEnemy = calculateAverageDoTDamagePerEnemy(0, DoTInformation.Electro_SecsDuration, DoTInformation.Electro_DPS);
			
			double estimatedNumEnemiesKilled = calculateMaxNumTargets() * (calculateFiringDuration() / averageTimeToKill());
			
			electrocuteDoTTotalDamage = electrocuteDoTDamagePerEnemy * estimatedNumEnemiesKilled;
		}
		
		return totalDamageDealt + electrocuteDoTTotalDamage;
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
	public double averageTimeToKill() {
		return EnemyInformation.averageHealthPool() / sustainedWeakpointDPS();
	}

	@Override
	public double averageOverkill() {
		double dmgPerShot = increaseBulletDamageForWeakpoints(getDirectDamage() * getFocusedShotMultiplier(), getWeakpointBonus());
		double enemyHP = EnemyInformation.averageHealthPool();
		double dmgToKill = Math.ceil(enemyHP / dmgPerShot) * dmgPerShot;
		return ((dmgToKill / enemyHP) - 1.0) * 100.0;
	}

	@Override
	public double estimatedAccuracy(boolean weakpointAccuracy) {
		// Manually aimed
		return -1.0;
	}

	@Override
	public double utilityScore() {
		// OC "Active Stability System" removes the movespeed penalty while Focusing
		utilityScores[0] = (getMovespeedWhileFocusing() - MathUtils.round(movespeedWhileFocusing * DwarfInformation.walkSpeed, 2)) * UtilityInformation.Movespeed_Utility;
		
		// OC "Hoverclock" gives a 2 second cap to Scout's vertical movement speed (guess: 0.5 m/sec?), but after that 2sec ends original velocity is restored
		if (selectedOverclock == 0) {
			// Because the vertical movespeed cap of +- 0.5 m/sec can be used to negate fall damage from infinite height, there's not really a 
			// way to give this OC a numerical value. For now, I'm just gonna call it 10 and move on.
			utilityScores[0] += 10;
		}
		
		// Armor Breaking
		// Because Blowthrough Rounds are on the same tier as Armor Break, this bonus isn't multiplied by max num targets.
		utilityScores[2] = (getArmorBreakChance() - 1) * UtilityInformation.ArmorBreak_Utility;
		
		// OC "Electrocuting Focus Shots" = 100% chance to electrocute on focused shots
		if (selectedOverclock == 4) {
			utilityScores[3] = calculateMaxNumTargets() * DoTInformation.Electro_SecsDuration * UtilityInformation.Electrocute_Slow_Utility;
		}
		else {
			utilityScores[3] = 0;
		}
		
		// Mod Tier 5 "Precision Terror" = 100% chance to Fear in 2m AoE
		if (selectedTier5 == 1) {
			double uptimeCoefficient = Math.min(UtilityInformation.Fear_Duration / averageTimeToKill(), 1);
			int numGlyphidsFeared = 12;  // calculateNumGlyphidsInRadius(2);
			utilityScores[4] = uptimeCoefficient * numGlyphidsFeared * UtilityInformation.Fear_Duration * UtilityInformation.Fear_Utility;
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
}
