package cl.smaass.disco3ddancer.audio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Message;

public abstract class AudioAnalysisThread extends Thread {
	private boolean running;
	private BlockingQueue<InstantSample> queue;
	private Handler resultHandler;
	
	public AudioAnalysisThread() {
		queue = new LinkedBlockingQueue<InstantSample>();
	}
	
	public void setResultHandler(Handler resultHandler) {
		this.resultHandler = resultHandler;
	}
	
	public void run() {
		running = true;
		while (running) {
			try {
				analyzeSample(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void end() {
		running = false;
	}
	
	public void analyzeInstantSample(byte[] sample, long time) {
		try {
			queue.put(new InstantSample(sample, time));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected class InstantSample {
		private byte[] sample;
		private long time;
		
		public InstantSample(byte[] sample, long time) {
			this.sample = new byte[sample.length];
			this.time = time;
			System.arraycopy(sample, 0, this.sample, 0, sample.length);
		}
		
		public byte[] getSample() {
			return sample;
		}
		
		public long getTime() {
			return time;
		}
	}
	
	protected void sendMessageToHandler(Object message) {
		Message msg = new Message();
		msg.obj = message;
		resultHandler.sendMessage(msg);
	}
	
	abstract public void analyzeSample(InstantSample sample);
	abstract public void init(int sampleRate, int bufferSize);
}
