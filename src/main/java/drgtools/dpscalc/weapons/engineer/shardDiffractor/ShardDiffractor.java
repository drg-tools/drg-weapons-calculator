package drgtools.dpscalc.weapons.engineer.shardDiffractor;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.weapons.Weapon;

public class ShardDiffractor extends Weapon {

  /****************************************************************************************
   * Class Variables
   ****************************************************************************************/

  private double directDamage;
  private double directHeating;
  private double areaDamage;
  private double effectRadius;
  private int totalCapacity;
  private double chargeCapacity;
  private int dischargeRate;
  private double rechargeTime;

  /****************************************************************************************
   * Constructors
   ****************************************************************************************/

  // Shortcut constructor to get baseline data
  public ShardDiffractor() {
    this(-1, -1, -1, -1, -1, -1);
  }

  // Shortcut constructor to quickly get statistics about a specific build
  public ShardDiffractor(String combination) {
    this(-1, -1, -1, -1, -1, -1);
    buildFromCombination(combination);
  }

  public ShardDiffractor(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
    fullName = "Shard Diffractor";
    weaponPic = WeaponPictures.minigun;

    // Base stats, before mods or overclocks alter them:
    directDamage = 7;
    directHeating = 0.25;
    areaDamage = 7;
    effectRadius = 1.3;
    totalCapacity = 300;
    chargeCapacity = 50;
    dischargeRate = 13;
    rechargeTime = 1.6;

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
    tier1[0] = new Mod("Impact Splash", "+2 Area Damage", ButtonIcons.modIcons.areaDamage, 1, 0);
    tier1[1] = new Mod("Increased Energy Density", "+2 Direct Damage", ButtonIcons.modIcons.directDamage, 1, 1);
    tier1[2] = new Mod("Larger Battery", "+100 Total Capacity", ButtonIcons.modIcons.carriedAmmo, 1, 2);

    tier2 = new Mod[2];
    tier2[0] = new Mod("Soft Tissue Disruption", "+33% Weakpoint Bonus", ButtonIcons.modIcons.weakpointBonus, 2, 0);
    tier2[1] = new Mod("Particle Spattering", "+0.8m AoE Radius", ButtonIcons.modIcons.aoeRadius, 2, 1);

    tier3 = new Mod[2];
    tier3[0] = new Mod("Aluminum Foil DIY", "+50 Charge Capacity", ButtonIcons.modIcons.carriedAmmo, 3, 0);
    tier3[1] = new Mod("Open Structure Battery", "-0.8 Recharge Time", ButtonIcons.modIcons.chargeSpeed, 3, 1);

    tier4 = new Mod[2];
    tier4[0] = new Mod("High-Intensity Heating", "Increases the amount of Direct Damage added as Heat from 25% to 100%", ButtonIcons.modIcons.heatDamage, 4, 0);
    tier4[1] = new Mod("Nitrogen Vaporizer", "+400% Armor Breaking", ButtonIcons.modIcons.armorBreaking, 4, 1);

    tier5 = new Mod[3];
    tier5[0] = new Mod("Hydrogen Rupturing", "When damaging an enemy that is either Electrocuted or affected by an IFG, " +
            "add an additional +33% Direct and Area Damage as Electric-element.", ButtonIcons.modIcons.electricity, 5, 0);
    tier5[1] = new Mod("Bio-Mass Converter", "Killing a medium-or-larger enemy refunds 3 ammo to the current Charge Capacity, prolonging the firing duration. This can only happen once every 0.25 seconds.", ButtonIcons.modIcons.special, 5, 1);
    tier5[2] = new Mod("Dazzler Module", "All enemies damaged by the AoE have their movespeed slowed by 80% for 1 second.", ButtonIcons.modIcons.slowdown, 5, 2);

    overclocks = new Overclock[6];
    overclocks[0] = new Overclock(Overclock.classification.clean, "Efficiency Tweaks", "+50 Total Capacity, +25 Charge Capacity", ButtonIcons.overclockIcons.carriedAmmo, 0);
    overclocks[1] = new Overclock(Overclock.classification.balanced, "Automated Beam Controller", "+100 Total Capacity, +4 Discharge Rate, and -0.2 Recharge Time. " +
            "In exchange: x0.8 Charge Capacity and the beam continues firing until the Charge Capacity is empty. Manually stopping the beam wastes 50% of the total Charge Capacity.", ButtonIcons.overclockIcons.rateOfFire, 1);
    overclocks[2] = new Overclock(Overclock.classification.balanced, "Feedback Loop", "For every full second that the beam fires continuously, the Area Damage increases by " +
            "+1 and the AoE Radius increases by +0.3m. In exchange, -100 Total Capacity.", ButtonIcons.overclockIcons.aoeRadius, 2);
    overclocks[3] = new Overclock(Overclock.classification.balanced, "Volatile Impact Reactor", "Terrain impacted by the beam is temporarily transformed into magma for 7 seconds, which slows enemies' " +
            "movespeed by 30% and does 40 Fire Damage and Heat per Second while they're in contact with it. In exchange, x0.5 AoE Radius and x0.5 Charge Capacity.", ButtonIcons.overclockIcons.special, 3, false);
    overclocks[4] = new Overclock(Overclock.classification.unstable, "Plastcrete Catalyst", "Shooting the beam at a platform increases Area Damage and AoE Radius by x1.5. If you shoot the platform continuously for 1 second (13 ammo), " +
            "it will explode dealing 150 Disintegrate-element + 150 Explosive-element Area Damage in a 5m radius, applying 100% Base Fear Chance to all enemies it damages, and carving a 3m diameter sphere. Triggering that " +
            "explosion will force the weapon to recharge. In exchange, -50 Total Capacity and +0.8 Recharge Time.", ButtonIcons.overclockIcons.addedExplosion, 4, false);
    overclocks[5] = new Overclock(Overclock.classification.unstable, "Overdrive Booster", "Press Reload to start Boosting. While Boosting: each shot costs 2 ammo but deals 2.5x Direct and Area Damage, you cannot move, " +
            "and the beam continues firing until the Charge Capacity is empty. Manually stopping the beam wastes 50% of the total Charge Capacity. After the Boost finishes, " +
            "the Recharge Time takes an extra 1.5 seconds.", ButtonIcons.overclockIcons.directDamage, 5);

    // This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
    modsAndOCsInitialized = true;
  }

  @Override
  public ShardDiffractor clone() {
    return new ShardDiffractor(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
  }

  @Override
  public String getDwarfClass() {
    return "Engineer";
  }

  @Override
  public String getSimpleName() {
    return "ShardDiffractor";
  }

  @Override
  public int getDwarfClassID() {
    return DatabaseConstants.engineerCharacterID;
  }

  @Override
  public int getWeaponID() {
    return DatabaseConstants.shardDiffractorID;
  }

  /****************************************************************************************
   * Setters and Getters
   ****************************************************************************************/

  private double getDirectDamage() {
    if (selectedTier1 == 1) {
      return directDamage + 2;
    }

    return directDamage;
  }

  private double getDirectHeating() {
    if (selectedTier4 == 0) {
      return 1.0;
    }

    return directHeating;
  }

  private double getAreaDamage() {
    if (selectedTier1 == 0) {
      return areaDamage + 2;
    }

    return areaDamage;
  }

  private double getEffectRadius() {
    double toReturn = effectRadius;

    if (selectedTier2 == 1) {
      toReturn += 0.8;
    }

    if (selectedOverclock == 3) {
      toReturn *= 0.5;
    }

    return toReturn;
  }

  private int getTotalCapacity() {
    int toReturn = totalCapacity;

    if (selectedTier1 == 2) {
      toReturn += 100;
    }

    if (selectedOverclock == 0) {
      toReturn += 50;
    } else if (selectedOverclock == 1) {
      toReturn += 100;
    } else if (selectedOverclock == 2) {
      toReturn -= 100;
    } else if (selectedOverclock == 4) {
      toReturn -= 50;
    }

    return toReturn;
  }

  private double getChargeCapacity() {
    double toReturn = chargeCapacity;

    if (selectedTier3 == 0) {
      toReturn += 50;
    }

    if (selectedOverclock == 0) {
      toReturn += 25;
    } else if (selectedOverclock == 1) {
      toReturn *= 0.8;
    } else if (selectedOverclock == 3) {
      toReturn *= 0.5;
    }

    return toReturn;
  }

  private int getDischargeRate() {
    if (selectedOverclock == 1) {
      return dischargeRate + 4;
    }

    return dischargeRate;
  }

  private double getRechargeTime() {
    double toReturn = rechargeTime;

    if (selectedTier3 == 1) {
      toReturn -= 0.8;
    }

    if (selectedOverclock == 1) {
      toReturn -= 0.2;
    } else if (selectedOverclock == 4) {
      toReturn += 0.8;
    }

    return toReturn;
  }

  private double getWeakpointBonus() {
    if (selectedTier2 == 0) {
      return 0.33;
    }

    return 0;
  }

  @Override
  public StatsRow[] getStats() {
    StatsRow[] toReturn = new StatsRow[9];

    toReturn[0] = new StatsRow("Direct Damage:", getDirectDamage(), ButtonIcons.modIcons.directDamage, selectedTier1 == 1);

    toReturn[1] = new StatsRow("Direct Heating:", convertDoubleToPercentage(getDirectHeating()), ButtonIcons.modIcons.heatDamage, selectedTier4 == 0);

    toReturn[2] = new StatsRow("Area Damage:", getAreaDamage(), ButtonIcons.modIcons.areaDamage, selectedTier1 == 0);

    toReturn[3] = new StatsRow("Effect Radius:", getEffectRadius(), ButtonIcons.modIcons.aoeRadius, selectedTier2 == 1 | selectedOverclock == 3);

    boolean totalCapacityModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 4;
    toReturn[4] = new StatsRow("Total Capacity:", getTotalCapacity(), ButtonIcons.modIcons.carriedAmmo, totalCapacityModified);

    boolean chargeCapacityModified = selectedTier3 == 0 || selectedOverclock == 0 || selectedOverclock == 1 || selectedOverclock == 3;
    toReturn[5] = new StatsRow("Charge Capacity:", getChargeCapacity(), ButtonIcons.modIcons.magSize, chargeCapacityModified);

    toReturn[6] = new StatsRow("Discharge Rate:", getDischargeRate(), ButtonIcons.modIcons.rateOfFire, selectedOverclock == 1);

    boolean rechargeModified = selectedTier3 == 1 || selectedOverclock == 1 || selectedOverclock == 4;
    toReturn[7] = new StatsRow("Recharge Time:", getRechargeTime(), ButtonIcons.modIcons.chargeSpeed, rechargeModified);

    toReturn[8] = new StatsRow("Weakpoint Bonus:", convertDoubleToPercentage(getWeakpointBonus()), ButtonIcons.modIcons.weakpointBonus, selectedTier2 == 0, selectedTier2 == 0);

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
  public double calculateSingleTargetDPS(boolean burst, boolean weakpoint, boolean accuracy, boolean armorWasting) {
    return 0;
  }

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
  public double averageOverkill() {
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
  public double averageTimeToCauterize() {
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
  public double damageWastedByArmor() {
    return 0;
  }
}
