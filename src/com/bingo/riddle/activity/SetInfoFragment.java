package com.bingo.riddle.activity;

import java.util.ArrayList;

import net.youmi.android.AdManager;
import net.youmi.android.listener.Interface_ActivityListener;
import net.youmi.android.offers.EarnPointsOrderInfo;
import net.youmi.android.offers.EarnPointsOrderList;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import net.youmi.android.offers.PointsEarnNotify;
import net.youmi.android.offers.PointsManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bingo.lantern.R;
import com.bingo.util.CopyImgToSDCard;
import com.tencent.connect.UserInfo;
import com.tencent.connect.avatar.QQAvatar;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.tool.BaseUIListener;
import com.tencent.tool.TecentConstants;
import com.tencent.tool.ThreadManager;
import com.tencent.tool.Util;
import com.wechat.tool.WeChatConstants;

public class SetInfoFragment extends Fragment implements PointsChangeNotify,
		PointsEarnNotify {
	private View rootView;
	private TextView qq;
	private TextView qqzone;
	private TextView wechat;
	private TextView wechatcircle;
	private TextView love;
	private TextView advise;
	private TextView share;
	private TextView headtip;
	private TextView login;
	private UserInfo mInfo;
	private TextView mUserInfo;
	private ImageView mUserLogo;

	private String userName;

	private static boolean isServerSideLogin = false;

	private static final int REQUEST_SET_AVATAR = 2;

	private static final String TAG = SetInfoFragment.class.getName();

	public static String mAppid;

	// QZone���� SHARE_TO_QQ_TYPE_DEFAULT ͼ�ģ�SHARE_TO_QQ_TYPE_IMAGE ��ͼ
	// private int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
	private String SCOPE = "all";

	// IWXAPI �ǵ�����app��΢��ͨ�ŵ�openapi�ӿ�
	private IWXAPI api;
	private static final int THUMB_SIZE = 150;
	private static final String SDCARD_ROOT = Environment
			.getExternalStorageDirectory().getAbsolutePath();

	/**
	 * ��ʾ�������Ŀؼ�
	 */
	TextView mTextViewPoints;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_set_info, container,
				false);

		qq = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_qq);
		qqzone = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_qqzone);
		wechat = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_wechat);
		wechatcircle = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_wechatcircle);
		love = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_love);
		login = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_login);
		mUserInfo = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_name);
		mUserLogo = (ImageView) rootView
				.findViewById(R.id.set_personal_info_activity_iv_head_pic);
		headtip = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_ll_head_tip);
		advise = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_advise);
		share = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_share);
		
		mTextViewPoints = (TextView) rootView
				.findViewById(R.id.set_personal_info_activity_tv_score);

		// ��5.3.0�汾�𣬿ͻ��˻����йܽ��� int ת��Ϊ float
		float pointsBalance = PointsManager.getInstance(getActivity())
				.queryPoints();
		mTextViewPoints.setText(" " + pointsBalance);

		updateLoginButton();

		mAppid = TecentConstants.APP_ID;
		TecentConstants.mTencent = Tencent
				.createInstance(mAppid, getActivity());

		// ͨ��WXAPIFactory��������ȡIWXAPI��ʵ��
		api = WXAPIFactory.createWXAPI(getActivity(), WeChatConstants.APP_ID,
				false);

		// ����android ����ǽsdk 5.0.0֮��֧�ֶ�������������������Ĳ���UI
		// deployOfferBrowserConfig();

		// (��ѡ)�ر�����log��������鿪������Ƕ�����׹����в�Ҫ�رգ��Է�����ʱ��׽�����Ϣ��������ʱ���Կ��ٶ�λ����
		// AdManager.getInstance(Context context).setEnableDebugLog(false);

		// ��ʼ���ӿڣ�Ӧ��������ʱ����ã�������appId, appSecret
		AdManager.getInstance(getActivity()).init("69d82a8f65a5975f",
				"035255d0087c1397");

		// ���������ʹ�û���ǽ�ķ������ص�,
		// 1.��Ҫ����sdk�����ڲ��÷������ص�
		// 2.���鿪���ߴ����Լ�ϵͳ���û�id���磺�����˺�֮��ģ�����������50���ַ������ڣ�
		// 3.����������OffersManager.getInstance(this).onAppLaunch();����֮ǰ����ʹ�÷������ص�

		// OffersManager.setUsingServerCallBack(true);
		// OffersManager.getInstance(this).setCustomUserId("user_id");

		// ���ʹ�û��ֹ�棬����ص��û��ֹ��ĳ�ʼ���ӿ�:
		OffersManager.getInstance(getActivity()).onAppLaunch();

		// (��ѡ)ע����ּ���-��ʱ��ػ�û��ֵı䶯���
		PointsManager.getInstance(getActivity()).registerNotify(this);

		// (��ѡ)ע����ֶ���׬ȡ������sdk v4.10�汾�������ܣ�
		PointsManager.getInstance(getActivity()).registerPointsEarnNotify(this);

		// (��ѡ)�����Ƿ���֪ͨ����ʾ���������ʾ��Ĭ��Ϊtrue����ʶ����������Ϊfalse��رա���sdk v4.10�汾�������ܣ�
		// AdManager.setIsDownloadTipsDisplayOnNotification(false);

		// (��ѡ)���ð�װ��ɺ��Ƿ���֪ͨ����ʾ�Ѱ�װ�ɹ���֪ͨ��Ĭ��Ϊtrue����ʶ����������Ϊfalse��رա���sdk
		// v4.10�汾�������ܣ�
		// AdManager.setIsInstallationSuccessTipsDisplayOnNotification(false);

		// (��ѡ)�����Ƿ���֪ͨ����ʾ����׬ȡ��ʾ��Ĭ��Ϊtrue����ʶ����������Ϊfalse��رա�
		// ��������߲����˷������ص����ֵķ�ʽ����ô��������������Ч
		// PointsManager.setEnableEarnPointsNotification(false);

		// (��ѡ)�����Ƿ�������׬ȡ��Toast��ʾ��Ĭ��Ϊtrue����ʶ����������Ϊfalse��رա�
		// ��������߲����˷������ص����ֵķ�ʽ����ô��������������Ч
		// PointsManager.setEnableEarnPointsToastTips(false);

		// -------------------------------------------------------------------------------------------
		// ����ǽSDK 5.3.0 ����������������Ϊ�����ӿ�

		// (��ѡ) ��ȡ��ǰӦ�õ�ǩ��md5�ַ���������������΢��appidʱʹ��
		// ע�⣺��ȡ��ȷ��Ӧ�ò��õ��Ƿ����汾��ǩ��������debugǩ��

		// Log.d("youmi", String.format("������%s\nǩ��md5ֵ��%s",
		// this.getPackageName(),
		// OffersManager.getInstance(this).getSignatureMd5String()));

		// (��Ҫ)
		// �����������Ҫ������������ǽ����Ҫ�������������ע��΢��appid�������appidΪ��Ӧ����΢��ƽ̨��ע���ȡ�õ���appid��
		// 1. ΢�ŵ�appid���뿪������΢�Ź���������ע��
		// 2. ���ע��ʧ��(����false)����ο�/doc/����AndroidSDK��������.html
		boolean isRegisterSuccess = OffersManager.getInstance(getActivity())
				.registerToWx("wx735b468e43f5d797");
//		Toast.makeText(getActivity(),
//				String.format("ע��΢��appid %s", isRegisterSuccess ? "�ɹ�" : "ʧ��"),
//				Toast.LENGTH_SHORT).show();

		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		// ����ͷ��
		mUserLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				onClickSetAvatar();
			}
		});

		// ���͵�QQ
		qq.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				// onClickShareApp();
				onClickShareOnlyImg();
			}
		});

		// ���͵�QQ�ռ�
		qqzone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				shareToQzone();
			}
		});

		// ����΢��
		wechat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				sendImgToWeChat();
				// Toast.makeText(getActivity(), "���ڿ����У������ڴ���",
				// Toast.LENGTH_SHORT)
				// .show();
			}
		});

		// ����΢������Ȧ
		wechatcircle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				sendImgToWeChatZone();
				// Toast.makeText(getActivity(), "���ڿ����У������ڴ���",
				// Toast.LENGTH_SHORT)
				// .show();
			}
		});

		// ��¼
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				// doLoginToQQ();
				onClickLogin();
			}
		});

		// �ҵ��ղ�
		love.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				Toast.makeText(getActivity(), "�뵽QQ�ղز鿴", Toast.LENGTH_SHORT)
						.show();
			}
		});

		// ����Ӧ��
		advise.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				// ���÷�ʽһ��ֱ�Ӵ�ȫ������ǽ
				// OffersManager.getInstance(this).showOffersWall();

				// ���÷�ʽ����ֱ�Ӵ�ȫ������ǽ�����Ҽ�������ǽ�˳����¼�onDestory
				OffersManager.getInstance(getActivity()).showOffersWall(
						new Interface_ActivityListener() {

							/**
							 * ������ǽ���ٵ�ʱ�򣬼�����ǽ��Activity������onDestory��ʱ��ص�
							 */
							@Override
							public void onActivityDestroy(Context context) {
								Toast.makeText(context, "ȫ������ǽ�˳���",
										Toast.LENGTH_SHORT).show();
							}
						});
			}
		});
		
		//�������ʯ
		share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Animation shake = AnimationUtils.loadAnimation(context,
						R.anim.shake);
				v.startAnimation(shake);
				// ���÷�ʽһ��ֱ�Ӵ�ȫ�������������ǽ
				// OffersManager.getInstance(this).showShareWall();

				// ���÷�ʽ����ֱ�Ӵ�ȫ�������������ǽ�����Ҽ��������������ǽ�˳����¼�onDestory
				OffersManager.getInstance(getActivity()).showShareWall(new Interface_ActivityListener() {

					/**
					 * �������������ǽ���ٵ�ʱ�򣬼������������ǽ��Activity������onDestory��ʱ��ص�
					 */
					@Override
					public void onActivityDestroy(Context context) {
						Toast.makeText(context, "ȫ�������������ǽ�˳���", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	// �ı��¼��ť��ʽ
	private void updateLoginButton() {
		if (TecentConstants.mTencent != null
				&& TecentConstants.mTencent.isSessionValid()) {
			if (isServerSideLogin) {
				updateUserInfo();
				// login.setTextColor(Color.BLUE);
				login.setText("QQ�ŵ�¼");
			} else {
				updateUserInfo();
				login.setTextColor(Color.RED);
				login.setText("�˳��ʺ�");
			}
		} else {
			updateUserInfo();
			// login.setTextColor(Color.BLUE);
			login.setText("QQ�ŵ�¼");
		}
	}

	// �޸��û���Ϣ
	private void updateUserInfo() {
		if (TecentConstants.mTencent != null
				&& TecentConstants.mTencent.isSessionValid()) {
			IUiListener listener = new IUiListener() {

				@Override
				public void onError(UiError e) {

				}

				@Override
				public void onComplete(final Object response) {
					Message msg = new Message();
					msg.obj = response;
					msg.what = 0;
					mHandler.sendMessage(msg);
					new Thread() {

						@Override
						public void run() {
							JSONObject json = (JSONObject) response;
							if (json.has("figureurl")) {
								Bitmap bitmap = null;
								try {
									bitmap = Util.getbitmap(json
											.getString("figureurl_qq_2"));
								} catch (JSONException e) {

								}
								Message msg = new Message();
								msg.obj = bitmap;
								msg.what = 1;
								mHandler.sendMessage(msg);
							}
						}

					}.start();
				}

				@Override
				public void onCancel() {

				}
			};
			mInfo = new UserInfo(getActivity(),
					TecentConstants.mTencent.getQQToken());
			mInfo.getUserInfo(listener);

		} else {
			mUserInfo.setText("δ��¼");
			login.setTextColor(Color.BLACK);
			mUserLogo.setBackgroundResource(R.drawable.default_avatar_shadow);
			mUserLogo.setVisibility(android.view.View.VISIBLE);
		}
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
						mUserInfo.setVisibility(android.view.View.VISIBLE);
						userName = response.getString("nickname");
						mUserInfo.setText(userName);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else if (msg.what == 1) {
				Bitmap bitmap = (Bitmap) msg.obj;
				mUserLogo.setImageBitmap(bitmap);
				mUserLogo.setVisibility(android.view.View.VISIBLE);
			}
		}

	};

	// ��¼��QQ
	private void onClickLogin() {
		if (!TecentConstants.mTencent.isSessionValid()) {
			TecentConstants.mTencent.login(this, "all", loginListener);
			isServerSideLogin = false;
			Log.d("SDKQQAgentPref",
					"FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
		} else {
			if (isServerSideLogin) { // Server-Side ģʽ�ĵ�½, ���˳����ٽ���SSO��½
				TecentConstants.mTencent.logout(getActivity());
				TecentConstants.mTencent.login(this, "all", loginListener);
				isServerSideLogin = false;
				Log.d("SDKQQAgentPref",
						"FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
				return;
			}
			TecentConstants.mTencent.logout(getActivity());
			mUserInfo.setText("δ��¼");
			mUserLogo.setBackgroundResource(R.drawable.default_avatar_shadow);
			updateLoginButton();
		}
	}

	public static void initOpenidAndToken(JSONObject jsonObject) {
		try {
			String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
			String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
			String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
			if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
					&& !TextUtils.isEmpty(openId)) {
				TecentConstants.mTencent.setAccessToken(token, expires);
				TecentConstants.mTencent.setOpenId(openId);
			}
		} catch (Exception e) {
		}
	}

	IUiListener loginListener = new BaseUiListener() {
		@Override
		protected void doComplete(JSONObject values) {
			Log.d("SDKQQAgentPref",
					"AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
			initOpenidAndToken(values);
			updateUserInfo();
			updateLoginButton();
		}
	};

	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
			if (null == response) {
				Util.showResultDialog(getActivity(), "����Ϊ��", "��¼ʧ��");
				return;
			}
			JSONObject jsonResponse = (JSONObject) response;
			if (null != jsonResponse && jsonResponse.length() == 0) {
				Util.showResultDialog(getActivity(), "����Ϊ��", "��¼ʧ��");
				return;
			}
			// Util.showResultDialog(getActivity(), response.toString(),
			// "��¼�ɹ�");

			doComplete((JSONObject) response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			Util.toastMessage(getActivity(), "onError: " + e.errorDetail);
			Util.dismissDialog();
		}

		@Override
		public void onCancel() {
			Util.toastMessage(getActivity(), "onCancel: ");
			Util.dismissDialog();
			if (isServerSideLogin) {
				isServerSideLogin = false;
			}
		}
	}

	// ����ͼƬ��QQ
	private void onClickShareOnlyImg() {
		ThreadManager.getMainHandler().post(new Runnable() {
			@Override
			public void run() {
				Bundle params = new Bundle();
				CopyImgToSDCard.CopyImg(getActivity(), "app.png");
				params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,
						TecentConstants.localImgUrl);
				params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "���տ�");
				params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
						QQShare.SHARE_TO_QQ_TYPE_IMAGE);
				// params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
				// QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
				TecentConstants.mTencent.shareToQQ(getActivity(), params,
						qqtestShareListener);
			}
		});
	}

	// ����QQ�ռ�
	private void shareToQzone() {
		ThreadManager.getMainHandler().post(new Runnable() {
			@Override
			public void run() {
				final Bundle params = new Bundle();
				params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "Ӧ�ã����տ⣩����");// ����
				params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "�����������ҳ��");
				params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,
						TecentConstants.shareTargetUrl);// ����
				// ֧�ִ����imageUrl
				ArrayList<String> imageUrls = new ArrayList<String>();
				for (int i = 0; i < 1; i++) {
					imageUrls.add(TecentConstants.shareImgUrl);
				}
				params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL,
						imageUrls);
				TecentConstants.mTencent.shareToQzone(getActivity(), params,
						qZoneShareListener);
			}
		});
	}

	// ����΢��
	private void startWeChat() {
		ThreadManager.getMainHandler().post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getActivity(), "result=" + api.openWXApp(),
						Toast.LENGTH_LONG).show();
			}
		});
	}

	// ����ͼƬ��΢��
	private void sendImgToWeChat() {
		CopyImgToSDCard.CopyImg(getActivity(), "app.png");
		String path = SDCARD_ROOT + "/app.png";

		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(path);

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;

		Bitmap bmp = BitmapFactory.decodeFile(path);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE,
				THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);

	}

	// ����ͼƬ��΢������Ȧ
	private void sendImgToWeChatZone() {
		CopyImgToSDCard.CopyImg(getActivity(), "app.png");
		String path = SDCARD_ROOT + "/app.png";

		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(path);

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;

		Bitmap bmp = BitmapFactory.decodeFile(path);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE,
				THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		api.sendReq(req);
	}

	IUiListener qqtestShareListener = new IUiListener() {
		@Override
		public void onCancel() {
			Log.d(getTag(), "-----onCancel()------");
		}

		@Override
		public void onComplete(Object response) {
			// TODO Auto-generated method stub
			Util.toastMessage(getActivity(),
					"onComplete: " + response.toString());
			Toast.makeText(getActivity(), "result=" + response.toString(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(UiError e) {
			// TODO Auto-generated method stub
			Util.toastMessage(getActivity(), "onError: " + e.errorMessage, "e");
		}
	};

	IUiListener qZoneShareListener = new IUiListener() {

		@Override
		public void onCancel() {
			Util.toastMessage(getActivity(), "onCancel: ");
		}

		@Override
		public void onError(UiError e) {
			// TODO Auto-generated method stub
			Util.toastMessage(getActivity(), "onError: " + e.errorMessage, "e");
		}

		@Override
		public void onComplete(Object response) {
			// TODO Auto-generated method stub
			Util.toastMessage(getActivity(),
					"onComplete: " + response.toString());
		}

	};

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	private void onClickSetAvatar() {
		if (this.ready(getActivity())) {
			Intent intent = new Intent();
			// ����Pictures����Type�趨Ϊimage
			intent.setType("image/*");
			// ʹ��Intent.ACTION_GET_CONTENT���Action
			intent.setAction(Intent.ACTION_GET_CONTENT);
			// ȡ����Ƭ�󷵻ر�����
			startActivityForResult(intent, REQUEST_SET_AVATAR);
			// �� onActivityResult �е��� doSetAvatar
		}
	}

	private void doSetAvatar(Uri uri) {
		QQAvatar qqAvatar = new QQAvatar(TecentConstants.mTencent.getQQToken());
		qqAvatar.setAvatar(getActivity(), uri,
				new BaseUIListener(getActivity()), R.anim.zoomout);
	}

	public boolean ready(Activity activity) {
		if (TecentConstants.mTencent == null) {
			return false;
		}
		boolean ready = TecentConstants.mTencent.isSessionValid()
				&& TecentConstants.mTencent.getQQToken().getOpenId() != null;
		if (!ready) {
			Toast.makeText(getActivity(), "��¼֮������޸�ͷ��Ŷ!", Toast.LENGTH_SHORT)
					.show();
		}
		return ready;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "-->onActivityResult " + requestCode + " resultCode="
				+ resultCode);
		if (requestCode == REQUEST_SET_AVATAR
				&& resultCode == Activity.RESULT_OK) {
			doSetAvatar(data.getData());
		} else {
			TecentConstants.mTencent.onActivityResultData(requestCode,
					resultCode, data, loginListener);
			if (requestCode == Constants.REQUEST_API) {
				if (resultCode == Constants.RESULT_LOGIN) {
					Tencent.handleResultData(data, loginListener);
					Log.d(TAG, "-->onActivityResult handle logindata");
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * �˳�ʱ������Դ
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		// ����ѡ��ע�����ּ���
		// �����onCreate������PointsManager.getInstance(this).registerNotify(this)���л�����������ע�ᣬ����������ע��
		PointsManager.getInstance(getActivity()).unRegisterNotify(this);

		// ����ѡ��ע�����ֶ���׬ȡ����
		// �����onCreate������PointsManager.getInstance(this).registerPointsEarnNotify(this)
		// ���л��ֶ���׬ȡ������ע�ᣬ����������ע��
		PointsManager.getInstance(getActivity()).unRegisterPointsEarnNotify(
				this);

		// ���ջ��ֹ��ռ�õ���Դ
		OffersManager.getInstance(getActivity()).onAppExit();
	}

	/**
	 * ���������䶯ʱ���ͻ�ص������������ص�����ִ����UI�߳��У�
	 *
	 * ��5.3.0�汾�𣬿ͻ��˻����йܽ��� int ת��Ϊ float
	 */
	@Override
	public void onPointBalanceChange(float pointsBalance) {
		mTextViewPoints.setText(" " + pointsBalance);
	}

	/**
	 * ���ֶ���׬ȡʱ��ص������������ص�����ִ����UI�߳��У�
	 */
	@Override
	public void onPointEarn(Context arg0, EarnPointsOrderList list) {
		// ��������
		for (int i = 0; i < list.size(); ++i) {
			EarnPointsOrderInfo info = list.get(i);
			Toast.makeText(getActivity(), info.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

}