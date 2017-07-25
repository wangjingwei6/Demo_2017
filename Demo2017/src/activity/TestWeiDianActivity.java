package activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.liweiqin.testselectphoto.utils.MediaScanner;

import com.example.demo2017.R;

public class TestWeiDianActivity extends Activity implements OnClickListener {

	private PopupWindow popupWindow;
	private TextView addcontent_tv;
	private View viewBg;

	private static final int MESSAGE_CLICK = 1;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CLICK:
				addcontent_tv.setClickable(true);
				 mHandler.removeMessages(MESSAGE_CLICK);
				break;

			default:
				break;
			}
			
			
			super.handleMessage(msg);
		}
	} ;
	
	 protected MediaScanner mMediaScanner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_weidian);
		mMediaScanner = new MediaScanner(this);
		initView();
	}
	
	private void initView() {
		viewBg = findViewById(R.id.myView);
		viewBg.setVisibility(View.GONE);
		addcontent_tv = (TextView) findViewById(R.id.addcontent_tv);
		addcontent_tv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addcontent_tv:
			showPopupWindow();
			break;
		case R.id.text_linear:
			Toast.makeText(TestWeiDianActivity.this, "文字", Toast.LENGTH_SHORT).show();

			break;

		case R.id.photo_linear:
			startActivity(new Intent(TestWeiDianActivity.this, PhotoMainActivity.class));
			popupWindow.dismiss();
			break;

		case R.id.cancel_tv:
			popupWindow.dismiss();
			break;

		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void showPopupWindow() {

		View view = LayoutInflater.from(TestWeiDianActivity.this).inflate(R.layout.popbottommenu, null);

		TextView text_linear = (TextView) view.findViewById(R.id.text_linear);
		TextView photo_linear = (TextView) view.findViewById(R.id.photo_linear);
		TextView cancel_tv = (TextView) view.findViewById(R.id.cancel_tv);

		text_linear.setOnClickListener(this);
		photo_linear.setOnClickListener(this);
		cancel_tv.setOnClickListener(this);

		if (popupWindow == null) {

			popupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, false){
				@Override
				public void dismiss() {
					dissmissViewBack();
					mHandler.sendEmptyMessageDelayed(MESSAGE_CLICK,500);
	                super.dismiss();
				}
			};
			popupWindow.setBackgroundDrawable(new BitmapDrawable());

			// popupWindow.setFocusable(true); 
			popupWindow.setTouchable(true); 
			popupWindow.setOutsideTouchable(true); 
			popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			popupWindow.setContentView(view);
			popupWindow.setAnimationStyle(R.style.popuStyle); 
		}
		
		 popupWindow.update();
		 addcontent_tv.setClickable(false);
		 viewBg.setVisibility(View.VISIBLE);
	     viewBg.startAnimation(AnimationUtils.loadAnimation(TestWeiDianActivity.this,R.anim.popup_background_anim_in));
		 popupWindow.showAtLocation(addcontent_tv, Gravity.BOTTOM, 0, 0);
	}

	private void dissmissViewBack() {
		viewBg.startAnimation(AnimationUtils.loadAnimation(TestWeiDianActivity.this, R.anim.popup_background_anim_out));
		viewBg.setVisibility(View.GONE);
	}

}
