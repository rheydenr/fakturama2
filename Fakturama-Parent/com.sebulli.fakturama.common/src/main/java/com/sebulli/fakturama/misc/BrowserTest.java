package com.sebulli.fakturama.misc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is intended for checking the functionality of the browser widget. To check, follow
 * these steps:
 * <ol>
 * 	<li>check the version of <tt>libwebkit-1.0-2</tt> (should be &gt= 1.2.0)
 *  <li>install the package if it's not installed<tt></tt>
 *  <li>check if <tt>/usr/lib</tt> and <tt>/usr/lib/jni</tt> are in <tt>java.library.path</tt>
 *  <li>if SWT 3.6 is used then check if the webkit jni wrapper is installed
 *  (e.g. <tt>libswt-webkit-gtk-3.6-jni</tt> and <tt>org.eclipse.swt.browser.UseWebKitGTK</tt> system option set to <code>true</code>)
 *  </ol>
 *  
 * @author Michael St&ouml;ger
 *
 */
public class BrowserTest {

	public static void main(String[] args) {

		System.out.println("java.library.path="
				+ System.getProperty("java.library.path"));
		System.out.println("org.eclipse.swt.browser.UseWebKitGTK="
				+ System.getProperty("org.eclipse.swt.browser.UseWebKitGTK"));

		Display display = new Display();
		Shell shell = new Shell(display);

		try {
			Browser browser = new Browser(shell, SWT.NONE);
			browser.setSize(shell.getSize());
			browser.setUrl("google.com");
		} catch (SWTError e) {
			e.printStackTrace();
		}

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();

	}
}
