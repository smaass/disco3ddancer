package cl.smaass.experiments.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

public class FloorRenderer implements GLSurfaceView.Renderer {
	
	/** Store our model data in a float buffer. */
	private final FloatBuffer mTriangleVertices;

	/** This will be used to pass in the transformation matrix. */
	private int iResolutionHandle;
	private int iMouseHandle;
	private int iGlobalTimeHandle;
	
	/** This will be used to pass in model position information. */
	private int mPositionHandle;

	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;
	
	/** How many elements per vertex. */
	private final int mStrideBytes = 3 * mBytesPerFloat;	
	
	/** Offset of the position data. */
	private final int mPositionOffset = 0;
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;
	
	private int width;
	private int height;
				
	/**
	 * Initialize the model data.
	 */
	public FloorRenderer()
	{
		// Define points for equilateral triangles.
		
				// This triangle is red, green, and blue.
				final float[] trianglesData = {
						// Two triangles
			            -1.0f, -1.0f, 0.5f, 
			            1.0f, -1.0f, 0.5f,
			            -1.0f, 1.0f, 0.5f,
			            
						1.0f, -1.0f, 0.5f,
						1.0f, 1.0f, 0.5f,
						-1.0f, 1.0f, 0.5f};
				
				// Initialize the buffers.
				mTriangleVertices = ByteBuffer.allocateDirect(trianglesData.length * mBytesPerFloat)
		        .order(ByteOrder.nativeOrder()).asFloatBuffer();
				mTriangleVertices.put(trianglesData).position(0);
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
		// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

		final String vertexShader =			
		  "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.		  
		  
		  + "void main()                    \n"		// The entry point for our vertex shader.
		  + "{                              \n"
		  + "   gl_Position = a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
		  + "}                              \n";    // normalized screen coordinates.
		
		final String fragmentShader =
				"precision mediump float;" + "\n" + 
						"" + "\n" + 
						"uniform float time;" + "\n" + 
						"" + "\n" + 
						"const float SQUARE_LENGTH = 100.0;" + "\n" + 
						"const float BORDER_LENGTH = 3.0;" + "\n" + 
						"" + "\n" + 
						"const vec4 BORDER_COLOR = vec4(0.0, 0.0, 0.0, 1.0);" + "\n" + 
						"const vec4 OFF_COLOR = vec4(0.15, 0.15, 0.15, 1.0);" + "\n" + 
						"" + "\n" + 
						"vec4 uvToSquareCoords(vec2 uv);" + "\n" + 
						"" + "\n" + 
						"void main( void ) {" + "\n" + 
						"	vec2 uv = gl_FragCoord.xy;" + "\n" + 
						"	vec4 sqr_coords = uvToSquareCoords(uv);" + "\n" + 
						"	" + "\n" + 
						"	if (sqr_coords.z <= BORDER_LENGTH || sqr_coords.z >= SQUARE_LENGTH - BORDER_LENGTH) {" + "\n" + 
						"		gl_FragColor = BORDER_COLOR;" + "\n" + 
						"		return;" + "\n" + 
						"	}" + "\n" + 
						"	" + "\n" + 
						"	if (sqr_coords.w <= BORDER_LENGTH || sqr_coords.w >= SQUARE_LENGTH - BORDER_LENGTH) {" + "\n" + 
						"		gl_FragColor = BORDER_COLOR;" + "\n" + 
						"		return;" + "\n" + 
						"	}" + "\n" + 
						"	" + "\n" + 
						"	gl_FragColor = (0.5 + abs(sin(time) + cos(2.0 * time))) * vec4(abs(sin(sqr_coords.x / 5.0 + time)),abs(cos(sqr_coords.y / 2.0 + time)), 0.0, 1.0) + OFF_COLOR;" + "\n" + 
						"}" + "\n" + 
						"" + "\n" + 
						"vec4 uvToSquareCoords(vec2 uv) {" + "\n" + 
						"	vec4 sqr_coords;" + "\n" + 
						"	sqr_coords.xy = floor(uv / SQUARE_LENGTH);" + "\n" + 
						"	sqr_coords.zw = uv -  SQUARE_LENGTH * sqr_coords.xy;" + "\n" + 
						"	" + "\n" + 
						"	return sqr_coords;" + "\n" + 
						"}";								
		
		// Load in the vertex shader.
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);

			// Compile the shader.
			GLES20.glCompileShader(vertexShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0)
		{
			throw new RuntimeException("Error creating vertex shader.");
		}
		
		// Load in the fragment shader shader.
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

			// Compile the shader.
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			
			Log.e("Hola", GLES20.glGetShaderInfoLog(fragmentShaderHandle));

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0)
		{
			throw new RuntimeException("Error creating fragment shader.");
		}
		
		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
        
        // Set program handles. These will later be used to pass in values to the program.      
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        iResolutionHandle = GLES20.glGetUniformLocation(programHandle, "iResolution");
        iGlobalTimeHandle = GLES20.glGetUniformLocation(programHandle, "time");
        iMouseHandle = GLES20.glGetUniformLocation(programHandle, "iMouse");
        
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);        
	}	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		this.width = width;
		this.height = height;
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
	}	

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        
        
        // Draw the triangle facing straight on.   
        drawTriangle(mTriangleVertices);
	}	
	
	/**
	 * Draws a triangle from the given vertex data.
	 * 
	 * @param aTriangleBuffer The buffer containing the vertex data.
	 */
	private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{		
		// Pass in the position information
		aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glUniform3f(iResolutionHandle, width, height, 0);
        GLES20.glUniform1f(iGlobalTimeHandle, SystemClock.uptimeMillis()%1000L/300f);
        GLES20.glUniform4f(iMouseHandle, 0,0,0,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);                               
	}
}
