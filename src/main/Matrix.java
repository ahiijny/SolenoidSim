package main;

/** Static class that performs various matrix operations.
 * Referenced rotation matrices from: http://en.wikipedia.org/wiki/Rotation_matrix
 * All rotation is based on a right-handed system. If y is pointing up on your screen
 * and x is pointing right, then z is pointing right at you out of the screen.
 * <p>
 * For succinctness, these methods do not verify inputs. If the programmer tries
 * to multiply two matrices together whose dimensions don't match up properly,
 * they might get a nice ArrayIndexOutOfBoundsException. 
 */
public class Matrix 
{
	/** Returns the rotation matrix for the specified rotation about
	 * the given unit vector u. The vector u must have length one and
	 * dimension R3. The rotation is counterclockwise when looking towards
	 * the origin, in the opposite direction of the unit vector.
	 * 
	 * @param u			the unit vector representing the rotation axis
	 * @param theta		angle of rotation (radians)
	 * @return the rotation matrix representing the specified rotation
	 */
	public static double[][] getRotationMatrix(double[] u, double theta)
	{
		// Variables
		
		double[][] R = new double[3][3];
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double ux = u[0];
		double uy = u[1];
		double uz = u[2];
		
		// Copying down the matrix entries from Wikipedia
		
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
	
	/** Finds the rotation matrix that rotates vector original onto vector target.
	 * 
	 * @param original
	 * @param target
	 * @return
	 */
	public static double[][] getRotationMatrix(double[] original, double[] target)
	{
		double[][] R = new double[3][3];
		double[] a = Calc.unit(original);
		double[] b = Calc.unit(target);
		
		// Find the rotation axis
		double[] u = Calc.cross(a, b);
				
		// Find sin
		double sin = Calc.mag(u);
			
		// Find cos
		double cos = Calc.dot(a, b);
		
		if (sin != 0)
		{				
			// Find the rotation matrix
			
			double[][] VX = getSkewSymmetricCrossProductMatrix(u);
			double[][] VXsq = multiply(VX, VX);
			double[][] I = getIdentityMatrix(3);
			
			R = add(add(I, VX), scale(VXsq, (1 - cos)/(sin*sin)));
		}
		else
		{
			R = scale(getIdentityMatrix(3), Math.signum(cos));
		}
		return R;
	}
	
	/** Returns the rotation matrix for a rotation about the x-axis.
	 * 
	 * @param theta		the rotation angle (radians)
	 * @return the rotation matrix representing the specified rotation
	 */
	public static double[][] getRotationMatrixX(double theta)
	{
		// Variables
		
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		// Copying down the matrix entries from Wikipedia
		
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
	
	/** Returns the rotation matrix for a rotation about the y-axis.
	 * 
	 * @param theta		the rotation angle (radians)
	 * @return the rotation matrix representing the specified rotation
	 */
	public static double[][] getRotationMatrixY(double theta)
	{
		// Variables
		
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		// Copying down the matrix entries from Wikipedia
		
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
	
	/** Returns the rotation matrix for a rotation about the z-axis.
	 * 
	 * @param theta		the rotation angle (radians)
	 * @return the rotation matrix representing the specified rotation
	 */
	public static double[][] getRotationMatrixZ(double theta)
	{
		// Variables
		
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		double[][] R = new double[3][3];
		
		// Copying down the matrix entries from Wikipedia
		
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
	
	/** Converts the given vector (array of doubles) into
	 * the corresponding column matrix (array of array of doubles).
	 * 
	 * @param a		the vector
	 * @return the matrix of dimensions [R x 1] where R is the vector dimension
	 */
	public static double[][] getColumnMatrix(double[] a)
	{
		double[][] X = new double[a.length][1];
		
		for (int i = 0; i < a.length; i++)
				X[i][0] = a[i];
		
		return X;
	}
	
	/** Converts the given column matrix (array of array of doubles)
	 * into the corresponding vector (array of doubles).
	 * 
	 * @param X		the column matrix of dimensions [R x 1] where R is the vector dimension
	 * @return the vector of dimension R
	 */
	public static double[] getColumnVector(double[][] X)
	{
		double[] a = new double[X.length];
		
		for (int i = 0; i < X.length; i++)
			a[i] = X[i][0];
		
		return a;
	}
			
	/** Left multiplies the matrix A with B.
	 * 
	 * @param A		the left matrix
	 * @param B		the right matrix
	 * @return the product matrix AB
	 */
	public static double[][] multiply(double[][] A, double[][] B)
	{
		int rA = A.length;
		int rB = B.length;
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
	
	/** Left multiplies the matrix R (usually a rotation matrix) with
	 * the given vector. This method automatically converts the
	 * vector into a column matrix to do the multiplication, and
	 * then converts the product back into a vector so that it can
	 * be returned to the caller of this method.
	 * 
	 * @param R		the matrix
	 * @param a		the vector
	 * @return	the vector corresponding with the product Ra 
	 */
	public static double[] multiply(double[][] R, double[] a)
	{
		double[][] X = getColumnMatrix(a);
		X = multiply(R, X);
		return getColumnVector(X);
	}
	
	public static double[][] add(double[][] A, double[][] B)
	{
		double[][] C = new double[A.length][A[0].length];
		
		for (int i = 0; i < C.length; i++)
			for (int j = 0; j < C[i].length; j++)
				C[i][j] = A[i][j] + B[i][j];
		
		return C;
	}
	
	public static double[][] scale(double[][] A, double scalar)
	{
		for (int i = 0; i < A.length; i++)
			for (int j = 0; j < A[i].length; j++)
				A[i][j] *= scalar;
		
		return A;
	}
	
	public static double[][] getSkewSymmetricCrossProductMatrix(double[] a)
	{
		double[] row1 = {0, -a[2], a[1]};
		double[] row2 = {a[2], 0, -a[0]};
		double[] row3 = {-a[1], a[0], 0};
		
		double[][] VX = {row1, row2, row3};
		
		return VX;
	}
	
	public static double[][] getIdentityMatrix(int dimension)
	{
		double[][] I = new double[dimension][dimension];
		for (int i = 0; i < I.length; i++)
			for (int j = 0; j < I[i].length; j++)
				I[i][j] = i == j ? 1 : 0;
		
		return I;
	}
}
