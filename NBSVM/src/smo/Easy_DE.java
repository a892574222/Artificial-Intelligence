package smo;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class Easy_DE {
	private double[][][] population;
	private int population_size;
	private int features_size;
	private Random random;
	private double tol;
	private String kernel;
	//random.nextdouble;
	private double[] x_l;
	private double[] x_u;
	//变异的大小设定，实数，F∈[0,2]
	private double F=1.0;
	//交叉概率
	private double CR=0.8;
	//最大迭代次数
	private int maxiter;
	//期望的目标的个体下标
	private int tar;
	private double distance=999.9;
	
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	
	private double[][][] validation_data_x;
	private Integer[] validation_data_y;
	
	private double cost[];
	//private boolean flag = true;
	
	private double[][][] x1;
	private Integer[] y1;
	
	//多分类
	public Easy_DE(double[][][] train_data_x,Integer[][] train_data_y,double[][][] validation_data_x,Integer[] validation_data_y,double tol,double[] x_l,double[] x_u,double[][][]x1,Integer[]y1) {
		random  = new Random();
		this.train_data_x = train_data_x;
		this.train_data_y = train_data_y;
		this.validation_data_x = validation_data_x;
		this.validation_data_y = validation_data_y;
		this.population_size =20;
		this.cost = new double[population_size];
		this.maxiter = 20;
		System.out.println(maxiter);
		this.tol = tol;
		this.x_l = x_l;
		this.x_u = x_u;
		this.features_size = x_l.length;
		this.population = new double[train_data_x.length][population_size][features_size];
		this.x1 = x1;
		this.y1 = y1;
		init();
		find();
	}
	
	
	public double[][] get_result() {
		double[][]result = new double[train_data_x.length][features_size];
		for(int i=0;i<train_data_x.length;i++) {
			for(int j=0;j<features_size;j++)result[i][j] = population[i][tar][j];
		}
		return result;
	}
	
	private void init() {
		for(int k=0;k<train_data_x.length;k++)
			for(int i=0;i<population_size;i++)
				for(int j=0;j<features_size;j++) {
					if(j==4||j==6) {
						population[k][i][j] =(int)random.nextInt((int)x_u[j]+1)-random.nextInt((int)x_l[j]+1);
						continue;
					}
					population[k][i][j] = x_l[j]+random.nextDouble()*(x_u[j]-x_l[j]);
				}
	}
	
	private void find() {
		//通过变异和交叉，选择来选择最优参数；
		int iter=0;
		double[][] gene = new double [train_data_x.length][];

		
		
		//T=3*T;
		

		//初始化fitness和distance
		for(int i=0;i<population_size;i++) {
			for(int m=0;m<train_data_x.length;m++)gene[m] = population[m][i];
			cost[i] = fitness(gene);
			if(cost[i]<distance) {tar=i;distance=cost[i];}
		}

		
		//除了到达迭代上限外，还不知道怎么决定另外一个跳出循环的条件
		while(iter<maxiter) {
//			if((iter)%50==0)
				System.out.println("第："+iter);
			//开始第iter代的差分进化
			for(int i=0;i<population_size;i++) {
				select(i);
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			gene=this.get_result();
			NBSVM_SMO[] ovo;
			ovo= new NBSVM_SMO[train_data_x.length];
			for(int i=0;i<train_data_x.length;i++) {
				if(gene[i][4]==0)kernel="line";
				else if(gene[i][4]==1)kernel="rbf";
				else if(gene[i][4]==2)kernel="poly";
				ovo[i] = new NBSVM_SMO(train_data_x[i],train_data_y[i],tol, gene[i][0],gene[i][1],gene[i][2],gene[i][3], kernel,gene[i][5],(int)gene[i][6]);}
			double[] result = new double[x1.length];
			for(int n=0;n<x1.length;n++) {
				double q = 0;
				for(int m=0;m<x1[n].length;m++)if(y1[n]==this.predict(ovo,x1[n][m]))q++;
				result[n] = q/x1[n].length;
				System.out.println("预测的类别:"+y1[n]);
				System.out.println("预测的类别的向量总数:"+x1[n].length);
				System.out.println("预测准确率"+result[n]);
			}
			double finally_result = 0;
			for(int n=0;n<result.length;n++)finally_result+=result[n];
			finally_result=finally_result/result.length;
			System.out.println("第"+iter+"次测试结果"+finally_result);
			
			
			
			
			
			
			
			
			
			
			
			
			
			
		

			
			System.out.println(distance);
			
			iter++;
		}
	}
	
	//目前目标函数,每一类的分类准确率的平均数
	private double fitness(double[][] gen) {
		NBSVM_SMO[] ovo = new NBSVM_SMO[train_data_x.length];
		double[] specificity = new double[validation_data_x.length];
		double sum=0;
		double support_sum=0;
		double data_sum=0;
		double result=0;
		for(int i=0;i<train_data_x.length;i++) {
			if(gen[i][4]==0)kernel="line";
			else if(gen[i][4]==1)kernel="rbf";
			else if(gen[i][4]==2)kernel="poly";
			ovo[i] = new NBSVM_SMO(train_data_x[i],train_data_y[i],tol, gen[i][0],gen[i][1],gen[i][2],gen[i][3], kernel,gen[i][5],(int)gen[i][6]);
			support_sum+=ovo[i].getNumber();
			data_sum+=train_data_x[i].length;
		}
		for(int i=0;i<validation_data_x.length;i++) {
			sum=0;
			for(int j=0;j<validation_data_x[i].length;j++) {
				if(validation_data_y[i]==predict(ovo,validation_data_x[i][j]))sum++;
			
			}
			specificity[i] = sum/validation_data_x[i].length;
		}
		for(int i=0;i<specificity.length;i++) {
			result+=specificity[i];
		}	
		
		result = result/specificity.length;
		//System.out.println(result);
		result=1-result+Math.sqrt((Math.log(support_sum)+Math.log(100))/(2*data_sum));
		//System.out.println(result);
		return result;
	}
	
	//最高票数相同的时候在所有最高票数的类中随机选取
	public Integer predict(NBSVM_SMO[] ovo,double[] unkonwn) {
		int value[] = new int[validation_data_y.length];
		Vector<Integer> index_list = new Vector<Integer>();
		Integer result;
		int p=0;
		for(int i=0;i<ovo.length;i++) {
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
	
	//变异操作,对i个个体的所有基因进行变异
	private double[][] mutation(int rx) {
		int r0=rx;
		int r1=rx;
		int r2=rx;
		Random randoms = new Random();
		double[][] v = new double[train_data_x.length][features_size];
		while(r1==rx)r1 = randoms.nextInt(population_size);
		while(r1==rx)r1 = randoms.nextInt(population_size);
		while(r0==rx||r0==r1)r0 = randoms.nextInt(population_size);
		while(r2==rx||r2==r0||r2==r1)r2 = randoms.nextInt(population_size);

		for(int k=0;k<train_data_x.length;k++) {
			for(int i=0;i<features_size;i++) {
				if(i==4||i==6) {v[k][i] = population[k][tar][i]+population[k][rx][i]-population[k][r0][i]+population[k][r1][i]-population[k][r2][i];continue;}
				else v[k][i] = population[k][tar][i]+F*(population[k][rx][i]-population[k][r0][i]) +F*(population[k][r1][i]-population[k][r2][i]);
			}
		}
		return v;
	}
	
	private double[][] crossover(int i) {
		double[][] v = mutation(i);
		double[][] u = new double[train_data_x.length][features_size];
		Random randoms = new Random();

		//int j = randoms.nextInt(features_size);
		int l = randoms.nextInt(train_data_x.length);
		double p;
		for(int n=0;n<train_data_x.length;n++) {
			for(int k=0;k<features_size;k++) {
				p = randoms.nextDouble();
				if(p<=CR || (n==l))u[n][k] = v[n][k];
				else u[n][k] = population[n][i][k];
				if(k==4&&(u[n][k]<x_l[k]||u[n][k]>x_u[k]))u[n][k]=(int)random.nextInt((int)x_u[k]+1)-random.nextInt((int)x_l[k]+1);
				else if(u[n][k]<x_l[k]&&k==6)u[n][k] = (int)random.nextInt((int)x_u[k]+1)-random.nextInt((int)x_l[k]+1);
				else if(u[n][k]<x_l[k])u[n][k] = x_l[k]+p*(x_u[k]-x_l[k]);
				else if(u[n][k]>x_u[k]&&(k==2||k==3))u[n][k] = x_l[k]+p*(x_u[k]-x_l[k]);
//				System.out.println(u[n][4]);
//				System.out.println(u[n][6]);
//				System.out.println("------------");
			}
		}
		return u;
	}
	
	private boolean select(int i) {
		boolean is_replace = false;
		double[][] u = crossover(i);
		double cost_0 = cost[i];
		double cost_1 = fitness(u);
		if(cost_1<cost_0) {
			is_replace = true;
			cost[i]=cost_1;
			for(int m=0;m<train_data_x.length;m++) 
				for(int n=0;n<features_size;n++)population[m][i][n] = u[m][n];
			if(cost_1<distance) {tar=i;distance = cost_1;}//flag=true;}
		}
		return is_replace;
	}
	
}
