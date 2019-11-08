import java.awt.Color;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.iflytek.cloud.speech.RecognizerListener;
import com.iflytek.cloud.speech.RecognizerResult;
import com.iflytek.cloud.speech.ResourceUtil;
import com.iflytek.cloud.speech.Setting;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechEvent;
import com.iflytek.cloud.speech.SpeechRecognizer;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.qingyunerya.util.DebugLog;
import com.qingyunerya.util.DrawableUtils;
import com.qingyunerya.util.JsonParser;
import com.qingyunerya.util.Version;

/**
 * MscDemo It's a Sample using MSC SDK, include tts, isr. you can just press
 * button to use it.
 * 
 * @author cyhu 2012-06-14
 */
@SuppressWarnings("serial")
public class AiRobot extends JFrame implements ActionListener {
	private JPanel mMainJpanel;
	private JPanel mContentPanel;
	private static JFrame mJframe;
	
	private JButton switch_btn;
	private JButton left_setting;
	private JButton right_setting;
	private ImageIcon background;
	private ImageIcon imgSwitch;
	private ImageIcon imgSwitch_p;
	private JButton mouse;
	private ImageIcon imgMouse;
	
	private JLabel labelWav;
	JTextArea resultArea;
	Ai ai=new Ai();
	private SpeechRecognizer mIat;
	private JPopupMenu mSettingMenu = new JPopupMenu( "设置" );	//主菜单
	private Map<String, String> mParamMap = new HashMap<String, String>();

	private String mSavePath = "./iat_test.pcm"; 
	private static final String VAL_TRUE = "1";
	
	private static class DefaultValue{
		public static final String ENG_TYPE = SpeechConstant.TYPE_CLOUD;
		public static final String SPEECH_TIMEOUT = "60000";
		public static final String NET_TIMEOUT = "20000";
		public static final String LANGUAGE = "zh_cn";
		
		public static final String ACCENT = "mandarin";
		public static final String DOMAIN = "iat";
		public static final String VAD_BOS = "5000";
		public static final String VAD_EOS = "1800";
		
		public static final String RATE = "16000";
		public static final String NBEST = "1";
		public static final String WBEST = "1";
		public static final String PTT = "1";
		
		public static final String RESULT_TYPE = "json";
		public static final String SAVE = "0";
	}
	
	// 合成的文本内容
		private String mRightText = "机器人"+ai.robotName+"为你服务！";

		private JTextArea resultRightArea;

		// 语音合成对象
		private SpeechSynthesizer mTts;
		
		//当前要显示的文本
		private String mRightCurText = "";
		//更新文本的执行对象
		private TextRunnable mTextRunnable = new TextRunnable();
		
		private JPopupMenu mRightSettingMenu = new JPopupMenu( "设置" );	//主菜单
		
		private Map<String, String[]> mRightVoiceMap = new LinkedHashMap<String, String[]>();
		private Map<String, String> mRightParamMap = new HashMap<String, String>();
		
		private String mRightSavePath = "./tts_test.pcm"; 
		private static final String RIGHT_VAL_TRUE = "1";
		
		private static final String RIGHT_KEY_SHOWLOG = "showlog";
		
		private static class RIGHT_DefaultValue{
			public static final String ENG_TYPE = SpeechConstant.TYPE_CLOUD;
			public static final String VOICE = "小燕";
			public static final String BG_SOUND = "0";
			public static final String SPEED = "50";
			public static final String PITCH = "50";
			public static final String VOLUME = "50";
			public static final String SAVE = "0";
		}
	/**
	 * 界面初始化.
	 * 
	 */
	public AiRobot() {
		// 初始化
		mIat=SpeechRecognizer.createRecognizer();
		initParamMap();
		initMenu();
		if (SpeechSynthesizer.getSynthesizer() == null)
			SpeechSynthesizer.createSynthesizer();
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer();
		initRightParamMap();
		initRightMenu();
		this.setUndecorated(true); // 禁用或启用此窗体的修饰。只有在窗体不可显示时
        //才调用此方法。
		this.setBackground(new Color(0, 0, 0, 0));
		StringBuffer param = new StringBuffer();
		param.append( "appid=" + Version.getAppid() );
//		param.append( ","+SpeechConstant.LIB_NAME_32+"=myMscName" );
		SpeechUtility.createUtility( param.toString() );
		Setting.setShowLog(false);
		// 设置界面大小，背景图片
		background = new ImageIcon("res/index_bg.png");
		JLabel label = new JLabel(background);
		label.setBounds(0, 0, background.getIconWidth(),
				background.getIconHeight());
		getLayeredPane().add(label, new Integer(Integer.MIN_VALUE));

		int frameWidth = background.getIconWidth();
		int frameHeight = background.getIconHeight();

		setSize(frameWidth, frameHeight);
		setResizable(false);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		imgSwitch = new ImageIcon("res/switch.png");
		imgSwitch_p = new ImageIcon("res/switch_p.png");
		switch_btn = this.createImageButton(imgSwitch);
		switch_btn.setBounds(314,350, imgSwitch.getIconWidth(),
				imgSwitch.getIconHeight());
		
		imgMouse = new ImageIcon("res/mouse.png");
		mouse = this.createImageButton(imgMouse);
		mouse.setBounds(230,184, imgMouse.getIconWidth(),
				imgMouse.getIconHeight());
		mouse.setVisible(false);
		
		ImageIcon imgLeft_setting = new ImageIcon("res/setting.png");
		left_setting = this.createImageButton(imgLeft_setting);
		left_setting.setBounds(181, 506, imgLeft_setting.getIconWidth(),
				imgLeft_setting.getIconHeight());
		DrawableUtils.setMouseListener(left_setting, "res/setting");

		ImageIcon imgRight_setting = new ImageIcon("res/setting.png");
		right_setting = this.createImageButton(imgRight_setting);
		right_setting.setBounds(365, 506, imgRight_setting.getIconWidth(),
				imgRight_setting.getIconHeight());
		DrawableUtils.setMouseListener(right_setting, "res/setting");
		
		ImageIcon img = new ImageIcon("res/mic_01.png");
		labelWav = new JLabel(img);
		labelWav.setBounds(210, 410, img.getIconWidth(),
				img.getIconHeight() * 4 / 5);
		
		
		resultArea = new JTextArea("");
		resultArea.setBounds(220,337, 540, 400);
		resultArea.setOpaque(false);
		resultArea.setEditable(false);
		resultArea.setLineWrap(true);
		resultArea.setForeground(Color.BLACK);
		Font font = new Font("宋体", Font.BOLD, 16);
		resultArea.setFont(font);
		
		resultRightArea = new JTextArea("");
		resultRightArea.setBounds(212,209, 540, 400);
		resultRightArea.setOpaque(false);
		resultRightArea.setEditable(true);
		resultRightArea.setLineWrap(true);
		resultRightArea.setForeground(Color.BLACK);
		resultRightArea.setFont(font);
		resultRightArea.setText(mRightText);
		
		mMainJpanel = new JPanel();
		mMainJpanel.setBounds(0, 0, background.getIconWidth(),
				background.getIconHeight());
		mMainJpanel.setLayout(null);
		mMainJpanel.setOpaque(false);
		mMainJpanel.add(switch_btn);
		mMainJpanel.add(mouse);
		mMainJpanel.add(left_setting);
		mMainJpanel.add(right_setting);
		mMainJpanel.add(labelWav);
		mMainJpanel.add(resultArea);
		mMainJpanel.add(resultRightArea);
		
		switch_btn.addActionListener(this);
		left_setting.addActionListener(this);
		right_setting.addActionListener(this);
		mContentPanel = new JPanel();
		mContentPanel.setOpaque(false);
		mContentPanel.setLayout(null);
		mContentPanel.add(mMainJpanel);
		setLocationRelativeTo(null);
		setContentPane(mContentPanel);
		setVisible(true);
	}

	/**
	 * 入口函数.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		mJframe = new AiRobot();
	}

	public static JFrame getFrame() {
		return mJframe;
	}

	public JButton createImageButton(ImageIcon img) {
		JButton button = new JButton("");
		button.setIcon(img);
		button.setSize(img.getIconWidth(), img.getIconHeight());
		button.setBackground(null);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == switch_btn) {
			if(switch_btn.getIcon()==imgSwitch)
			{
				switch_btn.setIcon(imgSwitch_p);
				setting();
				resultArea.setText( "" );
				if (!mIat.isListening())
					mIat.startListening(recognizerListener);
				else
					mIat.stopListening();
			}
			else
			{
				switch_btn.setIcon(imgSwitch);
				if (null != mIat ) {
					mIat.cancel();
					mIat.destroy();
					mTts.stopSpeaking();
					mTts.destroy();
				}
			}
			
		} else if (e.getSource() == left_setting) {
			DebugLog.Log( "actionPerformed setting" );
			mSettingMenu.show( this, this.left_setting.getX(), this.left_setting.getY()+50 );
		}  else if (e.getSource() == right_setting) {
			DebugLog.Log( "actionPerformed setting" );
			mRightSettingMenu.show( this, this.right_setting.getX(), this.right_setting.getY()+50 );
		}
	}
	/**
	 * 听写结束，恢复初始状态
	 */
	public void iatSpeechInitUI() {

		labelWav.setIcon(new ImageIcon("res/mic_01.png"));
	}

	private void initParamMap(){
		this.mParamMap.put( SpeechConstant.ENGINE_TYPE, DefaultValue.ENG_TYPE );
		this.mParamMap.put( SpeechConstant.SAMPLE_RATE, DefaultValue.RATE );
		this.mParamMap.put( SpeechConstant.NET_TIMEOUT, DefaultValue.NET_TIMEOUT );
		this.mParamMap.put( SpeechConstant.KEY_SPEECH_TIMEOUT, DefaultValue.SPEECH_TIMEOUT );
		
		this.mParamMap.put( SpeechConstant.LANGUAGE, DefaultValue.LANGUAGE );
		this.mParamMap.put( SpeechConstant.ACCENT, DefaultValue.ACCENT );
		this.mParamMap.put( SpeechConstant.DOMAIN, DefaultValue.DOMAIN );
		this.mParamMap.put( SpeechConstant.VAD_BOS, DefaultValue.VAD_BOS );
		
		this.mParamMap.put( SpeechConstant.VAD_EOS, DefaultValue.VAD_EOS );
		this.mParamMap.put( SpeechConstant.ASR_NBEST, DefaultValue.NBEST );
		this.mParamMap.put( SpeechConstant.ASR_WBEST, DefaultValue.WBEST );
		this.mParamMap.put( SpeechConstant.ASR_PTT, DefaultValue.PTT );
		
		this.mParamMap.put( SpeechConstant.RESULT_TYPE, DefaultValue.RESULT_TYPE );
		this.mParamMap.put( SpeechConstant.ASR_AUDIO_PATH, null );
	}
	
	private void initMenu(){
		//引擎类型
		{
			Map<String, String> engMap = new LinkedHashMap<String, String>();
			engMap.put( SpeechConstant.TYPE_CLOUD, "云端" );
			engMap.put( SpeechConstant.TYPE_LOCAL, "本地" );
			
			JMenu engMenu = this.addRadioMenu( "引擎类型", SpeechConstant.ENGINE_TYPE, engMap, DefaultValue.ENG_TYPE, mRadioItemListener );
			engMenu.setEnabled( false );	//目前暂不支持离线模式；
		}
		
		//采样率
		{
			Map<String, String> rateMap = new LinkedHashMap<String, String>();
			rateMap.put( "16000", "16k" );
			rateMap.put( "8000", "8k" );
			
			this.addRadioMenu( "采样率", SpeechConstant.SAMPLE_RATE, rateMap, DefaultValue.RATE, mRadioItemListener );
		}
		
		//网络超时
		this.addSliderMenu( "网络超时"
				, SpeechConstant.NET_TIMEOUT
				, 0
				, 30000
				, Integer.valueOf(DefaultValue.NET_TIMEOUT)
				, mChangeListener );
		
		//录音超时
		this.addSliderMenu( "录音超时"
				, SpeechConstant.KEY_SPEECH_TIMEOUT
				, 0
				, 60000
				, Integer.valueOf(DefaultValue.SPEECH_TIMEOUT)
				, mChangeListener );
		
		//语言
		{
			Map<String, String> languageMap = new LinkedHashMap<String, String>();
			languageMap.put( "zh_cn", "简体中文" );
			languageMap.put( "en_us", "美式英文" );
			
			this.addRadioMenu( "语言区域", SpeechConstant.LANGUAGE, languageMap, DefaultValue.LANGUAGE, mRadioItemListener );
		}
		
		//方言
		{
			Map<String, String> accentMap = new LinkedHashMap<String, String>();
			accentMap.put( "mandarin", "普通话" );
			accentMap.put( "cantonese", "粤语" );
			accentMap.put( "lmz", "湖南话" );
			accentMap.put( "henanese", "河南话" );
			
			this.addRadioMenu( "方言", SpeechConstant.ACCENT, accentMap, DefaultValue.ACCENT, mRadioItemListener );
		}
		
		//领域
		{
			Map<String, String> domainMap = new LinkedHashMap<String, String>();
			domainMap.put( "iat", "日常用语" );
			domainMap.put( "music", "音乐" );
			domainMap.put( "poi", "地图" );
			domainMap.put( "vedio", "视频" );
			
			this.addRadioMenu( "领域", SpeechConstant.DOMAIN, domainMap, DefaultValue.DOMAIN, mRadioItemListener );
		}
		
		//前端点超时
		this.addSliderMenu( "前端点超时"
				, SpeechConstant.VAD_BOS
				, 1000
				, 10000
				, Integer.valueOf(DefaultValue.VAD_BOS)
				, mChangeListener );
		
		//后端点超时
		this.addSliderMenu( "后端点超时"
				, SpeechConstant.VAD_EOS
				, 0
				, 10000
				, Integer.valueOf(DefaultValue.VAD_EOS)
				, mChangeListener );
		
		//句子多侯选
		{
			Map<String, String> nbestMap = new LinkedHashMap<String, String>();
			nbestMap.put( "1", "开" );
			nbestMap.put( "0", "关" );
			
			this.addRadioMenu( "句子多侯选", SpeechConstant.ASR_NBEST, nbestMap, DefaultValue.NBEST, mRadioItemListener );
		}
		
		//词语多侯选
		{
			Map<String, String> wbestMap = new LinkedHashMap<String, String>();
			wbestMap.put( "1", "开" );
			wbestMap.put( "0", "关" );
			
			this.addRadioMenu( "词语多侯选", SpeechConstant.ASR_WBEST, wbestMap, DefaultValue.WBEST, mRadioItemListener );
		}
		
		//标点符号
		{
			Map<String, String> pttMap = new LinkedHashMap<String, String>();
			pttMap.put( "1", "开" );
			pttMap.put( "0", "关" );
			
			this.addRadioMenu( "标点符号", SpeechConstant.ASR_PTT, pttMap, DefaultValue.PTT, mRadioItemListener );
		}
		
		//结果类型
		{
			Map<String, String> resultMap = new LinkedHashMap<String, String>();
			resultMap.put( "json", "json" );
			resultMap.put( "plain", "plain" );
			
			this.addRadioMenu( "结果类型", SpeechConstant.RESULT_TYPE, resultMap, DefaultValue.RESULT_TYPE, mRadioItemListener );
		}
		
		//保存音频
		{
			Map<String, String> saveMap = new LinkedHashMap<String, String>();
			saveMap.put( "1", "开" );
			saveMap.put( "0", "关" );
			
			this.addRadioMenu( "保存音频", SpeechConstant.ASR_AUDIO_PATH, saveMap, DefaultValue.SAVE, mRadioItemListener );
		}
		
	}//end of function initMenu
	
	private JMenu addRadioMenu( final String text, final String name, Map<String, String> cmd2Names, final String defaultVal, ActionListener actionListener ){
		JMenu menu = new JMenu( text );
		menu.setName( name );
		ButtonGroup group = new ButtonGroup();
		
		for( Entry<String, String>entry : cmd2Names.entrySet() ){
			JRadioButtonMenuItem item = new JRadioButtonMenuItem( entry.getValue(), false );
			item.setName( name );
			item.setActionCommand( entry.getKey() );
			item.addActionListener( actionListener );
			if( defaultVal.equals(entry.getKey()) ){
				item.setSelected( true );
			}
			
			group.add( item );
			menu.add( item );
		}
		
		this.mSettingMenu.add( menu );
		
		return menu;
	}
	
	private void addSliderMenu( final String text, final String name, final int min, final int max, final int defaultVal, ChangeListener changeListener ){
		JMenu menu = new JMenu( text );
		
		JSlider slider = new JSlider( SwingConstants.HORIZONTAL
				, min
				, max
				, defaultVal );
		
		slider.addChangeListener( this.mChangeListener );
		slider.setName( name );
		slider.setPaintTicks( true );
		slider.setPaintLabels( true );
		final int majarTick = Math.max( 1, (max-min)/5 );
		slider.setMajorTickSpacing( majarTick );
		slider.setMinorTickSpacing( majarTick/2 );
		menu.add( slider );
		
		this.mSettingMenu.add( menu );
	}
	
	//选择监听器
	private ActionListener mRadioItemListener = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			DebugLog.Log( "mRadioItemListener actionPerformed etner action command="+event.getActionCommand() );
			Object obj = event.getSource();
			if( obj instanceof JMenuItem ){
				JMenuItem item = (JMenuItem)obj;
				DebugLog.Log( "mRadioItemListener actionPerformed name="+item.getName()+", value="+event.getActionCommand() );
				String value = event.getActionCommand();
				if( SpeechConstant.ASR_AUDIO_PATH.equals(item.getName()) ){
					value = VAL_TRUE.equalsIgnoreCase(value) ? mSavePath : null;
				}
				
				mParamMap.put( item.getName(), value );
			}else{
				DebugLog.Log( "mRadioItemListener actionPerformed source object is not JMenuItem" );
			}// end of if-else is object instance of JMenuItem
			
		}
		
	}; //end of  mEngItemListener
	
	//滑动条监听器
	private ChangeListener mChangeListener = new ChangeListener(){

		@Override
		public void stateChanged(ChangeEvent event) {
			DebugLog.Log( "mChangeListener stateChanged enter" );
			Object obj = event.getSource();
			if( obj instanceof JSlider ){
				JSlider slider = (JSlider)obj;
				DebugLog.Log( "bar name="+slider.getName()+", value="+slider.getValue() );

				mParamMap.put( slider.getName(), String.valueOf(slider.getValue()) );
			}else{
				DebugLog.Log( "mChangeListener stateChanged source object is not JProgressBar" );
			}
		}
		
	};
	
	void setting(){
		final String engType = this.mParamMap.get(SpeechConstant.ENGINE_TYPE);
		
		for( Entry<String, String> entry : this.mParamMap.entrySet() ){
			mIat.setParameter( entry.getKey(), entry.getValue() );
		}
		
		//本地识别时设置资源，并启动引擎
		if( SpeechConstant.TYPE_LOCAL.equals(engType) ){
			//启动合成引擎
			mIat.setParameter( ResourceUtil.ENGINE_START, SpeechConstant.ENG_ASR );
			
			//设置资源路径
			final String rate = this.mParamMap.get( SpeechConstant.SAMPLE_RATE );
			final String tag = rate.equals("16000") ? "16k" : "8k";
			String curPath = System.getProperty("user.dir");
			DebugLog.Log( "Current path="+curPath );
			String resPath = ResourceUtil.generateResourcePath( curPath+"/asr/common.jet" )
					+ ";" + ResourceUtil.generateResourcePath( curPath+"/asr/src_"+tag+".jet" );
			System.out.println( "resPath="+resPath );
			mIat.setParameter( ResourceUtil.ASR_RES_PATH, resPath );
		}// end of if is TYPE_LOCAL
		
	}// end of function setting
	private RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			DebugLog.Log( "onBeginOfSpeech enter" );
			/*((JLabel) jbtnRecognizer.getComponent(0)).setText("听写中...");
			jbtnRecognizer.setEnabled(false);*/
		}

		@Override
		public void onEndOfSpeech() {
			DebugLog.Log( "onEndOfSpeech enter" );
		}

		/**
		 * 获取听写结果. 获取RecognizerResult类型的识别结果，并对结果进行累加，显示到Area里
		 */
		@Override
		public void onResult(RecognizerResult results, boolean islast) {
			DebugLog.Log( "onResult enter" );
			
			//如果要解析json结果，请考本项目示例的 com.iflytek.util.JsonParser类
			String text = JsonParser.parseIatResult(results.getResultString());
			//String text = results.getResultString();
			resultArea.append(text);
			text = resultArea.getText();
			if( islast ){
				iatSpeechInitUI();
				String answer=ai.abstractAsk(text);
				rightsetting();
				// 合成文本为TEXT_CONTENT的句子，设置监听器为mSynListener
				mRightText = answer.trim();
				mTts.startSpeaking(mRightText, mSynListener );
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
			DebugLog.Log( "onVolumeChanged enter" );
			if (volume == 0)
			{
				volume = 1;
				mouse.setVisible(false);
			}	
			else if (volume >= 6)
			{
				volume = 6;
			}
			if(volume>=3)mouse.setVisible(true);
			labelWav.setIcon(new ImageIcon("res/mic_0" + volume + ".png"));
		}

		@Override
		public void onError(SpeechError error) {
			DebugLog.Log( "onError enter" );
			if (null != error){
				DebugLog.Log("onError Code：" + error.getErrorCode());
				resultArea.setText( error.getErrorDescription(true) );
				iatSpeechInitUI();
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int agr2, String msg) {
			DebugLog.Log( "onEvent enter" );
			//以下代码用于调试，如果出现问题可以将sid提供给讯飞开发者，用于问题定位排查
			/*if(eventType == SpeechEvent.EVENT_SESSION_ID) {
				DebugLog.Log("sid=="+msg);
			}*/
		}
	};
	private SynthesizerListener mSynListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
		}

		@Override
		public void onBufferProgress(int progress, int beginPos, int endPos,
				String info) {
			DebugLog.Log("--onBufferProgress--progress:" + progress
					+ ",beginPos:" + beginPos + ",endPos:" + endPos);
		}

		@Override
		public void onSpeakPaused() {

		}

		@Override
		public void onSpeakResumed() {

		}

		@Override
		public void onSpeakProgress(int progress, int beginPos, int endPos) {
			DebugLog.Log("onSpeakProgress enter progress:" + progress
					+ ",beginPos:" + beginPos + ",endPos:" + endPos);

			updateText( mRightText.substring( beginPos, endPos+1 ) );
			
			DebugLog.Log( "onSpeakProgress leave" );
		}

		@Override
		public void onCompleted(SpeechError error) {
			DebugLog.Log( "onCompleted enter" );
			
			String text = mRightText;
			if (null != error){
				DebugLog.Log("onCompleted Code：" + error.getErrorCode());
				text = error.getErrorDescription(true);
			}
			
			updateText( text );
			
			DebugLog.Log( "onCompleted leave" );
		}


		@Override
		public void onEvent(int eventType, int arg1, int arg2, int arg3, Object obj1, Object obj2) {
			if( SpeechEvent.EVENT_TTS_BUFFER == eventType ){
				DebugLog.Log( "onEvent: type="+eventType
						+", arg1="+arg1
						+", arg2="+arg2
						+", arg3="+arg3
						+", obj2="+(String)obj2 );
				ArrayList<?> bufs = null;
				if( obj1 instanceof ArrayList<?> ){
					bufs = (ArrayList<?>) obj1;
				}else{
					DebugLog.Log( "onEvent error obj1 is not ArrayList !" );
				}//end of if-else instance of ArrayList
				
				if( null != bufs ){
					for( final Object obj : bufs ){
						if( obj instanceof byte[] ){
							final byte[] buf = (byte[]) obj;
							DebugLog.Log( "onEvent buf length: "+buf.length );
						}else{
							DebugLog.Log( "onEvent error element is not byte[] !" );
						}
					}//end of for
				}//end of if bufs not null
			}//end of if tts buffer event
			//以下代码用于调试，如果出现问题可以将sid提供给讯飞开发者，用于问题定位排查
			/*else if(SpeechEvent.EVENT_SESSION_ID == eventType) {
				DebugLog.Log("sid=="+(String)obj2);
			}*/
		}
	};

	private class TextRunnable implements Runnable{
		@Override
		public void run() {
			resultRightArea.setText( mRightCurText );
		}//end of function run
		
	}//end of class TextRunnable
	
	private void updateText( final String text ){
		this.mRightCurText = text;
		SwingUtilities.invokeLater( mTextRunnable );
	}

	private void initRightMenu(){
		//显示日志
		{
			Map<String, String> logMap = new LinkedHashMap<String, String>();
			logMap.put( Boolean.toString(true), "打开" );
			logMap.put( Boolean.toString(false), "关闭" );
			
			addRadioRightMenu( "打印日志", RIGHT_KEY_SHOWLOG, logMap, Boolean.toString(Setting.getShowLog()), mRightRadioItemListener );
		}//end of menuEng
		
		//引擎类型选择
		{
			Map<String, String> engMap = new LinkedHashMap<String, String>();
			engMap.put( SpeechConstant.TYPE_CLOUD, "云端" );
			engMap.put( SpeechConstant.TYPE_LOCAL, "本地" );
			
			JMenu engMenu = addRadioRightMenu( "引擎类型", SpeechConstant.ENGINE_TYPE, engMap, RIGHT_DefaultValue.ENG_TYPE, mRightRadioItemListener );
			engMenu.setEnabled( false );	//目前暂不支持离线模式；
		}//end of menuEng
		
		//发音人选择
		{
			this.rightInitVoiceMap();
			Map<String, String> voiceItemMap = new LinkedHashMap<String, String>();
			for( Entry<String, String[]> entry : this.mRightVoiceMap.entrySet() ){
				voiceItemMap.put( entry.getKey(), entry.getKey() );
			}
			addRadioRightMenu( "发音人", SpeechConstant.VOICE_NAME, voiceItemMap, RIGHT_DefaultValue.VOICE, mRightRadioItemListener );
		}//end of menuVoice
		
		//背景音乐
		{
			Map<String, String> bgMap = new LinkedHashMap<String, String>();
			bgMap.put( "1", "开" );
			bgMap.put( "0", "关" );
			
			addRadioRightMenu( "背景音乐", SpeechConstant.BACKGROUND_SOUND, bgMap, RIGHT_DefaultValue.BG_SOUND, mRightRadioItemListener );
		}//end of menuBackGround
		
		//语速、语调、音量
		{
			Map<String, String> percentMap = new LinkedHashMap<String, String>();
			percentMap.put( SpeechConstant.SPEED, "语速" );
			percentMap.put( SpeechConstant.PITCH, "语调" );
			percentMap.put( SpeechConstant.VOLUME, "音量" );
			
			for( Entry<String, String> entry: percentMap.entrySet() ){
				this.addRightSliderMenu( entry.getValue()
						, entry.getKey()
						, 0
						, 100
						, Integer.valueOf(RIGHT_DefaultValue.SPEED)
						, this.mRightChangeListener );
			}//end of for percentMap
			
		}//end of 语速，语调，音量
		
		//保存音频文件
		{
			Map<String, String> saveMap = new LinkedHashMap<String ,String>();
			saveMap.put( "1", "开" );
			saveMap.put( "0", "关" );
			
			this.addRadioRightMenu( "保存音频", SpeechConstant.TTS_AUDIO_PATH, saveMap, RIGHT_DefaultValue.SAVE, this.mRightRadioItemListener );
		}
		
	}//end of function initMenu
	
	private JMenu addRadioRightMenu( final String text, final String name, Map<String, String> cmd2Names, final String defaultVal, ActionListener actionListener ){
		JMenu menu = new JMenu( text );
		menu.setName( name );
		ButtonGroup group = new ButtonGroup();
		
		for( Entry<String, String>entry : cmd2Names.entrySet() ){
			JRadioButtonMenuItem item = new JRadioButtonMenuItem( entry.getValue(), false );
			item.setName( name );
			item.setActionCommand( entry.getKey() );
			item.addActionListener( actionListener );
			if( defaultVal.equals(entry.getKey()) ){
				item.setSelected( true );
			}
			
			group.add( item );
			menu.add( item );
		}
		
		this.mRightSettingMenu.add( menu );
		
		return menu;
	}
	
	private void addRightSliderMenu( final String text, final String name, final int min, final int max, final int defaultVal, ChangeListener changeListener ){
		JMenu menu = new JMenu( text );
		
		JSlider slider = new JSlider( SwingConstants.HORIZONTAL
				, min
				, max
				, defaultVal );
		
		slider.addChangeListener( this.mRightChangeListener );
		slider.setName( name );
		slider.setPaintTicks( true );
		slider.setPaintLabels( true );
		final int majarTick = Math.max( 1, (max-min)/5 );
		slider.setMajorTickSpacing( majarTick );
		slider.setMinorTickSpacing( majarTick/2 );
		menu.add( slider );
		
		this.mRightSettingMenu.add( menu );
	}
	
	//选择监听器
	private ActionListener mRightRadioItemListener = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			DebugLog.Log( "mRadioItemListener actionPerformed etner action command="+event.getActionCommand() );
			Object obj = event.getSource();
			if( obj instanceof JMenuItem ){
				JMenuItem item = (JMenuItem)obj;
				DebugLog.Log( "mRadioItemListener actionPerformed name="+item.getName()+", value="+event.getActionCommand() );
				String value = event.getActionCommand();
				if( SpeechConstant.TTS_AUDIO_PATH.equals(item.getName()) ){
					value = RIGHT_VAL_TRUE.equalsIgnoreCase(value) ? mRightSavePath : null;
				}
				
				if( RIGHT_KEY_SHOWLOG.equals(item.getName()) ){
					Setting.setShowLog( Boolean.parseBoolean(value) );
				}else{
					mRightParamMap.put( item.getName(), value );
				}
				
			}else{
				DebugLog.Log( "mRadioItemListener actionPerformed source object is not JMenuItem" );
			}// end of if-else is object instance of JMenuItem
			
		}
		
	}; //end of  mEngItemListener
	
	//滑动条监听器
	private ChangeListener mRightChangeListener = new ChangeListener(){

		@Override
		public void stateChanged(ChangeEvent event) {
			DebugLog.Log( "mChangeListener stateChanged enter" );
			Object obj = event.getSource();
			if( obj instanceof JSlider ){
				JSlider slider = (JSlider)obj;
				DebugLog.Log( "bar name="+slider.getName()+", value="+slider.getValue() );

				mRightParamMap.put( slider.getName(), String.valueOf(slider.getValue()) );
			}else{
				DebugLog.Log( "mChangeListener stateChanged source object is not JProgressBar" );
			}
		}
		
	};
	
	//初始化发音人镜像表，云端对应的本地
	//为了查找本地资源方便，请把资源文件置为发音人参数+.jet，如“xiaoyan.jet”
	void rightInitVoiceMap(){
		mRightVoiceMap.clear();
		String[] names = null;
		
		names = new String[2];
		names[0] = names[1] = "xiaoyan";
		this.mRightVoiceMap.put( "小燕", names );	//小燕
		
		names = new String[2];
		names[0] = names[1] = "xiaoyu";
		this.mRightVoiceMap.put( "小宇", names );	//小宇
		
		names = new String[2];
		names[0] = "vixf"; names[1] = "xiaofeng";
		this.mRightVoiceMap.put( "小峰", names );	//小峰
		
		names = new String[2];
		names[0] = "vixm"; names[1] = "xiaomei";
		this.mRightVoiceMap.put( "小梅", names );	//小梅
		
		names = new String[2];
		names[0] = "vixr"; names[1] = "xiaorong";
		this.mRightVoiceMap.put( "小蓉", names );	//小蓉
		
		names = new String[2];
		names[0] = names[1] = "catherine";
		this.mRightVoiceMap.put( "凯瑟琳", names );	//凯瑟琳
	}
	
	void rightsetting(){
		final String engType = this.mRightParamMap.get(SpeechConstant.ENGINE_TYPE);
		String voiceName = null; 
		
		for( Entry<String, String> entry : this.mRightParamMap.entrySet() ){
			String value = entry.getValue();
			if( SpeechConstant.VOICE_NAME.equals(entry.getKey()) ){
				String[] names = this.mRightVoiceMap.get( entry.getValue() );
				voiceName = value = SpeechConstant.TYPE_CLOUD.equals(engType) ? names[0] : names[1]; 
			}
			
			mTts.setParameter( entry.getKey(), value );
		}
		
		//本地合成时设置资源，并启动引擎
		if( SpeechConstant.TYPE_LOCAL.equals(engType) ){
			//启动合成引擎
			mTts.setParameter( ResourceUtil.ENGINE_START, SpeechConstant.ENG_TTS );
			//设置资源路径
			String curPath = System.getProperty("user.dir");
			DebugLog.Log( "Current path="+curPath );
			String resPath = ResourceUtil.generateResourcePath( curPath+"/tts/common.jet" )
					+ ";" + ResourceUtil.generateResourcePath( curPath+"/tts/"+voiceName+".jet" );
			System.out.println( "resPath="+resPath );
			mTts.setParameter( ResourceUtil.TTS_RES_PATH, resPath );
		}// end of if is TYPE_LOCAL
		
		//启用合成音频流事件，不需要时，不用设置此参数
		mTts.setParameter( SpeechConstant.TTS_BUFFER_EVENT, "1" );
	}// end of function setting
	
	private void initRightParamMap(){
		this.mRightParamMap.put( SpeechConstant.ENGINE_TYPE, RIGHT_DefaultValue.ENG_TYPE );
		this.mRightParamMap.put( SpeechConstant.VOICE_NAME, RIGHT_DefaultValue.VOICE );
		this.mRightParamMap.put( SpeechConstant.BACKGROUND_SOUND, RIGHT_DefaultValue.BG_SOUND );
		this.mRightParamMap.put( SpeechConstant.SPEED, RIGHT_DefaultValue.SPEED );
		this.mRightParamMap.put( SpeechConstant.PITCH, RIGHT_DefaultValue.PITCH );
		this.mRightParamMap.put( SpeechConstant.VOLUME, RIGHT_DefaultValue.VOLUME );
		this.mRightParamMap.put( SpeechConstant.TTS_AUDIO_PATH, null );
	}
	public JPanel getMainJpanel() {
		return mMainJpanel;
	}

	public JPanel getContePanel() {
		return mContentPanel;
	}
}