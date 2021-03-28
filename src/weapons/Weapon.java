package weapons;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import javax.swing.JPanel;

import guiPieces.AoEVisualizer;
import guiPieces.GuiConstants;
import guiPieces.customButtons.ButtonIcons.modIcons;
import modelPieces.AccuracyEstimator;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import utilities.ConditionalArrayList;
import utilities.MathUtils;
import utilities.Point2D;

public abstract class Weapon extends Observable {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	protected String fullName = "";
	protected BufferedImage weaponPic;
	protected boolean customizableRoF = false;
	// This value gets set back to 0 after every mod/OC selection so that changing the build resets CustomRoF to max RoF, and once the user has settled on a build they can tweak the RoF via the GUI.
	// It may feel frustrating for the user, but it neatly sidesteps an issue where the CustomRoF could be greater than the new Max RoF and artificially inflates the DPS stats.
	protected double customRoF = 0;
	// Since several of the weapons have a Homebrew Powder mod or OC, I'm adding this coefficient in the parent class so that they can all be updated simultaneously.
	// Taking the (integral of x dx from 0.8 -> 1.4) / (1.4 - 0.8) results in the intuitive 1.1
	protected double homebrewPowderCoefficient = 1.1;
	
	protected boolean modsAndOCsInitialized = false;
	protected String invalidCombinationMessage = "";
	// If any of these shorts is set to -1, that means there should be no mods equipped at that tier.
	protected Mod[] tier1;
	protected int selectedTier1;
	protected Mod[] tier2;
	protected int selectedTier2;
	protected Mod[] tier3;
	protected int selectedTier3;
	protected Mod[] tier4;
	protected int selectedTier4;
	protected Mod[] tier5;
	protected int selectedTier5;
	
	protected Overclock[] overclocks;
	protected int selectedOverclock;
	
	// AoE Radius, AoE Efficiency Coefficient, Total num Grunts hit in AoE radius
	protected double[] aoeEfficiency;
	
	// There are 31 breakpoints: 11 normal damage, 15 weakpoints, and 5 Light Armor.
	protected int[] breakpoints = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	// Mobility, Damage Resist, Armor Break, Slow, Fear, Stun, Freeze
	// Set them all to zero to start, then override values in child objects as necessary.
	protected double[] utilityScores = {0, 0, 0, 0, 0, 0, 0};
	
	
	protected double[][] damageWastedByArmorPerCreature = {
		// Spawn Probabilities
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		// Damage Wasted
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
	};
	
	protected double[][] overkillPercentages = {
		// Spawn Probabilities
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		// Overkill Percentages
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}	
	};
	
	// Burning, Frozen, Electrocuted, IFG Grenade
	protected boolean[] statusEffects = {false, false, false, false};
	
	protected boolean enableWeakpointsDPS = false;
	protected boolean enableGeneralAccuracyDPS = false;
	protected boolean enableArmorWastingDPS = false;
	
	// The only legitimate values for these two variables are -1 and [0, 100], so setting them to -100 lets me know later if these values have been set or not.
	private double metric_generalAccuracy = -100;
	private double metric_weakpointAccuracy = -100;
	
	protected double[] baselineBurstDPS;
	protected double[] baselineSustainedDPS;
	protected double[] baselineCalculatedStats;
	private AoEVisualizer illustration = null;
	
	protected AccuracyEstimator accEstimator = new AccuracyEstimator();
	
	/****************************************************************************************
	* Build from combination
	****************************************************************************************/
	public boolean isCombinationValid(String combination) {
		// Early exit conditions
		if (modsAndOCsInitialized == false) {
			// This flag should be set to true at the end of every initializeModsAndOverclocks() method
			return false;
		}
		if (tier1.length < 1 || tier2.length < 1 || tier3.length < 1 || tier4.length < 1 || tier5.length < 1 || overclocks.length < 1) {
			// If any of the arrays hasn't been initialized, this method would throw errors when it tries to see if an index is outside the number of elements
			return false;
		}
		if (fullName.equals("")) {
			// The fullname should be set inside the constructor of each weapon, but I'm adding this check just to be on the safe side.
			return false;
		}
		
		boolean combinationIsValid = true;
		invalidCombinationMessage = "<br/>";
		
		/**********************************************************************
		* First check: does the new combination contain exactly 6 characters?
		***********************************************************************/
		if (combination.length() != 6) {
			invalidCombinationMessage += "\"" + combination + "\" does not have 6 characters, which makes it invalid.";
			// If it fails the first check, return immediately so that it doesn't get second and third checks' error text appended unnecessarily.
			return false;
		}
		
		/**********************************************************************
		* Second check: are the first five characters capital ABC or hyphen, 
		* and the 6th character a number 1-7 or a hyphen?
		***********************************************************************/
		char[] symbols = combination.toCharArray();
		List<Character> validModSymbols = Arrays.asList(new Character[] {'A', 'B', 'C', '-'});
		for (int i = 0; i < 5; i ++) {
			if (!validModSymbols.contains(symbols[i])) {
				invalidCombinationMessage += "Character #" + (i+1) + ", '" + symbols[i] + "', is not a capital letter between A-C or a hyphen.<br/>";
				combinationIsValid = false;
			}
		}
		
		List<Character> validOverclockSymbols = Arrays.asList(new Character[] {'1', '2', '3', '4', '5', '6', '7', '-'});
		if (!validOverclockSymbols.contains(symbols[5])) {
			invalidCombinationMessage += "Character #6, '" + symbols[5] + "', is not a number between 1-7 or a hyphen.<br/>";
			combinationIsValid = false;
		}
		
		if (!combinationIsValid) {
			// Choosing to return early if it fails second check, so that the error text from third check won't be appended unnecessarily
			return false;
		}
		
		/**********************************************************************
		* Third check: do any of the letters indicate a Mod outside of this 
		* weapon's corresponding Mod Tier, or does the number indicate an 
		* Overclock outside of this weapon's Overclock group?
		***********************************************************************/
		// Because all weapons' Mod Tiers have at least 2 mods, I only need to check cases for 'C'
		if (symbols[0] == 'C' && tier1.length < 3) {
			invalidCombinationMessage += fullName + " Mod Tier 1 only has two mods, so 'C' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		if (symbols[1] == 'C' && tier2.length < 3) {
			invalidCombinationMessage += fullName + " Mod Tier 2 only has two mods, so 'C' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		if (symbols[2] == 'C' && tier3.length < 3) {
			invalidCombinationMessage += fullName + " Mod Tier 3 only has two mods, so 'C' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		if (symbols[3] == 'C' && tier4.length < 3) {
			invalidCombinationMessage += fullName + " Mod Tier 4 only has two mods, so 'C' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		if (symbols[4] == 'C' && tier5.length < 3) {
			invalidCombinationMessage += fullName + " Mod Tier 5 only has two mods, so 'C' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		
		// Overclocks can be anywhere from 5-7, so I need to check all instances of '6' and '7'
		if (overclocks.length == 5 && (symbols[5] == '6' || symbols[5] == '7')) {
			invalidCombinationMessage += fullName + " only has five Overclocks, so '" + symbols[5] + "' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		else if (overclocks.length == 6 && symbols[5] == '7') {
			invalidCombinationMessage += fullName + " only has six Overclocks, so '7' is an invalid choice.<br/>";
			combinationIsValid = false;
		}
		
		// If this method has successfully evaluated to this point, then combinationIsValid will still be set to True.
		return combinationIsValid;
	}
	
	// This method can be used by the GUI to tell the user why the String they entered is being rejected.
	public String getInvalidCombinationErrorMessage() {
		return invalidCombinationMessage;
	}
	
	/*
		This method used to be a void return, but in order to use it with user-comparing-builds features 
		it has to have a boolean return value to indicate whether or not their manual entry will work or not.
		
		In theory, this should ALWAYS be preceded by the code doing the isCombinationValid() check first, 
		but I'm adding it here to make absolutely sure. It doesn't take too long to run twice.
	*/
	public boolean buildFromCombination(String combination) {
		return buildFromCombination(combination, true);
	}
	public boolean buildFromCombination(String combination, boolean updateGUI) {
		boolean combinationIsValid = isCombinationValid(combination);
		
		if (!combinationIsValid) {
			// Return False to indicate that the build requested by the input combination is not possible.
			return false;
		}
		else {
			// This section of the code relies entirely on isCombinationValid() doing proper input santization and validation.
			char[] symbols = combination.toCharArray();
			
			// Start by setting all mods/OC to -1 so that no matter what the old build was, the new build will go through with no problem.
			setSelectedModAtTier(1, -1, false);
			setSelectedModAtTier(2, -1, false);
			setSelectedModAtTier(3, -1, false);
			setSelectedModAtTier(4, -1, false);
			setSelectedModAtTier(5, -1, false);
			setSelectedOverclock(-1, false);
			
			// Because they're already set to -1 above, these switch statements don't need to account for the hyphen case.
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
				case 'C': {
					setSelectedModAtTier(2, 2, false);
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
			
			// Re-set AoE Efficiency
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			if (updateGUI && countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
			
			// Return True to let other objects know that they have successfully edited this Weapon's build
			return true;
		}
	}
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	public int getSelectedModAtTier(int tierNumber) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					return selectedTier1;
				}
				case 2: {
					return selectedTier2;
				}
				case 3: {
					return selectedTier3;
				}
				case 4: {
					return selectedTier4;
				}
				case 5: {
					return selectedTier5;
				}
				default: {
					return -2;
				}
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
			return -2;
		}
	}
	public void setSelectedModAtTier(int tierNumber, int newSelection, boolean updateGUI) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					if (newSelection > -2 && newSelection < tier1.length) {
						if (selectedTier1 > -1) {
							tier1[selectedTier1].toggleSelected();
						}
						
						if (newSelection == selectedTier1) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier1 = -1;
						}
						else {
							selectedTier1 = newSelection;
							
							if (selectedTier1 > -1) {
								tier1[selectedTier1].toggleSelected();
							}
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 2: {
					if (newSelection > -2 && newSelection < tier2.length) {
						if (selectedTier2 > -1) {
							tier2[selectedTier2].toggleSelected();
						}
						
						if (newSelection == selectedTier2) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier2 = -1;
						}
						else {
							selectedTier2 = newSelection;
							
							if (selectedTier2 > -1) {
								tier2[selectedTier2].toggleSelected();
							}
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 3: {
					if (newSelection > -2 && newSelection < tier3.length) {
						if (selectedTier3 > -1) {
							tier3[selectedTier3].toggleSelected();
						}
						
						if (newSelection == selectedTier3) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier3 = -1;
						}
						else {
							selectedTier3 = newSelection;
							
							if (selectedTier3 > -1) {
								tier3[selectedTier3].toggleSelected();
							}
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 4: {
					if (newSelection > -2 && newSelection < tier4.length) {
						if (selectedTier4 > -1) {
							tier4[selectedTier4].toggleSelected();
						}
						
						if (newSelection == selectedTier4) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier4 = -1;
						}
						else {
							selectedTier4 = newSelection;
							
							if (selectedTier4 > -1) {
								tier4[selectedTier4].toggleSelected();
							}
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 5: {
					if (newSelection > -2 && newSelection < tier5.length) {
						if (selectedTier5 > -1) {
							tier5[selectedTier5].toggleSelected();
						}
						
						if (newSelection == selectedTier5) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier5 = -1;
						}
						else {
							selectedTier5 = newSelection;
							
							if (selectedTier5 > -1) {
								tier5[selectedTier5].toggleSelected();
							}
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
			}
			
			// Un-set these values for the new build
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
			customRoF = 0;
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			damageWastedByArmor();
			
			if (updateGUI && countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
		}
	}
	// Because this is only called from GUI, I don't need to add updateGUI flag.
	public void setIgnoredModAtTier(int tierNumber, int indexToIgnore) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					if (indexToIgnore > -2 && indexToIgnore < tier1.length) {
						// Special case: if the mod being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
						if (indexToIgnore == selectedTier1) {
							selectedTier1 = -1;
						}
						
						tier1[indexToIgnore].toggleIgnored();
					}
					break;
				}
				case 2: {
					if (indexToIgnore > -2 && indexToIgnore < tier2.length) {
						// Special case: if the mod being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
						if (indexToIgnore == selectedTier2) {
							selectedTier2 = -1;
						}
						
						tier2[indexToIgnore].toggleIgnored();
					}
					break;
				}
				case 3: {
					if (indexToIgnore > -2 && indexToIgnore < tier3.length) {
						// Special case: if the mod being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
						if (indexToIgnore == selectedTier3) {
							selectedTier3 = -1;
						}
						
						tier3[indexToIgnore].toggleIgnored();
					}
					break;
				}
				case 4: {
					if (indexToIgnore > -2 && indexToIgnore < tier4.length) {
						// Special case: if the mod being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
						if (indexToIgnore == selectedTier4) {
							selectedTier4 = -1;
						}
						
						tier4[indexToIgnore].toggleIgnored();
					}
					break;
				}
				case 5: {
					if (indexToIgnore > -2 && indexToIgnore < tier5.length) {
						// Special case: if the mod being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
						if (indexToIgnore == selectedTier5) {
							selectedTier5 = -1;
						}
						
						tier5[indexToIgnore].toggleIgnored();
					}
					break;
				}
			}
			
			// Un-set these values for the new build
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
			customRoF = 0;
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			damageWastedByArmor();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
		}
	}
	
	public int getSelectedOverclock() {
		return selectedOverclock;
	}
	public void setSelectedOverclock(int newSelection, boolean updateGUI) {
		if (newSelection > -2 && newSelection < overclocks.length) {
			if (selectedOverclock > -1) {
				overclocks[selectedOverclock].toggleSelected();
			}
			
			if (newSelection == selectedOverclock) {
				// If the same overclock is selected, that indicates that it's being unequipped. Set overclock = -1 to affect the math properly.
				selectedOverclock = -1;
			}
			else {
				selectedOverclock = newSelection;
				
				if (selectedOverclock > -1) {
					overclocks[selectedOverclock].toggleSelected();
				}
			}
			
			// Un-set these values for the new build
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
			customRoF = 0;
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			damageWastedByArmor();
			
			if (updateGUI && countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Overclock choice is outside array bounds");
		}
	}
	// Because this is only called from GUI, I don't need to add updateGUI flag.
	public void setIgnoredOverclock(int indexToIgnore) {
		if (indexToIgnore > -2 && indexToIgnore < overclocks.length) {
			// Special case: if the overclock being ignored was previously selected, un-select it so that the math lines up with what's displayed on the GUI
			if (indexToIgnore == selectedOverclock) {
				selectedOverclock = -1;
			}
			
			overclocks[indexToIgnore].toggleIgnored();
			
			// Un-set these values for the new build
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
			customRoF = 0;
			
			if (currentlyDealsSplashDamage()) {
				setAoEEfficiency();
			}
			
			damageWastedByArmor();
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	public boolean[] getCurrentStatusEffects() {
		return statusEffects;
	}
	// Because this is only used by the GUI, I'm choosing not to add the "updateGUI" flag.
	public void setStatusEffect(int effectIndex, boolean newValue) {
		if (effectIndex > -1 && effectIndex < statusEffects.length) {
			// Special case: Burning and Frozen are mutually exclusive statuses, so make sure that if one gets set to true, the other is automatically set to false
			if (effectIndex == 0 && newValue) {
				statusEffects[1] = false;
			}
			else if (effectIndex == 1 && newValue) {
				statusEffects[0] = false;
			}
			
			statusEffects[effectIndex] = newValue;
			
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	
	public Mod[] getModsAtTier(int tierNumber) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					return tier1;
				}
				case 2: {
					return tier2;
				}
				case 3: {
					return tier3;
				}
				case 4: {
					return tier4;
				}
				case 5: {
					return tier5;
				}
				default: {
					return null;
				}
			}
		}
		else {
			System.out.println("Tier #" + tierNumber + " is not a valid tier of gear modifications");
			return null;
		}
	}
	public Overclock[] getOverclocks() {
		return overclocks;
	}
	
	public int[] getModsAtTier(int tierNumber, boolean subset) {
		// First, get the right mods and selection
		Mod[] thisTier;
		int selection;
		switch (tierNumber) {
			case 1: {
				thisTier = tier1;
				selection = selectedTier1;
				break;
			}
			case 2: {
				thisTier = tier2;
				selection = selectedTier2;
				break;
			}
			case 3: {
				thisTier = tier3;
				selection = selectedTier3;
				break;
			}
			case 4: {
				thisTier = tier4;
				selection = selectedTier4;
				break;
			}
			case 5: {
				thisTier = tier5;
				selection = selectedTier5;
				break;
			}
			default: {
				thisTier = new Mod[1];
				selection = -1;
				break;
			}
		}
		
		if (subset) {
			// Early exit condition: if a mod is already selected, then just return an array with the selected index
			if (selection > -1) {
				return new int[] {selection};
			}
			
			ConditionalArrayList<Integer> macguffin = new ConditionalArrayList<Integer>();
			// Make sure to add "no Mod selected" as an option for this tier, in case all mods get ignored.
			macguffin.add(-1);
			for (int i = 0; i < thisTier.length; i++) {
				macguffin.conditionalAdd(i, !thisTier[i].isIgnored());
			}
			
			// This line of magic sourced from https://stackoverflow.com/a/23945015
			return macguffin.stream().mapToInt(i->i).toArray();
		}
		else {
			int[] toReturn = new int[thisTier.length + 1];
			// Always include the option for no mod to be selected
			toReturn[0] = -1;
			for (int i = 0; i < thisTier.length; i++) {
				toReturn[i + 1] = i;
			}
			
			return toReturn;
		}
	}
	public int[] getOverclocks(boolean subset) {
		if (subset) {
			// Early exit condition: if an OC is already selected, then just return an array with the selected index
			if (selectedOverclock > -1) {
				return new int[] {selectedOverclock};
			}
			
			ConditionalArrayList<Integer> macguffin = new ConditionalArrayList<Integer>();
			// Make sure to add "no OC selected" as an option for this tier, in case all OCs get ignored.
			macguffin.add(-1);
			for (int i = 0; i < overclocks.length; i++) {
				macguffin.conditionalAdd(i, !overclocks[i].isIgnored());
			}
			
			// This line of magic sourced from https://stackoverflow.com/a/23945015
			return macguffin.stream().mapToInt(i->i).toArray();
		}
		else {
			int[] toReturn = new int[overclocks.length + 1];
			// Always include the option for no OC to be selected
			toReturn[0] = -1;
			for (int i = 0; i < overclocks.length; i++) {
				toReturn[i + 1] = i;
			}
			
			return toReturn;
		}
	}
	
	public boolean isRofCustomizable() {
		return customizableRoF;
	}
	public void setCustomRoF(double newRoF) {
		// This will be a logic error for any class that has customizableRoF=true but doesn't initialize the customRoF=getRateOfFire()
		if (customizableRoF && newRoF > 0 && newRoF <= getRateOfFire()) {
			customRoF = newRoF;
			
			// This method is only called from the GUI, so I have to refresh said GUI for users to see the change.
			// Un-set these values for the new build
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
						
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	public double getCustomRoF() {
		if (customizableRoF && customRoF > 0) {
			return customRoF;
		}
		else {
			return getRateOfFire();
		}
	}
	public double getRateOfFire() {
		// This method only exists to be overridden in child classes, but it's necessary to make the user-set RoF trick work. :(
		return -1;
	}
	public double getRecommendedRateOfFire() {
		// Another useless getter unless the Weapon works with the user-set RoF trick.
		return -1;
	}
	
	protected abstract void initializeModsAndOverclocks();
	
	protected void setBaselineStats() {
		int oldT1 = selectedTier1, oldT2 = selectedTier2, oldT3 = selectedTier3, oldT4 = selectedTier4, oldT5 = selectedTier5, oldOC = selectedOverclock;
		selectedTier1 = selectedTier2 = selectedTier3 = selectedTier4 = selectedTier5 = selectedOverclock = -1;
		
		if (currentlyDealsSplashDamage()) {
			setAoEEfficiency();
		}
		
		damageWastedByArmor();
		
		baselineBurstDPS = new double[]{
			calculateSingleTargetDPS(true, false, false, false),  // Ideal
			calculateSingleTargetDPS(true, true, false, false),  // Weakpoint
			calculateSingleTargetDPS(true, false, true, false),  // Accuracy
			calculateSingleTargetDPS(true, false, false, true),  // Armor Wasting
			calculateSingleTargetDPS(true, true, true, false),  // WP + Acc
			calculateSingleTargetDPS(true, true, false, true),  // WP + AW
			calculateSingleTargetDPS(true, false, true, true),  // Acc + AW
			calculateSingleTargetDPS(true, true, true, true)  // WP + Acc + AW
		};
		
		baselineSustainedDPS = new double[]{
			calculateSingleTargetDPS(false, false, false, false),  // Ideal
			calculateSingleTargetDPS(false, true, false, false),  // Weakpoint
			calculateSingleTargetDPS(false, false, true, false),  // Accuracy
			calculateSingleTargetDPS(false, false, false, true),  // Armor Wasting
			calculateSingleTargetDPS(false, true, true, false),  // WP + Acc
			calculateSingleTargetDPS(false, true, false, true),  // WP + AW
			calculateSingleTargetDPS(false, false, true, true),  // Acc + AW
			calculateSingleTargetDPS(false, true, true, true)  // WP + Acc + AW
		};
		
		baselineCalculatedStats = new double[] {
			calculateAdditionalTargetDPS(), calculateMaxNumTargets(), calculateMaxMultiTargetDamage(), ammoEfficiency(), damageWastedByArmor(), 
			getGeneralAccuracy(), getWeakpointAccuracy(), calculateFiringDuration(), averageTimeToKill(), averageOverkill(), breakpoints(), 
			utilityScore(), averageTimeToCauterize()
		};
		selectedTier1 = oldT1;
		selectedTier2 = oldT2;
		selectedTier3 = oldT3;
		selectedTier4 = oldT4;
		selectedTier5 = oldT5;
		selectedOverclock = oldOC;
	}
	// These get used in WeaponTab for making the associated numbers change red/green/yellow
	public double getBaselineBurstDPS() {
		// Ideal
		if (!enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineBurstDPS[0];
		}
		// Wakpoint
		else if (enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineBurstDPS[1];
		}
		// Accuracy
		else if (!enableWeakpointsDPS &&  enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineBurstDPS[2];	
		}
		// Armor Wasting
		else if (!enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineBurstDPS[3];
		}
		// WP + Acc
		else if (enableWeakpointsDPS &&  enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineBurstDPS[4];
		}
		// WP + AW
		else if (enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineBurstDPS[5];
		}
		// Acc + AW
		else if (!enableWeakpointsDPS &&  enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineBurstDPS[6];
		}
		// WP + Acc + AW
		else if (enableWeakpointsDPS &&  enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineBurstDPS[7];
		}
		else {
			return -1;
		}
	}
	public double getBaselineSustainedDPS() {
		// Ideal
		if (!enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineSustainedDPS[0];
		}
		// Wakpoint
		else if (enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineSustainedDPS[1];
		}
		// Accuracy
		else if (!enableWeakpointsDPS &&  enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineSustainedDPS[2];	
		}
		// Armor Wasting
		else if (!enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineSustainedDPS[3];
		}
		// WP + Acc
		else if (enableWeakpointsDPS &&  enableGeneralAccuracyDPS && !enableArmorWastingDPS) {
			return baselineSustainedDPS[4];
		}
		// WP + AW
		else if (enableWeakpointsDPS &&  !enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineSustainedDPS[5];
		}
		// Acc + AW
		else if (!enableWeakpointsDPS &&  enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineSustainedDPS[6];
		}
		// WP + Acc + AW
		else if (enableWeakpointsDPS &&  enableGeneralAccuracyDPS && enableArmorWastingDPS) {
			return baselineSustainedDPS[7];
		}
		else {
			return -1;
		}
	}
	public double[] getBaselineStats() {
		return baselineCalculatedStats;
	}
	
	public boolean getWeakpointDPSEnabled() {
		return enableWeakpointsDPS;
	}
	public void setWeakpointDPS(boolean newValue, boolean updateGUI) {
		enableWeakpointsDPS = newValue;
		if (updateGUI && countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	public boolean getAccuracyDPSEnabled() {
		return enableGeneralAccuracyDPS;
	}
	public void setAccuracyDPS(boolean newValue, boolean updateGUI) {
		enableGeneralAccuracyDPS = newValue;
		if (updateGUI && countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	public boolean getArmorWastingDPSEnabled() {
		return enableArmorWastingDPS;
	}
	public void setArmorWastingDPS(boolean newValue, boolean updateGUI) {
		enableArmorWastingDPS = newValue;
		if (updateGUI && countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	
	protected void setAoEEfficiency() {
		/* 
			This is a placeholder method that only gets overwritten by weapons that deal splash damage (EPC_ChargedShot, GrenadeLauncher, and Autocannon)
			It just exists here so that Weapon can reference the method when it changes mods or OCs
			{
				AoE Radius
				AoE Efficiency Coefficient
				Total num Grunts hit in AoE radius
			}
		*/
		aoeEfficiency = new double[3];
	}
	
	// These methods are mostly pass-through to the internal AccuracyEstimator object
	public void setAccuracyDistance(double newDistance) {
		setAccuracyDistance(newDistance, true);
	}
	public void setAccuracyDistance(double newDistance, boolean updateGUI) {
		// Input sanitization
		if (newDistance > 0 && newDistance < 20) {
			// Un-set these values for the new estimates
			metric_generalAccuracy = -100;
			metric_weakpointAccuracy = -100;
			
			accEstimator.setDistance(newDistance);
			if (updateGUI && countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
	}
	public double getAccuracyDistance() {
		return accEstimator.getDistance();
	}
	
	public boolean isRecoilModeledInAccuracy() {
		return accEstimator.isModelingRecoil();
	}
	public void setModelRecoilInAccuracy(boolean newValue) {
		// Un-set these values for the new estimates
		metric_generalAccuracy = -100;
		metric_weakpointAccuracy = -100;
					
		accEstimator.setModelRecoil(newValue);
		// Because this method will only be called from the GUI, it doesn't need the updateGUI flag
		if (countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	
	public boolean isDwarfMoving() {
		return accEstimator.getDwarfIsMoving();
	}
	public void setDwarfMoving(boolean newValue) {
		// Un-set these values for the new estimates
		metric_generalAccuracy = -100;
		metric_weakpointAccuracy = -100;
					
		accEstimator.setDwarfIsMoving(newValue);
		// Because this method will only be called from the GUI, it doesn't need the updateGUI flag
		if (countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	
	public boolean accuracyCanBeVisualized() {
		return accEstimator.visualizerIsReady();
	}
	public boolean accuracyVisualizerShowsGeneralAccuracy() {
		return accEstimator.visualizerShowsGeneralAccuracy();
	}
	public void setAccuracyVisualizerToShowGeneralAccuracy(boolean newValue) {
		accEstimator.makeVisualizerShowGeneralAccuracy(newValue);
		// Because this method will only be called from the GUI, it doesn't need the updateGUI flag
		if (countObservers() > 0) {
			setChanged();
			notifyObservers();
		}
	}
	public JPanel getVisualizerPanel() {
		return accEstimator.getVisualizer();
	}
	
	// Rather than build out an entire cache for two variables per Weapon, I'll just fake it with these two methods.
	public double getGeneralAccuracy() {
		if (metric_generalAccuracy == -100) {
			metric_generalAccuracy = estimatedAccuracy(false);
			return metric_generalAccuracy;
		}
		else {
			return metric_generalAccuracy;
		}
	}
	public double getWeakpointAccuracy() {
		if (metric_weakpointAccuracy == -100) {
			metric_weakpointAccuracy = estimatedAccuracy(true);
			return metric_weakpointAccuracy;
		}
		else {
			return metric_weakpointAccuracy;
		}
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// Used by GUI and Auto-Calculator
	public abstract String getDwarfClass();
	public abstract String getSimpleName();
	
	// Used by the MySQL dump to establish the foreign-key relationships
	public abstract int getDwarfClassID();
	public abstract int getWeaponID();
	
	// Stats page
	public String getFullName() {
		return fullName;
	}
	public BufferedImage getPicture() {
		return weaponPic;
	}
	public String getCombination() {
		String toReturn = "";
		if (selectedTier1 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier1[selectedTier1].getLetterRepresentation();
		}
		if (selectedTier2 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier2[selectedTier2].getLetterRepresentation();
		}
		if (selectedTier3 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier3[selectedTier3].getLetterRepresentation();
		}
		if (selectedTier4 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier4[selectedTier4].getLetterRepresentation();
		}
		if (selectedTier5 < 0) {
			toReturn += "-";
		}
		else {
			toReturn += tier5[selectedTier5].getLetterRepresentation();
		}
		if (selectedOverclock < 0) {
			toReturn += "-";
		}
		else {
			toReturn += overclocks[selectedOverclock].getShortcutRepresentation();
		}
		return toReturn;
	}
	protected String convertDoubleToPercentage(double input) {
		int percent = (int) Math.round(input * 100.0);
		return percent + "%";
	}
	
	/*
		getStats() is the method used to interface between the Weapon and the left column of stats in WeaponTab. In general, the stats should be listed in this order:
		
		1. Direct Damage per projectile
		2. Number of projectiles per shot / Burst size
		3. Area Damage per shot
		4. Mechanics about how each shot gets fired (AoE radius, velocity, charge-time, etc)
		5. Magazine size / ammo consumed per shot
		6. Carried Ammo
		7. Rate of Fire (and any relevant mechanics)
		8. Reload Time / cooldown time and related mechanics or stats
		9. Weakpoint Bonus
		10. Armor Breaking
		11. Crowd Control effects (percentage to proc first, duration second)
		12. Additional Targets per projectile (blowthrough rounds, ricochets)
		13. Accuracy Modifiers
			a. Base Spread
			b. Spread Per Shot
			c. Max Bloom
			d. Spread Recovery Speed
			e. Recoil Per Shot
		14. Effects on the Dwarf
	*/
	public abstract StatsRow[] getStats();
	public abstract Weapon clone();
	
	/*
		This method is written out of frustration with having to do such an expanded numerical approximation of the Inverse Lambert function. Engineer's Shotgun Recoil
		doesn't play nicely with that method, so I'm going to create a binary-search styled method that samples the output of the Recoil equation and then narrows in on
		the desired value t, at which point it will return. To be clear: this is extremely inefficient to do computationally when the exact answer is technically calculable.
		However, after adding 20 segments and it STILL not being enough, my patience for Inverse Lambert has run out and I'm making this monstrosity instead.
		
		Because it works.
	*/
	protected double calculateTimeToRecoverRecoil(double recoilPitch, double recoilYaw, double mass, double springStiffness, double goalRecoilValue) {
		double desiredPrecision = 0.001;
		
		double v = Math.hypot(recoilPitch, recoilYaw);
		double w = Math.sqrt(springStiffness / mass);
		
		// Early exit condition: if the goalRecoilValue >= maxRecoil the while loop will never close, causing the program to freeze.
		if (goalRecoilValue >= v / (Math.E * w)) {
			return -1;
		}
		
		// Because Recoil changes from a positive slope to a negative slope at 1/w, I can start my binary search there.
		double minT = 1 / w;
		double maxT = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-w * 0.1 / v) / w;
		double currentT = minT;
		double currentRecoilValue =  Math.exp(-w * minT) * v * minT;
		
		while (currentRecoilValue + desiredPrecision < goalRecoilValue || currentRecoilValue - desiredPrecision > goalRecoilValue) {
			if (currentRecoilValue + desiredPrecision > goalRecoilValue) {
				minT = currentT;
				currentT = (currentT + maxT) / 2.0;
			}
			else if (currentRecoilValue - desiredPrecision < goalRecoilValue) {
				maxT = currentT;
				currentT = (minT + currentT) / 2.0;
			}
			else {
				// If by some bizarre coincidence currentT == goalRecoilValue, return immediately.
				return currentT;
			}
			
			currentRecoilValue =  Math.exp(-w * currentT) * v * currentT;
		}
		
		return currentT;
	}
	
	protected double calculateProbabilityToBreakLightArmor(double baseDamage) {
		return calculateProbabilityToBreakLightArmor(baseDamage, 1.0);
	}
	protected double calculateProbabilityToBreakLightArmor(double baseDamage, double armorBreaking) {
		return EnemyInformation.lightArmorBreakProbabilityLookup(baseDamage, armorBreaking, EnemyInformation.averageLightArmorStrength());
	}
	
	protected double calculateFearProcProbability(double fearFactor) {
		return Math.min(fearFactor * (1.0 - EnemyInformation.averageCourage()), 1.0);
	}
	
	protected double calculateRNGDoTDPSPerMagazine(double DoTProcChance, double DoTDPS, int magazineSize) {
		/*
		 	This method should be used whenever applying Electrocute or Neurotoxin DoTs, since they're RNG-based.
		 	It estimates what percentage of the magazine has to be fired before a DoT gets applied, and then uses
		 	that number to reduce the standard DPS of the DoT to effectively model what the DoT's average DPS is 
		 	across the duration of firing the magazine.
		 	
			When DoTs stack, like in BL2, the formula is PelletsPerSec * DoTDuration * DoTChance * DoTDmgPerSec.
			However, in DRG, once a DoT is applied it can only have its duration refreshed.
		*/
		double numBulletsFiredBeforeProc = Math.round(MathUtils.meanRolls(DoTProcChance));
		double numBulletsFiredAfterProc = magazineSize - numBulletsFiredBeforeProc;
		double DoTUptime = numBulletsFiredAfterProc / (numBulletsFiredBeforeProc + numBulletsFiredAfterProc);
		
		return DoTDPS * DoTUptime;
	}
	
	protected double calculateAverageDoTDamagePerEnemy(double timeBeforeProc, double averageDoTDuration, double DoTDPS) {
		/*
			I'm choosing to model the DoT total damage as "How much damage does the DoT do to the average enemy while it's still alive?"
		*/
		// Special case: Revolver can have a lower TTK than time to proc.
		double avgTTK = averageTimeToKill(false);
		if (avgTTK < timeBeforeProc) {
			return 0;
		}
		
		double timeWhileAfflictedByDoT = avgTTK - timeBeforeProc;
		
		// Don't let this math create a DoT that lasts longer than the default DoT duration.
		if (timeWhileAfflictedByDoT > averageDoTDuration) {
			timeWhileAfflictedByDoT = averageDoTDuration;
		}
		
		return timeWhileAfflictedByDoT * DoTDPS;
	}
	
	protected double[] calculateAverageAreaDamage(double radius, double fullDamageRadius, double falloffPercentageAtOuterEdge) {
		// Special condition: if fullDamageRadius >= radius, then return with 100% efficiency
		if (fullDamageRadius >= radius) {
			return new double[] {radius, 1.0, calculateNumGlyphidsInRadius(radius)};
		}
		
		// Want to test the fullDamageRadius radius and every radius in +0.05m increments, and finally the outermost radius
		int numRadiiToTest = (int) Math.floor((radius - fullDamageRadius) * 20.0) + 1;
		
		// Add an extra tuple at the start for the return values
		double[][] toReturn = new double[1 + numRadiiToTest][3];
		double currentRadius, currentDamage;
		int totalNumGlyphids = 0;
		int currentGlyphids;
		for (int i = 0; i < numRadiiToTest - 1; i++) {
			currentRadius = fullDamageRadius + i * 0.05;
			if (i > 0) {
				currentDamage = 1.0 - (1.0 - falloffPercentageAtOuterEdge) * (i - 1) / (numRadiiToTest - 2);
			}
			else {
				currentDamage = 1.0;
			}
			
			toReturn[i+1] = new double[3];
			toReturn[i+1][0] = currentRadius;
			toReturn[i+1][1] = currentDamage;
			currentGlyphids = calculateNumGlyphidsInRadius(currentRadius) - totalNumGlyphids;
			toReturn[i+1][2] = currentGlyphids;
			totalNumGlyphids += currentGlyphids;
		}
		toReturn[numRadiiToTest] = new double[3];
		toReturn[numRadiiToTest][0] = radius;
		toReturn[numRadiiToTest][1] = falloffPercentageAtOuterEdge;
		currentGlyphids = calculateNumGlyphidsInRadius(radius) - totalNumGlyphids;
		toReturn[numRadiiToTest][2] = currentGlyphids;
		totalNumGlyphids += currentGlyphids;
		
		toReturn[0] = new double[3];
		toReturn[0][0] = radius;
		toReturn[0][2] = totalNumGlyphids;
		
		double avgDmg = 0.0;
		for (int i = 1; i < toReturn.length; i++) {
			// System.out.println(toReturn[i][0] + " " + toReturn[i][1] + " " + toReturn[i][2] + " ");
			avgDmg += toReturn[i][1] * toReturn[i][2];
		}
		toReturn[0][1] = avgDmg / totalNumGlyphids;
		
		return toReturn[0];
	}
	
	/*
		This method will be used to model what the average multiplier of a short-duration conditional effect would be if spread out across 
		every bullet of the magazine. Depending on the duration of the effect, the size of the magazine, and the rate of fire of the gun, 
		the multiplier could only apply to 1-2 shots or upwards of 10 before it wears off.
		
		I'm writing this as "pessimistically" as possible, assuming it takes ceil() num shots to start the effect and that the effect 
		only lasts for floor() number of shots. This may return a very slight underestimation of efficacy, but I think it's a safer route
		than writing it "optimistically" and over-promising.
		
		Engineer/Shotgun/OC/"Stunner" x1.3 damage while enemies are stunned for 3 seconds
		Scout/AssaultRifle/OC/"Bullets of Mercy" x1.33 damage while enemies are stunned for 1.5 seconds
		Scout/AssaultRifle/Mod/5/B "Battle Cool" x12.5 Spread Recovery Speed for 1.5 seconds on kill
	*/
	protected double averageBonusPerMagazineForShortEffects(double conditionalMultiplier, double conditionDuration, boolean onKillEffect, double probabilityPerShot, double magazineSize, double RoF) {
		double numShotsWithoutMultiplier = 0;
		double numShotsWithMultiplier = 0;
		double numShotsFiredDuringEffect = 0;
		double shotsRemaining;
		
		if (onKillEffect) {
			// This section is for effects that happen any time this weapon scores a killing blow, like Scout/AssaultRifle/Mod/5/B/"Battle Cool"
			// Intentionally using incorrect "guessed" spawn rates to get better numbers.
			double burstTTK = EnemyInformation.averageHealthPool(false) / calculateSingleTargetDPS(true, false, false, false);
			double numShotsFiredPerKill = Math.ceil(RoF * burstTTK);
			if (burstTTK < conditionDuration) {
				// Early exit condition: if this weapon can score kills to trigger the On-Kill effect again before the effect duration ends, 
				// it effectively turns into a Long Effect because the Short Effect never ends after first activation.
				return averageBonusPerMagazineForLongEffects(conditionalMultiplier, numShotsFiredPerKill, magazineSize);
			}
			numShotsFiredDuringEffect = Math.floor(RoF * conditionDuration);
			
			double numKillsPerMag = Math.floor(magazineSize / numShotsFiredPerKill);
			if (numKillsPerMag == 0) {
				// Early exit condition: if the weapon can't achieve a kill with its Ideal Burst DPS before the magazine runs out, the On-Kill effect never triggers during the magazine
				numShotsWithoutMultiplier = magazineSize;
				numShotsWithMultiplier = 0;
			}
			else {
				// All the bullets fired from the start of the magazine to score the first kill don't get the multiplier
				numShotsWithoutMultiplier += numShotsFiredPerKill;
				if (numKillsPerMag > 1) {
					numShotsWithMultiplier += (numKillsPerMag - 1) * numShotsFiredDuringEffect;
					numShotsWithoutMultiplier += (numKillsPerMag - 1) * (numShotsFiredPerKill - numShotsFiredDuringEffect);
				}
				// At this point in the sequence, the last kill of the magazine should have happened on the bullet before this one, and there aren't enough bullets left in the mag to kill another enemy
				shotsRemaining = magazineSize % numShotsFiredPerKill;
				double timeToFire = shotsRemaining / RoF;
				if (timeToFire < conditionDuration) {
					numShotsWithMultiplier += shotsRemaining;
				}
				else {
					numShotsWithMultiplier += numShotsFiredDuringEffect;
					numShotsWithoutMultiplier += (shotsRemaining - numShotsFiredDuringEffect);
				}
			}
		}
		else {
			// This section is for effects that have a chance to start on any shot of the magazine, like Engineer/Shotgun/OC/"Stunner"
			double numShotsFiredBeforeProc = Math.ceil(MathUtils.meanRolls(probabilityPerShot));
			if (numShotsFiredBeforeProc >= magazineSize) {
				// Early exit condition: if the mag size is really small or the probability is really low, there's a possibility that the effect never procs during one magazine
				numShotsWithoutMultiplier = magazineSize;
				numShotsWithMultiplier = 0;
			}
			else {
				numShotsFiredDuringEffect = Math.floor(RoF * conditionDuration);
				double numberOfFullCycles = Math.floor(magazineSize / (numShotsFiredBeforeProc + numShotsFiredDuringEffect));
				if (numberOfFullCycles == 0) {
					numShotsWithoutMultiplier += numShotsFiredBeforeProc;
					numShotsWithMultiplier += (magazineSize - numShotsFiredBeforeProc);
				}
				else {
					numShotsWithoutMultiplier += numberOfFullCycles * numShotsFiredBeforeProc;
					numShotsWithMultiplier += numberOfFullCycles * numShotsFiredDuringEffect;
					
					shotsRemaining = magazineSize % (numShotsFiredBeforeProc + numShotsFiredDuringEffect);
					if (shotsRemaining > numShotsFiredBeforeProc) {
						numShotsWithoutMultiplier += numShotsFiredBeforeProc;
						numShotsWithMultiplier += (shotsRemaining - numShotsFiredBeforeProc);
					}
					else {
						numShotsWithoutMultiplier += shotsRemaining;
					}
				}
			}
		}
		
		return (numShotsWithoutMultiplier * 1.0 + numShotsWithMultiplier * conditionalMultiplier) / magazineSize;
	}
	
	/*
		This method will be used to model what the average multiplier of a long-duration conditional effect would be if spread out across
		every bullet of the magazine. Effects of this nature are generally activated partway through the magazine and then stay active until 
		the magazine's end when reload happens.
		
		Engineer/SMG/Mod/4/B "Conductive Bullets" x1.3 damage after enemy is electrocuted by the SMG
		Gunner/Minigun/Mod/4/A "Variable Chamber Pressure" x1.15 damage after max stability
		Gunner/Autocannon/Mod/5/A "Feedback Loop" x1.2 damage after full RoF
	*/
	protected double averageBonusPerMagazineForLongEffects(double conditionalMultiplier, double bulletsBeforeConditionStarts, double magazineSize) {
		return (bulletsBeforeConditionStarts * 1.0 + (magazineSize - bulletsBeforeConditionStarts) * conditionalMultiplier) / magazineSize;
	}
	
	protected int calculateNumGlyphidsInRadius(double radius) {
		/*
			This method should be used any time a projectile fired from this weapon has area-of-effect (AoE) damage in a radius.
			Assumptions made for this method:
				1. All targets hit by this projectile are Glyphid Grunts (regular, Guard, or Slasher) since they're the most common enemy type in swarms.
				2. All targets are standing on a flat surface so that the calculation can be reduced from three dimensions to two
				3. All targets are standing evenly distributed in an efficiently tessellated pattern (like equilateral hexagons) as close together as possible, allowing their legs to overlap but not their bodies
					(possibly an incorrect assumption, since in large swarms in Haz4+ their bodies can overlap when attacking the player)
				4. Hitbox detection of Glyphids' legs is perfect, so that any splash damage their leg would take is correctly applied to the Glyphid's healthbar
				5. Since Glyphids are all walking towards the player, only the rear half of the radius' circle will have Glyphids in it (because the frontline is flat, and then has more Glyphids behind it)
				6. One Glyphid will be hit directly by the projectile, and as such will be modeled as the center of the radius (guarantees a result of at least 1)
				
			Using those assumptions and some estimated measurements of in-game Glyphid models, this method should provide a reasonable estimate of how many Glyphid Grunts you can expect will 
			take damage from a projectile with the given radius of AoE damage.
			
			Quadratic regression approximation: f(x) = 0.942x^2 + 3.81x + 1.02 
			Approximation of the approximation: f(x) = x^2 + 4x + 1
		*/
		
		double glyphidBodyRadius = EnemyInformation.GlyphidGruntBodyRadius;
		double glyphidBodyAndLegsRadius = EnemyInformation.GlyphidGruntBodyAndLegsRadius;
		
		double effectivePackingRadius = glyphidBodyRadius + 0.5 * (glyphidBodyAndLegsRadius - glyphidBodyRadius);
		
		/*
			Using the packing radius and the triangular method of packing circles into 2D space, the Glyphid count can be estimated by using the center of
			each Glyphid as a point, and the constructing as many equilateral triangles as possible with the area using 2*effectivePackingRadius as the edge length.
		*/
		ArrayList<Point2D> glyphidCenters = new ArrayList<Point2D>();
		int glyphidsAcrossDiameter = 3 + 2 * (int) Math.ceil((radius - effectivePackingRadius)/(2.0*effectivePackingRadius));
		double rowHeight = effectivePackingRadius*Math.sqrt(3);
		int rowsOfGlyphids = 1 + (int) Math.ceil(radius / rowHeight);
		double furthestLeftGlyphidOnDiameter = Math.floor(glyphidsAcrossDiameter/2.0) * -2.0 * effectivePackingRadius;
		
		int numGlyphidsThisRow;
		double xOffset, yOffset;
		for (int row = 0; row <= rowsOfGlyphids; row ++) {
			numGlyphidsThisRow = glyphidsAcrossDiameter - row;
			xOffset = furthestLeftGlyphidOnDiameter + row * effectivePackingRadius;
			yOffset = row * rowHeight;
			for (int glyphidCenter = 0; glyphidCenter < numGlyphidsThisRow; glyphidCenter++) {
				glyphidCenters.add(new Point2D(xOffset + glyphidCenter * 2.0 * effectivePackingRadius, -1.0 * yOffset));
				// If the whole circle needs to be modeled:
				/*
				if (row > 0) {
					glyphidCenters.add(new Point2D(xOffset + 2.0*effectivePackingRadius, yOffset));
				}
				*/
			}
		}
		
		// This is the for loop that checks if a circle centered on each point will intersect the AoE circle
		double distanceFromCenterToOrigin;
		int numGlyphidsHitBySplash = 0;
		for (Point2D center: glyphidCenters) {
			distanceFromCenterToOrigin = center.vectorLength();
			// Special case: the Glyphid impacted by the projectile directly will be centered at (0, 0) which doesn't play nice with the math
			if (distanceFromCenterToOrigin == 0.0) {
				numGlyphidsHitBySplash++;
				continue;
			}
			
			// Due to rounding errors from double subtraction, this gets rounded to 2 decimal points
			if (MathUtils.round((distanceFromCenterToOrigin - glyphidBodyAndLegsRadius), 2) < radius) {
				numGlyphidsHitBySplash++;
			}
		}
		
		illustration = new AoEVisualizer(glyphidBodyRadius, glyphidBodyAndLegsRadius, radius, glyphidCenters);
		
		return numGlyphidsHitBySplash;
	}
	
	public abstract boolean currentlyDealsSplashDamage();
	public JPanel visualizeAoERadius() {
		if (currentlyDealsSplashDamage() && illustration != null) {
			return illustration;
		}
		else {
			return new JPanel();
		}
	}
	
	// Used by Flamethrower and Cryo Cannon
	protected int calculateNumGlyphidsInStream(double streamLength) {
		return (int) Math.ceil(streamLength / (2 * EnemyInformation.GlyphidGruntBodyRadius + EnemyInformation.GlyphidGruntBodyAndLegsRadius));
	}
	
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage) {
		return increaseBulletDamageForWeakpoints(preWeakpointBulletDamage, 0.0, EnemyInformation.probabilityBulletWillHitWeakpoint());
	}
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage, double weakpointBonusModifier) {
		return increaseBulletDamageForWeakpoints(preWeakpointBulletDamage, weakpointBonusModifier, EnemyInformation.probabilityBulletWillHitWeakpoint());
	}
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage, double weakpointBonusModifier, double probabilityBulletHitsWeakpoint) {
		/*
			Before weakpoint bonus modifier, weakpoint damage is roughly a 40% increase per bullet.
			As a rule of thumb, the weakpointBonusModifier is roughly a (2/3 * bonus damage) additional increase per bullet. 
			30% bonus modifier => ~20% increase to DPS
		*/
		double estimatedDamageIncreaseWithoutModifier = EnemyInformation.averageWeakpointDamageIncrease();
		return ((1.0 - probabilityBulletHitsWeakpoint) + probabilityBulletHitsWeakpoint * estimatedDamageIncreaseWithoutModifier * (1.0 + weakpointBonusModifier)) * preWeakpointBulletDamage;
	}
	
	/*
		These methods feed into the output field at the bottom-left of the WeaponTab in the GUI
	*/
	
	// Single-target calculations
	public double calculateSingleTargetDPS(boolean burst) {
		return calculateSingleTargetDPS(burst, enableWeakpointsDPS, enableGeneralAccuracyDPS, enableArmorWastingDPS);
	}
	public abstract double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting);
	
	// Multi-target calculations (based on "ideal" sustained DPS calculations)
	// I'm choosing not to implement Status Effects on the additional targets
	public abstract double calculateAdditionalTargetDPS();
	public abstract double calculateMaxMultiTargetDamage();
	
	// Non-damage calculations
	public abstract int calculateMaxNumTargets();
	
	protected double numMagazines(int carriedAmmo, int magazineSize) {
		// Don't forget to add the magazine that you start out with, in addition to the carried ammo
		return (((double) carriedAmmo) / ((double) magazineSize)) + 1.0;
	}
	protected int numReloads(int carriedAmmo, int magazineSize) {
		if (carriedAmmo % magazineSize == 0) {
			return (carriedAmmo / magazineSize) - 1;
		}
		else {
			return (int) Math.floorDiv(carriedAmmo, magazineSize);
		}
	}
	
	public abstract double calculateFiringDuration();
	public double averageTimeToKill() {
		return averageTimeToKill(true);
	}
	public double averageTimeToKill(boolean useExactSpawnRates) {
		return EnemyInformation.averageHealthPool(useExactSpawnRates) / calculateSingleTargetDPS(false, true, false, false);
	}
	protected abstract double averageDamageToKillEnemy();
	public abstract double averageOverkill();
	public double ammoEfficiency() {
		return calculateMaxMultiTargetDamage() / averageDamageToKillEnemy();
	}
	public abstract double estimatedAccuracy(boolean weakpointAccuracy); // -1 means manual or N/A; [0.0, 100.0] otherwise
	public abstract int breakpoints();
	
	// This method is used to explain what the individual numbers of the Breakpoints
	public StatsRow[] breakpointsExplanation() {
		StatsRow[] toReturn = new StatsRow[breakpoints.length];
		
		toReturn[0] = new StatsRow("Glypid Swarmer:", breakpoints[0], null, false);
		toReturn[1] = new StatsRow("Glypid Grunt (Light Armor):", breakpoints[1], null, false);
		toReturn[2] = new StatsRow("Glypid Grunt (Weakpoint):", breakpoints[2], null, false);
		toReturn[3] = new StatsRow("Glypid Grunt Guard (Light Armor):", breakpoints[3], null, false);
		toReturn[4] = new StatsRow("Glypid Grunt Guard (Weakpoint):", breakpoints[4], null, false);
		toReturn[5] = new StatsRow("Glypid Grunt Slasher (Light Armor):", breakpoints[5], null, false);
		toReturn[6] = new StatsRow("Glypid Grunt Slasher (Weakpoint):", breakpoints[6], null, false);
		toReturn[7] = new StatsRow("Glypid Warden:", breakpoints[7], null, false);
		toReturn[8] = new StatsRow("Glypid Warden (Orb):", breakpoints[8], null, false);
		toReturn[9] = new StatsRow("Glypid Praetorian (Mouth):", breakpoints[9], null, false);
		toReturn[10] = new StatsRow("Glypid Praetorian (Weakpoint):", breakpoints[10], null, false);
		toReturn[11] = new StatsRow("Glypid Oppressor (Weakpoint):", breakpoints[11], null, false);
		toReturn[12] = new StatsRow("Glypid Acid Spitter (Light Armor):", breakpoints[12], null, false);
		toReturn[13] = new StatsRow("Glypid Acid Spitter (Weakpoint):", breakpoints[13], null, false);
		toReturn[14] = new StatsRow("Glypid Web Spitter (Light Armor):", breakpoints[14], null, false);
		toReturn[15] = new StatsRow("Glypid Web Spitter (Weakpoint):", breakpoints[15], null, false);
		toReturn[16] = new StatsRow("Glypid Menace:", breakpoints[16], null, false);
		toReturn[17] = new StatsRow("Glypid Menace (Weakpoint):", breakpoints[17], null, false);
		toReturn[18] = new StatsRow("Glypid Exploder:", breakpoints[18], null, false);
		toReturn[19] = new StatsRow("Glypid Exploder (Weakpoint):", breakpoints[19], null, false);
		toReturn[20] = new StatsRow("Mactera Spawn:", breakpoints[20], null, false);
		toReturn[21] = new StatsRow("Mactera Spawn (Weakpoint):", breakpoints[21], null, false);
		toReturn[22] = new StatsRow("Mactera Brundle:", breakpoints[22], null, false);
		toReturn[23] = new StatsRow("Mactera Brundle (Weakpoint):", breakpoints[23], null, false);
		toReturn[24] = new StatsRow("Mactera Tri-Jaw:", breakpoints[24], null, false);
		toReturn[25] = new StatsRow("Mactera Tri-Jaw (Weakpoint):", breakpoints[25], null, false);
		toReturn[26] = new StatsRow("Mactera Goo Bomber:", breakpoints[26], null, false);
		toReturn[27] = new StatsRow("Mactera Goo Bomber (Weakpoint):    ", breakpoints[27], null, false);  // Added spaces at the end to create some whitespace in the JPanel
		toReturn[28] = new StatsRow("Mactera Grabber:", breakpoints[28], null, false);
		toReturn[29] = new StatsRow("Mactera Grabber (Weakpoint):", breakpoints[29], null, false);
		toReturn[30] = new StatsRow("Cave Leech:", breakpoints[30], null, false);
		
		return toReturn;
	}
	
	public abstract double utilityScore();
	
	// This method is used to explain how the Utility Scores are calculated for the UtilityBreakdownButton
	public StatsRow[] utilityExplanation() {
		StatsRow[] toReturn = new StatsRow[utilityScores.length];
		
		toReturn[0] = new StatsRow("Mobility:", utilityScores[0], modIcons.movespeed, false);
		toReturn[1] = new StatsRow("Damage Resist:", utilityScores[1], modIcons.damageResistance, false);
		toReturn[2] = new StatsRow("Armor Break:", utilityScores[2], modIcons.armorBreaking, false);
		toReturn[3] = new StatsRow("Slow:", utilityScores[3], modIcons.slowdown, false);
		toReturn[4] = new StatsRow("Fear:", utilityScores[4], modIcons.fear, false);
		toReturn[5] = new StatsRow("Stun:", utilityScores[5], modIcons.stun, false);
		toReturn[6] = new StatsRow("Freeze:", utilityScores[6], modIcons.coldDamage, false);
		
		return toReturn;
	}
	
	/*
		This metric will show the average time to ignite or freeze if the weapon can deal Temperature Damage. Non-negative numbers are Avg Time to Ignite or Freeze, 
		and negative means that the weapon currently doesn't do Temperature Damage.
		
		The name "cauterize" was the only term I was able to find that encompassed both freezing and burning, but it honestly has no relation to what this metric does.
	*/
	public abstract double averageTimeToCauterize();
	
	// These two methods will be added as columns to the MySQL metrics dump, but I have no plans to add them to the 15 metrics in the bottom panel.
	public abstract double damagePerMagazine();
	public abstract double timeToFireMagazine();
	
	public abstract double damageWastedByArmor();
	
	public StatsRow[] armorWastingExplanation() {
		StatsRow[] toReturn = new StatsRow[damageWastedByArmorPerCreature[1].length];
		
		toReturn[0] = new StatsRow("Glyphid Grunt:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][0], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[1] = new StatsRow("Glyphid Guard:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][1], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[2] = new StatsRow("Glyphid Slasher:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][2], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[3] = new StatsRow("Glyphid Warden:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][3], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[4] = new StatsRow("Glyphid Praetorian:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][4], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[5] = new StatsRow("Glyphid Acid Spitter:      ", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][5], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[6] = new StatsRow("Glyphid Web Spitter:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][6], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[7] = new StatsRow("Glyphid Menace:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][7], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[8] = new StatsRow("Mactera Brundle:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][8], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[9] = new StatsRow("Q'ronar Shellback:", MathUtils.round(100.0 * damageWastedByArmorPerCreature[1][9], GuiConstants.numDecimalPlaces) + "%", null, false);
		
		return toReturn;
	}
	
	public StatsRow[] overkillExplanation() {
		StatsRow[] toReturn = new StatsRow[overkillPercentages[1].length];
		
		toReturn[0] = new StatsRow("Glypid Swarmer:", MathUtils.round(overkillPercentages[1][0], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[1] = new StatsRow("Glypid Grunt:", MathUtils.round(overkillPercentages[1][1], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[2] = new StatsRow("Glypid Grunt Guard:", MathUtils.round(overkillPercentages[1][2], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[3] = new StatsRow("Glypid Grunt Slasher:", MathUtils.round(overkillPercentages[1][3], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[4] = new StatsRow("Glypid Warden:", MathUtils.round(overkillPercentages[1][4], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[5] = new StatsRow("Glypid Praetorian:", MathUtils.round(overkillPercentages[1][5], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[6] = new StatsRow("Glypid Oppressor:    ", MathUtils.round(overkillPercentages[1][6], GuiConstants.numDecimalPlaces) + "%", null, false);  // Added spaces at the end to create some whitespace in the JPanel
		toReturn[7] = new StatsRow("Glypid Acid Spitter:", MathUtils.round(overkillPercentages[1][7], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[8] = new StatsRow("Glypid Web Spitter:", MathUtils.round(overkillPercentages[1][8], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[9] = new StatsRow("Glypid Menace:", MathUtils.round(overkillPercentages[1][9], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[10] = new StatsRow("Glypid Exploder:", MathUtils.round(overkillPercentages[1][10], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[11] = new StatsRow("Glypid Bulk Detonator:", MathUtils.round(overkillPercentages[1][11], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[12] = new StatsRow("Glyphid Brood Nexus:", MathUtils.round(overkillPercentages[1][12], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[13] = new StatsRow("Mactera Spawn:", MathUtils.round(overkillPercentages[1][13], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[14] = new StatsRow("Mactera Brundle:", MathUtils.round(overkillPercentages[1][14], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[15] = new StatsRow("Mactera Tri-Jaw:", MathUtils.round(overkillPercentages[1][15], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[16] = new StatsRow("Mactera Goo Bomber:", MathUtils.round(overkillPercentages[1][16], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[17] = new StatsRow("Mactera Grabber:", MathUtils.round(overkillPercentages[1][17], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[18] = new StatsRow("Naedocyte Breeder:", MathUtils.round(overkillPercentages[1][18], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[19] = new StatsRow("Q'ronar Shellback:", MathUtils.round(overkillPercentages[1][19], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[20] = new StatsRow("Spitball Infector:", MathUtils.round(overkillPercentages[1][20], GuiConstants.numDecimalPlaces) + "%", null, false);
		toReturn[21] = new StatsRow("Cave Leech:", MathUtils.round(overkillPercentages[1][21], GuiConstants.numDecimalPlaces) + "%", null, false);
		
		return toReturn;
	}
	
	public abstract ArrayList<String> exportModsToMySQL(boolean exportAllMods);
	public abstract ArrayList<String> exportOCsToMySQL(boolean exportAllOCs);
}
