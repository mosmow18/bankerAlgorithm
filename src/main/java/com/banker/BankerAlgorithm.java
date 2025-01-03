package com.banker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.banker.BankersAlgorithmGUI.appendResult;

public class BankerAlgorithm {
    static int n = 0;//进程数
    static int m = 0;//资源数
    static Process[] processes;//进程数组
    static int[] available;//当前可用资源
    static boolean flag = false;//标记是否存在安全序列
    static List<Integer> set = new ArrayList<>();//存储完成分配资源，已经释放资源的进程
    static int[][] temp;
    static boolean[] release;

    //进程类
    static class Process{
        int[] max;//所需最大资源
        int[] allocation;//已分派资源
        int[] need;//还所需资源

        public Process(int[] max, int[] allocation) {
            this.max = max;
            this.allocation = allocation;
            this.need = new int[max.length];
            initNeed();
        }
        private void initNeed() {
            for (int i = 0; i < max.length; i++) if(max[i] >= allocation[i]) need[i] = max[i] - allocation[i];
        }

        @Override
        public String toString() {
            return show(max)+"\t"+show(allocation)+" \t"+show(need)+"\n";
        }
    }
    private static String show(int[] msg){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < msg.length; i++){
            sb.append(msg[i]).append(" ");
        }
        return sb.toString();
    }

    //从文件读取数据
    private static void readData(File selectedFile) throws FileNotFoundException {
        Scanner sc = new Scanner(selectedFile);
        if(sc.hasNext()) {
            n = sc.nextInt();//进程数
            m = sc.nextInt();//资源数
            //读取数据
            processes = new Process[n];
            for (int i = 0; i < n; i++) {
                int[] max = new int[m];
                for (int j = 0; j < m; j++) {
                    max[j] = sc.nextInt();
                }
                int[] allocation = new int[m];
                for (int j = 0; j < m; j++) {
                    allocation[j] = sc.nextInt();
                }
                processes[i] = new Process(max, allocation);
            }
            available = new int[m];
            for (int i = 0; i < m; i++) {
                available[i] = sc.nextInt();
            }
        }
        sc.close();
    }
    //检测判断,该进程是否能够分配资源
    private static boolean check(Process process){
        for (int i = 0; i < m; i++) {
            if(process.need[i] > available[i]){
                return false;
            }
        }
        return true;
    }

    /**
     * 处理单个进程请求
     * @param id 进程号
     * @param apply 申请的资源
     */
    public static void request(int id, int[] apply){
        for (int i = 0; i < m; i++) {//判断请求合法
            if( apply[i] > available[i]){
                System.out.println("该请求无安全序列");
                appendResult("\t该请求无安全序列,将产生死锁，无法为其分配资源");
//                back();
//                showPrint();
                return;
            }

            if(apply[i] > processes[id].need[i]){
                System.out.println("该请求资源数超过进程实际所需资源");
                appendResult("\t请求错误,该请求资源数超过进程实际所需资源");
                return;
            }
        }

        boolean ok = true;
        for (int i = 0; i < m; i++) {//处理申请，为其分配资源
            available[i] -= apply[i];
            processes[id].need[i] -= apply[i];
            processes[id].allocation[i] += apply[i];
            if(processes[id].need[i] != 0){
                ok = false;
            }
        }
//        showPrint();
        if(ok){//如果该进程已完成分配，则释放资源
            for(int i = 0; i < m; i++){
                temp[id][i] = processes[id].allocation[i] - apply[i];//存储进程原来的已分配资源,用于产生死锁时，恢复原状态。
                available[i] += processes[id].allocation[i];
                processes[id].allocation[i] = 0;
            }
            System.out.println(Arrays.toString(temp[id]));
            set.add(id);
            release[id] = true;
        }

        showPrint();//打印

        banker(set);//搜索安全序列

        //如果不存在安全序列，会发生死锁，将前面释放的支援都归还原进程
        if(!flag){
            System.out.println("该请求无安全序列,将产生死锁");
            appendResult("\t该请求无安全序列,将产生死锁,已归还分配资源");
            back();

            if(!ok){//本次申请资源没有完全释放，归还已经分配的部分
                for(int i = 0; i < m; i++){
                    available[i] += apply[i];
                    processes[id].need[i] += apply[i];
                    processes[id].allocation[i] -= apply[i];
                }
            }
            showPrint();
        }else{
            flag = false;
        }
    }

    /**
     * 归还前面已经释放的资源
     */
    private static void back() {
        set.clear();
        for(int i = 0; i < n; i++){//归还前面已经完全释放的资源
            for (int j = 0; j < m; j++) {
                if (release[i]){
                    available[j] -= temp[i][j];
                    processes[i].allocation[j] = temp[i][j];
                    processes[i].need[j] = processes[i].max[j] - temp[i][j];
                }
            }
        }
    }

    /**
     * 银行家算法核心代码,采用dfs深度优先搜索，搜索当前资源分配状态下的所有安全序列
     * @param set 存储已经完成分配,释放资源的进程
     */
    private static void banker(List<Integer> set){
        if(set.size() == n){//找到一个安全序列
            if(!flag) appendResult("\t该请求存在安全序列");
            flag = true;
            StringBuilder msg = new StringBuilder("\t安全序列: P" + set.getFirst());
            for (int i = 1; i < n; i++){
                msg.append(" -> P").append(set.get(i));
            }
            System.out.println(msg.toString().trim());
            appendResult(msg.toString());
            return;
        }
        for (int i = 0; i < processes.length; i++) {
            //该进程还未进行分配完,且该进程可获得资源
            if(!set.contains(i) && check(processes[i])){
                //进行分配，释放该进程已拥有的资源
                for(int j = 0; j < m; j++) available[j] += processes[i].allocation[j];
                set.add(i);
                banker(set);//继续搜索可满足的进程,并将i进程标记为已判断
                //回溯,还原现场,将已释放资源重新还给原进程
                set.removeLast();
                for(int j = 0; j < m; j++)available[j] -= processes[i].allocation[j];
            }
        }
    }

    public static void init(File selectedFile) throws FileNotFoundException {

        readData(selectedFile);
        temp = new int[n][m];
        release = new boolean[n];
        showPrint();
        banker(set);
        if(!flag){
            System.out.println("该请求无安全序列,将产生死锁");
            appendResult("\t该请求无安全序列,将产生死锁");
        }
        flag = false;
    }

    private static void showPrint() {
        System.out.println("===============================================================");
        appendResult("\t===============================================================");
        System.out.println("Id  max  allocation need");
        appendResult("\tId \t max \t allocation \tneed");
        for (int i = 0; i < processes.length; i++) {
            String msg = i +" \t "+ processes[i];
            System.out.println(msg);
            appendResult("\t"+msg);
        }
        System.out.println("available\t" + show(available));
        appendResult("\tavailable\t" + show(available));
        appendResult("");
        System.out.println("===============================================================");
        appendResult("\t===============================================================");
    }
}
