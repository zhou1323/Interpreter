

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.io.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame{
    private static int untitled = 0;
	//菜单栏
	private JMenuBar menuBar;
	//--------文件菜单-------
	private JMenu fileMenu;
	/*******文件菜单项*******/
	//新建文件,打开文件，保存文件，关闭文件，退出程序
	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem saveItem;
	private JMenuItem saveAllItem;
	private JMenuItem closeItem;
	private JMenuItem closeAllItem;
	private JMenuItem exitItem;
	//----------编辑菜单-------
	private JMenu editMenu;
	/*******编辑菜单项*******/
	//回撤，回进，复制，剪切，粘贴，删除，查找，替换
	private JMenuItem undoItem;
	private JMenuItem redoItem;
	private JMenuItem copyItem;
	private JMenuItem cutItem;
	private JMenuItem pasteItem;
	private JMenuItem deleteItem;
	private JMenuItem findItem;
	private JMenuItem replaceItem;
	//-----------运行菜单--------
	private JMenu runMenu;
	/*******运行菜单项*******/
	//词法分析，语法分析，运行
	private JMenuItem lexerItem;
	private JMenuItem parserItem;
	private JMenuItem runItem;
	//---------设置菜单-------
	private JMenu settingMenu;
	/*******设置菜单项*******/
	//字体
	private JMenuItem fontItem;
	//--------帮助菜单-----------
	private JMenu helpMenu;
	/*******帮助菜单项*******/
	//关于，作者
	private JMenuItem aboutItem;
	private JMenuItem authorItem;
	/*****文本编辑区********/
	//撤销重做管理器
	private final UndoManager undoManager = new UndoManager();
	private UndoableEditListener editListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			if(e.getEdit().isSignificant())
			undoManager.addEdit(e.getEdit());
		}
	};
	//查找时高亮显示器
	Highlighter highlighter;
	private int lineNum=30;
	public static Font font = new Font("微软雅黑 Light", Font.PLAIN, 15);
	private JTabbedPane editTabbedPane;
	private List<EditScrollPane> editPaneList = new ArrayList<>();
	//编辑区的右键菜单
	private JPopupMenu textPaneMenu;
	private JMenuItem textCopyItem;
	private JMenuItem textCutItem;
	private JMenuItem textPasteItem;
	private JMenuItem textDeleteItem;

	/******语法词法分析区**********/
	private JTabbedPane analysisPane;
	private JScrollPane lexerPane;
	private JScrollPane parserPane;

	/*****控制台与错误列表区*******/
	public JTabbedPane consolePane;
	private JScrollPane scrollConPane;
	private JScrollPane scrollErrorPane;
	private String userInput;
	public static JTextPane tabConsolePane;
	public static JTextPane tabErrorPane;

	//打开文件窗口
	private FileDialog openDialog;
	private FileDialog saveDiolog;

	//编辑区标签
	JPanel pnlTab;
	JLabel lblTitle;
	JLabel btnClose;
	//语法词法分析器
	Lexer lexer;
	List<Token> tokens;
	Parser parser;
	Sematics sematics;
	/**
	 * 构造函数
	 * @param title
	 */
	public MainFrame(String title){
		super();
		setTitle(title);
		setVisible(true);
		setResizable(false);
		setSize(900,650);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width-900)/2,(screenSize.height-600)/2);
		setDefaultCloseOperation(super.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		init();
	}

	/**
	 * 初始化窗体
	 */
	private void init(){
		//初始化菜单栏
		menuBar = new JMenuBar();
		menuBar.setSize(getWidth(),21);
		setJMenuBar(menuBar);
		fileMenu = new JMenu("文件");
		editMenu = new JMenu("编辑");
		runMenu = new JMenu("运行");
		settingMenu = new JMenu("设置");
		helpMenu = new JMenu("帮助");
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(runMenu);
		menuBar.add(settingMenu);
		menuBar.add(helpMenu);
		//初始化文件菜单项
		newItem = new JMenuItem("新建");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
		openItem = new JMenuItem("打开");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		saveItem = new JMenuItem("保存");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveAllItem = new JMenuItem("保存所有");
		saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.ALT_MASK));
		closeItem = new JMenuItem("关闭");
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.CTRL_MASK));
		closeAllItem = new JMenuItem("关闭所有");
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.ALT_MASK));
		exitItem = new JMenuItem("退出");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(saveAllItem);
		fileMenu.add(closeItem);
		fileMenu.add(closeAllItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		//初始化编辑菜单
		undoItem = new JMenuItem("回撤");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK));
		redoItem = new JMenuItem("回进");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
		copyItem = new JMenuItem("复制");
		copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
		cutItem = new JMenuItem("剪切");
		cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK));
		pasteItem = new JMenuItem("粘贴");
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK));
		deleteItem = new JMenuItem("删除");
		deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		findItem = new JMenuItem("查找");
		findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,ActionEvent.CTRL_MASK));
		replaceItem = new JMenuItem("替换");
		replaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.addSeparator();
		editMenu.add(copyItem);
		editMenu.add(cutItem);
		editMenu.add(pasteItem);
		editMenu.add(deleteItem);
		editMenu.addSeparator();
		editMenu.add(findItem);
		editMenu.add(replaceItem);
		//初始化运行菜单
		lexerItem = new JMenuItem("词法分析");
		lexerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		parserItem = new JMenuItem("语法分析");
		parserItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
		runItem = new JMenuItem("运行");
		runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		runMenu.add(lexerItem);
		runMenu.add(parserItem);
		runMenu.addSeparator();
		runMenu.add(runItem);
		//初始化设置菜单
		fontItem = new JMenuItem("字体");
		fontItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK));
		settingMenu.add(fontItem);
		//初始化帮助菜单
		aboutItem = new JMenuItem("关于");
		authorItem = new JMenuItem("作者");
		helpMenu.add(aboutItem);
		helpMenu.add(authorItem);

		//初始化编辑面板
		editTabbedPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		editTabbedPane.setBounds(0,0,690,400);
		if(editPaneList.size()==0){
			newFile(null);
		}
		//编辑区右键菜单
        textPaneMenu = new JPopupMenu();
        textCopyItem = new JMenuItem("复制");
        textCutItem = new JMenuItem("剪切");
        textPasteItem = new JMenuItem("粘贴");
        textDeleteItem = new JMenuItem("删除");
        textPaneMenu.add(textCopyItem);
        textPaneMenu.add(textCutItem);
        textPaneMenu.add(textPasteItem);
        textPaneMenu.add(textDeleteItem);
		getContentPane().add(editTabbedPane);


		//初始化分析面板
		analysisPane = new JTabbedPane(JTabbedPane.TOP);
		analysisPane.setBounds(690, 0, 210, 600);
		getContentPane().add(analysisPane);
		lexerPane = new JScrollPane();
		analysisPane.addTab("词法分析", null, lexerPane, null);

		parserPane = new JScrollPane();
		analysisPane.addTab("语法分析", null, parserPane, null);

		//初始化控制台面板
		consolePane = new JTabbedPane(JTabbedPane.TOP);
		consolePane.setBounds(0,400,690,200);
		getContentPane().add(consolePane);
		tabConsolePane = new JTextPane();
		tabConsolePane.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					try {
						String consoleText =tabConsolePane.getText();
						String[] ss = consoleText.split("\r\n");
						
						userInput=ss[ss.length-1];
						sematics.setInput(userInput);
					}
					catch (Exception exception) {
						
						exception.printStackTrace();
					}
				}
			}
		});
		scrollConPane = new JScrollPane(tabConsolePane);
		consolePane.addTab("控制台",null,scrollConPane,null);
		tabErrorPane = new JTextPane();
		scrollErrorPane = new JScrollPane(tabErrorPane);
		consolePane.addTab("错误列表",null,scrollErrorPane,null);
		//打开文件，保存对话窗
        openDialog = new FileDialog(this,"打开文件",FileDialog.LOAD);
		saveDiolog = new FileDialog(this,"保存文件",FileDialog.SAVE);
		setAction();

	}

	/**
	 * 为各个控件设置动作监听
	 */
	private void setAction(){
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    newFile(null);
			}
		});
		openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
		saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(editTabbedPane.getTabCount()>0) {
					int index = editTabbedPane.getSelectedIndex();
					save(index);
				}
            }
        });
		saveAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i <= editTabbedPane.getTabCount()-1;i++){
					save(i);
				}

			}
		});
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(editTabbedPane.getTabCount()>0) {
					int index = editTabbedPane.getSelectedIndex();
					close(index);
				}
			}
		});
		closeAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < editTabbedPane.getTabCount(); i++){
					close(i);
				}
			}
		});
		lexerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					lex();
				}catch (IOException ie){

				}
			}
			
		});
		parserItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					System.out.print("parsing");
					parse();

				}catch (ParserException pe){

				}catch (IOException ie){

				}
			}
		});
		runItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					syntaxRun();
				}catch (IOException io){

				}catch (ParserException pe){

				}catch (Exception ie){

				}


			}
		});
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
//				System.out.print(1);
				setFont();
			}
		});
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(getContentPane(),"此产品是一个简单的CMM语言解释器" +
						"，\n可以对一段CMM程序进行简单的词法语法分析，\n并将分析结果在视图中显示，\n" +
						"程序运行结果会在控制台中显示，\n还能进行打印一些基本的报错信息","关于",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		authorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(getContentPane(),"制作人：（无排名先后）\n" +
						"周慕哲，2015302580231\n" +
						"胡苏扬，2015302580206\n" +
						"蔡中超，2015302580220","作者",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		});
		redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				redo();
			}
		});
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		});
		pasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		findItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				find();
			}
		});
		replaceItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String replaceWord = find();
				replace(replaceWord);
			}
		});
		textCopyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		textCutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		});
		textPasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		textDeleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});

	}

    /**
     * 新建文件
     */
	private void newFile(String filename){
	    untitled ++;
	    if(filename==null){
	    	filename = "untitled_"+untitled+".cmm";
		}
	    EditScrollPane editorPane = new EditScrollPane();
	    editorPane.setIschange(true);
	    editorPane.setFilename(filename);
	    JScrollPane untitledPane = editorPane.getEditPane();
	    JTextPane textPane =editorPane.getTextPane();
	    //初始化
	    editorPane.initialize(lineNum);
		textPane.setFont(font);
		textPane.getDocument().addDocumentListener(new SyntaxHighlighter(textPane));
		textPane.getDocument().addUndoableEditListener(editListener);
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){
                    textPaneMenu.show(e.getComponent(),e.getX(),e.getY());
                }
            }
        });
	    editPaneList.add(editorPane);
		editTabbedPane.addTab(filename,null,untitledPane,null);
//	    System.out.println(editTabbedPane.getTabCount());
		int index = editTabbedPane.indexOfComponent(untitledPane);
		editTabbedPane.setSelectedIndex(index);
		//当文本区域内容改变时，设置变化
		textPane.getDocument().addDocumentListener(new DocumentListener() {
			int index = editTabbedPane.getSelectedIndex();
			String filename = editPaneList.get(index).getFilename();
			@Override
			public void insertUpdate(DocumentEvent e) {
				textPane.getHighlighter().removeAllHighlights();
				lblTitle.setText("*"+filename);
				editPaneList.get(index).setIschange(true);

				DefaultListModel dlm=editorPane.getDlm();
				JList list=editorPane.getList();
				int lastLine=(int)dlm.get(dlm.size()-1);
				int nowLine=textPane.getDocument().getDefaultRootElement().getElementCount();
				int diff=nowLine-lastLine;
				for(int i=0;i<diff;i++) {
					if(!dlm.contains(lastLine+i+1)) {
						dlm.addElement(lastLine+i+1);
						list.setModel(dlm);
					}
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				textPane.getHighlighter().removeAllHighlights();
				lblTitle.setText("*"+ filename);
				editPaneList.get(index).setIschange(true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}
		});
		pnlTab = new JPanel();
		pnlTab.setOpaque(false);
		lblTitle = new JLabel("*"+filename);
		lblTitle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int i = editTabbedPane.indexOfTabComponent(e.getComponent().getParent());
				if(SwingUtilities.isLeftMouseButton(e)){
					editTabbedPane.setSelectedIndex(i);
				}
			}
		});
		btnClose = new JLabel("×");
		btnClose.setFont(new Font("宋体",Font.BOLD,12));
		btnClose.setBorder(new BorderUIResource.EmptyBorderUIResource(0,4,0,0));
		btnClose.setToolTipText("关闭");
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)){
					int index = editTabbedPane.indexOfTabComponent(e.getComponent().getParent());
					close(index);
				}
			}
		});
		pnlTab.add(lblTitle);
		pnlTab.add(btnClose);
		editTabbedPane.setTabComponentAt(index, pnlTab);

    }

    /**
     * 打开文件
     */
    private void open(){
    	boolean isopened = false;
        openDialog.setVisible(true);
        StringBuilder sb = new StringBuilder();
        String path = openDialog.getDirectory();
        String filename = openDialog.getFile();
        if(path == null||filename == null){
            JOptionPane.showMessageDialog(this,"错误的文件路径","警告",JOptionPane.WARNING_MESSAGE);
        }else{
            if(!filename.matches("[\\S]*.cmm")){
                JOptionPane.showMessageDialog(this,"错误的文件格式，请选择.cmm格式的文件","警告",JOptionPane.ERROR_MESSAGE);
            }
            File file = new File(path,filename);
            try{
                BufferedReader bufr = new BufferedReader(new FileReader(file));
                String line  = null;
                while((line=bufr.readLine()) != null){
                    sb.append(line + "\r\n");
                }
                bufr.close();
//				CodeStyle codeStyle = new CodeStyle(textPane, CodeStyle.ColorType.WHITE);
//                editScrollPane.setCodeStyle(codeStyle);
            }catch (IOException e){
                e.getStackTrace();
            }
            for(int i = 0; i < editTabbedPane.getTabCount();i++){
				if(editPaneList.get(i).getFilename().equals(filename)&&editPaneList.get(i).getPath().equals(path)){
					isopened = true;
					editTabbedPane.setSelectedIndex(i);
				}
			}
			if(!isopened){
				newFile(filename);
				EditScrollPane editScrollPane = editPaneList.get(editPaneList.size()-1);
				editScrollPane.setFilename(filename);
				editScrollPane.setPath(path);
//            System.out.print(editTabbedPane.getComponentAt(editTabbedPane.getTabCount()-1));
				editTabbedPane.setSelectedIndex(editTabbedPane.getTabCount()-1);
				JTextPane textPane = editScrollPane.getTextPane();
				textPane.setText(sb.toString());
			}
        }
    }
    //保存当前编辑面板的内容
    private void save(int index){
    	String path;
    	String filename;
    	File file = null;
    	if(editPaneList.get(index).getPath()!=null){
    		path = editPaneList.get(index).getPath();
    		filename = editPaneList.get(index).getFilename();
    		file = new File(path,filename);
		}else{
			saveDiolog.setVisible(true);
			filename = saveDiolog.getFile();
			path = saveDiolog.getDirectory();
			if(filename==null){
				JOptionPane.showMessageDialog(this,"请填写文件名！","警告",JOptionPane.WARNING_MESSAGE);
			}else if(!filename.matches("[\\S]*.cmm")){
				JOptionPane.showMessageDialog(this,"请保存为CMM文件格式！","警告",JOptionPane.WARNING_MESSAGE);
			}else{
				file = new File(path,filename);
			}
		}
		if(file != null){
			try{
				BufferedWriter bfw = new BufferedWriter(new FileWriter(file));
				String text = editPaneList.get(index).getTextPane().getText();
				bfw.write(text);
				bfw.close();
				JPanel panel = (JPanel)editTabbedPane.getTabComponentAt(index);
				JLabel label = (JLabel)panel.getComponent(0);
				label.setText(filename);
				EditScrollPane savepane = editPaneList.get(index);
				savepane.setFilename(filename);
				savepane.setPath(path);
				savepane.setIschange(false);
			}catch (IOException e){
				e.getStackTrace();
			}
		}
    }
    //关闭文件
    private void close(int index){
		if(editPaneList.get(index).isIschange()){
			int option = JOptionPane.showInternalConfirmDialog(editTabbedPane.getParent(),
					"当前文本未保存，是否保存后再关闭？",
					"警告",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
			switch (option){
				case JOptionPane.YES_OPTION:
					save(index);
					editTabbedPane.removeTabAt(index);
					editPaneList.remove(index);
					break;
				case JOptionPane.NO_OPTION:
					editTabbedPane.removeTabAt(index);
					editPaneList.remove(index);
					break;
				case JOptionPane.CANCEL_OPTION:
					break;

			}
		}else{
			editTabbedPane.removeTabAt(index);
			editPaneList.remove(index);
		}
	}
	private void undo() {
		if (undoManager.canUndo()) {
			try {
				undoManager.undo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void redo(){
    	if(undoManager.canRedo()){
    		try{
    			undoManager.redo();
			}catch (Exception e){
    			e.printStackTrace();
			}
		}
	}
	private void copy(){
		editPaneList.get(editTabbedPane.getSelectedIndex()).getTextPane().copy();
	}
	private void cut(){
		editPaneList.get(editTabbedPane.getSelectedIndex()).getTextPane().cut();
	}
	private void paste(){
		editPaneList.get(editTabbedPane.getSelectedIndex()).getTextPane().paste();
	}
	private void delete(){
		editPaneList.get(editTabbedPane.getSelectedIndex()).getTextPane().replaceSelection("");
	}
	private String find(){
		int index = editTabbedPane.getSelectedIndex();
		JTextPane textPane = editPaneList.get(index).getTextPane();
		highlighter = textPane.getHighlighter();
		String text = textPane.getText();
		System.out.println(text);
		text = text.replace("\n","");
		DefaultHighlighter.DefaultHighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
		String findWord = JOptionPane.showInputDialog(getContentPane(),"请输入要查找的字符串：");
		if(!findWord.equals("")){
			System.out.println(findWord);
			int pos = 0;
			while((pos=text.indexOf(findWord,pos))>=0)
			{
				System.out.println(pos);
				try
				{
					highlighter.addHighlight(pos,pos+findWord.length(),p);
					pos += findWord.length();
				}
			catch(BadLocationException e)
				{
					e.printStackTrace();
				}
			}
			return findWord;

		}
		return  null;
	}
	private void replace(String findWord){
		String replaceWord = JOptionPane.showInputDialog(getContentPane(),"输入替换的字符串");
		JTextPane textPane = editPaneList.get(editTabbedPane.getSelectedIndex()).getTextPane();
		String text = textPane.getText();
		if(findWord!=null){
			textPane.setText(text.replace(findWord,replaceWord));
		}
	}

	/**
	 * 词法分析
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void lex()throws FileNotFoundException,IOException{
		int index = editTabbedPane.getSelectedIndex();
		String path;
		if(editPaneList.get(index).isIschange()) {
			int select = JOptionPane.showConfirmDialog(getContentPane(), "当前文档已被修改，是否保存后再运行？", "警告", JOptionPane.OK_CANCEL_OPTION);
			switch (select) {
				case JOptionPane.OK_OPTION:
					save(index);
					path = editPaneList.get(index).getPath() + editPaneList.get(index).getFilename();
					lexRun(path);
					break;
				case JOptionPane.CANCEL_OPTION:
					break;
			}
		}else{
			path = editPaneList.get(index).getPath() + editPaneList.get(index).getFilename();
			lexRun(path);
		}
	}
	private void lexRun(String path)throws IOException{
		lexer = new Lexer(path);
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while (token.getTokenType() != TokenType.END_OF_DOC) {
			tokens.add(token);
			token = lexer.nextToken();
		}
		DefaultMutableTreeNode lexNode = new DefaultMutableTreeNode("词法分析结果");
		for (Token t : tokens) {
			DefaultMutableTreeNode tokenNode = new DefaultMutableTreeNode(t.getTokenType());
			DefaultMutableTreeNode lineNode = new DefaultMutableTreeNode("行号: " + t.getLineNum());
			DefaultMutableTreeNode positionNode = new DefaultMutableTreeNode("位置: " + t.getPosition());
			DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode("值: " + t.getValue());
			tokenNode.add(lineNode);
			tokenNode.add(positionNode);
			tokenNode.add(valueNode);
			lexNode.add(tokenNode);
		}
		JTree lexerTree = new JTree(lexNode);
		analysisPane.setComponentAt(0, new JScrollPane(lexerTree));
		analysisPane.setSelectedIndex(0);
	}

	/**
	 * 语法分析
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parse()throws IOException,ParserException{
		int index = editTabbedPane.getSelectedIndex();
		String path;
		if(editPaneList.get(index).isIschange()){
			int select = JOptionPane.showConfirmDialog(getContentPane(),"当前文档已被修改，是否保存后再运行？","警告",JOptionPane.OK_CANCEL_OPTION);
			switch (select){
				case JOptionPane.OK_OPTION:
					save(index);
					path = editPaneList.get(index).getPath()+ editPaneList.get(index).getFilename();
					parseRun(path);
					break;
				case JOptionPane.CANCEL_OPTION:
					break;
			}
		}else{
			path = editPaneList.get(index).getPath()+editPaneList.get(index).getFilename();
			parseRun(path);
		}
	}
	private void parseRun(String path)throws ParserException,IOException{
		parser = new Parser(path);
		TreeNode root = parser.parseProgram();
		DefaultMutableTreeNode parseNode = parseChild(root);
		JTree parseTree = new JTree(parseNode);
		JScrollPane parPane = new JScrollPane(parseTree);
		parPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		parPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		analysisPane.setComponentAt(1, parPane);
		analysisPane.setSelectedIndex(1);
	}
	private DefaultMutableTreeNode parseChild(TreeNode root){
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root.getValue());
		for(TreeNode t:root.getChildren()){
			rootNode.add(parseChild(t));
		}
		return rootNode;
	}

	/**
	 *	运行
	 * @throws Exception
	 */
	private void syntaxRun()throws Exception{
		int index = editTabbedPane.getSelectedIndex();
		String path;
		if(editPaneList.get(index).isIschange()){
			int select = JOptionPane.showConfirmDialog(getContentPane(),"当前文档已被修改，是否保存后再运行？","警告",JOptionPane.OK_CANCEL_OPTION);
			switch (select){
				case JOptionPane.OK_OPTION:
					save(index);
					path = editPaneList.get(index).getPath()+editPaneList.get(index).getFilename();
					tabConsolePane.setText(null);
					tabErrorPane.setText(null);
					lexRun(path);
					parseRun(path);
					sematics = new Sematics(path);
					sematics.start();
					break;
				case JOptionPane.CANCEL_OPTION:
					break;
			}
		}else{
			path = editPaneList.get(index).getPath()+editPaneList.get(index).getFilename();
			tabConsolePane.setText(null);
			sematics = new Sematics(path);
			sematics.start();
		}
	}

	/**
	 * 字体设置
	 */
	private void setFont(){
		font = JFontDialog
				.showDialog(getContentPane(), "字体设置", true, getFont());
		for (int i = 0; i < editTabbedPane.getTabCount(); i++) {
			editPaneList.get(i).getTextPane().setFont(font);
			FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
			editPaneList.get(i).getList().setFixedCellHeight(fm.getHeight());
			editPaneList.get(i).getList().setFont(new Font("微软雅黑 Light",Font.PLAIN,font.getSize()));
		}

	}

	/**
	 * 主函数
	 * @param args
	 */
	public static void main(String[] args) {
		MainFrame frame = new MainFrame("CMM解释器");
		frame.setFont(new Font("微软雅黑", Font.PLAIN, 15));

	}
}
