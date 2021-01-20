package smo;

import java.util.Vector;

public class OVA {
	private double[][] data;
	private Integer[] label;
	private Integer[] label_kinds;
	private NBSVM_SMO[] smo;
	private double C;
	private double sigma;
	private double weight;
	private double tol;
	private String kernel;
	
	public OVA(double[][] data, Integer[] label,double tol,double C,String kernel,double sigma,double weight) {
		this.data = data;
		this.label = label;
		this.C = C;
		this.sigma = sigma;
		this.weight = weight;
		this.tol = tol;
		this.kernel = kernel;
		this.solve();
	}
	
	public Integer[] get_label_kinds() {
		return label_kinds;
	}
	
	private void solve() {
		Vector<Integer> list = new Vector<Integer>();
		double[][][] divied_data;
		Integer[][] divied_label;
		Integer positive = null;
		Integer negative = null;
		double positive_sum=0;
		double w1,w2,P,C1,C2;
		
		for(int i=0;i<label.length;i++) {
			if(!list.contains(label[i])) {
				list.add(label[i]);
			}
		}
		label_kinds = list.toArray(new Integer[list.size()]);
		divied_data = new double[label_kinds.length][][];
		divied_label = new Integer[label_kinds.length][];
		for(int i=0;i<divied_data.length;i++) {
			divied_data[i] = new double[data.length][];
			divied_label[i] = new Integer[data.length];
			for(int j=0;j<data.length;j++) {
				divied_data[i][j] = data[j].clone();
				if(label[j]==label_kinds[i]) 
					divied_label[i][j] = label_kinds[i];
				else
					divied_label[i][j] = -1;
			}
		}
		smo = new NBSVM_SMO[label_kinds.length];
		
		for(int i=0;i<smo.length;i++) {
			positive_sum=0;
			for(int j=0;j<divied_data[i].length;j++) {
				if(negative==null)negative=divied_label[i][j];
				if(divied_label[i][j]!=negative) {positive=divied_label[i][j];break;}
			}
			for(int j=0;j<divied_label[i].length;j++)if(positive==divied_label[i][j])positive_sum++;
			if(positive_sum>divied_data[i].length-positive_sum)P = positive_sum/divied_data[i].length;
			else P = (divied_data[i].length-positive_sum)/divied_data[i].length;
			C1 = C/(1-P);
			C2 = C/P;
			w1 = weight*P/(1.0+weight*P);
			w2 = 1/(1.0+weight*P);
			if(P<0) {
				System.out.println(P);
				System.out.println(positive_sum);
			}
//			System.out.println(C);
//			System.out.println(w1);
//			System.out.println(w2);
//			System.out.println(sigma);
//			System.out.println("----------");
			smo[i] = new NBSVM_SMO(divied_data[i],divied_label[i],tol,C1,C2,w1,w2,kernel,sigma);
			}
	}
	
	public Integer predict(double[] data) {
		double[] distance = new double[smo.length];
		int max_index = 0;
		for(int i=0;i<smo.length;i++) {
			distance[i] = smo[i].get_distance(data);
//			System.out.println(distance[i]);
			if(distance[i]>distance[max_index])max_index = i;
		}
//		System.out.println("---------");
//		if(distance[max_index]<0)System.out.println("训练模型存在问题");
		return label_kinds[max_index];
	}
	
//	public Integer predict(double[] data) {
//		Vector<Integer> index_list = new Vector<Integer>();
//		int p;
//		System.out.println(label[0]);
//		for(int i=1;i<smo.length;i++) {
//			System.out.println(smo[i].predict(this.data[0]));
//			System.out.println(smo[i].get_distance(this.data[0]));
//			if(smo[i].predict(data)==smo[i].get_label()[1])index_list.add(i);
//		}
//		if(index_list.size()==0) {
//			p=new Random().nextInt(label_kinds.length);
//			return label_kinds[p];
//		}
//		p=new Random().nextInt(index_list.size());
//		p=index_list.get(p);
//		return label_kinds[p];
//	}
}
