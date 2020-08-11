package smo;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class Grid_Search {
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
	
	private double[][][] x1;
	private Integer[] y1;
	private NBSVM_SMO[] ovo;
	
	private double[] finally_result = new double[14641];
	private String[] finally_string = new String[14641];
	
	public Grid_Search(double[][]data_x,Integer[] data_y,double tol,double[][][]x1,Integer[]y1) {
		this.tol = tol;
		label_kinds=set_num(data_y);
		data_divied(data_x,data_y);
		this.x1 = x1;
		this.y1 = y1;
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
		//训练样本的百分比
		float train_percent=1.0f;
		Random randoms = new Random();
		Vector<Vector<Vector<Double>>> data_xx = new Vector<Vector<Vector<Double>>>();
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
			while(sum/size<train_percent) {
				index=randoms.nextInt(xxx.get(i).size());
				xx.add(xxx.get(i).get(index));
				xxx.get(i).remove(index);
				sum++;
			}
			data_xx.add(xx);
			xx = new Vector<Vector<Double>>();
//			validation_x.add(xxx.get(i));
		}
		
		this.data_yy = y.toArray(new Integer[y.size()]);
		validation_data_y = y.toArray(new Integer[y.size()]);
		y.clear();
		
		validation_data_x = new double[data_xx.size()][][];
		for(int i=0;i<data_xx.size();i++) {
			validation_data_x[i] = new double[data_xx.get(i).size()][];
			for(int j=0;j<data_xx.get(i).size();j++) {
				validation_data_x[i][j] = new double[data_xx.get(i).get(j).size()];
				for(int k=0;k<data_xx.get(i).get(j).size();k++)validation_data_x[i][j][k] =data_xx.get(i).get(j).get(k);
			}
		}
		
		this.data_xx = new double[data_xx.size()][][];
		for(int i=0;i<this.data_xx.length;i++) {
			this.data_xx[i] = new double[(int)Math.ceil(data_xx.get(i).size()*1.0)][];
			for(int j=0;j<this.data_xx[i].length;j++) {
				this.data_xx[i][j] = new double[data_xx.get(i).get(j).size()];
				for(int k=0;k<data_xx.get(i).get(j).size();k++)this.data_xx[i][j][k] =data_xx.get(i).get(j).get(k);
			}
		}
		data_xx.clear();
		
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
	
	public double[] get_result() {
		return finally_result;
	}
	
	public String[] get_string() {
		return finally_string;
	}
	
	private void get_parameters() {
		Another_SMO[] ovo= new Another_SMO[train_data_x.length];
		double[][] res = new double[2][4];
		double[] result = new double[x1.length];
		int index=0;
		double distance[][] = new double[2][2];
		for(int i0=-3;i0<8;i0++) {
			for(int i1=-3;i1<8;i1++) {
				for(double i2=0;i2<=1;i2+=0.1) {
					for(double i3=0;i3<=1;i3+=0.1) {
						
						for(int i=0;i<train_data_x.length;i++) {
							ovo[i] = new Another_SMO(train_data_x[i],train_data_y[i],tol, Math.pow(10, i0),Math.pow(10, i1),i2,i3, "rbf");
							}
//						System.out.println(Math.pow(10, i0));
//						System.out.println(Math.pow(10, i1));
//						System.out.println(i2);
//						System.out.println(i3);
						for(int n=0;n<x1.length;n++) {
							double q = 0;
							for(int m=0;m<x1[n].length;m++)if(y1[n]==this.predict(ovo,x1[n][m]))q++;
							result[n] = q/x1[n].length;
//							System.out.println("预测的类别:"+y1[n]);
//							System.out.println("预测的类别的向量总数:"+x1[n].length);
//							System.out.println("预测准确率"+result[n]);
						}
						double finally_result0 = 0;
						for(int n=0;n<result.length;n++)finally_result0+=result[n];
						finally_result0=finally_result0/result.length;
						//System.out.println("第"+(index)+"次test测试结果"+finally_result);
						finally_result[index] = finally_result0;
						finally_string[index] = i0+","+i1+","+i2+","+i3+":";
						index++;
						
						
//						result = new double[validation_data_x.length];
//						for(int n=0;n<validation_data_x.length;n++) {
//							double q = 0;
//							for(int m=0;m<validation_data_x[n].length;m++)if(validation_data_y[n]==this.predict(ovo,validation_data_x[n][m]))q++;
//							result[n] = q/validation_data_x[n].length;
////							System.out.println("预测的类别:"+validation_data_y[n]);
////							System.out.println("预测的类别的向量总数:"+validation_data_x[n].length);
////							System.out.println("预测准确率"+result[n]);
//						}
//						double finally_result1 = 0;
//						for(int n=0;n<result.length;n++)finally_result1+=result[n];
//						finally_result1=finally_result1/result.length;
						
						
						
//						System.out.println("第"+(i0+4+i1+4+i2+1+i3+1)+"次validation测试结果"+finally_result);
//						if(finally_result0>distance[0][0]) {
//							distance[0][0]=finally_result0;
//							distance[0][1]=finally_result1;
//							res[0][0]= i0;
//							res[0][1]= i1;
//							res[0][2]= i2;
//							res[0][3]= i3;
//						}
//						if(finally_result1>distance[1][1]) {
//							distance[1][0]=finally_result0;
//							distance[1][1]=finally_result1;
//							res[1][0]= i0;
//							res[1][1]= i1;
//							res[1][2]= i2;
//							res[1][3]= i3;
//						}
//						System.out.println("----------------");
					}
				}
			}
		}
//		System.out.println("test最佳时的分布：");
//		System.out.println("test平均准确率："+distance[0][0]);
//		System.out.println("validation平均准确率："+distance[0][1]);
//		System.out.println(Arrays.toString(res[0]));
//		System.out.println("training最佳时的分布：");
//		System.out.println("test平均准确率："+distance[1][0]);
//		System.out.println("training平均准确率："+distance[1][1]);
//		System.out.println(Arrays.toString(res[1]));
//		System.out.println("-----------------");
		
	}
	
	private void train() {
		get_parameters();	
	}
	
	//最高票数相同的时候在所有最高票数的类中随机选取
	public Integer predict(Another_SMO[] ovo,double[] unkonwn) {
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
