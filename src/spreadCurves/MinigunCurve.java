package spreadCurves;

import guiPieces.accuracyEstimator.LineGraph;

public class MinigunCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {

		double toReturn = 0.0;
		if (inputSpread < 3.0) {
			// WolframAlpha says to use -2.73277*(x-3) + 8.70402*(x-3)^2 + 11.2665*(x-3)^3 + 5.7997*(x-3)^4 + 0.934667*(x-3)^5
			toReturn = -2.73277*(inputSpread-3) + 8.70402*Math.pow(inputSpread-3, 2) + 11.2665*Math.pow(inputSpread-3, 3) + 5.7997*Math.pow(inputSpread-3, 4) + 0.934667*Math.pow(inputSpread-3, 5);
		}
		else {
			toReturn = 0.0; 
		}
		
		return toReturn;
	}

	@Override
	public LineGraph getGraph() {
		return generateGraph(3.0);
	}
}
