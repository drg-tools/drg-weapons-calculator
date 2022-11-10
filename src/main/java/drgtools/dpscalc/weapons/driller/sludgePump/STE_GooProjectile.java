package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageElements.damageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_GooProjectile extends StatusEffect {
    public STE_GooProjectile() {
        super(damageElement.corrosive, 8, 8, 0.2, 0.25, 0.65, 4);
        canDamageArmor = true;
        minArmorDamagePerTick = 15;
        maxArmorDamagePerTick = 15;
    }
}
