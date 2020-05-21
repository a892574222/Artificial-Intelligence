package smo;

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
	//变异的大小设定，实数，F∈[0,1]
	private double F;
	//交叉概率
	private double CR;
	//最大迭代次数
	private int maxiter;
	//期望的目标的个体下标
	private int tar;
	private double distance;
	
	private double[][][] train_data_x;
	private Integer[][] train_data_y;
	
	private double[][][] validation_data_x;
	private Integer[] validation_data_y;
	
	private double cost[];
	//private boolean flag = true;
	private double P;
	private double p_d;
	private int first_in_i;
	
	//单分类
	public DE(double[][] train_data_x,Integer[] train_data_y,double[][][] validation_data_x,Integer[] validation_data_y,double tol,double[] x_l,double[] x_u,String kernel) {
		random  = new Random();
		double[][][] data_xx;
		Integer[][] data_yy;
		data_xx = new double[1][][];
		data_xx[0] = train_data_x;
		this.train_data_x = data_xx;
		data_yy = new Integer[1][];
		data_yy[0] = train_data_y;
		this.train_data_y = data_yy;
		this.validation_data_x = validation_data_x;
		this.validation_data_y = validation_data_y;
		this.population_size =5*4*this.train_data_x.length;
		this.cost = new double[population_size];
		this.maxiter = 1000*x_l.length/population_size;
		this.tol = tol;
		this.kernel = kernel;
		this.x_l = x_l;
		this.x_u = x_u;
		this.features_size = x_l.length;
		this.population = new double[1][population_size][features_size];
		this.first_in_i = -1;
		init();
		find();
	}
	
	//多分类
	public DE(double[][][] train_data_x,Integer[][] train_data_y,double[][][] validation_data_x,Integer[] validation_data_y,double tol,double[] x_l,double[] x_u,String kernel) {
		random  = new Random();
		this.train_data_x = train_data_x;
		this.train_data_y = train_data_y;
		this.validation_data_x = validation_data_x;
		this.validation_data_y = validation_data_y;
		this.population_size =5*x_l.length*this.train_data_x.length;
		this.cost = new double[population_size];
		this.maxiter = 1000*x_l.length*train_data_x.length/population_size;
		this.tol = tol;
		this.kernel = kernel;
		this.x_l = x_l;
		this.x_u = x_u;
		this.features_size = x_l.length;
		this.population = new double[train_data_x.length][population_size][features_size];
		this.first_in_i = -1;
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
				for(int j=0;j<features_size;j++)
					population[k][i][j] = x_l[j]+random.nextDouble()*(x_u[j]-x_l[j]);
	}
	
	private void find() {
		//通过变异和交叉，选择来选择最优参数；
		int iter=0;
		double percent;
		double[][] gene = new double [train_data_x.length][];
		double temp_cost;
		double[][] temp_gene = new double [train_data_x.length][];
		boolean s_gt=true;
		boolean in_s;
		int g_t=-1;
		//论文中没有第0代
		int T = Math.round((float)1000*x_l.length*train_data_x.length/population_size-1);
		
		
		//T=3*T;
		
		
		double[] SR_T = new double[maxiter];
		double[] SR_G = new double[maxiter];
		double sum;
		for(int i=0;i<SR_T.length;i++) {
			if(i<T+1)SR_T[i] = 0;
			else SR_T[i] = 0.1;
		}
		//除了到达迭代上限外，还不知道怎么决定另外一个跳出循环的条件
		while(iter<maxiter) {
//			if((iter)%50==0)
				System.out.println("第："+iter);
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
			//计算所有个体的fitness
			for(int i=0;i<population_size;i++) {
				for(int m=0;m<train_data_x.length;m++)gene[m] = population[m][i];
				cost[i] = fitness(gene);
			}
			//对个体按照fitness从好到坏排序,插入排序
			for(int i=1;i<population_size;i++) {
				if(cost[i]>cost[i-1]) {
					temp_cost = cost[i];
					for(int m=0;m<train_data_x.length;m++)temp_gene[m] = population[m][i];
					for(int j=i;j>-1;j--) {
						if(j>0&&cost[j-1]<temp_cost) {
							cost[j] = cost[j-1];
							for(int m=0;m<train_data_x.length;m++)population[m][j] = population[m][j-1];
						}else{
							cost[j] = temp_cost;
							for(int m=0;m<train_data_x.length;m++)population[m][j] = temp_gene[m];
							break;
						}
					}
				}
			}
			sum=0;
			P = 0.1+0.9*Math.pow(10, 5*((iter+1)/maxiter-1));
			p_d = 0.1*P;
			//开始第iter代的差分进化
			for(int i=0;i<population_size;i++) {
				in_s=false;
				percent = (double)i/population_size;
				if(percent<=P)in_s=true;
				if(!in_s&&first_in_i==-1)first_in_i=iter;
				if(select(i,s_gt,in_s)) sum++;
			}
			SR_G[iter] = sum/population_size;
			//System.out.println(SR_G[iter]);
			if(s_gt==true&&iter==g_t)s_gt=false;
			
			System.out.println(distance);
			
			iter++;
		}
	}
	
	//目前目标函数,每一类的分类准确率的平均数
	private double fitness(double[][] gen) {
		NBSVM_SMO[] ovo = new NBSVM_SMO[train_data_x.length];
		double[] specificity = new double[validation_data_x.length];
		double sum;
		double result=0;;
		for(int i=0;i<train_data_x.length;i++) {
			ovo[i] = new NBSVM_SMO(train_data_x[i],train_data_y[i],tol, gen[i][0],gen[i][1],gen[i][2], kernel,gen[i][3]);
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
	private double[][] mutation(int rx,boolean s_gt,boolean in_s) {
		int r0=rx;
		int r1=rx;
		int r2=rx;
		int r3=rx;
		Random randoms = new Random();
		double[][] v = new double[train_data_x.length][features_size];
		double[][] d_r3 = new double[train_data_x.length][features_size];
		while(r1==rx)r1 = randoms.nextInt(population_size);
		if(!in_s)while(r1==rx)r1 = randoms.nextInt(first_in_i);
		while(r0==rx||r0==r1)r0 = randoms.nextInt(population_size);
		while(r2==rx||r2==r0||r2==r1)r2 = randoms.nextInt(population_size);
		while(r3==rx||r3==r0||r3==r1||r3==r2)r3 = randoms.nextInt(population_size);
		
		//这里的flag对应论文的g ≤ gt. true的时候Set o = i；
		if(s_gt)r0=rx;
		F = new Random().nextGaussian()*Math.sqrt(0.1)+((double)r0/population_size);
		for(int k=0;k<train_data_x.length;k++) {
			for(int i=0;i<features_size;i++) {
				if(randoms.nextDouble()<p_d)d_r3[k][i] = x_l[i]+randoms.nextDouble()*(x_u[i]-x_l[i]);
				else d_r3[k][i] = population[k][r3][i];
			}
		}
		for(int k=0;k<train_data_x.length;k++) {
			for(int i=0;i<features_size;i++) {
				if(in_s)v[k][i] = F*(population[k][r1][i]-population[k][r0][i]) +F*(population[k][r2][i]-d_r3[k][i]);
				else v[k][i] = F*(population[k][r1][i]-population[k][r0][i]) +F*(population[k][r2][i]-d_r3[k][i]);
			}
		}
		return v;
	}
	
	private double[][] crossover(int i,boolean s_gt,boolean in_s) {
		double[][] v = mutation(i,s_gt,in_s);
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
				if(u[n][k]<x_l[k]||u[n][k]>x_u[k])u[n][k] = x_l[k]+p*(x_u[k]-x_l[k]);
			}
		}
		return u;
	}
	
	private boolean select(int i,boolean s_gt,boolean in_s) {
		boolean is_replace = false;
		double[][] u = crossover(i,s_gt,in_s);
		double[][] v = new double [train_data_x.length][features_size];
		for(int m=0;m<train_data_x.length;m++) 
			for(int n=0;n<features_size;n++)v[m][n] = population[m][i][n];
		double cost_0 = fitness(v);
		double cost_1 = fitness(u);
		if(cost_1>cost_0) {
			is_replace = true;
			for(int m=0;m<train_data_x.length;m++) 
				for(int n=0;n<features_size;n++)population[m][i][n] = u[m][n];
			if(cost_1>distance) {tar=i;distance = cost_1;}//flag=true;}
		}
		return is_replace;
	}
	
}
