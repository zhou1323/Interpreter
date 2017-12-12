package Utils;
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

import GUI.MainFrame;
import jdk.internal.dynalink.beans.StaticClass;
import sun.applet.Main;

public class Redirector{

	static JTextPane consolePane = MainFrame.tabConsolePane;
	static JTextPane errorPane=MainFrame.tabErrorPane;
	public static boolean hasInput=false;
	
	public static void updateConsolePane(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Document doc = consolePane.getDocument();
				try {
					doc.insertString(doc.getLength(), text + "\r\n", null);
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
				consolePane.setCaretPosition(doc.getLength());
			}
		});
	}
	
	public static void updateErrorPane(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Document doc = errorPane.getDocument();
				try {
					doc.insertString(doc.getLength(), text + "\r\n", null);
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
				errorPane.setCaretPosition(doc.getLength());
			}
		});
	}
}
