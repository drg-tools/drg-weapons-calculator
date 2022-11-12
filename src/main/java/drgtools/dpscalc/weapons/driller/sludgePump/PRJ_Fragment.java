package drgtools.dpscalc.weapons.driller.sludgePump;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;
import drgtools.dpscalc.weapons.Projectile;

public class PRJ_Fragment extends Projectile {
    public PRJ_Fragment(DamageComponent dmg) {
        // TODO: investigate if this is affected by AG Mixture, both gravity and velocity
        // PRJ_GooProjectile_Fragment_Normal -> 7 m/sec, 2x gravity
        // PRJ_GooProjectile_Fragment_Base -> 14 m/sec, 4x gravity  -- is this what MeatMorning was seeing when AG Mixture is equipped??? similar to how the T5 DoTs don't get pushed
        super(7, 2, 0.2, dmg);
    }
}
