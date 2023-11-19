package drgtools.dpscalc.weapons.gunner.coilGun;

import drgtools.dpscalc.dataGenerator.DatabaseConstants;
import drgtools.dpscalc.guiPieces.WeaponPictures;
import drgtools.dpscalc.guiPieces.customButtons.ButtonIcons;
import drgtools.dpscalc.modelPieces.Mod;
import drgtools.dpscalc.modelPieces.Overclock;
import drgtools.dpscalc.modelPieces.StatsRow;
import drgtools.dpscalc.weapons.Weapon;

public class CoilGun extends Weapon {

  /****************************************************************************************
   * Class Variables
   ****************************************************************************************/

  private double directDamage;
  private int maxAmmo;
  private int magazineSize;
  private double chargeSpeed;
  private double reloadTime;
  private int ammoPerChargedShot;
  private double trailRadius;
  private int trailDuration;

  /****************************************************************************************
   * Constructors
   ****************************************************************************************/

  // Shortcut constructor to get baseline data
  public CoilGun() {
    this(-1, -1, -1, -1, -1, -1);
  }

  // Shortcut constructor to quickly get statistics about a specific build
  public CoilGun(String combination) {
    this(-1, -1, -1, -1, -1, -1);
    buildFromCombination(combination);
  }

  public CoilGun(int mod1, int mod2, int mod3, int mod4, int mod5, int overclock) {
    fullName = "Armskore Coil Gun";
    weaponPic = WeaponPictures.burstPistol;

    // Base stats, before mods or overclocks alter them:
    directDamage = 130;
    maxAmmo = 640;
    magazineSize = 40;
    chargeSpeed = 1.0;
    reloadTime = 1.9;
    ammoPerChargedShot = 40;
    trailRadius = 0.3;
    trailDuration = 5;

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
    tier1[0] = new Mod("Extra Coil", "+40 Damage", ButtonIcons.modIcons.directDamage, 1, 0);
    tier1[1] = new Mod("Larger Battery", "+280 Ammo", ButtonIcons.modIcons.carriedAmmo, 1, 1);
    tier1[2] = new Mod("Resonance Tuning", "+2 sec Trail Duration", ButtonIcons.modIcons.hourglass, 1, 2);

    tier2 = new Mod[3];
    tier2[0] = new Mod("Overcharger", "After fully charging a shot, every additional 0.25 seconds that you hold the shot will add +12.5 Kinetic-element Direct Damage to the shot (up to a maximum of 100 extra damage at 2 seconds past full charge)", ButtonIcons.modIcons.lastShotDamage, 2, 0);
    tier2[1] = new Mod("Controlled Magnetic Flow", "Lowers the amount of charge required to fire a Charged Shot from 100% to 25%. The Damage and Ammo consumption of the shot is reduced proportional to the percentage charged (intervals of 25%, 50%, 75%, and the default 100%).", ButtonIcons.modIcons.duration, 2, 1);
    tier2[2] = new Mod("Optimized Magnetic Circuit", "+150% Charge Speed", ButtonIcons.modIcons.chargeSpeed, 2, 2);

    tier3 = new Mod[2];
    tier3[0] = new Mod("Concussive Shockwave", "Adds a 50% chance to Stun enemies within 1.5m of the shot's trajectory for 3 seconds", ButtonIcons.modIcons.stun, 3, 0);
    tier3[1] = new Mod("Fear Trajectory", "Applies a 250% Base Fear Chance to enemies within 5m of the shot's trajectory", ButtonIcons.modIcons.fear, 3, 1);

    tier4 = new Mod[2];
    tier4[0] = new Mod("Defence Enhancement", "While charging up or holding a Charged Shot, you take x0.5 damage from all sources (50% Damage Resistance).", ButtonIcons.modIcons.damageResistance, 4, 0);
    tier4[1] = new Mod("Shockwave", "Deal 20 Kinetic-element Area Damage to enemies within 3m of the Coil Gun's muzzle when you release a Charged Shot", ButtonIcons.modIcons.special, 4, 1);

    tier5 = new Mod[3];
    tier5[0] = new Mod("Necro-Thermal Catalyst", "The primary Damage Component gains a bonus +30% damage as Fire-element + Heat when damaging Burning enemies. Additionally, killing a Burning enemy will cause an explosion that does 80 Fire-element + Heat Radial Damage in a 6m radius.", ButtonIcons.modIcons.addedExplosion, 5, 0);
    tier5[1] = new Mod("Dilated Injector System", "+1m Trail Radius", ButtonIcons.modIcons.aoeRadius, 5, 1);
    tier5[2] = new Mod("Electric Trail", "Adds an Electric Trail to each of your shots. Any enemy in contact with the Electric Trail takes 8 Electric Damage per Second and has their movespeed slowed by 80%.", ButtonIcons.modIcons.electricity, 5, 2);

    overclocks = new Overclock[6];
    overclocks[0] = new Overclock(Overclock.classification.clean, "Ultra-Magnetic Coils", "+0.5m Trail Radius, +1 sec Trail Duration", ButtonIcons.overclockIcons.hourglass, 0);
    overclocks[1] = new Overclock(Overclock.classification.clean, "Re-Atomizer", "If the first enemy hit by the shot is suffering from any Corrosion, Electrocution, Neurotoxin, Persistent Plasma, or Slowdown Status Effects caused by a player, then any enemies hit afterwards will have the base version of those Status Effects applied to them too. Additionally, if the first enemy hit is either On Fire or Frozen, then it will transfer Heat or Cold (respectively) to any enemies hit afterwards. For a full explanation of how this OC works and the enumerated Status Effect whitelist, go to the Wiki.gg DRG Wiki for further reading.", ButtonIcons.overclockIcons.special, 1);
    overclocks[2] = new Overclock(Overclock.classification.balanced, "Backfeeding Module", "+320 Ammo, -3 sec Trail Duration", ButtonIcons.overclockIcons.carriedAmmo, 2);
    overclocks[3] = new Overclock(Overclock.classification.balanced, "The Mole", "Each shot penetrates 2.69x further through terrain, and for each section of terrain it pierces through it gains +150 Kinetic-element Damage. In exchange, x0.8 Charge Speed.", ButtonIcons.overclockIcons.blowthrough, 3);
    overclocks[4] = new Overclock(Overclock.classification.unstable, "Hellfire", "At full charge, the Charged Shot Trail gains the ability to heat up enemies at an average of 68.6 Heat/sec. In exchange: -200 Ammo, x0.7 Charge Speed, and -2 sec Trail Duration.", ButtonIcons.overclockIcons.heatDamage, 4);
    overclocks[5] = new Overclock(Overclock.classification.unstable, "Triple-Tech Chambers", "After releasing a fully charged shot, you can fire up to two more shots that each deal 75% damage, cost 50% ammo, and don't penetrate terrain. You must fire the the 2nd shot within one second of the 1st, and the 3rd shot within one second of the 2nd. In exchange, x0.7 Charge Speed and +0.5 Reload Time.", ButtonIcons.overclockIcons.directDamage, 5);

    // This boolean flag has to be set to True in order for Weapon.isCombinationValid() and Weapon.buildFromCombination() to work.
    modsAndOCsInitialized = true;
  }

  @Override
  public String getDwarfClass() {
    return "Gunner";
  }

  @Override
  public String getSimpleName() {
    return "CoilGun";
  }

  @Override
  public int getDwarfClassID() {
    return DatabaseConstants.gunnerCharacterID;
  }

  @Override
  public int getWeaponID() {
    return DatabaseConstants.coilGunID;
  }

  @Override
  public CoilGun clone() {
    return new CoilGun(selectedTier1, selectedTier2, selectedTier3, selectedTier4, selectedTier5, selectedOverclock);
  }

  /****************************************************************************************
   * Setters and Getters
   ****************************************************************************************/

  private double getDirectDamage() {
    if (selectedTier1 == 0) {
      return directDamage += 40;
    }
    return directDamage;
  }

  private int getMaxAmmo() {
    int toReturn = maxAmmo;

    if (selectedTier1 == 1) {
      toReturn += 280;
    }

    if (selectedOverclock == 2) {
      toReturn += 320;
    } else if (selectedOverclock == 4) {
      toReturn -= 200;
    }

    return toReturn;
  }

  private double getChargeSpeed() {
    double toReturn = chargeSpeed;

    if (selectedTier2 == 2) {
      toReturn += 1.5;
    }

    if (selectedOverclock == 3) {
      toReturn *= 0.8;
    } else if (selectedOverclock == 4 || selectedOverclock == 5) {
      toReturn *= 0.7;
    }

    return toReturn;
  }

  private double getReloadTime() {
    if (selectedOverclock == 5) {
      return reloadTime + 0.5;
    }

    return reloadTime;
  }

  private double getTrailRadius() {
    double toReturn = trailRadius;
    if (selectedTier5 == 1) {
      toReturn += 1;
    }

    if (selectedOverclock == 0) {
      toReturn += 0.5;
    }

    return toReturn;
  }

  private int getTrailDuration() {
    int toReturn = trailDuration;

    if (selectedTier1 == 2) {
      toReturn += 2;
    }

    if (selectedOverclock == 0) {
      toReturn += 1;
    } else if (selectedOverclock == 2) {
      toReturn -= 3;
    } else if (selectedOverclock == 5) {
      toReturn -= 2;
    }

    return toReturn;
  }

  @Override
  public StatsRow[] getStats() {
    StatsRow[] toReturn = new StatsRow[15];

    toReturn[0] = new StatsRow("Damage:", getDirectDamage(), ButtonIcons.modIcons.directDamage, selectedTier1 == 0);

    boolean ammoModified = selectedTier1 == 1 || selectedOverclock == 2 || selectedOverclock == 4;
    toReturn[1] = new StatsRow("Max Ammo:", getMaxAmmo(), ButtonIcons.modIcons.carriedAmmo, ammoModified);

    toReturn[2] = new StatsRow("Magazine Size:", magazineSize, ButtonIcons.modIcons.magSize, false);

    boolean chargeSpeedModified = selectedTier2 == 2 || selectedOverclock == 3 || selectedOverclock == 4 || selectedOverclock == 5;
    toReturn[3] = new StatsRow("Charge Speed:", convertDoubleToPercentage(getChargeSpeed()), ButtonIcons.modIcons.chargeSpeed, chargeSpeedModified);

    toReturn[4] = new StatsRow("Reload Time:", getReloadTime(), ButtonIcons.modIcons.reloadSpeed, selectedOverclock == 5);

    toReturn[5] = new StatsRow("Ammo per Charged Shot:", ammoPerChargedShot, ButtonIcons.modIcons.blank, false);

    toReturn[6] = new StatsRow("Trail Radius:", getTrailRadius(), ButtonIcons.modIcons.aoeRadius, selectedTier5 == 1 || selectedOverclock == 0);

    boolean trailDurationModified = selectedTier1 == 2 || selectedOverclock == 0 || selectedOverclock == 2 || selectedOverclock == 5;
    toReturn[7] = new StatsRow("Trail Duration:", getTrailDuration(), ButtonIcons.modIcons.duration, trailDurationModified);

    double stunChance = 0.5;
    boolean stunEnabled = selectedTier3 == 0;
    toReturn[8] = new StatsRow("Trajectory Stun Chance:", convertDoubleToPercentage(stunChance), ButtonIcons.modIcons.stun, stunEnabled, stunEnabled);

    int stunDuration = 3;
    toReturn[9] = new StatsRow("Trajectory Stun Duration:", stunDuration, ButtonIcons.modIcons.blank, stunEnabled, stunEnabled);

    double stunRadius = 1.5;
    toReturn[10] = new StatsRow("Trajectory Stun Radius:", stunRadius, ButtonIcons.modIcons.blank, stunEnabled, stunEnabled);

    double fearChance = 2.5;
    boolean fearEnabled = selectedTier3 == 1;
    toReturn[11] = new StatsRow("Trajectory Base Fear Chance:", convertDoubleToPercentage(fearChance), ButtonIcons.modIcons.fear, fearEnabled, fearEnabled);

    int fearRadius = 5;
    toReturn[12] = new StatsRow("Trajectory Fear Radius:", fearRadius, ButtonIcons.modIcons.blank, fearEnabled, fearEnabled);

    double damageResistance = 0.5;
    boolean resistanceEnabled = selectedTier4 == 0;
    toReturn[13] = new StatsRow("Damage Resistance while Charging:", convertDoubleToPercentage(damageResistance), ButtonIcons.modIcons.damageResistance, resistanceEnabled, resistanceEnabled);

    int shockwaveDamage = 20;
    boolean shockwaveEnabled = selectedTier4 == 1;
    toReturn[14] = new StatsRow("Front AoE Shockwave Damage:", shockwaveDamage, ButtonIcons.modIcons.special, shockwaveEnabled, shockwaveEnabled);

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
