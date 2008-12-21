package net.miginfocom.examples;

import net.miginfocom.swt.MigLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Mikael Grev, MiG InfoCom AB
 *         Date: Apr 12, 2008
 *         Time: 8:19:01 AM
 */
public class SwtTest
{
	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setMaximized(true);

		MigLayout migLayout = new MigLayout("fill", "[left]");
		shell.setLayout(migLayout);

		List list = new List(shell, SWT.V_SCROLL);
		list.setLayoutData("grow");

		for (int i = 0; i < 128; i++) {
			list.add("Item " + i);
		}
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
