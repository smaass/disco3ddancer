package cl.smaass.disco3ddancer.audio;

public class SignalProcessing {
	static private FFT transform = new FFT(8);
	
	static public Result frequencyDomain(float[] sample, int sampleRate) {
		float[] ivalues = new float[sample.length];
		if (transform.n != sample.length)
			transform = new FFT(sample.length);
		transform.fft(sample, ivalues);
		float[] magnitudes = getComplexMagnitudes2(sample, ivalues);
		float[] frequencies = computeFrequencies(sample.length/2, sampleRate);
		magnitudes[0] = 0;
		return new Result(frequencies, magnitudes);
	}
	
	public static float[] computeFrequencies(int size, int sampleRate) {
		 float[] frequencies = new float[size];
		 for (int i=0; i<size; i++) {
			 frequencies[i] = (float) (((1.0 * sampleRate) / (1.0 * size)) * i);
		 }
		 return frequencies;
	 }
	 
	 private static float[] getComplexMagnitudes2(float[] rvalues, float[] ivalues) {
		 int size = rvalues.length/2;
		 float[] magnitude = new float[size];
		 for (int i=0; i<size; i++)
			 magnitude[i] = rvalues[i]*rvalues[i] + ivalues[i]*ivalues[i];
		 return magnitude;
	 }
	
	public static class Result {
		private float x[], y[];
		
		public Result(float[] x, float[] y) {
			this.x = x;
			this.y = y;
		}
		
		public float[] x() {
			return x;
		}
		
		public float[] y() {
			return y;
		}
	}
	
	private static class FFT {
		 int n, m;
		 
		 // Lookup tables. Only need to recompute when size of FFT changes.
		 double[] cos;
		 double[] sin;

		 public FFT(int n) {
			 this.n = n;
			 this.m = (int) (Math.log(n) / Math.log(2));

			 // Make sure n is a power of 2
			 if (n != (1 << m))
				 throw new RuntimeException("FFT length must be power of 2");

			 // precompute tables
			 cos = new double[n / 2];
			 sin = new double[n / 2];

			 for (int i = 0; i < n / 2; i++) {
				 cos[i] = Math.cos(-2 * Math.PI * i / n);
				 sin[i] = Math.sin(-2 * Math.PI * i / n);
			 }
		 }

		 public void fft(float[] re, float[] im) {
			 int i, j, k, n1, n2, a;
			 float c, s, t1, t2;

			 // Bit-reverse
			 j = 0;
			 n2 = n / 2;
			 for (i = 1; i < n - 1; i++) {
				 n1 = n2;
				 while (j >= n1) {
					 j = j - n1;
					 n1 = n1 / 2;
				 }
				 j = j + n1;

				 if (i < j) {
					 t1 = re[i];
					 re[i] = re[j];
					 re[j] = t1;
					 t1 = im[i];
					 im[i] = im[j];
					 im[j] = t1;
				 }
			 }
			 
			 // FFT
			 n1 = 0;
			 n2 = 1;
			 for (i = 0; i < m; i++) {
				 n1 = n2;
				 n2 = n2 + n2;
				 a = 0;

				 for (j = 0; j < n1; j++) {
					 c = (float) cos[a];
					 s = (float) sin[a];
					 a += 1 << (m - i - 1);

					 for (k = j; k < n; k = k + n2) {
						 t1 = c * re[k + n1] - s * im[k + n1];
						 t2 = s * re[k + n1] + c * im[k + n1];
						 re[k + n1] = re[k] - t1;
						 im[k + n1] = im[k] - t2;
						 re[k] = re[k] + t1;
						 im[k] = im[k] + t2;
					 }
				 }
			 }
		 }
	}
}
