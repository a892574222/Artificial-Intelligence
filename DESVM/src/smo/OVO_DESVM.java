package smo;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class OVO_DESVM {
	private Integer[] label_kinds;
	private double tol;
	private boolean type;
	//按类别分类的未combine的train_data
	private double[][][] data_xx;
	private Integer[] data_yy;
	//训练样本
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	
	//测试样本
	private double[][][] test_data_x;
	private Integer[] test_data_y;
	private String file;
	
	private DESVM_SMO[] ovo;
	public OVO_DESVM(double[][]data_x,Integer[] data_y,double tol,boolean type,double[][][] test_data_x,Integer[] test_data_y,String file) throws IOException {
		this.tol = tol;
		this.type = type;
		this.file = file;
		label_kinds=set_num(data_y);
		data_divied(data_x,data_y);
		this.test_data_x = test_data_x;
		this.test_data_y = test_data_y;
		set_combinations();
		train();
	}
	
	public Integer[] get_label_kinds() {
		return label_kinds;
	}
	
	//获取多分类中样本的种类个数
	public static Integer[] set_num(Integer[] data_y) {
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
	
	//训练样本==验证样本
	private void data_divied(double[][]data_x,Integer[] data_y) {
		Vector<Integer> y= new Vector<Integer>();
		Vector<Vector<Vector<Double>>> xxx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		Vector<Double> x = new Vector<Double>();

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

		
		this.data_yy = y.toArray(new Integer[y.size()]);
		y.clear();
		
		data_xx = new double[xxx.size()][][];
		for(int i=0;i<xxx.size();i++) {
			data_xx[i] = new double[xxx.get(i).size()][];
			for(int j=0;j<xxx.get(i).size();j++) {
				data_xx[i][j] = new double[xxx.get(i).get(j).size()];
				for(int k=0;k<xxx.get(i).get(j).size();k++)data_xx[i][j][k] =xxx.get(i).get(j).get(k);
			}
		}
	}

	//获取所有的训练样本
	private void set_combinations() {
		train_data_x = new double[data_xx.length*(data_xx.length-1)/2][][];
		train_data_y = new Integer[data_yy.length*(data_yy.length-1)/2][];
		int index=0;
		//训练样本
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
	
	
	private double[][] get_parameters() throws IOException {
		double[] l= {0.0,0.0,0.0,0.0,0.0,0.0,1.0};
		double[] u= {Math.pow(10, 3),Math.pow(10, 3),1.0,1.0,2.0,100,5.0};
		//基因片段是所有SVM的四个参
		double[][] result;
		DE de = new DE(train_data_x,train_data_y,data_xx,data_yy,tol,l,u,type,test_data_x,test_data_y,file);
		test_data_x = null;
		test_data_y = null;
		result = de.get_result();
		return result;
	}
	
	private void train() throws IOException {
		double[][] parameters = new double[train_data_x.length][];
		String kernel=null;
		parameters = get_parameters();
//		System.out.println(Arrays.deepToString(parameters));
		ovo= new DESVM_SMO[train_data_x.length];
		for(int i=0;i<train_data_x.length;i++) {
			if(parameters[i][4]==0)kernel="line";
			else if(parameters[i][4]==1)kernel="rbf";
			else if(parameters[i][4]==2)kernel="poly";
			else {System.out.println("error");System.exit(0);}
			ovo[i] = new DESVM_SMO(train_data_x[i],train_data_y[i],tol, parameters[i][0],parameters[i][1],parameters[i][2],parameters[i][3], kernel,parameters[i][5],(int)parameters[i][6]);}
	}
	
	//最高票数相同的时候在所有最高票数的类中随机选取
	//预测需要改成加权的投票
//	public Integer predict(double[] unkonwn) {
//		double value[] = new double[validation_data_y.length];
//		Vector<Integer> index_list = new Vector<Integer>();
//		double percent;
//		Integer[] label;
//		int p=0;
//		for(int i=0;i<ovo.length;i++) {
//			percent = ovo[i].get_probability(unkonwn);
//			label=ovo[i].get_label();
//			for(int j=0;j<validation_data_y.length;j++) {
//				if(label[0]==validation_data_y[j])value[j]+=1-percent;
//				if(label[1]==validation_data_y[j])value[j]+=percent;
//			}
//		}
//		for(int i=1;i<value.length;i++)if(value[i]>value[p])p=i;
////		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length<validation_data_x[p].length)p=i;
////		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length==validation_data_x[p].length)index_list.add(i);
////		p=new Random().nextInt(index_list.size());
////		p=index_list.get(p);
//		return validation_data_y[p];
//	}
	
	
	public Integer predict(double[] unkonwn) {
		int value[] = new int[data_yy.length];
		Vector<Integer> index_list = new Vector<Integer>();
		double percent;
		Integer[] label;
		Integer result;
		int p=0;
		for(int i=0;i<ovo.length;i++) {
//			percent = ovo[i].get_probability(unkonwn);
//			label=ovo[i].get_label();
//			if(percent>=0.5)result=label[1];
//			else result=label[0];
			
			result = ovo[i].predict(unkonwn);
			
			for(int j=0;j<data_yy.length;j++)if(result==data_yy[j])value[j]++;
		}
		for(int i=1;i<value.length;i++)if(value[i]>value[p])p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&data_xx[i].length<data_xx[p].length)p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&data_xx[i].length==data_xx[p].length)index_list.add(i);
		p=new Random().nextInt(index_list.size());
		p=index_list.get(p);
		return data_yy[p];
	}
}
