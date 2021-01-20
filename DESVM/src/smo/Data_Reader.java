package smo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Data_Reader {
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
	
	//获取样本分类结果
	public static Integer[] getdata_attributes(String path) {
		Integer[] p = null;
		try {
		File file = new File(path);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String linetxt = null;
		linetxt = bufferedReader.readLine();
		p = new Integer[linetxt.split("	").length];
		for(int i=0;i<linetxt.split("	").length;i++)p[i] = Integer.parseInt(linetxt.split("	")[i]);
		read.close();
		}catch(Exception e){
			System.out.println("读取文件时出错");
            e.printStackTrace();
		}
		return p;
	}
}