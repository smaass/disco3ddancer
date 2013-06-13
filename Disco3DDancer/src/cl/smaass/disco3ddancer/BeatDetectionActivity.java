package cl.smaass.disco3ddancer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import cl.smaass.disco3ddancer.SignalProcessing.Result;
import cl.smaass.tarea3_grafica.R;

public class BeatDetectionActivity extends Activity {
    private static final int RECORDER_SAMPLERATE = 22050;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private int bufferSize;
	private AudioRecord recorder;
	private Thread recordingThread;
	private ByteArrayOutputStream stream;
	private Button captureButton;
	private boolean isCapturing;
	private byte result[];
	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.beat_detection_layout);
		 bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				 RECORDER_AUDIO_ENCODING);	 
	 }
	 
	 @Override
	 public void onStart() {
		 super.onStart();
		 captureButton = (Button) this.findViewById(R.id.captureButton);
		 setCaptureHandler(captureButton);
	 }
	 
	 public void setCaptureHandler(final Button button) {
		 final Context ctx = this;
		 button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isCapturing) {
					endCapture();
					button.setText("Record!");
				}
				else {
					startCapture();
					Toast.makeText(ctx, "Recording...", Toast.LENGTH_SHORT).show();
					button.setText("Stop");
				}
			}
		 });
	 }
	 
	 public void startCapture() {
		 recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                 RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
		 recorder.startRecording();
		 isCapturing = true;
		 recordingThread = new Thread(new Runnable() {
             @Override
             public void run() {
                     writeAudioDataToArray();
             }
		 },"AudioRecorder Thread");
     
		 recordingThread.start();
	 }
	 
	private void writeAudioDataToArray(){
		byte data[] = new byte[bufferSize];
		stream = new ByteArrayOutputStream();

		int read = 0;
         
		while(isCapturing){
			read = recorder.read(data, 0, bufferSize);
                         
			if(AudioRecord.ERROR_INVALID_OPERATION != read){
				stream.write(data, 0, bufferSize);
			}
		}
		result = stream.toByteArray();
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
       
	 
	 public void endCapture() {
		 isCapturing = false;
         recorder.stop();
         recorder.release();
         try {
			recordingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
         recorder = null;
         recordingThread = null;
         plotResults();
	 }
	 
	 public void plotResults() {
		 int sampleSize = result.length/2;
		 //int transformSize = closestPowerOfTwo(result.length/2);
		 float[] xvalues = incrementalArray(sampleSize);
		 float[] yvalues = new float[sampleSize];
		 for (int i=0; i<sampleSize; i++) {
			 yvalues[i] = result[2*i+1]*256 + result[2*i];
		 }
		 //Result transform = SignalProcessing.getFrequencySpectrum(yvalues, RECORDER_SAMPLERATE);
		 plot2d graph = new plot2d(this, xvalues, yvalues, 1);
		 ViewGroup v = (ViewGroup) this.findViewById(R.id.plotView);
	     v.addView(graph);
	 }
	 
	 public int closestPowerOfTwo(int n) {
		 int p = 1;
		 while (p <= n) p *= 2;
		 return p/2;
	 }
	 
	 public float[] incrementalArray(int size) {
		 float[] array = new float[size];
		 float increment = ((float) 1)/RECORDER_SAMPLERATE;
		 for (int i=0; i<size; i++) {
			 array[i] = i*increment;
		 }
		 return array;
	 }
}
