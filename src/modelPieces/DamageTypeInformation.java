package modelPieces;

public class DamageTypeInformation {
	/*
		Direct Damage (Bullets, mostly)
	*/
	public static double Direct_Utility = 0;
	
	/*
		Area Damage (Explosions, mostly)
		
		Enemies who resist Area Damage:
			Glyphid Bulk Detonator; -50% damage taken
			Glyphid Dreadnaught: -40% damage taken
		
	*/
	public static double Area_Utility = 0;
	
	/*
		Heat Damage
		
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
		
		Enemies who are weak to Cold Damage:
			Q'ronar Shellback and Youngling: +70% damage taken
			Nayaka Trawler: +100% damage taken
			Naedocyte Breeder and all Mactera except Ice Bomber die instantly if frozen
		
		Enemies who resist Cold Damage:
			Glacial Strata - Glyphid Grunt, Grunt Guard, Grunt Slasher, and Praetorian
	*/
	public static double Cold_Utility = 1;
	
	/*
		Electric Damage
		
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
	*/
	public static double Fear_Utility = 1;
}
