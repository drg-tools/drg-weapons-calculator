package drgtools.dpscalc.weapons;

import drgtools.dpscalc.modelPieces.damage.DamageComponent;

public class Projectile {
    protected double initialSpeed;
    protected double maxSpeed;
    protected double gravityScale;  // 0 means that this projectile is not affected by gravity in-game.
    protected double collisionRadius;
    protected DamageComponent dmgComponent;

    protected Projectile(double speed, double sizeRadius, DamageComponent dmg) {
        initialSpeed = speed;
        maxSpeed = speed;
        gravityScale = 1.0;
        collisionRadius = sizeRadius;
        dmgComponent = dmg;
    }

    protected Projectile(double speed, double gravity, double sizeRadius, DamageComponent dmg) {
        initialSpeed = speed;
        maxSpeed = speed;
        gravityScale = gravity;
        collisionRadius = sizeRadius;
        dmgComponent = dmg;
    }

    protected Projectile(double initSpeed, double mxSpeed, double gravity, double sizeRadius, DamageComponent dmg) {
        initialSpeed = initSpeed;
        maxSpeed = mxSpeed;
        gravityScale = gravity;
        collisionRadius = sizeRadius;
        dmgComponent = dmg;
    }

    public void setSpeed(double initial, double max) {
        initialSpeed = initial;
        maxSpeed = max;
    }
    public double getInitialSpeed() {
        return initialSpeed;
    }
    public double getMaxSpeed() {
        return maxSpeed;
    }
    public void setGravityScale(double in) {
        gravityScale = in;
    }
    public boolean isAffectedByGravity() {
        return !(gravityScale == 0);
    }
    public double getGravityScale() {
        return gravityScale;
    }
    public double getCollisionRadius() {
        return collisionRadius;
    }
    public void setDamageComponent(DamageComponent newDmg) {
        dmgComponent = newDmg;
    }
    public DamageComponent getDamageComponent() {
        return dmgComponent;
    }
}
