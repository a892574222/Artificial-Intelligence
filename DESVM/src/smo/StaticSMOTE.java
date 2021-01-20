package smo;

import java.util.Vector;

public class StaticSMOTE {
	private double[][] data;
	private Integer[] label;
	private double[][] finally_data;
	private Integer[] finally_label;
	public StaticSMOTE(double data[][], Integer label[]) {
		this.data = data;
		this.label = label;
		solve();
	}
	
	private void solve() {
		Integer[] label_kinds;
		Vector<Integer> list = new Vector<Integer>();
		Vector<Vector<Vector<Double>>> xxx = new Vector<Vector<Vector<Double>>>();
		Vector<Vector<Double>> xx = new Vector<Vector<Double>>();
		Vector<Double> x = new Vector<Double>();
		Vector<Integer> y= new Vector<Integer>();
		double[][][] divied_data;
		Integer[] added_label;
		double[][][] added_data;
		int[] data_number;
		int[] finally_data_number;
		int sum_data = 0;
		int min_index;
		int index=0;
		SMOTE smote;
		
		for(int i=0;i<label.length;i++) {
			if(!list.contains(label[i])) {
				list.add(label[i]);
			}
		}
		label_kinds = list.toArray(new Integer[list.size()]);
		divied_data = new double[label_kinds.length][][];
		
		for(int i=0;i<label_kinds.length;i++) {
			for(int z=0;z<data.length;z++) {
				if(label[z]==label_kinds[i]) {
					for(int p=0;p<data[z].length;p++)x.add(data[z][p]);
					xx.add(x);
					x = new Vector<Double>();
				}
			}
			y.add(label_kinds[i]);
			xxx.add(xx);
			xx = new Vector<Vector<Double>>();
		}
		
		for(int i=0;i<label_kinds.length;i++) {
			divied_data[i] = new double[xxx.get(i).size()][];
			for(int j=0;j<xxx.get(i).size();j++) {
				divied_data[i][j] = new double[xxx.get(i).get(j).size()];
				for(int p=0;p<xxx.get(i).get(j).size();p++) {
					divied_data[i][j][p] = xxx.get(i).get(j).get(p);
				}
			}
		}
		data_number = new int[label_kinds.length];
		added_data = new double[label_kinds.length][][];
		added_label = new Integer[label_kinds.length];
		for(int i=0;i<label_kinds.length;i++)data_number[i] = divied_data[i].length;
		finally_data_number = data_number.clone();
//		System.out.println(Arrays.toString(finally_data_number));
		for(int i=0;i<label_kinds.length;i++) {
			min_index = get_min_index(finally_data_number);
			smote = new SMOTE(divied_data[min_index],label_kinds[min_index],5,100);
			added_data[i] = smote.get_added_data();
			added_label[i] = smote.get_label()[0];
			finally_data_number[min_index] += data_number[min_index];
//			System.out.println(Arrays.toString(finally_data_number));
		}
		for(int i=0;i<finally_data_number.length;i++) {
			sum_data+=finally_data_number[i];
		}
		finally_data = new double[sum_data][];
		finally_label = new Integer[sum_data];
		for(int i=0;i<data.length;i++) {
			finally_data[i] = data[i];
			finally_label[i] = label[i];
		}
		for(int i=0;i<added_data.length;i++) {
			for(int j=0;j<added_data[i].length;j++) {
				finally_data[data.length+index] = added_data[i][j];
				finally_label[data.length+index] = added_label[i];
				index++;
			}
		}
	}
	
	private int get_min_index(int[] data_number) {
		int min_index = 0;
		for(int i=1;i<data_number.length;i++) {
			if(data_number[i]<data_number[min_index])min_index = i;
		}
		return min_index;
	}
	
	public double[][] get_data(){
		return finally_data;
	}
	
	public Integer[] get_label(){
		return finally_label;
	}
}
