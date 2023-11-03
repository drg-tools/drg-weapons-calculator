package drgtools.dpscalc.weapons.scout.classic;

import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.modelPieces.accuracy.SpreadCurve;

public class Classic_SpreadCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		/*
			{0, 0}
			{1, 0.3}
			{2, 1}
			{5, 5}
		*/
		
		double toReturn = 0.0;
		if (inputSpread < 1.0) {
			toReturn = inputSpread * 0.3;
		}
		else if (inputSpread < 2.0) {
			toReturn = 0.3 + (inputSpread - 1.0) * 0.7;
		}
		else if (inputSpread < 5.0) {
			toReturn = 1.0 + (inputSpread - 2.0) * (5.0 - 1.0) / (5.0 - 2.0);
		}
		else {
			toReturn = 5.0;
		}
		
		return toReturn;
	}

	@Override
	public LineGraph getGraph() {
		return generateGraph(5.0);
	}
}
