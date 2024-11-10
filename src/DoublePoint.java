import java.util.Objects;

public class DoublePoint
{
    private double x, y;

    public DoublePoint(double inX, double inY)
    {
        x=inX;
        y=inY;
    }

    public DoublePoint()
    {
        this(0,0);
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public void setXY(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoublePoint that = (DoublePoint) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    @Override
    public String toString()
    {
        return "DoublePoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
