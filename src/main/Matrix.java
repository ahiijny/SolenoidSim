package main;

/** http://en.wikipedia.org/wiki/Rotation_matrix
 */
public class Matrix 
{

	/** Returns the rotation matrix for the specified rotation about
	 * the given unit vector u.
	 * 
	 * @param u
	 * @param theta		angle in radians
	 * @return
	 */
	public static double[][] getRotationMatrix(double[] u, double theta)
	{
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double ux = u[0];
		double uy = u[1];
		double uz = u[2];
		
		double[][] R = new double[3][3];
		
		R[0][0] = cos + ux*ux*(1-cos);
		R[1][0] = uy*ux*(1-cos) + uz*sin;
		R[2][0] = uz*ux*(1-cos) - uy*sin;
		R[0][1] = ux*uy*(1-cos) - uz*sin;
		R[1][1] = cos + uy*uy*(1-cos);
		R[2][1] = uz*uy*(1-cos) + ux*sin;
		R[0][2] = ux*uz*(1-cos)+ uy*sin;
		R[1][2] = uy*uz*(1-cos) - ux*sin;
		R[2][2] = cos + uz*uz*(1-cos);
				
		return R;
	}
	
	public static double[][] getRotationMatrixX(double theta)
	{
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		R[0][0] = 1;
		R[0][1] = 0;
		R[0][2] = 0;
		R[1][0] = 0;
		R[1][1] = cos;
		R[1][2] = -sin;
		R[2][0] = 0;
		R[2][1] = sin;
		R[2][2] = cos;
		
		return R;
	}
	
	public static double[][] getRotationMatrixY(double theta)
	{
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		R[0][0] = cos;
		R[0][1] = 0;
		R[0][2] = sin;
		R[1][0] = 0;
		R[1][1] = 1;
		R[1][2] = 0;
		R[2][0] = -sin;
		R[2][1] = 0;
		R[2][2] = cos;
		
		return R;
	}
	
	public static double[][] getRotationMatrixZ(double theta)
	{
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		R[0][0] = cos;
		R[0][1] = -sin;
		R[0][2] = 0;
		R[1][0] = sin;
		R[1][1] = cos;
		R[1][2] = 0;
		R[2][0] = 0;
		R[2][1] = 0;
		R[2][2] = 1;
		
		return R;
	}
	
	public static double[][] getColumnMatrix(double[] a)
	{
		double[][] X = new double[a.length][1];
		
		for (int i = 0; i < a.length; i++)
				X[i][0] = a[i];
		
		return X;
	}
	
	public static double[] getColumnVector(double[][] X)
	{
		double[] a = new double[X.length];
		
		for (int i = 0; i < X.length; i++)
			a[i] = X[i][0];
		
		return a;
	}
			
	public static double[][] multiply(double[][] A, double[][] B)
	{
		int rA = A.length;
		int rB = B.length;
		int cA = A[0].length;
		int cB = B[0].length;
		
		double[][] C = new double [rA][cB];
		
		for (int i = 0; i < rA; i++)
		{
			for (int j = 0; j < cB; j++)
			{
				C[i][j] = 0;
				
				for (int k = 0; k < rB; k++)
					C[i][j] += A[i][k] * B[k][j];
			}
		}
				
		return C;
	}
	
	public static double[] multiply(double[][] R, double[] a)
	{
		double[][] X = getColumnMatrix(a);
		X = multiply(R, X);
		return getColumnVector(X);
	}
	

}
