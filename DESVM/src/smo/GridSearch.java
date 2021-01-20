package smo;


public class GridSearch {
	private double[] Sigma;
	private double[] Cost;
	private double[] Weight;
	private double[] Percent;
	private double[] Delta;
	private int[] Unequal;
	private double best_sigma;
	private double best_cost;
	private String best_kernel;
	private double best_weight;
	private double best_percent;
	private double best_delta;
	private int best_unequal;
	
	//普通SVM,SMOTE_SVM的参数传入
	public GridSearch(String type,double tol,double[][] data, Integer[] label, double[]Cost,double[] Sigma,int K,boolean static_smote) {
		this.Sigma = Sigma;
		this.Cost = Cost;
		Search(type,K,data,label,tol,static_smote);
	}
	//COST_SVM的参数传入
	public GridSearch(String type,double tol,double[][] data, Integer[] label, double[]Cost,double[] Sigma,double[] Weight,int K,boolean static_smote) {
		this.Sigma = Sigma;
		this.Cost = Cost;
		if(static_smote) {
			this.Weight= new double[1];
			this.Weight[0] = 1;
		}
		else this.Weight = Weight;
		Search(type,K,data,label,tol,static_smote);
	}
	//PPSVM的参数传入
	public GridSearch(String type,double tol,double[][] data, Integer[] label, double[]Cost,double[] Sigma,double[] Percent,double[] Delta,int K) {
		this.Sigma = Sigma;
		this.Cost = Cost;
		this.Percent = Percent;
		this.Delta = Delta;
		Search(type,K,data,label,tol,false);
	}
	
	//WK_SVM的参数传入
	public GridSearch(String type,double tol,double[][] data, Integer[] label, double[]Cost,double[] Sigma,int[] Unequal,int K) {
		this.Sigma = Sigma;
		this.Cost = Cost;
		this.Unequal = Unequal;
		Search(type,K,data,label,tol,false);
	}
	//NBSVM的参数传入
	public GridSearch(String type,double tol,double[][] data, Integer[] label, double[]Cost,double[] Sigma,double[] Weight,int K) {
		this.Sigma = Sigma;
		this.Cost = Cost;
		this.Weight = Weight;
		Search(type,K,data,label,tol,false);
	}
	
	public void Search(String type,int K,double[][] data, Integer[] label,double tol,boolean static_smote) {
		Data_Divied data_divied = new Data_Divied(K,data,label);
		double[][][] training_data_x = data_divied.get_training_data_x();
		Integer[][] traning_data_y = data_divied.get_training_data_y();
		double[][][][] test_data_x = data_divied.get_test_data_x();
		Integer[][] test_data_y = data_divied.get_test_data_y();
		OVO svm;
		WK_SMOTE wk_svm;
		OVA nbsvm;
		double recall;
		double fold_recall;
		double sum_recall;
		double best_recall = 0;
		
		for(int cost=0;cost<Cost.length;cost++) {
			for(int sigma=0;sigma<Sigma.length+1;sigma++) {
				switch(type) {
				case "SVM":
					sum_recall=0;
					for(int k=0;k<K;k++) {
						fold_recall = 0;
						if(sigma<Sigma.length)
							svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"rbf",Sigma[sigma],static_smote,0,0,0);
						else
							svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"line",0,static_smote,0,0,0);
						for(int label_test=0;label_test<test_data_x[k].length;label_test++) {
							recall=0;
							for(int number=0;number<test_data_x[k][label_test].length;number++) {
								if(svm.predict(test_data_x[k][label_test][number])==test_data_y[k][label_test])recall++;
							}
							fold_recall += recall/test_data_x[k][label_test].length;
						}
						sum_recall += fold_recall/test_data_x[k].length;
					}
					sum_recall /=K;
					if(sum_recall>best_recall) {
						best_recall=sum_recall;
						best_cost = Cost[cost];
						if(sigma==Sigma.length) best_kernel = "line";
						else {
							best_kernel = "rbf";
							best_sigma = Sigma[sigma];
						}
					}
					break;
				case "Cost_SVM":
					for(int weight=0;weight<Weight.length;weight++) {
						sum_recall=0;
						for(int k=0;k<K;k++) {
							fold_recall = 0;
							if(sigma<Sigma.length)
								svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"rbf",Sigma[sigma],static_smote,Weight[weight],0,0);
							else
								svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"line",0,static_smote,Weight[weight],0,0);
							for(int label_test=0;label_test<test_data_x[k].length;label_test++) {
								recall=0;
								for(int number=0;number<test_data_x[k][label_test].length;number++) {
									if(svm.predict(test_data_x[k][label_test][number])==test_data_y[k][label_test])recall++;
								}
								fold_recall += recall/test_data_x[k][label_test].length;
							}
							sum_recall += fold_recall/test_data_x[k].length;
						}
						sum_recall /=K;
						if(sum_recall>best_recall) {
							best_recall=sum_recall;
							best_cost = Cost[cost];
							best_weight = Weight[weight];
							if(sigma==Sigma.length) best_kernel = "line";
							else {
								best_kernel = "rbf";
								best_sigma = Sigma[sigma];
							}
						}
					}
					break;
				case "PPSVM":
					for(int percent=0;percent<Percent.length;percent++) {
						for(int delta=0;delta<Delta.length;delta++) {
							sum_recall=0;
							for(int k=0;k<K;k++) {
								fold_recall = 0;
								if(sigma<Sigma.length)
									svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"rbf",Sigma[sigma],static_smote,0,Percent[percent],Delta[delta]);
								else
									svm = new OVO(training_data_x[k],traning_data_y[k],type,Cost[cost],tol,"line",0,static_smote,0,Percent[percent],Delta[delta]);
								for(int label_test=0;label_test<test_data_x[k].length;label_test++) {
									recall=0;
									for(int number=0;number<test_data_x[k][label_test].length;number++) {
										if(svm.predict(test_data_x[k][label_test][number])==test_data_y[k][label_test])recall++;
									}
									fold_recall += recall/test_data_x[k][label_test].length;
								}
								sum_recall += fold_recall/test_data_x[k].length;
							}
							sum_recall /=K;
							if(sum_recall>best_recall) {
								best_recall=sum_recall;
								best_cost = Cost[cost];
								best_percent = Percent[percent];
								best_delta = Delta[delta];
								if(sigma==Sigma.length) best_kernel = "line";
								else {
									best_kernel = "rbf";
									best_sigma = Sigma[sigma];
								}
							}
						}
					}
					break;
				case"WK_SVM":
					for(int unequal=0;unequal<Unequal.length;unequal++) {
						sum_recall=0;
						for(int k=0;k<K;k++) {
							fold_recall = 0;
							if(sigma<Sigma.length)
								wk_svm = new WK_SMOTE(training_data_x[k],traning_data_y[k],Cost[cost],tol,"rbf",Sigma[sigma],Unequal[unequal]);
							else
								wk_svm = new WK_SMOTE(training_data_x[k],traning_data_y[k],Cost[cost],tol,"line",0,Unequal[unequal]);
							for(int label_test=0;label_test<test_data_x[k].length;label_test++) {
								recall=0;
								for(int number=0;number<test_data_x[k][label_test].length;number++) {
									if(wk_svm.predict(test_data_x[k][label_test][number])==test_data_y[k][label_test])recall++;
								}
								fold_recall += recall/test_data_x[k][label_test].length;
							}
							sum_recall += fold_recall/test_data_x[k].length;
						}
						sum_recall /=K;
						if(sum_recall>best_recall) {
							best_recall=sum_recall;
							best_cost = Cost[cost];
							best_unequal = Unequal[unequal];
							if(sigma==Sigma.length) best_kernel = "line";
							else {
								best_kernel = "rbf";
								best_sigma = Sigma[sigma];
							}
						}
					}
					break;
				case "NBSVM":
					for(int weight=0;weight<Weight.length;weight++) {
						sum_recall=0;
						for(int k=0;k<K;k++) {
							fold_recall = 0;
							if(sigma<Sigma.length)
								nbsvm = new OVA(training_data_x[k],traning_data_y[k],tol,Cost[cost],"rbf",Sigma[sigma],Weight[weight]);
							else
								nbsvm = new OVA(training_data_x[k],traning_data_y[k],tol,Cost[cost],"line",0,Weight[weight]);
							for(int label_test=0;label_test<test_data_x[k].length;label_test++) {
								recall=0;
								for(int number=0;number<test_data_x[k][label_test].length;number++) {
									if(nbsvm.predict(test_data_x[k][label_test][number])==test_data_y[k][label_test])recall++;
								}
								fold_recall += recall/test_data_x[k][label_test].length;
							}
							sum_recall += fold_recall/test_data_x[k].length;
						}
						sum_recall /=K;
						if(sum_recall>best_recall) {
							best_recall=sum_recall;
							best_cost = Cost[cost];
							best_weight = Weight[weight];
							if(sigma==Sigma.length) best_kernel = "line";
							else {
								best_kernel = "rbf";
								best_sigma = Sigma[sigma];
							}
						}
					}
					break;
				}
			}
		}
//		System.out.println(best_recall);
	}
	
	public double get_best_cost() {
		return best_cost;
	}
	
	public double get_best_sigma() {
		return best_sigma;
	}
	
	public String get_best_kernel() {
		return best_kernel;
	}
	
	public double get_best_weight() {
		return best_weight;
	}
	
	public double get_best_percent() {
		return best_percent;
	}
	
	public double get_best_delta() {
		return best_delta;
	}
	
	public int get_best_unequal() {
		return best_unequal;
	}
}
