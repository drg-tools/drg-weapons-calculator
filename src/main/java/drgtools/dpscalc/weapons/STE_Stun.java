package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

/*
    Technically, this isn't a Status Effect in-game. However, to do the math right for increasing damage dealt to enemies
    stunned within an AoE Status Effect, it's probably going to be easier to pretend that it is one and that it just
    multiplies their movespeed by 0 for however long the duration is.
*/
public class STE_Stun extends StatusEffect {
    public STE_Stun(double duration) {
        super(0.0, duration);
    }

    @Override
    public String getName() {
        return "STE_Stun";
    }
}
