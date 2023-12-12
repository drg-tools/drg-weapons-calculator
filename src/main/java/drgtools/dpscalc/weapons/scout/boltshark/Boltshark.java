package drgtools.dpscalc.weapons.scout.boltshark;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.weapons.Weapon;

public class Boltshark extends Weapon {

  /****************************************************************************************
   * Class Variables
   ****************************************************************************************/

  private double directDamage;
  private int areaDamage;
  private double maxAmmo;
  private double reloadTime;
  private double ammoSwapTime;
  private double projectileVelocity;

  /****************************************************************************************
   * Constructors
   ****************************************************************************************/

  // Shortcut constructor to get baseline data
  public Boltshark() {
    this(-1, -1, -1, -1, -1, -1);
  }

  public Boltshark(String combination) {
    this(-1, -1, -1, -1, -1, -1);
    buildFromCombination(combination);
  }

  public Boltshark(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
    fullName = "Nishanka Boltshark X-80";
    weaponPic = WeaponPictures.breachCutter;

    // Base stats, before mods or overclocks alter them:
    directDamage = 105;
    areaDamage = 60;
    maxAmmo = 20;
    reloadTime = 1.6;
    ammoSwapTime = 1.75;
    projectileVelocity = 1.0;

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
    tier1[0] = new Mod("Special Bolt: Pheromone Dart", "Special Bolt: 15 Direct Damage, 10 Area Damage, 9 bolts, 10 sec effect duration. If you hit an enemy, it will have its movespeed slowed by 30% and be hostile to other enemies for the duration of the effect.", ButtonIcons.modIcons.pheromones, 1, 0);
    tier1[1] = new Mod("Special Bolt: Chemical Explosion", "Special Bolt: 15 Direct Damage, 12 Area Damage, 9 bolts, 14 sec effect duration. If you hit an enemy, it will take 10 Poison Damage per Second for the duration of the effect. If it dies while it still has that status effect active (or is killed by the status effect), then after a 1 sec delay it will explode for 140 Poison Area Damage in a 4m radius. The explosion also Stuns for 1 second and inflicts 100% Base Fear Chance.", ButtonIcons.modIcons.addedExplosion, 1, 1);
    tier1[2] = new Mod("Special Bolt: Taser", "Special Bolt: 15 Direct Damage, 10 Area Damage, 12 bolts, 12 sec effect duration. If you hit an enemy, it will have its movespeed slowed by 50% and take an average of 16 Electric Damage per Second. If you shoot a second Taser bolt within 6m of the first one, an electric beam will arc between the two bolts that slows enemies by 80% and does 30 Electric DPS. Taser Bolts are not retrievable.", ButtonIcons.modIcons.electricity, 1, 2);

    tier2 = new Mod[3];
    tier2[0] = new Mod("Broadhead Bolts", "+25 Direct Damage, +15 Area Damage", ButtonIcons.modIcons.directDamage, 2, 0);
    tier2[1] = new Mod("Increased Quiver Capacity", "+7 Max Ammo", ButtonIcons.modIcons.carriedAmmo, 2, 1);
    tier2[2] = new Mod("Expanded Special Quiver", "x1.4 Special Ammo", ButtonIcons.modIcons.special, 2, 2);

    tier3 = new Mod[2];
    tier3[0] = new Mod("Stabilizing Arm Brace", "-0.4 Reload Time, -0.4 Ammo Swap Time", ButtonIcons.modIcons.reloadSpeed, 3, 0);
    tier3[1] = new Mod("Reinforced String", "x1.5 Projectile Velocity", ButtonIcons.modIcons.projectileVelocity, 3, 1);

    tier4 = new Mod[2];
    tier4[0] = new Mod("Battle Frenzy", "After killing an enemy, gain +50% Movement Speed for 2.5 seconds", ButtonIcons.modIcons.movespeed, 4, 0);
    tier4[1] = new Mod("Radio Transmitter Module", "Hovering your crosshair over a retrievable arrow will pull it back into your inventory.", ButtonIcons.modIcons.remoteMechanism, 4, 1);

    tier5 = new Mod[3];
    tier5[0] = new Mod("Potent Special Bolts", "x1.25 Special Effect Duration", ButtonIcons.modIcons.hourglass, 5, 0);
    tier5[1] = new Mod("Magnetic Shafts", "Bolts are magnetically pulled towards enemies that are affected by IFG or Electrocuted. Additionally, they do a bonus 25% Direct and Area Damage as Kinetic-element to those enemies.", ButtonIcons.modIcons.electricity, 5, 1);
    tier5[2] = new Mod("Banshee Module", "When normal arrows impact terrain, they apply 33% Base Fear Chance to enemies within a 3m radius, pulsing once per second for 5 seconds.", ButtonIcons.modIcons.fear, 5, 2);

    overclocks = new Overclock[6];
    overclocks[0] = new Overclock(Overclock.classification.clean, "Quick Fire", "-0.2 Reload Time and x2 Projectile Velocity", ButtonIcons.overclockIcons.rateOfFire, 0);
    overclocks[1] = new Overclock(Overclock.classification.clean, "The Specialist", "x1.25 Special Ammo and x1.3 Special Effect Duration", ButtonIcons.overclockIcons.special, 1);
    overclocks[2] = new Overclock(Overclock.classification.balanced, "Cryo Bolt", "Hitting an enemy directly with a normal arrows will start freezing it at an average of 18 Cold/sec for 6 seconds. Enemies within 2m of the arrow will start to freeze at 20 Cold/sec for 6 seconds. These effects stack with themselves if you fire multiple arrows. In exchange, -50 Direct Damage. These bolts are no longer retrievable.", ButtonIcons.overclockIcons.coldDamage, 2);
    overclocks[3] = new Overclock(Overclock.classification.balanced, "Fire Bolt", "Hitting an enemy directly with a normal arrows will start igniting it at an average of 16 Heat/sec for 6 seconds. Enemies within 2m of the arrow will start to ignite at 20 Heat/sec for 5 seconds. These effects stack with themselves if you fire multiple arrows. In exchange, -50 Direct Damage. These bolts are no longer retrievable.", ButtonIcons.overclockIcons.heatDamage, 3);
    overclocks[4] = new Overclock(Overclock.classification.unstable, "Bodkin Points", "Normal bolts will ricochet towards nearby enemies up to two times, and the internal Rate of Fire gets increased from 4 to 6 (effectively a -0.083 Reload Time bonus). In exchange, -75 Direct Damage and x1.5 Reload Time.", ButtonIcons.overclockIcons.ricochet, 4);
    overclocks[5] = new Overclock(Overclock.classification.unstable, "Trifork Volley", "Fire three normal bolts at once and gain x1.21 Max Ammo. In exchange: x0.85 Direct Damage, x1.5 Reload Time, and the internal Rate of Fire gets reduced from 4 to 1.5 (effectively an additional +0.417 Reload Time). These bolts are no longer retrievable.", ButtonIcons.overclockIcons.lastShotDamage, 5);
  }

  @Override
  public Boltshark clone() {
    return new Boltshark(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
  }

  @Override
  public String getDwarfClass() {
    return "Scout";
  }

  @Override
  public String getSimpleName() {
    return "Boltshark";
  }

  @Override
  public int getDwarfClassID() {
    return DatabaseConstants.scoutCharacterID;
  }

  @Override
  public int getWeaponID() {
    return DatabaseConstants.boltsharkID;
  }

  /****************************************************************************************
   * Setters and Getters
   ****************************************************************************************/

  private double getDirectDamage() {
    double toReturn = directDamage;

    if (selectedTier2 == 0) {
      toReturn += 25;
    }

    if (selectedOverclock == 2 || selectedOverclock == 3) {
      toReturn -= 50;
    } else if (selectedOverclock == 4) {
      toReturn -= 75;
    } else if (selectedOverclock == 5) {
      toReturn *= 0.85;
    }

    return toReturn;
  }

  private int getAreaDamage() {
    if (selectedTier2 == 0) {
      return areaDamage += 15;
    }

    return areaDamage;
  }

  private double getMaxAmmo() {
    double toReturn = maxAmmo;

    if (selectedTier2 == 1) {
      toReturn += 7;
    }

    if (selectedOverclock == 5) {
      toReturn *= 1.21;
    }

    return toReturn;
  }

  private double getReloadTime() {
    double toReturn = reloadTime;

    if (selectedTier3 == 0) {
      toReturn -= 0.4;
    }

    if (selectedOverclock == 0) {
      toReturn -= 0.2;
    }

    return toReturn;
  }

  private double getAmmoSwapTime() {
    if (selectedTier3 == 0) {
      return ammoSwapTime -= 0.4;
    }

    return ammoSwapTime;
  }

  private double getProjectileVelocity() {
    double toReturn = projectileVelocity;

    if (selectedTier3 == 1) {
      toReturn *= 1.5;
    }

    if (selectedOverclock == 0) {
      toReturn *= 2.0;
    }

    return toReturn;
  }

  private double getSpecialAmmo() {
    double specialAmmo = 0;

    if (selectedTier1 == 0 || selectedTier1 == 1) {
      specialAmmo = 9;
    } else if (selectedTier1 == 2) {
      specialAmmo = 12;
    }

    if (selectedTier2 == 2) {
      specialAmmo *= 1.4;
    }

    if (selectedOverclock == 1) {
      specialAmmo *= 1.25;
    }

    return specialAmmo;
  }

  private double getSpecialEffectDuration() {
    double duration = 0;

    if (selectedTier1 == 0) {
      duration = 10;
    } else if (selectedTier1 == 1) {
      duration = 14;
    } else if (selectedTier1 == 2) {
      duration = 12;
    }

    if (selectedTier5 == 0) {
      duration *= 1.25;
    }

    if (selectedOverclock == 1) {
      duration *= 1.3;
    }

    return duration;
  }

  private double getDamageVsElectrically() {
    if (selectedTier5 == 1) {
      return 0.25;
    }

    return 0;
  }

  @Override
  public StatsRow[] getStats() {
    StatsRow[] toReturn = new StatsRow[9];

    boolean directDamageModified = selectedTier2 == 0 || selectedOverclock == 2 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 5;
    toReturn[0] = new StatsRow("Damage:", getDirectDamage(), ButtonIcons.modIcons.directDamage, directDamageModified);

    toReturn[1] = new StatsRow("Area Damage:", getAreaDamage(), ButtonIcons.modIcons.areaDamage, selectedTier2 == 0);

    toReturn[2] = new StatsRow("Max Ammo:", getMaxAmmo(), ButtonIcons.modIcons.carriedAmmo, selectedTier2 == 1 || selectedOverclock == 5);


    toReturn[3] = new StatsRow("Reload Time:", getReloadTime(), ButtonIcons.modIcons.reloadSpeed, selectedTier3 == 0 || selectedOverclock == 0);

    toReturn[4] = new StatsRow("Ammo Swap Time:", getAmmoSwapTime(), ButtonIcons.modIcons.reloadSpeed, selectedTier3 == 0);

    toReturn[5] = new StatsRow("Projectile Velocity:", convertDoubleToPercentage(getProjectileVelocity()), ButtonIcons.modIcons.projectileVelocity, selectedTier3 == 1 || selectedOverclock == 0);

    boolean specialAmmoModified = selectedTier1 >= 0 || selectedTier2 == 2 || selectedOverclock == 1;
    toReturn[6] = new StatsRow("Special Ammo:", Math.round(getSpecialAmmo()), ButtonIcons.modIcons.special, specialAmmoModified, specialAmmoModified);

    boolean specialDurationModified = selectedTier1 >= 0 || selectedTier5 == 0 || selectedOverclock == 1;
    toReturn[7] = new StatsRow("Special Effect Duration", getSpecialEffectDuration(), ButtonIcons.modIcons.hourglass, specialDurationModified, specialDurationModified);

    toReturn[8] = new StatsRow("Bonus Damage vs Electrically Affected:", convertDoubleToPercentage(getDamageVsElectrically()), ButtonIcons.modIcons.electricity, selectedTier5 == 1, selectedTier5 == 1);

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
