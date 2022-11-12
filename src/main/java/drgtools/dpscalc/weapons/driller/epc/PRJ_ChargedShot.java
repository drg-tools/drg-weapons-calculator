package drgtools.dpscalc.weapons.driller.epc;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_ChargedShot extends Projectile {
    public PRJ_ChargedShot(DamageComponent dmg) {
        super(13, 0, 0.5, dmg);
    }
}
