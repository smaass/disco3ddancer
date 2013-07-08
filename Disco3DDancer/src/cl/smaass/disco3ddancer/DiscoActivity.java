package cl.smaass.disco3ddancer;

import rajawali.RajawaliActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cl.smaass.disco3ddancer.audio.AudioAnalyzer;
import cl.smaass.disco3ddancer.audio.IsThereMusicThread;

public class DiscoActivity extends RajawaliActivity {
	private ImageView mLoaderGraphic;
	private DiscoRenderer mRenderer;
	private AudioAnalyzer mAnalyzer;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		mSurfaceView.setKeepScreenOn(true);
		mRenderer = new DiscoRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		setRenderer(mRenderer);
		initLoader();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mAnalyzer = new AudioAnalyzer();
		mAnalyzer.startAnalysis(new IsThereMusicThread(), new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				Boolean musicPlaying = (Boolean) msg.obj;
				if (musicPlaying) {
					mRenderer.dance();
				}
				else {
					mRenderer.dontDance();
				}
				return true;
			}
		});
	}
	
	public void onDestroy() {
		super.onDestroy();
		mAnalyzer.endAnalysis();
	}

	protected void initLoader() {
		mLoaderGraphic = new ImageView(this);
	}

	public void showLoader() {
		mLayout.post(new Runnable() {
			public void run() {
				mLoaderGraphic.setId(1);
				mLoaderGraphic.setScaleType(ScaleType.CENTER);
				mLoaderGraphic.setImageResource(R.drawable.ic_launcher);
				if(mLoaderGraphic.getParent() == null)
					mLayout.addView(mLoaderGraphic);
				
				AnimationSet animSet = new AnimationSet(false);
		
				RotateAnimation anim1 = new RotateAnimation(360, 0,
						Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
						.5f);
				anim1.setRepeatCount(Animation.INFINITE);
				anim1.setDuration(2000);
				animSet.addAnimation(anim1);
		
				AlphaAnimation anim2 = new AlphaAnimation(0, 1);
				anim2.setRepeatCount(0);
				anim2.setDuration(1000);
				animSet.addAnimation(anim2);
		
				mLoaderGraphic.setAnimation(animSet);
			}
		});
	}

	public void hideLoader() {
		mLayout.post(new Runnable() {
			public void run() {
				AlphaAnimation anim = new AlphaAnimation(1, 0);
				anim.setRepeatCount(0);
				anim.setDuration(500);
				anim.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						mLoaderGraphic.setVisibility(View.INVISIBLE);
						mLayout.removeView(mLoaderGraphic);
						
					}
				});
				((AnimationSet) mLoaderGraphic.getAnimation())
						.addAnimation(anim);
			}
		});
	}
}
