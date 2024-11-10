import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectTester extends JFrame implements ActionListener
{
    public static void main(String[] args)
    {
        ProjectTester app = new ProjectTester();
    }

    private JButton smallerButton, largerButton;
    private ColorSweepPanel csPanel;

    public ProjectTester() throws HeadlessException
    {
        super("Tester");

        testScalingManager();
        testFractalCalculator();
        System.out.println("Now making color tester. The window should have a smooth blend of colors or grey, with _occasional_ jumps.");
        makeColorTesterWindow();
    }

    public void testScalingManager()
    {
        System.out.println("---------------------------- Testing Scaling Manager.");

        DoubleRectangle screenRect = new DoubleRectangle(0,0,800,790);
        DoubleRectangle mathRect = new DoubleRectangle(-1,-1.5,2,2);

        ScalingManager manager = new ScalingManager(screenRect,mathRect);
        DoublePoint mp = new DoublePoint();
        DoublePoint sp = new DoublePoint(250,170);

        manager.mathPointForScreenPoint(mp,sp);
        System.out.println("A Expected DoublePoint{x=-0.375, y=-1.0696202531645569}. Got "+mp);

        sp.setXY(793,485);
        manager.mathPointForScreenPoint(mp,sp);
        System.out.println("B Expected DoublePoint{x=0.9824999999999999, y=-0.2721518987341771}. Got "+mp);

        sp.setXY(0,0);
        manager.mathPointForScreenPoint(mp,sp);
        System.out.println("C Expected DoublePoint{x=-1.0, y=-1.5}. Got "+mp);

        sp.setXY(47,689);
        manager.mathPointForScreenPoint(mp,sp);
        System.out.println("D Expected DoublePoint{x=-0.8825000000000001, y=0.2443037974683544}. Got "+mp);

        sp.setXY(-200,1085.5);
        manager.mathPointForScreenPoint(mp,sp);
        System.out.println("E Expected DoublePoint{x=-1.5, y=1.2481012658227848}. Got "+mp);

    }

    void testFractalCalculator()
    {
        System.out.println("----------------------------- Testing Fractal Calculator");
        FractalCalculator calculator = new FractalCalculator();

        System.out.println("Note: Because fractals are notoriously sensitive to slight changes, it is possible that you"+
                " will find\n that there is some variation on the values that are expecting more than, say, 10.");

        ComplexNumber c = new ComplexNumber(-0.35,+0.47);
        System.out.println("1 Expected -1. Got "+calculator.getCountForComplexNumber(c)+".");

        c = new ComplexNumber(-0.45,+0.825);
        System.out.println("2 Expected 7. Got "+calculator.getCountForComplexNumber(c)+".");

        c = new ComplexNumber(+0.25,+0.975);
        System.out.println("3 Expected 5. Got "+calculator.getCountForComplexNumber(c)+".");

        c = new ComplexNumber(-2,+6);
        System.out.println("4 Expected 2. Got "+calculator.getCountForComplexNumber(c)+".");

        c = new ComplexNumber(-0.95,+0.2475);
        System.out.println("5 Expected 648. Got "+calculator.getCountForComplexNumber(c)+".");
    }

    public void makeColorTesterWindow()
    {
        setSize(600,100);
        getContentPane().setLayout(new BorderLayout());
        csPanel = new ColorSweepPanel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1,2));
        smallerButton = new JButton("<");
        smallerButton.addActionListener(this);
        largerButton = new JButton(">");
        largerButton.addActionListener(this);
        bottomPanel.add(smallerButton);
        bottomPanel.add(largerButton);
        getContentPane().add(csPanel,BorderLayout.CENTER);
        getContentPane().add(bottomPanel,BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == smallerButton)
            csPanel.decreaseOffset();
        if (e.getSource() == largerButton)
            csPanel.increaseOffset();
    }


    class ColorSweepPanel extends JPanel
    {
        private int offset;
        private ColorConverter converter;

        public ColorSweepPanel()
        {
            offset = 0;
            converter = new ColorConverter();
        }

        public void paintComponent(Graphics g)
        {
            for (int i=0; i<getWidth(); i++)
            {
                g.setColor(converter.colorMap(i+offset));
                g.drawLine(i,0,i,getHeight());
            }
            g.setColor(Color.BLACK);
            g.drawString(""+offset,4,10);
            g.drawString(""+(offset+getWidth()),getWidth()-40,10);
            g.setColor(Color.WHITE);
            g.drawString(""+offset,3,9);
            g.drawString(""+(offset+getWidth()),getWidth()-41,9);
        }

        public void increaseOffset()
        {
            offset+=10;
            repaint();
        }

        public void decreaseOffset()
        {
            offset -= 10;
            offset = Math.max(offset,0);
            repaint();
        }

    }
}
