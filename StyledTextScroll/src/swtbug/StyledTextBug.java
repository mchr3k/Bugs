package swtbug;

import java.util.Random;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class StyledTextBug
{
  private final Shell window;
  private final StyledText text2;
  private final StyledText text1;
  private final Button start;

  public StyledTextBug(Shell xiWindow)
  {
    window = xiWindow;
    MigLayout mainLayout = new MigLayout("fill", "[]", "[][grow]");
    window.setLayout(mainLayout);

    Composite topBar = new Composite(window, SWT.NONE);
    topBar.setLayoutData("grow,wrap");
    MigLayout topLayout = new MigLayout("fill", "[][100,center][]");
    topBar.setLayout(topLayout);

    start = new Button(topBar, SWT.PUSH);
    start.setText("Start");
    start.setLayoutData("skip,grow");

    start.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent arg0)
      {
        runTest();
      }
    });

    Composite bottomBar = new Composite(window, SWT.NONE);
    MigLayout bottomLayout = new MigLayout("fill", "[grow][grow]", "[grow]");
    bottomBar.setLayout(bottomLayout);
    bottomBar.setLayoutData("grow,wmin 0,hmin 0");

    text1 = new StyledText(bottomBar, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
        | SWT.BORDER);
    text1.setLayoutData("grow,wmin 0,hmin 0");
    text2 = new StyledText(bottomBar, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
        | SWT.BORDER);
    text2.setLayoutData("grow,wmin 0,hmin 0");

    Random r = new Random();
    StringBuilder str1 = new StringBuilder();
    StringBuilder str2 = new StringBuilder();

    for (int ii = 0; ii < 1000; ii++)
    {
      int max = r.nextInt(2000);
      for (int jj = 0; jj < max; jj++)
      {
        str1.append("1 2 3 4 5 ");
        str2.append("1,2,3,4,5,");
      }
      str1.append("\n");
      str2.append("\n");
    }
    text1.setText(str1.toString());
    text2.setText(str2.toString());
  }

  private void runTest()
  {
    start.setEnabled(false);
    Runnable r = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          // Sleep for a second to let the UI start up
          Thread.sleep(1000);

          long spaceTime = 0;
          {
            long startTime = System.currentTimeMillis();
            for (int ii = 0; ii < 500; ii++)
            {
              final int index = ii;
              window.getDisplay().syncExec(new Runnable()
              {
                @Override
                public void run()
                {
                  text1.setTopIndex(index);
                }
              });
            }
            long endTime = System.currentTimeMillis();
            spaceTime = (endTime - startTime);
          }

          long commaTime = 0;
          {
            long startTime = System.currentTimeMillis();
            for (int ii = 0; ii < 500; ii++)
            {
              final int index = ii;
              window.getDisplay().syncExec(new Runnable()
              {
                @Override
                public void run()
                {
                  text2.setTopIndex(index);
                }
              });
            }
            long endTime = System.currentTimeMillis();
            commaTime = (endTime - startTime);                                   
            
            final String results = "Time to scroll space separated text: " + spaceTime + "\n" + 
                                   "Time to scroll comma separated text: " + commaTime;
            
            window.getDisplay().syncExec(new Runnable()
            {
              @Override
              public void run()
              {
                MessageBox messageBox = new MessageBox(window, SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setMessage(results);
                messageBox.setText("Results");
                messageBox.open();
                
                start.setEnabled(true);
              }
            });
          }
        }
        catch (InterruptedException e)
        {
          // Ignore
        }
      }
    };

    Thread t = new Thread(r);
    t.start();
  }

  public static void main(String[] args)
  {
    final Shell window = new Shell();
    window.setSize(new Point(800, 400));

    // Fill in UI
    StyledTextBug ui = new StyledTextBug(window);

    // Open UI
    ui.open();
  }

  public void open()
  {
    window.open();
    Display display = Display.getDefault();
    while (!window.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}
