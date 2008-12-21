package net.miginfocom.examples;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Mikael Grev, MiG InfoCom AB
 *         Date: Apr 20, 2008
 *         Time: 10:32:58 PM
 */
public class JavaOneShrink
{
	private static JComponent createPanel(String ... args)
	{
		JPanel panel = new JPanel(new MigLayout("nogrid"));

		panel.add(createTextArea(args[0].replace(", ", "\n    ")), args[0] + ", w 200");
		panel.add(createTextArea(args[1].replace(", ", "\n    ")), args[1] + ", w 200");
		panel.add(createTextArea(args[2].replace(", ", "\n    ")), args[2] + ", w 200");
		panel.add(createTextArea(args[3].replace(", ", "\n    ")), args[3] + ", w 200");

		JSplitPane gSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, panel, new JPanel());
		gSplitPane.setOpaque(true);
		gSplitPane.setBorder(null);

		return gSplitPane;
	}

	private static JComponent createTextArea(String s)
	{
		JTextArea ta = new JTextArea("\n\n    " + s, 6, 20);
		ta.setBorder(new LineBorder(new Color(200, 200, 200)));
		ta.setFont(new Font("Helvetica", Font.BOLD, 20));
		ta.setMinimumSize(new Dimension(20, 20));
		ta.setFocusable(false);
		return ta;
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				JFrame frame = new JFrame("JavaOne Shrink Demo");
				Container cp = frame.getContentPane();
				cp.setLayout(new MigLayout("wrap 1"));

				cp.add(createPanel("", "", "", ""));
				cp.add(createPanel("shrinkprio 1", "shrinkprio 1", "shrinkprio 2", "shrinkprio 3"));
				cp.add(createPanel("shrink 25", "shrink 50", "shrink 75", "shrink 100"));
				cp.add(createPanel("shrinkprio 1, shrink 50", "shrinkprio 1, shrink 100", "shrinkprio 2, shrink 50", "shrinkprio 2, shrink 100"));

				frame.pack();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
