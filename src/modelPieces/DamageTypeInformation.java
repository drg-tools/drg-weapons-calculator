package modelPieces;

public class DamageTypeInformation {
	/*
		Direct Damage
		
		Weapons that deal Direct Damage:
			Every weapon that fires bullets
			Driller - EPC's normal shots
			Driller - Impact Axe
			Engineer - Breach Cutter
	*/
	public static double Direct_Utility = 0;
	
	/*
		Area Damage
		
		Weapons that deal Area Damage:
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
		
		Enemies who resist Area Damage:
			Glyphid Bulk Detonator; -50% damage taken
			Glyphid Dreadnaught: -40% damage taken
		
	*/
	public static double Area_Utility = 0;
	
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
			Scout - Boomstick (Mod Tier 5, White Phosphorous Shells)
		
		Enemies who are weak to Heat Damage:
			Glacial Strata - Glyphid Grunt, Grunt Guard, Grunt Slasher, and Praetorian
			All Mactera
			Q'ronar Shellback and Youngling: +50% damage taken
			Nayaka Trawler: +100% damage taken
		
		Enemies who resist Heat Damage:
			Glyphid Dreadnaught: -50% damage taken
	*/
	public static double Heat_Utility = 0;
	
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
		
		Enemies who are weak to Cold Damage:
			Q'ronar Shellback and Youngling: +70% damage taken, but cannot be Frozen
			Nayaka Trawler: +100% damage taken
			Naedocyte Breeder and all normal Mactera die instantly if Frozen (Mactera Ice Bomber resists cold damage, though)
		
		Enemies who resist Cold Damage:
			Glacial Strata - Glyphid Grunt, Grunt Guard, Grunt Slasher, and Praetorian
	*/
	public static double Cold_Utility = 2;
	
	/*
		Electric Damage
		
		Weapons that deal Electric Damage:
			Engineer - "Stubby" SMG
		
		Enemies who are weak to Electric Damage:
			Huuli Hoarder: +80% damage taken
		
		Enemies who resist Electric Damage:
			Glyphid Dreadnaught: -60% damage taken
	*/
	public static double Electric_Utility = 0;
	
	/*
		Armor Breaking
		
		Most of this information comes from here: https://old.reddit.com/r/DeepRockGalactic/comments/aey0by/how_armor_works_types_mechanics_an_information/
		
		Many enemies have armor on them. There are 3 types of armor:
			1. Damage-Reducing armor (reduces incoming damage by 20%)
			2. Breakable Damage-Immune armor (100 hp per armor plate)
			3. Unbreakable Damage-Immune armor
		
		Damage-Reducing armor ("DR armor") has a chance to break when being damaged by Direct Damage or Area Damage. Any Mods or Overclocks that increase 
		Armor Break will multiplicatively increase that chance without increasing the actual damage dealt. The chance to break Damage-Reducing Armor
		can be approximated by the formula:
			
			Math.Max(0, Math.Min(1, Math.log10(baseDamage * armorBreakMultiplier / DRArmorValue)))
			
		where DRArmorValue is 10 for Glyphid Webspitter and Acidspitter, and 15 for most other enemies like Glyphid Grunt, Grunt Slasher, Warden, etc.
		
		Breakable Damage-Immune armor ("BDI armor"), as the name implies, reduces all damage dealt to 0 until the armor plate is broken off. These armor plates all have 
		their own health bars, at 100 hp each. Any damage over that 100 will still be absorbed by the plate, so it's better to use low-damage bullets
		instead of high-damage grenades, since any overkill damage will be lost. For BDI armor, the Armor Break multiplier is just a straight damage multiplier
		for any damage that hits a BDI armor plate. 300% armor break chance is effectively reducing the BDI armor plates to just 33.3 hp. The only enemy types 
		with BDI armor plates are Glyphid Grunt Guard, Glyphid Praetorian, and Q'ronar Shellback.
		
		Finally, Unbreakable Damage-Immune armor ("UDI armor") is armor that reduces all incoming damage to zero and cannot be broken. Instead, you MUST damage
		the enemy somewhere else, or use Area Damage to bypass it. The only two enemies that have UDI armor are Glyphid Dreadnaught and Glyphid Praetorian Oppressor.
		For both of those enemies, their abdomen can be damaged while the rest of their body is immune.
	*/
	public static double ArmorBreak_Utility = 1;
	
	/*
		Stun
		
		
		One of the more simple mechanics in DRG, Stun quite simply stops an enemy from attacking and moving, making them an easy target for the Stun duration.
		Once an enemy has been stunned, it cannot be re-stunned until it has recoved. As such, "stun-locking" an enemy is impossible. 
		
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
			Glyphid Bulk Detonator
			Glyphid Dreadnaught
			Glyphid Brood Nexus
			BET-C
	*/
	public static double Stun_Utility = 1;
	
	/*
		Fear
		
		Fear makes enemies stop what they're doing, and move away from the Fear location for about 2 seconds. This provides temporary safety for the players.
		
		Weapons that can inflict Fear:
			Driller - Flamethrower (Mod Tier 4, It Burns!)
			Driller - HE Grenade
			Driller - Satchel Charge (Tier 4 upgrade "Big Bang")
			Gunner - Minigun (Mod Tier 5, Aggressive Overheat)
			Gunner - Autocannon (Mod Tier 5, Suppressive Fire)
			Gunner - Sticky Grenade
			Scout - M1000 Classic (Mod Tier 5, Precision Terror)
			Scout - Boomstick (Mod Tier 5, Fear the Boomstick)
			
		Eneies immune to Fear:
			Glyphid Bulk Detonator
			Glyphid Dreadnaught
			Glyphid Brood Nexus
			BET-C
	*/
	public static double Fear_Utility = 2;
	
	/*
		Slow
		
		There are only a few ways to inflict a Slow on enemies. Both the Neurotoxin and Electrocute DoTs apply a slow to the enemy, and the Sticky Flames
		from Driller's Flamethrower can slow enemies passing through with the use of Mod Tier 3 "Sticky Flame Slowdown". Scout's IFG grenades apply a slow, but 
		do not apply an Electrocute DoT. Flying enemies and Dreadnaughts are immune to being slowed.
	*/
	public static double Slow_Utility = 0.5;
}
