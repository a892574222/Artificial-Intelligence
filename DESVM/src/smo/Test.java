package smo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;


public class Test {
	
	//归一化，包含数值和非数值形
	public static double[][] normalization(double[][] data_x, Integer[] attributes) {
		Vector<Double> x = new Vector<Double>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		
		Vector<Double>feature;
		for(int i=0;i<data_x.length;i++) {
			x = new Vector<Double>();
			xx.add(x);
		}
		for(int i=0;i<attributes.length;i++) {
			feature = new Vector<Double>();
			if(attributes[i]==1) {
				for(int j=0;j<data_x.length;j++){
					if(!feature.contains(data_x[j][i]))feature.add(data_x[j][i]);
				}
				for(int j=0;j<data_x.length;j++){
					for(int p=0;p<feature.size();p++) {
						if(data_x[j][i]==feature.get(p)) xx.get(j).add(1.0);
						else xx.get(j).add(0.0);
					}
				}
			}else {
				for(int j=0;j<data_x.length;j++)xx.get(j).add(data_x[j][i]);
			}
		}
		data_x = new double[xx.size()][];
		for(int i=0;i<data_x.length;i++) {
			data_x[i] = new double[xx.get(i).size()];
			for(int j=0;j<xx.get(i).size();j++) {
				data_x[i][j] = xx.get(i).get(j);
			}
		}
		xx = new Vector<Vector<Double>>();
		x = new Vector<Double>();
		
		
		//[0,1]归一化
		double max;
		double min;
		
		for(int i=0;i<data_x[0].length;i++) {
			max=data_x[0][i];
			min=data_x[0][i];
			for(int j=1;j<data_x.length;j++){
				if(data_x[j][i]>max)max=data_x[j][i];
				if(data_x[j][i]<min)min=data_x[j][i];
			}
			for(int j=0;j<data_x.length;j++){
				if(max-min==0)data_x[j][i]=0;
				else data_x[j][i]=(data_x[j][i]-min)/(max-min);
			}
		}
		return data_x;
	}
	
	public static void write(double[][] arr) throws IOException {
        File file = new File("result.csv");  //存放数组数据的文件
        FileWriter out = new FileWriter(file);  //文件写入流
        int n=arr.length;
        int m=arr[0].length;
        //将数组中的数据写入到文件中。每行各数据之间TAB间隔
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                out.write(arr[i][j]+",");
            }
            out.write("\r\n");
        }
        out.close();
	}

	public static void main(String[] args) throws IOException {
		String data_X = "src\\data\\right_circular_x.txt";
		String data_Y = "src\\data\\right_circular_y.txt";
		String data_attributes = "src\\data\\right_circular_a.txt";
		double[][] data_x = Data_Reader.getdata_x(data_X);
		Integer[] data_y = Data_Reader.getdata_y(data_Y);
		Integer[] attributes = Data_Reader.getdata_attributes(data_attributes);
		
		System.out.println(data_X);
		//折数
		int k=5;
		int times = 5;
		
		double[][][][] training_data_x;
		Integer[][][] training_data_y;
		double[][][][][] test_data_x;
		Integer[][][] test_data_y;
		
		training_data_x = new double[times][][][];
		training_data_y = new Integer[times][][];
		test_data_x = new double[times][][][][];
		test_data_y = new Integer[times][][];
	
		data_x = normalization(data_x,attributes);
//		输出正则化的数据
//		write(data_x);
		
		
		
		for(int i=0;i<times;i++) {
			Data_Divied data_divied = new Data_Divied(k,data_x,data_y);
			training_data_x[i] = data_divied.get_training_data_x();
			training_data_y[i] = data_divied.get_training_data_y();
			test_data_x[i] = data_divied.get_test_data_x();
			test_data_y[i] = data_divied.get_test_data_y();
			}
		
		
//		SVM svm = new SVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,false);
//		System.out.println("SVM:");
//		svm.get_all_result(1);
//		svm=null;
//		SVM static_svm = new SVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,true);
//		System.out.println("Static SVM:");
//		static_svm.get_all_result(1);
//		static_svm=null;
//		Cost_SVM cost_svm = new Cost_SVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,false);
//		System.out.println("Cost SVM:");
//		cost_svm.get_all_result(1);
//		cost_svm=null;
//		Cost_SVM SDC = new Cost_SVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,true);
//		System.out.println("SDC:");
//		SDC.get_all_result(1);
//		SDC=null;
//		WK_SVM wk_svm = new WK_SVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y);
//		System.out.println("WK_SVM:");
//		wk_svm.get_all_result(1);
//		wk_svm=null;
//		PPSVM ppsvm = new PPSVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y);
//		System.out.println("PPSVM:");
//		ppsvm.get_all_result(1);
//		ppsvm=null;
//		NBSVM nbsvm = new NBSVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y);
//		System.out.println("NBSVM:");
//		nbsvm.get_all_result(1);
//		nbsvm=null;
//		DESVM mysmo_max = new DESVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,true,data_X.substring(9, data_X.length()-5)+"max");
//		System.out.println("DESVM MAX:");
//		mysmo_max.get_all_result(1);
//		mysmo_max=null;
		DESVM mysmo_ave = new DESVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,true,data_X.substring(9, data_X.length()-5)+"ave");
		System.out.println("DESVM AVE:");
		mysmo_ave.get_all_result(1);
	}
}

class Data_Divied{
	double[][][] training_data_x;
	Integer[][] training_data_y;
	double[][][][] test_data_x;
	Integer[][] test_data_y;;
	
	public Data_Divied(int k,double[][]data_x,Integer[]data_y) {
		this.training_data_x = new double[k][][];
		this.training_data_y = new Integer[k][];
		this.test_data_x = new double[k][][][];
		this.test_data_y = new Integer[k][];;
		this.divied(k, data_x, data_y);
	}
	
	//拆分数据
	public void divied(int k,double[][]data_x,Integer[]data_y) {
		//交叉验证组数
		Integer[] label_kinds;
		Vector<Integer> y= new Vector<Integer>();
		Vector<Vector<Vector<Double>>> xxx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Vector<Vector<Double>>>> xxx_divied = new Vector<Vector<Vector<Vector<Double>>>>();
		Vector<Vector<Integer>> y_divied = new Vector<Vector<Integer>>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		Vector<Double> x = new Vector<Double>();
		int index;
		double[][] fold_training_data_x;
		Integer[] fold_training_data_y;
		double[][][] fold_test_data_x;
		Integer[] fold_test_data_y;
		Vector<Integer> list = new Vector<Integer>();
		Random randoms = new Random();
		randoms.setSeed(1);
		int rest;
		
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
		//生成k个m类的vector向量空间
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
			fold_training_data_x = new double [index][];
			fold_training_data_y = new Integer [index];
			index=0;
			for(int i=0;i<j;i++) {
				for(int m=0;m<xxx_divied.get(i).size();m++) {
					for(int n=0;n<xxx_divied.get(i).get(m).size();n++) {
						fold_training_data_x[index] = new double[xxx_divied.get(i).get(m).get(n).size()];
						for(int z=0;z<xxx_divied.get(i).get(m).get(n).size();z++) {
							fold_training_data_x[index][z]= xxx_divied.get(i).get(m).get(n).get(z);
						}
						fold_training_data_y[index]=y_divied.get(i).get(m);
						index++;
					}
				}
			}
			for(int i=j+1;i<k;i++) {
				for(int m=0;m<xxx_divied.get(i).size();m++) {
					for(int n=0;n<xxx_divied.get(i).get(m).size();n++) {
						fold_training_data_x[index] = new double[xxx_divied.get(i).get(m).get(n).size()];
						for(int z=0;z<xxx_divied.get(i).get(m).get(n).size();z++) {
							fold_training_data_x[index][z]= xxx_divied.get(i).get(m).get(n).get(z);
						}
						fold_training_data_y[index]=y_divied.get(i).get(m);
						index++;
					}
				}
			}
			
			fold_test_data_x = new double [xxx_divied.get(j).size()][][];
			fold_test_data_y=new Integer[y_divied.get(j).size()];
			for(int m=0;m<xxx_divied.get(j).size();m++) {
				fold_test_data_x[m] = new double[xxx_divied.get(j).get(m).size()][];
				for(int n=0;n<xxx_divied.get(j).get(m).size();n++) {
					fold_test_data_x[m][n] = new double[xxx_divied.get(j).get(m).get(n).size()];
					for(int z=0;z<xxx_divied.get(j).get(m).get(n).size();z++) {
						fold_test_data_x[m][n][z] = xxx_divied.get(j).get(m).get(n).get(z);
					}
				}
				fold_test_data_y[m] = y_divied.get(j).get(m);
			}
			training_data_x[j] = fold_training_data_x;
			training_data_y[j] = fold_training_data_y;
			test_data_x[j] = fold_test_data_x;
			test_data_y[j] = fold_test_data_y;
		}
//		for()
	}
	
	public double[][][] get_training_data_x(){
		return training_data_x;
	}
	
	public Integer[][] get_training_data_y(){
		return training_data_y;
	}
	
	public double[][][][] get_test_data_x(){
		return test_data_x;
	}
	
	public Integer[][] get_test_data_y(){
		return test_data_y;
	}
}