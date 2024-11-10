public class FractalCalculator
{

    private final int  countThreshold = 1000; // how many "jumps" will the point make before we assume it will never go out of bounds
    private final double  radiusThreshold = 100;  // the square of the radius of the "out of bounds" zone.

    /**
     * Calculates the number for a given mathematical point on the screen, which will later be converted into a color
     * (in a different method) - it bounces a point around the complex plane via a mathematical formula and sees how
     * many steps it takes to leave the vicinity of the origin. This will have a ceiling of countThreshold steps - if it
     * still hasn't exited by then, it will return -1.
     *
     * @param c       the complex number in question.
     * @return the count of steps it takes to get "away" from the origin, or -1 if it reached countThreshold.
     */
    public int getCountForComplexNumber(ComplexNumber c)
    {
        // TODO #1 - you need to write this.
        return -1; // temporary for stub function.
    }
}


