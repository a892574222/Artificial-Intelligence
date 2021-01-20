package smo;

import java.util.Vector;

public class WK_SMOTE {
	private double[][] data;
	private Integer[] label;
	private Integer[] label_kinds;
	private WK_SMO[] smo;
	private double sigma,C;
	private String kernel;
	private int unequal;
	private double tol;
	
	public WK_SMOTE(double[][] data, Integer[] label,double C,double tol,String kernel,double sigma, int unequal) {
		this.data = data;
		this.label = label;
		this.C = C;
		this.tol = tol;
		this.kernel = kernel;
		this.sigma = sigma;
		this.unequal = unequal;
		this.solve();
	}
	
	public Integer[] get_label_kinds() {
		return label_kinds;
	}
	
	private void solve() {
		Vector<Integer> list = new Vector<Integer>();
		double[][][] divied_data;
		Integer[][] divied_label;
		int sum=0;
		boolean flag=true;
		int[] label_sum;
		int[] added;
		int temp;
		int index;
		boolean unequal;
		
		for(int i=0;i<label.length;i++) {
			if(!list.contains(label[i])) {
				list.add(label[i]);
			}
		}
		label_kinds = list.toArray(new Integer[list.size()]);
		label_sum = new int[label_kinds.length];
		for(int i=0;i<label_kinds.length;i++) {
			for(int j=0;j<data.length;j++) {
				if(label[j]==label_kinds[i]) label_sum[i]++;
			}
		}
		for(int i=0;i<label_kinds.length-1;i++) {
			for(int j=label_kinds.length-1;j>i;j--) {
				if(label_sum[j]<label_sum[j-1]) {
					temp=label_sum[j-1];
					label_sum[j-1]=label_sum[j];
					label_sum[j]=temp;
					temp = label_kinds[j-1];
					label_kinds[j-1] = label_kinds[j];
					label_kinds[j] = temp;
					}
			}
		}
		divied_data = new double[label_kinds.length-1][][];
		divied_label = new Integer[label_kinds.length-1][];
		added = new int[label_kinds.length-1];
		for(int i=0;i<divied_data.length;i++) {
			divied_data[i] = new double[data.length-sum][];
			divied_label[i] = new Integer[data.length-sum];
			added[i] = 0;
			index=0;
			for(int j=0;j<data.length;j++) {
				if(label[j]==label_kinds[i]) {
					sum++;
					added[i]++;
					divied_data[i][index] = data[j].clone();
					divied_label[i][index] = label_kinds[i];
					index++;
				}else {
					for(int p=0;p<i;p++)if(label_kinds[p]==label[j])flag=false;
					if(flag) {
						divied_data[i][index] = data[j].clone();
						if(i<divied_data.length-1)divied_label[i][index] = -1;
						else divied_label[i][index] = label[j];
						index++;
					}
					flag=true;
				}
			}
			added[i] = divied_data[i].length-added[i];
		}
//		System.out.println(Arrays.toString(added));
		if(this.unequal == 0)unequal = false;
		else unequal = true;
		smo = new WK_SMO[label_kinds.length-1];
		for(int i=0;i<smo.length;i++) {
			smo[i] = new WK_SMO(divied_data[i],divied_label[i],C,added[i],5,tol,kernel,sigma,unequal);
		}
	}
	
	public Integer predict(double[] data) {
		Integer positive,result=null;
		for(int i=0;i<smo.length;i++) {
			positive = smo[i].get_label()[1];
			if(i<smo.length-1&&smo[i].predict(data)==positive) {result = positive;break;}
			else {
				if(i==smo.length-1)result = smo[i].predict(data);
			}
		}
//		System.out.println(result);
		return result;
	}
}
