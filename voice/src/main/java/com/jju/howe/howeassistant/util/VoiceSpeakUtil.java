package com.jju.howe.howeassistant.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

public class VoiceSpeakUtil {
	SpeechSynthesizer speechSynthesizer;
	SpeakCallback callback;
	private String mSampleDirPath;
	private static final String SAMPLE_DIR_NAME = "baiduTTS";
	private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
	private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
	private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
	private static final String LICENSE_FILE_NAME = "temp_license";
	private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
	private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
	private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
	protected static final int FINISH = 0;
	protected static final int ONERROR = 1;

	public VoiceSpeakUtil(Context context) {

		initialEnv(context);

		initialTts(context);

	}

	private void initialEnv(Context context) {
		if (mSampleDirPath == null) {
			String sdcardPath = Environment.getExternalStorageDirectory()
					.toString();
			mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
		}
		makeDir(mSampleDirPath);
		copyFromAssetsToSdcard(context, false, SPEECH_FEMALE_MODEL_NAME,
				mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
		copyFromAssetsToSdcard(context, false, SPEECH_MALE_MODEL_NAME,
				mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
		copyFromAssetsToSdcard(context, false, TEXT_MODEL_NAME, mSampleDirPath
				+ "/" + TEXT_MODEL_NAME);
		copyFromAssetsToSdcard(context, false, LICENSE_FILE_NAME,
				mSampleDirPath + "/" + LICENSE_FILE_NAME);
		copyFromAssetsToSdcard(context, false, "english/"
				+ ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
				+ ENGLISH_SPEECH_FEMALE_MODEL_NAME);
		copyFromAssetsToSdcard(context, false, "english/"
				+ ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
				+ ENGLISH_SPEECH_MALE_MODEL_NAME);
		copyFromAssetsToSdcard(context, false, "english/"
				+ ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
				+ ENGLISH_TEXT_MODEL_NAME);
	}

	private void makeDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
	 * 
	 * @param isCover
	 *            是否覆盖已存在的目标文件
	 * @param source
	 * @param dest
	 */
	private void copyFromAssetsToSdcard(Context context, boolean isCover,
			String source, String dest) {
		File file = new File(dest);
		if (isCover || (!isCover && !file.exists())) {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				is = context.getResources().getAssets().open(source);
				String path = dest;
				fos = new FileOutputStream(path);
				byte[] buffer = new byte[1024];
				int size = 0;
				while ((size = is.read(buffer, 0, 1024)) >= 0) {
					fos.write(buffer, 0, size);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initialTts(final Context context) {
		this.speechSynthesizer = SpeechSynthesizer.getInstance();
		this.speechSynthesizer.setContext(context);
		this.speechSynthesizer
				.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {

					@Override
					public void onSynthesizeStart(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSynthesizeFinish(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSynthesizeDataArrived(String arg0,
							byte[] arg1, int arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSpeechStart(String arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSpeechProgressChanged(String arg0, int arg1) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSpeechFinish(String arg0) {
						// Utils.showToast(context, "hahaha");
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						msg.what = FINISH;
						VoiceSpeakUtil.this.mHandler.sendMessage(msg);
					}

					@Override
					public void onError(String arg0, SpeechError arg1) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						msg.what = ONERROR;
						msg.obj = arg0;
						VoiceSpeakUtil.this.mHandler.sendMessage(msg);
					}
				});
		// 文本模型文件路径 (离线引擎使用)
		this.speechSynthesizer.setParam(
				SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath
						+ "/" + TEXT_MODEL_NAME);
		// 声学模型文件路径 (离线引擎使用)
		this.speechSynthesizer.setParam(
				SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath
						+ "/" + SPEECH_FEMALE_MODEL_NAME);
		// 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
		this.speechSynthesizer.setParam(
				SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
						+ LICENSE_FILE_NAME);
		// 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
		this.speechSynthesizer.setAppId("16096017");
		// 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
		this.speechSynthesizer.setApiKey("6UgltGGWGyMMEOpcrg85EGG8",
				"gvUECdiZGCqZOxsLBRdhEKK5WoVRwIl9");
		// 授权检测接口
		AuthInfo authInfo = this.speechSynthesizer.auth(TtsMode.MIX);
		if (authInfo.isSuccess()) {

			this.speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER,
					"0");
			speechSynthesizer.initTts(TtsMode.MIX);
//			int result = speechSynthesizer.loadEnglishModel(mSampleDirPath
//					+ "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
//					+ ENGLISH_SPEECH_FEMALE_MODEL_NAME);
			// Utils.showToast(context,"loadEnglishModel result=" + result);
		} else {
			String errorMsg = authInfo.getTtsError().getDetailMessage();
			// Utils.showToast(context,"auth failed errorMsg=" + errorMsg);
		}
	}

	public void speak(String content, SpeakCallback callback) {
		this.callback = callback;
		speechSynthesizer.speak(content);
	}

	public void speak(String content) {
		this.callback = new SpeakCallback() {

			@Override
			public void speakFinish() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(String string) {
				// TODO Auto-generated method stub

			}
		};
		speechSynthesizer.speak(content);
	}

	public interface SpeakCallback {
		public void onError(String string);

		public void speakFinish();
	}

	private Handler mHandler = new Handler() {

		/*
		 * @param msg
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			switch (what) {
			case FINISH:
				callback.speakFinish();
				break;
			case ONERROR:
				callback.onError((String) msg.obj);
				break;

			default:
				break;
			}
		}

	};
}
