package spreadCurves;

import guiPieces.accuracyEstimator.LineGraph;

public class AssaultRifleCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		
		double toReturn = 0.0;
		if (inputSpread < 0.25) {
			toReturn = 2.0 * inputSpread / 3.0;
		}
		else if (inputSpread < 3.0) {
			toReturn = 2.0 * 0.25 / 3.0 + (inputSpread - 0.25) / 5.0;
		}
		else if (inputSpread < 4.5) {
			toReturn = 2.0 * 0.25 / 3.0 + (3.0 - 0.25) / 5.0 + 4.0 * (inputSpread - 3.0) / 1.5;
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
