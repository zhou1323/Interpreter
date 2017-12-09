import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.lang.model.util.SimpleElementVisitor6;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;
import com.sun.org.apache.xpath.internal.axes.WalkingIterator;

import jdk.internal.dynalink.beans.StaticClass;

public class Redirector{

	static JTextPane textPane = MainFrame.tabConsolePane;
	public static boolean hasInput=false;
	public static void updateTextPane(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Document doc = textPane.getDocument();
				try {
					doc.insertString(doc.getLength(), text + "\r\n", null);
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
				textPane.setCaretPosition(doc.getLength());
			}
		});
	}
	
	public static void input() throws Exception {
		System.out.println("In input");
		textPane.addKeyListener(new KeyAdapter() {
			
			public void keyPressed(KeyEvent event) {
				if(event.getKeyCode()==event.VK_ENTER) {
					hasInput=true;
				}
			}
			
			
		});
	}

	
	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				updateTextPane(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextPane(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
}
