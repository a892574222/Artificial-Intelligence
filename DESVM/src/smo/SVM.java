package smo;

public class SVM extends Based_SVM{
	
	public SVM(double[][][][] training_data_x, Integer[][][] training_data_y, double KKTviolationsLevel, double[][][][][] test_data_x,
			Integer[][][] test_data_y,boolean static_smote) {
		super(training_data_x, test_data_y, KKTviolationsLevel, test_data_x, test_data_y);
		super.type = "SVM";
		select_index(training_data_x, training_data_y, KKTviolationsLevel,static_smote);
//		get_label_kinds();
	}
}
