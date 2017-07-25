package activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.demo2017.R;

public class MainActivity extends Activity implements OnClickListener {

	private Button add_balance_bt;
	private Button test_weidian_bt;
	private Button test_weidian_note_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		add_balance_bt = (Button) findViewById(R.id.add_balance_bt);
		test_weidian_bt = (Button) findViewById(R.id.test_weidian_bt);
		test_weidian_note_bt = (Button) findViewById(R.id.test_weidian_note_bt);
		
		add_balance_bt.setOnClickListener(this);
		test_weidian_bt.setOnClickListener(this);
		test_weidian_note_bt.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_balance_bt:
			startActivity(new Intent(MainActivity.this,AddBalanceActivity.class));
			break;
		case R.id.test_weidian_bt:
			startActivity(new Intent(MainActivity.this,TestWeiDianActivity.class));
			break;
		case R.id.test_weidian_note_bt:
			startActivity(new Intent(MainActivity.this,TestWeiDianNoteActivity.class));
			break;
		default:
			break;
		}

	}

}
