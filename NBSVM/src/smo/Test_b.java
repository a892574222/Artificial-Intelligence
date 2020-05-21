package smo;

import java.util.Arrays;
import java.util.Vector;

public class Test_b {
	public static void main(String[] args) {
		Vector<Integer> a = new Vector<Integer>();
		Vector<Vector<Integer>> aa = new Vector<Vector<Integer>>();
		Vector<Vector<Vector<Integer>>> aaa = new Vector<Vector<Vector<Integer>>>();
		Vector<Vector<Vector<Integer>>> b = new Vector<Vector<Vector<Integer>>>();
		int[] c=new int[3];
		int[] d;
		a.add(2);
		aa.add(a);
		aaa.add(aa);
		b.add(aaa.get(0));
		b.remove(0);
		d=c.clone();
		d[0]=123;
		System.out.println(aaa);
		System.out.println(b);
		System.out.println(Arrays.toString(c));
		System.out.println(Arrays.toString(d));
	}
}
