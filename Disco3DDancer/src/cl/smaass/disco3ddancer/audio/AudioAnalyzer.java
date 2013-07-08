package cl.smaass.disco3ddancer.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

public class AudioAnalyzer {
	private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private int bufferSize;
	private AudioRecord recorder;
	private Thread recordingThread;
	private AudioAnalysisThread analyzerThread;
	private boolean isCapturing;
	private Handler handler;
	
	public AudioAnalyzer() {
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				 RECORDER_AUDIO_ENCODING);
	}
	
	public void startAnalysis(AudioAnalysisThread analyzer, Handler.Callback resultCallback) {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
		recorder.startRecording();
		isCapturing = true;
		recordingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				processAudio(analyzerThread);
			}
		},"AudioRecorder Thread");
		
		analyzerThread = analyzer;
		handler = new Handler(resultCallback);
		analyzerThread.setResultHandler(handler);
		analyzerThread.init(RECORDER_SAMPLERATE, bufferSize);
		recordingThread.start();
		analyzerThread.start();
	}
	
	public void processAudio(AudioAnalysisThread audioThread) {
		byte data[] = new byte[bufferSize];
		int read = 0;
		
		while(isCapturing) {
			long time1 = System.currentTimeMillis();
			read = recorder.read(data, 0, bufferSize);
			long currentTime = (time1 + System.currentTimeMillis())/2;
			if (AudioRecord.ERROR_INVALID_OPERATION == read)
				throw new RuntimeException("Error while trying to read from AudioRecord!");
			analyzerThread.analyzeInstantSample(data, currentTime);
		 }
	 }
	
	public void endAnalysis() {
		isCapturing = false;
		analyzerThread.end();
		recorder.stop();
		recorder.release();
		try {
			recordingThread.join();
			analyzerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
