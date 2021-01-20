package smo;

import java.util.Random;
import java.util.Vector;

public class OVO {
	private Integer[] label_kinds;
	private String kernel;
	//按类别分类的未combine的train_data
	private double[][][] data_xx;
	private Integer[] data_yy;
	//训练样本
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	
	private double[][] data;
	private Integer[] label;
	private double C;
	private double sigma;
	private double tol;
	private String SVM_type;
	private boolean static_smote;
	private double weight;
	private double percent;
	private double delta;
	
	private Based_SMO[] ovo;
	
	public OVO(double[][]data_x,Integer[] data_y,String SVM_type,double C,double tol,String kernel,double sigma,boolean static_smote,double weight,double percent,double delta) {
		this.data = data_x;
		this.label = data_y;
		this.SVM_type = SVM_type;
		this.C = C;
		this.sigma = sigma;
		this.static_smote = static_smote;
		this.change_data(static_smote);
		this.weight = weight;
		this.percent = percent;
		this.delta = delta;
		label_kinds=set_num(label);
		data_divied(data,label);
		this.kernel = kernel;
		set_combinations();
		train();
	}
	
	public Integer[] get_label_kinds() {
		return label_kinds;
	}
	
	private void change_data(boolean static_smote) {
		if(static_smote&&SVM_type.equals("SVM")) {
			StaticSMOTE staticsmote = new StaticSMOTE(data,label);
			data = staticsmote.get_data();
			label = staticsmote.get_label();
		}
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
	
	private void train() {
		switch(SVM_type) {
		case "SVM":
			ovo= new SMO[train_data_x.length];
			for(int i=0;i<train_data_x.length;i++)ovo[i] = new SMO(train_data_x[i],train_data_y[i],C,tol,kernel,sigma);
			break;
		case "Cost_SVM":
			ovo= new Cost_SMO[train_data_x.length];
			for(int i=0;i<train_data_x.length;i++)ovo[i] = new Cost_SMO(train_data_x[i],train_data_y[i],C,tol,kernel,sigma,static_smote,weight);
			break;
		case "PPSVM":
			ovo= new PPSMO[train_data_x.length];
			for(int i=0;i<train_data_x.length;i++)ovo[i] = new PPSMO(train_data_x[i],train_data_y[i],C,tol,kernel,sigma,percent,delta);
			break;
		}
	}
	
	public boolean is_train_data(double[] unknown) {
		for(int i=0;i<data.length;i++) {
			if(is_equal(data[i],unknown)) {
//				System.out.println(i);
				return true;
			}
		}
		return false;
	}
	
	public int get_data_length() {
		return data.length;
	}
	
	public double[][] get_data() {
		return data;
	}
	
	public static boolean is_equal(double[] a,double[] b) {
		for(int i=0;i<a.length;i++) {
			if(a[i]!=b[i])return false;
		}
		return true;
	}
	
	public Integer predict(double[] unkonwn) {
		int value[] = new int[data_yy.length];
		Vector<Integer> index_list = new Vector<Integer>();
		Integer result;
		int p=0;
		for(int i=0;i<ovo.length;i++) {
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
