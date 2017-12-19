package GUI;


import javax.swing.*;

import com.sun.org.apache.xml.internal.resolver.helpers.Debug;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by superman on 2017/12/5.
 */
public class EditScrollPane extends Component {
    private JScrollPane editPane;
    private JTextPane textPane;
    private DefaultListModel dlm;
    private JList list;
    private ArrayList<Object> marks=new ArrayList<Object>();
    private String filename;
    private String path;
    private boolean ischange;
    
    public EditScrollPane() {
    	
       this.textPane = new JTextPane();
       this.editPane = new JScrollPane(textPane);
       this.dlm = new DefaultListModel();
       this.list=new JList();
       filename = null;
       path = null;
       ischange = false;
    }

    public void initialize(int j) {
    	for(int i=1;i<j;i++) {
    		this.dlm.addElement(i);
    	}
    	list.setModel(dlm);
    	list.setFixedCellHeight(22);
    	list.setBackground(new Color(228, 228, 228));
    	list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    	list.setPreferredSize(new Dimension(50, 9999));
    	this.editPane.setRowHeaderView(list);
    }
    
    public DefaultListModel getDlm() {
		return dlm;
	}

	public void setDlm(DefaultListModel dlm) {
		this.dlm = dlm;
	}

	public JList getList() {
		return list;
	}

	public void setList(JList list) {
		this.list = list;
	}

	public JScrollPane getEditPane() {
        return editPane;
    }

    public void setEditPane(JScrollPane editPane) {
        this.editPane = editPane;
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public void setTextPane(JTextPane textArea) {
        this.textPane = textArea;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIschange() {
        return ischange;
    }

    public void setIschange(boolean ischange) {
        this.ischange = ischange;
    }
}
