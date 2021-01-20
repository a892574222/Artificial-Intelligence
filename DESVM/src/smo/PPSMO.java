package smo;

import java.util.Arrays;

public class PPSMO extends Based_SMO{
	private double percent;
	private double delta;
	
	public PPSMO(double[][]data, Integer[] label, double C,double toler, String kernel, double sigma,double percent,double delta) {
		super(data,label,toler,C,C,1.0,1.0,kernel,sigma,0);
		this.percent = percent;
		this.delta = delta;
		turn_label();
		this.get_probability();
		createKernelCache();
		SetBoxAndP();
		start();
	}
	
	private void get_probability() {
		double sum=0;
		double positive_sum,negative_sum;
		double max_distance=0;
		double[][] distance = new double[data.length][data.length];
		double[] class_probability = new double[2];
		double[] sub = new double[data[0].length];
		double now_distance;
		double positive_class_conditional_probability;
		double negative_class_conditional_probability;
		double[]probability = new double[data.length];
		
		for(int i=0;i<label.length;i++) {
			for(int j=i;j<label.length;j++) {
				for(int p=0;p<data[i].length;p++) {
					sub[p] = data[i][p]-data[j][p];
				}
				now_distance = Math.sqrt(calculate(sub));
				distance[i][j] = now_distance;
				distance[j][i] = now_distance;
				if(now_distance>max_distance)max_distance = now_distance;
			}
			if(label[i]==1)sum++;
		}
//		System.out.println(max_distance);
		max_distance*=percent;
//		System.out.println(max_distance);
		class_probability[0] = sum/label.length;
		class_probability[1] = (label.length-sum)/label.length;
		for(int i=0;i<label.length;i++) {
			positive_sum=0;
			negative_sum=0;
			for(int j=0;j<label.length;j++) {
				if(distance[i][j]<=max_distance) {
					if(label[j]==1)positive_sum++;
					else negative_sum++;
				}
			}
			positive_class_conditional_probability = positive_sum/sum;
			negative_class_conditional_probability = negative_sum/(label.length-sum);
			probability[i] = positive_class_conditional_probability*class_probability[0]/(positive_class_conditional_probability*class_probability[0]+class_probability[1]*negative_class_conditional_probability);
			if(probability[i]>0.5&&label[i]==0)probability[i]=0.5-delta;
			if(probability[i]<0.5&&label[i]==1)probability[i]=0.5+delta;
		}
		for(int i=0;i<probability.length;i++) {
			label[i] = 2*probability[i]-1;
		}
//		System.out.println(Arrays.toString(label));
//		System.out.println(Arrays.toString(probability));
//		System.out.println("----------");
	}
	
	//设置boxconstraint和P以及Gi
	protected void SetBoxAndP() {
		for(int i=0;i<label.length;i++) {
			if(label[i]>=0) {positive_number++;upMask[i]=1;}
			else downMask[i]=1;
		}
		boxconstraint[0] = C1;
		boxconstraint[1] = C2;
		for(int i=0;i<label.length;i++) {
			Gi[i] = label[i]*label[i];
		}
//		System.out.println("Gi:"+Arrays.toString(Gi));
	}

}
