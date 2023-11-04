package drgtools.dpscalc.weapons.gunner.revolver;

import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.modelPieces.accuracy.SpreadCurve;

public class Revolver_SpreadCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		/*
			{0, 0.6062937}
			{9, 9}
			{11, 20}
		*/

		double toReturn = 0.0;
		if (inputSpread < 9.0) {
			toReturn = 0.6062937 + inputSpread * (9 - 0.6062937) / 9.0;
		}
		else if (inputSpread < 11.0) {
			toReturn = 9.0 + (inputSpread - 9.0) * (20.0 - 9.0) / (11.0 - 9.0);
		}
		else {
			toReturn = 20.0; 
		}
		
		return toReturn;
	}

	@Override
	public LineGraph getGraph() {
		return generateGraph(11.0);
	}
}
