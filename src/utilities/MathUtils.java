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
		return 1.0 / probability;
	}
	public static double medianRolls(double probability) {
		return 1.0 - (1.0 / log2(1.0 - probability));
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
	
	public static double[] vectorScalarMultiply(double scalar, double[] vector) {
		for (int i = 0; i < vector.length; i++) {
			vector[i] *= scalar;
		}
		
		return vector;
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
	
	public static int sum(int[] A) {
		if (A.length == 0) {
			return 0;
		}
		
		int sum = 0;
		for (int i = 0; i < A.length; i++) {
			sum += A[i];
		}
		return sum;
	}
	
	public static double probabilityInNormalDistribution(double minValue, double maxValue, double inputValue) {
		// Input Sanitization
		if (minValue >= maxValue) {
			return -1;
		}
		if (inputValue < minValue || inputValue > maxValue) {
			return -1;
		}
		
		// Step 1: convert inputValue to fit the range of [-2, 4] proportionally to minValue and maxValue acting as -2 and 4 respectively
		double x = 6.0 * (inputValue - minValue) / (maxValue - minValue) - 2.0;
		
		// Step 2: input X into the equation e^(-1/2(x-1)^2)/sqrt(2Pi)
		return Math.exp(-0.5 * Math.pow(x - 1.0, 2)) / Math.sqrt(2.0 * Math.PI);
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
	
	// Only need primes <= 33 for choose(N, x)
	private static int[] primeNumbersLessThanThirtyThree = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
	// Adapted from https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
	private static int[] convertNumberToPrimeFactors(int n) {
		int[] toReturn = new int[primeNumbersLessThanThirtyThree.length];
		
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
		double toReturn = 1.0;
		for (int i = 0; i < primeNumbersLessThanThirtyThree.length; i++) {
			if (factors[i] != 0) {
				toReturn *= Math.pow(primeNumbersLessThanThirtyThree[i], factors[i]);
			}
		}
		return (int) round(toReturn, 2);
	}
	
	private static int[] primeFactorMultiply(ArrayList<Integer> numbers) {
		int toReturn[] = new int[primeNumbersLessThanThirtyThree.length];
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
		int toReturn[] = new int[primeNumbersLessThanThirtyThree.length];
		
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = numerator[i] - denominator[i];
		}
		
		return convertPrimeFactorsToNumber(toReturn);
	}
	
	public static int choose(int N, int x) {
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
		
		// Third, evaluate the numerator and denominator into its prime factors
		int[] numeratorProduct = primeFactorMultiply(numerator);
		int[] denominatorProduct = primeFactorMultiply(denominator);
		
		// Finally, divide the two and convert it back to an int
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
		double NchooseX = choose(numberOfTrials, desiredNumberSuccesses);
		double probabilityOfSuccesses = Math.pow(probabilityOfSuccess, desiredNumberSuccesses);
		double probabilityOfFailures = Math.pow((1.0 - probabilityOfSuccess), (numberOfTrials - desiredNumberSuccesses));
		
		return NchooseX * probabilityOfSuccesses * probabilityOfFailures;
	}
	
	public static double areaOfLens(double R, double r, double d) {
		// Sourced from https://en.wikipedia.org/wiki/Lens_(geometry)
		double firstThird = Math.pow(r, 2) * Math.acos((Math.pow(d, 2) + Math.pow(r, 2) - Math.pow(R, 2)) / (2 * d * r));
		double secondThird = Math.pow(R, 2) * Math.acos((Math.pow(d, 2) + Math.pow(R, 2) - Math.pow(r, 2)) / (2 * d * R));
		double finalThird = 0.5 * Math.sqrt((-d + r + R) * (d - r + R) * (d + r - R) * (d + r + R));
		
		return firstThird + secondThird - finalThird;
	}
	
	public static double lambertInverseWNumericalApproximation(double z) {
		// Taylor Series approximation calculated for me by WolframAlpha, using input: Series[Re[ProductLog[-1, z]], {z, Center, 4}]
		// This should be a fair approximation for any z value between [-0.1, -0.001] and have an output of around [-9, -3.5]
		double zed, A, B, C, D, E, F;
		
		if (z > -0.006922) {
			/*
				Centered around -0.004341
				Re((-7.44753 - 266.09 (z + 0.004341) - 29911.2 (z + 0.004341)^2 - 4.55864×10^6 (z + 0.004341)^3 - 7.84078×10^8 (z + 0.004341)^4 - 1.4405×10^11 (z + 0.004341)^5
			*/
			zed = z + 0.004341;
			A = -7.44753;
			B = -266.09 * zed;
			C = -29911.2 * Math.pow(zed, 2);
			D = -4.55864 * Math.pow(10, 6) * Math.pow(zed, 3);
			E = -7.84078 * Math.pow(10, 8) * Math.pow(zed, 4);
			F = -1.4405 * Math.pow(10, 11) * Math.pow(zed, 5);
		}
		else if (z > -0.012084) {
			/*
				Centered around -0.009503
				Re((-6.53302 - 124.248 (z + 0.009503) - 6323.79 (z + 0.009503)^2 - 439586. (z + 0.009503)^3 - 3.45057×10^7 (z + 0.009503)^4 - 2.89403×10^9 (z + 0.009503)^5
			*/
			zed = z + 0.009503;
			A = -6.53302;
			B = -124.248 * zed;
			C = -6323.79 * Math.pow(zed, 2);
			D = -439586 * Math.pow(zed, 3);
			E = -3.45057 * Math.pow(10, 7) * Math.pow(zed, 4);
			F = -2.89403 * Math.pow(10, 9) * Math.pow(zed, 5);
		}
		else if (z > -0.017246) {
			/*
				Centered around -0.014665
				Re((-6.01686 - 81.7817 (z + 0.014665) - 2677.54 (z + 0.014665)^2 - 120506. (z + 0.014665)^3 - 6.12574×10^6 (z + 0.014665)^4 - 3.32788×10^8 (z + 0.014665)^5
			*/
			zed = z + 0.014665;
			A = -6.01686;
			B = -81.7817 * zed;
			C = -2677.54 * Math.pow(zed, 2);
			D = -120506 * Math.pow(zed, 3);
			E = -6.12574 * Math.pow(10, 6) * Math.pow(zed, 4);
			F = -3.32788 * Math.pow(10, 8) * Math.pow(zed, 5);
		}
		else if (z > -0.022408) {
			/*
				Centered around -0.019827
				Re((-5.65287 - 61.2761 (z + 0.019827) - 1473.89 (z + 0.019827)^2 - 49040.5 (z + 0.019827)^3 - 1.84292×10^6 (z + 0.019827)^4 - 7.40287×10^7 (z + 0.019827)^5
			*/
			zed = z + 0.019827;
			A = -5.65287;
			B = -61.2761 * zed;
			C = -1473.89 * Math.pow(zed, 2);
			D = -49040.5 * Math.pow(zed, 3);
			E = -1.84292 * Math.pow(10, 6) * Math.pow(zed, 4);
			F = -7.40287 * Math.pow(10, 7) * Math.pow(zed, 5);
		}
		else if (z > -0.02757) {
			/*
				Centered around -0.024989
				Re((-5.37018 - 49.1746 (z + 0.024989) - 932.406 (z + 0.024989)^2 - 24610.3 (z + 0.024989)^3 - 733474. (z + 0.024989)^4 - 2.33705×10^7 (z + 0.024989)^5
			*/
			zed = z + 0.024989;
			A = -5.37018;
			B = -49.1746 * zed;
			C = -932.406 * Math.pow(zed, 2);
			D = -24610.3 * Math.pow(zed, 3);
			E = -733474 * Math.pow(zed, 4);
			F = -2.33705 * Math.pow(10, 7) * Math.pow(zed, 5);
		}
		else if (z > -0.032732) {
			/*
				Centered around -0.030151
				Re((-5.13825 - 41.181 (z + 0.030151) - 643.035 (z + 0.030151)^2 - 14067.5 (z + 0.030151)^3 - 347338. (z + 0.030151)^4 - 9.17029×10^6 (z + 0.030151)^5
			*/
			zed = z + 0.030151;
			A = -5.13825;
			B = -41.181 * zed;
			C = -643.035 * Math.pow(zed, 2);
			D = -14067.5 * Math.pow(zed, 3);
			E = -347338 * Math.pow(zed, 4);
			F = -9.17029 * Math.pow(10, 6) * Math.pow(zed, 5);
		}
		else if (z > -0.037894) {
			/*
				Centered around -0.035313
				Re((-4.94109 - 35.5036 (z + 0.035313) - 470.333 (z + 0.035313)^2 - 8787.86 (z + 0.035313)^3 - 185186. (z + 0.035313)^4 - 4.17369×10^6 (z + 0.035313)^5
			*/
			zed = z + 0.035313;
			A = -4.94109;
			B = -35.5036 * zed;
			C = -470.333 * Math.pow(zed, 2);
			D = -8787.86 * Math.pow(zed, 3);
			E = -185186 * Math.pow(zed, 4);
			F = -4.17369 * Math.pow(10, 6) * Math.pow(zed, 5);
		}
		else if (z > -0.043056) {
			/*
				Centered around -0.040475
				Re((-4.76926 - 31.2614 (z + 0.040475) - 358.999 (z + 0.040475)^2 - 5855.3 (z + 0.040475)^3 - 107607. (z + 0.040475)^4 - 2.11556×10^6 (z + 0.040475)^5
			*/
			zed = z + 0.040475;
			A = -4.76926;
			B = -31.2614 * zed;
			C = -358.999 * Math.pow(zed, 2);
			D = -5855.3 * Math.pow(zed, 3);
			E = -107607 * Math.pow(zed, 4);
			F = -2.11556 * Math.pow(10, 6) * Math.pow(zed, 5);
		}
		else if (z > -0.048218) {
			/*
				Centered around -0.045637
				Re((-4.61672 - 27.9706 (z + 0.045637) - 283.019 (z + 0.045637)^2 - 4097.1 (z + 0.045637)^3 - 66748.8 (z + 0.045637)^4 - 1.16369×10^6 (z + 0.045637)^5
			*/
			zed = z + 0.045637;
			A = -4.61672;
			B = -27.9706 * zed;
			C = -283.019 * Math.pow(zed, 2);
			D = -4097.1 * Math.pow(zed, 3);
			E = -66748.8 * Math.pow(zed, 4);
			F = -1.16369 * Math.pow(10, 6) * Math.pow(zed, 5);
		}
		else if (z > -0.05338) {
			/*
				Centered around -0.050799
				Re((-4.47936 - 25.3432 (z + 0.050799) - 228.841 (z + 0.050799)^2 - 2979.24 (z + 0.050799)^3 - 43583.1 (z + 0.050799)^4 - 682537. (z + 0.050799)^5
			*/
			zed = z + 0.050799;
			A = -4.47936;
			B = -25.3432 * zed;
			C = -228.841 * Math.pow(zed, 2);
			D = -2979.24 * Math.pow(zed, 3);
			E = -43583.1 * Math.pow(zed, 4);
			F = -682537 * Math.pow(zed, 5);
		}
		else if (z > -0.058542) {
			/*
				Centered around -0.055961
				Re((-4.35425 - 23.197 (z + 0.055961) - 188.839 (z + 0.055961)^2 - 2234.61 (z + 0.055961)^3 - 29657.9 (z + 0.055961)^4 - 421585. (z + 0.055961)^5
			*/
			zed = z + 0.055961;
			A = -4.35425;
			B = -23.197 * zed;
			C = -188.839 * Math.pow(zed, 2);
			D = -2234.61 * Math.pow(zed, 3);
			E = -29657.9 * Math.pow(zed, 4);
			F = -421585 * Math.pow(zed, 5);
		}
		else if (z > -0.063704) {
			/*
				Centered around -0.061123
				Re((-4.23925 - 21.4111 (z + 0.061123) - 158.456 (z + 0.061123)^2 - 1719.47 (z + 0.061123)^3 - 20880.1 (z + 0.061123)^4 - 271733. (z + 0.061123)^5
			*/
			zed = z + 0.061123;
			A = -4.23925;
			B = -21.4111 * zed;
			C = -158.456 * Math.pow(zed, 2);
			D = -1719.47 * Math.pow(zed, 3);
			E = -20880.1 * Math.pow(zed, 4);
			F = -271733 * Math.pow(zed, 5);
		}
		else if (z > -0.068866) {
			/*
				Centered around -0.066285
				Re((-4.13273 - 19.9021 (z + 0.066285) - 134.828 (z + 0.066285)^2 - 1351.75 (z + 0.066285)^3 - 15124.9 (z + 0.066285)^4 - 181508. (z + 0.066285)^5
			*/
			zed = z + 0.066285;
			A = -4.13273;
			B = -19.9021 * zed;
			C = -134.828 * Math.pow(zed, 2);
			D = -1351.75 * Math.pow(zed, 3);
			E = -15124.9 * Math.pow(zed, 4);
			F = -181508 * Math.pow(zed, 5);
		}
		else if (z > -0.074028) {
			/*
				Centered around -0.071447
				Re((-4.03341 - 18.6105 (z + 0.071447) - 116.086 (z + 0.071447)^2 - 1082.22 (z + 0.071447)^3 - 11224.1 (z + 0.071447)^4 - 124974. (z + 0.071447)^5
			*/
			zed = z + 0.071447;
			A = -4.03341;
			B = -18.6105 * zed;
			C = -116.086 * Math.pow(zed, 2);
			D = -1082.22 * Math.pow(zed, 3);
			E = -11224.1 * Math.pow(zed, 4);
			F = -124974 * Math.pow(zed, 5);
		}
		else if (z > -0.07919) {
			/*
				Centered around -0.076609
				Re((-3.9403 - 17.4927 (z + 0.076609) - 100.963 (z + 0.076609)^2 - 880.165 (z + 0.076609)^3 - 8504.31 (z + 0.076609)^4 - 88323.7 (z + 0.076609)^5
			*/
			zed = z + 0.076609;
			A = -3.9403;
			B = -17.4927 * zed;
			C = -100.963 * Math.pow(zed, 2);
			D = -880.165 * Math.pow(zed, 3);
			E = -8504.31 * Math.pow(zed, 4);
			F = -88323.7 * Math.pow(zed, 5);
		}
		else if (z > -0.084352) {
			/*
				Centered around -0.081771
				Re((-3.85257 - 16.5164 (z + 0.081771) - 88.5805 (z + 0.081771)^2 - 725.715 (z + 0.081771)^3 - 6560.92 (z + 0.081771)^4 - 63854.8 (z + 0.081771)^5
			*/
			zed = z + 0.081771;
			A = -3.85257;
			B = -16.5164 * zed;
			C = -88.5805 * Math.pow(zed, 2);
			D = -725.715 * Math.pow(zed, 3);
			E = -6560.92 * Math.pow(zed, 4);
			F = -63854.8 * Math.pow(zed, 5);
		}
		else if (z > -0.089514) {
			/*
				Centered around -0.086933
				Re((-3.76958 - 15.6565 (z + 0.086933) - 78.3096 (z + 0.086933)^2 - 605.633 (z + 0.086933)^3 - 5142.3 (z + 0.086933)^4 - 47094. (z + 0.086933)^5
			*/
			zed = z + 0.086933;
			A = -3.76958;
			B = -15.6565 * zed;
			C = -78.3096 * Math.pow(zed, 2);
			D = -605.633 * Math.pow(zed, 3);
			E = -5142.3 * Math.pow(zed, 4);
			F = -47094 * Math.pow(zed, 5);
		}
		else if (z > -0.094676) {
			/*
				Centered around -0.092095
				Re((-3.69077 - 14.8938 (z + 0.092095) - 69.6926 (z + 0.092095)^2 - 510.87 (z + 0.092095)^3 - 4087.04 (z + 0.092095)^4 - 35350.9 (z + 0.092095)^5
			*/
			zed = z + 0.092095;
			A = -3.69077;
			B = -14.8938 * zed;
			C = -69.6926 * Math.pow(zed, 2);
			D = -510.87 * Math.pow(zed, 3);
			E = -4087.04 * Math.pow(zed, 4);
			F = -35350.9 * Math.pow(zed, 5);
		}
		else if (z > -0.099838) {
			/*
				Centered around -0.097257
				Re((-3.61568 - 14.213 (z + 0.097257) - 62.3892 (z + 0.097257)^2 - 435.093 (z + 0.097257)^3 - 3288.84 (z + 0.097257)^4 - 26957.1 (z + 0.097257)^5
			*/
			zed = z + 0.097257;
			A = -3.61568;
			B = -14.213* zed;
			C = -62.3892 * Math.pow(zed, 2);
			D = -435.093 * Math.pow(zed, 3);
			E = -3288.84 * Math.pow(zed, 4);
			F = -26957.1 * Math.pow(zed, 5);
		}
		else {
			/*
				Centered around -0.102419
				Re((-3.54392 - 13.6019 (z + 0.102419) - 56.1424 (z + 0.102419)^2 - 373.784 (z + 0.102419)^3 - 2675.96 (z + 0.102419)^4 - 20849. (z + 0.102419)^5
			*/
			zed = z + 0.102419;
			A = -3.54392;
			B = -13.6019* zed;
			C = -56.1424 * Math.pow(zed, 2);
			D = -373.784 * Math.pow(zed, 3);
			E = -2675.96 * Math.pow(zed, 4);
			F = -20849 * Math.pow(zed, 5);
		}
		
		return A + B + C + D + E + F;
	}
}
