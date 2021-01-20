package smo;

public class PPSVM extends Based_SVM{

	public PPSVM(double[][][][] training_data_x, Integer[][][] training_data_y, double KKTviolationsLevel,
			double[][][][][] test_data_x, Integer[][][] test_data_y) {
		super(training_data_x, training_data_y, KKTviolationsLevel, test_data_x, test_data_y);
		super.type = "PPSVM";
		select_index(training_data_x, training_data_y, KKTviolationsLevel);
	}

	protected void select_index(double[][][][] training_data_x,Integer[][][] training_data_y, double KKTviolationsLevel) {
		double[] Cost = {10,100,1000};
		double[] Sigma = {0.1,0.5,1,5,10,50,100};
		double[] Delta = {0.05};
		double[] Percent= {0.075,0.1};
		
		GridSearch gridsearch;
		for(int times=0;times<training_data_x.length;times++) {
			for(int k=0;k<training_data_x[times].length;k++) {
				gridsearch = new GridSearch(type,KKTviolationsLevel,training_data_x[times][k],training_data_y[times][k],Cost,Sigma,Percent,Delta,5);
				finally_svm[times][k] = new OVO(training_data_x[times][k],training_data_y[times][k],type,gridsearch.get_best_cost(),KKTviolationsLevel,
				gridsearch.get_best_kernel(),gridsearch.get_best_sigma(),false,0,gridsearch.get_best_percent(),gridsearch.get_best_delta());
			}
		}
	}
}
