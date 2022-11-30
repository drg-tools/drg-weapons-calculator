package drgtools.dpscalc.modelPieces.damage;

public class DamageFlags {
    // Technically speaking, reducedByLightArmor and stoppedByHeavyArmor are the same Damage Flag.
    // Likewise, lightArmor and heavyArmor are the same Material Flag.
    // However, in order to model Overclocks like "Explosive Reload", "Embedded Detonators", and "Electrifying Reload", it's simpler to split them up like this.
    // Because all three of those do no damage if they don't damage the enemy's healthbar, which works through Light Armor but not Heavy or Unbreakable Armor.
    public enum DamageFlag{benefitsFromWeakpoint, benefitsFromFrozen, reducedByLightArmor, stoppedByHeavyArmor, canDamageArmor}
    public enum MaterialFlag{normalFlesh, weakpoint, lightArmor, heavyArmor, frozen}
    public enum RicochetFlag{everything, notCreatures, onlyCreatures, onlyCreatureWeakpoints}
}
