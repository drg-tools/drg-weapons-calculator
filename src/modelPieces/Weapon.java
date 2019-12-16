package modelPieces;

import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JPanel;

import guiPieces.AoEVisualizer;
import utilities.Point2D;

public abstract class Weapon extends Observable {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	protected String fullName;
	
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
	
	protected double[] baselineCalculatedStats;
	private AoEVisualizer illustration = null;
	
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
	public void setSelectedModAtTier(int tierNumber, int newSelection) {
		if (tierNumber > 0 && tierNumber < 6) {
			switch (tierNumber) {
				case 1: {
					if (newSelection > -2 && newSelection < tier1.length) {
						if (newSelection == selectedTier1) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier1 = -1;
						}
						else {
							selectedTier1 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 2: {
					if (newSelection > -2 && newSelection < tier2.length) {
						if (newSelection == selectedTier2) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier2 = -1;
						}
						else {
							selectedTier2 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 3: {
					if (newSelection > -2 && newSelection < tier3.length) {
						if (newSelection == selectedTier3) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier3 = -1;
						}
						else {
							selectedTier3 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 4: {
					if (newSelection > -2 && newSelection < tier4.length) {
						if (newSelection == selectedTier4) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier4 = -1;
						}
						else {
							selectedTier4 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
				case 5: {
					if (newSelection > -2 && newSelection < tier5.length) {
						if (newSelection == selectedTier5) {
							// If the same mod is selected, that indicates that it's being unequipped. Set tier = -1 to affect the math properly.
							selectedTier5 = -1;
						}
						else {
							selectedTier5 = newSelection;
						}
					}
					else {
						System.out.println("Mod choice is outside array bounds");
					}
					break;
				}
			}
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
	public void setSelectedOverclock(int newSelection) {
		if (newSelection > -2 && newSelection < overclocks.length) {
			if (newSelection == selectedOverclock) {
				// If the same overclock is selected, that indicates that it's being unequipped. Set overclock = -1 to affect the math properly.
				selectedOverclock = -1;
			}
			else {
				selectedOverclock = newSelection;
			}
			if (countObservers() > 0) {
				setChanged();
				notifyObservers();
			}
		}
		else {
			System.out.println("Overclock choice is outside array bounds");
		}
	}
	
	public abstract void buildFromCombination(String combination); 
	
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
	
	protected abstract void initializeModsAndOverclocks();
	
	protected void setBaselineStats() {
		int oldT1 = selectedTier1, oldT2 = selectedTier2, oldT3 = selectedTier3, oldT4 = selectedTier4, oldT5 = selectedTier5, oldOC = selectedOverclock;
		selectedTier1 = selectedTier2 = selectedTier3 = selectedTier4 = selectedTier5 = selectedOverclock = -1;
		baselineCalculatedStats = new double[] {
			calculateBurstDPS(),
			calculateSustainedDPS(),
			calculateAdditionalTargetDPS(),
			calculateMaxMultiTargetDamage(),
			(double) calculateMaxNumTargets(),
			calculateFiringDuration()
		};
		selectedTier1 = oldT1;
		selectedTier2 = oldT2;
		selectedTier3 = oldT3;
		selectedTier4 = oldT4;
		selectedTier5 = oldT5;
		selectedOverclock = oldOC;
	}
	public double[] getBaselineStats() {
		return baselineCalculatedStats;
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// Stats page
	public String getFullName() {
		return fullName;
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
	public abstract StatsRow[] getStats();
	public abstract Weapon clone();
	
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
		*/
		
		// Measured using meters
		double glyphidBodyRadius = 0.4;
		double glyphidBodyAndLegsRadius = 0.9;
		
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
			
			if ((distanceFromCenterToOrigin - glyphidBodyAndLegsRadius) < radius) {
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
	
	private double estimatedPercentageOfWeakpointHits() {
		/*
			This is a utility method that makes an educated guess at what % of shots fired at various enemies will hit their weakpoints,
			and then weighs that % by the odds of that enemy being encountered. Sum up all the weighted percentages, and in theory it 
			should be a halfway-decent estimate of what % of shots fired will hit a weakpoint. That estimate can then be used to 
			estimate the "real" DPS of the guns since it can account for weakpoint bonus damage mods and OCs.
		*/
		
		// Section 1: estimates of what % of all enemies fought is each type
		double glyphidSwarmerRate = 0.17;
		double glyphidGruntRate = 0.24;
		double glyphidGruntGuardRate = 0.08;
		double glyphidGruntSlasherRate = 0.08;
		double glyphidPraetorianRate = 0.05;
		double glyphidExploderRate = 0.08;
		double glyphidBulkDetonatorRate = 0.01;
		double glyphidWebspitterRate = 0.04;
		double glyphidAcidspitterRate = 0.02;
		double glyphidMenaceRate = 0.02;
		double glyphidWardenRate = 0.02;
		double qronarShellbackRate = 0.01;
		double macteraSpawnRate = 0.08;
		double macteraGrabberRate = 0.01;
		double macteraBomberRate = 0.03;
		double naedocyteBreederRate = 0.02;
		double glyphidBroodNexusRate = 0.02;
		double spitballInfectorRate = 0.01;
		double caveLeechRate = 0.01;
		
		// Section 2: enemy weakpoint hit percentage estimates
		double glyphidSwarmerWeakpointPercentage = 0.0;  // No weakpoint
		double glyphidGruntWeakpointPercentage = 0.9;
		double glyphidGruntGuardWeakpointPercentage = 0.5;
		double glyphidGruntSlasherWeakpointPercentage = 0.9;
		double glyphidPraetorianWeakpointPercentage = 0.2;
		double glyphidExploderWeakpointPercentage = 0.1;
		double glyphidBulkDetonatorWeakpointPercentage = 0.2;
		double glyphidWebspitterWeakpointPercentage = 0.1;
		double glyphidAcidspitterWeakpointPercentage = 0.4;
		double glyphidMenaceWeakpointPercentage = 0.7;
		double glyphidWardenWeakpointPercentage = 0.5;
		double qronarShellbackWeakpointPercentage = 0.1;
		double macteraSpawnWeakpointPercentage = 0.8;
		double macteraGrabberWeakpointPercentage = 0.2;
		double macteraBomberWeakpointPercentage = 0.9;
		double naedocyteBreederWeakpointPercentage = 0.1;
		double glyphidBroodNexusWeakpointPercentage = 0.9;
		double spitballInfectorWeakpointPercentage = 0.4;
		double caveLeechWeakpointPercentage = 0.0;  // No weakpoint
		
		// Section 3: weighted sum of those values
		double weightedSum = 0.0;
		weightedSum += glyphidSwarmerRate * glyphidSwarmerWeakpointPercentage;
		weightedSum += glyphidGruntRate * glyphidGruntWeakpointPercentage;
		weightedSum += glyphidGruntGuardRate * glyphidGruntGuardWeakpointPercentage;
		weightedSum += glyphidGruntSlasherRate * glyphidGruntSlasherWeakpointPercentage;
		weightedSum += glyphidPraetorianRate * glyphidPraetorianWeakpointPercentage;
		weightedSum += glyphidExploderRate * glyphidExploderWeakpointPercentage;
		weightedSum += glyphidBulkDetonatorRate * glyphidBulkDetonatorWeakpointPercentage;
		weightedSum += glyphidWebspitterRate * glyphidWebspitterWeakpointPercentage;
		weightedSum += glyphidAcidspitterRate * glyphidAcidspitterWeakpointPercentage;
		weightedSum += glyphidMenaceRate * glyphidMenaceWeakpointPercentage;
		weightedSum += glyphidWardenRate * glyphidWardenWeakpointPercentage;
		weightedSum += qronarShellbackRate * qronarShellbackWeakpointPercentage;
		weightedSum += macteraSpawnRate * macteraSpawnWeakpointPercentage;
		weightedSum += macteraGrabberRate * macteraGrabberWeakpointPercentage;
		weightedSum += macteraBomberRate * macteraBomberWeakpointPercentage;
		weightedSum += naedocyteBreederRate * naedocyteBreederWeakpointPercentage;
		weightedSum += glyphidBroodNexusRate * glyphidBroodNexusWeakpointPercentage;
		weightedSum += spitballInfectorRate * spitballInfectorWeakpointPercentage;
		weightedSum += caveLeechRate * caveLeechWeakpointPercentage;
		
		System.out.println("Estimated percentage of bullets fired that will hit a weakpoint: " + weightedSum);
		return weightedSum;
	}
	private double estimatedWeakpointDamageIncrease() {
		/*
			This method uses the same spawn rate stats as above, but instead returns a damage multiplier for all bullets that can do weakpoint damage.
		*/
		
		// Section 1: estimates of what % of all enemies fought is each type
		double glyphidSwarmerRate = 0.17;
		double glyphidGruntRate = 0.24;
		double glyphidGruntGuardRate = 0.08;
		double glyphidGruntSlasherRate = 0.08;
		double glyphidPraetorianRate = 0.05;
		double glyphidExploderRate = 0.08;
		double glyphidBulkDetonatorRate = 0.01;
		double glyphidWebspitterRate = 0.04;
		double glyphidAcidspitterRate = 0.02;
		double glyphidMenaceRate = 0.02;
		double glyphidWardenRate = 0.02;
		double qronarShellbackRate = 0.01;
		double macteraSpawnRate = 0.08;
		double macteraGrabberRate = 0.01;
		double macteraBomberRate = 0.03;
		double naedocyteBreederRate = 0.02;
		double glyphidBroodNexusRate = 0.02;
		double spitballInfectorRate = 0.01;
		double caveLeechRate = 0.01;
		
		// Section 2: enemy weakpoint hit percentage estimates
		double glyphidSwarmerWeakpointPercentage = 0.0;  // No weakpoint
		double glyphidGruntWeakpointPercentage = 2.0;
		double glyphidGruntGuardWeakpointPercentage = 2.0;
		double glyphidGruntSlasherWeakpointPercentage = 2.0;
		double glyphidPraetorianWeakpointPercentage = 1.0; // It has a weakpoint, but it only takes normal damage.
		double glyphidExploderWeakpointPercentage = 2.0;
		double glyphidBulkDetonatorWeakpointPercentage = 3.0;
		double glyphidWebspitterWeakpointPercentage = 2.0;
		double glyphidAcidspitterWeakpointPercentage = 2.0;
		double glyphidMenaceWeakpointPercentage = 2.0;
		double glyphidWardenWeakpointPercentage = 3.0;
		double qronarShellbackWeakpointPercentage = 2.0;
		double macteraSpawnWeakpointPercentage = 3.0;
		double macteraGrabberWeakpointPercentage = 3.0;
		double macteraBomberWeakpointPercentage = 3.0;
		double naedocyteBreederWeakpointPercentage = 3.0;
		double glyphidBroodNexusWeakpointPercentage = 2.0;
		double spitballInfectorWeakpointPercentage = 2.0;
		double caveLeechWeakpointPercentage = 0.0;  // No weakpoint
		
		// Section 3: weighted sum of those values
		double weightedSum = 0.0;
		weightedSum += glyphidSwarmerRate * glyphidSwarmerWeakpointPercentage;
		weightedSum += glyphidGruntRate * glyphidGruntWeakpointPercentage;
		weightedSum += glyphidGruntGuardRate * glyphidGruntGuardWeakpointPercentage;
		weightedSum += glyphidGruntSlasherRate * glyphidGruntSlasherWeakpointPercentage;
		weightedSum += glyphidPraetorianRate * glyphidPraetorianWeakpointPercentage;
		weightedSum += glyphidExploderRate * glyphidExploderWeakpointPercentage;
		weightedSum += glyphidBulkDetonatorRate * glyphidBulkDetonatorWeakpointPercentage;
		weightedSum += glyphidWebspitterRate * glyphidWebspitterWeakpointPercentage;
		weightedSum += glyphidAcidspitterRate * glyphidAcidspitterWeakpointPercentage;
		weightedSum += glyphidMenaceRate * glyphidMenaceWeakpointPercentage;
		weightedSum += glyphidWardenRate * glyphidWardenWeakpointPercentage;
		weightedSum += qronarShellbackRate * qronarShellbackWeakpointPercentage;
		weightedSum += macteraSpawnRate * macteraSpawnWeakpointPercentage;
		weightedSum += macteraGrabberRate * macteraGrabberWeakpointPercentage;
		weightedSum += macteraBomberRate * macteraBomberWeakpointPercentage;
		weightedSum += naedocyteBreederRate * naedocyteBreederWeakpointPercentage;
		weightedSum += glyphidBroodNexusRate * glyphidBroodNexusWeakpointPercentage;
		weightedSum += spitballInfectorRate * spitballInfectorWeakpointPercentage;
		weightedSum += caveLeechRate * caveLeechWeakpointPercentage;
		
		System.out.println("Estimated damage multiplier from hitting a weakpoint: " + weightedSum);
		return weightedSum;
	}
	protected double increaseBulletDamageForWeakpoints(double preWeakpointBulletDamage, double weakpointBonusModifier) {
		// As a rule of thumb, the weakpointBonusModifier is roughly a (2/3 * bonus damage) increase per bullet. 30% bonus modifier => 20% dmg increase to DPS
		double probabilityBulletHitsWeakpoint = estimatedPercentageOfWeakpointHits();
		double estimatedDamageIncreaseWithoutModifier = estimatedWeakpointDamageIncrease();
		
		return ((1.0 - probabilityBulletHitsWeakpoint) + probabilityBulletHitsWeakpoint * estimatedDamageIncreaseWithoutModifier * (1.0 + weakpointBonusModifier)) * preWeakpointBulletDamage;
	}
	
	// Single-target calculations
	public abstract double calculateBurstDPS();
	public abstract double calculateSustainedDPS();
	
	// Multi-target calculations
	public abstract double calculateAdditionalTargetDPS();
	public abstract double calculateMaxMultiTargetDamage();
	
	// Non-damage calculations
	public abstract int calculateMaxNumTargets();
	public abstract double calculateFiringDuration();
}
