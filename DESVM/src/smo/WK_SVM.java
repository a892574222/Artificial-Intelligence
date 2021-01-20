package smo;

public class WK_SVM {
	private double[][][][][] test_data_x;
	private Integer[][][] test_data_y;
	private WK_SMOTE[][] finally_svm;
	
	public WK_SVM(double[][][][] training_data_x, Integer[][][] training_data_y, double KKTviolationsLevel, double[][][][][] test_data_x,
			Integer[][][] test_data_y) {
		this.test_data_x = test_data_x;
		this.test_data_y = test_data_y;
		finally_svm = new WK_SMOTE[test_data_x.length][test_data_x[0].length];
		this.select_index(training_data_x, training_data_y, KKTviolationsLevel);
	}
	
	protected void select_index(double[][][][] training_data_x,Integer[][][] training_data_y, double KKTviolationsLevel) {
		double[] Cost = {10,100,1000};
		double[] Sigma = {0.1,0.5,1,5,10,50,100};
		int[] Unequal = {0,1};
		
		GridSearch gridsearch;
		for(int times=0;times<training_data_x.length;times++) {
			for(int k=0;k<training_data_x[times].length;k++) {
				gridsearch = new GridSearch("WK_SVM",KKTviolationsLevel,training_data_x[times][k],training_data_y[times][k],Cost,Sigma,Unequal,5);
				finally_svm[times][k] = new WK_SMOTE(training_data_x[times][k],training_data_y[times][k],gridsearch.get_best_cost(),KKTviolationsLevel,gridsearch.get_best_kernel(),gridsearch.get_best_sigma(),gridsearch.get_best_unequal());
//				System.out.println(gridsearch.get_best_cost());
//				System.out.println(gridsearch.get_best_kernel());
//				System.out.println(gridsearch.get_best_sigma());
			}
		}
	}
	
	public void get_all_result(double beta) {
		Integer[] label_kinds;
		double[][] confusion_matrix;
		
		double fold_class_balance_accuracy;
		double class_balance_accuracy=0;
		
		double fold_average_accuracy;
		double average_accuracy=0;
		
		double fold_ave_recall;
		double ave_recall=0;
		
		//0ÊÇmacro£¬1ÊÇF1measure
		double[] fold_macro_average_and_F1_measure;
		
		double fold_beta_F1_measure;
		double beta_F1_measure=0;
		
		double fold_macro_average;
		double beta_macro_average=0;
		
		double fold_micro_average;
		double beta_micro_average=0;
		
		double fold_g_mean;
		double g_mean=0;

		for(int times=0;times<test_data_x.length;times++) {
			fold_class_balance_accuracy = 0;
			fold_average_accuracy=0;
			fold_beta_F1_measure=0;
			fold_macro_average=0;
			fold_micro_average=0;
			fold_g_mean=0;
			fold_ave_recall=0;
			for(int k=0;k<test_data_x[times].length;k++) {
				label_kinds = finally_svm[times][k].get_label_kinds();
				confusion_matrix = get_confusion_matrix(times,k);
				fold_class_balance_accuracy += Measurement.get_class_balance_accuracy(confusion_matrix,label_kinds);
				fold_ave_recall += Measurement.get_ave_recall(confusion_matrix);
				fold_average_accuracy += Measurement.get_average_accuracy(confusion_matrix,label_kinds);
				fold_macro_average_and_F1_measure = Measurement.get_macro_average_and_F1_measure(confusion_matrix,label_kinds,beta);
				fold_beta_F1_measure += fold_macro_average_and_F1_measure[1];
				fold_macro_average += fold_macro_average_and_F1_measure[0];
				fold_micro_average += Measurement.get_micro_average(confusion_matrix,label_kinds,beta);
				fold_g_mean += Measurement.get_g_mean(confusion_matrix,label_kinds);
			}
			class_balance_accuracy+=fold_class_balance_accuracy/test_data_x[times].length;
			ave_recall+=fold_ave_recall/test_data_x[times].length;
			average_accuracy+=fold_average_accuracy/test_data_x[times].length;
			beta_F1_measure+=fold_beta_F1_measure/test_data_x[times].length;
			beta_macro_average+=fold_macro_average/test_data_x[times].length;
			beta_micro_average+=fold_micro_average/test_data_x[times].length;
			g_mean+=fold_g_mean/test_data_x[times].length;
			
		}
		class_balance_accuracy/=test_data_x.length;
		ave_recall/=test_data_x.length;
		average_accuracy/=test_data_x.length;
		beta_F1_measure/=test_data_x.length;
		beta_macro_average/=test_data_x.length;
		beta_micro_average/=test_data_x.length;
		g_mean/=test_data_x.length;
		System.out.println("Average Recall:"+ave_recall);
		System.out.println("Class Balance Accuracy:"+class_balance_accuracy);
		System.out.println("Classes Average AVccuracy"+average_accuracy);
		System.out.println("Beta F1 Measure:"+beta_F1_measure);
		System.out.println("Beta Macro-average precision and recall:"+beta_macro_average);
		System.out.println("Beta Micro-average precision and recall:"+beta_micro_average);
		System.out.println("G-mean:"+g_mean);
	}

	private double[][] get_confusion_matrix(int times, int k) {
		Integer[] label_kinds = finally_svm[times][k].get_label_kinds();
		double[][] result = new double[label_kinds.length][label_kinds.length];
		Integer predict;
		int index_origin;
		int index_predict;
		for(int label=0;label<test_data_x[times][k].length;label++) {
			for(int number=0;number<test_data_x[times][k][label].length;number++) {
				predict = finally_svm[times][k].predict(test_data_x[times][k][label][number]);
				index_origin = Based_SVM.get_label_index(label_kinds,test_data_y[times][k][label]);
				index_predict = Based_SVM.get_label_index(label_kinds,predict);
				result[index_origin][index_predict]++;
			}
		}
		return result;
	}

}
