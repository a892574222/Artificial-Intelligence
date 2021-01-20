package smo;



public class DESVM_SMO extends Based_SMO{
	
	public DESVM_SMO(double[][] data, Integer[] label, double KKTviolationsLevel, double C1, double C2, double w1,
			double w2, String kernel, double sigma, int d) {
		super(data, label, KKTviolationsLevel, C1, C2, w1, w2, kernel, sigma, d);
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
//		this.parameters= calculate_parameters();
	}
	 
	public double get_probability(double[] unkonwn) {
		double fApB=b;
		double p;
		for(int i=0;i<data.length;i++) {
			fApB +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
		}
		p = 1/(1+Math.pow(Math.E, -fApB));
		return p;
	}
 }