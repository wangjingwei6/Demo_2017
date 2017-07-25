package activity;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;

import utils.CustomAlterDialogUtil;
import utils.CustomAlterDialogUtil.DialogClickListenner;
import utils.ImageLoaderUtil;
import utils.KeyBoardUtils;
import utils.WindowUtils;
import view.NoScrollListView;
import view.ResizeLayout;
import view.ResizeLayout.OnkeyboardShowListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import bean.NoteItemBean;
import cn.liweiqin.testselectphoto.core.FunctionConfig;
import cn.liweiqin.testselectphoto.core.PhotoFinal;
import cn.liweiqin.testselectphoto.model.PhotoInfo;
import cn.liweiqin.testselectphoto.utils.PhotoUtil;

import com.example.demo2017.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@SuppressLint("NewApi")
public class TestWeiDianNoteActivity extends Activity implements OnClickListener {

	private PopupWindow popupWindow;
	private TextView addcontent_tv;
	private EditText title_edt;
	private View viewBg;

	private NoScrollListView listView;
	private ScrollView scrollView;
	private MyAdapter mAdapter;

	private List<NoteItemBean> datas = new ArrayList<NoteItemBean>();
	private int currentClickItemPosition;// 当前Item 右下角按钮点击位置
	private int firstVisiblePosition;
	private int visiableItemCount;
	private int selectOntouchIndex = 0;
	
	private static int CLICK_TYPE; // 当前ListView中ITEM触发事件的标记
	private static final int TYPE_ADD = 01; // ITEM 添加
	private static final int TYPE_INSERT = 02; // ITEM 插入
	private static final int TYPE_TOUCH = 03; // ITEM 中 EditText触摸

	public static final int ITEM_TEXT = 0;
	public static final int ITEM_PHOTO = 1;

	private static final int MESSAGE_CLICK = 04;
	private static final int MESSAGE_SCROLL_FLUSH = 05;
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CLICK:
				addcontent_tv.setClickable(true);
				viewBg.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return false;
					}
				});
				mHandler.removeMessages(MESSAGE_CLICK);
				break;
			case MESSAGE_SCROLL_FLUSH:
				 scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				mHandler.removeMessages(MESSAGE_SCROLL_FLUSH);
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_note_weidian);
		screenHeight = WindowUtils.getScreenHeight(TestWeiDianNoteActivity.this);
		initView();
	}

	private void flushtData() {
		if (mAdapter == null) {
			listView.setVisibility(View.VISIBLE);
			mAdapter = new MyAdapter();
			listView.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void initView() {
		final ResizeLayout mResizeLayout = (ResizeLayout) findViewById(R.id.fl_resize_layout);
		mResizeLayout.setOnKeyboardShowListener(new OnkeyboardShowListener() {
			public void onKeyboardShowOver() {
			}

			public void onKeyboardShow(int keyHeight) {
			}

			public void onKeyboardHide() {
				scrollBy();
			}

			@Override
			public void onKeyboardChange() {
				boolean isShow = isSoftKeyboardShow(mResizeLayout);
				firstVisiblePosition = listView.getFirstVisiblePosition();
				visiableItemCount = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition() + 1;

				if (!isShow) {
					if (getListViewHeight() < WindowUtils.getScreenHeight(TestWeiDianNoteActivity.this) / 2) {
						return;
					}
					if (selectOntouchIndex == firstVisiblePosition) {
						return;
					}
					if (selectOntouchIndex - firstVisiblePosition + 1 > visiableItemCount / 2) {
						Log.i("Test", "下半区域 ");
						scrollBy();
					}
				}
			}
		});

		scrollView = (ScrollView) findViewById(R.id.scrollview);
		listView = (NoScrollListView) findViewById(R.id.listview);
		viewBg = findViewById(R.id.myView);
		viewBg.setVisibility(View.GONE);
		addcontent_tv = (TextView) findViewById(R.id.addcontent_tv);
		title_edt = (EditText) findViewById(R.id.title_edt);
		addcontent_tv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addcontent_tv:
			CLICK_TYPE = TYPE_ADD;
			showPopupWindow();
			break;
		case R.id.text_linear:
			if (CLICK_TYPE == TYPE_ADD) {
				addItem();
			} else if (CLICK_TYPE == TYPE_INSERT) {
				insertItem();
			}
			popupWindow.dismiss();
			break;

		case R.id.photo_linear:
			final FunctionConfig functionConfig = initConfig();
			PhotoFinal.openMuti(functionConfig, mOnHanlderResultCallback);
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

		View view = LayoutInflater.from(TestWeiDianNoteActivity.this).inflate(R.layout.popbottommenu, null);

		TextView text_linear = (TextView) view.findViewById(R.id.text_linear);
		TextView photo_linear = (TextView) view.findViewById(R.id.photo_linear);
		TextView cancel_tv = (TextView) view.findViewById(R.id.cancel_tv);

		text_linear.setOnClickListener(this);
		photo_linear.setOnClickListener(this);
		cancel_tv.setOnClickListener(this);

		if (popupWindow == null) {

			popupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, false) {
				@Override
				public void dismiss() {
					mHandler.sendEmptyMessageDelayed(MESSAGE_CLICK, 500);
					dissmissViewBack();
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
		KeyBoardUtils.closeKeybord(title_edt, TestWeiDianNoteActivity.this);
		popupWindow.update();
		addcontent_tv.setClickable(false);
		viewBg.setVisibility(View.VISIBLE);
		viewBg.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		viewBg.startAnimation(
				AnimationUtils.loadAnimation(TestWeiDianNoteActivity.this, R.anim.popup_background_anim_in));
		popupWindow.showAtLocation(addcontent_tv, Gravity.BOTTOM, 0, 0);
	}

	private void dissmissViewBack() {
		viewBg.startAnimation(
				AnimationUtils.loadAnimation(TestWeiDianNoteActivity.this, R.anim.popup_background_anim_out));
		viewBg.setVisibility(View.GONE);
	}

	private class MyAdapter extends BaseAdapter {

		class DeleteListener implements View.OnClickListener {
			private int position;

			@SuppressWarnings("unused")
			public DeleteListener(final int position, TextView view) {
				view.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							currentClickItemPosition = position;
						}
						return false;
					}
				});
			}

			@Override
			public void onClick(View v) {

				CustomAlterDialogUtil.remindUserDialog(TestWeiDianNoteActivity.this, "取消", "确定", null,
						new DialogClickListenner() {

							@Override
							public void confirm() {
								datas.remove(currentClickItemPosition);
								notifyDataSetChanged();
							}

							@Override
							public void cancle() {
							}
						});

			}

		}

		public class MoveUpListener implements View.OnClickListener {
			@SuppressWarnings("unused")
			public MoveUpListener(final int position, TextView view) {
				view.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							currentClickItemPosition = position;
						}
						return false;
					}
				});
			}

			@Override
			public void onClick(View v) {
				moveUpItem();
			}

		}

		class InsertListener implements View.OnClickListener {
			@SuppressWarnings("unused")
			public InsertListener(final int position, TextView view) {
				view.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							currentClickItemPosition = position;
						}
						return false;
					}
				});
			}

			@Override
			public void onClick(View v) {
				CLICK_TYPE = TYPE_INSERT;
				showPopupWindow();
			}
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 两个样式 返回2
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			return datas.get(position).mType;
		}

		@SuppressWarnings("unused")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
//			Log.e("Test", "getView == " + position);
			int type = getItemViewType(position);
			MyViewHolder mViewHolder = null;
			MyViewHolderS mMyViewHolderS = null;
			final NoteItemBean mNoteItemBean = datas.get(position);
			switch (type) {
			case ITEM_TEXT:
				if (convertView == null) {
					convertView = LayoutInflater.from(TestWeiDianNoteActivity.this).inflate(R.layout.item_text_note,
							null);
					mViewHolder = new MyViewHolder(convertView);
					convertView.setTag(mViewHolder);
				} else {
					mViewHolder = (MyViewHolder) convertView.getTag();
				}

				mViewHolder.delete.setOnClickListener(new DeleteListener(position, mViewHolder.delete));
				mViewHolder.move_up.setOnClickListener(new MoveUpListener(position, mViewHolder.move_up));
				mViewHolder.insert.setOnClickListener(new InsertListener(position, mViewHolder.insert));
				final EditText editText = mViewHolder.content_edt;
				if (position == 0) {
					mViewHolder.move_up.setVisibility(View.GONE);
				} else {
					mViewHolder.move_up.setVisibility(View.VISIBLE);
				}

				// 根据Tag移除之前的监听事件，防止造成数据丢失，错乱的问题；
				if (editText.getTag() instanceof TextWatcher) {
					editText.removeTextChangedListener((TextWatcher) (editText.getTag()));
				}
					editText.setText(mNoteItemBean.mTextContent);

				// 根据手指触碰的位置，获取当前EditText的位置；
				editText.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						CLICK_TYPE = TYPE_TOUCH;
						if (event.getAction() == MotionEvent.ACTION_UP) {
							selectOntouchIndex = position;
						}
						return false;
					}

				});

				TextWatcher watcher = new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						// 动态存储数据
						mNoteItemBean.mTextContent = s.toString();
					}
				};
				editText.addTextChangedListener(watcher);
				editText.setTag(watcher);

				editText.clearFocus();
				if (CLICK_TYPE == TYPE_ADD) {
					if (position == datas.size() - 1) {
						editText.requestFocus();
					}

				} else if (CLICK_TYPE == TYPE_TOUCH) {
					if (selectOntouchIndex != -1 && selectOntouchIndex == position) {
						// 如果当前的行下标和点击事件中保存的index一致，手动为EditText设置焦点。
						editText.requestFocus();
						Log.i("Test", "index==" + selectOntouchIndex);
					}
				} else {
					editText.clearFocus();
				}
				editText.setSelection(editText.getText().length());

				break;
			case ITEM_PHOTO:
				if (convertView == null) {
					convertView = LayoutInflater.from(TestWeiDianNoteActivity.this).inflate(R.layout.item_photo_note,null);
					mMyViewHolderS = new MyViewHolderS(convertView);
					convertView.setTag(mMyViewHolderS);
				} else {
					mMyViewHolderS = (MyViewHolderS) convertView.getTag();
				}

				mMyViewHolderS.delete.setOnClickListener(new DeleteListener(position, mMyViewHolderS.delete));
				mMyViewHolderS.move_up.setOnClickListener(new MoveUpListener(position, mMyViewHolderS.move_up));
				mMyViewHolderS.insert.setOnClickListener(new InsertListener(position, mMyViewHolderS.insert));
				if (position == 0) {
					mMyViewHolderS.move_up.setVisibility(View.GONE);
				} else {
					mMyViewHolderS.move_up.setVisibility(View.VISIBLE);
				}

				String mPhotoPath = mNoteItemBean.getmPhotoPath();
				
//				Log.i("Test", "mPhotoPath = " + mPhotoPath);
				
				ViewGroup.LayoutParams layoutParams = mMyViewHolderS.content_img.getLayoutParams();
				mMyViewHolderS.content_img.setLayoutParams(layoutParams);
				
				ImageLoaderUtil.displayImage("file://"+mPhotoPath, mMyViewHolderS.content_img);
				
				break;
			default:
				break;
			}
			return convertView;
		}

		private ImageLoader mImageLoader;

		class MyViewHolder {
			private TextView delete, move_up, insert;
			private EditText content_edt;

			@SuppressWarnings("unused")
			public MyViewHolder(View view) {
				delete = (TextView) view.findViewById(R.id.delete);
				move_up = (TextView) view.findViewById(R.id.move_up);
				insert = (TextView) view.findViewById(R.id.insert);
				content_edt = (EditText) view.findViewById(R.id.content_edt);
			}
		}

		class MyViewHolderS {
			private TextView delete, move_up, insert;
			private ImageView content_img;

			@SuppressWarnings("unused")
			public MyViewHolderS(View view) {
				delete = (TextView) view.findViewById(R.id.delete);
				move_up = (TextView) view.findViewById(R.id.move_up);
				insert = (TextView) view.findViewById(R.id.insert);
				content_img = (ImageView) view.findViewById(R.id.content_img);
			}
		}
	}

	/**
	 * 插入数据
	 */
	private void insertItem() {
		for (int i = 0; i < datas.size(); i++) {
			if (i == currentClickItemPosition) {
				NoteItemBean noteItemBean = new NoteItemBean();
				noteItemBean.mType = ITEM_TEXT;
				datas.add(currentClickItemPosition + 1, noteItemBean);
				flushtData();
				return;
			}
		}
	}

	/**
	 * 上移数据
	 */
	private void moveUpItem() {
		int upPosition = currentClickItemPosition - 1;
		int currentPosition = currentClickItemPosition;
		datas.add(upPosition, datas.get(currentClickItemPosition));
		datas.add(currentPosition + 1, datas.get(upPosition + 1));
		datas.remove(upPosition + 1);
		datas.remove(currentPosition + 1);
		flushtData();
	}

	/**
	 * 添加数据
	 */
	@SuppressLint("UseSparseArrays")
	private void addItem() {
		int size = datas.size();
		NoteItemBean noteItemBean = new NoteItemBean();
		noteItemBean.mType = ITEM_TEXT;
		noteItemBean.setPosition(size);
		datas.add(noteItemBean);
		flushScrollView();
		flushtData();
	}

	private int listViewHeight;
	private static boolean isMeasure;
	private int offset;

	private void flushScrollView() {
		int currentListViewHeight = getListViewHeight();
		if (currentListViewHeight > screenHeight * 2 / 5) {
			Log.i("Test","isMeasure= "+isMeasure);
			if (!isMeasure) {
				listViewHeight = currentListViewHeight;
				isMeasure = true;
			} else {
				if (offset == 0) {
					offset = currentListViewHeight - listViewHeight;
				}
				Log.i("Test", " offset= " + offset + "  currentListViewHeight= " + currentListViewHeight
						+ "  listViewHeight= " + listViewHeight);
				scrollBy();
			}
		}
	}

	private void scrollBy() {
		scrollView.postDelayed(new Runnable() {

			@Override
			public void run() {
				scrollView.scrollBy(0, offset);
			}
		}, 100);
	}

	/**
	 * 计算ListView总高度
	 */
	public int getListViewHeight() {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return 0;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			if (listItem == null)
				return 0;
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		return params.height;
	}

	/**
	 * 回调
	 */
	private PhotoFinal.OnHanlderResultCallback mOnHanlderResultCallback = new PhotoFinal.OnHanlderResultCallback() {
		@Override
		public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
			if (reqeustCode == PhotoFinal.REQUEST_CODE_MUTI) {
				// 是选择图片回来的照片
				ArrayList<NoteItemBean> photoList = new ArrayList<NoteItemBean>();
				for (PhotoInfo info : resultList) {
					NoteItemBean mNoteItemBean = new NoteItemBean();
					mNoteItemBean.setmPhotoPath(info.getPhotoPath());
					mNoteItemBean.mType = ITEM_PHOTO;
					photoList.add(mNoteItemBean);
				}

				if (CLICK_TYPE == TYPE_ADD) { // 添加ITEM
					datas.addAll(photoList);
				} else if (CLICK_TYPE == TYPE_INSERT) { // 插入
					datas.addAll(currentClickItemPosition + 1, photoList);
				}
				flushtData();

			} else if (reqeustCode == PhotoFinal.REQUEST_CODE_CAMERA) {
				// 是拍照带回来的照片
				NoteItemBean mNoteItemBean = new NoteItemBean();
				mNoteItemBean.setmPhotoPath(resultList.get(0).getPhotoPath());
				mNoteItemBean.mType = ITEM_PHOTO;
				if (CLICK_TYPE == TYPE_ADD) { // 添加ITEM
					datas.add(mNoteItemBean);
				} else if (CLICK_TYPE == TYPE_INSERT) { // 插入
					datas.add(currentClickItemPosition + 1, mNoteItemBean);
				}

				flushtData();
			}

			if (CLICK_TYPE == TYPE_ADD) {
					mHandler.sendEmptyMessage(MESSAGE_SCROLL_FLUSH);
			}

		}

		@Override
		public void onHanlderFailure(int requestCode, String errorMsg) {
			Toast.makeText(TestWeiDianNoteActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * 加载配置的信息
	 */
	private ArrayList<String> sekectList = new ArrayList<String>();

	private FunctionConfig initConfig() {
		final FunctionConfig.Builder functionBuilder = new FunctionConfig.Builder();
		final FunctionConfig functionConfig = functionBuilder.setMaxSize(5)// 设置最大选择数
				.setSelected(sekectList)// 设置选泽的照片集
				.setContext(this)// 设置上下文对象
				.setTakePhotoFolder(null)// 设置拍照存放地址 默认为null
				.build();
		PhotoFinal.init(functionConfig);
		return functionConfig;
	}

	private int screenHeight;
	private int threshold;

	public boolean isSoftKeyboardShow(View rootView) {
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		threshold = screenHeight / 3;
		int rootViewBottom = rootView.getBottom();
		Rect rect = new Rect();
		rootView.getWindowVisibleDisplayFrame(rect);
		int visibleBottom = rect.bottom;
		int heightDiff = rootViewBottom - visibleBottom;
		System.out.println("----> rootViewBottom=" + rootViewBottom + ",visibleBottom=" + visibleBottom);
		System.out.println("----> heightDiff=" + heightDiff + ",threshold=" + threshold);
		return heightDiff > threshold;
	}

}
