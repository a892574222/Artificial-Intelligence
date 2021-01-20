package smo;

import java.util.Arrays;

class Node{
	int idx;
	double val;
	public Node(int idx,double val) {
		this.idx = idx;
		this.val = val;
	}
}

public class Based_SMO {
	//SMO的参
	protected double[][] data;
	protected double[] label;
	protected double C1;
	protected double C2;
	//kkt条件的误差容忍
	protected double tolKKT = 1e-3;
	//选择α时候允许的误差
	protected double svTol=1.4901*Math.pow(10, -8);
	protected double eps=svTol*svTol;
	//允许违反kkt条件的α个数
	protected int acceptedKKTviolations;
	//最大迭代次数
	protected int maxiter = 50000;
	protected double[][] fullKernel=null;
	protected double[] kernelDiag;
	protected double[] alphas;
	protected double[] Gi;
	//boxconstraint是上下界，P表unequal cost，都是从外界输入，这里认为下标0都表示正样本
	protected double[] boxconstraint = new double[2];
	protected double b=Float.POSITIVE_INFINITY;
	protected String kernel;
	protected double w1,w2;
	protected int[] upMask;
	protected int[] downMask;
	
	//rfb的gamma
	protected double sigma;
	protected int d;
	//针对样本结果分类不为-1和1，记录原始类别
	protected Integer positive=null;
	protected Integer negative=null;
	protected double positive_number=0;
	protected double[] parameters;
	
	//KKTviolationsLevel 允许违反kkt条件α数的百分比 例子 0.05表示百分之五
	//unbsvm, 目标是负类，w1和w2表示少数类和多数类的平衡参数,C1和C2表示少数类和多数类的惩罚系数
	public Based_SMO(double[][]data,Integer[] label,double KKTviolationsLevel,double C1,double C2,double w1,double w2, String kernel,double sigma,int d) {
		this.data = data;
		this.label = new double[label.length];
		for(int i=0;i<label.length;i++)this.label[i] = label[i];
		this.C1 = C1;
		this.C2 = C2;
		this.w1 = w1;
		this.w2 = w2;
		this.acceptedKKTviolations = (int)KKTviolationsLevel*label.length;
		this.kernel = kernel;
		this.alphas = new double[label.length];
		this.Gi = new double[label.length];
		this.upMask = new int[label.length];
		this.downMask = new int[label.length];
		this.sigma = sigma;
		this.d=d;
//		System.out.println(b);
//		System.out.println(C1);
//		System.out.println(C2);
	}
	
	protected void turn_label() {
		Integer p;
		int positive_sum=0;
		for(int i=0;i<label.length;i++) {
			if(negative==null)negative=(int)label[i];
			if(label[i]!=negative) {positive=(int)label[i];break;}
		}
		for(int i=0;i<label.length;i++)if(positive==label[i])positive_sum++;
		if(positive_sum>label.length-positive_sum) {p=positive;positive=negative;negative=p;}
		for(int i=0;i<label.length;i++) {
			if(label[i]==negative)label[i]=-1;
			else label[i]=1;
		}
	}
	
	//设置boxconstraint和P以及Gi
	protected void SetBoxAndP() {
		for(int i=0;i<label.length;i++) {
			if(label[i]==1) {positive_number++;upMask[i]=1;}
			else downMask[i]=1;
		}
		boxconstraint[0] = C1;
		boxconstraint[1] = C2;
		for(int i=0;i<label.length;i++) {
			if(label[i]==1) Gi[i]=w1;
			else Gi[i]=w2;
		}
//		System.out.println("Gi:"+Arrays.toString(Gi));
	}
	

	//运行smo算法，使得α与b成为最终解
	protected void start() {
		//System.out.println(Arrays.toString(Gi));
		int iter=0;
		int idx1;
		double val1=0;
		int idx2;
		double val2=0;
		int kktViolationCount;
		int[] flags;
		//这里寻找idx1,在每次循环开始的时候要置idx=-1,表示要找第一个满足条件的α1下标;
		while(iter<maxiter) {
			idx1=-1;
			idx2=-1;
			
			for(int i=0;i<upMask.length;i++) {
				if(upMask[i]==1) {
					if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
					else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
				}
			}
			
			for(int i=0;i<downMask.length;i++) {
				if(downMask[i]==1) {
					if(idx2==-1) {idx2=i;val2=label[i]*Gi[i];}
					else if(label[i]*Gi[i]<val2) {idx2=i;val2=label[i]*Gi[i];}
				}
			}
			
			if(idx2==idx1) {
				idx1=-1;
				for(int i=0;i<upMask.length;i++) {
					if(upMask[i]==1) {
						if(i==idx2)continue;
						if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
						else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
					}
				}
			}
			
			if(val1-val2<=tolKKT) {
				b=(val1+val2)/2;
//				System.out.println(Arrays.toString(checkKKT()));
				break;
			}
			if(idx2==-1) {
				System.out.println(Arrays.toString(boxconstraint));
				System.out.println(w1+""+w2);
				System.out.println(Arrays.toString(label));
				System.out.println(Arrays.toString(alphas));
				System.out.println(Arrays.toString(checkKKT()));
				System.out.println(Arrays.toString(downMask));
				System.out.println(Arrays.toString(upMask));
				System.out.println(kernel);
				System.out.println("多项式核"+d);
				System.out.println("高斯核"+sigma);
			}
			updateAlphas(idx1, idx2);
			if(iter%500==0) {
				idx1=-1;
				idx2=-1;
				for(int i=0;i<downMask.length;i++) {
					if(downMask[i]==1) {
						if(idx2==-1) {idx2=i;val2=label[i]*Gi[i];}
						else if(label[i]*Gi[i]<val2) {idx2=i;val2=label[i]*Gi[i];}
					}
				}
				
				
				for(int i=0;i<upMask.length;i++) {
					if(upMask[i]==1) {
						if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
						else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
					}
				}
				if(idx2==idx1) {
					idx1=-1;
					for(int i=0;i<upMask.length;i++) {
						if(upMask[i]==1) {
							if(i==idx2)continue;
							if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
							else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
						}
					}
				}
				b=(val1+val2)/2;
				flags=checkKKT();
				kktViolationCount=0;
				for(int i=0;i<flags.length;i++)if(flags[i]==0)kktViolationCount +=1;
				if(acceptedKKTviolations>0&& kktViolationCount<=acceptedKKTviolations)break;
			}
			iter++;
		}
	}
	//根据α1获得α2
//	private Node getMaxGain(int idx1) {
////		return new Node(-1,-1);
//		int idx2=0;
//		double val2;
//		int p=0;
//		int[] mask = new int [downMask.length];
//		double val1 = label[idx1]*Gi[idx1];
//		for(int i=0;i<mask.length;i++)if((downMask[i]==1)&&(label[i]*Gi[i]<val1)) {mask[i]=1;p++;}
//		double[] gainNumerator = new double[p];
//		double[] gainDenominator = new double[p];
//		int[] idx = new int[p];
//		double max,flag;
//		double[] kerDiag = getKernelDiag();
//		p=0;
//		for(int i=0;i<mask.length;i++) {
//			if(mask[i]==1) {
//				gainNumerator[p] = (label[i]*Gi[i]-val1)*(label[i]*Gi[i]-val1);
//				idx[p] = i;
//				p++;	
//				}
//			}
//		p=0;
//		for(int i=0;i<mask.length;i++) {
//			if(mask[i]==1) {
//				gainDenominator[p] = -4*fullKernel[i][idx1]+2*kerDiag[i]+2*kerDiag[idx1];
//				p++;	
//				}
//			}
//		if(p==0)return new Node(-1,-1);
//		max=gainNumerator[0]/gainDenominator[0];
//		for(int i=1;i<gainNumerator.length;i++) {
//			flag=gainNumerator[i]/gainDenominator[i];
//			if(flag>max) {max=flag;idx2=i;}
//		}
//		idx2=idx[idx2];
//		val2=label[idx2]*Gi[idx2];
//		
//		return new Node(idx2,val2);
//	}
	
	private void updateAlphas(int idx1, int idx2) {
		//System.out.println(Arrays.toString(Gi));
		//更新α对，这里的下标是分类正负样本后的
		double eta;
		double low;
		double high;
		double lambda;
		double alpha_j;
		double alpha_i;
		double psi_l;
		double psi_h;
		double s;
		double fi;
		double fj;
		double Li;
		double Hi;
		double idx1_boxconstraint;
		double idx2_boxconstraint;
		if(label[idx1]>=0)idx1_boxconstraint = boxconstraint[0];
		else idx1_boxconstraint = boxconstraint[1];
		if(label[idx2]>=0)idx2_boxconstraint = boxconstraint[0];
		else idx2_boxconstraint = boxconstraint[1];
		eta=fullKernel[idx1][idx1]+fullKernel[idx2][idx2]-2*fullKernel[idx1][idx2];
		if(label[idx1]*label[idx2]>=0) {
			low = Math.max(0, alphas[idx1]+alphas[idx2]-idx1_boxconstraint);
			high = Math.min(idx2_boxconstraint, alphas[idx1]+alphas[idx2]);
		}else {
			low = Math.max(0, alphas[idx2]-alphas[idx1]);
			high = Math.min(idx2_boxconstraint, idx1_boxconstraint+alphas[idx2]-alphas[idx1]);
		}
		if(eta>eps) {
			lambda = -label[idx1]*Gi[idx1]+label[idx2]*Gi[idx2];
			alpha_j=alphas[idx2]+label[idx2]/eta*lambda;
			if(alpha_j<low)alpha_j=low;
			else if(alpha_j>high)alpha_j=high;
		}else {
			double[] K = getElements(idx1,idx2);
			s=label[idx1]*label[idx2];
			fi=-Gi[idx1]-alphas[idx1]*K[0]-s*alphas[idx2]*K[2];
			fj=-Gi[idx2]-alphas[idx2]*K[1]-s*alphas[idx1]*K[2];
			Li=alphas[idx1]+s*(alphas[idx2]-low);
			Hi=alphas[idx1]+s*(alphas[idx2]-high);
			psi_l=Li*fi+low*fj+Li*Li*K[0]/2+low*low*K[1]/2+s*low*Li*K[2];
			psi_h=Hi*fi+high*fj+Hi*Hi*K[0]/2+high*high*K[1]/2+s*high*Hi*K[2];
			
			if(psi_l<(psi_h-eps))alpha_j=low;
			else if(psi_l>(psi_h+eps))alpha_j=high;
			else alpha_j=alphas[idx2];
		}
		alpha_i = alphas[idx1]+label[idx2]*label[idx1]*(alphas[idx2]-alpha_j);
		if(alpha_i<eps)alpha_i=0;
		else if(alpha_i>(idx1_boxconstraint-eps))alpha_i = idx1_boxconstraint;

		double[] kerCol1;
		double[] kerCol2;
		double idx1_change=alpha_i - alphas[idx1];
		double idx2_change=alpha_j - alphas[idx2];
		kerCol1=getColumn(idx1);
		kerCol2=getColumn(idx2);
		for(int i=0;i<Gi.length;i++)Gi[i] = Gi[i]-(kerCol1[i]*label[i])*label[idx1]*idx1_change-(kerCol2[i]*label[i])*idx2_change*label[idx2];
		if(label[idx1]>=0) {
			if(label[idx1]*alpha_i<idx1_boxconstraint-svTol)upMask[idx1]=1;else upMask[idx1]=0;
			if(label[idx1]*alpha_i>svTol)downMask[idx1]=1;else downMask[idx1]=0;
		}
		else {
			if(label[idx1]*alpha_i<-svTol)upMask[idx1]=1;else upMask[idx1]=0;
			if(label[idx1]*alpha_i>svTol-idx1_boxconstraint)downMask[idx1]=1;else downMask[idx1]=0;
		}
		if(label[idx2]>=0) {
			if(label[idx2]*alpha_j<idx2_boxconstraint-svTol)upMask[idx2]=1;else upMask[idx2]=0;
			if(label[idx2]*alpha_j>svTol)downMask[idx2]=1;else downMask[idx2]=0;
		}
		else {
			if(label[idx2]*alpha_j<-svTol)upMask[idx2]=1;else upMask[idx2]=0;
			if(label[idx2]*alpha_j>svTol-idx2_boxconstraint)downMask[idx2]=1;else downMask[idx2]=0;
		}
		alphas[idx1] = alpha_i;
        alphas[idx2] = alpha_j;
	}
	
	private int[] checkKKT() {
		double[] amount = new double[label.length];
		int[] flags = new int[label.length];
		double boxConstraint;
		for(int i=0;i<amount.length;i++)amount[i] = label[i]*b-Gi[i];
		for(int i=0;i<label.length;i++) {
			if(label[i]>=0)boxConstraint=boxconstraint[0];else boxConstraint=boxconstraint[1];
			if(alphas[i]>svTol && (alphas[i]<boxConstraint-svTol)) {
				if(Math.abs(amount[i])<tolKKT)flags[i]=1;
			}else {
				if(alphas[i]<svTol) {
					if(amount[i]>-tolKKT)flags[i]=1;
				}else {
					if(boxConstraint-alphas[i]<svTol)
						if(amount[i]<=tolKKT)flags[i]=1;
				}
			}
		}
		return flags;
	}
	
	protected void createKernelCache() {
		this.fullKernel = new double [label.length][label.length];
		for(int i=0;i<label.length;i++)for(int j=0;j<label.length;j++)fullKernel[i][j] = kernel(data[i],data[j],kernel);
	}
	
	private double[] getElements(int i,int j) {
		double Kii,Kjj,Kij;
		double[] result = new double[3];
		Kii = fullKernel[i][i];
		Kjj = fullKernel[j][j];
		Kij = fullKernel[i][j];
		result[0]=Kii;
		result[1]=Kjj;
		result[2]=Kij;
		return result;
	}
	
	private double[] getColumn(int colIdx) {
		double[] kerCol;
		kerCol = new double[fullKernel.length];
		for(int i=0;i<fullKernel.length;i++)kerCol[i] = fullKernel[i][colIdx];
		return kerCol;
	}
	
//	private double[] getKernelDiag() {
//		double[] ret = new double[fullKernel.length];
//		for(int i=0;i<fullKernel.length;i++)ret[i] = fullKernel[i][i];
//		return ret;
//	}
	
	//从外输入特征进行预测,这里还没有确定如果落在超平面上怎么分类,暂定属于-1这一类;
	public int predict(double[] unkonwn) {
		double result=b;
		for(int i=0;i<label.length;i++) {
			result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
		}
		if(result>=0)return positive;
		else return negative;
	}
	
	
	//列向量与行向量的矩阵乘积
	private double matrix_multiply(double[] x,double[] y) {
		//存储矩阵乘法的结果
		double p = 0.0;
		for(int i=0;i<x.length;i++)p+=x[i]*y[i];
		return p;
	}
	
    //核函数,默认gamma为特征数分之一
	protected double kernel(double[] x1,double[] x2,String k) {
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
		else if(k.equals("poly")) {
			double temp;
			K=1.0;
			temp = matrix_multiply(x1,x2);
			temp+=1.0;
			for(int i=0;i<d;i++)K = K*temp;
		}
		return K;
	}
	
	//计算2-范式的平方
	protected double calculate(double[] x1) {
		double result = 0;
		for(int i=0;i<x1.length;i++)result += x1[i]*x1[i];
		return result;
	}
	//获得支持向量的个数
	public int getNumber() {
		int sum=0;
		double result;
		double[] temp = new double[label.length];
		int kktViolationCount;
		int[] flags;
		for(int i=0;i<label.length;i++) {
			result=label[i]*b-Gi[i];
			temp[i]=result;
			if(result<=tolKKT)sum++;
		}
		if(sum==0) {
			flags=checkKKT();
			kktViolationCount=0;
			for(int i=0;i<flags.length;i++)if(flags[i]==0)kktViolationCount +=1;
			System.out.println(sum);
			System.out.println(kktViolationCount);
			System.out.println(svTol);
			System.out.println(kernel);
			System.out.println(d);
			System.out.println(Arrays.toString(flags));
			System.out.println(Arrays.toString(alphas));
			System.out.println(Arrays.toString(temp));
			System.out.println(Arrays.toString(upMask));
			System.out.println(Arrays.deepToString(fullKernel));
			System.out.println("-------------");
			}
		if(sum==0)sum=label.length;
		return sum;
	}
	
	//获取该二分类所对应的标签
	public Integer[] get_label() {
		Integer[] result=new Integer[2];
		result[0] = negative;
		result[1] = positive;
		return result;
	}
	
}
