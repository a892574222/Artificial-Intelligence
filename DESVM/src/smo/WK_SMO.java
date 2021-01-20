package smo;

import java.util.Random;

public class WK_SMO extends Based_SMO{
	
	private int[] S_seed;
	private int[] S_neighbor;
	private double[] delta;
	private int P;
	private int K;
	private double C;
	private boolean unequal;
	
	public WK_SMO(double[][]data, Integer[] label, double C,int P,int K, double toler, String kernel, double sigma,boolean unequal) {
		super(data,label,toler,C,C,1.0,1.0,kernel,sigma,0);
		this.P = P;
		this.K = K;
		this.C = C;
		this.unequal = unequal;
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
	}
	
	protected void turn_label() {
		Integer p;
		double positive_sum=0;
		double[] temp_label;
		for(int i=0;i<label.length;i++) {
			if(negative==null)negative=(int)label[i];
			if(label[i]!=negative) {positive=(int)label[i];break;}
		}
		for(int i=0;i<label.length;i++)if(positive==label[i])positive_sum++;
		if(positive_sum>data.length-positive_sum) {
			p=positive;positive=negative;negative=p;
			if(unequal) {
				C1=C*data.length/(data.length-positive_sum);
				C2=C*data.length/positive_sum;
			}
		}
		else {
			if(unequal) {
				C1=C*data.length/positive_sum;
				C2=C*data.length/(data.length-positive_sum);
			}
		}
		temp_label = label.clone();
		label = new double[data.length+P];
		alphas = new double[label.length];
		Gi = new double[label.length];
		upMask = new int[label.length];
		downMask = new int[label.length];
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
	
	protected void createKernelCache() {
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
		fullKernel = new double[data.length+P][data.length+P];
		delta = new double[P];
		for(int i=0;i<P;i++)delta[i] = random.nextDouble();
		for(int i=0;i<fullKernel.length;i++) {
			for(int j=0;j<fullKernel.length;j++) {
				if(i<data.length) {
					if(j<data.length)fullKernel[i][j] = kernel(data[i],data[j],kernel);
					else {
						fullKernel[i][j] = (1-delta[j-data.length])*kernel(data[i],data[S_seed[j-data.length]],kernel)+delta[j-data.length]*kernel(data[i],data[S_neighbor[j-data.length]],kernel);
					}
				}else {
					if(j<data.length) {
						fullKernel[i][j] = (1-delta[i-data.length])*kernel(data[S_seed[i-data.length]],data[j],kernel)+delta[i-data.length]*kernel(data[S_neighbor[i-data.length]],data[j],kernel);
					}else {
						fullKernel[i][j] = (1-delta[i-data.length])*(1-delta[j-data.length])*kernel(data[S_seed[i-data.length]],data[S_seed[j-data.length]],kernel)
						+(1-delta[i-data.length])*delta[j-data.length]*kernel(data[S_neighbor[j-data.length]],data[S_seed[i-data.length]],kernel)
						+delta[i-data.length]*(1-delta[j-data.length])*kernel(data[S_seed[j-data.length]],data[S_neighbor[i-data.length]],kernel)
						+delta[i-data.length]*delta[j-data.length]*kernel(data[S_neighbor[i-data.length]],data[S_neighbor[j-data.length]],kernel);
						
					}
				}
			}
		}
	}
	
	//从外输入特征进行预测,这里还没有确定如果落在超平面上怎么分类,暂定属于-1这一类;
	public int predict(double[] unkonwn) {
		double result=b;
		for(int i=0;i<label.length;i++) {
			if(i<data.length)result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
			else result+=alphas[i]*((1-delta[i-data.length])*kernel(unkonwn,data[S_seed[i-data.length]],kernel)+delta[i-data.length]*kernel(unkonwn,data[S_neighbor[i-data.length]],kernel));
		}
		if(result>0)return positive;
		else return negative;
	}
}
