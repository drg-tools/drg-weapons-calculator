package drgtools.dpscalc.weapons.engineer.breachCutter;

import drgtools.dpscalc.modelPieces.statusEffects.AoEStatusEffect;

public class STE_BreachCutterSlow extends AoEStatusEffect {
    /*
        PRJ movespeed = 10 m/sec
        Grunt length = 2.206m
        Grunt movespeed = 2.9 m/sec * 1.15 Haz5 * 0.3 Breach Cutter

        sec of intersection = 2.206 / (10 + 2.9 * 1.15 * 0.3) = 2.206/11.0005 = 0.2 sec
    */
    public STE_BreachCutterSlow() {
        // TODO: i have no idea how to figure this out right now. tired and distracted. come back tomorrow.
        super(2.2, null, 0, 0, 0.5, 0.5, 0.3, 0.3, 5);
    }
}
