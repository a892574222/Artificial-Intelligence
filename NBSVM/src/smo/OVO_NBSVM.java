package smo;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class OVO_NBSVM {
	private Integer[] label_kinds;
	private double tol;
	private String kernel;
	//按类别分类的未combine的train_data
	private double[][][] data_xx;
	private Integer[] data_yy;
	//训练样本
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	//验证样本
	private double[][][] validation_data_x;
	private Integer[] validation_data_y;
	//原始样本
	private double[][][] finally_x;
	private Integer[][] finally_y;
	private NBSVM_SMO[] ovo;
	public OVO_NBSVM(double[][]data_x,Integer[] data_y,double tol,String kernel) {
		this.tol = tol;
		this.kernel = kernel;
		label_kinds=set_num(data_y);
		data_divied(data_x,data_y);
		set_combinations();
		train();
	}
	
	//获取多分类中样本的种类个数
	private Integer[] set_num(Integer[] data_y) {
		Integer[] result;
		Vector<Integer> list = new Vector<Integer>();
		for(int i=0;i<data_y.length;i++) {
			if(!list.contains(data_y[i])) {
				list.add(data_y[i]);
			}
		}
		result = list.toArray(new Integer[list.size()]);
		return result;
	}
	
	//将样本每一类随机7，3分为未combine的训练样本和验证样本
	private void data_divied(double[][]data_x,Integer[] data_y) {
		Vector<Integer> y= new Vector<Integer>();
		Vector<Vector<Vector<Double>>> xxx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		Vector<Double> x = new Vector<Double>();
		double size;
		double sum;
		int index;
		float train_percent=0.7f;
		Random randoms = new Random();
		Vector<Vector<Vector<Double>>> data_xx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Vector<Double>>> validation_x = new Vector<Vector<Vector<Double>>>();
		//原始数据的按照类别分类的未划分数据

		for(int i=0;i<label_kinds.length;i++) {
				for(int k=0;k<data_x.length;k++) {
					if(data_y[k]==label_kinds[i]) {
						for(int p=0;p<data_x[k].length;p++)x.add(data_x[k][p]);
						xx.add(x);
						x = new Vector<Double>();
					}
				}
				y.add(label_kinds[i]);
				xxx.add(xx);
				xx = new Vector<Vector<Double>>();
		}
		for(int i=0;i<xxx.size();i++) {
			sum=0;
			size=xxx.get(i).size();
			while(sum/size<train_percent&&(xxx.get(i).size()>1)) {
				index=randoms.nextInt(xxx.get(i).size());
				xx.add(xxx.get(i).get(index));
				xxx.get(i).remove(index);
				sum++;
			}
			data_xx.add(xx);
			xx = new Vector<Vector<Double>>();
			validation_x.add(xxx.get(i));
		}
		
		this.data_yy = y.toArray(new Integer[y.size()]);
		validation_data_y = y.toArray(new Integer[y.size()]);
		y.clear();
		this.data_xx = new double[data_xx.size()][][];
		for(int i=0;i<data_xx.size();i++) {
			this.data_xx[i] = new double[data_xx.get(i).size()][];
			for(int j=0;j<data_xx.get(i).size();j++) {
				this.data_xx[i][j] = new double[data_xx.get(i).get(j).size()];
				for(int k=0;k<data_xx.get(i).get(j).size();k++)this.data_xx[i][j][k] =data_xx.get(i).get(j).get(k);
			}
		}
		data_xx.clear();
		validation_data_x = new double[validation_x.size()][][];
		
		for(int i=0;i<validation_x.size();i++) {
			validation_data_x[i] = new double[validation_x.get(i).size()][];
			for(int j=0;j<validation_x.get(i).size();j++) {
				validation_data_x[i][j] = new double[validation_x.get(i).get(j).size()];
				for(int k=0;k<validation_x.get(i).get(j).size();k++)validation_data_x[i][j][k] =validation_x.get(i).get(j).get(k);
			}
		}
		validation_x.clear();
//		index=0;
//		finally_x = new double[this.data_xx.length*(this.data_xx.length-1)/2][][];
//		finally_y = new Integer[data_yy.length*(data_yy.length-1)/2][];
//		for(int i=0;i<this.data_xx.length-1;i++) {
//			for(int j=i+1;j<this.data_xx.length;j++) {
//				finally_x[index] = new double[this.data_xx[i].length+validation_data_x[i].length+this.data_xx[j].length+validation_data_x[j].length][];
//				
//				finally_y[index] = new Integer[this.data_xx[i].length+validation_data_x[i].length+this.data_xx[j].length+validation_data_x[j].length];
//				for(int z=0;z<this.data_xx[i].length;z++) {
//					finally_x[index][z]=this.data_xx[i][z];
//					finally_y[index][z]=data_yy[i];
//				}
//				for(int z=0;z<validation_data_x[i].length;z++) {
//					finally_x[index][z+this.data_xx[i].length]=validation_data_x[i][z];
//					finally_y[index][z+this.data_xx[i].length]=validation_data_y[i];
//				}
//				for(int z=0;z<this.data_xx[j].length;z++) {
//					finally_x[index][z+validation_data_x[i].length+this.data_xx[i].length]=this.data_xx[j][z];
//					finally_y[index][z+validation_data_x[i].length+this.data_xx[i].length]=data_yy[j];
//				}
//				for(int z=0;z<validation_data_x[j].length;z++) {
//
//					finally_x[index][z+validation_data_x[i].length+this.data_xx[j].length+this.data_xx[i].length]=validation_data_x[j][z];
//					finally_y[index][z+validation_data_x[i].length+this.data_xx[j].length+this.data_xx[i].length]=validation_data_y[j];
//				}
//				index++;
//			}
//		}
		
	}
	
	
	//获取所有的组合
	private void set_combinations() {
		train_data_x = new double[data_xx.length*(data_xx.length-1)/2][][];
		train_data_y = new Integer[data_yy.length*(data_yy.length-1)/2][];
		int index=0;
		for(int i=0;i<data_xx.length-1;i++) {
			for(int j=i+1;j<data_xx.length;j++) {
				train_data_x[index] = new double[data_xx[i].length+data_xx[j].length][];
				train_data_y[index] = new Integer[data_xx[i].length+data_xx[j].length];
				for(int z=0;z<data_xx[i].length;z++) {
					train_data_x[index][z]=data_xx[i][z];
					train_data_y[index][z]=data_yy[i];
				}
				for(int z=0;z<data_xx[j].length;z++) {
					train_data_x[index][z+data_xx[i].length]=data_xx[j][z];
					train_data_y[index][z+data_xx[i].length]=data_yy[j];
				}
				index++;
			}
		}
	}
	
	
	private double[][] get_parameters() {
		double[] l= {0.0,0.5,0.0,0.0};
		double[] u= {1000000000,1.0,0.5,1000};
		//基因片段是所有SVM的四个参
		double[][] result;
		DE de = new DE(train_data_x,train_data_y,validation_data_x,validation_data_y,tol,l,u,kernel);
		result = de.get_result();
		return result;
	}
	
	private void train() {
		double[][] parameters = new double[train_data_x.length][];
//		double[] a = new double[4];
//		Scanner input = new Scanner(System.in);
//		for(int i=0;i<4;i++)a[i]=input.nextDouble();
//		for(int i=0;i<parameters.length;i++)parameters[i] = a;
		parameters = get_parameters();
		
		ovo= new NBSVM_SMO[train_data_x.length];
		for(int i=0;i<train_data_x.length;i++) 
			ovo[i] = new NBSVM_SMO(train_data_x[i],train_data_y[i],tol, parameters[i][0],parameters[i][1],parameters[i][2], kernel,parameters[i][3]);
	}
	
	//最高票数相同的时候在所有最高票数的类中随机选取
	public Integer predict(double[] unkonwn) {
		int value[] = new int[validation_data_y.length];
		Vector<Integer> index_list = new Vector<Integer>();
		Integer result;
		int p=0;
		for(int i=0;i<ovo.length;i++) {
			result = ovo[i].predict(unkonwn);
			for(int j=0;j<validation_data_y.length;j++)if(result==validation_data_y[j])value[j]++;
		}
		for(int i=1;i<value.length;i++)if(value[i]>value[p])p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length<validation_data_x[p].length)p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length==validation_data_x[p].length)index_list.add(i);
		p=new Random().nextInt(index_list.size());
		p=index_list.get(p);
		return validation_data_y[p];
	}
}
