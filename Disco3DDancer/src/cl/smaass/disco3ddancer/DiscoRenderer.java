package cl.smaass.disco3ddancer;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.animation.Animation3D;
import rajawali.animation.Animation3D.RepeatMode;
import rajawali.animation.ColorAnimation3D;
import rajawali.animation.EllipticalOrbitAnimation3D;
import rajawali.animation.EllipticalOrbitAnimation3D.OrbitDirection;
import rajawali.animation.mesh.SkeletalAnimationObject3D;
import rajawali.animation.mesh.SkeletalAnimationSequence;
import rajawali.lights.ALight;
import rajawali.lights.PointLight;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.SphereMapMaterial;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.SphereMapTexture;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.ObjParser;
import rajawali.parser.md5.MD5AnimParser;
import rajawali.parser.md5.MD5MeshParser;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

public class DiscoRenderer extends RajawaliRenderer {
	private PointLight mLight1, mLight2;
	private List<ALight> clubLights;
	private SkeletalAnimationObject3D mDancer;
	private SkeletalAnimationSequence step1, idle;
	private Animation3D mCameraAnim, mLightAnim1, mLightAnim2;
	private Camera mCamera;
	private Vector3 mFocal;
	private Vector3 mPeriapsis;
	private BaseObject3D mFloor;
	private BaseObject3D mBall;
	private boolean isDancing;
	
	public DiscoRenderer(Context context) {
		super(context);
		setFrameRate(60);
	}
	
	public void dance() {
		if (mDancer != null && step1 != null && !isDancing) {
			mDancer.transitionToAnimationSequence(step1, 1000);
			isDancing = true;
		}
	}
	
	public void dontDance() {
		if (mDancer != null && idle != null & isDancing) {
			mDancer.transitionToAnimationSequence(idle, 1000);
			isDancing = false;
		}
	}
	
	protected void initScene() {
		initLights();
		mFocal = new Vector3(0, 2, 0);
		
		mCamera = getCurrentCamera();
		mCamera.setPosition(0, 4, 7);
		mCamera.setLookAt(0.0f, 2f, 0.0f);
		mPeriapsis = new Vector3(0, 2, 7);
		
		SphereMapTexture sphereMapTexture = new SphereMapTexture(R.drawable.squares_sphere_map);
		SphereMapMaterial reflectiveMaterial = new SphereMapMaterial();
		reflectiveMaterial.setSphereMapStrength(2);
		reflectiveMaterial.setUseSingleColor(true);		
		try {
			reflectiveMaterial.addTexture(sphereMapTexture);
		} catch (TextureException e) {
			e.printStackTrace();
		}
		
		mBall = initDiscoBall(.5f);
		mBall.setPosition(0,4f,0);
		mBall.setMaterial(reflectiveMaterial);
		mBall.setColor(0xff666666);
		mBall.setLights(clubLights);
		mDancer = initDancer(.2f);
		mDancer.setPosition(0,0,0);
		mDancer.setLights(clubLights);
		mDancer.play();
		
		final SimpleMaterial material = new SimpleMaterial();
		material.setUseSingleColor(true);
		
		mFloor = new Plane(2f, 2f, 1, 1, Axis.Y);
		mFloor.setMaterial(material);
		mFloor.setColor(0xaaff1111);
		mFloor.setPosition(0,0,0);
		final Animation3D anim = new ColorAnimation3D(0xaaff1111, 0xffffff11);
		anim.setTransformable3D(mFloor);
		anim.setDuration(2000);
		anim.setRepeatMode(RepeatMode.REVERSE_INFINITE);
		anim.play();
		
		mCameraAnim = new EllipticalOrbitAnimation3D(mFocal, mPeriapsis, 0.0,
				360, OrbitDirection.CLOCKWISE);
		mCameraAnim.setDuration(10000);
		mCameraAnim.setRepeatMode(Animation3D.RepeatMode.INFINITE);
		mCameraAnim.setTransformable3D(mCamera);
		mCameraAnim.play();
		
		addChild(mDancer);
		addChild(mFloor);
		addChild(mBall);
		registerAnimation(anim);		
		registerAnimation(mCameraAnim);
	}
	
	private void initLights() {
		Vector3 positionLight1 = new Vector3(-2, 3,-2);
		Vector3 positionLight2 = new Vector3(2, 3, 2);
		
		clubLights = new LinkedList<ALight>();
		mLight1 = new PointLight();
		mLight1.setPosition(positionLight1);
		mLight1.setPower(1.5f);
		mLight1.setColor(0.1f, 1f, 0.5f);
		clubLights.add(mLight1);
		
		mLight2 = new PointLight();
		mLight2.setPosition(positionLight2);
		mLight2.setPower(1.5f);
		mLight2.setColor(0.5f, 0.1f, 1f);
		clubLights.add(mLight2);
		
		mLightAnim1 = new EllipticalOrbitAnimation3D(new Vector3(0,3,0), positionLight1, 0.0,
				360, OrbitDirection.CLOCKWISE);
		mLightAnim1.setDuration(3000);
		mLightAnim1.setRepeatMode(Animation3D.RepeatMode.INFINITE);
		mLightAnim1.setTransformable3D(mLight1);
		mLightAnim1.play();
		
		mLightAnim2 = new EllipticalOrbitAnimation3D(new Vector3(0,3,0), positionLight2, 0.0,
				360, OrbitDirection.CLOCKWISE);
		mLightAnim2.setDuration(3000);
		mLightAnim2.setRepeatMode(Animation3D.RepeatMode.INFINITE);
		mLightAnim2.setTransformable3D(mLight2);
		mLightAnim2.play();
		
		registerAnimation(mLightAnim1);
		registerAnimation(mLightAnim2);
	}
	
	private BaseObject3D initDiscoBall(float scale) {
		ObjParser objParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.disco_ball);
		try {
			objParser.parse();
			BaseObject3D ball = objParser.getParsedObject();
			ball.setScale(scale);
			return ball;
		} catch(ParsingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private SkeletalAnimationObject3D initDancer(float scale) {
		try {
			MD5MeshParser meshParser = new MD5MeshParser(this, R.raw.disco_guy_mesh);
			meshParser.parse();
			
			MD5AnimParser animParser = new MD5AnimParser("step1", this, R.raw.disco_guy_step1);
			animParser.parse();
			step1 = ((SkeletalAnimationSequence) animParser.getParsedAnimationSequence()).getSkippedSequence(2f);
			
			animParser = new MD5AnimParser("idle", this, R.raw.disco_guy_standing);
			animParser.parse();
			idle = (SkeletalAnimationSequence) animParser.getParsedAnimationSequence();
			
			SkeletalAnimationObject3D dancer = (SkeletalAnimationObject3D) meshParser.getParsedAnimationObject();
			dancer.setAnimationSequence(idle);
			dancer.setScale(scale);
			dancer.setRotY(180);
			this.isDancing = false;
			return dancer;
					
		} catch(ParsingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		((DiscoActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((DiscoActivity) mContext).hideLoader();
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
	}
}

