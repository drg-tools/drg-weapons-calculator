package drgtools.dpscalc.modelPieces.damage;

public class DamageInstance {
    private DamageComponent damagePerPellet;
    private int numPellets;
    private DamageComponent[] otherDamage;

    // Shortcut constructor for most hitscan weapons
    public DamageInstance(DamageComponent dmgPerPellet) {
        damagePerPellet = dmgPerPellet;
        numPellets = 1;
        otherDamage = null;
    }
    // Shortcut constructor for non-shotguns
    public DamageInstance(DamageComponent primaryDmgComp, DamageComponent[] oDamage) {
        damagePerPellet = primaryDmgComp;
        numPellets = 1;
        otherDamage = oDamage;
    }
    public DamageInstance(DamageComponent dmgPerPellet, int nPellets, DamageComponent[] oDamage) {
        damagePerPellet = dmgPerPellet;
        numPellets = nPellets;
        otherDamage = oDamage;
    }

    public DamageComponent getDamagePerPellet() {
        return damagePerPellet;
    }
    public int getNumPellets() {
        return numPellets;
    }
    public boolean otherDamageIsDefined() {
        return otherDamage != null && otherDamage.length > 0;
    }
    public int getNumberOfOtherDamageComponents() {
        if (otherDamageIsDefined()) {
            return otherDamage.length;
        }
        else {
            return -1;
        }
    }
    public DamageComponent getOtherDamageComponentAtIndex(int index) {
        if (otherDamageIsDefined() && index > -1 && index < otherDamage.length) {
            return otherDamage[index];
        }
        else {
            return null;
        }
    }

    public int getTotalNumberOfDamageComponents() {
        if (otherDamageIsDefined()) {
            return numPellets + otherDamage.length;
        }
        else {
            return numPellets;
        }
    }

    public DamageComponent getDamageComponentAtIndex(int index) {
        int numDmgComps = getTotalNumberOfDamageComponents();
        if (index < 0 || index >= numDmgComps) {
            return null;
        }
        else if (index < numPellets) {
            return damagePerPellet;
        }
        else {
            // Because the only way that index >= numPellets is if otherDamage is non-null and non-empty, this accessor should be safe.
            return otherDamage[index - numPellets];
        }
    }
}
