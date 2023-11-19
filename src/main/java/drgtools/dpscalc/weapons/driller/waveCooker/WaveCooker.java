package drgtools.dpscalc.weapons.driller.waveCooker;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.weapons.Weapon;

public class WaveCooker extends Weapon {

  /****************************************************************************************
   * Class Variables
   ****************************************************************************************/

  private double directDamage;
  private double shotWidth;
  private int magazineSize;
  private double rateOfFire;
  private double heatGeneration;
  private double coolingRate;
  private double overheatDuration;

  /****************************************************************************************
   * Constructors
   ****************************************************************************************/

  // Shortcut constructor to get baseline data
  public WaveCooker() {
    this(-1, -1, -1, -1, -1, -1);
  }

  // Shortcut constructor to quickly get statistics about a specific build
  public WaveCooker(String combination) {
    this(-1, -1, -1, -1, -1, -1);
    buildFromCombination(combination);
  }

  public WaveCooker(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
    fullName = "Colette Wave Cooker";
    weaponPic = WeaponPictures.flamethrower;

    // Base stats, before mods or overclocks alter them:
    directDamage = 7;
    shotWidth = 120;
    magazineSize = 300;
    rateOfFire = 7;
    heatGeneration = 1;
    coolingRate = 1;
    overheatDuration = 2;


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
    tier1[0] = new Mod("Convex Lens", "+2 Damage", ButtonIcons.modIcons.directDamage, 1, 0);
    tier1[1] = new Mod("Magnetron Tube", "+100 Magazine Size", ButtonIcons.modIcons.carriedAmmo, 1, 1);
    tier1[2] = new Mod("Concave Lens", "x3 Shot Width", ButtonIcons.modIcons.aoeRadius, 1, 2);

    tier2 = new Mod[3];
    tier2[0] = new Mod("Heat Sink", "-20% Heat Generation", ButtonIcons.modIcons.coolingRate, 2, 0);
    tier2[1] = new Mod("Larger Power Supply", "+2 Rate of Fire", ButtonIcons.modIcons.rateOfFire, 2, 1);
    tier2[2] = new Mod("Thermoelectric Cooler", "+50% Cooling Rate, -0.5 Overheat Duration", ButtonIcons.modIcons.coolingRate, 2, 2);

    tier3 = new Mod[2];
    tier3[0] = new Mod("Densification Ray", "Enemies damaged will have their movespeed slowed by 50% for 1 second", ButtonIcons.modIcons.slowdown, 3, 0);
    tier3[1] = new Mod("Temperature Amplifier", "Whenever the CWC damages a Burning or Frozen enemy, it inflicts 50% of its damage " +
            "as Heat or 75% as Cold (respectively) to all enemies within a 2.5m radius (including the Burning/Frozen enemy itself). " +
            "This causes the primary target to have an extended Burning or Frozen duration, while also causing nearby enemies to start " +
            "to Ignite or Freeze.", ButtonIcons.modIcons.coolingRate, 3, 1);

    tier4 = new Mod[2];
    tier4[0] = new Mod("Wide Lens Add-On", "Press Reload to toggle Wide Lens Add-On off/on. " +
            "When activated, x1.25 Shot Width in exchange for +110% Heat Generation (this penalty is currently bugged " +
            "and not affected by other upgrades that affect Heat Generation).", ButtonIcons.modIcons.aoeRadius, 4, 0);
    tier4[1] = new Mod("Power Supply Overdrive", "Press Reload to toggle Power Supply Overdrive off/on. " +
            "When activated, +2.5 Rate of Fire in exchange for x0.1 Shot Width and +40% Heat Generation " +
            "(this penalty is currently bugged and not affected by other upgrades that affect Heat Generation).", ButtonIcons.modIcons.rateOfFire, 4, 1);

    tier5 = new Mod[3];
    tier5[0] = new Mod("Contagion Transmitter", "x1.3 Damage vs enemies afflicted by Neurotoxin, Corrosion, or Sludge Puddles. " +
            "Additionally, a 10% chance per ammo to spread Neurotoxin from the primary target to nearby enemies.", ButtonIcons.modIcons.neurotoxin, 5, 0);
    tier5[1] = new Mod("Boiler Ray", "Enemies killed by the Wave Cooker have a 50% chance to explode, dealing 100 Explosive Area Damage in a 2.5m radius.", ButtonIcons.modIcons.addedExplosion, 5, 1);
    tier5[2] = new Mod("Exothermic Reactor", "Every time the Wave Cooker damages a Burning or Frozen enemy, it has a 25% chance to trigger Exothermic Reactor. " +
            "Exothermic Reactor sets the enemy's temperature back to zero, applies Temperature Shock to them (dealing 200 Disintegrate-element damage), " +
            "and causes the enemy to deal 50 Temperature \"damage\" in a 2m radius -- Heat if they were Burning, or Cold if they were Frozen.", ButtonIcons.modIcons.special, 5, 2);

    overclocks = new Overclock[6];
    overclocks[0] = new Overclock(Overclock.classification.clean, "Liquid Cooling System", "-10% Heat Generation, +17% Cooling Rate, and -0.2 Overheat Duration", ButtonIcons.overclockIcons.coolingRate, 0);
    overclocks[1] = new Overclock(Overclock.classification.clean, "Super Focus Lens", "x1.5 Damage to enemies within 4m of you", ButtonIcons.overclockIcons.baseSpread, 1);
    overclocks[2] = new Overclock(Overclock.classification.balanced, "Diffusion Ray", "Adds 3 penetrations and each enemy damaged has their movespeed slowed by 20% for 1 second; in exchange, -1 Damage.", ButtonIcons.overclockIcons.blowthrough, 2);
    overclocks[3] = new Overclock(Overclock.classification.balanced, "Mega Power Supply", "+100 Magazine Size, +3 Rate of Fire, -50% Cooling Rate, and +1 Overheat Duration", ButtonIcons.overclockIcons.carriedAmmo, 3);
    overclocks[4] = new Overclock(Overclock.classification.unstable, "Blistering Necrosis", "Every time the Wave Cooker damages an enemy, " +
            "it has a 10% chance to spawn an Explosive Boil on that enemy (with a 0.3 sec cooldown between procs). Each Explosive Boil multiplies non-Radial damage by x1.5, has " +
            "50 health, and does 80 Poison Damage to the enemy when broken. In exchange, x1.25 Heat Generation and x0.75 Cooling Rate.", ButtonIcons.overclockIcons.weakpointBonus, 4, false);
    overclocks[5] = new Overclock(Overclock.classification.unstable, "Gamma Contamination", "Every time the Wave Cooker damages an enemy, " +
            "it has a 25% chance to irradiate that enemy dealing an average of 22.22 Radiation Damage per Second for 7 seconds. Additionally, irradiated enemies do an average of " +
            "13.33 Radiation DPS to enemies within 2m of them. In exchange, -1 Damage, -50 Magazine Size, and -50 Shot Width.", ButtonIcons.overclockIcons.radioactive, 5, false);
    // TODO: Not sure if MS wants to implement Blistering and Gamma.

    // This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
    modsAndOCsInitialized = true;
  }

  @Override
  public WaveCooker clone() {
    return new WaveCooker(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
  }

  @Override
  public String getDwarfClass() {
    return "Driller";
  }

  @Override
  public String getSimpleName() {
    return "WaveCooker";
  }

  @Override
  public int getDwarfClassID() {
    return DatabaseConstants.drillerCharacterID;
  }

  @Override
  public int getWeaponID() {
    return DatabaseConstants.waveCookerID;
  }

  /****************************************************************************************
   * Setters and Getters
   ****************************************************************************************/

  private double getDirectDamage() {
    double toReturn = directDamage;

    if (selectedTier1 == 0) {
      toReturn += 2;
    }

    // For now, applying Super Focus Lens bonus all the time.
    if (selectedOverclock == 1) {
      toReturn *= 1.5;
    } else if (selectedOverclock == 2 || selectedOverclock == 5) {
      toReturn -= 1;
    }

    return toReturn;
  }

  private double getShotWidth() {
    double toReturn = shotWidth;

    if (selectedOverclock == 5) {
      toReturn -= 50;
    }

    if (selectedTier1 == 2) {
      toReturn *= 3;
    }
    if (selectedTier4 == 0) {
      toReturn *= 1.25;
    } else if (selectedTier4 == 1) {
      toReturn *= 0.1;
    }

    return toReturn;
  }

  private int getMagazineSize() {
    int toReturn = magazineSize;

    if (selectedTier1 == 1) {
      toReturn += 100;
    }

    if (selectedOverclock == 3) {
      toReturn += 100;
    } else if (selectedOverclock == 5) {
      toReturn -= 50;
    }

    return toReturn;
  }

  public double getRateOfFire() {
    double toReturn = rateOfFire;

    if (selectedTier2 == 1) {
      toReturn += 2;
    }
    if (selectedTier4 == 1) {
      toReturn += 2.5;
    }

    if (selectedOverclock == 3) {
      toReturn += 3;
    }

    return toReturn;
  }

  private double getHeatGeneration() {
    double toReturn = heatGeneration;

    if (selectedTier2 == 0) {
      toReturn -= 0.2;
    }

//        Not sure if T4 is still bugged.
//        if (selectedTier4 == 0) {
//            toReturn += 1.1;
//        }
//        else if (selectedTier4 == 1) {
//            toReturn += 0.4;
//        }

    if (selectedOverclock == 0) {
      toReturn -= 0.1;
    } else if (selectedOverclock == 4) {
      toReturn *= 1.25;
    }

    return toReturn;
  }

  private double getCoolingRate() {
    double toReturn = coolingRate;

    if (selectedTier2 == 2) {
      toReturn += 0.5;
    }

    if (selectedOverclock == 0) {
      toReturn += 0.17;
    } else if (selectedOverclock == 3) {
      toReturn -= 0.5;
    } else if (selectedOverclock == 4) {
      toReturn *= 0.75;
    }

    return toReturn;
  }

  private double getOverheatDuration() {
    double toReturn = overheatDuration;

    if (selectedTier2 == 2) {
      toReturn -= 0.5;
    }

    if (selectedOverclock == 0) {
      toReturn -= 0.2;
    } else if (selectedOverclock == 3) {
      toReturn += 1;
    }

    return toReturn;
  }

  @Override
  public StatsRow[] getStats() {
    StatsRow[] toReturn = new StatsRow[7];

    boolean damageModified = selectedTier1 == 0 || selectedOverclock == 1 || selectedOverclock == 2 || selectedOverclock == 5;
    toReturn[0] = new StatsRow("Damage:", getDirectDamage(), ButtonIcons.modIcons.directDamage, damageModified);

    boolean widthModified = selectedTier1 == 2 || selectedTier4 == 0 || selectedTier4 == 1 || selectedOverclock == 5;
    toReturn[1] = new StatsRow("Shot Width:", getShotWidth(), ButtonIcons.modIcons.aoeRadius, widthModified);

    boolean magazineModified = selectedTier1 == 1 || selectedOverclock == 3 || selectedOverclock == 5;
    toReturn[2] = new StatsRow("Magazine Size:", getMagazineSize(), ButtonIcons.modIcons.carriedAmmo, magazineModified);

    boolean rofModified = selectedTier2 == 1 || selectedTier4 == 1 || selectedOverclock == 3;
    toReturn[3] = new StatsRow("Rate of Fire:", getRateOfFire(), ButtonIcons.modIcons.rateOfFire, rofModified);

    boolean heatModified = selectedTier2 == 0 || selectedOverclock == 0 || selectedOverclock == 4;
    toReturn[4] = new StatsRow("Heat Generation:", convertDoubleToPercentage(getHeatGeneration()), ButtonIcons.modIcons.blank, heatModified);

    boolean coolingModified = selectedTier2 == 2 || selectedOverclock == 0 || selectedOverclock == 3 || selectedOverclock == 4;
    toReturn[5] = new StatsRow("Cooling Rate:", convertDoubleToPercentage(getCoolingRate()), ButtonIcons.modIcons.coolingRate, coolingModified);

    boolean overheatModified = selectedTier2 == 2 || selectedOverclock == 0 || selectedOverclock == 3;
    toReturn[6] = new StatsRow("Overheat Duration:", getOverheatDuration(), ButtonIcons.modIcons.duration, overheatModified);

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
