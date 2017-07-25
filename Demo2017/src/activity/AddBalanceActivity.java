package activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.OtherUtils;
import utils.WindowUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo2017.R;

@SuppressLint("InflateParams")
public class AddBalanceActivity extends Activity {
	private float mDesity;
	private LinearLayout balance_linear_all;
	
	private String mCardDesc = "工商银行-储蓄卡";
	private String mCardNum  = "4327";
	
	private ImageButton balance_addcard_but;
	private ScrollView scrollView;
	
	private List<Map<Integer,Object>> datas = new ArrayList<Map<Integer,Object>>();
	
	private int position = 0;
	private Handler mHandler = new Handler();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_balance);
		initView();
	}

	private void initView() {

		mDesity = WindowUtils.getDesity(this);
		scrollView = (ScrollView) findViewById(R.id.scrollow);
		balance_linear_all = (LinearLayout) findViewById(R.id.balance_linear_all);
		
		balance_addcard_but = (ImageButton) findViewById(R.id.balance_addcard_but);
		balance_addcard_but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(OtherUtils.isAllowClickable()){
					addItemCard(mCardDesc, mCardNum);
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
						scrollView.fullScroll(ScrollView.FOCUS_DOWN);	
						}
					});
					
				}
			}
		});
	}

	private void addItemCard(String cardDesc, String cardNum) {
		
		final RelativeLayout relativeLayout = new RelativeLayout(this);
		ViewGroup.LayoutParams mLayoutParams = new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, (int) (mDesity * 52));
		relativeLayout.setPadding((int) mDesity * 8, (int) mDesity * 8, (int) mDesity * 8, (int) mDesity * 8);

		ImageView imageView = new ImageView(this);
		imageView.setBackgroundResource(R.drawable.balance_online_recharge);

		TextView textView1 = new TextView(this);
		textView1.setText(cardDesc);
		textView1.setTextColor(Color.WHITE);
		textView1.setTextSize(18);
		textView1.setPadding((int) mDesity * 32, 0, 0, 0);

		
		RelativeLayout.LayoutParams textviewLayoutParams = new RelativeLayout.LayoutParams(-2,-2);
		textviewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
		TextView textView2 = new TextView(this);
		textView2.setText("尾号"+cardNum);
		textView2.setTextColor(Color.WHITE);
		textView2.setTextSize(18);
		textView2.setPadding((int) mDesity * 32, 0, 0, 0);
		
		relativeLayout.setGravity(Gravity.CENTER_VERTICAL);
		relativeLayout.setLayoutParams(mLayoutParams);
		relativeLayout.setBackgroundResource(R.drawable.balance_yinhangka_shape);

		LinearLayout lineLinearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,(int) (mDesity*10));
		lineLinearLayout.setLayoutParams(lp);
		
		relativeLayout.addView(imageView);
		relativeLayout.addView(textView1);
		relativeLayout.addView(textView2, textviewLayoutParams);;
		balance_linear_all.addView(lineLinearLayout);
		balance_linear_all.addView(relativeLayout);
		
		position++;
		relativeLayout.setId(position);
		
		relativeLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					Toast.makeText(AddBalanceActivity.this, "选择了 "+ relativeLayout.getId(),Toast.LENGTH_SHORT).show();
			}
		});
	}
	
}
