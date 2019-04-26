package com.jju.howe.howeassistant.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.sunflower.FlowerCollector;
import com.jju.howe.howeassistant.R;
import com.jju.howe.howeassistant.action.CallAction;
import com.jju.howe.howeassistant.action.CallView;
import com.jju.howe.howeassistant.action.MessageView;
import com.jju.howe.howeassistant.action.OpenAppAction;
import com.jju.howe.howeassistant.action.OpenQA;
import com.jju.howe.howeassistant.action.ScheduleCreate;
import com.jju.howe.howeassistant.action.ScheduleView;
import com.jju.howe.howeassistant.action.SearchAction;
import com.jju.howe.howeassistant.action.SearchApp;
import com.jju.howe.howeassistant.action.SearchWeather;
import com.jju.howe.howeassistant.action.SendMessage;
import com.jju.howe.howeassistant.bean.AnswerBean;
import com.jju.howe.howeassistant.bean.DataBean;
import com.jju.howe.howeassistant.bean.DatetimeBean;
import com.jju.howe.howeassistant.bean.MainBean;
import com.jju.howe.howeassistant.bean.ResultBean;
import com.jju.howe.howeassistant.bean.SlotsBean;
import com.jju.howe.howeassistant.util.CommonConst;
import com.jju.howe.howeassistant.util.JsonParser;
import com.jju.howe.howeassistant.util.MicRecordingView;
import com.jju.howe.howeassistant.voice.AutoCheck;
import com.jju.howe.howeassistant.voice.InitConfig;
import com.jju.howe.howeassistant.voice.MySyntherizer;
import com.jju.howe.howeassistant.voice.OfflineResource;
import com.jju.howe.howeassistant.voice.UiMessageListener;
import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.AbsCredentialProvider;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.config.ClientConfiguration;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.listener.AudioRecognizeTimeoutListener;
import com.tencent.aai.log.AAILogger;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencent.aai.model.type.AudioRecognizeConfiguration;
import com.tencent.aai.model.type.AudioRecognizeTemplate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TopActivity extends Activity implements View.OnClickListener {

    private static String TAG = TopActivity.class.getSimpleName();
    private MainBean mMainBean;
    private SpeechUnderstander mSpeechUnderstander;
    private Toast mToast;
    private TextView mAskText, mUnderstanderText;
    private ImageButton mMicButton;
    private MicRecordingView mMicRecordingView;
    private HeartProgressBar heartProgressBar;
    private FiveLine mFiveLine;

    private static final String[] permissionsArray = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_SMS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    List<String> mPermissionList = new ArrayList<>();


    int ret = 0;// 函数调用返回值

    public static boolean service_flag = false;//表示是否在一项服务中
    public static String SRResult = "";    //识别结果

    private MySyntherizer synthesizer;  //语音合成控制主类
    private String offlineVoice = OfflineResource.VOICE_MALE;
    private String appId = "16096017";

    private String appKey = "6UgltGGWGyMMEOpcrg85EGG8";

    private String secretKey = "gvUECdiZGCqZOxsLBRdhEKK5WoVRwIl9";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.MIX;
    private Handler mHandler;


    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_top);
        initPermission();
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58057ac8");

        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(TopActivity.this, mSpeechUdrInitListener);

        mToast = Toast.makeText(TopActivity.this, "", Toast.LENGTH_SHORT);

        initialTts();
        initLayout();
        speakAnswer("我能帮您做什么吗?");
    }

    //权限判断和申请
    private void initPermission() {

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissionsArray.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissionsArray[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissionsArray[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissionsArray, 1);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (1 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
            }else{
                //全部权限通过，可以进行下一步操作。。。

            }
        }

    }

    /**
     * 初始化Layout。
     */
    private void initLayout() {
        mAskText = (TextView) findViewById(R.id.tv_ask);
        mMicRecordingView = (MicRecordingView)findViewById(R.id.micView);
        mUnderstanderText = (TextView) findViewById(R.id.tv_answer);
        heartProgressBar = (HeartProgressBar) findViewById(R.id.progressBar);
        mFiveLine = (FiveLine) findViewById(R.id.fiveLine);

        mUnderstanderText.setText("我能帮您做什么吗?");
        mMicButton = (ImageButton) findViewById(R.id.start_understander);
        mMicButton.setOnClickListener(TopActivity.this);
    }

    //初始化语音相关接口
    public void initialTts() {
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        mHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }

        };
        UiMessageListener.speakFinListener mlistener = new UiMessageListener.speakFinListener() {
			@Override
			public void finish() {
				mFiveLine.setVisibility(View.INVISIBLE);
			}
		};
        SpeechSynthesizerListener listener = new UiMessageListener(mHandler, mlistener);

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                         Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new MySyntherizer(this, initConfig, mHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_MIX_MODE, com.baidu.tts.client.SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(com.baidu.tts.client.SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.i("failed" , e.getMessage());
        }
        return offlineResource;
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.start_understander){
            mFiveLine.setVisibility(View.INVISIBLE);
            heartProgressBar.start();
            synthesizer.stop();

            if (mSpeechUnderstander.isUnderstanding()) {// 开始前检查状态
                mSpeechUnderstander.stopUnderstanding();
                //showTip("停止录音");
            }
            ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
            if (ret != 0) {
                showTip("语义理解失败,错误码:" + ret);
            } else {
                showTip("请开始说话…");
            }
        }

    }

    /**
     * 帮助
     */
    public void help(View view) {
        startActivity(new Intent(TopActivity.this, HelpActivity.class));
    }

    /**
     * 初始化监听器（语音到语义）。
     */
    private InitListener mSpeechUdrInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "speechUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            }
        }
    };

    /**
     * 语义理解回调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                Log.d(TAG, result.getResultString());

                // 显示
                String text = result.getResultString();
                Log.e(TAG, text);
                mMainBean = JsonParser.parseIatResult(text);


                if (!TextUtils.isEmpty(text)) {
                    mAskText.setText(mMainBean.getText());
                    if (mMainBean.getRc() == 0) {
                        SRResult = mMainBean.getText();
                        judgeService();
                    } else {
                        mUnderstanderText.setText("我听不懂您说什么，亲爱的，下次可能我就明白了");
                        speakAnswer("我听不懂您说什么，亲爱的，下次可能我就明白了");
                    }
                }
            } else {
                showTip("识别结果不正确。");
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, data.length + "");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //showTip("结束说话");
            heartProgressBar.dismiss();
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    public void speakAnswer(String text) {
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(TopActivity.this, "tts_play");

        if (TextUtils.isEmpty(text)){
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
        }
        int code = synthesizer.speak(text);
        mFiveLine.setVisibility(View.VISIBLE);
        mUnderstanderText.setText(text);
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //未安装则跳转到提示安装页面
                showTip("请安装语记!");
            } else {
                showTip("语音合成失败,错误码: " + code);
            }
        }
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


    //语义场景判断
    private void judgeService() {

        SRResult = null;
        String service = mMainBean.getService();
        String operation = mMainBean.getOperation();

        AnswerBean answerBean = new AnswerBean();
        SlotsBean slotsBean = new SlotsBean();
        DatetimeBean datetimeBean = new DatetimeBean();
        ResultBean resultBean = new ResultBean();
        DataBean dataBean = new DataBean();

        String date = "该天";

        if (mMainBean.getAnswer() != null) {
            answerBean = mMainBean.getAnswer();
        }


        if (mMainBean.getSemantic() != null) {
            if (mMainBean.getSemantic().getSlots() != null) {
                slotsBean = mMainBean.getSemantic().getSlots();
                if (mMainBean.getSemantic().getSlots().getDatetime() != null) {
                    datetimeBean = mMainBean.getSemantic().getSlots().getDatetime();
                }
            }
        }


        if (mMainBean.getData() != null) {
            if (mMainBean.getData().getResult() != null) {
                if (mMainBean.getSemantic().getSlots().getDatetime() != null) {
                    Calendar calendar = Calendar.getInstance();
                    int today = calendar.get(Calendar.DAY_OF_MONTH);
                    dataBean = mMainBean.getData();
                    String day = datetimeBean.getDate().substring(datetimeBean.getDate().length() - 2, datetimeBean.getDate().length());
                    if (day.equals("AY")) {
                        day = today + "";
                    }
                    int getday = Integer.parseInt(day);
                    int sub = getday - today;
                    resultBean = dataBean.getResult().get(sub);

                    if (sub == 0) {
                        date = "今天";
                    } else if (sub == 1) {
                        date = "明天";
                    } else if (sub == 2) {
                        date = "后天";
                    } else if (sub == 3) {
                        date = "大后天";
                    } else if (sub == 4) {
                        date = "四天后";
                    } else if (sub == 5) {
                        date = "五天后";
                    } else if (sub == 6) {
                        date = "六天后";
                    }
                }
            }

        }

        if (service_flag == false) {//如果不在一项服务中才进行服务的判断


            switch (service) {

                case "telephone":
                    switch (operation) {
                        case "CALL": {    //1打电话
                            //必要条件【电话号码code】
                            //可选条件【人名name】【类型category】【号码归属地location】【运营商operator】【号段head_num】【尾号tail_num】
                            //可由多个可选条件确定必要条件
                            CallAction callAction = new CallAction(slotsBean.getName(), slotsBean.getCode(), TopActivity.this);//目前可根据名字或电话号码拨打电话
                            callAction.start();
                            break;
                        }
                        case "VIEW": {    //2查看电话拨打记录
                            //必要条件无
                            //可选条件【未接电话】【已拨电话】【已接电话】
                            CallView callview = new CallView(this);
                            callview.start();
                            break;
                        }
                        default:
                            break;
                    }

                    break;
                case "message": {//2 短信相关服务

                    switch (operation) {

                        case "SEND": {//1发送短信
                            SendMessage sendMessage = new SendMessage(slotsBean.getName(), slotsBean.getCode(), slotsBean.getContent(), TopActivity.this);
                            sendMessage.start();
                            break;
                        }

                        case "VIEW": {//2查看发送短信页面

                            MessageView messageView = new MessageView(this);
                            messageView.start();
                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }
                case "app": {//3 应用相关服务

                    switch (operation) {

                        case "LAUNCH": {//1打开应用
                            OpenAppAction openApp = new OpenAppAction(slotsBean.getName(), TopActivity.this);
                            openApp.start();
                            break;
                        }

                        case "QUERY": {//2应用中心搜索应用
                            SearchApp searchApp = new SearchApp(slotsBean.getName(), this);
                            searchApp.start();
                            break;
                        }

                        default:
                            break;

                    }
                    break;
                }

                case "websearch": {//5 搜索相关服务

                    switch (operation) {

                        case "QUERY": {//1搜索

                            SearchAction searchAction = new SearchAction(slotsBean.getKeywords(), TopActivity.this);
                            searchAction.Search();
                            break;
                        }

                        default:
                            break;

                    }

                    break;
                }

                case "faq": {//6 社区问答相关服务

                    switch (operation) {
                        case "ANSWER": {//1社区问答
                            OpenQA openQA = new OpenQA(answerBean.getText(), this);
                            openQA.start();
                            break;
                        }
                        default:
                            break;
                    }

                    break;

                }

                case "chat": {//7 聊天相关服务

                    switch (operation) {

                        case "ANSWER": {//1聊天模式

                            OpenQA openQA = new OpenQA(answerBean.getText(), this);
                            openQA.start();

                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case "openQA": {//8 智能问答相关服务

                    switch (operation) {

                        case "ANSWER": {//1智能问答

                            OpenQA openQA = new OpenQA(answerBean.getText(), this);
                            openQA.start();

                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case "baike": {//9 百科知识相关服务

                    switch (operation) {

                        case "ANSWER": {//1百科

                            OpenQA openQA = new OpenQA(answerBean.getText(), this);
                            openQA.start();

                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case "schedule": {//10 日程相关服务

                    switch (operation) {

                        case "CREATE": {//1创建日程/闹钟(直接跳转相应设置界面)

                            ScheduleCreate scheduleCreate = new ScheduleCreate(slotsBean.getName(), datetimeBean.getTime(), datetimeBean.getDate(), slotsBean.getContent(), this);
                            scheduleCreate.start();

                            break;
                        }

                        case "VIEW": {//1查看闹钟/日历(未实现)

                            ScheduleView scheduleView = new ScheduleView(slotsBean.getName(), datetimeBean.getTime(), datetimeBean.getDate(), slotsBean.getContent(), this);
                            scheduleView.start();
                            break;
                        }


                        default:
                            break;
                    }

                    break;
                }

                case "weather": {//11 天气相关服务

                    switch (operation) {

                        case "QUERY": {//1查询天气

                            SearchWeather searchWeather = new SearchWeather(date, resultBean.getCity(), resultBean.getSourceName(), resultBean.getDate(), resultBean.getWeather(), resultBean.getTempRange(), resultBean.getAirQuality(), resultBean.getWind(), resultBean.getHumidity(), resultBean.getWindLevel() + "", this);
                            searchWeather.start();

                            break;
                        }

                        default:
                            break;

                    }

                    break;
                }

                default:
                    mUnderstanderText.setText("我听不懂您说什么，亲爱的，下次可能我就明白了");
                    speakAnswer("我听不懂您说什么，亲爱的，下次可能我就明白了");
                    break;
            }
        }

    }

    /**
     * 双击退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }

        return false;
    }

    private long time = 0;

    public void exit() {
        if (System.currentTimeMillis() - time > 2000) {
            time = System.currentTimeMillis();
            showTip("再点击一次退出应用程序");
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mSpeechUnderstander.cancel();
        mSpeechUnderstander.destroy();
        synthesizer.stop();

    }


    @Override
    protected void onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(TopActivity.this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(TopActivity.this);
        super.onPause();
    }


}
