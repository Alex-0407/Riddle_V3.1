package com.bingo.riddle.activity;

import com.bingo.lantern.R;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SplashView;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class SplashSpotActivity extends Activity {

	SplashView splashView;
	Context context;
	View splash;
	RelativeLayout splashLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this;
		// ��ʼ���ӿڣ�Ӧ��������ʱ�����
		// ������appId, appSecret, ����ģʽ
		AdManager.getInstance(context).init("69d82a8f65a5975f", "035255d0087c1397");

//		AdManager.getInstance(context).init("85aa56a59eac8b3d", "a14006f66f58d5d7");
		
		// �ڶ�����������Ŀ��activity�����ߴ���null����ΪsetIntent������ת��intent
		splashView = new SplashView(context, null);
		// �����Ƿ���ʾ����
		splashView.setShowReciprocal(true);
		// ���عرհ�ť
		splashView.hideCloseBtn(true);

		Intent intent = new Intent(context, MainFragmentActivity.class);
		splashView.setIntent(intent);
		splashView.setIsJumpTargetWhenFail(true);

		splash = splashView.getSplashView();
		setContentView(R.layout.splash_activity);
		splashLayout = ((RelativeLayout) findViewById(R.id.splashview));
		splashLayout.setVisibility(View.GONE);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
//		params.addRule(RelativeLayout.ABOVE, R.id.cutline);
		splashLayout.addView(splash, params);

		SpotManager.getInstance(context).showSplashSpotAds(context, splashView,
				new SpotDialogListener() {

					@Override
					public void onShowSuccess() {
						splashLayout.setVisibility(View.VISIBLE);
						splashLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pic_enter_anim_alpha));
						Log.d("youmisdk", "չʾ�ɹ�");
					}

					@Override
					public void onShowFailed() {
						Log.d("youmisdk", "չʾʧ��");
					}

					@Override
					public void onSpotClosed() {
						Log.d("youmisdk", "չʾ�ر�");
					}

					@Override
					public void onSpotClick(boolean isWebPath) {
						Log.i("YoumiAdDemo", "�������");
					}
				});

		// 2.�򵥵��÷�ʽ
		// ���û������Ҫ�󣬼�ʹ�ô˾伴��ʵ�ֲ�����չʾ
		// SpotManager.getInstance(context).showSplashSpotAds(context,
		// MainActivity.class);

	}

	@Override
	public void onBackPressed() {
		// ȡ�����˼�
	}

	@Override
	protected void onResume() {

		/**
		 * ����Ϊ����
		 */
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// land
		} else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// port
		}
	}

}
