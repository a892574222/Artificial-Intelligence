package smo;

import java.io.IOException;
import java.util.Arrays;
import com.mathworks.toolbox.javabuilder.MWException;

public class Test_ {
	public static void main(String[] args) throws IOException, MWException {
		int[] number = {20,40,200};
		double[][] para = {
				{0.5,0,25},
				{0.25,0.25,25},
				{0,0,25},
				
		};
		dataset da = Artifical_Data.Right_Circular_Cone_data(number,para);
		double[][] data_x = da.get_data();
		Integer[] label = da.get_label();
		double[][] a = new double[label.length][1];
		for(int i=0;i<a.length;i++) {
			a[i][0] = label[i];
		}
		Test.write(a);
	}
}
