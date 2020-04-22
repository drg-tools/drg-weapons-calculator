package guiPieces;

import java.awt.image.BufferedImage;

import utilities.ResourceLoader;

public class ButtonIcons {
	// Start by loading all of the images once at the start, so that they don't have to be loaded every time repaint() gets called.
	private static BufferedImage baseSpreadWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Aim.png");
	private static BufferedImage magSizeWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_ClipSize.png");
	private static BufferedImage reloadSpeedWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Speed.png");
	private static BufferedImage carriedAmmoWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Ammo.png");
	private static BufferedImage directDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_DamageGeneral.png");
	private static BufferedImage recoilWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Recoil.png");
	private static BufferedImage weakpointBonusWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Weakspot.png");
	private static BufferedImage heatDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Heat.png");
	private static BufferedImage specialStarWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Special.png");
	private static BufferedImage aoeRadiusWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Area.png");
	private static BufferedImage projectileVelocityWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_ProjectileSpeed.png");
	private static BufferedImage areaDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Area_Damage.png");
	private static BufferedImage fuelWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Fuel.png");
	private static BufferedImage chargeSpeedWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_ChargeUp.png");
	private static BufferedImage coolingRateWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_TemperatureCoolDown.png");
	private static BufferedImage ricochetWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Ricoshet.png");
	private static BufferedImage rateOfFireWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_FireRate.png");
	private static BufferedImage pelletsPerShotWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Shotgun_Pellet.png");
	private static BufferedImage armorBreakingWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_ArmorBreaking.png");
	private static BufferedImage electricityWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Electricity.png");
	private static BufferedImage homebrewPowderWhite = ResourceLoader.loadImage("images/mod/white/Icon_Overclock_ChanceOfHigherDamage.png");
	private static BufferedImage stunWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Stun.png");
	private static BufferedImage blowthroughWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_BulletPenetration.png");
	private static BufferedImage addedExplosionWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Explosion.png");
	private static BufferedImage fearWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_ScareEnemies.png");
	private static BufferedImage damageResistanceWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Resistance.png");
	private static BufferedImage neurotoxinWhite = ResourceLoader.loadImage("images/mod/white/Icon_Overclock_Neuro.png");
	private static BufferedImage movespeedWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Speed.png");
	/*
	private static BufferedImage coldDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Cold.png");
	private static BufferedImage distanceWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Distance.png");
	private static BufferedImage durationWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Duration.png");
	*/
	
	private static BufferedImage baseSpreadBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Aim_Black.png");
	
	// Use a large enum variable to keep track of which icon each Mod or OC needs
	public enum drgIcons {
		baseSpread,
		magSize,
		reloadSpeed,
		carriedAmmo,
		directDamage,
		recoil,
		weakpointBonus,
		heatDamage,
		special,
		aoeRadius,
		projectileVelocity,
		areaDamage,
		fuel,
		chargeSpeed,
		coolingRate,
		ricochet,
		rateOfFire,
		pelletsPerShot,
		armorBreaking,
		electricity,
		homebrewPowder,
		stun,
		blowthrough,
		addedExplosion,
		fear,
		damageResistance,
		neurotoxin,
		movespeed
	};
	
	public static BufferedImage getModIcon(drgIcons iconSelection, boolean getBlackVersion) {
		switch (iconSelection) {
			case baseSpread: {
				if (getBlackVersion) {
					return baseSpreadBlack;
				}
				else {
					return baseSpreadWhite;
				}
			}
			case magSize: {
				return magSizeWhite;
			}
			case reloadSpeed: {
				return reloadSpeedWhite;
			}
			case carriedAmmo: {
				return carriedAmmoWhite;
			}
			case directDamage: {
				return directDamageWhite;
			}
			case recoil: {
				return recoilWhite;
			}
			case weakpointBonus: {
				return weakpointBonusWhite;
			}
			case heatDamage: {
				return heatDamageWhite;
			}
			case special: {
				return specialStarWhite;
			}
			case aoeRadius: {
				return aoeRadiusWhite;
			}
			case projectileVelocity: {
				return projectileVelocityWhite;
			}
			case areaDamage: {
				return areaDamageWhite;
			}
			case fuel: {
				return fuelWhite;
			}
			case chargeSpeed: {
				return chargeSpeedWhite;
			}
			case coolingRate: {
				return coolingRateWhite;
			}
			case ricochet: {
				return ricochetWhite;
			}
			case rateOfFire: {
				return rateOfFireWhite;
			}
			case pelletsPerShot: {
				return pelletsPerShotWhite;
			}
			case armorBreaking: {
				return armorBreakingWhite;
			}
			case electricity: {
				return electricityWhite;
			}
			case homebrewPowder: {
				return homebrewPowderWhite;
			}
			case stun: {
				return stunWhite;
			}
			case blowthrough: {
				return blowthroughWhite;
			}
			case addedExplosion: {
				return addedExplosionWhite;
			}
			case fear: {
				return fearWhite;
			}
			case damageResistance: {
				return damageResistanceWhite;
			}
			case neurotoxin: {
				return neurotoxinWhite;
			}
			case movespeed: {
				return movespeedWhite;
			}
			default: {
				return null;
			}
		}
	}
}
