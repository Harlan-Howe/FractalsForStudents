import java.util.Objects;

public class DoubleRectangle
{
    private double x,y,width, height;

    public DoubleRectangle(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public DoubleRectangle(DoublePoint p1, DoublePoint p2)
    {
        this.x = Math.min(p1.getX(),p2.getX());
        this.y = Math.min(p1.getY(),p2.getY());
        this.width = Math.abs(p1.getX()-p2.getX());
        this.height = Math.abs(p1.getY()-p2.getY());
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

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    // Convenience accessors.....
    public double getLeft()
    {   return x;}
    public double getRight()
    { return x+width; }
    public double getTop()
    { return y; }
    public double getBottom()
    { return y+height; }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleRectangle that = (DoubleRectangle) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.width, width) == 0 &&
                Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString()
    {
        return "DoubleRectangle{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
