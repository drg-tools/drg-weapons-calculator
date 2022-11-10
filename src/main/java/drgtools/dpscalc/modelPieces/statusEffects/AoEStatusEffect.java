package drgtools.dpscalc.modelPieces.statusEffects;

// TODO
// For things like Persistent Plasma, Fat Boy, Sticky Flames, Coilgun trail, etc. Any STE that has a short duration but
// gets re-applied frequently as long as enemies stay within the Area of Effect
// Needs to know the average speed of enemies, scales with Hazard Level
public class AoEStatusEffect extends StatusEffect {
    public AoEStatusEffect() {
        super(0, 0, 0, 0);
    }
}
