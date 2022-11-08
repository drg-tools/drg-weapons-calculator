package drgtools.dpscalc.enemies;

import drgtools.dpscalc.damage.DamageElements;
import drgtools.dpscalc.damage.DamageElements.damageElement;

public class ElementalResistancesArray {
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
	
	public void setResistance(damageElement el, double resistance) {
		// -0.5 means they take 150% damage of that element.
		// 0.25 means they take 75% damage of that element.
		resistances[DamageElements.getElementIndex(el)] = 1.0 - resistance;
	}
	public double getResistance(damageElement el) {
		return resistances[DamageElements.getElementIndex(el)];
	}
}
