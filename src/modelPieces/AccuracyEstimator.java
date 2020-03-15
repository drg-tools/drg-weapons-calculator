package modelPieces;

public class AccuracyEstimator {
	public static double targetRadius = 0.4; // meters
	public static double targetDistance = 5.0; // meters
	
	/*
		Except for RoF and magSize, all of these parameters should be passed in as degrees of deviation from a central axis 
		which are strictly less than 90 degrees.
	*/
	public static double calculateAccuracy(double rateOfFire, int numBulletsPerMagazine, 
			 double baseSpread, double spreadPerShot, double maxSpread, double spreadRecovery,
			 double recoilPerShot, double maxRecoil, double recoilRecovery) {
		
		
		double RoF = rateOfFire;
		int magSize = numBulletsPerMagazine;
		// The time that passes between each shot
		double deltaT = 1.0 / RoF;
		
		double Sb = baseSpread;
		double Ss = spreadPerShot;
		double Sr = spreadRecovery;
		// TODO: For most guns, the max spread is calculated by adding the base spread + max spread -- thus base and max spread are tied together and grow/shrink by the same value (not same multiplier)
		// This should be refactored to represent that.
		double Sm = maxSpread;
		
		// I'm applying a 0.5 multiplier to all of these recoil coefficients, to factor in the player counter-acting the recoil by 50%.
		// Intentionally using 1 - Counter so that I can change the player's efficiency directly, rather than having to do indirect math every time I want to change the value.
		double playerRecoilCorrectionCoefficient = (1.0 - 0.5);
		double Rs = recoilPerShot * playerRecoilCorrectionCoefficient;
		double Rr = recoilRecovery * playerRecoilCorrectionCoefficient;
		double Rm = maxRecoil * playerRecoilCorrectionCoefficient;
		
		double sumOfAllProbabilities = 0.0;
		double timeElapsed = 0.0;
		
		double reticleRadius, recoil, P; 
		for (int i = 0; i < magSize; i++) {
			reticleRadius = convertDegreesToMeters(radius(i, timeElapsed, Sb, Ss, Sr, Sm));
			recoil = convertDegreesToMeters(recoil(i, timeElapsed, Rs, Rr, Rm));
			
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
	private static double radius(int numBulletsFired, double timeElapsed, double Sb, double Ss, double Sr, double Sm) {
		return Sb + Math.min(Math.max(numBulletsFired * Ss - timeElapsed * Sr, 0), Sm - Sb);
	}
	
	private static double recoil(int numBulletsFired, double timeElapsed, double Rs, double Rr, double Rm) {
		return Math.min(Math.max(numBulletsFired * Rs - timeElapsed * Rr, 0), Rm);
	}
	
	private static double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	private static double convertDegreesToMeters(double degrees) {
		double radians = degrees * Math.PI / 180.0;
		return targetDistance * Math.tan(radians);
	}
}
