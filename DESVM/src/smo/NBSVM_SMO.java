package smo;

public class NBSVM_SMO extends Based_SMO{
	public NBSVM_SMO(double[][]data, Integer[] label,double toler, double C0,double C1,double P0,double P1, String kernel, double sigma) {
		super(data,label,toler,C0,C1,P0,P1,kernel,sigma,0);
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
	}
	
	public double get_distance(double[] unkonwn) {
		double result=b;
		for(int i=0;i<data.length;i++) {
			result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
		}
		return result;
	}
}