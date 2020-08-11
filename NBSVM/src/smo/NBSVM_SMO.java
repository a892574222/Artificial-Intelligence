package smo;

import java.util.Arrays;

//自定义类，相当于结构体
class Node{
	int idx;
	double f;
	public Node(int idx,double f) {
		this.idx=idx;
		this.f=f;
	}
} 


public class NBSVM_SMO {
	//SMO的参
	private double[][] data;
	private Integer[] label;
	private double C1;
	private double C2;
	//kkt条件的误差容忍
	private double tolKKT = 1e-3;
	//选择α时候允许的误差
	private double svTol=1.4901*Math.pow(10, -8);
	private double eps=svTol*svTol;
	//允许违反kkt条件的α个数
	private int acceptedKKTviolations;
	//最大迭代次数
	private int maxiter = 150000;
	private int KernelCacheLimit=5000;
	private double[][] fullKernel=null;
	private double[] kernelDiag;
	private double[] alphas;
	private double[] Gi;
	//boxconstraint是上下界，P表unequal cost，都是从外界输入，这里认为下标0都表示正样本
	private double[] boxconstraint = new double[2];
	private double b=0;
	private String kernel;
	private double w1,w2;
	private int[] upMask;
	private int[] downMask;
	
	//createKernelCache相关参数
	private int s;
	private int[] leastIndices;
	private double[][] subKernel;
	private int[] subKernelIndices;
	
	//rfb的gamma
	private double sigma;
	private int d;
	//针对样本结果分类不为-1和1，记录原始类别
	private Integer positive=null;
	private Integer negative=null;
	private double positive_number=0;
	double[] parameters;
	
	//KKTviolationsLevel 允许违反kkt条件α数的百分比 例子 0.05表示百分之五
	//unbsvm, 目标是负类，w1和w2表示少数类和多数类的平衡参数,C1和C2表示少数类和多数类的惩罚系数
	public NBSVM_SMO(double[][]data,Integer[] label,double KKTviolationsLevel,double C1,double C2,double w1,double w2, String kernel,double gamma,int d) {
		this.data = data;
		this.label = label.clone();
		this.C1 = C1;
		this.C2 = C2;
		this.w1 = w1;
		this.w2 = w2;
		this.acceptedKKTviolations = (int)KKTviolationsLevel*label.length;
		this.kernel = kernel;
		this.alphas = new double[data.length];
		this.Gi = new double[data.length];
		this.upMask = new int[data.length];
		this.downMask = new int[data.length];
		this.sigma = gamma;
		this.d=d;
		turn_label();
		createKernelCache();
		SetBoxAndP();
		start();
		this.parameters= calculate_parameters();
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
	
	//设置boxconstraint和P以及Gi
	private void SetBoxAndP() {
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
		//System.out.println("P:"+Arrays.toString(P));
	}
	

	//运行smo算法，使得α与b成为最终解
	private void start() {
		//System.out.println(Arrays.toString(Gi));
		int iter=0;
		int idx1;
		double val1=0;
		Node alpha2;
		int kktViolationCount;
		int[] flags;
		//这里寻找idx1,在每次循环开始的时候要置idx=-1,表示要找第一个满足条件的α1下标;
		while(iter<maxiter) {
			idx1=-1;
			for(int i=0;i<upMask.length;i++) {
				if(upMask[i]==1) {
					if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
					else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
				}
			}
			if(idx1==-1)break;
			alpha2=getMaxGain(idx1);
			if(alpha2.idx==-1) {
				for(int i=0;i<downMask.length;i++) {
					if(downMask[i]==1) {
						if(alpha2.idx==-1) {alpha2.idx=i;alpha2.f=label[i]*Gi[i];}
						else if(label[i]*Gi[i]<alpha2.f) {alpha2.idx=i;alpha2.f=label[i]*Gi[i];}
					}
				}
			}
			
			if(val1-alpha2.f<=tolKKT) {
				b=(val1+alpha2.f)/2;
				break;
			}
			updateAlphas(idx1, alpha2.idx);
			if(iter%500==0) {
				idx1=-1;
				for(int i=0;i<upMask.length;i++) {
					if(upMask[i]==1) {
						if(idx1==-1) {idx1=i;val1=label[i]*Gi[i];}
						else if(label[i]*Gi[i]>val1) {idx1=i;val1=label[i]*Gi[i];}
					}
				}
				alpha2.idx=-1;
				if(alpha2.idx==-1) {
					for(int i=0;i<downMask.length;i++) {
						if(downMask[i]==1) {
							if(alpha2.idx==-1) {alpha2.idx=i;alpha2.f=label[i]*Gi[i];}
							else if(label[i]*Gi[i]<alpha2.f) {alpha2.idx=i;alpha2.f=label[i]*Gi[i];}
						}
					}
				}
				b=(val1+alpha2.f)/2;
				flags=checkKKT();
				kktViolationCount=0;
				for(int i=0;i<flags.length;i++)if(flags[i]==0)kktViolationCount +=1;
				if(acceptedKKTviolations>0&& kktViolationCount<=acceptedKKTviolations)break;
			}
			iter++;
		}
	}
	//根据α1获得α2
	private Node getMaxGain(int idx1) {
		int idx2=0;
		double val2;
		int p=0;
		int[] mask = new int [downMask.length];
		double val1 = label[idx1]*Gi[idx1];
		for(int i=0;i<mask.length;i++)if((downMask[i]==1)&&(label[i]*Gi[i]<val1)) {mask[i]=1;p++;}
		double[] gainNumerator = new double[p];
		double[] gainDenominator = new double[p];
		int[] idx = new int[p];
		double max,flag;
		double[] kerDiag = getKernelDiag();
		p=0;
		for(int i=0;i<mask.length;i++) {
			if(mask[i]==1) {
				gainNumerator[p] = (label[i]*Gi[i]-val1)*(label[i]*Gi[i]-val1);
				idx[p] = i;
				p++;	
				}
			}
		p=0;
		if(fullKernel==null) {
			double[] kerCol1;
			kerCol1=getColumn(idx1);
			for(int i=0;i<mask.length;i++) {
				if(mask[i]==1) {
					gainDenominator[p] = -4*kerCol1[i]+2*kerDiag[i]+2*kerDiag[idx1];
					p++;	
					}
				}
		}
		else {
			for(int i=0;i<mask.length;i++) {
				if(mask[i]==1) {
					gainDenominator[p] = -4*fullKernel[i][idx1]+2*kerDiag[i]+2*kerDiag[idx1];
					p++;
					}
				}
		}
		if(p==0)return new Node(-1,-1);
		max=gainNumerator[0]/gainDenominator[0];
		for(int i=1;i<gainNumerator.length;i++) {
			flag=gainNumerator[i]/gainDenominator[i];
			if(flag>max) {max=flag;idx2=i;}
		}
		idx2=idx[idx2];
		val2=label[idx2]*Gi[idx2];
		
		return new Node(idx2,val2);
	}
	
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
		int s;
		double fi;
		double fj;
		double Li;
		double Hi;
		double idx1_boxconstraint;
		double idx2_boxconstraint;
		if(label[idx1]==1)idx1_boxconstraint = boxconstraint[0];
		else idx1_boxconstraint = boxconstraint[1];
		if(label[idx2]==1)idx2_boxconstraint = boxconstraint[0];
		else idx2_boxconstraint = boxconstraint[1];
		if(fullKernel==null) {
			double[] K = getElements(idx1,idx2);
			eta=K[0]+K[1]-2*K[2];
		}
		else eta=fullKernel[idx1][idx1]+fullKernel[idx2][idx2]-2*fullKernel[idx1][idx2];
		if(label[idx1]==label[idx2]) {
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
		}
		else {
			double[] K = getElements(idx1,idx2);
			s=label[idx1]*label[idx2];
			fi=-Gi[idx1]-alphas[idx1]*K[0]-s*alphas[idx2]*K[1];
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
		if(fullKernel==null) {
			double[] kerCol1;
			double[] kerCol2;
			double idx1_change=alpha_i - alphas[idx1];
			double idx2_change=alpha_j - alphas[idx2];
			kerCol1=getColumn(idx1);
			kerCol2=getColumn(idx2);
			for(int i=0;i<Gi.length;i++)Gi[i] = Gi[i]-(kerCol1[i]*label[i])*label[idx1]*idx1_change-(kerCol2[i]*label[i])*idx2_change*label[idx2];
		}
		else {
			double[] kerCol1 = new double[fullKernel.length];
			double[] kerCol2 = new double[fullKernel.length];
			double idx1_change=alpha_i - alphas[idx1];
			double idx2_change=alpha_j - alphas[idx2];
			for(int i=0;i<fullKernel.length;i++) {kerCol1[i]=fullKernel[i][idx1];kerCol2[i]=fullKernel[i][idx2];}
			for(int i=0;i<Gi.length;i++)Gi[i] = Gi[i]-(kerCol1[i]*label[i])*label[idx1]*idx1_change-(kerCol2[i]*label[i])*idx2_change*label[idx2];
			//System.out.println(Arrays.toString(Gi));
		}
		if(label[idx1]==1) {
			if(label[idx1]*alpha_i<idx1_boxconstraint-svTol)upMask[idx1]=1;else upMask[idx1]=0;
			if(label[idx1]*alpha_i>svTol)downMask[idx1]=1;else downMask[idx1]=0;
		}
		else {
			if(label[idx1]*alpha_i<-svTol)upMask[idx1]=1;else upMask[idx1]=0;
			if(label[idx1]*alpha_i>svTol-idx1_boxconstraint)downMask[idx1]=1;else downMask[idx1]=0;
		}
		if(label[idx2]==1) {
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
		double[] amount = new double[data.length];
		int[] flags = new int[data.length];
		double boxConstraint;
		for(int i=0;i<amount.length;i++)amount[i] = label[i]*b-Gi[i];
		for(int i=0;i<alphas.length;i++) {
			if(label[i]==1)boxConstraint=boxconstraint[0];else boxConstraint=boxconstraint[1];
			if(alphas[i]>svTol && (alphas[i]<boxConstraint-svTol))
				if(Math.abs(amount[i])<tolKKT)flags[i]=1;
		}
		for(int i=0;i<alphas.length;i++) {
			if(alphas[i]<svTol)
				if(amount[i]>-tolKKT)flags[i]=1;
		}
		for(int i=0;i<alphas.length;i++) {
			if(label[i]==1)boxConstraint=boxconstraint[0];else boxConstraint=boxconstraint[1];
			if(boxConstraint-alphas[i]<svTol)
				if(amount[i]<=tolKKT)flags[i]=1;
		}
		return flags;
	}
	
	private void createKernelCache() {
		if(data.length<=KernelCacheLimit) {
			this.fullKernel = new double [data.length][data.length];
			for(int i=0;i<data.length;i++)for(int j=0;j<data.length;j++)fullKernel[i][j] = kernel(data[i],data[j],kernel);
		}
		else {
			this.kernelDiag = new double[data.length];
			for(int i=0;i<data.length;i++)kernelDiag[i] = kernel(data[i],data[i],kernel);
			s=(int)Math.max(1, Math.floor(KernelCacheLimit*KernelCacheLimit/data.length));
			leastIndices = new int[s];
			subKernel = new double [data.length][s];
			subKernelIndices = new int [data.length];
		}
	}
	
	private int loadColumn(int idx) {
		int deleteIndex = leastIndices[s];
		int subKernelIndex;
		int max_subKernelIndices=subKernelIndices[0];
		if(deleteIndex==0) {
			for(int i=0;i<subKernelIndices.length;i++)if(subKernelIndices[i]>max_subKernelIndices)max_subKernelIndices=subKernelIndices[i];
			subKernelIndex = 1+max_subKernelIndices;
		}
		else {
			subKernelIndex = subKernelIndices[deleteIndex];
			subKernelIndices[deleteIndex] = 0;
		}
		subKernelIndices[idx] = subKernelIndex;
		for(int i=0;i<data.length;i++)subKernel[i][subKernelIndex] = kernel(data[i],data[idx],kernel);
		for(int i=1;i<leastIndices.length;i++)leastIndices[i] = leastIndices[i-1];
		leastIndices[0] = idx;
		return subKernelIndex;
	}
	
	private double[] getElements(int i,int j) {
		double Kii,Kjj,Kij;
		double[] result = new double[3];
		if(fullKernel!=null) {
			Kii = fullKernel[i][i];
			Kjj = fullKernel[j][j];
			Kij = fullKernel[i][j];
		}
		else {
			Kii = kernelDiag[i];
			Kjj = kernelDiag[j];
			if (subKernelIndices[i]>0)Kij = subKernel[j][ subKernelIndices[i]];
			else if(subKernelIndices[j]>0)Kij = subKernel[i][subKernelIndices[j]];
			else Kij = subKernel[i][loadColumn(j)];
		}
		result[0]=Kii;
		result[1]=Kjj;
		result[2]=Kij;
		return result;
	}
	
	private double[] getColumn(int colIdx) {
		double[] kerCol;
		if(fullKernel!=null) {
			kerCol = new double[fullKernel.length];
			for(int i=0;i<fullKernel.length;i++)kerCol[i] = fullKernel[i][colIdx];
		}
		else{
			kerCol = new double[subKernel.length];
			if(subKernelIndices[colIdx] == 0) {
			int p = loadColumn(colIdx);
			for(int i=0;i<fullKernel.length;i++)kerCol[i] = subKernel[i][p];
			}
			else {
			int p = subKernelIndices[colIdx];
			for(int i=0;i<fullKernel.length;i++)kerCol[i] = subKernel[i][p];
			}
		}
		return kerCol;
	}
	
	private double[] getKernelDiag() {
		double[] ret = new double[fullKernel.length];
		if(fullKernel!=null) {
			for(int i=0;i<fullKernel.length;i++)ret[i] = fullKernel[i][i];
		}
		else {
			ret = kernelDiag;
		}
		return ret;
	}
	
	//从外输入特征进行预测,这里还没有确定如果落在超平面上怎么分类,暂定属于-1这一类;
	public int predict(double[] unkonwn) {
		double result=b;
		for(int i=0;i<data.length;i++) {
			result +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
		}
		if(result>0)return positive;
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
		else if(k.equals("poly")) {
			K = matrix_multiply(x1,x2);
			K = Math.pow((K+1), d);
		}
		return K;
	}
	
	//计算2-范式的平方
	private double calculate(double[] x1) {
		double result = 0;
		for(int i=0;i<x1.length;i++)result += x1[i]*x1[i];
		return result;
	}
	//获得支持向量的个数
	public int getNumber() {
		int sum=0;
		double result;
		double[] temp = new double[data.length];
		int kktViolationCount;
		int[] flags;
		for(int i=0;i<data.length;i++) {
			result=label[i]*b-Gi[i];
			temp[i]=result;
			if(result<=tolKKT)sum++;
		}
//		if(sum==0) {
//			flags=checkKKT();
//			kktViolationCount=0;
//			for(int i=0;i<flags.length;i++)if(flags[i]==0)kktViolationCount +=1;
//			System.out.println(sum);
//			System.out.println(kktViolationCount);
//			System.out.println(svTol);
//			System.out.println(Arrays.toString(flags));
//			System.out.println(Arrays.toString(alphas));
//			System.out.println(Arrays.toString(temp));
//			System.out.println(Arrays.toString(upMask));
//			System.out.println("-------------");
//			}
		if(sum==0)sum=data.length;
		return sum;
	}
	
	//获取该二分类所对应的标签
	public Integer[] get_label() {
		Integer[] result=new Integer[2];
		result[0] = negative;
		result[1] = positive;
		return result;
	}
	
	//计算sigmoid函数的A,B参数
	private double[] calculate_parameters() {
		double maxiter=100;
		double minstep=1e-10;
		double sigma=1e-12;
		double h11,h22,h21,g1,g2,p,q,d1,d2,det,dA,dB,gd,stepsize,newA,newB,newf;
		double result;
		double deci[] = new double[data.length];
		double negative_num=data.length-positive_number;
		double hiTarget=(positive_number+1.0)/(positive_number+2.0);
		double loTarget=1/(negative_num+2.0);
		double t[]=new double[data.length];
		double A=0,fval=0,fApB;
		double B=Math.log((negative_num+1.0)/(positive_number+1.0));
		double[]finally_A_B = new double[2];
		for(int i=0;i<data.length;i++) {
			result=b;
			
			for(int j=0;j<data.length;j++) {
				result +=alphas[j]*label[j]*kernel(data[j],data[i],kernel);
			}
			deci[i] = result;
			if(label[i]==1)t[i]=hiTarget;
			else t[i]=loTarget;
			fApB=deci[i]*A+B;
			if(fApB>=0) fval+=t[i]*fApB+Math.log(1+Math.exp(-fApB));
			else fval += (t[i]-1)*fApB+Math.log(1+Math.exp(fApB));
		}
		
		
//		newf=0.0;
//		for(int i=0;i<data.length;i++) {
//			fApB=deci[i]*A+B;
//			if(fApB>=0) newf+=t[i]*fApB+Math.log(1+Math.exp(-fApB));
//			else newf += (t[i]-1)*fApB+Math.log(1+Math.exp(fApB));
//		}
//		System.out.println("newf:"+newf);
		
		for(int it=0;it<maxiter;it++) {
			h11=sigma;
			h22=sigma;
			h21=0.0;
			g1=0.0;
			g2=0.0;
			for(int i=0;i<data.length;i++) {
				fApB=deci[i]*A+B;
				if (fApB >= 0) {p=Math.exp(-fApB)/(1.0+Math.exp(-fApB));q=1.0/(1.0+Math.exp(-fApB));}
				else {p=1.0/(1.0+Math.exp(fApB));q=Math.exp(fApB)/(1.0+Math.exp(fApB));}
			d2=p*q;
			h11 += deci[i]*deci[i]*d2;
			h22 += d2;
			h21 += deci[i]*d2;
			d1=t[i]-p;
			g1 += deci[i]*d1;
			g2 += d1;
			}
			if (Math.abs(g1)<1e-5 && Math.abs(g2)<1e-5)break;
			det=h11*h22-h21*h21;
			dA=-(h22*g1-h21*g2)/det;
			dB=-(-h21*g1+h11*g2)/det;
			gd=g1*dA+g2*dB;
			stepsize=1;
			while (stepsize >= minstep){
				newA=A+stepsize*dA;
				newB=B+stepsize*dB;
				newf=0.0;
				for(int i=0;i<data.length;i++) {
					fApB=deci[i]*newA+newB;
					if(fApB>=0) newf+=t[i]*fApB+Math.log(1+Math.exp(-fApB));
					else newf += (t[i]-1)*fApB+Math.log(1+Math.exp(fApB));
				}
				if (newf<fval+0.0001*stepsize*gd){
					A=newA;
					B=newB;
					fval=newf;
					break;
				}
				else stepsize /= 2.0;
			}
			if (stepsize < minstep) {
				//System.out.println("Line search fails");
				break;
				}
		}
		//System.out.println("Reaching maximum iterations");
		finally_A_B[0]=A;
		finally_A_B[1]=B;
//		System.out.println(A);
//		System.out.println(B);
//		System.out.println("++++++++++");
		return finally_A_B;
	}
	
	//计算属于正类的概率
	public double get_probability(double[] unkonwn) {
		double fApB=b;
		double p;
		double f;
		for(int i=0;i<data.length;i++) {
			fApB +=alphas[i]*label[i]*kernel(unkonwn,data[i],kernel);
		}
		if(fApB>=0) {f=Math.exp(-parameters[0]*fApB-parameters[1]);p=f/(1+f);}
		else {f=Math.exp(parameters[0]*fApB+parameters[1]);p=1/(1+f);}
//		System.out.println(fApB);
//		System.out.println(p);
//		System.out.println("-----------");
		return p;
	}
}
