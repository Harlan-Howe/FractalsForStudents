import javax.swing.*;
import java.awt.*;

public class FractalFrame extends JFrame
{

    private final FractalPane mainPane;

    public FractalFrame()
    {
        super("Fractals!");
        setSize(800,800);
        getContentPane().setLayout(new GridLayout(1,1));
        mainPane = new FractalPane();
        getContentPane().add(mainPane);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }




}
