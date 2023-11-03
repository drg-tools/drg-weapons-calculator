package drgtools.dpscalc.enemies;

import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;

import java.util.EnumMap;

// TODO: at some point in the future, this might need to track Radial-type Resistance too?
// Currently only seen on Hiveguard and Caretaker, but the precedent has been set...
public class ElementalResistancesMap {
	private EnumMap<DamageElement, Double> resistances;
	
	public ElementalResistancesMap() {
		resistances = new EnumMap<>(DamageElement.class);
		for (int i = 0; i < DamageElements.numElements; i++) {
			// Technically, this creates four resistances that get unused: fireAndHeat, heat, frostAndCold, and cold.
			// But excluding them would take more code than to just leave them inserted uselessly.
			resistances.put(DamageElements.getElementAtIndex(i), 1.0);
		}
	}
	
	public EnumMap<DamageElement, Double> getResistances() {
		return resistances;
	}
	
	public void setResistance(DamageElement el, double resistance) {
		// -0.5 means they take 150% damage of that element.
		// 0.25 means they take 75% damage of that element.
		resistances.put(el, 1.0 - resistance);
	}
	public double getResistance(DamageElement el) {
		return resistances.get(el);
	}

	public double multiplyDamageByElements(EnumMap<DamageElement, Double> damageByElements) {
		double sum = 0.0;
		DamageElement alias;
		for (int i = 0; i < DamageElements.numElements; i++) {
			alias = DamageElements.getElementAtIndex(i);
			switch (alias) {
				case fireAndHeat: {
					sum += resistances.get(DamageElement.fire) * damageByElements.getOrDefault(alias, 0.0);
					break;
				}
				case heat:
					continue;
				case frostAndCold: {
					sum += resistances.get(DamageElement.frost) * damageByElements.getOrDefault(alias, 0.0);
					break;
				}
				case cold:
					continue;
				default: {
					sum += resistances.get(alias) * damageByElements.getOrDefault(alias, 0.0);
					break;
				}
			}
		}
		return sum;
	}
}
