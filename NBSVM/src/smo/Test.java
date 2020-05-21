package smo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class Test {
	
	
	public static void main(String[] args) {
		String data_X = "F:\\javatest\\NBSVM\\src\\gla_x.txt";
		String data_Y = "F:\\javatest\\NBSVM\\src\\gla_y.txt";
		double[][] data_x = NBSVM.getdata_x(data_X);
		Integer[] data_y = NBSVM.getdata_y(data_Y);
		Integer[] label_kinds;
		Vector<Integer> y= new Vector<Integer>();
		Vector<Vector<Vector<Double>>> xxx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Vector<Vector<Double>>>> xxx_divied = new Vector<Vector<Vector<Vector<Double>>>>();
		Vector<Vector<Integer>> y_divied = new Vector<Vector<Integer>>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		Vector<Double> x = new Vector<Double>();
		int index;
		double[][] x0;
		Integer[] y0;
		double[][][] x1;
		Integer[] y1;
		double q;
		int rest;
		Random randoms = new Random();
		//归一化
		double sum;
		double mean;
		double std;
		for(int i=0;i<data_x.length;i++) {
			sum=0;
			mean=0;
			std=0;
			for(int j=0;j<data_x[i].length;j++) 
				sum+=data_x[i][j];
			 mean=sum/data_x[i].length;
			 sum=0;
			for(int j=0;j<data_x[i].length;j++) 
					sum+=(data_x[i][j]-mean)*(data_x[i][j]-mean);
			std=sum/data_x[i].length;
			for(int j=0;j<data_x[i].length;j++) {
				if(std>0)data_x[i][j] = (data_x[i][j]-mean)/(std);
				else data_x[i][j]=0.0;
			}
		}
		//System.out.println(Arrays.deepToString(data_x));
		//交叉验证组数
		int k=10;
		double[] result;
		double[] finally_result = new double[k];
		double k_result;
		Vector<Integer> list = new Vector<Integer>();
		
		
		for(int i=0;i<data_y.length;i++) {
			if(!list.contains(data_y[i])) {
				list.add(data_y[i]);
			}
		}
		label_kinds = list.toArray(new Integer[list.size()]);
		//将样本按类别分类
		for(int i=0;i<label_kinds.length;i++) {
				for(int z=0;z<data_x.length;z++) {
					if(data_y[z]==label_kinds[i]) {
						for(int p=0;p<data_x[z].length;p++)x.add(data_x[z][p]);
						xx.add(x);
						x = new Vector<Double>();
					}
				}
				y.add(label_kinds[i]);
				xxx.add(xx);
				xx = new Vector<Vector<Double>>();
		}
		
		for(int i=0;i<k;i++) {
			xxx_divied.add(new Vector<Vector<Vector<Double>>>());
			y_divied.add(new Vector<Integer>());
			for(int j=0;j<xxx.size();j++) {
				xxx_divied.get(i).add(new Vector<Vector<Double>>());
			}
		}
		//将样本的每一类随机分成10份
		rest = data_x.length;
		while(rest>0) {
			rest=0;
			for(int i=0;i<xxx.size();i++) {
				for(int j=0;j<k;j++) {
					if(xxx.get(i).size()==0) break;
					index = randoms.nextInt(xxx.get(i).size());
					xxx_divied.get(j).get(i).add(xxx.get(i).get(index));
					xxx.get(i).remove(index);
					rest++;
				}
			}
		}
		for(int i=0;i<k;i++) 
			for(int j=0;j<y.size();j++)y_divied.get(i).add(y.get(j));
		
		//将没有分到某一类样本的某一折的x和y这一类去掉
		for(int i=0;i<k;i++) 
			for(int j=xxx.size()-1;j>-1;j--) 
				if(xxx_divied.get(i).get(j).size()==0) {
					xxx_divied.get(i).remove(j);
					y_divied.get(i).remove(j);
				}

		//在for里将样本有序的组合成9,1分的训练和测试样本进行测试
		for(int j=0;j<k;j++) {
			index=0;
			for(int i=0;i<j;i++)for(int m=0;m<xxx_divied.get(i).size();m++)index+=xxx_divied.get(i).get(m).size();
			for(int i=j+1;i<k;i++)for(int m=0;m<xxx_divied.get(i).size();m++)index+=xxx_divied.get(i).get(m).size();
			x0 = new double [index][];
			y0 = new Integer [index];
			index=0;
			for(int i=0;i<j;i++) {
				for(int m=0;m<xxx_divied.get(i).size();m++) {
					for(int n=0;n<xxx_divied.get(i).get(m).size();n++) {
						x0[index] = new double[xxx_divied.get(i).get(m).get(n).size()];
						for(int z=0;z<xxx_divied.get(i).get(m).get(n).size();z++) {
							x0[index][z]= xxx_divied.get(i).get(m).get(n).get(z);
						}
						y0[index]=y_divied.get(i).get(m);
						index++;
					}
				}
			}
			for(int i=j+1;i<k;i++) {
				for(int m=0;m<xxx_divied.get(i).size();m++) {
					for(int n=0;n<xxx_divied.get(i).get(m).size();n++) {
						x0[index] = new double[xxx_divied.get(i).get(m).get(n).size()];
						for(int z=0;z<xxx_divied.get(i).get(m).get(n).size();z++) {
							x0[index][z]= xxx_divied.get(i).get(m).get(n).get(z);
						}
						y0[index]=y_divied.get(i).get(m);
						index++;
					}
				}
			}
			
			x1 = new double [xxx_divied.get(j).size()][][];
			y1=new Integer[y_divied.get(j).size()];
			for(int m=0;m<xxx_divied.get(j).size();m++) {
				x1[m] = new double[xxx_divied.get(j).get(m).size()][];
				for(int n=0;n<xxx_divied.get(j).get(m).size();n++) {
					x1[m][n] = new double[xxx_divied.get(j).get(m).get(n).size()];
					for(int z=0;z<xxx_divied.get(j).get(m).get(n).size();z++) {
						x1[m][n][z] = xxx_divied.get(j).get(m).get(n).get(z);
					}
				}
				y1[m] = y_divied.get(j).get(m);
			}
			//System.out.print(Arrays.toString(a));
			
			
			
			//现在基因片段由C,正负类的p,和sigma组成
			OVO_NBSVM mysmo = new OVO_NBSVM(x0, y0,0.05, "rbf");
			result = new double[x1.length];
			for(int n=0;n<x1.length;n++) {
				q=0;
				for(int m=0;m<x1[n].length;m++)if(y1[n]==mysmo.predict(x1[n][m]))q++;
				result[n] = q/x1[n].length;
				System.out.println("预测的类别:"+y1[n]);
				System.out.println("预测的类别的向量总数:"+x1[n].length);
				System.out.println("预测准确率"+result[n]);
			}
			finally_result[j]=0;
			for(int n=0;n<result.length;n++)finally_result[j]+=result[n];
			finally_result[j]=finally_result[j]/result.length;
			System.out.println("第"+j+"次测试结果"+finally_result[j]);
		}
		k_result=0.0;
		for(int i=0;i<k;i++)k_result+=finally_result[i];
		k_result = k_result/k;
		
		
		
		
		System.out.print("10折结果:"+k_result);
	}
}
