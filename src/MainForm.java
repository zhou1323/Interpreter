/*
import javax.swing.*;
import java.awt.BorderLayout;

public class MainForm {
    private JPanel Title;
    private JToolBar ToolBar;

}
*//*


import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

*/
/**
 * ?????
 *//*

public class MainForm {


    private Frame f;
    private MenuBar mb;
    private Menu fi,ed,ru,he,subfi,subed;
    private MenuItem closeItem,openItem,saveItem,subItem1,subItem;
    private MenuItem undoItem,redoItem;
    private FileDialog openDialog,saveDialog;
    private TextArea ta;
    private File file;

    MainForm(){
        init();
    }

    private void init(){
        f=new Frame("CMM Interpreter");
        f.setBounds(300,100,1400,900);
        //f.setLayout();

        mb=new MenuBar();*/
/**//*

        fi=new Menu("文 件(F)");
        closeItem=new MenuItem("退 出 (Ctrl+E)");
        openItem=new MenuItem("打 开 (Ctrl+O)");
        saveItem=new MenuItem("保 存 (Ctrl+S)");

        subfi=new Menu("新 建 (Ctrl+N)");
        subItem1=new MenuItem("Web Project");
        subItem=new MenuItem("Java Project");
        subfi.add(subItem);
        subfi.add(subItem1);

        fi.add(subfi);
        fi.add(openItem);
        fi.add(saveItem);
        fi.add(closeItem);

        ed=new Menu("编 辑(E)");

        ru=new Menu("运 行(R)");
        he=new Menu("帮 助(H)");
        undoItem=new MenuItem("撤 销 (Ctrl+Z)");
        redoItem=new MenuItem("重 做 (Ctrl+Y)");

        ed.add(undoItem);
        ed.add(redoItem);
        mb.add(fi);
        mb.add(ed);
        mb.add(ru);
        mb.add(he);

        openDialog=new FileDialog(f,"????",FileDialog.LOAD);
        saveDialog=new FileDialog(f,"保存",FileDialog.SAVE);

        ta=new TextArea();

        f.add(ta);
        f.setMenuBar(mb);
        myEvent();

        f.setVisible(true);

    }

    private void myEvent() {

        saveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                if(file==null){
                    saveDialog.setVisible(true);
                    String dirPath=saveDialog.getDirectory();
                    String fileName=saveDialog.getFile();
                    if(dirPath==null || fileName==null)
                        return;
                    file=new File(dirPath,fileName);
                }
                try {

                    BufferedWriter bufw=new BufferedWriter(new FileWriter(file));

                    String text=ta.getText();

                    bufw.write(text);
                    bufw.close();
                } catch (IOException e2) {
                    throw new RuntimeException("????");
                }

            }
        });

        //????
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                openDialog.setVisible(true);
                String dirPath=openDialog.getDirectory();
                String fileName=openDialog.getFile();
                System.out.println(dirPath+"...."+fileName);
                if(dirPath==null || fileName==null)
                    return;
                ta.setText("");
                file=new File(dirPath,fileName);
                try {
                    BufferedReader bufr=new BufferedReader(new FileReader(file));
                    String line=null;
                    while((line=bufr.readLine())!=null){
                        ta.append(line+"\r\n");
                    }
                    bufr.close();
                } catch (IOException e2) {
                    throw new RuntimeException("????");
                }

            }
        });


        closeItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                System.exit(0);
            }
        });

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }

        });

    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new MainForm();
    }

}
*/
