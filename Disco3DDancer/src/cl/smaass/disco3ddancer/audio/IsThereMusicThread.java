package cl.smaass.disco3ddancer.audio;

public class IsThereMusicThread extends AudioAnalysisThread {
	private final int SECONDS = 1;
	private int historyBufferSize;
	private long A[];				// amplitude history buffer
	private long avgA[];			// average amplitude history
	private int pos = 0;			// actual position in history buffer
	private int seconds = 0;

	@Override
	public void init(int sampleRate, int bufferSize) {
		this.historyBufferSize = 2*sampleRate/bufferSize;
		A = new long[historyBufferSize];
		avgA = new long[SECONDS];
	}
	
	@Override
	public void analyzeSample(InstantSample sample) {
		byte data[] = sample.getSample();
		int sampleLength = data.length/2;
		int localAvgA = 0;

		// instant energy
		int e = 0;
		for (int i=0; i<sampleLength; i++)
			e += Math.abs(data[2*i+1]*256 + data[2*i]);
		A[pos++] = e;

		// average local amplitude
		for (int i=0; i<A.length; i++)
			localAvgA += A[i];
		localAvgA /= A.length;
		avgA[seconds] = localAvgA;

		if (pos == historyBufferSize) {			// 1 second elapsed
			pos = 0;
			seconds++;
		}
		
		if (seconds == SECONDS) {
			int averageA = 0;
			seconds = 0;
			for (int i=0; i<avgA.length; i++)
				averageA += avgA[i];
			averageA /= avgA.length;
			sendMessageToHandler(isThereMusic(averageA));
		}
	}
	
	private Boolean isThereMusic(long avgA) {
		return avgA > 1000000;
	}
}
