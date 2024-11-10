public class ComplexNumber
{
    private double realCoefficient;
    private double imaginaryCoefficient;

    public ComplexNumber(double real, double img)
    {
        realCoefficient = real;
        imaginaryCoefficient = img;
    }

    public ComplexNumber(DoublePoint pt)
    {
        this(pt.getX(),pt.getY());
    }

    public ComplexNumber()
    {
        this(0.0,0.0);
    }

    public static ComplexNumber one()
    {
        return new ComplexNumber(1,0);
    }

    public static ComplexNumber i()
    {
        return new ComplexNumber(0,1);
    }

    public static ComplexNumber zero()
    {
        return new ComplexNumber(0,0);
    }

    public double getRealCoefficient()
    {
        return realCoefficient;
    }

    public double getImaginaryCoefficient()
    {
        return imaginaryCoefficient;
    }

    public double real()
    {
        return realCoefficient;
    }

    public double imaginary()
    {
        return imaginaryCoefficient;
    }

    public ComplexNumber plus(ComplexNumber b)
    {
        return new ComplexNumber(this.realCoefficient+b.realCoefficient, this.imaginaryCoefficient+b.imaginaryCoefficient);
    }

    public ComplexNumber times(ComplexNumber b)
    {
        return new ComplexNumber(this.realCoefficient * b.realCoefficient - this.imaginaryCoefficient*b.imaginaryCoefficient,
                                 this.realCoefficient * b.imaginaryCoefficient + this.imaginaryCoefficient*b.realCoefficient);
    }

    public ComplexNumber times(double r)
    {
        return new ComplexNumber(this.realCoefficient*r, this.imaginaryCoefficient*r);
    }

    public ComplexNumber squared()
    {
        return this.times(this);
    }


    /**
     * the norm is the "distance" from the origin to this point in the complex plane, this method returns the square of that.
     * We use the square because to find the norm, you use pythagoras, which involves a square root. Square roots are slow,
     * so if we don't take the root, we get the norm squared, which takes (much) less time to calculate.
     * @return the square of the norm of this complex number.
     */
    public double normSquared()
    {
        return realCoefficient*realCoefficient + imaginaryCoefficient*imaginaryCoefficient;
    }

    @Override
    public String toString()
    {
        // the String.format("%6.3e", x) command makes the computer print the number in scientific notation with three
        // decimal places and as many as 6 leading digits. (The 6 is overkill.)

        return "(" +String.format("%6.3e",realCoefficient) +
                ", "+ String.format("%6.3e",imaginaryCoefficient) +
                "i)";
    }
}
