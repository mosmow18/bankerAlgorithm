package com.banker;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

import static com.banker.BankerAlgorithm.init;
import static com.banker.BankerAlgorithm.request;

public class BankersAlgorithmGUI {


    // JFrame 用于显示界面
    private static JFrame frame;
    //显示结果区域
    private static JTextArea resultArea;
    private static File selectedFile;


    // 用于在文本区显示结果
    public static void appendResult(String message) {
        resultArea.append(message + "\n");
    }

    // 创建界面
    private static void createGUI() {
        frame = new JFrame("银行家算法 - 可视化界面");
        frame.setSize(600, 500);
        //置顶
        frame.setAlwaysOnTop(true);
        //居中
        frame.setLocationRelativeTo(null);
        //关闭方式
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //布局组件
        frame.setLayout(new BorderLayout());

        // 创建文本区域显示结果
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 创建资源请求面板
        JPanel requestPanel = new JPanel();
        requestPanel.setLayout(new GridLayout(4, 1));

        JTextField processField = new JTextField();
        JTextField requestField = new JTextField();

        //资源申请按钮
        JButton requestButton = getjButton(processField, requestField);

        //文件选择按钮
        JButton openButton = getjButton();


        requestPanel.add(new JLabel("进程 ID:"));
        requestPanel.add(processField);
        requestPanel.add(new JLabel("请求资源 (格式: 资源1, 资源2, 资源3):"));
        requestPanel.add(requestField);
        requestPanel.add(requestButton);
        requestPanel.add(openButton);

        frame.add(requestPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private static JButton getjButton() {
        JButton openButton = new JButton("选择输入文件");
        //时间监听,获取输入文件
        openButton.addActionListener(e -> {
            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // 获取用户选择的文件
                selectedFile = fileChooser.getSelectedFile();
                try {
                    init(selectedFile);//将选择的文件,交给银行家算法判断
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                // 打印文件路径
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            }
        });
        return openButton;
    }

    private static JButton getjButton(JTextField processField, JTextField requestField) {
        JButton requestButton = new JButton("请求资源");
        //事件监听，获取输入的请求
        requestButton.addActionListener(e -> {
            try {
                int processId = Integer.parseInt(processField.getText());
                String[] reqStr = requestField.getText().split(",");
                int[] apply = new int[reqStr.length];
                for (int i = 0; i < reqStr.length; i++) {
                    apply[i] = Integer.parseInt(reqStr[i]);
                }
                //申请资源
                request(processId, apply);
            } catch (Exception ex) {
                appendResult("输入无效，请检查输入格式！");
            }
        });
        return requestButton;
    }

    public static void main(String[] args) {
        createGUI();
    }
}