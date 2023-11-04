package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

/*
    Also like Stun, this isn't a Status Effect in-game but it's going to be easier to model Fear's effect on making
    enemies move faster out of AoE Status Effects as if it's "slowing" them by x1.5 while they're within the 10m flee
    distance. Really hoping that this is simpler, and that it works as intended. This will be really messy if Stun and
    Fear try to get applied simultaneously...
*/
public class STE_Fear extends AoEStatusEffect {
    public STE_Fear() {
        super(10, null, 0, 0, 0.5, 0.5, 1.5, 0.5, 15);
    }
}
