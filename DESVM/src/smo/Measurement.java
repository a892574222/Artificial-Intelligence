package smo;

public class Measurement {
	
	public static double get_ave_recall(double[][] confusion_matrix) {
		double ave_recall = 0;
		double line;
		double sum = 0;
		for(int i=0;i<confusion_matrix.length;i++) {
			line=0;
			for(int j=0;j<confusion_matrix[i].length;j++) {
				line+=confusion_matrix[i][j];
			}
			if(line!=0)ave_recall+=confusion_matrix[i][i]/line;
			else sum++;
		}
		ave_recall /= (confusion_matrix.length-sum);
		
		return ave_recall;
	}
	
	public static double get_g_mean(double[][] confusion_matrix, Integer[] label_kinds) {
		double result =1;
		double row;
		double label_sum = label_kinds.length;
		
		for(int label=0;label<label_kinds.length;label++) {
			row=0;
			for(int i=0;i<label_kinds.length;i++)row+=confusion_matrix[label][i];
			if(row==0) {label_sum--;continue;}
			result *= confusion_matrix[label][label]/row;
		}
		result = Math.pow(result, 1/label_sum);
		return result;
	}
	
	public static double get_micro_average(double[][] confusion_matrix, Integer[] label_kinds, double beta) {
		double result;
		double sum=0;
		double row=0;
		double line=0;
		double rec_u;
		double prec_u;
		double beta_Square = beta*beta;
		
		for(int label=0;label<label_kinds.length;label++) {
			sum+=confusion_matrix[label][label];
			for(int i=0;i<label_kinds.length;i++) {
				row+=confusion_matrix[label][i];
				line+=confusion_matrix[i][label];
			}
		}
		rec_u = sum/row;
		prec_u = sum/line;
		result = (1+beta_Square)*rec_u*prec_u/(beta_Square*prec_u+rec_u);
		return result;
	}
	
	public static double get_average_accuracy(double[][] confusion_matrix, Integer[] label_kinds) {
		double result = 0;
		double sum = 0;
		double row;
		double line;
		for(int label=0;label<label_kinds.length;label++) {
			sum+=confusion_matrix[label][label];
		}
		for(int label=0;label<label_kinds.length;label++) {
			row=0;
			line=0;
			for(int i=1;i<label_kinds.length;i++) {
				if(i==label)continue;
				row += confusion_matrix[label][i];
				line += confusion_matrix[i][label];
			}
			result+=sum/(sum+row+line);
		}
		result/=label_kinds.length;
		return result;
	}
	
	public static double[] get_macro_average_and_F1_measure(double[][] confusion_matrix, Integer[] label_kinds, double beta) {
		double[] result = new double[2];
		double[] recall = new double[confusion_matrix.length];
		double[] precision = new double[confusion_matrix.length];
		double row;
		double line;
		double rec_m = 0;
		double Prec_m = 0;
		double beta_Square = beta*beta;
		double label_sum = label_kinds.length;
		
		for(int label=0;label<label_kinds.length;label++) {
			row=0;
			line=0;
			for(int i=0;i<label_kinds.length;i++) {
				row += confusion_matrix[label][i];
				line += confusion_matrix[i][label];
			}
			if(row!=0)recall[label] = confusion_matrix[label][label]/row;
			if(line!=0)precision[label] = confusion_matrix[label][label]/line;
			if(recall[label]!=0||precision[label]!=0) {
				result[1] += (1+beta_Square)*precision[label]*recall[label]/(beta_Square*precision[label]+recall[label]);
			}
			if(row==0&&line==0) {label_sum--;continue;}
		}
		result[1]/=label_sum;
		for(int label=0;label<label_kinds.length;label++) {
			rec_m+=recall[label];
			Prec_m+=precision[label];
		}
		result[0] = (1+beta_Square)*rec_m*Prec_m/(beta_Square*Prec_m+rec_m);
		result[0]/=label_sum;
		return result;
	}
	
	public static double get_class_balance_accuracy(double[][] confusion_matrix, Integer[] label_kinds) {
		double result = 0;
		double row;
		double line;
		double label_sum = label_kinds.length;
		for(int label=0;label<label_kinds.length;label++) {
			row=0;
			line=0;
			for(int i=0;i<label_kinds.length;i++) {
				row += confusion_matrix[label][i];
				line += confusion_matrix[i][label];
			}
			if(row==0&&line==0) {label_sum--;continue;}
			if(row>line)result+=confusion_matrix[label][label]/row;
			else result+=confusion_matrix[label][label]/line;
		}
		result/=label_sum;
		return result;
	}
	
	public static double get_accuracy(double[][] confusion_matrix) {
		double result=0;
		double sum=0;
		for(int i=0;i<confusion_matrix.length;i++) {
			result+=confusion_matrix[i][i];
			for(int j=0;j<confusion_matrix.length;j++) sum+=confusion_matrix[i][j];
		}
		result/=sum;
		return result;
	}
}
