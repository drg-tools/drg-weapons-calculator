package drgtools.dpscalc.modelPieces.damage;

public class DamageElements {
	/*
		Group 1: "impact"
			Melee
			Kinetic
			Piercing
			
		Group 2: "elemental"
			Fire + Heat
			Fire
			Heat
			Frost + Cold
			Frost
			Cold
			Explosive
			Electric
			Poison
			Radiation
			Corrosive
			
		Group 3: "niche"
			Disintegrate
			Internal
	*/
	// TODO: do I need to add Typeless, for Subata T5.C NCTC explosion?
	public enum DamageElement {melee, kinetic, piercing, fireAndHeat, fire, heat, frostAndCold, frost, cold, explosive, electric, poison, radiation, corrosive, disintegrate, internal}
	public static int numElements = DamageElement.values().length;
	
	public static int getElementIndex(DamageElement in) {
		switch(in) {
			case melee:
				return 0;
			case kinetic:
				return 1;
			case piercing:
				return 2;
			case fireAndHeat:
				return 3;
			case fire:
				return 4;
			case heat:
				return 5;
			case frostAndCold:
				return 6;
			case frost:
				return 7;
			case cold:
				return 8;
			case explosive:
				return 9;
			case electric:
				return 10;
			case poison:
				return 11;
			case radiation:
				return 12;
			case corrosive:
				return 13;
			case disintegrate:
				return 14;
			case internal:
				return 15;
			default:
				return -1;
		}
	}
	public static DamageElement getElementAtIndex(int in) {
		switch(in) {
			case 0:
				return DamageElement.melee;
			case 1:
				return DamageElement.kinetic;
			case 2:
				return DamageElement.piercing;
			case 3:
				return DamageElement.fireAndHeat;
			case 4:
				return DamageElement.fire;
			case 5:
				return DamageElement.heat;
			case 6:
				return DamageElement.frostAndCold;
			case 7:
				return DamageElement.frost;
			case 8:
				return DamageElement.cold;
			case 9:
				return DamageElement.explosive;
			case 10:
				return DamageElement.electric;
			case 11:
				return DamageElement.poison;
			case 12:
				return DamageElement.radiation;
			case 13:
				return DamageElement.corrosive;
			case 14:
				return DamageElement.disintegrate;
			case 15:
				return DamageElement.internal;
			default:
				return null;
		}
	}
	public static String prettyPrint(DamageElement in) {
		switch(in) {
			case melee:
				return "Melee-element";
			case kinetic:
				return "Kinetic-element";
			case piercing:
				return "Piercing-element";
			case fireAndHeat:
				return "Fire-element & Heat";
			case fire:
				return "Fire-element";
			case heat:
				return "Heat";
			case frostAndCold:
				return "Frost-element & Cold";
			case frost:
				return "Frost-element";
			case cold:
				return "Cold";
			case explosive:
				return "Explosive-element";
			case electric:
				return "Electric-element";
			case poison:
				return "Poison-element";
			case radiation:
				return "Radiation-element";
			case corrosive:
				return "Corrosive-element";
			case disintegrate:
				return "Disintegrate-element";
			case internal:
				return "Internal-element";
			default:
				return "";
		}
	}
}
