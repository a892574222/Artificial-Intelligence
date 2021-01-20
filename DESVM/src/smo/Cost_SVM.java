package smo;

public class Cost_SVM extends Based_SVM{
	
	public Cost_SVM(double[][][][] training_data_x, Integer[][][] training_data_y, double KKTviolationsLevel, double[][][][][] test_data_x,
			Integer[][][] test_data_y,boolean smote) {
		super(training_data_x, test_data_y, KKTviolationsLevel, test_data_x, test_data_y);
		super.type = "Cost_SVM";
		select_index(training_data_x, training_data_y, KKTviolationsLevel,smote);
	}
	
	protected void select_index(double[][][][] training_data_x,Integer[][][] training_data_y, double KKTviolationsLevel, boolean smote) {
		double[] Cost = {10,100,1000};
		double[] Sigma = {0.1,0.5,1,5,10,50,100};
		double[] Weight = {(double)1/4,(double)1/3,(double)1/2,1,2,3,4};
		
		GridSearch gridsearch;
		for(int times=0;times<training_data_x.length;times++) {
			for(int k=0;k<training_data_x[times].length;k++) {
				gridsearch = new GridSearch(type,KKTviolationsLevel,training_data_x[times][k],training_data_y[times][k],Cost,Sigma,Weight,5,smote);
				finally_svm[times][k] = new OVO(training_data_x[times][k],training_data_y[times][k],type,gridsearch.get_best_cost(),KKTviolationsLevel,
				gridsearch.get_best_kernel(),gridsearch.get_best_sigma(),smote,gridsearch.get_best_weight(),0,0);
			}
		}
	}

}
