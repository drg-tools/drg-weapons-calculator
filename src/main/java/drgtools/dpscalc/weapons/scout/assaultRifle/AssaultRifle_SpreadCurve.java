package drgtools.dpscalc.weapons.scout.assaultRifle;

import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.modelPieces.accuracy.SpreadCurve;

public class AssaultRifle_SpreadCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		/*
			{0, 0}
			{0.2, 0.15}
			{3, 0.7}
			{4.5, 4.5}
			{9, 4.5}
		*/
		
		double toReturn = 0.0;
		if (inputSpread < 0.2) {
			toReturn = 0.15 * inputSpread / 0.2;
		}
		else if (inputSpread < 3.0) {
			toReturn = 0.15 + (inputSpread - 0.2) * (0.7 - 0.15) / (3.0 - 0.2);
		}
		else if (inputSpread < 4.5) {
			toReturn = 0.7 + (inputSpread - 3.0) * (4.5 - 0.7) / (4.5 - 3.0);
		}
		else {
			toReturn = 4.5; 
		}
		
		return toReturn;
	}

	@Override
	public LineGraph getGraph() {
		return generateGraph(4.5);
	}
}
