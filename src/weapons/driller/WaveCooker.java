package weapons.driller;

import dataGenerator.DatabaseConstants;
import guiPieces.WeaponPictures;
import guiPieces.customButtons.ButtonIcons.modIcons;
import guiPieces.customButtons.ButtonIcons.overclockIcons;
import modelPieces.UtilityInformation;
import modelPieces.EnemyInformation;
import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import utilities.MathUtils;
import weapons.Weapon;

public class WaveCooker extends Weapon {

    /****************************************************************************************
     * Class Variables
     ****************************************************************************************/

    private double directDamage;
    private int shotWidth;
    private int magazineSize;
    private double rateOfFire;
    private double heatPerSecond;
    private double coolingRate;
    private int cooldownAfterOverheat;

    /****************************************************************************************
     * Constructors
     ****************************************************************************************/

    public WaveCooker() {
        this(-1, -1, -1, -1, -1, -1);
    }

    public WaveCooker(String combination) {
        this(-1, -1, -1, -1, -1, -1);
        buildFromCombination(combination);
    }

    @Override
    protected void initializeModsAndOverclocks() {

    }

    @Override
    public String getDwarfClass() {
        return null;
    }

    @Override
    public String getSimpleName() {
        return null;
    }

    @Override
    public int getDwarfClassID() {
        return 0;
    }

    @Override
    public int getWeaponID() {
        return 0;
    }

    @Override
    public StatsRow[] getStats() {
        return new StatsRow[0];
    }

    @Override
    public Weapon clone() {
        return null;
    }

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
