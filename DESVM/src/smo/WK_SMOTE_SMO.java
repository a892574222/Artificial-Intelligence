package smo;

import java.util.Arrays;
import java.util.Random;

public class WK_SMOTE_SMO {
	//SMO的参
	private double[][] data;
	private Integer[] label;
	private double C1;
	private double C2;
	private double C3;
	private double toler;
	private double[][] kernel_mat;
	private double[] alphas;
	private double b;
	private String kernel;
	private Integer negative=null;
	private Integer positive=null;
	private double[] delta;
	private int[] S_seed;
	private int[] S_neighbor;
	private double sigma;
	
	//C1表+的惩罚，C2表-的惩罚
	public WK_SMOTE_SMO(double[][]data,Integer[] label,Double C,int P,int K,Double toler,String kernel,Double sigma) {
		this.data = data;
		this.label = label;
		this.turn_label(P);
		this.C1 = C;
		this.C2 = C;
		this.C3 = C;
		this.toler = toler;
		this.kernel = kernel;
		this.sigma = sigma;
		this.set_kernel_mat(P,K);
		this.alphas = new double[kernel_mat.length];
		this.b=0;
		start();
	}

	private void turn_label(int P) {
		Integer p;
		int positive_sum=0;
		Integer[] temp_label;
		for(int i=0;i<label.length;i++) {
			if(negative==null)negative=label[i];
			if(label[i]!=negative) {positive=label[i];break;}
		}
		for(int i=0;i<label.length;i++)if(positive==label[i])positive_sum++;
		if(positive_sum>data.length-positive_sum) {p=positive;positive=negative;negative=p;
//		C1=C1*(positive_sum)/data.length;
//		C2=C2*(data.length-positive_sum)/data.length;
//		C3=C3*((positive_sum))/data.length;
		}
		else {
//		C1=C1*(data.length-positive_sum)/data.length;
//		C2=C2*positive_sum/data.length;
//		C3=C3*((data.length-positive_sum)/data.length);
		}
		temp_label = label.clone();
		label = new Integer[data.length+P];
		for(int i=0;i<label.length;i++) {
			if(i<data.length)label[i] = temp_label[i];
			else label[i] = positive;
		}
		for(int i=0;i<label.length;i++) {
			if(label[i]==negative)label[i]=-1;
			else label[i]=1;
		}
//		System.out.println(positive_sum);
//		System.out.println(temp_label.length);
//		System.out.println(label.length);
//		System.out.println("-----------");
	}
	
	private void set_kernel_mat(int P,int K) {
		double[][] mat = new double[data.length][data.length];
		int index=0;
		int[] positive_index;
		int[][] K_neighbor;
		Random random = new Random();
		
		S_seed = new int[P];
		S_neighbor = new int[P];
		for(int i=0;i<data.length;i++)for(int j=0;j<data.length;j++)mat[i][j] = kernel(data[i],data[j],kernel);
		for(int i=0;i<data.length;i++)if(label[i]==1)index++;
		positive_index = new int[index];
		index=0;
		for(int i=0;i<data.length;i++)if(label[i]==1) {positive_index[index]=i;index++;}
		K_neighbor = get_K_neighbor(K,mat);

		for(int i=0;i<P;i++) {
			S_seed[i] = positive_index[random.nextInt(positive_index.length)];
			S_neighbor[i] = K_neighbor[S_seed[i]][random.nextInt(K_neighbor[S_seed[i]].length)];
		}
		kernel_mat = new double[data.length+P][data.length+P];
		delta = new double[P];
		for(int i=0;i<P;i++)delta[i] = random.nextDouble();
		for(int i=0;i<kernel_mat.length;i++) {
			for(int j=0;j<kernel_mat.length;j++) {
				if(i<data.length) {
					if(j<data.length)kernel_mat[i][j] = kernel(data[i],data[j],kernel);
					else {
						kernel_mat[i][j] = (1-delta[j-data.length])*kernel(data[i],data[S_seed[j-data.length]],kernel)+delta[j-data.length]*kernel(data[i],data[S_neighbor[j-data.length]],kernel);
					}
				}else {
					if(j<data.length) {
						kernel_mat[i][j] = (1-delta[i-data.length])*kernel(data[S_seed[i-data.length]],data[j],kernel)+delta[i-data.length]*kernel(data[S_neighbor[i-data.length]],data[j],kernel);
					}else {
						kernel_mat[i][j] = (1-delta[i-data.length])*(1-delta[j-data.length])*kernel(data[S_seed[i-data.length]],data[S_seed[j-data.length]],kernel)
						+(1-delta[i-data.length])*delta[j-data.length]*kernel(data[S_neighbor[j-data.length]],data[S_seed[i-data.length]],kernel)
						+delta[i-data.length]*(1-delta[j-data.length])*kernel(data[S_seed[j-data.length]],data[S_neighbor[i-data.length]],kernel)
						+delta[i-data.length]*delta[j-data.length]*kernel(data[S_neighbor[i-data.length]],data[S_neighbor[j-data.length]],kernel);
						
					}
				}
			}
		}
	}
	
	private int[][] get_K_neighbor(int K,double[][] mat){
		int[][] result = new int[data.length][];
		double[][] k_distance = new double[data.length][];
		double distance;
		double temp_distance;
		int temp_index;
		int sum_k;
		int now_k;
		
		for(int i=0;i<data.length;i++) {
			sum_k=0;
			now_k=0;
			for(int j=0;j<data.length;j++) if(label[i]==label[j])now_k++;
			if(now_k>K) now_k = K;
			result[i] = new int[now_k];
			k_distance[i] = new double[now_k];
			for(int j=0;j<data.length;j++) {
				if(i==j||label[i]!=label[j])continue;
				distance = mat[i][i]-2*mat[i][j]+mat[j][j];
				if(sum_k<now_k) {
					k_distance[i][sum_k] = distance;
					result[i][sum_k] = j;
					sum_k++;
				}else {
					if(sum_k==now_k) {
						for(int p=0;p<now_k-1;p++) {
							for(int q=now_k-1;q>p;q--) {
								if(k_distance[i][q]<k_distance[i][q-1]) {
									temp_distance = k_distance[i][q-1];
									k_distance[i][q-1] = k_distance[i][q];
									k_distance[i][q] = temp_distance;
									temp_index = result[i][q-1];
									result[i][q-1] = result[i][q];
									result[i][q] = temp_index;
								}
							}
						}
						sum_k++;
					}
					if(distance<k_distance[i][now_k-1]) {
						for(int p=0;p<now_k;p++) {
							if(distance<k_distance[i][p]) {
								for(int q=now_k-1;q>p;q--) {
									k_distance[i][q] = k_distance[i][q-1];
									result[i][q] = result[i][q-1];
								}
								k_distance[i][p] = distance;
								result[i][p] = j;
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	
	//计算误差
	private double calEi(int j) {
		double p = 0;
		for(int i=0;i<kernel_mat.length;i++)p += alphas[i]*label[i]*kernel_mat[i][j];
		p=p+b-label[j];
		return p;
	}
	
	private double clipalpha(int a1, int a2 ,double C_a1, double C_a2) {
		double L;
		double H;
		double new_a2;
		double eta;
		eta=(kernel_mat[a1][a1]+kernel_mat[a2][a2]-2*kernel_mat[a1][a2]);
		if(eta==0)return -1;
		new_a2 = alphas[a2]+label[a2]*(calEi(a1)-calEi(a2))/eta;
		if(label[a2]==label[a1]) {
			L = Math.max(0, alphas[a2]+alphas[a1]-C_a1);
			H = Math.min(C_a2, alphas[a2]+alphas[a1]);
		}else {
			L = Math.max(0, alphas[a2]-alphas[a1]);
			H = Math.min(C_a2, C_a1+alphas[a2]-alphas[a1]);
		}
		if(new_a2>H)new_a2 = H;
		else if(new_a2<L)new_a2 = L;
		return new_a2;
	}
	
	//选择第二个α,传入第一个α的下标，传出第二个α的下标
	private int get_alpha2(int a1) {
		int p = -1;
		double distance=-1;
		for(int i=0;i<kernel_mat.length;i++) {
			if(i==a1)continue;
			if(Math.abs(calEi(i)-calEi(a1))>distance) {
				p=i;
				distance = Math.abs(calEi(i)-calEi(a1));
				}
		}
		return p;
	}
	
	private int innerL(int i) {
		double old_alpha1;
		double old_alpha2;
		int alpha2;
		double b1,b2;
		double old_E1;
		double old_E2;
		old_E1 = calEi(i);
		double Ci,Cj;
		if(i>=data.length)Ci=C3;
		else if(label[i]==1)Ci=C1;
		else Ci=C2;
		if((alphas[i]<Ci&&old_E1*label[i]<-toler) ||(alphas[i]>0&&old_E1*label[i]>toler)) {
			old_alpha1 = alphas[i];
			alpha2 = get_alpha2(i);
			old_alpha2 = alphas[alpha2];
			old_E2 = calEi(alpha2);
			if(alpha2>=data.length)Cj=C3;
			else if(label[i]==1)Cj=C1;
			else Cj=C2;
			if(clipalpha(i,alpha2,Ci,Cj)==-1)return 0;
			alphas[alpha2] = clipalpha(i,alpha2,Ci,Cj);
			if(Math.abs(alphas[alpha2]-old_alpha2)>=0.00001) {
				alphas[i] = alphas[i]+label[i]*label[alpha2]*(old_alpha2-alphas[alpha2]);
				b1 = -old_E1+(old_alpha1-alphas[i])*label[i]*kernel_mat[i][i]+(old_alpha2-alphas[alpha2])*label[alpha2]*kernel_mat[alpha2][i]+b;
				b2 = -old_E2+(old_alpha1-alphas[i])*label[i]*kernel_mat[i][alpha2]+(old_alpha2-alphas[alpha2])*label[alpha2]*kernel_mat[alpha2][alpha2]+b;
				if(alphas[i]>0&&alphas[i]<Ci)b=b1;
				else if(alphas[alpha2]>0&&alphas[alpha2]<Cj)b=b2;
				else b=(b1+b2)/2.0;
				return 1;
			}
			return 0;
		}
		return 0;
	}
	
	
	//运行smo算法，使得α与b成为最终解
	private void start() {
		
		int iter=0;
		int maxiter = 4000;
		boolean flag = true;
		double alphaschanged = 0;
		double Ci;
		//初始核函数得到的矩阵
		
		while((alphaschanged>0 || flag) && iter<maxiter) {
			//System.out.println(iter);
			alphaschanged=0;
			if(flag==true) {
				for(int i=0;i<kernel_mat.length;i++) {
					alphaschanged += innerL(i);
					//if(i%100==0)System.out.println("full set loop, iter: "+i+", alphapairschanged: "+alphaschanged+", iterNum:"+iter);
				}
				iter++;
			}else {
				for(int i=0;i<kernel_mat.length;i++) {
					if(i>=data.length)Ci=C3;
					else if(label[i]==1)Ci=C1;
					else Ci=C2;
					if(alphas[i]>0&&alphas[i]<Ci) {
						alphaschanged += innerL(i);
						//if(i%100==0)System.out.println("non-bound set loop: "+i+", alphapairschanged: "+alphaschanged+", iterNum:"+iter);
					}
				}
				iter++;
			}
			if(flag==true)flag = false;
			else if(alphaschanged==0) flag = true;
		}
		//System.out.println(Arrays.toString(alphas));
		//System.out.println(b);
	}


	//从外输入特征进行预测,这里还没有确定如果落在超平面上怎么分类,暂定属于-1这一类;
	public int predict(double[] unkonwn) {
		double result=b;
		for(int i=0;i<kernel_mat.length;i++) {
			if(i<data.length)result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
			else result+=alphas[i]*((1-delta[i-data.length])*kernel(unkonwn,data[S_seed[i-data.length]],kernel)+delta[i-data.length]*kernel(unkonwn,data[S_neighbor[i-data.length]],kernel));
		}
		if(result>0)return positive;
		else return negative;
	}
	
	public double get_distance(double[] unkonwn) {
		double result=b;
		for(int i=0;i<kernel_mat.length;i++) {
			if(i<data.length)result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
			else result+=alphas[i]*((1-delta[i-data.length])*kernel(unkonwn,data[S_seed[i-data.length]],kernel)+delta[i-data.length]*kernel(unkonwn,data[S_neighbor[i-data.length]],kernel));
		}
		return result;
	}
	
	//列向量与行向量的矩阵乘积
	private double matrix_multiply(double[] x,double[] y) {
		//存储矩阵乘法的结果
		double p = 0.0;
		for(int i=0;i<x.length;i++)p+=x[i]*y[i];
		return p;
	}
	
    //核函数,默认gamma为特征数分之一
	private double kernel(double[] x1,double[] x2,String k) {
		//定义核函数的结果
		double K=0.0;
		//线性内核
		if(k.equals("line")){
			K = matrix_multiply(x1,x2);
		}
		//高斯核，目前以默认gamma=1/样本特征
		else if(k.equals("rbf")) {
			double[] x = new double[x1.length]; 
			for(int i=0;i<x.length;i++)x[i] = x1[i]-x2[i];
			K=Math.exp(-calculate(x)/(2*sigma*sigma));
			//2*sigma*sigma,x1.length
		}
//		else if(k.equals("poly")) {
//			K = matrix_multiply(x1,x2);
//			K = Math.pow((K+1), d);
//		}
		return K;
	}
	
	//计算2-范式的平方
	private double calculate(double[] x1) {
		double result = 0;
		for(int i=0;i<x1.length;i++)result += x1[i]*x1[i];
		return result;
	}
	
	public Integer[] get_label(){
		Integer[] result = new Integer[2];
		result[0] = negative;
		result[1] = positive;
		return result;
	}
	

}