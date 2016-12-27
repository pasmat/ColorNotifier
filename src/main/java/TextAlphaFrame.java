import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

/**
 * Created by PasiMatalamaki on 27.12.2016.
 */
public class TextAlphaFrame extends JFrame {

    private JPanel contentPane;
    private JLabel textLabel;

    public TextAlphaFrame(String text, int x, int y, int width, int height) throws HeadlessException {
        textLabel = new JLabel(text, JLabel.CENTER);
        contentPane = new JPanel();

        contentPane.setLayout(null);
        contentPane.setBackground(new Color(0, 0, 0, 1));
        textLabel.setFont(new Font("Arial", Font.BOLD, 64));
        contentPane.add(textLabel);
        textLabel.setBounds(0, 0, width, height);
        setAlwaysOnTop(true);
        setBounds(x, y, width, height);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setBounds(0, 0, width, height);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

}
