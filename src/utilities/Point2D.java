package utilities;

/*
	Even though Java already has a built-in Point2D, I had to write this custom class to use double values for x and y.
*/
public class Point2D {
	private double x, y;
	
	public Point2D(double xCoord, double yCoord) {
		x = xCoord;
		y = yCoord;
	}
	
	public double x() {
		return x;
	}
	public double y() {
		return y;
	}
	
	public double vectorLength() {
		return Math.hypot(x, y);
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
