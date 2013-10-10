package com.sebulli.fakturama.i18n;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeShellExample2 {
  Display d;

  Shell s;

  TreeShellExample2() {
    d = new Display();
    s = new Shell(d);

    s.setSize(250, 200);
    
    s.setText("A Tree Shell Example");
    s.setLayout(new FillLayout(SWT.HORIZONTAL));
    final Tree t = new Tree(s, SWT.SINGLE | SWT.BORDER);
    final TreeItem child1 = new TreeItem(t, SWT.NONE, 0);
    child1.setText("1");
    child1.setImage(new Image(d, "c:\\install\\famfamfam\\icons\\folder.png"));
    final TreeItem child2 = new TreeItem(t, SWT.NONE, 1);
    child2.setText("2");
    child2.setImage(new Image(d, "c:\\install\\famfamfam\\icons\\folder.png"));
    final TreeItem child2a = new TreeItem(child2, SWT.NONE, 0);
    child2a.setText("2A");
    final TreeItem child2b = new TreeItem(child2, SWT.NONE, 1);
    child2b.setText("2B");
    final TreeItem child3 = new TreeItem(t, SWT.NONE, 2);
    child3.setText("3");
    child3.setImage(new Image(d, "c:\\install\\famfamfam\\icons\\folder.png"));

    final List l = new List(s, SWT.BORDER | SWT.SINGLE);

    t.addTreeListener(new TreeListener() {
      public void treeExpanded(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(new Image(d, "c:\\install\\famfamfam\\icons\\folder_explore.png"));
      }

      public void treeCollapsed(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(new Image(d, "c:\\install\\famfamfam\\icons\\folder.png"));
      }
    });

    t.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        TreeItem ti = (TreeItem) e.item;
        populateList(ti.getText());
      }

      private void populateList(String type) {
        if (type.equals("1")) {
          l.removeAll();
          l.add("File 1");
          l.add("File 2");
        }
        if (type.equals("2")) {
          l.removeAll();
        }
        if (type.equals("2A")) {
          l.removeAll();
          l.add("File 3");
          l.add("File 4");
        }
        if (type.equals("2B")) {
          l.removeAll();
          l.add("File 5");
          l.add("File 6");
        }
        if (type.equals("3")) {
          l.removeAll();
          l.add("File 7");
          l.add("File 8");
        }

      }
    });
    s.open();
    while (!s.isDisposed()) {
      if (!d.readAndDispatch())
        d.sleep();
    }
    d.dispose();
  }

  public static void main(String[] argv) {
    new TreeShellExample2();
  }
}
