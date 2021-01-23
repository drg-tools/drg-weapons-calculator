package spreadCurves;

import guiPieces.accuracyEstimator.LineGraph;

public class RevolverCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {

		double toReturn = 0.0;
		if (inputSpread < 9.0) {
			toReturn = 0.5 + 8.5 * inputSpread / 9.0;
		}
		else if (inputSpread < 11.0) {
			toReturn = 9.0 + 11.0 * (inputSpread - 9.0) / 2.0;
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
