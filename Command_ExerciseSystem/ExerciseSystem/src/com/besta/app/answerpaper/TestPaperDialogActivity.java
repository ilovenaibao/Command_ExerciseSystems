package com.besta.app.answerpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.besta.app.answerpaper.drawview.DrawView;
import com.besta.app.answerpaper.othersclassinfo.AnswerPaperFun;
import com.besta.app.toolswindow.MyToolsWindow;

/**
 * @author BXC2011007 Taylor
 * 
 * @describe 答題紙(草稿紙)Activity, 主要用來方便演算的工具。
 * 
 * @call_method 可參見工程路徑： com.besta.app.testcallactiv ity.testFirstActivity.java
 * 
 */
public class TestPaperDialogActivity extends Activity {
	// 捆綁的所有被調用的設置信息類
	private AnswerPaperFun.ReturnGetStartBundleData bundleData = null;
	// 所有初始化需要的屬性類
	private AnswerPaperFun.InitializeStartData initialStartData = null;
	// 答題紙 & 演算紙 View
	private int layoutMargin = 2;
	DrawView[] drawView = null;
	// 是否移動紙張的標記
	boolean[] isMove = null;
	// 是否切換Pen || Eraser的標記
	boolean[] isSetPen = null;
	// 答題紙介面工具箱
	ImageView[] imgBt_tool = null;
	// 水印
	ImageView writePaperText = null;
	ImageView testPaperText = null;
	// 工具箱是否可用
	boolean[] isDisable_tool = null;
	// 工具焦點
	private int nowToolChos = 0;
	// 答題工具箱
	private MyToolsWindow myTools = null;
	// 答題工具箱是否彈出
	boolean reShowDlg = false;
	private boolean onkeydown_back_flag = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化獲得出入信息
		bundleData = new AnswerPaperFun.ReturnGetStartBundleData();
		// 初始化所有屬性值
		bundleData = AnswerPaperFun.GetStartBundle(this, bundleData,
				AnswerPaperFun.ACTIVITY_CHOICE);
		if (bundleData.bRet) {
			InitializeNewActivityView();
			initialStartData = new AnswerPaperFun.InitializeStartData();
			AnswerPaperFun.InitializeStart(initialStartData, bundleData,
					AnswerPaperFun.ACTIVITY_CHOICE);
			AnswerPaperFun.InitializeAllViewAttrib(
					TestPaperDialogActivity.this, bundleData, initialStartData,
					drawView, imgBt_tool, null, testPaperText, isDisable_tool,
					nowToolChos, myTools, AnswerPaperFun.ACTIVITY_CHOICE);
			nowToolChos = AnswerPaperFun.TOOL_WRITEPAPER;
			AnswerPaperFun.SetOthersToolFocus(this, imgBt_tool, null,
					testPaperText, nowToolChos, AnswerPaperFun.ACTIVITY_CHOICE);

			// 加載題目信息以及答題信息
			if (!AnswerPaperFun.LoadMyDrawView(this, null, bundleData,
					initialStartData, drawView, imgBt_tool, isMove, isSetPen,
					myTools, AnswerPaperFun.ACTIVITY_CHOICE)) {
				finish();
			}

			// 設置當前工具箱的焦點位置
			AnswerPaperFun.SetToolsListener(this, imgBt_tool, drawView, isMove,
					isSetPen, myTools, initialStartData, bundleData,
					AnswerPaperFun.ACTIVITY_CHOICE);
		} else {
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		AnswerPaperFun.OnChangeTitleBarHeight(TestPaperDialogActivity.this);
		AnswerPaperFun.OnChangeScreenSize(TestPaperDialogActivity.this, null,
				bundleData, initialStartData, drawView, imgBt_tool, myTools,
				reShowDlg, AnswerPaperFun.ACTIVITY_CHOICE);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		WindowManager wm = AnswerPaperFun.GetActivityWindows();
		if (wm != null) {
			// 在程序退出(Activity销毁）时销毁悬浮窗口
			wm.removeView(bundleData.myMainView);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// Log.i("onPause ==========", "true");
		if (myTools.toolsWindow.isShowing()) {
			myTools.toolsWindow.dismiss();
		}
		WindowManager wm = AnswerPaperFun.GetActivityWindows();
		if (wm != null && !onkeydown_back_flag) {
			bundleData.myMainView.setVisibility(View.INVISIBLE);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		AnswerPaperFun.OnChangeTitleBarHeight(TestPaperDialogActivity.this);
		AnswerPaperFun.OnChangeScreenSize(TestPaperDialogActivity.this, null,
				bundleData, initialStartData, drawView, imgBt_tool, myTools,
				reShowDlg, AnswerPaperFun.ACTIVITY_CHOICE);
		if (bundleData.myMainView.getVisibility() == View.INVISIBLE) {
			bundleData.myMainView.setVisibility(View.VISIBLE);
		}

		// 判斷Tools工具欄是否在onPause時是打開狀態，如果是則重新打開
		if (null != myTools && null != myTools.toolsWindow
				&& myTools.toolsWindowIsShowing
				&& !myTools.toolsWindow.isShowing()) {
			if (null != imgBt_tool[AnswerPaperFun.TOOL_TOOLS]) {
				myTools.toolsWindow.showAtLocation(
						imgBt_tool[AnswerPaperFun.TOOL_TOOLS], Gravity.BOTTOM,
						0, 0);
			} else {
				myTools.toolsWindowIsShowing = false;
			}
		}

		super.onResume();
	}

	/**
	 * 初始化新的Activity所需的View
	 */
	private void InitializeNewActivityView() {
		drawView = new DrawView[2];
		isMove = new boolean[2];
		isSetPen = new boolean[2];
		drawView[AnswerPaperFun.TOOL_WRITEPAPER] = null;
		// drawView[AnswerPaperFun.TOOL_WRITEPAPER] = new DrawView(this);
		drawView[AnswerPaperFun.TOOL_TESTPAPER] = new DrawView(this);
		writePaperText = null;
		// writePaperText = new ImageView(this);
		testPaperText = new ImageView(this);

		imgBt_tool = new ImageView[AnswerPaperFun.tools_count];
		isDisable_tool = new boolean[AnswerPaperFun.tools_count];
		int disable_count = 0;
		for (disable_count = 0; disable_count < AnswerPaperFun.tools_count; disable_count++) {
			if (disable_count == AnswerPaperFun.TOOL_WRITEPAPER) {
				imgBt_tool[disable_count] = null;
				// imgBt_tool[disable_count] = new ImageView(this);
				// isDisable_tool[disable_count] = false;
			} else {
				imgBt_tool[disable_count] = new ImageView(this);
				isDisable_tool[disable_count] = false;
			}
		}
		myTools = new MyToolsWindow(this, bundleData.realWidth,
				bundleData.realHeight, AnswerPaperFun.ACTIVITY_CHOICE,
				AnswerPaperFun.IsClearAllData(
						drawView[AnswerPaperFun.TOOL_TESTPAPER].dvSaveData,
						myTools, AnswerPaperFun.IS_CLEAR_START));

		AnswerPaperFun.CreateAllView(this, null, bundleData, initialStartData,
				imgBt_tool, writePaperText, testPaperText, drawView,
				isDisable_tool, nowToolChos, myTools,
				AnswerPaperFun.ACTIVITY_CHOICE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (!myTools.toolsWindow.isShowing()) {
				myTools.toolsWindow.dismiss();
				myTools.toolsWindowIsShowing = false;
			}
			AnswerPaperFun.onMyFinish(TestPaperDialogActivity.this, drawView,
					bundleData, AnswerPaperFun.ACTIVITY_CHOICE, myTools,
					imgBt_tool);
			// AnswerPaperFun.MyFinish(TestPaperDialogActivity.this, drawView,
			// bundleData, AnswerPaperFun.ACTIVITY_CHOICE);
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

}