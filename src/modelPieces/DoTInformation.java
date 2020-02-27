package modelPieces;

/*
	This class will be a standardized location for all Damage Over Time mechanics that can be applied
	by DRG weapons. Most of these numbers will be educated guesses based on personal testing, since the 
	Wiki doesn't have very much information about these mechanics. For all of these DoTs, they cannot be 
	stacked on themselves (e.g. 2 Electrocute DoTs and 3 Fire DoTs), but they do stack with each other.
	If a second DoT of the same type would be applied before the DoT duration is done, its duration is refreshed 
	instead.
	
	Wiki page just added: https://deeprockgalactic.gamepedia.com/Status_Effects
*/
public class DoTInformation {
	/*
		Neurotoxin
		
		Weapons that can apply this DoT:
			Driller - Neurotoxin Grenade (AoE gas cloud, 100% chance to proc. +15 sec duration, I think?)
			Gunner - "Bulldog" Revolver (Mod Tier 5, Glyphid Neurotoxin Coating, 50% chance to proc)
			Gunner - "Thunderhead" Autocannon (Overclock, Neurotoxin Payload, 20% chance to proc)
	*/
	public static double Neuro_DmgPerTick = 12;
	public static double Neuro_TicksPerSec = 1.5;  // 3 ticks over 2 seconds
	public static double Neuro_SecsDuration = 10;
	public static double Neuro_DPS = Neuro_DmgPerTick * Neuro_TicksPerSec;
	
	/*
		Electrocution
		
		Weapons that can apply this DoT:
			Engineer - "Stubby" SMG (built into weapon, proc chance ranges from 5 - 30% depending on Mods and OC)
			Engineer - Breach Cutter (Overclock, High Voltage Crossover, 100% chance to proc?)
			Gunner - Burst Pistol (Overclock, Electro Minelets, 100% chance to proc)
			Scout - Deepcore AR (Overclock, Electrifying Reload, 5% proc chance/bullet hit upon reload)
			Scout - M1000 Classic (Overclock, Electrocuting Focus Shots, 100% chance to proc on focused shots)
			All dwarves - Armor (Tier 4, Static Discharge, around 50% chance to proc?)
			Bosco - Tier 5 upgrade, Overcharged Rounds, 30% chance to proc
	*/
	public static double Electro_DmgPerTick = 3;
	public static double Electro_TicksPerSec = 4;
	public static double Electro_SecsDuration = 4;
	public static double Electro_DPS = Electro_DmgPerTick * Electro_TicksPerSec;
	
	/*
		Fire
		 
		Not an RNG proc. Instead there's a "heat" meter per enemy that once filled will apply the fire DoT to them.
		
		Weapons that can apply this DoT:
			Driller - CRSPR Flamethrower (built into weapon)
			Engineer - Grenade Launcher (Mod Tier 3, Incendiary Compound)
			Engineer - Breach Cutter (Overclock, Inferno)
			Gunner - Minigun (Mod Tier 5, Hot Bullets)
			Gunner - Minigun (Mod Tier 5, Aggressive Overheat)
			Gunner - Minigun (Overclock, Burning Hell)
			Gunner - Incendiary Grenade
			Scout - Boomstick (Mod Tier 5, White Phosphorous Shells)
	*/
	public static double Fire_DmgPerTick = 5;
	public static double Fire_TicksPerSec = 4;
	// Burn DoT durations are specific per enemy, but it averages around 5 seconds.
	public static double Fire_SecsDuration = EnemyInformation.averageBurnDuration();
	public static double Fire_DPS = Fire_DmgPerTick * Fire_TicksPerSec;
	
	/*
		Radiation
		
		Not an RNG proc. Once a radioactive field is applied, any enemy within the field will take the DoT until they leave. It looks like 
		Radioactive Praetorians and Exploders leave behind Radioactive fields for 4-5 seconds on death.
		
		Weapons that can apply this DoT:
			Engineer - Grenade Launcher (Overclock, Fat Boy, 10-12 sec field duration?)
	*/
	public static double Rad_DmgPerTick = 6;
	public static double Rad_TicksPerSec = 1.5;  // 3 ticks over 2 seconds
	public static double Rad_DPS = Rad_DmgPerTick * Rad_TicksPerSec;
	
	// Cryo is another elemental damage type, but has no DoT associated. Instead, enemies who have their "cold" meter filled become frozen in place.
}
