package com.sebulli.fakturama.i18n;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StackOverflow
{
    public static void main(String[] args)
    {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);

        /* GridLayout for the Shell to make things easier */
        shell.setLayout(new GridLayout(1, false));

        for(int i = 0; i < 5; i++)
        {
            createRow(shell, i);
        }

        shell.pack();
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void createRow(Shell shell, int i)
    {
        /* GridLayout for the rows, two columns, equal column width */
        Composite row = new Composite(shell, SWT.NONE);
        row.setLayout(new GridLayout(2, true));

        /* Make each row expand horizontally but not vertically */
        row.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        /* Create the content of the row, expand horizontally as well */
        Button first = new Button(row, SWT.PUSH);
        first.setText("FIRST " + i);
        first.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        Button second = new Button(row, SWT.PUSH);
        second.setText("SECOND " + i);
        second.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }
}