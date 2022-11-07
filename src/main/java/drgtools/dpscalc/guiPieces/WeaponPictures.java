package drgtools.dpscalc.guiPieces;

import java.awt.image.BufferedImage;

import drgtools.dpscalc.utilities.ResourceLoader;

public class WeaponPictures {
	private static String pathPrefix = "images";
	
	// Driller
	public static BufferedImage flamethrower = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_FlameThrower.png");
	public static BufferedImage cryoCannon = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_Cryospray.png");
	public static BufferedImage subata = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_Pistol.png");
	public static BufferedImage EPC = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_ChargeBlaster.png");
	
	// Engineer
	public static BufferedImage shotgun = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_CombatShotgun.png");
	public static BufferedImage SMG = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_SMG.png");
	public static BufferedImage grenadeLauncher = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_GrenadeLauncher.png");
	public static BufferedImage breachCutter = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_LineCutter.png");
	
	// Gunner
	public static BufferedImage minigun = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_GatlingGun.png");
	public static BufferedImage autocannon = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_AutoCannon.png");
	public static BufferedImage revolver = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_Revolver.png");
	public static BufferedImage burstPistol = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_BurstPistol.png");
	
	// Scout
	public static BufferedImage assaultRifle = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_AssaultRifle.png");
	public static BufferedImage classic = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_BoltActionRifle.png");
	public static BufferedImage boomstick = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_SawedOffShotgun.png");
	public static BufferedImage zhukovs = ResourceLoader.loadImage(pathPrefix + "/weapons/GearGraphic_DuelMachinePistols_DMP_MK_II.png");
}
