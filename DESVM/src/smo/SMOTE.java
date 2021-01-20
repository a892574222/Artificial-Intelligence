package smo;

import java.util.Random;

public class SMOTE {
	
	private double[][] data;
	private Integer[] label;
	//k近邻需要自己设大小
	private int k;
	private int N;
	private double[][] finally_data;
	private Integer[] finally_label;
	private double[][] added_data;
	
	//针对两分类的SMOTE
	public SMOTE(double data[][], Integer label[], int k, int N) {
		this.data = data;
		this.label = label;
		this.k = k;
		this.N = N;
		solve();
	}
	
	//staticsmote需要用到的构造方法
	public SMOTE(double data[][], Integer label, int k, int N) {
		Integer[] temp_label = new Integer[data.length];
		for(int i=0;i<temp_label.length;i++)temp_label[i] = label;
		this.data = data;
		this.label = temp_label;
		this.k = k;
		this.N = N;
		solve();
	}
	
	
	
	private void solve() {
		Integer minority=null;
		Integer majority=null;
		int minority_number=0;
		int majority_number=0;
		int now_k;
		int index = 0;
		Integer target_label;
		double[][] target_data;
		int data_length;
		double[][] temp_data;
		double[][][] k_neighbor_data;
		double distance;
		int neighbor_number=0;
		Random random = new Random();
		double[] temp;
		for(int i=0;i<label.length;i++) {
			if(minority==null) {
				minority=label[i];
			}else {
				if(majority==null&&label[i]!=minority) {
					majority=label[i];
				}
			}
			if(label[i]==minority)minority_number++;
			if(label[i]==majority)majority_number++;
		}
		if(majority==null) {
			target_label = minority;
			target_data = new double[minority_number][];
		}else {
			if(minority_number>=majority_number) {
				target_label = majority;
				target_data = new double[majority_number][];
			}else {
				target_label = minority;
				target_data = new double[minority_number][];
			}
		}
		if(target_data.length<k) {
			k = target_data.length;
		}
		for(int i=0;i<data.length;i++) {
			if(label[i]==target_label) {
				target_data[index] = data[i].clone();
				index++;
			}
		}
		if(N<100) {
			data_length = (int)N*target_data.length/100;
			temp_data = new double[data_length][];
			shuffle(target_data);
			for(int i=0;i<data_length;i++) {
				temp_data[i] = target_data[i];
			}
			target_data = temp_data;
			N=100;
		}
		N/=100;
		k_neighbor_data = new double[target_data.length][k][2];
		for(int i=0;i<target_data.length;i++) {
			for(int j=0;j<target_data.length;j++) {
				if(i==j)continue;
				distance = calculate(target_data[i],target_data[j]);
				if(neighbor_number<k) {
					k_neighbor_data[i][neighbor_number][0] = distance;
					k_neighbor_data[i][neighbor_number][1] = j;
					neighbor_number++;
				}else {
					if(neighbor_number==k) {
						for(int p=0;p<k-1;p++) {
							for(int q=k-1;q>p;q--) {
								if(k_neighbor_data[i][q][0]<k_neighbor_data[i][q-1][0]) {
									temp = k_neighbor_data[i][q-1];
									k_neighbor_data[i][q-1] = k_neighbor_data[i][q];
									k_neighbor_data[i][q] = temp;
								}
							}
						}
						neighbor_number++;
					}
					if(distance<k_neighbor_data[i][k-1][0]) {
						for(int p=0;p<k;p++) {
							if(distance<k_neighbor_data[i][p][0]) {
								for(int q=k-1;q>p;q--)k_neighbor_data[i][q] = k_neighbor_data[i][q-1];
								k_neighbor_data[i][p][0] = distance;
								k_neighbor_data[i][p][1] = j;
								break;
							}
						}
					}
				}
			}
		}
		finally_data = new double[target_data.length*N+data.length][];
		finally_label = new Integer[target_data.length*N+data.length];
		added_data = new double[target_data.length*N][];
		index=0;
		temp = new double[target_data[0].length];
		for(int i=0;i<data.length;i++) {
			finally_data[i] = data[i].clone();
			finally_label[i] = label[i];
			}
		for(int i=0;i<target_data.length;i++){
			for(int j=0;j<N;j++) {
				now_k = random.nextInt(k);
				for(int p=0;p<target_data[i].length;p++) {
					temp[p] = target_data[i][p]+random.nextDouble()*(target_data[(int) k_neighbor_data[i][now_k][1]][p]-target_data[i][p]);
				}
				added_data[index] = temp;
				finally_data[data.length+index] = temp;
				finally_label[data.length+index] = target_label;
				index++;
			}
		}
		
	}
	
	public double[][] get_data(){
		return finally_data;
	}
	
	public Integer[] get_label(){
		return finally_label;
	}
	
	public double[][] get_added_data(){
		return added_data;
	}
	
	//欧氏距离的平方
	private double calculate(double[] x,double[] y) {
		double result = 0;
		for(int i=0;i<x.length;i++) {
			result+=(x[i]-y[i])*(x[i]-y[i]);
		}
		return result;
	}
	
	private void swap(double[][] a, int i, int j) {
		double[] temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}
 
	private void shuffle(double[][] arr) {
		Random rand = new Random();
		int length = arr.length;
		for (int i = length; i > 0; i--) {
			int randInd = rand.nextInt(i);
			swap(arr, randInd, i - 1);
		}
	}
}
