package modelPieces;

public class UtilityInformation {
	/*
		Direct Damage
		
		Weapons that deal Direct Damage:
			Every weapon that fires bullets
			Driller - EPC's normal shots
			Driller - Impact Axe
			Engineer - Breach Cutter
	*/
	
	/*
		Area Damage
		
		Weapons that deal Area Damage:
			Driller - Subata (OC Explosive Reload)
			Driller - EPC's charged shots
			Driller - Satchel Charge
			Driller - Impact Axe
			Driller - HE Grenade
			Engineer - Grenade Launcher
			Engineer - all throwables
			Gunner - Autocannon
			Gunner - Revolver (Mod Tier 3, Explosive Rounds)
			Gunner - Sticky Grenade
			Gunner - Cluster Grenade
			Scout - Boomstick (Blastwave)
			Scout - Zhukovs (OC Embedded Detonators)
		
	*/
	
	/*
		Heat Damage
		
		As Heat Damage is dealt to an enemy, their Heat Meter increases. Once the Heat Meter is full, a Fire DoT is applied to them.
		Continuing to deal Heat Damage will sustain the Fire DoT.
		
		The Heat Meter is reduced by Cold Damage, and conversely Heat Damage reduces the Cold Meter.
		
		It seems that the Heat Meter can have 3 values: small, medium, or large. Each correspond approximately to enemy size, with small 
		getting lit on Fire immediately, medium taking about a second to ignite, and large taking around four seconds to ignite. (10 Heat on Flamethrower for testing)
		
		Weapons that deal Heat Damage:
			Driller - CRSPR Flamethrower
			Engineer - Grenade Launcher (Mod Tier 3, Incendiary Compound)
			Engineer - Breach Cutter (Overclock, Inferno)
			Gunner - Minigun (Mod Tier 5, Hot Bullets)
			Gunner - Minigun (Mod Tier 5, Aggressive Overheat)
			Gunner - Minigun (Overclock, Burning Hell)
			Gunner - Incendiary Grenade
			Scout - Boomstick (Mod Tier 5, White Phosphorus Shells)
	*/
	
	/*
		Cold Damage
		
		As Cold Damage is dealt to an enemy, their Cold Meter increases. Once the Cold Meter is full, the enemy is Frozen for a few seconds.
		While Frozen, enemies cannot move or attack and take x3 damage from all sources. Additionally, Glyphid Exlpoders, Praetorians, and 
		Bulk Detonators do not use their on-death mechanic if they die while Frozen.
		
		The Cold Meter is reduced by Heat Damage, and conversely Cold Damage reduces the Heat Meter.
		
		It seems that the Cold Meter can have 3 values: small, medium, or large. Each correspond approximately to enemy size, with small 
		getting Frozen immediately, medium taking about a second to Freeze, and large taking around four seconds to Freeze. (5 Cold on Cryo Cannon for testing)
		
		Weapons that deal Cold Damage:
			Driller - Cryo Cannon
			Scout - Zhukov (Overclock, Cryo Minelets)
			Scout - Cryo Grenade
	*/
	public static double Cold_Utility = 0.4;  // It appears that the slow from Cold damage increases as their Cold Meter fills up, from 0% slow at no Cold to 75-85% slowed right before frozen. The average is 40%
	public static double Frozen_Utility = 2.5;  // Not only are Frozen enemies "stunned" but they also take x3 Direct Damage (without getting Weakpoint Bonuses)
	public static double Frozen_Damage_Multiplier = 3;  // Only applies to Direct Damage; not Area Damage or DoTs
	
	/*
		Electric Damage
		
		Weapons that deal Electric Damage:
			Driller - EPC
			Engineer - "Stubby" SMG
			Engineer - Breach Cutter
	*/
	public static double IFG_Damage_Multiplier = 1.3;
	
	/*
		Armor Breaking
		
		Many enemies have armor on them. There are 3 types of armor:
			1. Light Armor (reduces incoming Direct Damage by 20%)
			2. Heavy Armor (100 or 150 hp per armor plate)
			3. Unbreakable Armor
		
		Light Armor has a chance to break when being damaged by Direct Damage. Any Mods or Overclocks that increase 
		Armor Break will increase that chance without increasing the actual damage dealt. 
		
		Heavy armor reduces all Direct Damage dealt to 0 until the armor plate is broken off. These armor plates all have 
		their own health bars, at 100 or 150 hp each. Any damage over that 100 will still be absorbed by the plate, so it's better to use low-damage bullets
		instead of high-damage grenades, since any overkill damage will be lost. For Heavy armor, the Armor Break multiplier is just a straight damage multiplier
		for any damage that hits a Heavy Armor plate. 300% armor break chance is effectively reducing the Heavy armor plates from 100 to just 33.3 hp. The only enemy types 
		with Heavy Armor plates are Glyphid Grunt Guard, Glyphid Praetorian, and Q'ronar Shellback.
		
		Finally, Unbreakable Armor is armor that reduces all incoming damage to zero and cannot be broken. Instead, you MUST damage
		the enemy somewhere else, or use Area Damage to bypass it. The only two enemies that have UDI armor are Glyphid Dreadnought and Glyphid Oppressor.
		For both of those enemies, their abdomen can be damaged while the rest of their body is immune.
	*/
	public static double ArmorBreak_Utility = 1.0;
	// Technically Q'ronar Younglings have Light Armor that reduces incoming damage by 50%, but they're not modeled in this program.
	public static double LightArmor_DamageReduction = 0.8;
	
	/*
		Stun
		
		One of the more simple mechanics in DRG, Stun quite simply stops an enemy from attacking and moving, making them an easy target for the Stun duration.
		Once an enemy has been stunned, it cannot be re-stunned until it has recovered. As such, "stun-locking" an enemy is impossible. 
		
		Weapons that can Stun enemies:
			Driller - Subata (Overclock, Tranquilizing Rounds)
			Driller - Satchel Charge (Tier 4 upgrade "Stun")
			Engineer - Shotgun (baseline, but can be improved by Mod Tier 4 "Stun Duration")
			Engineer - Grenade Launcher (Mod Tier 4, Concussive Blast)
			Gunner - Minigun (baseline, but can be improved by Mod Tier 3 "Stun Duration")
			Gunner - Burst Pistol (Mod Tier 5, Burst Stun)
			Gunner - Cluster Grenade
			Scout - Deepcore AR (baseline, but can be improved by Mod Tier 5 "Stun")
			Scout - M1000 Classic (Mod Tier 5, Hitting Where It Hurts)
			Scout - Boomstick (baseline, but can be improved by Mod Tier 3 "Stun Duration")
		
		Enemies who resist Stun:
			Glyphid Praetorian
			Glyphid Grunt Guard
			
		Enemies who are immune to Stun:
			Glyphid Oppressor
			Glyphid Bulk Detonator
			Glyphid Dreadnought
			Glyphid Brood Nexus
			Naedocyte Breeder
			Spitball Infector
			BET-C
	*/
	public static double Stun_Utility = 1.5;
	
	/*
		Fear
		
		Fear makes enemies stop what they're doing, and move away from the Fear location for about 2 seconds. This provides temporary safety for the players.
		
		All enemies have a Courage value. For the vast majority, it's set to 0.0, but these are the exceptions:
		
		Courage values:
		
				Praetorian 0.5
				Acid Spitter 0.3
				Web Spitter 0.3
				Grunt 0.5
				Slasher 0.5
				Guard 0.5
				Warden 0.5
				Huuli Hoarder 0.5
				Dreadnought 1.0
				Bulk Detonator 1.0
				Oppressor 100?!
				Menace 0.7
				
		The % chance that Fear will be inflicted on enemies uses the following formula:
		
			% Proc = Math.min( (1.0 - Courage) * Fear Factor, 1.0)
		
		So, in theory, a Fear Factor of 3.34 would be enough to 100% proc Fear on a Glyphid Menace. Anything with Fear Factor 0.5 would fear Grunts and Praetorians with a probability of 0.25.
		
		The duration of the Fear status effect is different per enemy type, according to MikeGSG. He hasn't confirmed this yet, but I'm under the impression that ground enemies have to walk a certain
		distance away from the point of fear before it ends, so their movespeed affects the Fear duration. If that's true, then Slows would extend the Fear, and higher Hazard levels would shorten the Fear.
		
		Weapons that can inflict Fear:
			Driller - Flamethrower (Mod Tier 4, It Burns!)
			Driller - HE Grenade
			Driller - Satchel Charge (Tier 4 upgrade "Big Bang")
			Engineer - Grenade Launcher (default behavior)
			Engineer - Proximity Mines (default behavior)
			Gunner - Minigun (Mod Tier 5, Aggressive Venting)
			Gunner - Autocannon (Mod Tier 5, Suppressive Fire)
			Gunner - Sticky Grenade
			Scout - M1000 Classic (Mod Tier 5, Precision Terror)
			Scout - Boomstick (Mod Tier 5, Fear the Boomstick)
			
		Enemies immune to Fear:
			Glyphid Oppressor
			Glyphid Bulk Detonator
			Glyphid Dreadnaught
			BET-C
	*/
	public static double Fear_Utility = 0.75;
	
	/*
		Slow
		
		There are only a few ways to inflict a Slow on enemies. Both the Neurotoxin and Electrocute DoTs apply a slow to the enemy, and the Sticky Flames
		from Driller's Flamethrower can slow enemies. Scout's IFG grenades apply a 75% slow, but do not apply an Electrocute DoT. Flying enemies and 
		Dreadnoughts are immune to being slowed.
		
		Neurotoxin = 30% slow
		Electrocute = 80% slow
	*/
	public static double Neuro_Slow_Utility = 0.3;
	public static double Electrocute_Slow_Utility = 0.8;
	
	/*
 		Mobility
 		
 		Technically neither damage type nor status effect, but still a category of Utility that needs to be covered. There are two main categories of Mobility:
 		buffs or debuffs to your dwarf's walking speed, or "blast jumping" which uses its own projectile physics and applies its own velocity to the dwarf independent 
 		of walking.
	*/
	public static double Movespeed_Utility = 2;
	public static double BlastJump_Utility = 1;
	
	/*
		Damage Resistance
		
		This is a pretty rare effect in DRG right now. While active, it increases a Dwarf's Effective Health Pool (EHP) and makes them harder to kill. As it stands, 
		a 30% Damage Resistance gives a score of 1.42857, which is a little too low in comparison to other Utility scores.
	*/
	public static double DamageResist_Utility = 2;
}
