package drgtools.dpscalc.modelPieces.accuracy;

import drgtools.dpscalc.enemies.glyphid.Grunt;
import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.modelPieces.Projectile;

import javax.swing.*;

public class ProjectileAccuracyEstimator extends AccuracyEstimator {
    // Define the "max travel time" that a projectile would be expected to still hit its target dead-center
    private final double expectedPerfectAccuracyTravelTime = 0.3;  // seconds
    private final double gravityAcceleration = 9.8;  // m/sec^2
    private final double ceilingHeight = 10.0;  // meters

    private Projectile projectile;
    private SpreadSettings spreadSettings;
    private RecoilSettings recoilSettings;

    public ProjectileAccuracyEstimator(Projectile prj, SpreadSettings spread, RecoilSettings recoil) {
        super();
        projectile = prj;
        spreadSettings = spread;
        recoilSettings = recoil;
    }

    private double calculateProjectileTravelTime(double projectileVelocity, double gravityMultiplier, double launchAngleRadians) {
        double verticalVelocity = Math.sin(launchAngleRadians) * projectileVelocity;
        return 2 * verticalVelocity / (gravityAcceleration * gravityMultiplier);
    }

    private double calculateProjectileDistance(double projectileVelocity, double gravityMultiplier, double launchAngleRadians) {
        double horizontalVelocity = Math.cos(launchAngleRadians) * projectileVelocity;
        double travelTime = calculateProjectileTravelTime(projectileVelocity, gravityMultiplier, launchAngleRadians);
        return horizontalVelocity * travelTime;
    }

    private double calculateMinimumProjectileLaunchAngle(double projectileVelocity, double gravityMultiplier, double goalHorizontalDistance) {
        // angle to get projectile from (0, 0) to (x, y)
        // tan(theta) = (V^2 +- SqRt[V^4 - g(g*x^2 + 2y*v^2)])/(gx)
        // Because this method assumes flat ground, y=0 simplifies the equation to:
        // theta = arctan((V^2 +- SqRt[V^4 - g^2 * x^2])/(gx))

        double rootA = Math.atan((Math.pow(projectileVelocity, 2) + Math.sqrt(
                Math.pow(projectileVelocity, 4) - Math.pow(gravityAcceleration * gravityMultiplier, 2) * Math.pow(goalHorizontalDistance, 2))
        ) / (gravityAcceleration * gravityMultiplier * goalHorizontalDistance));

        double rootB = Math.atan((Math.pow(projectileVelocity, 2) - Math.sqrt(
                Math.pow(projectileVelocity, 4) - Math.pow(gravityAcceleration * gravityMultiplier, 2) * Math.pow(goalHorizontalDistance, 2))
        ) / (gravityAcceleration * gravityMultiplier * goalHorizontalDistance));

        // Disallow negative launch angles
        rootA = Math.max(rootA, 0);
        rootB = Math.max(rootB, 0);

        return Math.min(rootA, rootB);
    }

    // TODO: finish getting this to work
    @Override
    public double estimateAccuracy(boolean weakpoint) {
        // Aliases to prevent repeated calls to the Getter methods
        double gravityMultiplier = projectile.getGravityScale();
        double projectileVelocity = projectile.getInitialSpeed();
        double projectileRadius = projectile.getCollisionRadius();

        double actualTravelTime = 0;

        // Some projectiles aren't affected by gravity and just fly in a straight line
        if (gravityMultiplier == 0) {
            // If the targetDistanceMeters is greater than "max distance", calculate how much additional time would be needed for the projectile to travel to its target
            actualTravelTime = targetDistanceMeters / projectileVelocity;
        }
        // The rest of the projectiles are pulled downwards by gravity.
        else {
            // For all projectiles that are affected by gravity, I want to limit their effective range by enforcing a ceiling. This method calculates the max angle
            // that the projectile can be launched at and still pass underneath the ceiling, which will then inform the max distance and max travel time values later.

            // Max Height = V^2 * sin(theta)^2 / (2 * g)
            // theta = arcsin(SqRt[(Max Height * 2 * g) / (V^2)])
            double maxProjectileLaunchAngle = Math.asin(Math.sqrt((ceilingHeight * 2 * gravityAcceleration * gravityMultiplier) / Math.pow(projectileVelocity, 2)));  // radians
            double absoluteMaxDistance = calculateProjectileDistance(projectileVelocity, gravityMultiplier, maxProjectileLaunchAngle);

            // Early exit condition: if a projectile is affected by gravity and the launch angle required to travel the targetDistance is so high that it would hit the ceiling, return 0% Accuracy to indicate it's impossible.
            if (absoluteMaxDistance < targetDistanceMeters) {
                return 0;
            }

            // At this point, it's now safe to assume that the projectile CAN reach the target; it's just a question of how long it takes.
            double minimumLaunchAngle = calculateMinimumProjectileLaunchAngle(projectileVelocity, gravityMultiplier, targetDistanceMeters);  // radians
            actualTravelTime = calculateProjectileTravelTime(projectileVelocity, gravityMultiplier, minimumLaunchAngle);
        }

        // If the travel time is greater than the expected time to have every projectile hit dead-on, estimate how much distance the target would move horizontally (account for Hazard speed scaling multiplier)
        double targetMovementOffset = 0;  // meters
        if (actualTravelTime > expectedPerfectAccuracyTravelTime) {
            double gruntMovespeed = new Grunt().getMaxMovespeedWhenFeared() * EnemyInformation.getMovespeedDifficultyScaling();
            // This is the angle from which the Grunt is walking towards you. 0 = straight in line with your crosshair, 90 = directly to your right or left
            double angleOfIncidence = 15.0; // degrees
            double horizontalTargetMovementSpeed = gruntMovespeed * Math.sin(angleOfIncidence * Math.PI / 180.0);

            targetMovementOffset = (actualTravelTime - expectedPerfectAccuracyTravelTime) * horizontalTargetMovementSpeed;
        }

        // Finally, calculate the Lens formed by the Projectile Circle cross-section and the Target Circle. Offset from center by the distance traveled, instead of by Recoil.
        // This re-uses the logic from Circular Accuracy
        double targetRadius = 0.0;
        if (weakpoint) {
            targetRadius = 0.2;
        }
        else {
            targetRadius = 0.4;
        }

        double P = 0;  // probability of hitting target
        if (targetRadius >= projectileRadius) {
            if (targetMovementOffset <= targetRadius - projectileRadius) {
                // In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
                P = 1.0;
            }
            else if (targetMovementOffset >= targetRadius + projectileRadius) {
                // In this case, the two circles have no intersection.
                P = 0.0;
            }
            else {
                // For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
                P = MathUtils.areaOfLens(targetRadius, projectileRadius, targetMovementOffset) / (Math.PI * Math.pow(targetRadius, 2));
            }
        }
        else {
            if (targetMovementOffset <= projectileRadius - targetRadius) {
                // In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
                P = Math.pow((targetRadius / projectileRadius), 2);
            }
            else if (targetMovementOffset >= projectileRadius + targetRadius) {
                // In this case, the two circles have no intersection.
                P = 0.0;
            }
            else {
                // For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
                P = MathUtils.areaOfLens(projectileRadius, targetRadius, targetMovementOffset) / (Math.PI * Math.pow(projectileRadius, 2));
            }
        }

        return P * 100.0;
    }

    @Override
    public JPanel getVisualizer() {
        return null;
    }
}
