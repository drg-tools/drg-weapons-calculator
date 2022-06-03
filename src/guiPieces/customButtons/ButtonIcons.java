package guiPieces.customButtons;

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
	private static BufferedImage movespeedWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_MovementSpeed.png");
	private static BufferedImage coldDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Cold.png");
	private static BufferedImage distanceWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Distance.png");
	private static BufferedImage durationWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Duration.png");
	private static BufferedImage slowdownWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Sticky.png");
	private static BufferedImage hourglassWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Duration_V2.png");
	private static BufferedImage specialReloadWhite = ResourceLoader.loadImage("images/mod/white/Icon_Overclock_ExplosiveReload.png");
	private static BufferedImage angleWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Angle.png");
	private static BufferedImage lightWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Light.png");
	private static BufferedImage acidWhite = ResourceLoader.loadImage("images/mod/white/Icon_Acid.png");
	private static BufferedImage numTargetsWhite = ResourceLoader.loadImage("images/mod/white/Icon_Upgrade_Arperture_Extension.png");
	private static BufferedImage lastShotDamageWhite = ResourceLoader.loadImage("images/mod/white/Icon_Overclock_LastShellHigherDamage.png");
	
	private static BufferedImage baseSpreadBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Aim_Black.png");
	private static BufferedImage magSizeBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_ClipSize_Black.png");
	private static BufferedImage reloadSpeedBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Speed_Black.png");
	private static BufferedImage carriedAmmoBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Ammo_Black.png");
	private static BufferedImage directDamageBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_DamageGeneral_Black.png");
	private static BufferedImage recoilBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Recoil_Black.png");
	private static BufferedImage weakpointBonusBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Weakspot_Black.png");
	private static BufferedImage heatDamageBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Heat_Black.png");
	private static BufferedImage specialStarBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Special_Black.png");
	private static BufferedImage aoeRadiusBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Area_Black.png");
	private static BufferedImage projectileVelocityBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_ProjectileSpeed_Black.png");
	private static BufferedImage areaDamageBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Area_Damage_Black.png");
	private static BufferedImage fuelBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Fuel_Black.png");
	private static BufferedImage chargeSpeedBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_ChargeUp_Black.png");
	private static BufferedImage coolingRateBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_TemperatureCoolDown_Black.png");
	private static BufferedImage ricochetBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Ricoshet_Black.png");
	private static BufferedImage rateOfFireBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_FireRate_Black.png");
	private static BufferedImage pelletsPerShotBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Shotgun_Pellet_Black.png");
	private static BufferedImage armorBreakingBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_ArmorBreaking_Black.png");
	private static BufferedImage electricityBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Electricity_Black.png");
	private static BufferedImage homebrewPowderBlack = ResourceLoader.loadImage("images/mod/black/Icon_Overclock_ChanceOfHigherDamage_Black.png");
	private static BufferedImage stunBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Stun_Black.png");
	private static BufferedImage blowthroughBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_BulletPenetration_Black.png");
	private static BufferedImage addedExplosionBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Explosion_Black.png");
	private static BufferedImage fearBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_ScareEnemies_Black.png");
	private static BufferedImage damageResistanceBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Resistance_Black.png");
	private static BufferedImage neurotoxinBlack = ResourceLoader.loadImage("images/mod/black/Icon_Overclock_Neuro_Black.png");
	private static BufferedImage movespeedBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_MovementSpeed_Black.png");
	private static BufferedImage coldDamageBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Cold_Black.png");
	private static BufferedImage distanceBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Distance_Black.png");
	private static BufferedImage durationBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Duration_Black.png");
	private static BufferedImage slowdownBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Sticky_Black.png");
	private static BufferedImage hourglassBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Duration_V2_Black.png");
	private static BufferedImage specialReloadBlack = ResourceLoader.loadImage("images/mod/black/Icon_Overclock_ExplosiveReload_Black.png");
	private static BufferedImage angleBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Angle_Black.png");
	private static BufferedImage lightBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Light_Black.png");
	private static BufferedImage acidBlack = ResourceLoader.loadImage("images/mod/black/Icon_Acid_Black.png");
	private static BufferedImage numTargetsBlack = ResourceLoader.loadImage("images/mod/black/Icon_Upgrade_Arperture_Extension_Black.png");
	private static BufferedImage lastShotDamageBlack = ResourceLoader.loadImage("images/mod/black/Icon_Overclock_LastShellHigherDamage_Black.png");
	
	// This gets used by StatsRow when there's no associated picture.
	private static BufferedImage blank = ResourceLoader.loadImage("images/mod/blank.png");
	
	public static BufferedImage cleanFrame = ResourceLoader.loadImage("images/overclock/Frame_Overclock_Clean.png");
	public static BufferedImage balancedFrame = ResourceLoader.loadImage("images/overclock/Frame_Overclock_Balanced.png");
	public static BufferedImage unstableFrame = ResourceLoader.loadImage("images/overclock/Frame_Overclock_Unstable.png");
	private static BufferedImage minishells = ResourceLoader.loadImage("images/overclock/Icon_Overclock_SmallBullets.png");
	private static BufferedImage grenadeJump = ResourceLoader.loadImage("images/overclock/Icon_Overclock_ExplosionJump.png");
	private static BufferedImage hoverclock = ResourceLoader.loadImage("images/overclock/Icon_Overclock_Hoverclock.png");
	private static BufferedImage shotgunJump = ResourceLoader.loadImage("images/overclock/Icon_Overclock_ShotgunJump.png");
	private static BufferedImage breachCutterRoll = ResourceLoader.loadImage("images/overclock/Icon_Overclock_Spinning_Linecutter.png");
	private static BufferedImage breachCutterReturn = ResourceLoader.loadImage("images/overclock/Icon_Overclock_ForthAndBack_Linecutter.png");
	private static BufferedImage numPellets2 = ResourceLoader.loadImage("images/overclock/Icon_Upgrade_Shotgun_Pellet2.png");
	
	private static BufferedImage statusFire = ResourceLoader.loadImage("images/statusEffect/Status_fire.png");
	private static BufferedImage statusFrozen = ResourceLoader.loadImage("images/statusEffect/Status_frozen.png");
	private static BufferedImage statusElectricity = ResourceLoader.loadImage("images/statusEffect/Status_electricity.png");
	
	// Use a large enum variable to keep track of which icon each Mod or OC needs
	public enum modIcons {
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
		movespeed,
		coldDamage,
		distance,
		duration,
		slowdown,
		hourglass,
		blank,
		specialReload,
		angle,
		light,
		acid,
		numTargets,
		lastShotDamage
	};
	
	public static BufferedImage getModIcon(modIcons iconSelection, boolean getBlackVersion) {
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
				if (getBlackVersion) {
					return magSizeBlack;
				}
				else {
					return magSizeWhite;
				}
			}
			case reloadSpeed: {
				if (getBlackVersion) {
					return reloadSpeedBlack;
				}
				else {
					return reloadSpeedWhite;
				}
			}
			case carriedAmmo: {
				if (getBlackVersion) {
					return carriedAmmoBlack;
				}
				else {
					return carriedAmmoWhite;
				}
			}
			case directDamage: {
				if (getBlackVersion) {
					return directDamageBlack;
				}
				else {
					return directDamageWhite;
				}
			}
			case recoil: {
				if (getBlackVersion) {
					return recoilBlack;
				}
				else {
					return recoilWhite;
				}
			}
			case weakpointBonus: {
				if (getBlackVersion) {
					return weakpointBonusBlack;
				}
				else {
					return weakpointBonusWhite;
				}
			}
			case heatDamage: {
				if (getBlackVersion) {
					return heatDamageBlack;
				}
				else {
					return heatDamageWhite;
				}
			}
			case special: {
				if (getBlackVersion) {
					return specialStarBlack;
				}
				else {
					return specialStarWhite;
				}
			}
			case aoeRadius: {
				if (getBlackVersion) {
					return aoeRadiusBlack;
				}
				else {
					return aoeRadiusWhite;
				}
			}
			case projectileVelocity: {
				if (getBlackVersion) {
					return projectileVelocityBlack;
				}
				else {
					return projectileVelocityWhite;
				}
			}
			case areaDamage: {
				if (getBlackVersion) {
					return areaDamageBlack;
				}
				else {
					return areaDamageWhite;
				}
			}
			case fuel: {
				if (getBlackVersion) {
					return fuelBlack;
				}
				else {
					return fuelWhite;
				}
			}
			case chargeSpeed: {
				if (getBlackVersion) {
					return chargeSpeedBlack;
				}
				else {
					return chargeSpeedWhite;
				}
			}
			case coolingRate: {
				if (getBlackVersion) {
					return coolingRateBlack;
				}
				else {
					return coolingRateWhite;
				}
			}
			case ricochet: {
				if (getBlackVersion) {
					return ricochetBlack;
				}
				else {
					return ricochetWhite;
				}
			}
			case rateOfFire: {
				if (getBlackVersion) {
					return rateOfFireBlack;
				}
				else {
					return rateOfFireWhite;
				}
			}
			case pelletsPerShot: {
				if (getBlackVersion) {
					return pelletsPerShotBlack;
				}
				else {
					return pelletsPerShotWhite;
				}
			}
			case armorBreaking: {
				if (getBlackVersion) {
					return armorBreakingBlack;
				}
				else {
					return armorBreakingWhite;
				}
			}
			case electricity: {
				if (getBlackVersion) {
					return electricityBlack;
				}
				else {
					return electricityWhite;
				}
			}
			case homebrewPowder: {
				if (getBlackVersion) {
					return homebrewPowderBlack;
				}
				else {
					return homebrewPowderWhite;
				}
			}
			case stun: {
				if (getBlackVersion) {
					return stunBlack;
				}
				else {
					return stunWhite;
				}
			}
			case blowthrough: {
				if (getBlackVersion) {
					return blowthroughBlack;
				}
				else {
					return blowthroughWhite;
				}
			}
			case addedExplosion: {
				if (getBlackVersion) {
					return addedExplosionBlack;
				}
				else {
					return addedExplosionWhite;
				}
			}
			case fear: {
				if (getBlackVersion) {
					return fearBlack;
				}
				else {
					return fearWhite;
				}
			}
			case damageResistance: {
				if (getBlackVersion) {
					return damageResistanceBlack;
				}
				else {
					return damageResistanceWhite;
				}
			}
			case neurotoxin: {
				if (getBlackVersion) {
					return neurotoxinBlack;
				}
				else {
					return neurotoxinWhite;
				}
			}
			case movespeed: {
				if (getBlackVersion) {
					return movespeedBlack;
				}
				else {
					return movespeedWhite;
				}
			}
			case coldDamage: {
				if (getBlackVersion) {
					return coldDamageBlack;
				}
				else {
					return coldDamageWhite;
				}
			}
			case distance: {
				if (getBlackVersion) {
					return distanceBlack;
				}
				else {
					return distanceWhite;
				}
			}
			case duration: {
				if (getBlackVersion) {
					return durationBlack;
				}
				else {
					return durationWhite;
				}
			}
			case slowdown: {
				if (getBlackVersion) {
					return slowdownBlack;
				}
				else {
					return slowdownWhite;
				}
			}
			case hourglass: {
				if (getBlackVersion) {
					return hourglassBlack;
				}
				else {
					return hourglassWhite;
				}
			}
			case blank: {
				return blank;
			}
			case specialReload: {
				if (getBlackVersion) {
					return specialReloadBlack;
				}
				else {
					return specialReloadWhite;
				}
			}
			case angle: {
				if (getBlackVersion) {
					return angleBlack;
				}
				else {
					return angleWhite;
				}
			}
			case light: {
				if (getBlackVersion) {
					return lightBlack;
				}
				else {
					return lightWhite;
				}
			}
			case acid: {
				if (getBlackVersion) {
					return acidBlack;
				}
				else {
					return acidWhite;
				}
			}
			case numTargets: {
				if (getBlackVersion) {
					return numTargetsBlack;
				}
				else {
					return numTargetsWhite;
				}
			}
			case lastShotDamage: {
				if (getBlackVersion) {
					return lastShotDamageBlack;
				}
				else {
					return lastShotDamageWhite;
				}
			}
			default: {
				return null;
			}
		}
	}
	
	public enum overclockIcons {
		baseSpread,
		magSize,
		ricochet,
		homebrewPowder,
		rateOfFire,
		specialReload,
		stun,
		coolingRate,
		chargeSpeed,
		fuel,
		directDamage,
		duration,
		carriedAmmo,
		miniShells,
		electricity,
		areaDamage,
		aoeRadius,
		grenadeJump,
		projectileVelocity,
		heatDamage,
		movespeed,
		neurotoxin,
		hoverclock,
		shotgunJump,
		pelletsPerShot,
		coldDamage,
		reloadSpeed,
		distance,
		hourglass,
		special,
		rollControl,
		returnToSender,
		damageResistance,
		blowthrough,
		armorBreaking,
		addedExplosion,
		weakpointBonus,
		slowdown,
		acid,
		numPellets2
	}
	
	public static BufferedImage getOverclockIcon(overclockIcons iconSelection) {
		switch (iconSelection) {
			case baseSpread: {
				return baseSpreadWhite;
			}
			case magSize: {
				return magSizeWhite;
			}
			case ricochet: {
				return ricochetWhite;
			}
			case homebrewPowder: {
				return homebrewPowderWhite;
			}
			case rateOfFire: {
				return rateOfFireWhite;
			}
			case specialReload: {
				return specialReloadWhite;
			}
			case stun: {
				return stunWhite;
			}
			case coolingRate: {
				return coolingRateWhite;
			}
			case chargeSpeed: {
				return chargeSpeedWhite;
			}
			case fuel: {
				return fuelWhite;
			}
			case directDamage: {
				return directDamageWhite;
			}
			case duration: {
				return durationWhite;
			}
			case carriedAmmo: {
				return carriedAmmoWhite;
			}
			case miniShells: {
				return minishells;
			}
			case electricity: {
				return electricityWhite;
			}
			case areaDamage: {
				return areaDamageWhite;
			}
			case aoeRadius: {
				return aoeRadiusWhite;
			}
			case grenadeJump: {
				return grenadeJump;
			}
			case projectileVelocity: {
				return projectileVelocityWhite;
			}
			case heatDamage: {
				return heatDamageWhite;
			}
			case movespeed: {
				return movespeedWhite;
			}
			case neurotoxin: {
				return neurotoxinWhite;
			}
			case hoverclock: {
				return hoverclock;
			}
			case shotgunJump: {
				return shotgunJump;
			}
			case pelletsPerShot: {
				return pelletsPerShotWhite;
			}
			case coldDamage: {
				return coldDamageWhite;
			}
			case reloadSpeed: {
				return reloadSpeedWhite;
			}
			case distance: {
				return distanceWhite;
			}
			case hourglass: {
				return hourglassWhite;
			}
			case special: {
				return specialStarWhite;
			}
			case rollControl: {
				return breachCutterRoll;
			}
			case returnToSender: {
				return breachCutterReturn;
			}
			case damageResistance: {
				return damageResistanceWhite;
			}
			case blowthrough: {
				return blowthroughWhite;
			}
			case armorBreaking: {
				return armorBreakingWhite;
			}
			case addedExplosion: {
				return addedExplosionWhite;
			}
			case weakpointBonus: {
				return weakpointBonusWhite;
			}
			case slowdown: {
				return slowdownWhite;
			}
			case acid: {
				return acidWhite;
			}
			case numPellets2: {
				return numPellets2;
			}
			default: {
				return null;
			}
		}
	}
	
	public enum statusEffectIcons {
		fire,
		frozen,
		electricity
	}
	
	public static BufferedImage getStatusEffectIcon(statusEffectIcons iconSelection) {
		switch (iconSelection) {
			case fire: {
				return statusFire;
			}
			case frozen: {
				return statusFrozen;
			}
			case electricity: {
				return statusElectricity;
			}
			default: {
				return null;
			}
		}
	}
}
