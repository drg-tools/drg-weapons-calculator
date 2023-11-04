package drgtools.dpscalc.modelPieces.accuracy;

public class SpreadSettings {
    public final double horizontalBaseSpread;
    public final double verticalBaseSpread;
    public final double spreadPerShot;
    public final double spreadRecoverySpeed;
    public final double maxBloom;
    public final double spreadPenaltyWhileWalking;
    public final SpreadCurve spreadCurve;

    public SpreadSettings(double hBaseSpread, double vBaseSpread, double SpS, double recoveryPerSec, double maxBlm, double movePenalty) {
        this(hBaseSpread, vBaseSpread, SpS, recoveryPerSec, maxBlm, movePenalty, null);
    }
    public SpreadSettings(double hBaseSpread, double vBaseSpread, double SpS, double recoveryPerSec, double maxBlm, double movePenalty, SpreadCurve crv) {
        horizontalBaseSpread = hBaseSpread;
        verticalBaseSpread = vBaseSpread;
        spreadPerShot = SpS;
        spreadRecoverySpeed = recoveryPerSec;
        maxBloom = maxBlm;
        spreadPenaltyWhileWalking = movePenalty;
        spreadCurve = crv;
    }

    public boolean spreadCurveIsDefined() {
        return spreadCurve != null;
    }
}
