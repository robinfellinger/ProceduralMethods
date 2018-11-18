/* Copyright 1994, 2002 by Steven Worley
 * This software may be modified and redistributed without restriction
 * provided this comment header remains intact in the source code.
 * This code is provided with no warrantee, express or implied, for
 * any purpose.
 *
 * A detailed description and application examples can be found in the
 * 1996 SIGGRAPH paper "A Cellular Texture Basis Function" and
 * especially in the 2002 book "Texturing and Modeling, a Procedural
 * Approach, 3rd edition." There is also extra information on the web
 * site http://www.worley.com/cellular.html .
 *
 * If you do find interesting uses for this tool, and especially if
 * you enhance it, please drop me an email at steve@worley.com.
 */

/* Ported (badly) from C to Java by Stefan Gustavson (stegu@itn.liu.se). */

public final class WorleyNoise {

  // This method is a *lot* faster than using (int)Math.floor() on an x86 CPU.
  private static int fastfloor(double x) {
  	int xi = (int)x; // Chop off decimals, i.e round towards zero
  	return x<xi ? xi-1 : xi; // Adjust to round down even for x<0
  }
  
  // A hardwired lookup table to quickly determine how many feature
  // points should be in each spatial cube. We use a table so we don't
  // need to make multiple slower tests.  A random number indexed into
  // this array will give an approximate Poisson distribution of mean
  // density 2.5. Read the book for the longwinded explanation.
  private static final int Poisson_count[]=
  {4,3,1,1,1,2,4,2,2,2,5,1,0,2,1,2,2,0,4,3,2,1,2,1,3,2,2,4,2,2,5,1,2,3,2,2,2,2,2,3,
   2,4,2,5,3,2,2,2,5,3,3,5,2,1,3,3,4,4,2,3,0,4,2,2,2,1,3,2,2,2,3,3,3,1,2,0,2,1,1,2,
   2,2,2,5,3,2,3,2,3,2,2,1,0,2,1,1,2,1,2,2,1,3,4,2,2,2,5,4,2,4,2,2,5,4,3,2,2,5,4,3,
   3,3,5,2,2,2,2,2,3,1,1,4,2,1,3,3,4,3,2,4,3,3,3,4,5,1,4,2,4,3,1,2,3,5,3,2,1,3,1,3,
   3,3,2,3,1,5,5,4,2,2,4,1,3,4,1,5,3,3,5,3,4,3,2,2,1,1,1,1,1,2,4,5,4,5,4,2,1,5,1,1,
   2,3,3,3,2,5,2,3,3,2,0,2,1,1,4,2,1,3,2,1,2,2,3,2,5,5,3,4,5,5,2,4,4,5,3,2,2,2,1,4,
   2,3,3,4,2,5,4,2,4,2,2,2,4,5,3,2};
  
  // This constant is manipulated to make sure that the mean value of F[0]
  // is 1.0. This makes an easy natural "scale" size of the cellular features.
  private static final double DENSITY_ADJUSTMENT = 0.398150;
  private static final double INV_DENSITY_ADJUSTMENT = 1.0/0.398150;
  
  // The main function!
  public static void noise(double at[], int max_order, double F[],
                           double delta[][], int ID[])
  {
    double x2,y2,z2, mx2,my2,mz2;
    double new_at[] = new double[3];
    int int_at[] = new int[3];
    int i;
    
    // Initialize the F values to "huge" so they will be replaced by the
    // first real sample tests. Note that we'll be storing and comparing the
    // SQUARED distance from the feature points to avoid lots of slow
    // sqrt() calls. We'll use sqrt() only on the final answer.
    for (i=0; i<max_order; i++) F[i]=999999.9;
    
    // Make our own local copy, multiplying to make mean(F[0])==1.0
    new_at[0]=DENSITY_ADJUSTMENT*at[0];
    new_at[1]=DENSITY_ADJUSTMENT*at[1];
    new_at[2]=DENSITY_ADJUSTMENT*at[2];
  
    // Find the integer cube holding the hit point
    int_at[0]=fastfloor(new_at[0]);
    int_at[1]=fastfloor(new_at[1]);
    int_at[2]=fastfloor(new_at[2]);
  
    // Naive but working exhaustive search of all 27 neighboring cubes.
    int ii, jj, kk;
    for (ii=-1; ii<=1; ii++)
      for (jj=-1; jj<=1; jj++)
        for (kk=-1; kk<=1; kk++)
          AddSamples(int_at[0]+ii,int_at[1]+jj,int_at[2]+kk, 
                     max_order, new_at, F, delta, ID);

/*
 * The code below gives the wrong result, but is copied directly from
 * the working C version. Find the bug, and this method will be twice
 * as fast as the above naive exhaustive loop over all 27 cubes.
 *
 *
    // Test the central cube for closest point(s).
    AddSamples(int_at[0], int_at[1], int_at[2], max_order, new_at, F, delta, ID);
  
    // We test if neighbor cubes are even POSSIBLE contributors by examining the
    // combinations of the sum of the squared distances from the cube's lower 
    // or upper corners.
    x2=new_at[0]-int_at[0];
    y2=new_at[1]-int_at[1];
    z2=new_at[2]-int_at[2];
    mx2=(1.0-x2)*(1.0-x2);
    my2=(1.0-y2)*(1.0-y2);
    mz2=(1.0-z2)*(1.0-z2);
    x2*=x2;
    y2*=y2;
    z2*=z2;
    
    // Test 6 facing neighbors of center cube. These are closest and most 
    // likely to have a close feature point.
    if (x2<F[max_order-1])  AddSamples(int_at[0]-1, int_at[1]  , int_at[2]  , 
  				     max_order, new_at, F, delta, ID);
    if (y2<F[max_order-1])  AddSamples(int_at[0]  , int_at[1]-1, int_at[2]  , 
  				     max_order, new_at, F, delta, ID);
    if (z2<F[max_order-1])  AddSamples(int_at[0]  , int_at[1]  , int_at[2]-1, 
  				     max_order, new_at, F, delta, ID);
    
    if (mx2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]  , int_at[2]  , 
  				     max_order, new_at, F, delta, ID);
    if (my2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]+1, int_at[2]  , 
  				     max_order, new_at, F, delta, ID);
    if (mz2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]  , int_at[2]+1, 
  				     max_order, new_at, F, delta, ID);
    
    // Test 12 "edge cube" neighbors if necessary. They're next closest.
    if ( x2+ y2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]-1, int_at[2]  , 
  					 max_order, new_at, F, delta, ID);
    if ( x2+ z2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]  , int_at[2]-1, 
  					 max_order, new_at, F, delta, ID);
    if ( y2+ z2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]-1, int_at[2]-1, 
  					 max_order, new_at, F, delta, ID);  
    if (mx2+my2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]+1, int_at[2]  , 
  					 max_order, new_at, F, delta, ID);
    if (mx2+mz2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]  , int_at[2]+1, 
  					 max_order, new_at, F, delta, ID);
    if (my2+mz2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]+1, int_at[2]+1, 
  					 max_order, new_at, F, delta, ID);  
    if ( x2+my2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]+1, int_at[2]  , 
  					 max_order, new_at, F, delta, ID);
    if ( x2+mz2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]  , int_at[2]+1, 
  					 max_order, new_at, F, delta, ID);
    if ( y2+mz2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]-1, int_at[2]+1, 
  					 max_order, new_at, F, delta, ID);  
    if (mx2+ y2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]-1, int_at[2]  , 
  					 max_order, new_at, F, delta, ID);
    if (mx2+ z2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]  , int_at[2]-1, 
  					 max_order, new_at, F, delta, ID);
    if (my2+ z2<F[max_order-1]) AddSamples(int_at[0]  , int_at[1]+1, int_at[2]-1, 
  					 max_order, new_at, F, delta, ID);  
    
    // Final 8 "corner" cubes
    if ( x2+ y2+ z2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]-1, int_at[2]-1, 
  					     max_order, new_at, F, delta, ID);
    if ( x2+ y2+mz2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]-1, int_at[2]+1, 
  					     max_order, new_at, F, delta, ID);
    if ( x2+my2+ z2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]+1, int_at[2]-1, 
  					     max_order, new_at, F, delta, ID);
    if ( x2+my2+mz2<F[max_order-1]) AddSamples(int_at[0]-1, int_at[1]+1, int_at[2]+1, 
  					     max_order, new_at, F, delta, ID);
    if (mx2+ y2+ z2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]-1, int_at[2]-1, 
  					     max_order, new_at, F, delta, ID);
    if (mx2+ y2+mz2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]-1, int_at[2]+1, 
  					     max_order, new_at, F, delta, ID);
    if (mx2+my2+ z2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]+1, int_at[2]-1, 
  					     max_order, new_at, F, delta, ID);
    if (mx2+my2+mz2<F[max_order-1]) AddSamples(int_at[0]+1, int_at[1]+1, int_at[2]+1, 
  					     max_order, new_at, F, delta, ID);
*/
  
    // We're done! Convert everything to right size scale
    for (i=0; i<max_order; i++)
      {
        F[i]=Math.sqrt(F[i])*(1.0/DENSITY_ADJUSTMENT);      
        delta[i][0]*=INV_DENSITY_ADJUSTMENT;
        delta[i][1]*=INV_DENSITY_ADJUSTMENT;
        delta[i][2]*=INV_DENSITY_ADJUSTMENT;
      }
    
    return;
  }
  
  
  
  private static void AddSamples(int xi, int yi, int zi, int max_order,
  		       double at[], double F[],
  		       double delta[][], int ID[])
  {
    double dx, dy, dz, fx, fy, fz, d2;
    int count, i, j, index;
    int seed, this_id;
    
    /* Each cube has a random number seed based on the cube's ID number.
       The seed might be better if it were a nonlinear hash like Perlin uses
       for noise but we do very well with this faster simple one.
       Our LCG uses Knuth-approved constants for maximal periods. */
    seed=702395077*xi + 915488749*yi + 2120969693*zi;
    
    /* How many feature points are in this cube? */
    count=Poisson_count[(seed>>24) & 0xFF]; /* 256 element lookup table. Use MSB */
  
    seed=1402024253*seed+586950981; /* churn the seed with good Knuth LCG */
  
    for (j=0; j<count; j++) /* test and insert each point into our solution */
      {
        this_id=seed;
        seed=1402024253*seed+586950981; /* churn */
  
        /* compute the 0..1 feature point location's XYZ */
        fx=(seed+0.5)*(1.0/4294967296.0); 
        seed=1402024253*seed+586950981; /* churn */
        fy=(seed+0.5)*(1.0/4294967296.0);
        seed=1402024253*seed+586950981; /* churn */
        fz=(seed+0.5)*(1.0/4294967296.0);
        seed=1402024253*seed+586950981; /* churn */
  
        /* delta from feature point to sample location */
        dx=xi+fx-at[0]; 
        dy=yi+fy-at[1];
        dz=zi+fz-at[2];
        
        /* Distance computation!  Lots of interesting variations are
  	 possible here!
  	 Biased "stretched"   A*dx*dx+B*dy*dy+C*dz*dz
  	 Manhattan distance   fabs(dx)+fabs(dy)+fabs(dz)
  	 Radial Manhattan:    A*fabs(dR)+B*fabs(dTheta)+C*dz
  	 Superquadratic:      pow(fabs(dx), A) + pow(fabs(dy), B) + pow(fabs(dz),C)
  	 
  	 Go ahead and make your own! Remember that you must insure that
  	 new distance function causes large deltas in 3D space to map into
  	 large deltas in your distance function, so our 3D search can find
  	 them! [Alternatively, change the search algorithm for your special
  	 cases.]       
        */
  
        d2=dx*dx+dy*dy+dz*dz; /* Euclidean distance, squared */
  
        if (d2<F[max_order-1]) /* Is this point close enough to remember? */
  	{
  	  /* Insert the information into the output arrays if it's close enough.
  	     We use an insertion sort.  No need for a binary search to find
  	     the appropriate index.. usually we're dealing with order 2,3,4 so
  	     we can just go through the list. If you were computing order 50
  	     (wow!!) you could get a speedup with a binary search in the sorted
  	     F[] list. */
  	  
  	  index=max_order;
  	  while (index>0 && d2<F[index-1]) index--;
  
  	  /* We insert this new point into slot # <index> */
  
  	  /* Bump down more distant information to make room for this new point. */
  	  for (i=max_order-2; i>=index; i--)
  	    {
  	      F[i+1]=F[i];
  	      ID[i+1]=ID[i];
  	      delta[i+1][0]=delta[i][0];
  	      delta[i+1][1]=delta[i][1];
  	      delta[i+1][2]=delta[i][2];
  	    }		
  	  /* Insert the new point's information into the list. */
  	  F[index]=d2;
  	  ID[index]=this_id;
  	  delta[index][0]=dx;
  	  delta[index][1]=dy;
  	  delta[index][2]=dz;
  	}
      }
    
    return;
  }
}
