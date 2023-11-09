package drgtools.dpscalc.enemies;

import drgtools.dpscalc.modelPieces.EnemyInformation;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;

public class TargetDummy extends Enemy {
    public TargetDummy() {
        calculateBreakpoints = false;

        enemyName = "Target Dummy";
        baseHealth = 1000000;
        normalScaling = false;

        hasExposedBodySomewhere = true;

        hasWeakpoint = true;
        weakpointMultiplier = EnemyInformation.averageWeakpointDamageIncrease();
        estimatedProbabilityBulletHitsWeakpoint = EnemyInformation.probabilityBulletWillHitWeakpoint();

        // Copy of Glyphid Grunt's temperatures
        // TODO: change this to use EnemyInformation and get the average values of all modeled enemies
        temperatureComponent = new CreatureTemperatureComponent(30, 10, 6, 2, -30, 0, 6, 2);
    }
}

