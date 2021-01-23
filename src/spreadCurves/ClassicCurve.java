package spreadCurves;

import guiPieces.accuracyEstimator.LineGraph;

public class ClassicCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		
		double toReturn = 0.0;
		if (inputSpread < 1.0) {
			toReturn = inputSpread / 3.5;
		}
		else if (inputSpread < 2.0) {
			toReturn = 1.0 / 3.5 + 3.5 * (inputSpread - 1.0) / 5.0;
		}
		else if (inputSpread < 5.0) {
			toReturn = 1.0 + 4.0 * (inputSpread - 2.0) / 3.0;
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
