package utilities;

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
		return Math.hypot(x, y);  // Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
