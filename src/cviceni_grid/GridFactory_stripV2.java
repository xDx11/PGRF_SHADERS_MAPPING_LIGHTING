package cviceni_grid;

import com.jogamp.opengl.GL2;

import oglutils.OGLBuffers;

public class GridFactory_stripV2 {
	
	
	public static OGLBuffers createOGLBuffers(GL2 gl, int m, int n){		
		int vertices = m * n;
		int triangles_list = (m-1)*(n-1)*2;
		int triangles_strip = 2*m*(n-1);
		
		float[] vb = new float[vertices * 2];
		//int[] ib = new int[triangles*3]; // triangles * 3 values
		int[] ib = new int[triangles_strip+n*2-2]; // triangles * 3 values
		
		int index = 0;
		
		for(int j = 0; j < n; j++){
			for(int i = 0; i < m; i++){
				vb[index] =  i / (float)(m-1);
				vb[index + 1] =  j / (float)(n-1);
				index+=2;								
			}
		}
		
		index = 0;
		int height = n;
		int width = m;
		for(int row = 0; row < height-1; row++){
			
			if(row % 2 == 0){
				for(int col = 0; col < width; col++){		//																			
					ib[index++] = row * width + col;
					ib[index++] = (row+1) * width + col;
					if(col+1 == width){
						ib[index++] = (row+1) * width + col;
						ib[index++] = (row+1) * width + col;																		
					}
				}												
			} else {
				for(int col = width-1; col >= 0; col--){		//
					ib[index++] = (row+1) * width + col;				
					ib[index++] = (row) * width + col ;
					if(col-1 < 0){
						ib[index++] = (row+1) * width + col;
						ib[index++] = (row+1) * width + col;
					}
				}
			}					
		}
		
	
		
		/*
		for(int i = 0; i< vb.length-1; i+=2){
			System.out.print(vb[i] + " " + vb[i+1] + " ,");
		}
		System.out.println();
		
		for(int i = 0; i<= ib.length-3; i+=3){
			System.out.print(ib[i] + " ," + ib[i+1] + " ," + ib[i+2] + " ,        ");
		}
		System.out.println();
		System.out.println();
		System.out.println("IB size is: " + ib.length);
		System.out.println();
		
		for(int i = 0; i< ib.length; i++){
			System.out.print(ib[i] + " ,");
		}
		System.out.println();
		*/
		OGLBuffers.Attrib[] attributes = {
				new OGLBuffers.Attrib("inPosition", 2), // 2 floats				
		};
		
		OGLBuffers buffers = new OGLBuffers(gl, vb, attributes,
				ib);
		return buffers;
	}
	
	
}
