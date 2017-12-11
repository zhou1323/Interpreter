

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.UndoManager;

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
	//字体，主题（黑白）
	private JMenuItem fontItem;
	private JMenu themeItem;
	private JMenuItem blackItem;
	private JMenuItem whiteItem;
	//--------帮助菜单-----------
	private JMenu helpMenu;
	/*******帮助菜单项*******/
	//关于，作者
	private JMenuItem aboutItem;
	private JMenuItem authorItem;
	/*****文本编辑区********/
	private int lineNum=30;
	public static Font font = new Font("微软雅黑 Light", Font.PLAIN, 15);
	private JTabbedPane editTabbedPane;
	private List<EditScrollPane> editPaneList = new ArrayList<>();
	//编辑区的右键菜单
	private JPopupMenu popupMenu;
	private JMenuItem closeSelfItem;
	private JMenuItem closeOthersItem;
	private JMenuItem closeALLItem;
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
	
	//撤销功能
	private UndoManager um;
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
		themeItem = new JMenu("主题");
		blackItem = new JMenuItem("黑");
		whiteItem = new JMenuItem("白");
		themeItem.add(blackItem);
		themeItem.add(whiteItem);
		settingMenu.add(fontItem);
		settingMenu.add(themeItem);
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
		popupMenu = new JPopupMenu();
		closeSelfItem = new JMenuItem("关闭当前页面");
		closeOthersItem = new JMenuItem("关闭其他页面");
		closeALLItem = new JMenuItem("关闭所有页面");
		popupMenu.add(closeSelfItem);
		popupMenu.add(closeOthersItem);
		popupMenu.add(closeALLItem);
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
               int index = editTabbedPane.getSelectedIndex();
               save(index);
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
		lexerItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = editTabbedPane.getSelectedIndex();
				String strpath = editPaneList.get(index).getPath();
				String strfilename = editPaneList.get(index).getFilename();
				String path = strpath+strfilename;
				try{
					lex(path);
				}catch (IOException ie){

				}
			}
			
		});
		parserItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = editTabbedPane.getSelectedIndex();
				String strpath = editPaneList.get(index).getPath();
				String strfilename = editPaneList.get(index).getFilename();
				String path = strpath+strfilename;
				try{
					parse(path);
				}catch (ParserException pe){

				}catch (IOException ie){

				}
			}
		});
		runItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				int index = editTabbedPane.getSelectedIndex();
				String strpath = editPaneList.get(index).getPath();
				String strfilename = editPaneList.get(index).getFilename();
				String path = strpath+strfilename;
				try{
					lex(path);
					parse(path);
					syntaxRun(path);
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
                if (um.canUndo()) {
                    um.undo();
                    um.undo();
                }
            }   
        });

		redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (um.canRedo()) {
                    um.redo();
                    um.redo();
                }
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
	    editorPane.setFilename(filename);
	    JScrollPane untitledPane = editorPane.getEditPane();
	    JTextPane textPane =editorPane.getTextPane();
	    
	    //初始化
	    um = new UndoManager();
        textPane.getDocument().addUndoableEditListener(um);
	    editorPane.initialize(lineNum);
		textPane.setFont(font);
		textPane.getDocument().addDocumentListener(new SyntaxHighlighter(textPane));
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
	    System.out.println(editTabbedPane.getTabCount());
		int index = editTabbedPane.indexOfComponent(untitledPane);
		editTabbedPane.setSelectedIndex(index);
		//当文本区域内容改变时，设置变化
		textPane.getDocument().addDocumentListener(new DocumentListener() {
			int index = editTabbedPane.getSelectedIndex();
			String filename = editPaneList.get(index).getFilename();
			@Override
			public void insertUpdate(DocumentEvent e) {
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
//				StyledDocument oDoc = textPane.getStyledDocument();
//				StyleContext context = new StyleContext();
//				StyledDocument nDoc = new DefaultStyledDocument(context);
//				try {
//					CodeStyle codeStyle = new CodeStyle(ColorType.WHITE,null);
//					String paneText =textPane.getText();
//					String text = oDoc.getText(0, oDoc.getLength());
//					codeStyle.initCodeStyle(paneText,oDoc);
//					oDoc.removeDocumentListener(this);
//					nDoc.addDocumentListener(this);
//
//					int off = textPane.getCaretPosition();
//					textPane.setDocument(nDoc);
//					textPane.setCaretPosition(off);
//				} catch (BadLocationException be) {
//					be.printStackTrace();
//				} catch (IOException ie){
//
//				}finally {
//					oDoc = null;
//				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				lblTitle.setText("*"+ filename);
				editPaneList.get(index).setIschange(true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				lblTitle.setText(filename);
				editPaneList.get(index).setIschange(false);

			}
		});
		pnlTab = new JPanel();
		pnlTab.setOpaque(false);
		lblTitle = new JLabel(editTabbedPane.getTitleAt(index));
		lblTitle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int i = editTabbedPane.indexOfTabComponent(e.getComponent().getParent());
				if(SwingUtilities.isLeftMouseButton(e)){
					editTabbedPane.setSelectedIndex(i);
				}
				if(SwingUtilities.isRightMouseButton(e)){
					System.out.println(i);
					popupMenu.show(e.getComponent(),e.getX(),e.getY());
					closeSelfItem.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent e) {
							if(SwingUtilities.isLeftMouseButton(e)){
								System.out.println(i);
								close(i);
							}
						}
					});
					closeOthersItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(int j = 0; j <= editTabbedPane.getTabCount()-1;j++){
								if (i != j) {
									close(j);
								}
							}
						}
					});
					closeALLItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							editTabbedPane.removeAll();
							editPaneList.clear();
						}
					});
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

	/**
	 * 词法分析
	 * @param path
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void lex(String path)throws FileNotFoundException,IOException{
		lexer = new Lexer(path);
		tokens = new ArrayList<>();
		Token token = lexer.nextToken();
		while(token.getTokenType()!=TokenType.END_OF_DOC){
			tokens.add(token);
			token = lexer.nextToken();
		}
		DefaultMutableTreeNode lexNode = new DefaultMutableTreeNode("词法分析结果");
		for(Token t:tokens){
			DefaultMutableTreeNode tokenNode = new DefaultMutableTreeNode(t.getTokenType());
			DefaultMutableTreeNode lineNode = new DefaultMutableTreeNode("行号: "+t.getLineNum());
			DefaultMutableTreeNode positionNode = new DefaultMutableTreeNode("位置: "+t.getPosition());
			DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode("值: "+t.getValue());
			tokenNode.add(lineNode);
			tokenNode.add(positionNode);
			tokenNode.add(valueNode);
			lexNode.add(tokenNode);
		}
		JTree lexerTree = new JTree(lexNode);
		analysisPane.setComponentAt(0,new JScrollPane(lexerTree));
		analysisPane.setSelectedIndex(0);
	}

	/**
	 * 语法分析
	 * @param path
	 * @throws IOException
	 * @throws ParserException
	 */
	private void parse(String path)throws IOException,ParserException{
		parser = new Parser(path);
		TreeNode root = parser.parseProgram();
		DefaultMutableTreeNode parseNode = parseChild(root);
		JTree parseTree = new JTree(parseNode);
		JScrollPane parPane = new JScrollPane(parseTree);
		parPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		parPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		analysisPane.setComponentAt(1,parPane);
		analysisPane.setSelectedIndex(1);

	}
	private DefaultMutableTreeNode parseChild(TreeNode root){
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root.getValue());
		for(TreeNode t:root.getChildren()){
			rootNode.add(parseChild(t));
		}
		return rootNode;
	}
	private void syntaxRun(String path)throws Exception{
		tabConsolePane.setText(null);
		sematics = new Sematics(path);
		sematics.start();
	}

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
	public static void main(String[] args) {
		MainFrame frame = new MainFrame("CMM解释器");
		frame.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		if(("/*\n" +
				"output:\n" +
				"报错:报错信息可能各异\n" +
				"*/").matches("/\\*(\\s|.)*?\\*/")){
			System.out.print("pipei");
		}

	}
}
