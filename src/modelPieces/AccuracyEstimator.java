package modelPieces;

/*
	Weapons that will use this:
	Subata
	EPC_RegularShot
	Shotgun
	SMG
	Autocannon
	BurstPistol
	Minigun?
	Revolver
	Boomstick
	Classic_Hipfire
	Deepcore
	Zhukov
*/

// TODO: add a way to model Rectangular crosshairs, like Autocannon, Boomstick, and Zhukovs.

// It might be possible that Base Spread percentage doesn't scale the RADIUS, but rather the AREA of the base spread. So 600% base spread -> SqRt(6)*radius, and 50% -> SqRt(0.5)*radius

public class AccuracyEstimator {
	private double targetRadius;
	private double targetDistance;
	
	private double RoF;
	private int magSize;
	private double deltaT;
	
	private double Sb;
	private double Ss;
	private double Sr;
	private double Sm;
	
	private double Rs;
	private double Rr;
	private double Rm;
	
	/*
		Except for RoF and magSize, all of these parameters should be passed in as degrees of deviation from a central axis 
		which are strictly less than 90 degrees.
	*/
	public AccuracyEstimator(double rateOfFire, int numBulletsPerMagazine, 
							 double baseSpread, double spreadPerShot, double maxSpread, double spreadRecovery,
							 double recoilPerShot, double maxRecoil, double recoilRecovery) {
		targetRadius = 0.4; // meters
		targetDistance = 5.0; // meters
		
		RoF = rateOfFire;
		magSize = numBulletsPerMagazine;
		// The time that passes between each shot
		deltaT = 1.0 / RoF;
		
		Sb = baseSpread;
		Ss = spreadPerShot;
		Sr = spreadRecovery;
		Sm = maxSpread;
		
		// I'm applying a 0.5 multiplier to all of these recoil coefficients, to factor in the player counter-acting the recoil by 50%.
		// Intentionally using 1 - Counter so that I can change the player's efficiency directly, rather than having to do indirect math every time I want to change the value.
		double playerRecoilCorrectionCoefficient = (1 - 0.5);
		Rs = recoilPerShot * playerRecoilCorrectionCoefficient;
		Rr = recoilRecovery * playerRecoilCorrectionCoefficient;
		Rm = maxRecoil * playerRecoilCorrectionCoefficient;
	}
	
	public double calculateAccuracy() {
		
		double sumOfAllProbabilities = 0.0;
		double timeElapsed = 0.0;
		
		double reticleRadius, recoil, P; 
		for (int i = 0; i < magSize; i++) {
			reticleRadius = convertDegreesToMeters(radius(i, timeElapsed));
			recoil = convertDegreesToMeters(recoil(i, timeElapsed));
			
			if (targetRadius >= reticleRadius) {
				if (recoil <= targetRadius - reticleRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
					P = 1.0;
				}
				else if (recoil >= targetRadius + reticleRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = areaOfLens(targetRadius, reticleRadius, recoil) / (Math.PI * Math.pow(targetRadius, 2));
				}
			}
			else {
				if (recoil <= reticleRadius - targetRadius) {
					// In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
					P = Math.pow((targetRadius / reticleRadius), 2);
				}
				else if (recoil >= reticleRadius + targetRadius) {
					// In this case, the two circles have no intersection.
					P = 0.0;
				}
				else {
					// For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
					P = areaOfLens(reticleRadius, targetRadius, recoil) / (Math.PI * Math.pow(reticleRadius, 2));
				}
			}
			
			// System.out.println("P for bullet # " + (i + 1) + ": " + P);
			sumOfAllProbabilities += P;
			
			timeElapsed += deltaT;
		}
		
		return sumOfAllProbabilities / magSize * 100.0;
	}
	
	// Both radius() and recoil() return degrees of deviation and need to have their outputs changed to meters before use.
	private double radius(int numBulletsFired, double timeElapsed) {
		return Sb + Math.min(Math.max(numBulletsFired * Ss - timeElapsed * Sr, 0), Sm - Sb);
	}
	
	private double recoil(int numBulletsFired, double timeElapsed) {
		return Math.min(Math.max(numBulletsFired * Rs - timeElapsed * Rr, 0), Rm);
	}
	
	private double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	private double convertDegreesToMeters(double degrees) {
		double radians = degrees * Math.PI / 180.0;
		return targetDistance * Math.tan(radians);
	}
}
