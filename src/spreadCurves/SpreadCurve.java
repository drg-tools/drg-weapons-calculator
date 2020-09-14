package spreadCurves;

import java.util.HashMap;

import guiPieces.LineGraph;

public abstract class SpreadCurve {
	
	public abstract double convertSpreadValue(double inputSpread);
	
	// This method is going to be primarily used for debugging purposes during development, but it might be cool to have in the Visualizer pane?
	protected LineGraph generateGraph(double largestX) {
		double largestY = 0.0;
		Double[] sortedXvalues = new Double[1 + (int) (largestX * 10.0)];
		HashMap<Double, Double> keyValuePairs = new HashMap<Double, Double>();
		
		double x, y;
		for (int i = 0; i < sortedXvalues.length; i++) {
			x = i * 0.1;
			y = convertSpreadValue(x);
			
			if (y > largestY) {
				largestY = y;
			}
			
			sortedXvalues[i] = x;
			keyValuePairs.put(x, y);
		}
		
		LineGraph toReturn = new LineGraph(sortedXvalues, keyValuePairs, largestX, largestY);
		toReturn.setGraphAnimation(false);
		
		return toReturn;
	}
	
	public abstract LineGraph getGraph();
}
