package drgtools.dpscalc.weapons.driller.epc;

import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

public class STE_PersistentPlasma extends StatusEffect {
    public STE_PersistentPlasma() {
        // Although this is a short duration, both BC T5.A "Explosive Goodbye" and EPC OC "Persistent Plasma" apply it constantly within spheres for 4 or 7 seconds
        super(DamageElement.fire, 8, 8, 0.2, 0.25, 0.8, 0.6);
    }
}
