package smo;

import java.util.Arrays;

public class SMO extends Based_SMO{
	
	public SMO(double[][]data, Integer[] label, double C, double toler, String kernel, double sigma) {
		super(data,label,toler,C,C,1.0,1.0,kernel,sigma,0);
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
	}
}