package drgtools.dpscalc.enemies;

import drgtools.dpscalc.modelPieces.damage.DamageElements;
import drgtools.dpscalc.modelPieces.damage.DamageElements.DamageElement;

// TODO: at some point in the future, this might need to track Radial-type Resistance too?
// Currently only seen on Hiveguard and Caretaker, but the precedent has been set...
public class ElementalResistancesArray {
	// TODO: EnumMap
	private double[] resistances;
	
	public ElementalResistancesArray() {
		resistances = new double[DamageElements.numElements];
		for (int i = 0; i < resistances.length; i++) {
			resistances[i] = 1.0;
		}
	}
	
	public double[] getResistances() {
		return resistances;
	}
	
	public void setResistance(DamageElement el, double resistance) {
		// -0.5 means they take 150% damage of that element.
		// 0.25 means they take 75% damage of that element.
		resistances[DamageElements.getElementIndex(el)] = 1.0 - resistance;
	}
	public double getResistance(DamageElement el) {
		return resistances[DamageElements.getElementIndex(el)];
	}
}
