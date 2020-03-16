package utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		 
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/*
		Mean num bullets fired before proc = 1 / Probability
		Median num bullets fired before proc = 1 - (1 / Log2[1 - Probability])
		
		If Probability < 50%, then Median <= Mean
	*/
	public static double meanRolls(double probability) {
		return 1 / probability;
	}
	public static double medianRolls(double probability) {
		return 1 - (1 / log2(1 - probability));
	}
	
	public static double log2(double a) {
		return Math.log(a) / Math.log(2);
	}
	
	public static double vectorDotProduct(double[] A, double[] B) {
		if (A.length != B.length) {
			return -1.0;
		}
		
		double sum = 0.0;
		for (int i = 0; i < A.length; i++) {
			sum += A[i] * B[i];
		}
		return sum;
	}
	
	public static double sum(double[] A) {
		if (A.length == 0) {
			return 0;
		}
		
		double sum = 0.0;
		for (int i = 0; i < A.length; i++) {
			sum += A[i];
		}
		return sum;
	}
	
	private static double erf(double x) {
		// erf can't be expressed precisely, so I'm using a quick-and-dirty numerical approximation.
		// Sourced from https://en.wikipedia.org/wiki/Error_function#Numerical_approximations
		boolean xWasNegative = false;
		if (x < 0) {
			xWasNegative = true;
			x *= -1.0;
		}
		
		// Early exit case to save a few CPU cycles
		if (x > 2) {
			if (xWasNegative) {
				return -1.0;
			}
			else {
				return 1.0;
			}
		}
		
		double a = 0.278393 * x;
		double b = 0.230389 * Math.pow(x, 2);
		double c = 0.000972 * Math.pow(x, 3);
		double d = 0.078108 * Math.pow(x, 4);
		
		double sum = 1.0 + a + b + c + d;
		double toReturn = 1.0 - (1.0 / Math.pow(sum, 4));
		
		if (xWasNegative) {
			return -1.0 * toReturn;
		}
		else {
			return toReturn;
		}
	}
	
	public static double areaUnderNormalDistribution(double low, double high) {
		if (low > high) {
			throw new ArithmeticException("You can't take the integral from high to low, silly!");
		}
		if (low == high) {
			return 0;
		}
		
		// Calculated for me by WolframAlpha
		double lowCDF = 0.5 * erf(low / Math.sqrt(2.0));
		double highCDF = 0.5 * erf(high / Math.sqrt(2.0));
		
		return highCDF - lowCDF;
	}
	
}
