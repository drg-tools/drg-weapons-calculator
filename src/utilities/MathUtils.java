package utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

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
	
	private static ArrayList<Integer> factorialArrayList(int x) {
		if (x < 0) {
			return null;
		}
		
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		if (x == 0 || x == 1) {
			toReturn.add(1);
		}
		else {
			for (int i = x; i > 1; i--) {
				toReturn.add(i);
			}
		}
		return toReturn;
	}
	
	// Sourced from https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
	private static ArrayList<Integer> primeFactors(int n) {
		ArrayList<Integer> factors = new ArrayList<Integer>();
        for (int i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        if (n > 1) {
            factors.add(n);
        }
        return factors;
	}
	
	private static int product(ArrayList<Integer> factors) {
		int toReturn = 1;
		for (int i = 0; i < factors.size(); i++) {
			toReturn *= factors.get(i);
		}
		return toReturn;
	}
	
	// This method is an approximation of how this calculation is done by hand: cancel out as many terms as possible before doing the multiplication
	public static int optimizedChoose(int N, int x) {
		// N! / (x! * (N - x)!)
		if (N < 0 || x < 0 || x > N) {
			return -1;
		}
		
		// First, identify which half of the denominator will cancel out more of the numerator
		int largerDenominator, smallerDenominator, i, j;
		if (x > N - x) {
			largerDenominator = x;
			smallerDenominator = N - x;
		}
		else {
			largerDenominator = N - x;
			smallerDenominator = x;
		}
		
		// Second, cancel that factorial out of both the numerator and denominator
		ArrayList<Integer> numerator = new ArrayList<Integer>();
		for (i = N; i > largerDenominator; i--) {
			numerator.add(i);
		}
		ArrayList<Integer> denominator = factorialArrayList(smallerDenominator);
		
		// Third, iterate over numerator terms and cancel out terms from the denominator (horrible performance -- N^4 or worse)
		ArrayList<Integer> nFactors, dFactors;
		int currentNumeratorTerm, currentDenominatorTerm, nFactorsIndex, dFactorsIndex;
		boolean cancellationMade;
		for (i = 0; i < numerator.size(); i++) {
			currentNumeratorTerm = numerator.get(i);
			nFactors = primeFactors(currentNumeratorTerm);
			
			// Optimization: if the only factor of a term is itself (prime number), skip trying to cancel terms out
			if (nFactors.size() == 1 && nFactors.get(0) == currentNumeratorTerm) {
				continue;
			}
			
			for (j = 0; j < denominator.size(); j++) {
				currentDenominatorTerm = denominator.get(j);
				
				// Optimization: if a term in the denominator has already been canceled out to 1, skip it
				if (currentDenominatorTerm == 1) {
					continue;
				}
				
				dFactors = primeFactors(currentDenominatorTerm);
				
				for (nFactorsIndex = 0; nFactorsIndex < nFactors.size(); nFactorsIndex++) {
					
					cancellationMade = false;
					for (dFactorsIndex = 0; dFactorsIndex < dFactors.size(); dFactorsIndex++) {
						// Optimization: if either one of the current factors is a 1 from previous cancellation, skip to the next one
						if (nFactors.get(nFactorsIndex) == 1 || dFactors.get(dFactorsIndex) == 1) {
							continue;
						}
						
						// This is what actually does the cancelling out
						if (nFactors.get(nFactorsIndex) == dFactors.get(dFactorsIndex)) {
							nFactors.set(nFactorsIndex, 1);
							dFactors.set(dFactorsIndex, 1);
							cancellationMade = true;
						}
					}
					
					if (cancellationMade) {
						// Now that the numerator and denominator terms have been cancelled out, replace them with the smaller products to optimize this process on the next iteration
						// TODO: there's a little more logical optimization to be made here, but it's good enough for now.
						numerator.set(i, product(nFactors));
						denominator.set(j, product(dFactors));
					}
				}
			}
		}
		
		int numeratorProduct = product(numerator);
		int denominatorProduct = product(denominator);
		
		return numeratorProduct / denominatorProduct;
	}
	
	// This will calculate the probability that there are AT LEAST desiredNumberSuccesses successes across the total numberOfTrials
	// Used in Engineer/Shotgun stun chance calculation
	public static double cumulativeBinomialProbability(double probabilityOfSuccess, int numberOfTrials, int desiredNumberSuccesses) {
		// First, sanitize inputs
		if (probabilityOfSuccess < 0.0 || probabilityOfSuccess > 1.0) {
			return -1.0;
		}
		if (numberOfTrials < 1 || desiredNumberSuccesses < 0 || desiredNumberSuccesses > numberOfTrials) {
			return -1.0;
		}
		
		// This needs to total up the probability of all results where numberOfSucceses >= desiredNumberOfSuccesses
		double totalProbability = 0.0;
		
		for (int i = desiredNumberSuccesses; i < numberOfTrials + 1; i++) {
			totalProbability += binomialProbability(probabilityOfSuccess, numberOfTrials, i);
		}
		
		return totalProbability;
	}
	
	// This method is adapted from http://onlinestatbook.com/2/probability/binomial.html
	public static double binomialProbability(double probabilityOfSuccess, int numberOfTrials, int desiredNumberSuccesses) {
		// First, sanitize inputs
		if (probabilityOfSuccess < 0.0 || probabilityOfSuccess > 1.0) {
			return -1.0;
		}
		if (numberOfTrials < 1 || desiredNumberSuccesses < 0 || desiredNumberSuccesses > numberOfTrials) {
			return -1.0;
		}
		
		// http://onlinestatbook.com/2/probability/graphics/binomial_formula.gif
		double NchooseX = optimizedChoose(numberOfTrials, desiredNumberSuccesses);
		double probabilityOfSuccesses = Math.pow(probabilityOfSuccess, desiredNumberSuccesses);
		double probabilityOfFailures = Math.pow((1.0 - probabilityOfSuccess), (numberOfTrials - desiredNumberSuccesses));
		
		return NchooseX * probabilityOfSuccesses * probabilityOfFailures ;
	}
}
