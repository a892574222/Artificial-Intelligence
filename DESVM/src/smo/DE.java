package smo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class DE {
	private double[][][] population;
	private int population_size;
	private int features_size;
	private Random random;
	private double tol;
	private String kernel;
	//random.nextdouble;
	private double[] x_l;
	private double[] x_u;
	//����Ĵ�С�趨
	private double F;
	//�������
	private double CR;
	//����������
	private int maxiter;
	//������Ŀ��ĸ����±�
	private double[][] tar;
	private double distance=999.9;
	
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	
	private double[][][] validation_data_x;
	private Integer[] validation_data_y;
	
	private double[][][] test_data_x;
	private Integer[] test_data_y;
	
	private double cost[];
	//private boolean flag = true;
	private double P;
	private double p_d;
	private int first_in_i;
	private boolean type;
	
	private DESVM_SMO[] ovo;
	
	private BufferedWriter writer;
	
	//�����
	public DE(double[][][] train_data_x,Integer[][] train_data_y,double[][][] validation_data_x,Integer[] validation_data_y,double tol,double[] x_l,double[] x_u,boolean type,
			double[][][] test_data_x,Integer[] test_data_y,String file_name) throws IOException {
		random  = new Random();
		this.train_data_x = train_data_x;
		this.train_data_y = train_data_y;
		this.validation_data_x = validation_data_x;
		this.validation_data_y = validation_data_y;
		this.population_size =40;
		this.cost = new double[population_size];
		this.maxiter = 100;
		this.test_data_x = test_data_x;
		this.test_data_y = test_data_y;
		this.tol = tol;
		this.x_l = x_l;
		this.x_u = x_u;
		this.type = type;
		this.features_size = x_l.length;
		this.population = new double[train_data_x.length][population_size][features_size];
		this.first_in_i = -1;
		this.ovo = new DESVM_SMO[train_data_x.length];
		File file = new File("src\\result\\"+file_name+".csv");
		writer = new BufferedWriter(new FileWriter(file));
		writer.write("Average Recall"+",");
		writer.write("Class Balance Accuracy"+",");
		writer.write("Classes Average AVccuracy"+",");
		writer.write("Beta F1 Measure"+",");
		writer.write("Beta Macro-average precision and recall"+",");
		writer.write("Beta Micro-average precision and recall"+",");
		writer.write("G-mean"+",");
		writer.newLine();
		init();
		find();
	}
	
	
	public double[][] get_result() {
		return tar;
	}
	
	
	
	private void init() {
		for(int k=0;k<train_data_x.length;k++)
			for(int i=0;i<population_size;i++)
				for(int j=0;j<features_size;j++) {
					if(j==4||j==6) {
						population[k][i][j] =(int)x_l[j]+random.nextInt((int)(x_u[j]-x_l[j])+1);
						continue;
					}
					population[k][i][j] = x_l[j]+random.nextDouble()*(x_u[j]-x_l[j]);
				}
	}
	
	private void find() throws IOException {
		//ͨ������ͽ��棬ѡ����ѡ�����Ų�����
		int iter=0;
		double percent;
		double[][] gene = new double [train_data_x.length][];
		double temp_cost;
		double[][] temp_gene = new double [train_data_x.length][];
		boolean s_gt=true;
		boolean in_s;
		int g_t=-1;
		int flag;
		//������û�е�0��,��Ϊ���ǽ�maxiters��С��100��������tҲ��С100��
		int T = Math.round((float)0.1*population_size-1);
		
		
//		T=3*T;
		
		
		double[] SR_T = new double[maxiter];
		double[] SR_G = new double[maxiter];
		double sum;
		for(int i=0;i<SR_T.length;i++) {
			if(i<T+1)SR_T[i] = 0;
			else SR_T[i] = 0.1;
		}
		//��ʼ��fitness
		for(int i=0;i<population_size;i++) {
			for(int m=0;m<train_data_x.length;m++)gene[m] = population[m][i];
			cost[i] = fitness(gene);
		}
		//���˵�����������⣬����֪����ô��������һ������ѭ��������
		while(iter<maxiter) {
//			if((iter+1)%50==0)
				System.out.println("�ڣ�"+(iter+1));
			if(iter>=T) {
				if(s_gt=false)break;
				else {
					boolean all=true;
					for(int i=iter-T;i<iter;i++)if(SR_G[i]>SR_T[i]) {all=false;break;}
					if(all) {g_t = iter;s_gt=false;}
				}
			}
			if(s_gt==false&&iter==g_t)s_gt=true;
			//flag=false;
			//�Ի����fitness�Ӻõ���ѡ������
			for(int i=0;i<cost.length-1;i++) {
				flag=i;
				for(int j=i+1;j<cost.length;j++) {
					if(cost[j]<cost[flag])flag=j;
				}
				temp_cost = cost[i];
				cost[i] = cost[flag];
				cost[flag] = temp_cost;
				for(int m=0;m<train_data_x.length;m++)temp_gene[m] = population[m][flag];
				for(int m=0;m<train_data_x.length;m++)population[m][flag] = population[m][i];
				for(int m=0;m<train_data_x.length;m++)population[m][i] = temp_gene[m];
				if(i==0&&iter==0) {
					distance=temp_cost;
					tar=temp_gene.clone();
				}
			}
//			System.out.println(Arrays.toString(cost));
			sum=0;
			P = 0.1+0.9*Math.pow(10, 5*((double)(iter+1)/maxiter-1));
			p_d = 0.1*P;
			//��ʼ��iter���Ĳ�ֽ���
			first_in_i = -1;
			for(int i=0;i<population_size;i++) {
				in_s=false;
				percent = (double)(i+1)/population_size;
				if(percent<P)in_s=true;
//				System.out.println(percent);
//				System.out.println(P);
//				System.out.println("--------");
				if(!in_s&&first_in_i==-1){
					if(percent>P)first_in_i = i;
					else first_in_i=i+1;
				}
				
			}
//			System.out.println(first_in_i);
//			System.out.println("--------");
			for(int i=0;i<population_size;i++) {
				if(select(i,s_gt)) sum++;
			}
			SR_G[iter] = sum/population_size;
			//System.out.println(SR_G[iter]);
			if(s_gt==true&&iter==g_t)s_gt=false;
//			System.out.println(distance);
			
			iter++;
			record();
		}
		writer.close();
	}
	
	//��ÿһ�����ŵĽ�����浽csv��
	private void record() {
		// TODO Auto-generated method stub
		String kernel = null;
		ovo= new DESVM_SMO[train_data_x.length];
		Integer predict;
		int index_origin;
		int index_predict;
		Integer[] label_kinds = OVO_DESVM.set_num(validation_data_y);
		double[][] confusion_matrix = new double[label_kinds.length][label_kinds.length];
		double ave_recall,g_mean,micro_average,average_accuracy,class_balance_accuracy;
		double[] macro_average_and_F1_measure;
		
		for(int i=0;i<train_data_x.length;i++) {
			if(tar[i][4]==0)kernel="line";
			else if(tar[i][4]==1)kernel="rbf";
			else if(tar[i][4]==2)kernel="poly";
			else {System.out.println("error");System.exit(0);}
			ovo[i] = new DESVM_SMO(train_data_x[i],train_data_y[i],tol, tar[i][0],tar[i][1],tar[i][2],tar[i][3], kernel,tar[i][5],(int)tar[i][6]);}

		for(int i=0;i<validation_data_x.length;i++) {
			for(int j=0;j<validation_data_x[i].length;j++) {
				predict = predict(validation_data_x[i][j]);
				index_origin = Based_SVM.get_label_index(label_kinds,validation_data_y[i]);
				index_predict = Based_SVM.get_label_index(label_kinds,predict);
				confusion_matrix[index_origin][index_predict]++;
			}
		}
		ave_recall = Measurement.get_ave_recall(confusion_matrix);
		class_balance_accuracy = Measurement.get_class_balance_accuracy(confusion_matrix, label_kinds);
		average_accuracy = Measurement.get_average_accuracy(confusion_matrix, label_kinds);
		macro_average_and_F1_measure = Measurement.get_macro_average_and_F1_measure(confusion_matrix, label_kinds, 1);
		micro_average = Measurement.get_micro_average(confusion_matrix, label_kinds, 1);
		g_mean = Measurement.get_g_mean(confusion_matrix, label_kinds);
		try {
			writer.write(String.valueOf(ave_recall)+",");
			writer.write(String.valueOf(class_balance_accuracy)+",");
			writer.write(String.valueOf(average_accuracy)+",");
			writer.write(String.valueOf(macro_average_and_F1_measure[1])+",");
			writer.write(String.valueOf(macro_average_and_F1_measure[0])+",");
			writer.write(String.valueOf(micro_average)+",");
			writer.write(String.valueOf(g_mean)+",");
		}catch(IOException e) {}
		label_kinds = OVO_DESVM.set_num(validation_data_y);
		confusion_matrix = new double[label_kinds.length][label_kinds.length];
		for(int i=0;i<test_data_x.length;i++) {
			for(int j=0;j<test_data_x[i].length;j++) {
				predict = predict(test_data_x[i][j]);
				index_origin = Based_SVM.get_label_index(label_kinds,test_data_y[i]);
				index_predict = Based_SVM.get_label_index(label_kinds,predict);
				confusion_matrix[index_origin][index_predict]++;
			}
		}
		ave_recall = Measurement.get_ave_recall(confusion_matrix);
		class_balance_accuracy = Measurement.get_class_balance_accuracy(confusion_matrix, label_kinds);
		average_accuracy = Measurement.get_average_accuracy(confusion_matrix, label_kinds);
		macro_average_and_F1_measure = Measurement.get_macro_average_and_F1_measure(confusion_matrix, label_kinds, 1);
		micro_average = Measurement.get_micro_average(confusion_matrix, label_kinds, 1);
		g_mean = Measurement.get_g_mean(confusion_matrix, label_kinds);
		try {
			writer.write(String.valueOf(ave_recall)+",");
			writer.write(String.valueOf(class_balance_accuracy)+",");
			writer.write(String.valueOf(average_accuracy)+",");
			writer.write(String.valueOf(macro_average_and_F1_measure[1])+",");
			writer.write(String.valueOf(macro_average_and_F1_measure[0])+",");
			writer.write(String.valueOf(micro_average)+",");
			writer.write(String.valueOf(g_mean)+",");
			writer.newLine();
		}catch(IOException e) {}
	}
	
	public Integer predict(double[] unkonwn) {
		int value[] = new int[validation_data_y.length];
		Vector<Integer> index_list = new Vector<Integer>();
		Integer result;
		int p=0;
		
		for(int i=0;i<ovo.length;i++) {
//			percent = ovo[i].get_probability(unkonwn);
//			label=ovo[i].get_label();
//			if(percent>=0.5)result=label[1];
//			else result=label[0];
			
			result = ovo[i].predict(unkonwn);
			
			for(int j=0;j<validation_data_y.length;j++)if(result==validation_data_y[j])value[j]++;
		}
		for(int i=1;i<value.length;i++)if(value[i]>value[p])p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length<validation_data_x[p].length)p=i;
		for(int i=0;i<value.length;i++)if(value[i]==value[p]&&validation_data_x[i].length==validation_data_x[p].length)index_list.add(i);
		p=new Random().nextInt(index_list.size());
		p=index_list.get(p);
		return validation_data_y[p];
	}


	//ƽ��error
	private double get_loss_ave(double[] data_x,Integer data_y) {
		Integer positive,negative;
		double error=0;
		double probability;
		for(int i=0;i<ovo.length;i++) {
			negative = ovo[i].get_label()[0];
			positive = ovo[i].get_label()[1];
			if(data_y==negative||data_y==positive) {
				probability = ovo[i].get_probability(data_x);
				if(data_y==positive)error+=1-probability;
				else error+=probability;
				error+=Math.sqrt((Math.log(ovo[i].getNumber())+Math.log(20))/(2*train_data_x[i].length));
			}
		}
		error=error/(validation_data_y.length-1);
		return error;
	}
	
	//���error
	private double get_loss_max(double[] data_x,Integer data_y) {
		Integer positive,negative;
		double error;
		double probability;
		double result=0;
		for(int i=0;i<ovo.length;i++) {
			error=0;
			negative = ovo[i].get_label()[0];
			positive = ovo[i].get_label()[1];
			if(data_y==negative||data_y==positive) {
				probability = ovo[i].get_probability(data_x);
				if(data_y==positive)error+=1-probability;
				else error+=probability;
				error+=Math.sqrt((Math.log(ovo[i].getNumber())+Math.log(20))/(2*train_data_x[i].length));
			}
			if(error>result)result=error;
		}
		return result;
	}
	
	
	//��������error
	private double fitness(double[][] gen) {
		double result=0;
		double error;
		for(int i=0;i<train_data_x.length;i++) {
			if(gen[i][4]==0)kernel="line";
			else if(gen[i][4]==1)kernel="rbf";
			else if(gen[i][4]==2)kernel="poly";
			ovo[i] = new DESVM_SMO(train_data_x[i],train_data_y[i],tol, gen[i][0],gen[i][1],gen[i][2],gen[i][3], kernel,gen[i][5],(int)gen[i][6]);
		}
		for(int i=0;i<validation_data_x.length;i++) {
			error=0;
			for(int j=0;j<validation_data_x[i].length;j++) {
				if(type)error+=get_loss_max(validation_data_x[i][j],validation_data_y[i]);
				else error+=get_loss_ave(validation_data_x[i][j],validation_data_y[i]);
			}
			result+=error/validation_data_x[i].length;
		}
		result/=validation_data_y.length;
		
		
		return result;
	}
	
	//�������,��i����������л�����б���
	private double[][] mutation(int rx,boolean s_gt) {
		int r0;
		int r1;
		int r2;
		int r3;
		Random randoms = new Random();
		double[][] v = new double[train_data_x.length][features_size];
		double[][] d_r3 = new double[train_data_x.length][features_size];
		if(s_gt)r0=rx;
		else r0 = randoms.nextInt(population_size);
		r1 = r0;
		if(r0>first_in_i)while(r1==r0)r1 = randoms.nextInt(first_in_i);
		else while(r1==r0)r1 = randoms.nextInt(population_size);
		r2 = r0;
		r3 = r0;
		while(r2==r0||r2==r1)r2 = randoms.nextInt(population_size);
		while(r3==r0||r3==r1||r3==r2)r3 = randoms.nextInt(population_size);
		
		//�����flag��Ӧ���ĵ�g �� gt. true��ʱ��Set o = i��
		F = new Random().nextGaussian()*Math.sqrt(0.1)+((double)r0/population_size);
		for(int k=0;k<train_data_x.length;k++) {
			for(int i=0;i<features_size;i++) {
				if(randoms.nextDouble()<p_d) {
					
					if(i==4||i==6) {d_r3[k][i] = (int)x_l[i]+random.nextInt((int)(x_u[i]-x_l[i])+1);}
					else {d_r3[k][i] = d_r3[k][i] = x_l[i]+randoms.nextDouble()*(x_u[i]-x_l[i]);}
					}
				else {d_r3[k][i] = population[k][r3][i];}
			}
		}
		
		for(int k=0;k<train_data_x.length;k++) {
			for(int i=0;i<features_size;i++) {
				if(i==4||i==6) {v[k][i] =population[k][r0][i]+population[k][r1][i]-population[k][r0][i]+population[k][r2][i]-d_r3[k][i];}
				else v[k][i] =population[k][r0][i]+F*(population[k][r1][i]-population[k][r0][i]) +F*(population[k][r2][i]-d_r3[k][i]);
			}
		}
		return v;
	}
	
	private double[][] crossover(int i,boolean s_gt) {
		double[][] v = mutation(i,s_gt);
		double[][] u = new double[train_data_x.length][features_size];
		Random randoms = new Random();
		CR = randoms.nextGaussian()*Math.sqrt(0.1)+((double)i/population_size);
		int j = randoms.nextInt(features_size);
		int l = randoms.nextInt(train_data_x.length);
		double p;
		for(int n=0;n<train_data_x.length;n++) {
			for(int k=0;k<features_size;k++) {
				p = randoms.nextDouble();
				if(p<=CR || (k==j&&n==l))u[n][k] = v[n][k];
				else u[n][k] = population[n][i][k];
				if((k==6||k==4)&&(u[n][k]<x_l[k]||u[n][k]>x_u[k]))u[n][k]=(int)x_l[k]+random.nextInt((int)(x_u[k]-x_l[k])+1);
//				else if(u[n][k]<x_l[k]&&k==6)u[n][k] = (int)random.nextInt((int)x_u[k]+1)-random.nextInt((int)x_l[k]+1);
				else if(u[n][k]<x_l[k])u[n][k] = x_l[k]+p*(x_u[k]-x_l[k]);
				else if(u[n][k]>x_u[k]&&(k==2||k==3))u[n][k] = x_l[k]+p*(x_u[k]-x_l[k]);
//				System.out.println(u[n][4]);
//				if(k==6) {
//					System.out.println(u[n][6]);
//					System.out.println(x_l[6]);
//					System.out.println(u[n][k]<x_l[k]||u[n][k]>x_u[k]);
//					System.out.println("------------");
//				}
			}
		}
		return u;
	}
	
	private boolean select(int i,boolean s_gt) {
		boolean is_replace = false;
		double[][] u = crossover(i,s_gt);
		double cost_0 = cost[i];
		double cost_1 = fitness(u);
		if(cost_1<cost_0) {
			is_replace = true;
			cost[i]=cost_1;
			for(int m=0;m<train_data_x.length;m++) 
				for(int n=0;n<features_size;n++)population[m][i][n] = u[m][n];
			if(cost_1<distance) {tar=u.clone();distance = cost_1;}//flag=true;}
		}
		return is_replace;
	}
	
}
