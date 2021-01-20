package smo;


public class Cost_SMO extends Based_SMO{
	private double C;
	private double weight;
	private boolean is_smote;
	public Cost_SMO(double[][]data, Integer[] label, double C, double toler, String kernel, double sigma,boolean smote,double weight) {
		super(data,label,toler,C,C,1.0,1.0,kernel,sigma,0);
		this.C = C;
		this.weight = weight;
		this.is_smote = smote;
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
	}
	
	protected void turn_label() {
		
		
		Integer p;
		double positive_sum=0;
		SMOTE smote;
		int N;
		Integer[] temp_label = new Integer[label.length];
		for(int i=0;i<label.length;i++)temp_label[i] = (int)label[i];

		for(int i=0;i<label.length;i++) {
			if(negative==null)negative=(int)label[i];
			if(label[i]!=negative) {positive=(int)label[i];break;}
		}
		for(int i=0;i<label.length;i++)if(positive==label[i])positive_sum++;
		if(positive_sum>data.length-positive_sum) {
		p=positive;positive=negative;negative=p;
		if(is_smote){
			N=(int) (positive_sum*100/(data.length-positive_sum));
			smote = new SMOTE(data,temp_label,5,N);
			data = smote.get_data();
			alphas = new double[data.length];
			label = new double [data.length];
			Gi = new double[data.length];
			upMask = new int[data.length];
			downMask = new int[data.length];
			temp_label = smote.get_label();
			for(int i=0;i<label.length;i++)label[i] = temp_label[i];
		}
		C1=weight*C*data.length/(data.length-positive_sum);
		C2=weight*C*data.length/positive_sum;
		}
		else {
		if(is_smote){
			N=(int) (((data.length-positive_sum)*100)/positive_sum);
			smote = new SMOTE(data,temp_label,5,N);
			data = smote.get_data();
			temp_label = smote.get_label();
			alphas = new double[data.length];
			label = new double [data.length];
			Gi = new double[data.length];
			upMask = new int[data.length];
			downMask = new int[data.length];
			for(int i=0;i<label.length;i++)label[i] = temp_label[i];
		}
		C1=weight*C*data.length/positive_sum;
		C2=weight*C*data.length/(data.length-positive_sum);
		}
		for(int i=0;i<label.length;i++) {
			if(label[i]==negative)label[i]=-1;
			else label[i]=1;
		}
	}
}