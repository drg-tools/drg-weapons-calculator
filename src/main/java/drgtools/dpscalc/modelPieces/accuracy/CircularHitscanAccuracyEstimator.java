package drgtools.dpscalc.modelPieces.accuracy;

import drgtools.dpscalc.guiPieces.GuiConstants;
import drgtools.dpscalc.guiPieces.accuracyEstimator.AccuracyAnimation;
import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.utilities.Point2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CircularHitscanAccuracyEstimator extends AccuracyEstimator {
    private double avgBaseSpread;

    public CircularHitscanAccuracyEstimator(double RoF, int mSize, int bSize, double bInterval, SpreadSettings spread, RecoilSettings recoil) {
        super();
        rateOfFire = RoF;
        magSize = mSize;
        burstSize = bSize;
        burstInterval = bInterval;
        spreadSettings = spread;
        recoilSettings = recoil;

        // Engineer/Shotgun uses an ellipse instead of a circle. To estimate its accuracy using Lens intersections, I have to approximate that ellipse as a circle with equal area.
        avgBaseSpread = Math.sqrt(spreadSettings.horizontalBaseSpread * spreadSettings.verticalBaseSpread);
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

    private double getTotalSpreadAtTime(double t) {
        // This method is modeled as if every bullet was fired at maximum possible RoF  TODO: is this comment still true?

        // For practicality purposes, I have to model it as if the exact moment the bullet gets fired its Total Spread stays the same, and then gets added a very short time afterwards.
        double spreadPerShotAddTime = 0.01;
        double minimumBloom;
        if (dwarfIsMoving) {
            minimumBloom = spreadSettings.spreadPenaltyWhileWalking;
        }
        else {
            minimumBloom = 0.0;
        }
        double currentSpread = minimumBloom;
        double bulletFiredTimestamp, nextTimestamp;
        for (int i = 0; i < bulletFiredTimestamps.length; i++) {
            bulletFiredTimestamp = bulletFiredTimestamps[i];

            // Early exit condition: if t is before any bullet timestamp, that means that this for loop should stop evaluating
            if (t < bulletFiredTimestamp) {
                break;
            }

            if (t > bulletFiredTimestamp + spreadPerShotAddTime) {
                currentSpread = Math.min(currentSpread + spreadSettings.spreadPerShot, spreadSettings.maxBloom);
            }

            if (i < bulletFiredTimestamps.length - 1) {
                nextTimestamp = bulletFiredTimestamps[i+1];
                if (t >= nextTimestamp) {
                    currentSpread = Math.max(currentSpread - (nextTimestamp - bulletFiredTimestamp) * spreadSettings.spreadRecoverySpeed, minimumBloom);
                }
                else {
                    currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadSettings.spreadRecoverySpeed, minimumBloom);
                }
            }
            else {
                // The last bullet is allowed to trail off to Base Spread
                currentSpread = Math.max(currentSpread - (t - bulletFiredTimestamp) * spreadSettings.spreadRecoverySpeed, minimumBloom);
            }
        }

        if (spreadSettings.spreadCurveIsDefined()) {
            return avgBaseSpread + spreadSettings.spreadCurve.convertSpreadValue(currentSpread);
        }
        else {
            return avgBaseSpread + currentSpread;
        }
    }

    @Override
    public double estimateAccuracy(boolean weakpointTarget) {
        /*
			Step 1: Calculate when each bullet will be fired for this magazine, and store the timestamps internally
		*/
        calculateBulletFiredTimestamps();

		/*
			Step 2: Use Spread and Recoil to calculate the size and offset of the crosshair relative to the static target for each bullet in the magazine
		*/
        double targetRadius;
        if (weakpointTarget) {
            targetRadius = 0.2;
        }
        else {
            targetRadius = 0.4;
        }

        double sumOfAllProbabilities = 0.0;
        double bulletFiredTimestamp, crosshairRadius, crosshairRecoil, P;
        for (int i = 0; i < magSize; i++) {
            bulletFiredTimestamp = bulletFiredTimestamps[i];
            // Spread Units are like the FoV setting; it needs to be divided by 2 before it can be used in trigonometry correctly
            crosshairRadius = convertDegreesToMeters(getTotalSpreadAtTime(bulletFiredTimestamp) / 2.0);
            crosshairRecoil = convertDegreesToMeters(getTotalRecoilAtTime(bulletFiredTimestamp, true));

            if (targetRadius >= crosshairRadius) {
                if (crosshairRecoil <= targetRadius - crosshairRadius) {
                    // In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil.
                    P = 1.0;
                }
                else if (crosshairRecoil >= targetRadius + crosshairRadius) {
                    // In this case, the two circles have no intersection.
                    P = 0.0;
                }
                else {
                    // For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
                    P = MathUtils.areaOfLens(targetRadius, crosshairRadius, crosshairRecoil) / (Math.PI * Math.pow(targetRadius, 2));
                }
            }
            else {
                if (crosshairRecoil <= crosshairRadius - targetRadius) {
                    // In this case, the larger circle entirely contains the smaller circle, even when displaced by recoil. P = (smaller circle area / larger circle area)
                    P = Math.pow((targetRadius / crosshairRadius), 2);
                }
                else if (crosshairRecoil >= crosshairRadius + targetRadius) {
                    // In this case, the two circles have no intersection.
                    P = 0.0;
                }
                else {
                    // For all other cases, the area of the smaller circle that is still inside the larger circle is known as a "Lens". P = (Lens area / larger circle area)
                    P = MathUtils.areaOfLens(crosshairRadius, targetRadius, crosshairRecoil) / (Math.PI * Math.pow(crosshairRadius, 2));
                }
            }

            // System.out.println("P for bullet # " + (i + 1) + ": " + P);
            sumOfAllProbabilities += P;
        }

        return sumOfAllProbabilities / magSize;
    }

    @Override
    public JPanel getVisualizer() {
        JPanel toReturn = new JPanel();

        double sampleDensity = 100.0;
        int i;

        // Part 1: figuring out stuff before rendering
        double lastBulletFiredTimestamp = bulletFiredTimestamps[bulletFiredTimestamps.length - 1];
        // For Engineer/Shotgun, SRS = 0, so I have to use Math.min() to sidestep 0/0 errors.
        double loopDuration = lastBulletFiredTimestamp + Math.max((getTotalSpreadAtTime(lastBulletFiredTimestamp + 0.01) - avgBaseSpread) / Math.max(spreadSettings.spreadRecoverySpeed, 0.00001), recoilPerShotEndTime);

        // There will be this many data points taken per second (should be at least 10?)
        // This number should match the FPS in AccuracyAnimation so that every frame is just pulling a the next value
        double timeBetweenSamples = 1.0 / sampleDensity;
        int numSamples = (int) Math.ceil(loopDuration * sampleDensity) + 1;

        // Part 2: constructing the datasets to plot
        double currentTime, currentValue;

        ArrayList<Point2D> rawSpreadData = new ArrayList<>();
        ArrayList<Point2D> convertedSpreadData = new ArrayList<>();
        ArrayList<Point2D> rawRecoilData = new ArrayList<>();
        ArrayList<Point2D> convertedRawRecoilData = new ArrayList<>();
        ArrayList<Point2D> reducedRecoilData = new ArrayList<>();
        ArrayList<Point2D> convertedReducedRecoilData = new ArrayList<>();

        double maxSpread = 0.0;
        double minSpread = 10000.0;
        double maxRawRecoil = 0.0;
        double maxReducedRecoil = 0.0;
        for (i = 0; i < numSamples; i++) {
            currentTime = i * timeBetweenSamples;

            // Spread
            currentValue = getTotalSpreadAtTime(currentTime);
            minSpread = Math.min(minSpread, currentValue);
            maxSpread = Math.max(maxSpread, currentValue);
            rawSpreadData.add(new Point2D(currentTime, currentValue));
            convertedSpreadData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue / 2.0)));

            // Raw Recoil
            currentValue = getTotalRecoilAtTime(currentTime, false);
            maxRawRecoil = Math.max(maxRawRecoil, currentValue);
            rawRecoilData.add(new Point2D(currentTime, currentValue));
            convertedRawRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));

            // Reduced Recoil
            currentValue = getTotalRecoilAtTime(currentTime, true);
            maxReducedRecoil = Math.max(maxReducedRecoil, currentValue);
            reducedRecoilData.add(new Point2D(currentTime, currentValue));
            convertedReducedRecoilData.add(new Point2D(currentTime, convertDegreesToMeters(currentValue)));
        }

        // Part 3: displaying the data
        JPanel granularDataPanel = new JPanel();
        granularDataPanel.setPreferredSize(new Dimension(420, 684));
        granularDataPanel.setLayout(new BoxLayout(granularDataPanel, BoxLayout.PAGE_AXIS));
        JPanel variables = new JPanel();
        variables.setLayout(new GridLayout(4, 4));
        variables.add(new JLabel("Avg Base Spread:"));
        variables.add(new JLabel(MathUtils.round(avgBaseSpread, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Recoil Pitch:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.recoilPitch, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spread per Shot:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.spreadPerShot, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Recoil Yaw:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.recoilYaw, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spread Recovery:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.spreadRecoverySpeed, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Mass:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.mass, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Max Bloom:"));
        variables.add(new JLabel(MathUtils.round(spreadSettings.maxBloom, GuiConstants.numDecimalPlaces) + ""));
        variables.add(new JLabel("Spring Stiffness:"));
        variables.add(new JLabel(MathUtils.round(recoilSettings.springStiffness, GuiConstants.numDecimalPlaces) + ""));
        granularDataPanel.add(variables);

        JPanel recoilPerShotPanel = new JPanel();
        //recoilPerShotPanel.setPreferredSize(new Dimension(228, 162));
        recoilPerShotPanel.setLayout(new BoxLayout(recoilPerShotPanel, BoxLayout.PAGE_AXIS));
        recoilPerShotPanel.add(new JLabel("Recoil per Shot Graph"));
        ArrayList<Point2D> recoilPerShotData = new ArrayList<Point2D>();
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

        JPanel lineGraphsPanel = new JPanel();
        lineGraphsPanel.setLayout(new BoxLayout(lineGraphsPanel, BoxLayout.PAGE_AXIS));

        LineGraph spreadGraph = new LineGraph(rawSpreadData, loopDuration, Math.max(maxSpread, 8.0));
        new Thread(spreadGraph).start();
        JPanel spreadGraphAndLabel = new JPanel();
        spreadGraphAndLabel.setLayout(new BoxLayout(spreadGraphAndLabel, BoxLayout.PAGE_AXIS));
        spreadGraphAndLabel.add(new JLabel("Crosshair diameter (degrees) vs Time (seconds)"));
        spreadGraphAndLabel.add(spreadGraph);
        spreadGraphAndLabel.setBorder(GuiConstants.blackLine);
        lineGraphsPanel.add(spreadGraphAndLabel);

        LineGraph rawRecoilGraph = new LineGraph(rawRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
        new Thread(rawRecoilGraph).start();
        JPanel rawRecoilGraphAndLabel = new JPanel();
        rawRecoilGraphAndLabel.setLayout(new BoxLayout(rawRecoilGraphAndLabel, BoxLayout.PAGE_AXIS));
        rawRecoilGraphAndLabel.add(new JLabel("Recoil offset (degrees) vs Time (seconds)"));
        rawRecoilGraphAndLabel.add(rawRecoilGraph);
        rawRecoilGraphAndLabel.setBorder(GuiConstants.blackLine);
        lineGraphsPanel.add(rawRecoilGraphAndLabel);

        LineGraph playerReducedRecoilGraph = new LineGraph(reducedRecoilData, loopDuration, Math.max(maxRawRecoil, 17.0));
        new Thread(playerReducedRecoilGraph).start();
        JPanel reducedRecoilAndGraph = new JPanel();
        reducedRecoilAndGraph.setLayout(new BoxLayout(reducedRecoilAndGraph, BoxLayout.PAGE_AXIS));
        reducedRecoilAndGraph.add(new JLabel("Player-reduced recoil offset (degrees) vs Time (seconds)"));
        reducedRecoilAndGraph.add(playerReducedRecoilGraph);
        reducedRecoilAndGraph.setBorder(GuiConstants.blackLine);
        lineGraphsPanel.add(reducedRecoilAndGraph);

        AccuracyAnimation rawRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration,
                convertedSpreadData, convertDegreesToMeters(maxSpread),
                convertedRawRecoilData, convertDegreesToMeters(maxRawRecoil));
        rawRecoilGif.setBorder(GuiConstants.blackLine);
        new Thread(rawRecoilGif).start();

        AccuracyAnimation reducedRecoilGif = new AccuracyAnimation(visualizeGeneralAccuracy, loopDuration,
                convertedSpreadData, convertDegreesToMeters(maxSpread),
                convertedReducedRecoilData, convertDegreesToMeters(maxReducedRecoil));
        reducedRecoilGif.setBorder(GuiConstants.blackLine);
        new Thread(reducedRecoilGif).start();

        toReturn.add(granularDataPanel);
        toReturn.add(lineGraphsPanel);
        toReturn.add(rawRecoilGif);
        toReturn.add(reducedRecoilGif);

        /*
            If I ever want to re-add the Spread Curve transformation graphs, this is where I could easily do it.

            if (spreadSettings.spreadCurveIsDefined()) {
                toReturn.add(spreadSettings.spreadCurve.getGraph());
            }
        */

        return toReturn;
    }
}
