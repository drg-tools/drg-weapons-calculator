package drgtools.dpscalc.modelPieces.accuracy;

public class RecoilSettings {
    public final double recoilPitch;
    public final double recoilYaw;
    public final double mass;
    public final double springStiffness;
    public final boolean canRecoilDown;

    public RecoilSettings(double rPitch, double rYaw, double m, double sStiffness) {
        this(rPitch, rYaw, m, sStiffness, false);
    }
    public RecoilSettings(double rPitch, double rYaw, double m, double sStiffness, boolean canRecoilDwn) {
        recoilPitch = rPitch;
        recoilYaw = rYaw;
        mass = m;
        springStiffness = sStiffness;
        canRecoilDown = canRecoilDwn;
    }
}
