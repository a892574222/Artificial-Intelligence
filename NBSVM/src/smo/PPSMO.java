package smo;


public class PPSMO {
	//SMO的参
	private double[][] data;
	private Integer[] label;
	private double C;
	private double toler;
	private double[][] kernel_mat;
	private double[] alphas;
	private double b;
	private String kernel;
	private Integer positive=null;
	private Integer negative=null;
	private double percent;
	//0<delta<0.5
	private double delta;
	private double[]probability;
	
	public PPSMO(double[][]data,Integer[] label,Double C,Double toler,double percent,double delta, String kernel) {
		this.data = data;
		this.label = label;
		turn_label();
		this.probability =new double[data.length];
		this.percent = percent;
		this.delta = delta;
		get_probability();
		this.C = C;
		this.toler = toler;
		this.kernel = kernel;
		this.kernel_mat = new double [data.length][data.length];
		for(int i=0;i<data.length;i++)for(int j=0;j<data.length;j++)kernel_mat[i][j] = kernel(data[i],data[j],kernel);
		this.alphas = new double[data.length];
		this.b=0;
		start();
	}
	
	
	private void turn_label() {
		Integer p;
		for(int i=0;i<label.length;i++) {
			if(negative==null)negative=label[i];
			if(label[i]!=negative) {positive=label[i];break;}
		}
		if(negative>positive) {p=positive;positive=negative;negative=p;}
		for(int i=0;i<label.length;i++) {
			if(label[i]==negative)label[i]=-1;
			else label[i]=1;
		}
	}
	
	//计算样本属于+类的后验概率
	private void get_probability() {
		double sum=0;
		double positive_sum;
		double max_distance=0;
		double[][] distance = new double[data.length][data.length];
		double[] class_probability = new double[2];
		double[] sub = new double[data[0].length];
		double now_distance;
		double positive_class_conditional_probability;
		for(int i=0;i<label.length;i++) {
			for(int j=i;j<label.length;j++) {
				for(int p=0;p<data[i].length;p++) {
					sub[p] = data[i][p]-data[j][p];
				}
				now_distance = Math.sqrt(calculate(sub));
				distance[i][j] = now_distance;
				distance[j][i] = now_distance;
				if(now_distance>max_distance)max_distance = now_distance;
			}
			if(label[i]==1)sum++;
		}
		max_distance*=percent;
		class_probability[0] = sum/label.length;
		class_probability[1] = (label.length-sum)/label.length;
		for(int i=0;i<label.length;i++) {
			sum=0;
			positive_sum=0;
			for(int j=0;j<label.length;j++) {
				if(distance[i][j]<=max_distance) {
					sum++;
					if(label[j]==1)positive_sum++;
				}
			}
			positive_class_conditional_probability = positive_sum/sum;
			probability[i] = positive_class_conditional_probability*class_probability[0]/(positive_class_conditional_probability*class_probability[0]+class_probability[1]*(sum-positive_sum)/sum);
			if(probability[i]>0.5&&label[i]==0)probability[i]=0.5-delta;
			if(probability[i]<0.5&&label[i]==1)probability[i]=0.5+delta;
		}
		for(int i=0;i<probability.length;i++) {
			probability[i] = 2*probability[i]-1;
		}
		
	}
	
	//计算误差
	private double calEi(int j) {
		double p = 0;
		for(int i=0;i<data.length;i++)p += alphas[i]*probability[i]*kernel_mat[i][j];
		p=p+b-probability[j];
		return p;
	}
	
	private double clipalpha(int a1, int a2) {
		double L;
		double H;
		double new_a2;
		double eta;
		eta=(kernel_mat[a1][a1]+kernel_mat[a2][a2]-2*kernel_mat[a1][a2]);
		if(eta==0)return -1;
		new_a2 = alphas[a2]+probability[a2]*(calEi(a1)-calEi(a2))/eta;
		if((probability[a1]>=0&&probability[a2]>=0)||(probability[a1]<0&&probability[a2]<0)) {
			L = Math.max(0, alphas[a2]+alphas[a1]-C);
			H = Math.min(C, alphas[a2]+alphas[a1]);
		}else {
			L = Math.max(0, alphas[a2]-alphas[a1]);
			H = Math.min(C, C+alphas[a2]-alphas[a1]);
		}
		if(new_a2>H)new_a2 = H;
		else if(new_a2<L)new_a2 = L;
		return new_a2;
	}
	
	//选择第二个α,传入第一个α的下标，传出第二个α的下标
	private int get_alpha2(int a1) {
		int p = -1;
		double distance=-1;
		for(int i=0;i<data.length;i++) {
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
		if((alphas[i]<C&&old_E1*probability[i]<-toler) ||(alphas[i]>0&&old_E1*probability[i]>toler)) {
			old_alpha1 = alphas[i];
			alpha2 = get_alpha2(i);
			old_alpha2 = alphas[alpha2];
			old_E2 = calEi(alpha2);
			if(clipalpha(i,alpha2)==-1)return 0;
			alphas[alpha2] = clipalpha(i,alpha2);
			if(Math.abs(alphas[alpha2]-old_alpha2)>=0.00001) {
				alphas[i] = alphas[i]+probability[i]*probability[alpha2]*(old_alpha2-alphas[alpha2]);
				b1 = -old_E1+(old_alpha1-alphas[i])*probability[i]*kernel_mat[i][i]+(old_alpha2-alphas[alpha2])*probability[alpha2]*kernel_mat[alpha2][i]+b;
				b2 = -old_E2+(old_alpha1-alphas[i])*probability[i]*kernel_mat[i][alpha2]+(old_alpha2-alphas[alpha2])*probability[alpha2]*kernel_mat[alpha2][alpha2]+b;
				if(alphas[i]>0&&alphas[i]<C)b=b1;
				else if(alphas[alpha2]>0&&alphas[alpha2]<C)b=b2;
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
		//初始核函数得到的矩阵
		
		while((alphaschanged>0 || flag) && iter<maxiter) {
			//System.out.println(iter);
			alphaschanged=0;
			if(flag==true) {
				for(int i=0;i<data.length;i++) {
					alphaschanged += innerL(i);
					//if(i%100==0)System.out.println("full set loop, iter: "+i+", alphapairschanged: "+alphaschanged+", iterNum:"+iter);
				}
				iter++;
			}else {
				for(int i=0;i<data.length;i++) {
					if(alphas[i]>0&&alphas[i]<C) {
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
		for(int i=0;i<data.length;i++) {
			result +=alphas[i]*probability[i]*kernel(unkonwn,data[i],kernel);
		}
		if(result>0)return 1;
		else return -1;
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
			K=Math.exp(-calculate(x)/x1.length);
		}
		return K;
	}
	
	//计算2-范式的平方
	private double calculate(double[] x1) {
		double result = 0;
		for(int i=0;i<x1.length;i++)result += x1[i]*x1[i];
		return result;
	}
	

}