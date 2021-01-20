package smo;

import java.io.IOException;
import java.util.Arrays;

import com.mathworks.toolbox.javabuilder.MWException;

class dataset{
	private double[][] data;
	private Integer[] label;
	public dataset(double[][] data,Integer[] label) {
		this.data = data;
		this.label = label;
	}
	
	public double[][] get_data() {
		return data;
	}
	
	public Integer[] get_label() {
		return label;
	}
	
}

public class Artifical_Data {
	
	public static dataset Right_Circular_Cone_data(int[] number,double[][] para) throws MWException{
		Integer[] label;
		double[][] data;
		int sum=0;
		double[][] temp;
		for(int i=0;i<number.length;i++)sum+=number[i];
		data = new double[sum][];
		label = new Integer[sum];
		sum=0;
		for(int i=0;i<number.length;i++) {
			temp = Distribution_Function.Right_circular_cone(para[i][0], para[i][1], para[i][2], number[i]);
			for(int j=0;j<temp.length;j++) {
				data[sum] = temp[j];
				label[sum] = i;
				sum++;
			}
		}
		dataset result = new dataset(data,label);
		return result;
	}
	
	public static void main(String[] args) throws MWException, IOException {
		int[] number = {100,200,1000};
		double[][] para = {
				{0.1,0,75},
				{0.05,0.05,50},
				{0,0,25},
				
		};
		dataset da = Right_Circular_Cone_data(number,para);
		double[][] data_x = da.get_data();
		Integer[] data_y = da.get_label();
		Integer[] attributes = {0,0};
		Test.write(data_x);
		//ÕÛÊý
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
		
		
		data_x = Test.normalization(data_x,attributes);
		
		for(int i=0;i<times;i++) {
			Data_Divied data_divied = new Data_Divied(k,data_x,data_y);
			training_data_x[i] = data_divied.get_training_data_x();
			training_data_y[i] = data_divied.get_training_data_y();
			test_data_x[i] = data_divied.get_test_data_x();
			test_data_y[i] = data_divied.get_test_data_y();
			}
		DESVM mysmo_max = new DESVM(training_data_x, training_data_y,0.0,test_data_x,test_data_y,false,"Right_Circular_Cone_max");
		System.out.println("DESVM MAX:");
		mysmo_max.get_all_result(1);
		mysmo_max=null;
		
	}
}
