package drgtools.dpscalc.modelPieces;

/*
    Many of DRG's weapons use Cubic Curves of some kind. In the gamefiles, they show the set inflection points,
    but the actual polynomial equation was nowhere to be found. In Sept 2023, I figured out a way to use WolframAlpha
    to take in the inflection points and then return the cubic polynomial function!

    With this new tech unlocked, I decided to create this utility class to represent each of the cubic curve splines
    in DRG, in order to make it easier to model them in this program.
*/
public abstract class CubicCurve {
    protected double a;  // Coefficient of x^3
    protected double b;  // Coefficient of x^2
    protected double c;  // Coefficient of x^1
    protected double d;  // Coefficient of x^0

    // The bounds of the cubic curve, in which the polynomial equation is correct.
    protected double minX;
    protected double maxX;
    protected double minY;
    protected double maxY;

    public double getY(double x) {
        if (x < minX || x > maxX) {
            return -1;
        }
        else {
            return a * Math.pow(x, 3) + b * Math.pow(x, 2) + c * x + d;
        }
    }

    // Sourced from https://math.vanderbilt.edu/schectex/courses/cubic/
    // This is effectively the "cubic" version of the Quadratic Formula
    public double getX(double y) {
        if (y < minY || y > maxY) {
            return -1;
        }
        else {
            double p = -b / (3 * a);
            double q = Math.pow(p, 3) + (b * c - 3 * a * (d-y)) / (6 * Math.pow(a, 2));
            double r = c / (3 * a);
            double s = Math.sqrt(Math.pow(q,2) + Math.pow(r - Math.pow(p,2),3));

            return Math.cbrt(q + s) + Math.cbrt(q - s) + p;
        }
    }
}
