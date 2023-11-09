package drgtools.dpscalc.modelPieces.accuracy;

import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.guiPieces.accuracyEstimator.RectangularAccuracyVisualizer;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.utilities.Point2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RectangularHitscanAccuracyEstimator extends AccuracyEstimator {

    public RectangularHitscanAccuracyEstimator(SpreadSettings spread, RecoilSettings recoil) {
        super();
        spreadSettings = spread;
        recoilSettings = recoil;

        naturalFrequency = Math.sqrt(recoilSettings.springStiffness / recoilSettings.mass);
        initialRecoilVelocity = Math.hypot(recoilSettings.recoilPitch, recoilSettings.recoilYaw);
        if (initialRecoilVelocity > 0) {
            recoilPerShotEndTime = -1.0 * MathUtils.lambertInverseWNumericalApproximation(-naturalFrequency * recoilGoal / initialRecoilVelocity) / naturalFrequency;
        }
        else {
            recoilPerShotEndTime = 0.0;
        }

        canBeVisualized = true;
    }

    // TODO: someday I might like to model Recoil into this, too...
    @Override
    public double estimateAccuracy(boolean weakpointTarget) {
        // Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
        double crosshairHeightMeters = convertDegreesToMeters(spreadSettings.verticalBaseSpread / 2.0);
        double crosshairWidthMeters = convertDegreesToMeters(spreadSettings.horizontalBaseSpread / 2.0);
        double targetRadius;
        if (weakpointTarget) {
            targetRadius = 0.2;
        }
        else {
            targetRadius = 0.4;
        }

		/*
			After Lunari pointed out that the old model didn't work in extreme values (like 1m distance away from targets), I basically scrapped the old model
			and decided to brute-force it with a double for-loop. It's not pretty, it's not elegant, but by golly it's gonna be RIGHT. People are trusting me to
			model this stuff correctly, and this has been bugged for months with only Lunari questioning it. Makes me wonder what else I have wrong in here...
		*/
        double w = crosshairWidthMeters * 2.0, h = crosshairHeightMeters * 2.0;
        double horizontalProbability, verticalProbability, totalProbability;
        double sumOfProbabilitiesInsideTarget = 0.0, sumOfAllProbabilities = 0.0;
        double precision = 50.0;

        double i, j;
        for (i = 0.0; i < w; i += w / precision) {
            horizontalProbability = MathUtils.probabilityInNormalDistribution(0, w, i);

            for (j = 0.0; j < h; j += h / precision) {
                verticalProbability = MathUtils.probabilityInNormalDistribution(0, h, j);
                totalProbability = horizontalProbability * verticalProbability;

                sumOfAllProbabilities += totalProbability;
                if (Math.hypot(crosshairWidthMeters - i, crosshairHeightMeters - j) <= targetRadius) {
                    sumOfProbabilitiesInsideTarget += totalProbability;
                }
            }
        }

        return (sumOfProbabilitiesInsideTarget / sumOfAllProbabilities);
    }

    @Override
    public JPanel getVisualizer() {
        JPanel toReturn = new JPanel();

        double sampleDensity = 100.0;
        int i;

        // First: show the Spread and Recoil stats on the left-hand side, just like Circular Accuracy.
        JPanel granularDataPanel = new JPanel();
        granularDataPanel.setPreferredSize(new Dimension(420, 684));
        granularDataPanel.setLayout(new BoxLayout(granularDataPanel, BoxLayout.PAGE_AXIS));
        JPanel variables = new JPanel();
        variables.setLayout(new GridLayout(5, 4));
        variables.add(new JLabel("Horiz. Spread:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.horizontalBaseSpread, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Recoil Pitch:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.recoilPitch, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Vertical Spread:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.verticalBaseSpread, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Recoil Yaw:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.recoilYaw, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spread per Shot:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.spreadPerShot, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Mass:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.mass, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spread Recovery:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.spreadRecoverySpeed, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spring Stiffness:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.springStiffness, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Max Bloom:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.maxBloom, GuiConstants.numDecimalPlaces) + ""));
        granularDataPanel.add(variables);

        JPanel recoilPerShotPanel = new JPanel();
        //recoilPerShotPanel.setPreferredSize(new Dimension(228, 162));
        recoilPerShotPanel.setLayout(new BoxLayout(recoilPerShotPanel, BoxLayout.PAGE_AXIS));
        recoilPerShotPanel.add(new JLabel("Recoil per Shot Graph"));
        ArrayList<Point2D> recoilPerShotData = new ArrayList<>();
        double t;
        for (i = 0; i < (int) Math.ceil(recoilPerShotEndTime * sampleDensity) + 1; i++) {
            t = i*0.01;
            recoilPerShotData.add(new Point2D(t, getRecoilPerShotOverTime(t)));
        }
        double maxRecoilPerShot = getRecoilPerShotOverTime(1.0 / naturalFrequency);
        LineGraph recoilPerShot = new LineGraph(recoilPerShotData, recoilPerShotEndTime, Math.max(3.0, maxRecoilPerShot));
        recoilPerShot.setGraphAnimation(false);
        recoilPerShotPanel.add(recoilPerShot);
        granularDataPanel.add(recoilPerShotPanel);

        // Second: because Recoil is not yet modeled in Rectangular Accuracy, I can just have a custom-rendered panel that I color pixel-by-pixel to show how it works and put in them middle+right 2/3 of the pop-up.
        RectangularAccuracyVisualizer rectAccVis = new RectangularAccuracyVisualizer(visualizeGeneralAccuracy, convertDegreesToMeters(spreadSettings.horizontalBaseSpread/2.0), convertDegreesToMeters(spreadSettings.verticalBaseSpread/2.0));

        toReturn.add(granularDataPanel);
        toReturn.add(rectAccVis);

        return toReturn;
    }
}
