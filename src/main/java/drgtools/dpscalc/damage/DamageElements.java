package drgtools.dpscalc.damage;

public class DamageElements {
	/*
		Group 1: "impact style"
			Melee
			Physical (dealt by enemies' attacks, not players')
			Kinetic
			Piercing
			
		Group 2: "elemental"
			Explosive
			Fire
			Frost
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
	public enum damageElement{melee, physical, kinetic, piercing, explosive, fire, frost, electric, poison, radiation, corrosive, disintegrate, internal};
	public static int numElements =  damageElement.values().length;
	
	public enum temperatureElement{heat, cold};
	
	public static int getElementIndex(damageElement in) {
		switch(in) {
			case melee:
				return 0;
			case physical:
				return 1;
			case kinetic:
				return 2;
			case piercing:
				return 3;
			case explosive:
				return 4;
			case fire:
				return 5;
			case frost:
				return 6;
			case electric:
				return 7;
			case poison:
				return 8;
			case radiation:
				return 9;
			case corrosive:
				return 10;
			case disintegrate:
				return 11;
			case internal:
				return 12;
			default:
				return -1;
		}
	}
}
