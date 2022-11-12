package drgtools.dpscalc.weapons.driller.cryoCannon;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_IceSpear extends Projectile {
    public PRJ_IceSpear() {
        super(50, 50, 1.0, 0.2, null);
        DamageComponent dmg = new DamageComponent(350, DamageElement.kinetic, 150, DamageElement.explosive, 0.8, 1.4, 0.2, 1.0 , 0.5, null);
        dmg.setStun(false, 1.0, 3.0);
        dmgComponent = dmg;
    }
}
