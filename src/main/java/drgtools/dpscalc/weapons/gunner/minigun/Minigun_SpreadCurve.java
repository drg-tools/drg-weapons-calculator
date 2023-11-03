package drgtools.dpscalc.weapons.gunner.minigun;

import drgtools.dpscalc.guiPieces.accuracyEstimator.LineGraph;
import drgtools.dpscalc.modelPieces.accuracy.SpreadCurve;

public class Minigun_SpreadCurve extends SpreadCurve {
	@Override
	public double convertSpreadValue(double inputSpread) {
		// Goes from {X=0, Y=25} to {X=3, Y=0}
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
