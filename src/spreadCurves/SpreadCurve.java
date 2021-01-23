package spreadCurves;

import java.util.ArrayList;

import guiPieces.accuracyEstimator.LineGraph;
import utilities.Point2D;

public abstract class SpreadCurve {
	
	public abstract double convertSpreadValue(double inputSpread);
	
	// This method is going to be primarily used for debugging purposes during development, but it might be cool to have in the Visualizer pane?
	protected LineGraph generateGraph(double largestX) {
		double largestY = 0.0;
		ArrayList<Point2D> values = new ArrayList<Point2D>();
		
		double x = 0;
		double y;
		while(x <= largestX) {
			y = convertSpreadValue(x);
			largestY = Math.max(y, largestY);
			
			values.add(new Point2D(x, y));
			
			// This should match the FPS setting in LineGraph
			x += 0.01;
		}
		
		LineGraph toReturn = new LineGraph(values, largestX, largestY);
		toReturn.setGraphAnimation(false);
		
		return toReturn;
	}
	
	public abstract LineGraph getGraph();
}
