package math;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Random;

public class Diffuse
{

	int n = 100;
	
	HashMap<String, Double> coords = new HashMap<String, Double>();
	double[][] coords1;
	double[][] coords2;
	double[][] coords3;
	
	private double sh(double x)
	{
		return (Math.exp(x) - Math.exp(-x)) / 2.0;
	}
	
	private double ch(double x)
	{
		return (Math.exp(x) + Math.exp(-x)) / 2.0;
	}
	
	public double c(double a, double b)
	{
		double z = 26.0;
		double H = 50.0;
		double x0 = 0;
		double y0 = 0;
		double z0 = 25.0;
		
		double v = 0.1;
		double w = 0.0;
		double wg = 0.005;
		double o = 0.0;
		double mu = 0.4;
		double nu = -1.0;
		double u = 2.0;
		
//		double O = mu * (a*a + b*b) - (a*u+b*nu) + o;
		double O = mu * (a*a + b*b) + o;
		
		if(Double.isNaN(O))
			System.out.println("NAN, O: " + a + " " + b);
		
		double C = 1.0;
		
		double k1 = (w - wg) / (2*v);
		double k2 = (Math.sqrt((w-wg) * (w-wg) + 4*v*O)) / (2*v);
		
		if(Double.isNaN(k1))
			System.out.println("NAN, k1: " + a + " " + b);
		if(Double.isNaN(k2))
			System.out.println("NAN, k2: " + a + " " + b);
		
//		double e1 = C*Math.exp(i*(a*x0-b*y0))*Math.exp(k1*(z-z0));
		double e1 = C*Math.exp(k1*(z-z0));
		
		if(Double.isNaN(e1))
			System.out.println("NAN, e1: " + a + " " + b);
		
		double shk2 = sh(k2);
		double chk2 = ch(k2);
		
		if(Double.isNaN(shk2))
			System.out.println("NAN, shk2: " + a + " " + b);
		if(Double.isNaN(chk2))
			System.out.println("NAN, chk2: " + a + " " + b);
		
		if(z <= z0)
		{
			double l1 = e1 * shk2 * (H - z0);
			double l2 = (k1*shk2*z - k2* chk2 * z);
			
			if(Double.isNaN(l1))
				System.out.println("NAN, l1: " + a + " " + b);
			if(Double.isNaN(l2))
				System.out.println("NAN, l2: " + a + " " + b);
			
			double first = (l1 * l2) / (v * k2 * (k1* shk2 *H - k2*chk2*H));
			
			if(Double.isNaN(first))
				System.out.println("NAN, first: " + a + " " + b);
			
			return first;
		}
		else
		{
			double second = (e1 * shk2 * (H - z) * (k1*shk2*z0 - k2*chk2 * z0)) / (v * k2 * (k1*shk2*H - k2*chk2*H));
			return second;
		}
	}
	
	public double c(double x, double y, double z, double I0,
			double u, double H)
	{	
		
		double DTX = 0.000396 * u;
		double DTY = 0.000396 * u;
		double DTZ = 20;
		
		double mDTX = 1.0 / Math.sqrt(DTX);
		
		if(Double.isInfinite(mDTX))
		{
			System.out.println();
		}
		
		double a = 1.0 / Math.sqrt((x*x) * (1.0 / DTX) + (y*y) * (1.0 / DTY) + ((z - H) * (z - H)) * (1.0 / DTZ));
		double b = 1.0 / Math.sqrt((x*x) * (1.0 / DTX) + (y*y) * (1.0 / DTY) + ((z + H) * (z + H)) * (1.0 / DTZ));
		
		if(Double.isInfinite(a) || Double.isInfinite(b))
		{
			System.out.println();
		}
		
		double ma = 1.0 / a;
		double mb = 1.0 / b;
		
		if(Double.isInfinite(ma) || Double.isInfinite(mb))
		{
			System.out.println();
		}
		
		double first = 0.0796 * I0;
		
		if(Double.isInfinite(first))
		{
			System.out.println();
		}
		
		double second = first * (1.0 / Math.sqrt(DTX * DTY * DTZ));
		
		if(Double.isInfinite(second))
		{
			System.out.println();
		}
		
		double third = Math.exp(0.5 * u * mDTX * (x * mDTX - a));
		
		if(Double.isInfinite(third))
		{
			System.out.println();
		}
		
		double fourth = Math.exp(0.5 * u * mDTX * (x * mDTX - b));
		
		if(Double.isInfinite(fourth))
		{
			System.out.println();
		}
		
		return second * ( ma * third + mb * fourth );
	}

	public double max = 0;
	public double min = 0;
	
	public void calculate()
	{
		max = Double.MIN_VALUE;
		min = Double.MAX_VALUE;
		coords.clear();
		double stepX = 0.5;
		double stepY = 0.5;
		double stepZ = 7.5;
		
		int minX = -0;
		int minY = -17;
		int maxX = 20;
		int maxY = 15;
		int sizeX = (int) ((maxX - minX) / stepX) + 1;
		int sizeY = (int) ((maxY - minY) / stepY) + 1;
		
		NumberFormat formatter = new DecimalFormat("#0.0000");
//		try
//		{
//			FileWriter wr = new FileWriter(new File(".", "result.txt"));
			coords1 = new double[sizeX][sizeY];
			coords2 = new double[sizeX][sizeY];
			coords3 = new double[sizeX][sizeY];
			int i = 0;
			int j = 0;
			for(double x = minX; x <= maxX; x += stepX)
			{
				for(double y = minY; y<=maxY;y+=stepY)
				{
					double c = c(x,y);
					
					String s = x + " " + y;
	//				coords.put(s, c);
					coords1[i][j] = c;
					coords2[i][j] = x;
					coords3[i][j] = y;
//					wr.write(s + " = " + c + "\n");
					if(c > max)
					{
						max = c;
					}
					else if(c < min)
					{
						min = c;
					}
					j++;
				}
				j = 0;
				i++;
			}
//			wr.close();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		System.out.println((max));
//		System.out.println((min));
//		System.out.println(formatter.format(max));
//		System.out.println(formatter.format(min));
	}
	
	public double[][] getCoords()
	{
		return this.coords1;
	}
	
	public double[][] getCoords2()
	{
		return this.coords2;
	}
	public double[][] getCoords3()
	{
		return this.coords3;
	}
}
