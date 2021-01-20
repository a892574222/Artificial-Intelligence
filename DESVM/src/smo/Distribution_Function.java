package smo;

import java.util.Arrays;
import java.util.Random;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import Right_Circular_Cone_Matlab.Right_Circular;

public class Distribution_Function {
	
	//这里我们只生成二维数据
	public static double[][] Right_circular_cone(double mean1, double mean2, double gradient,int number) throws MWException{
		double[][] result = new double[number][2];
		
		Right_Circular right_circular = new Right_Circular();
		Object[] matrix = right_circular.Right_Circular_Cone(1, mean1,mean2,gradient,number);
		MWNumericArray data = (MWNumericArray) matrix[0];
		double[][] x_y = (double[][]) data.toDoubleArray();	
		for(int i=0;i<number;i++) {
			result[i][0] = x_y[0][i];
			result[i][1] = x_y[1][i];
		}
		return result;
	}
	
	//
	public static double[][] Gaussian(double mean1,double mean2,double[][] cov_matrix,int number){
		double[] x1 = new double[number];
		double[] x2 = new double[number];
		double R,theta;
		Random random = new Random();
		double[][] cholesky_matrix = new double[2][2];
		double[][] result = new double[number][2];
		
		
		if(cov_matrix.length!=2||cov_matrix[0].length!=2) return null;
		cholesky_matrix[0][0] = Math.sqrt(cov_matrix[0][0]);
		cholesky_matrix[0][1] = 0;
		cholesky_matrix[1][0] = cov_matrix[1][0]/Math.sqrt(cov_matrix[0][0]);
		cholesky_matrix[1][1] = Math.sqrt(cov_matrix[1][1]-cov_matrix[0][1]*cov_matrix[0][1]/cov_matrix[0][0]);
		System.out.println(Arrays.deepToString(cholesky_matrix));
		for(int i=0;i<number;i++) {
			R = Math.sqrt(-2*Math.log(random.nextDouble()));
			theta = 2*Math.PI*random.nextDouble();
			x1[i] = R*Math.cos(theta);
			x2[i] = R*Math.sin(theta);
			x2[i] = mean2+cholesky_matrix[1][0]*x1[i]+cholesky_matrix[1][1]*x2[i];
			x1[i] = mean1+cholesky_matrix[0][0]*x1[i];
		}
		for(int i=0;i<number;i++) {
			result[i][0] = x1[i];
			result[i][1] = x2[i];
		}
		return result;
	}
	

}
