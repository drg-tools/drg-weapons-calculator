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
	private static ArrayList<Integer> primeFactors(int n) {
		// Only need primes <= 33 for optimizedChoose, but 50 is a nice round number :-)
		int[] primeNumbersLessThanFifty = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47};
		
		ArrayList<Integer> factors = new ArrayList<Integer>();
		int currentPrime;
        for (int i = 0; i < primeNumbersLessThanFifty.length; i++) {
        	currentPrime = primeNumbersLessThanFifty[i];
        	if (currentPrime * currentPrime > n) {
        		// No need to check primes greater than the square root of n
        		break;
        	}
            while (n % currentPrime == 0) {
                factors.add(currentPrime);
                n /= currentPrime;
            }
        }
        // If the n has a remainder, it by definition will be a prime factor
        if (n > 1) {
            factors.add(n);
        }
        return factors;
	}
	
	private static int product(ArrayList<Integer> factors) {
		int toReturn = 1;
		int factor;
		for (int i = 0; i < factors.size(); i++) {
			factor = factors.get(i);
			if (factor > 1) {
				toReturn *= factor;
			}
			if (toReturn < 0) {
				throw new ArithmeticException("Int overflow");
			}
		}
		return toReturn;
	}
	
	/*
		Using normal means, Java's implementation of Integer can only do up to 12! before it overflows to a negative number. By writing out this method to cancel out common factors in the
		(N choose x) formula, just like we do by hand on paper, it can effectively do up to (34 choose 15) before Integer overflows. For simplicity's sake, let N <= 33 for this method.
		
		Un-optimized: 
			Gets: 2057, ArrayLists created: 256, Comparisons made: 2370, Values assigned: 1918, Sets: 130
			
		First draft of optimization:
			Gets: 1671, ArrayLists created: 111, Comparisons made: 2385, Values assigned: 1232, Sets: 112
		
		
	*/
	public static int optimizedChoose(int N, int x) {
		// N! / (x! * (N - x)!)
		if (N < 0 || x < 0 || x > N) {
			return -1;
		}
		
		// From testing, I found that int overflows at N == 34 && 16 <= x <= 18
		if (N > 33) {
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
		
		int getOperations = 0, arrayListCreations = 0, comparisonsMade = 0, valuesAssigned = 0, setOperations = 0;
		
		getOperations++;
		comparisonsMade += numerator.size();
		valuesAssigned += numerator.size();
		for (i = 0; i < numerator.size(); i++) {
			getOperations++;
			currentNumeratorTerm = numerator.get(i);
			arrayListCreations++;
			nFactors = primeFactors(currentNumeratorTerm);
			
			// Optimization: if the only factor of a term is itself (prime number), skip trying to cancel terms out
			getOperations += 2;
			comparisonsMade += 2;
			if (nFactors.size() == 1 && nFactors.get(0) == currentNumeratorTerm) {
				continue;
			}
			
			getOperations++;
			comparisonsMade += denominator.size();
			valuesAssigned += denominator.size();
			for (j = 0; j < denominator.size(); j++) {
				getOperations++;
				currentDenominatorTerm = denominator.get(j);
				
				// Optimization: if a term in the denominator has already been canceled out to 1, skip it
				comparisonsMade++;
				if (currentDenominatorTerm == 1) {
					continue;
				}
				
				// In order to get the two loops below fully optimized, I'm choosing to re-create the factors ArrayLists. It's sub-optimal memory usage and a medium-length operation,
				// but I'm hopeful that it will result in less cycles total
				arrayListCreations++;
				//nFactors = primeFactors(currentNumeratorTerm);
				dFactors = primeFactors(currentDenominatorTerm);
				
				getOperations++;
				comparisonsMade += nFactors.size();
				valuesAssigned += nFactors.size();
				for (nFactorsIndex = 0; nFactorsIndex < nFactors.size(); nFactorsIndex++) {
					
					valuesAssigned++;
					cancellationMade = false;
					
					getOperations++;
					comparisonsMade += dFactors.size();
					valuesAssigned += dFactors.size();
					for (dFactorsIndex = 0; dFactorsIndex < dFactors.size(); dFactorsIndex++) {
						// Optimization: if either one of the current factors is a 1 from previous cancellation, skip to the next one
						getOperations += 2;
						comparisonsMade += 2;
						if (nFactors.get(nFactorsIndex) == 1 || dFactors.get(dFactorsIndex) == 1) {
							continue;
						}
						
						// This is what actually does the cancelling out
						getOperations += 2;
						comparisonsMade++;
						if (nFactors.get(nFactorsIndex) == dFactors.get(dFactorsIndex)) {
							setOperations += 2;
							nFactors.set(nFactorsIndex, 1);
							dFactors.set(dFactorsIndex, 1);
							valuesAssigned++;
							cancellationMade = true;
						}
					}
					
					comparisonsMade++;
					if (cancellationMade) {
						// Now that the numerator and denominator terms have been cancelled out, replace them with the smaller products to optimize this process on the next iteration
						// TODO: there's a little more logical optimization to be made here, but it's good enough for now.
						setOperations += 2;
						numerator.set(i, product(nFactors));
						denominator.set(j, product(dFactors));
						//currentNumeratorTerm = product(nFactors);
						//currentDenominatorTerm = product(dFactors);
					}
				}
			}
		}
		
		valuesAssigned += 2;
		int numeratorProduct = product(numerator);
		// Because N and x are both <= 33, the denominator is always 1 because all those low numbers cancel out quite nicely. As N and x get bigger, the denominator can be > 1
		int denominatorProduct = product(denominator);
		
		System.out.println("Gets: " + getOperations + ", ArrayLists created: " + arrayListCreations + ", Comparisons made: " + comparisonsMade + ", Values assigned: " + valuesAssigned + ", Sets: " + setOperations);
		
		// I'm leaving this division in place on principle, even though it's always just dividing by 1 at such low numbers of N and x
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
		
		return NchooseX * probabilityOfSuccesses * probabilityOfFailures;
	}
}
