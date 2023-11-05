package drgtools.dpscalc.modelPieces.damage;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.modelPieces.statusEffects.StatusEffect;

import java.util.HashSet;

// Things like Volatile Bullets, Conductive Bullets, Bullets of Mercy, Stunner, etc...
public class ConditionalDamageConversion {
    // Although technically Stun and Fear aren't Status Effects, by modeling them as STEs it allows me to use them as triggers here.
    private HashSet<String> statusEffectNamesThatTrigger;
    private DamageConversion dcToApply;

    public ConditionalDamageConversion(StatusEffect[] triggeringSTEs, DamageConversion dc) {
        statusEffectNamesThatTrigger = new HashSet<>();
        for (StatusEffect ste: triggeringSTEs) {
            statusEffectNamesThatTrigger.add(ste.getName());
        }
        dcToApply = dc;
    }

    public boolean shouldApplyConversion(Enemy target) {
        HashSet<String> targetsCurrentAfflictions = target.getCurrentStatusEffectNames();
        HashSet<String> intersection = new HashSet<>(statusEffectNamesThatTrigger);
        intersection.retainAll(targetsCurrentAfflictions);
        return intersection.size() > 0;
    }

    public DamageConversion getDamageConversion() {
        return dcToApply;
    }

    public String prettyPrint() {
        return prettyPrint(0);
    }
    public String prettyPrint(int indentLevel) {
        String indent = "    ";
        String toReturn = "";

        toReturn += indent.repeat(indentLevel) + "If any of these Status Effects are present on the Enemy:\n";
        for (String steName : statusEffectNamesThatTrigger) {
            toReturn += indent.repeat(indentLevel + 1) + steName + "\n";
        }

        toReturn += indent.repeat(indentLevel) + "Then this DamageConversion will be temporarily applied:\n";
        toReturn += dcToApply.prettyPrint(indentLevel + 1);

        return toReturn;
    }
}
