package com.senior.roadrunner.trackchooser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.internal.an;
import com.senior.roadrunner.R;

public class UploadActionBarView extends RelativeLayout implements
		AnimationListener {

	private final Animation mCycleFadeAnimation;
	private Animation rotateAnimation;

	public UploadActionBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCycleFadeAnimation = AnimationUtils.loadAnimation(context,
				R.anim.cycle_fade);
		mCycleFadeAnimation.setAnimationListener(this);
		rotateAnimation = AnimationUtils.loadAnimation(context,
				R.anim.anim_rotate);
	}

	public void animateBackground() {
		View animationBackground = getAnimationBackgroundView();
		if (null != animationBackground
				&& animationBackground.getVisibility() != View.VISIBLE) {
			animationBackground.startAnimation(mCycleFadeAnimation);
			animationBackground.setVisibility(View.VISIBLE);
		}
	}

	public void stopAnimatingBackground() {
		View animationBackground = getAnimationBackgroundView();
		if (null != animationBackground
				&& animationBackground.getVisibility() == View.VISIBLE) {
			animationBackground.setVisibility(View.GONE);
			animationBackground.clearAnimation();
		}
	}

	public void animateArrowLeft(){
    	ImageView animationImageView = getAnimateImageView();
    	animationImageView.startAnimation(rotateAnimation);
    	animationImageView.setImageResource(R.drawable.al);
    }
	public void animateArrowRight(){
    	ImageView animationImageView = getAnimateImageView();
    	animationImageView.startAnimation(rotateAnimation);
    	animationImageView.setImageResource(R.drawable.ar);
    }
	
	public ImageView getAnimateImageView() {
		return (ImageView) findViewById(R.id.iv_action_upload);
	}

	private View getAnimationBackgroundView() {
		return findViewById(R.id.v_action_upload_bg);
	}

	public void onAnimationEnd(Animation animation) {
		View animationBackground = getAnimationBackgroundView();
		if (null != animationBackground
				&& animationBackground.getVisibility() == View.VISIBLE) {
			animationBackground.startAnimation(animation);
		}
	}

	public void onAnimationRepeat(Animation animation) {
		// NO-OP
	}

	public void onAnimationStart(Animation animation) {
		// NO-OP
	}

}