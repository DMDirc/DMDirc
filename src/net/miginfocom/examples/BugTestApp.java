package net.miginfocom.examples;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * @author Mikael Grev, MiG InfoCom AB
 *         Date: Dec 15, 2008
 *         Time: 7:04:50 PM
 */
public class BugTestApp
{
	private static JPanel createPanel()
	{
		JPanel panel = new JPanel(new MigLayout("debug, wrap 2", "", "[]10[]20[]30[]"));

		panel.add(new JButton("1"));
		panel.add(new JButton("2"), "skip 3");
		panel.add(new JButton("3"));
		panel.add(new JButton("4"), "skip 0");
//		panel.add("newline, span", new JButton());

//		panel.add("newline 100", new JButton("Should have 40 pixels before"));
//
//		panel.add("newline", new JButton());
//		panel.add("newline", new JButton());
//		panel.add("newline", new JButton());

		return panel;
	}

	private static JPanel createPanel2()
	{
		JPanel panel = new JPanel(new MigLayout("wrap 2"));

		panel.add(new JTable(), "spany 2, w 100, h 100");
		panel.add(new JButton("Button 1"));
		panel.add(new JButton("Button 2"), "skip, span");

		return panel;
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

				JFrame frame = new JFrame("Bug Test App");
				frame.getContentPane().add(createPanel2());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
