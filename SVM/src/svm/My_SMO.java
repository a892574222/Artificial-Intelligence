package svm;


public class My_SMO {
	//SMO的参
	private double[][] data;
	private Integer[] label;
	private double C;
	private double toler;
	private double[][] kernel_mat;
	private double[] alphas;
	private double b;
	private String kernel;
	
	public My_SMO(double[][]data,Integer[] label,Double C,Double toler,String kernel) {
		this.data = data;
		this.label = label;
		this.C = C;
		this.toler = toler;
		this.kernel = kernel;
		this.kernel_mat = new double [data.length][data.length];
		for(int i=0;i<data.length;i++)for(int j=0;j<data.length;j++)kernel_mat[i][j] = kernel(data[i],data[j],kernel);
		this.alphas = new double[data.length];
		this.b=0;
		start();
	}
	
	
	//计算误差
	private double calEi(int j) {
		double p = 0;
		for(int i=0;i<data.length;i++)p += alphas[i]*label[i]*kernel_mat[i][j];
		p=p+b-label[j];
		return p;
	}
	
	private double clipalpha(int a1, int a2) {
		double L;
		double H;
		double new_a2;
		double eta;
		eta=(kernel_mat[a1][a1]+kernel_mat[a2][a2]-2*kernel_mat[a1][a2]);
		if(eta==0)return 0;
		new_a2 = alphas[a2]+label[a2]*(calEi(a1)-calEi(a2))/(kernel_mat[a1][a1]+kernel_mat[a2][a2]-2*kernel_mat[a1][a2]);
		if(label[a1]==label[a2]) {
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
			if(Math.abs(clipalpha(a1,i)-alphas[i])>distance) {
				p=i;
				distance = Math.abs(clipalpha(a1,i)-alphas[i]);
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
		if((alphas[i]<C&&old_E1*label[i]<-toler) ||(alphas[i]>0&&old_E1*label[i]>toler)) {
			old_alpha1 = alphas[i];
			alpha2 = get_alpha2(i);
			old_alpha2 = alphas[alpha2];
			old_E2 = calEi(alpha2);
			alphas[alpha2] = clipalpha(i,alpha2);
			if(Math.abs(alphas[alpha2]-old_alpha2)>=0.00001) {
				alphas[i] = alphas[i]+label[i]*label[alpha2]*(old_alpha2-alphas[alpha2]);
				b1 = -old_E1+(old_alpha1-alphas[i])*label[i]*kernel_mat[i][i]+(old_alpha2-alphas[alpha2])*label[alpha2]*kernel_mat[alpha2][i]+b;
				b2 = -old_E2+(old_alpha1-alphas[i])*label[i]*kernel_mat[i][alpha2]+(old_alpha2-alphas[alpha2])*label[alpha2]*kernel_mat[alpha2][alpha2]+b;
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
			result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
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