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
	
	// Adapted from https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
	private static int[] convertNumberToPrimeFactors(int n) {
		if (n > 33) {
			return null;
		}
		
		// Only need primes <= 33 for smartChoose
		int[] primeNumbersLessThanThirtyThree = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
		int[] toReturn = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		
		int currentPrime;
        for (int i = 0; i < primeNumbersLessThanThirtyThree.length; i++) {
        	currentPrime = primeNumbersLessThanThirtyThree[i];
        	while (n > 1 && n % currentPrime == 0) {
                toReturn[i]++;
                n /= currentPrime;
            }
        }
        
        return toReturn;
	}
	
	private static int convertPrimeFactorsToNumber(int[] factors) {
		// Only need primes <= 33 for smartChoose
		int[] primeNumbersLessThanThirtyThree = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
		
		double toReturn = 1.0;
		for (int i = 0; i < primeNumbersLessThanThirtyThree.length; i++) {
			if (factors[i] != 0) {
				toReturn *= Math.pow(primeNumbersLessThanThirtyThree[i], factors[i]);
			}
		}
		return (int) round(toReturn, 2);
	}
	
	private static int[] primeFactorMultiply(ArrayList<Integer> numbers) {
		int toReturn[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] primeFactors;
		int j;
		for (int i = 0; i < numbers.size(); i++) {
			primeFactors = convertNumberToPrimeFactors(numbers.get(i));
			for(j = 0; j < primeFactors.length; j++) {
				toReturn[j] += primeFactors[j];
			}
		}
		return toReturn;
	}
	
	private static int primeFactorDivide(int[] numerator, int[] denominator) {
		int toReturn[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = numerator[i] - denominator[i];
		}
		
		return convertPrimeFactorsToNumber(toReturn);
	}
	
	public static int smartChoose(int N, int x) {
		// N! / (x! * (N - x)!)
		if (N < 0 || x < 0 || x > N) {
			return -1;
		}
		
		// From testing, I found that int overflows at N == 34 && 16 <= x <= 18
		if (N > 33) {
			return -1;
		}
		
		// First, identify which half of the denominator will cancel out more of the numerator
		int largerDenominator, smallerDenominator, i;
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
		
		int[] numeratorProduct = primeFactorMultiply(numerator);
		int[] denominatorProduct = primeFactorMultiply(denominator);
		
		return primeFactorDivide(numeratorProduct, denominatorProduct);
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
		double NchooseX = smartChoose(numberOfTrials, desiredNumberSuccesses);
		double probabilityOfSuccesses = Math.pow(probabilityOfSuccess, desiredNumberSuccesses);
		double probabilityOfFailures = Math.pow((1.0 - probabilityOfSuccess), (numberOfTrials - desiredNumberSuccesses));
		
		return NchooseX * probabilityOfSuccesses * probabilityOfFailures;
	}
}
