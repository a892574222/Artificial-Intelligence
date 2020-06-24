package smo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NBSVM {
	//获取样本属性
	public static double[][] getdata_x(String path) {
		List<double[]> data = new ArrayList<>();
		double[] p = null;
		try {
		File file = new File(path);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String linetxt = null;
		while((linetxt = bufferedReader.readLine()) != null) {
			p = new double[linetxt.split("	").length];
			for(int i=0;i<linetxt.split("	").length;i++)p[i] = Double.parseDouble(linetxt.split("	")[i]);
			data.add(p);
		}
		read.close();
		}catch(Exception e){
			System.out.println("读取文件时出错");
            e.printStackTrace();
		}
		double[][] array = data.toArray(new double[data.size()][]);
		return array;
	}
	//获取样本分类结果
	public static Integer[] getdata_y(String path) {
		List<Integer> data = new ArrayList<>();
		try {
		File file = new File(path);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String linetxt = null;
		while((linetxt = bufferedReader.readLine()) != null) {
			data.add(Integer.parseInt(linetxt));
		}
		read.close();
		}catch(Exception e){
			System.out.println("读取文件时出错");
            e.printStackTrace();
		}
		Integer[] array = data.toArray(new Integer[data.size()]);
		return array;
	}
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		String data_x = "F:\\javatest\\SVM\\src\\data_x.txt";
		String data_y = "F:\\javatest\\SVM\\src\\data_y.txt";
		double[][] x = getdata_x(data_x);
		Integer[] y = getdata_y(data_y);
		//交叉验证组数
		int k=10;
		//记录预测正确的样本
		double p=0;
		double q=0;
		double[][] x0 = new double [(x.length)/k][];
		Integer[] y0 = new Integer [(x.length)/k];
		double[][] x1 = new double [x0.length*(k-1)][];
		Integer[] y1 = new Integer [x0.length*(k-1)];
		for(int j=0;j<k;j++) {
		p=0;
		q=0;
		for(int i=0;i<x0.length+x1.length;i++) {
			if(i<x0.length*(j+1)&&i>=x0.length*j) {x0[i-x0.length*j] = x[i];y0[i-x0.length*j] = y[i];}
			else{
				if(i<x0.length*j) {x1[i] = x[i];y1[i] = y[i];}
				if(i>=x0.length*(j+1)) {x1[i-x0.length]  = x[i];y1[i-x0.length]  = y[i];}

			}
		}
		//System.out.println(Arrays.deepToString(x1));
		Another_SMO mysmo = new Another_SMO(x1, y1, 0.0,1.0,1.0,2.0,2.0, "rbf");
		for(int i=0;i<x0.length;i++) if(y0[i]==-1)q++;
		for(int i=0;i<x0.length;i++) if(mysmo.predict(x0[i])==-1)p++;
		System.out.print("交叉验证第"+(j+1)+"组结果");
		System.out.println((p/q)*100);
		}


		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间


	}
	
}