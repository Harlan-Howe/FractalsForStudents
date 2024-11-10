import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class FractalPane extends JPanel implements MouseListener, MouseMotionListener
{

    private BufferedImage workspace; // a window-sized chunk of memory where we'll draw the fractal. Periodically, we'll
                                     //        copy this to the window.
    private final Object workspaceMutex;  // a "mutex" (a.k.a. "traffic control") that ensures only one thing
                                     //        uses/changes the workspace at a time. (Definitely not on the AP.)

    ScalingManager scaleManager;

    private boolean isDragging;  // is the user currently dragging a selection box?
    private DoublePoint dragStart, dragEnd; // the location in the window where the drag started and where it is currently
                                            //      (or where it just ended).

    private CalculationThread calculationThread;  // An instance of a class that will execute a run() function
                                                  //      simultaneously with the code in this class.
                                                  //      (Definitely not on the AP.)

    public FractalPane()
    {
        super();
        scaleManager = new ScalingManager();

        workspace = new BufferedImage((int)scaleManager.getWindowBounds().getWidth(),(int)scaleManager.getWindowBounds().getHeight(),BufferedImage.TYPE_INT_RGB);
        workspaceMutex = new Object();

        isDragging = false;

        calculationThread = new CalculationThread();
        calculationThread.start();

        dragStart = new DoublePoint();
        dragEnd = new DoublePoint();

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

    }

    public void paintComponent(Graphics g)
    {
        // wait for the workspace to be available, then "lock" it so that we can use it briefly.
        synchronized (workspaceMutex)
        {
            g.drawImage(workspace,0,0,null);
        } // (ok, we're done with it.)

        // if the user is dragging a box, we can now draw the box on top of the copied workspace.
        if (isDragging)
        {
            //pick a random color black, red, green, blue, yellow, cyan, magenta, or white.
            g.setColor(new Color(255*(int)(Math.random()+0.5),255*(int)(Math.random()+0.5),255*(int)(Math.random()+0.5)));

            DoubleRectangle box = new DoubleRectangle(dragStart,dragEnd);
            g.drawRect((int)box.getX(), (int)box.getY(),(int)box.getWidth(), (int)box.getHeight());
        }

        // if the calculation thread is doing a calculation pass, draw a little red dot in the upper left corner.
        if (calculationThread.isScanning())
        {
            g.setColor(Color.RED);
            g.fillOval(2,2,10,10);
            g.setColor(Color.BLACK);
            g.drawOval(2,2,10,10);
        }

    }

    @Override
    /**
     * the user has the mouse button pressed and the mouse has just changed position. This is the response.
     * Since we are in the midst of a drag operation, update the drag area on the screen.
     */
    public void mouseDragged(MouseEvent e) //overriding method in MouseMotionListener interface
    {
        dragEnd.setXY(e.getX(),e.getY());
        repaint();
    }

    @Override
    /**
     * The user has the mouse button unpressed and the mouse just changed position. This is the response.
     */
    public void mouseMoved(MouseEvent e) //overriding method in MouseMotionListener interface
    {
        // Nothing in this one - we don't have any plans to do anything when the mouse moves and is not dragged, but we
        //    are committed to having this method.

    }

    @Override
    /**
     * The user has just released the mouse button in the same place as where it was pressed. This is the response.
     */
    public void mouseClicked(MouseEvent e) //overriding method in MouseListener interface
    {
        // Nothing in this one, either. We aren't particularly interested in the user releasing at the same place as
        //    he/she pressed the mouse, but (again) we are committed to having this method.
    }

    @Override
    /**
     * The user has just changed the mouse button from unpressed to pressed. This is the response.
     * The user is starting a drag... so start handling the drag.
     */
    public void mousePressed(MouseEvent e) //overriding method in MouseListener interface
    {
        isDragging = true;
        dragStart.setXY(e.getX(),e.getY());
        dragEnd.setXY(e.getX(),e.getY());
        repaint();
    }

    @Override
    /**
     * The user has just changed the mouse button from pressed to unpressed. This is the response.
     * Since this would suggest that the user has been dragging a zoom box, and now it is done, we want to perform the
     * zoom.
     */
    public void mouseReleased(MouseEvent e) //overriding method in MouseListener interface
    {
        dragEnd.setXY(e.getX(),e.getY());
        isDragging = false;
        if (dragEnd.getX() != dragStart.getX() && dragStart.getY() != dragEnd.getY())
        {
            if (e.isShiftDown())
                zoomOut();
            else
                zoomIn();
            repaint();
        }
    }

    @Override
    /**
     * the user has just moved the mouse into this panel. This is the response
     */
    public void mouseEntered(MouseEvent e) //overriding method in MouseListener interface
    {
        // Nope, not interested in this one, either.
    }

    @Override
    /**
     * the user has just moved the mouse out of this panel. This is the response.
     * In this case, we want to cancel the drag.
     */
    public void mouseExited(MouseEvent e) //overriding method in MouseListener interface
    {
        isDragging = false;
        repaint();
    }

    /**
     * The user has just released the mouse while the shift key was down, and we want to change the mathematical bounds
     * so that the corners of the window appear to "contract" into the selected area.
     */
    public void zoomOut()
    {
        // make a DoubleRectangle of dragStart and dragEnd locations. This will automatically make it think in terms of
        //      (x,y) of top left corner and (width/length) - even if the dragEnd isn't below and to the right of dragStart.
        DoubleRectangle dragRect = new DoubleRectangle(dragStart,dragEnd);

        // Calculate the new mathBounds we would need at the window's corners to shrink the current math bounds
        //     (currently at the window's corners)  to get shrunk in so that they are found at the locations in the
        //     window set by the dragged rect.
        //     Note: we will use the original mathBounds for all four computations, we don't want to change mathBounds
        //     until we are done with all four.
        //     Also note: this is similar to zoomIn, but is different.
        DoubleRectangle mathBounds = scaleManager.getMathBounds();
        DoubleRectangle windowBounds = scaleManager.getWindowBounds();
        double tempMinX = scaleManager.map(0,dragRect.getLeft(),dragRect.getRight(),mathBounds.getLeft(),mathBounds.getRight());
        double tempMaxX = scaleManager.map(windowBounds.getRight(),dragRect.getLeft(),dragRect.getRight(),mathBounds.getLeft(),mathBounds.getRight());
        double tempMinY = scaleManager.map(0,dragRect.getTop(),dragRect.getBottom(),mathBounds.getTop(),mathBounds.getBottom());
        double tempMaxY = scaleManager.map(windowBounds.getBottom(),dragRect.getTop(),dragRect.getBottom(),mathBounds.getTop(),mathBounds.getBottom());
        scaleManager.setMathBounds(new DoubleRectangle(tempMinX,tempMinY,tempMaxX-tempMinX,tempMaxY-tempMinY));


        // ---- The rest of this is just fancy. When we zoom out a step, instead of just restarting our scan with a blank
        //          screen, we are going to copy a scaled-down version of the window into the dragged area, so we can
        //          see the relationship between the previous view and this one as it loads.

        // find the ratio between the size of the dragged box and the size of the window. This will be used to determine
        //     the spacing of the pixels we'll be copying.
        double horizontalRatio = windowBounds.getWidth()/dragRect.getWidth();
        double verticalRatio = windowBounds.getHeight()/dragRect.getHeight();

        // wait until the workspace is available, and then lock it so that others can't mess with it for a while.
        synchronized ((workspaceMutex))
        {
            //Make a copy of the workspace.
            BufferedImage tempImage = new BufferedImage(workspace.getWidth(),workspace.getHeight(),BufferedImage.TYPE_INT_RGB);
            Graphics temp_g = tempImage.getGraphics();
            temp_g.drawImage(workspace,0,0,null);

            // for each pixel in the dragged rect (which is smaller than the window) copy a pixel from the workspace copy
            // into the shrunk-down area in the real workspace.
            for (int i=(int)dragRect.getLeft(); i<dragRect.getRight(); i++)
                for (int j = (int)dragRect.getTop(); j<dragRect.getBottom(); j++)
                {
                    workspace.setRGB(i,j,tempImage.getRGB((int)((i-dragRect.getLeft())*horizontalRatio),(int)((j-dragRect.getTop())*verticalRatio)));

                }

        }//release the workspace lock!
        // -----

        // Tell the calculation thread that it should stop the current scan (if any) and start on a new one.
        calculationThread.resetScan();

        // we've made changes to the workspace - make sure they get drawn to the screen the next chance we get!
        repaint();
    }

    /**
     * The user just released the mouse to select a drag area - we want the computer to "expand" this area to the size
     * of the window and scan that area in more detail.
     */
    public void zoomIn()
    {
        // make a DoubleRectangle of dragStart and dragEnd locations. This will automatically make it think in terms of
        //      (x,y) of top left corner and (width/length) - even if the dragEnd isn't below and to the right of dragStart.
        DoubleRectangle dragRect = new DoubleRectangle(dragStart,dragEnd);

        // Calculate the mathematical equivalences of the left and right "x" values and the top and bottom "y" values.
        //     Since these calculations depend on mathBounds for all four computations, we don't want to change mathBounds
        //     until we are done with all four. NOTE: this is similar to ZoomOut, but different.
        DoubleRectangle mathBounds = scaleManager.getMathBounds();
        DoubleRectangle windowBounds = scaleManager.getWindowBounds();
        double tempMinX = scaleManager.map(dragRect.getX(), 0, windowBounds.getWidth(), mathBounds.getX(),mathBounds.getX()+mathBounds.getWidth());
        double tempMaxX = scaleManager.map(dragRect.getX()+dragRect.getWidth(), 0, windowBounds.getWidth(), mathBounds.getX(),mathBounds.getX()+mathBounds.getWidth());
        double tempMinY = scaleManager.map(dragRect.getY(), 0, windowBounds.getHeight(), mathBounds.getY(),mathBounds.getY()+mathBounds.getHeight());
        double tempMaxY = scaleManager.map(dragRect.getY()+dragRect.getHeight(), 0, windowBounds.getHeight(), mathBounds.getY(),mathBounds.getY()+mathBounds.getHeight());
        scaleManager.setMathBounds(new DoubleRectangle(tempMinX,tempMinY,tempMaxX-tempMinX,tempMaxY-tempMinY));
        System.out.println(mathBounds);

        // ---- The rest of this is just fancy. When we zoom in a step, instead of just restarting our scan with a blank
        //          screen, we are going to copy a blown-up version of the image in the drag window into the window's
        //          workspace, so we can see the relationship between the previous view and this one as it loads.

        // find the ratio between the size of the dragged box and the size of the window. This will be used to determine
        //     the size of the boxes we'll be drawing.
        double horizontalRatio = windowBounds.getWidth()/dragRect.getWidth();
        double verticalRatio = windowBounds.getHeight()/dragRect.getHeight();

        // make a blank BufferedImage the size of the draggedRect we're going to copy that portion of the workspace into
        //     it in a moment.
        BufferedImage tempImage = new BufferedImage((int)(dragRect.getWidth()),
                                                    (int)dragRect.getHeight(),
                                                    BufferedImage.TYPE_INT_RGB);
        Graphics temp_g = tempImage.getGraphics();

        // wait until the workspace is available, and then lock it so that others can't mess with it for a while.
        synchronized (workspaceMutex)
        {
            // copy the dragRectangle area from the current workspace into the temp BI we just created.
            temp_g.drawImage(workspace.getSubimage( (int)dragRect.getX(),
                                                    (int)dragRect.getY(),
                                                    (int)dragRect.getWidth(),
                                                    (int)dragRect.getHeight()),
                             0, 0, null);

            // draw boxes of color into the workspace that correspond to the pixels in the temp image.
            Graphics work_g = workspace.getGraphics();
            for (int i=(int)dragRect.getWidth()-1; i>-1; i--)
                for (int j=(int)dragRect.getHeight()-1; j>-1; j--)
                {
                    work_g.setColor(new Color(tempImage.getRGB(i,j)));
                    work_g.fillRect((int)(i*horizontalRatio),(int)(j*verticalRatio),(int)horizontalRatio+1,(int)verticalRatio+1);
                }
        } //release the workspace lock!
        // -----

        // Tell the calculation thread that it should stop the current scan (if any) and start on a new one.
        calculationThread.resetScan();

        // we've made changes to the workspace - make sure they get drawn to the screen the next chance we get!
        repaint();
    }


    //------------------------------------------------------------------------- Calculation Thread
    // This is using two things that definitely aren't on the AP test - an internal class that extends Thread so that we
    //     can multitask.
    // The fact that this class is defined INSIDE the FractalPane class means that a) only FractalPane knows about this
    //     class, and b) It has access to the private variables and methods of FractalPane, as if they were its own.
    // The fact that it extends Thread means that the run() method can be operating at the same time as the other things
    //     in FractalPane (and the rest of the program) are happening. So we can do an extended set of calculations and
    //     updating the screen at the same time - this is what makes the screen seem to "live update" instead of just
    //     popping a finished fractal on the screen at the end of the (time-consuming) scan. We can also interrupt the
    //     calculation mid-loop.
    //     Note: we never call "run()" directly - the thread is activated by calling the Thread method "start()" - which
    //           calls run(), itself.
    class CalculationThread extends Thread
    {
        private int currentX, currentY;
        DoublePoint cp = new DoublePoint();
        DoublePoint sp = new DoublePoint();

        DoubleRectangle windowBounds;
        ColorConverter converter;
        FractalCalculator calculator;

        private boolean needsReset;
        private boolean isScanning;

        public CalculationThread()
        {
            super();
            currentX = 0;
            currentY = 0;
            cp = new DoublePoint();
            sp = new DoublePoint();
            needsReset = false;
            isScanning = false;
        }

        /**
         * stops the current scan, if there is one active, and starts a new scan, from the top.
         */
        public void resetScan()
        {
            needsReset = true;
        }

        public boolean isScanning()
        {
            return isScanning;
        }

        /**
         * This is the method that will start running simultaneously with the main program when we say start().
         */
        public void run()
        {
            ComplexNumber c;
            windowBounds = scaleManager.getWindowBounds();
            converter = new ColorConverter();
            calculator = new FractalCalculator();

            isScanning = false;
            needsReset = true;

            while (true)
            {
                if (needsReset)
                {
                    needsReset = false;
                    isScanning = true;
                    CalculateFractal();
                }
                if (isScanning) // if we got here, we're not scanning anymore; we might have been interrupted.
                                // If we just finished scanning, update the indicator
                {
                    isScanning = false; //(This deactivates the little red dot in the corner)
                    repaint();   // update the screen at the next opportunity.
                }
                try
                {
                Thread.sleep(250); // chill out for 1/4 second. This means that we will only be considering whether
                                    // to recalculate the screen that often, if the scan is complete.
                } catch (InterruptedException iExp)  // in case something cancels the program in the 1/4 second.
                {
                    System.out.println("Thread interrupted.");
                }
            }
        }

        /**
         * Performs the calculations to fill in the workspace with a fractal
         */
        private void CalculateFractal()
        {
            ComplexNumber c;
            int count;
            Color pixelColor;

            // TODO #3: Loop over all pixels on this screen: calculate the color and set the color for that pixel.
            // Please use variables "currentX" and "currentY" for the pixel coordinates. (Perhaps instead of "i"
            // and "j"?)
            // Hint: make use of windowBounds.getWidth() and getHeight().
            // The remainder of this method has the stuff that should happen inside the loop(s).

                    if (needsReset) // if this scan is canceled (in favor of another one), leave this loop early.
                        return;

                    // find the complex number corresponding to this pixel.
                    c = ConvertCurrentXYtoComplex();
                    count = calculator.getCountForComplexNumber(c); // bounce around until you go out of range...
                    pixelColor = converter.colorMap(count); // convert the number of bounces to a color...

                    // draw that color at this location.
                    setPixelToColor(pixelColor);

                // The next time it gets a chance, the main thread should update the screen. We only do this
                //    ONCE PER LINE, because the program runs too slow if it is drawing after (almost) every
                //    pixel.
                repaint();
        }

        /**
         * sets the color of the pixel at (currentX, currentY) to the given pixelColor.
         * @param pixelColor - color to set the pixel to
         */
        private void setPixelToColor(Color pixelColor)
        {
            synchronized (workspaceMutex) // wait until you can "lock" the workspace image - this prevents
                                          // simultaneous use.
            {
                workspace.setRGB(currentX, currentY, pixelColor.getRGB()); // put the color in for this pixel location.
            } // ok, I'm finished with the workspace for the moment.
        }

        /**
         * fills a box with upper left corner (currentX, currentY) with the given color
         * @param width - width of the box, in pixels
         * @param height - height of the box, in pixels
         * @param boxColor - color of the box
         */
        private void setBoxToColor(int width, int height, Color boxColor)
        {
            synchronized (workspaceMutex) // wait until you can "lock" the workspace image - this prevents
                                          // simultaneous use.
            {
                Graphics wg = workspace.getGraphics();
                wg.setColor(boxColor);
                wg.fillRect(currentX, currentY, width, height); // draw the box in this color.
            } // ok, I'm finished with the workspace for the moment.
        }

        /**
         * convert the onscreen (currentX, currentY) to a complex number
         * (Note: we're using the fields (currentX, currentY), not passing (x, y) as a parameter to save time allocating and
         * copying the values.)
         * @return - the complex number corresponding to the mathematical field at (currentX, currentY).
         */
        private ComplexNumber ConvertCurrentXYtoComplex()
        {
            sp.setXY(currentX, currentY);
            scaleManager.mathPointForScreenPoint(cp, sp);
            return new ComplexNumber(cp);
        }
    }
}
