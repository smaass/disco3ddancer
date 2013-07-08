package cl.smaass.disco3ddancer.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TempoAnalysisThread extends AudioAnalysisThread {
	private static final int TEMPO_WINDOW_SECONDS = 4;
	private static final int MAX_ERROR_MILLIS = 50;
	private int historyBufferSize;
	private long E[];				// sound energy history buffer
	private long onsetStack[];		// stack to store onset times in a second
	private int stackPointer = 0;	// next free space in stack
	private long avgE;				// average local energy (last second)
	private long var;				// energy variance (last second)
	private int pos = 0;			// actual position in history buffer
	private int seconds = 0;		// elapsed seconds since last beat-detection

	@Override
	public void init(int sampleRate, int bufferSize) {
		this.historyBufferSize = 2*sampleRate/bufferSize;
		E = new long[historyBufferSize];
		onsetStack = new long[historyBufferSize*TEMPO_WINDOW_SECONDS];
	}
	
	@Override
	public void analyzeSample(InstantSample sample) {
		byte data[] = sample.getSample();
		int sampleLength = data.length/2;
		avgE = var = 0;

		// instant energy
		int e = 0;
		for (int i=0; i<sampleLength; i++)
			e += square(data[2*i+1]*256 + data[2*i]);
		E[pos++] = e;
			
		// average local energy
		for (int i=0; i<E.length; i++)
			avgE += E[i];
		avgE /= E.length;
				
		// variance of the energies in E
		for (int i=0; i<E.length; i++)
			var += square(E[i] - avgE);
		var /= E.length;
			
		// threshold constant
		double C = -0.0025714*var + 1.5142857;
			
		// onset detection
		if (e > avgE*C)
			onsetStack[stackPointer++] = sample.getTime();
			
		if (pos == historyBufferSize) {			// 1 second elapsed
			seconds++;
			pos = 0;
		}
		if (seconds == TEMPO_WINDOW_SECONDS) {	// time to analyze
			recognizeTempo(onsetStack, stackPointer);
			seconds = stackPointer = 0;
		}
	}
	
	private void recognizeTempo(long[] onsets, int size) {
		List<IntervalClass> classes = new LinkedList<IntervalClass>();
		int interval, minDifference, difference;
		IntervalClass bestClass = null;
		
		// For each pair of onset times t1, t2 (with t1 < t2)
		for (int i=0; i<size-1; i++) {
			for (int j=i+1; j<size; j++) {
				interval = (int) (onsets[j] - onsets[i]);
				minDifference = Integer.MAX_VALUE;
				
				// Get class C such that |avg(C) - interval| is minimum
				for (IntervalClass C : classes) {
					difference = Math.abs(C.getAverage() - interval);
					if (difference < minDifference) {
						minDifference = difference;
						bestClass = C;
					}
				}
				
				if (minDifference < MAX_ERROR_MILLIS)
					bestClass.addInterval(interval);
				else
					classes.add(new IntervalClass(interval));
			}
		}
		
		Collections.sort(classes);
		int period = classes.get(classes.size()-1).getAverage();
		int tempo = 60000/period;
		sendMessageToHandler(tempo+"");
	}
	
	private class IntervalClass implements Comparable<IntervalClass> {
		private int count;
		private int average;
		
		public IntervalClass(int interval) {
			average = interval;
			count = 1;
		}
		
		public int getAverage() {
			return average;
		}
		
		public void addInterval(int interval) {
			average = (average*count + interval)/(++count);
		}
		
		public int getScore() {
			return count;
		}

		@Override
		public int compareTo(IntervalClass another) {
			if (this.getScore() == another.getScore())
				return 0;
			if (this.getScore() < another.getScore())
				return -1;
			return 1;
		}
	}
	
	private long square(long num) {
		return num*num;
	}
}
