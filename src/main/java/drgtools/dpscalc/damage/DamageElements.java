package drgtools.dpscalc.damage;

public class DamageElements {
	/*
		Group 1: "impact style"
			Melee
			Kinetic
			Piercing
			
		Group 2: "elemental"
			Fire
			Frost
			Explosive
			Electric
			Poison
			Radiation
			Corrosive
			
		Group 3: "niche"
			Disintegrate
			Internal
			
		Group 4: "temperature" (technically damage types in-game, but separated to Temperature-type for easier coding)
			Heat
			Cold
	*/
	public enum damageElement{melee, kinetic, piercing, fire, frost, explosive, electric, poison, radiation, corrosive, disintegrate, internal};
	public static int numElements =  damageElement.values().length;
	
	public enum temperatureElement{heat, cold};
	
	public static int getElementIndex(damageElement in) {
		switch(in) {
			case melee:
				return 0;
			case kinetic:
				return 1;
			case piercing:
				return 2;
			case fire:
				return 3;
			case frost:
				return 4;
			case explosive:
				return 5;
			case electric:
				return 6;
			case poison:
				return 7;
			case radiation:
				return 8;
			case corrosive:
				return 9;
			case disintegrate:
				return 10;
			case internal:
				return 11;
			default:
				return -1;
		}
	}
}
