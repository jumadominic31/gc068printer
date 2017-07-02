/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android_serialport_api.sample3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.Log;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;
import android.view.View;
import java.io.*;

import android.widget.CheckBox;
import java.lang.String;

import java.io.IOException;
import android.app.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import hdx.HdxUtil;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.Gravity;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




public class ConsoleActivity extends SerialPortActivity {


	private final int ENABLE_BUTTON = 2;
	private final int SHOW_VERSION = 3;
	private final int UPDATE_FW = 4;
	private final int SHOW_PROGRESS = 5;
	private final int DISABLE_BUTTON = 6;
	private final int HIDE_PROGRESS=7;
	private final int REFRESH_PROGRESS=8;
	private final int SHOW_FONT_UPTAE_INFO=9;
	private final int SHOW_PRINTER_INFO_WHEN_INIT=10;
	private final byte  HDX_ST_NO_PAPER1 = (byte)(1<<0);     // 1 缺纸
	//private final byte  HDX_ST_BUF_FULL  = (byte)(1<<1);     // 1 缓冲满
	//private final byte  HDX_ST_CUT_ERR   = (byte)(1<<2);     // 1 打印机切刀错误
	private final byte  HDX_ST_HOT       = (byte)(1<<4);     // 1 打印机太热
	private final byte  HDX_ST_WORK      = (byte)(1<<5);     // 1 打印机在工作状态

	private boolean stop = false;
	public static int BinFileNum = 0;
	public static boolean ver_start_falg = false;
	boolean Status_Start_Falg = false;
	byte [] Status_Buffer=new byte[300];
	int Status_Buffer_Index = 0;
	public static int update_ver_event = 0;
	public static boolean update_ver_event_err = false;
	public static StringBuilder strVer=new StringBuilder("922");
	public static StringBuilder oldVer=new StringBuilder("922");
	public static File BinFile;
	// EditText mReception;
	private static final String TAG = "ConsoleActivity";
	private static   String Error_State = "";
	Time time = new Time();
	int TimeSecond;
	public CheckBox myCheckBox;
	public ProgressDialog myDialog = null;
	private int iProgress   = 0;
	String Printer_Info =new String();
	String jsonArray  = new String();

	public static boolean flow_start_falg = false;
	byte [] flow_buffer=new byte[300];

	public TextView TextViewSerialRx;
	public static Context context;
	private  static int get_ver_count = 0;

	MyHandler handler;
	EditText Emission;
	Button ButtonCodeDemo;
	Button ButtonImageDemo;
	Button ButtonGetVersion;
	Button ButtonUpdateVersion;
	Button ButtonCharacterDemo;
	Button ButtonUpdateFontLib;
	Button ButtonFastPrintTest;

	ExecutorService pool = Executors.newSingleThreadExecutor();
	WakeLock lock;
	int printer_status = 0;
	private ProgressDialog m_pDialog;

	private Bitmap mBitmap ;
	private Canvas mCanvas;
	private int lcd_width;
	private int lcd_height;
	final int speed=0;
	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			if (stop == true)
				return;
			switch (msg.what) {
				case DISABLE_BUTTON:
					Close_Button();
					Log.d(TAG,"DISABLE_BUTTON");
					break;
				case ENABLE_BUTTON:
					ButtonCodeDemo.setEnabled(true);
					ButtonImageDemo.setEnabled(true);
					ButtonGetVersion.setEnabled(true);
					ButtonCharacterDemo.setEnabled(true);
					if(get_ver_count>5)
					{
						ButtonUpdateVersion.setEnabled(true);
						ButtonUpdateFontLib.setEnabled(true);
					}






					Log.d(TAG,"ENABLE_BUTTON");
					break;
				case SHOW_FONT_UPTAE_INFO:
					TextView tv3 = new TextView(ConsoleActivity.this);
					tv3.setText((String)msg.obj);
					tv3.setGravity(Gravity.CENTER);
					tv3.setTextSize(25);
					tv3.findFocus();
					new AlertDialog.Builder(ConsoleActivity.this)
							.setIcon(R.drawable.icon)
							.setView(tv3)
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface arg0, int arg1) {
									handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1,0, null));
								}
							}).show();
					break;
				case SHOW_VERSION:
					TextView tv2 = new TextView(ConsoleActivity.this);
					tv2.setText(getString(R.string.currentFWV)
							+ ConsoleActivity.strVer.toString());
					tv2.setGravity(Gravity.CENTER);
					tv2.setTextSize(25);
					tv2.findFocus();
					new AlertDialog.Builder(ConsoleActivity.this)
							.setTitle(getString(R.string.getV))
							.setIcon(R.drawable.icon)
							.setView(tv2)
							.setCancelable(false)
							.setPositiveButton("OK", null).show();
					break;
				case UPDATE_FW:
					m_pDialog.hide();
					TextView tv4 = new TextView(ConsoleActivity.this);
					// if(!ConsoleActivity.oldVer.toString().isEmpty())
				{
					tv4.setText(getString(R.string.previousFWV)
							+ ConsoleActivity.oldVer.toString() + "\n"
							+ getString(R.string.currentFWV)
							+ ConsoleActivity.strVer.toString());
					TextViewSerialRx.setText(Printer_Info
							+getString(R.string.previousFWV)
							+ ConsoleActivity.oldVer.toString() + "\n"
							+ getString(R.string.currentFWV)
							+ ConsoleActivity.strVer.toString());
				}
				// else
				{
					// tv3.setText("update firmware version failed ");
				}
				tv4.setGravity(Gravity.CENTER);
				tv4.setTextSize(22);
				tv4.findFocus();
				new AlertDialog.Builder(ConsoleActivity.this)
						.setTitle(getString(R.string.updateFWFinish))
						.setIcon(R.drawable.icon).setView(tv4)
						.setCancelable(false)
						.setPositiveButton("OK", null).show();
				break;
				case SHOW_PROGRESS:
					m_pDialog = new ProgressDialog(ConsoleActivity.this);
					m_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					m_pDialog.setMessage((String)msg.obj);
					m_pDialog.setIndeterminate(false);
					m_pDialog.setCancelable(false);
					m_pDialog.show();
					break;
				case  HIDE_PROGRESS:
					m_pDialog.hide();
					break;
				case   REFRESH_PROGRESS :
					m_pDialog.setProgress(iProgress);
					break;
				case     SHOW_PRINTER_INFO_WHEN_INIT:
					TextViewSerialRx.setText(Printer_Info+strVer.toString());
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "ConsoleActivity====onCreate");
		FirebaseCrash.log("Activity created");
		//FirebaseCrash.report(new Exception("My first Android non-fatal error"));


		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		jsonArray = intent.getStringExtra("jsonArray");
		FirebaseCrash.log("checking string" + jsonArray);
		//Log.d("String",jsonArray);



		setContentView(R.layout.console);
		setTitle(getString(R.string.appName));
		context = ConsoleActivity.this;
		handler = new MyHandler();
		HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_PRINTER);
		PowerManager pm = (PowerManager) getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
		ConsoleActivity.strVer = new StringBuilder();
		ConsoleActivity.oldVer = new StringBuilder();
		ButtonCodeDemo = (Button) findViewById(R.id.ButtonCodeDemo);
		ButtonImageDemo = (Button) findViewById(R.id.ButtonImageDemo);
		ButtonGetVersion = (Button) findViewById(R.id.GetVersion);
		ButtonUpdateVersion = (Button) findViewById(R.id.UpdateVersion);
		ButtonCharacterDemo = (Button) findViewById(R.id.ButtonCharacterDemo);
		ButtonUpdateFontLib = (Button) findViewById(R.id.UpdateFontLib);
		ButtonFastPrintTest= (Button) findViewById(R.id.FastPrintTest);

		final Button ButtonQuit = (Button) findViewById(R.id.quit);
		ButtonQuit.setVisibility(0);
		TextViewSerialRx = (TextView) findViewById(R.id.TextViewSerialRx);
		//Close_Button();

		// new InitThread(3).start();
		//Init_Data_When_Start();
		ButtonCharacterDemo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
				new WriteThread(0).start();

			}
		});
		ButtonQuit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ConsoleActivity.this.finish();
			}
		});
		ButtonCodeDemo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
				new WriteThread(1).start();
			}
		});
		ButtonFastPrintTest.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
				new WriteThread(3).start();
			}
		});
		ButtonImageDemo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
				new BmpThread().start();

			}
		});

		ButtonGetVersion.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean result;

				//handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
				//new Busser().start();
				result = get_fw_version();
				if (result) {
					get_ver_count++;
					//	handler.sendMessage(handler.obtainMessage(SHOW_VERSION, 1,0, null));

				}
				handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));
			}
		});

		ButtonUpdateVersion.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				boolean result = get_fw_version();
				if (!result) {

					return;
				}
				ConsoleActivity.oldVer=ConsoleActivity.strVer;
				String[] TransactionType = new String[] {
						getResources().getString(R.string.TransactionType1),
						getResources().getString(R.string.TransactionType2),
						getResources().getString(R.string.TransactionType3),
						getResources().getString(R.string.TransactionType4),


				};
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ConsoleActivity.this);
				builder.setTitle(getString(R.string.selectFW));
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setSingleChoiceItems(TransactionType, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								System.out.println("whichwhichwhichwhich == "
										+ which);
								switch (which) {
									case 0:
										ConsoleActivity.strVer = new StringBuilder("955");
										BinFileNum=R.raw.cut955;
										break;
									case 1:
										ConsoleActivity.strVer = new StringBuilder("946");
										BinFileNum=R.raw.cut953;
										break;
									case 2:
										ConsoleActivity.strVer = new StringBuilder("958");
										BinFileNum=R.raw.lp958_1;
										break;
									case 3:
										ConsoleActivity.strVer = new StringBuilder("956");
										BinFileNum=R.raw.lp956;
										break;

									default:
										return;
								}
								String strOldOne = ConsoleActivity.oldVer
										.toString().trim();
								String strNewOne = ConsoleActivity.strVer
										.toString().trim();
								if (strOldOne.compareTo(strNewOne) >= 0)// 更新到老版本,发出警告信息
								{
									Log.e("quck2",
											"bin : strOldOne.compareTo(strNewOne) >= 0 ");
									// TextView warnText =new
									// TextView(ConsoleActivity.this);
									// warnText.setText(R.string.UpdateFWWarn);
									new AlertDialog.Builder(
											ConsoleActivity.this)
											.setTitle(R.string.UpdateFWWarn)
											.setIcon(R.drawable.icon)
											// .setView(warnText)
											.setPositiveButton(
													R.string.Determine,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface arg0,
																int arg1) {

															handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));

															new UpdateFWThread(0)
																	.start();
														}
													})
											.setNegativeButton(
													R.string.cancel,
													new DialogInterface.OnClickListener() {

														public void onClick(
																DialogInterface arg0,
																int arg1) {
														}
													}).show();
								} else {
									handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));

									new UpdateFWThread(0).start();

								}
								Log.e("quck2", "go to update fw  ");
								dialog.dismiss();
							}
						});
				builder.setCancelable(true);
				builder.setNegativeButton(
						"从其他地方选择固件",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface arg0,int arg1) {
								SDFileExplorer.FileType=0;
								//Intent intent = new Intent(ConsoleActivity.this,SDFileExplorer.class);
								//startActivity(intent);
								//startActivityForResult(intent,0);
								Log.e("quck2", "finish->从其他地方选择固件  ");


							}
						});
				builder.setPositiveButton(R.string.cancel, null);
				builder.show();
			}
		});

		ButtonUpdateFontLib.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean result = get_fw_version();
				if (!result) {

					return;
				}
				ConsoleActivity.oldVer=ConsoleActivity.strVer;
				String[] TransactionType = new String[] {
						getResources().getString(R.string.ZiKuFile1),
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ConsoleActivity.this);
				builder.setTitle(R.string.WarningTimeIsLong);
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setSingleChoiceItems(TransactionType, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {

								switch (which) {
									case 0:
										BinFileNum=R.raw.ziku_20160602;
										break;
									default:
										return;
								}
								handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
								new UpdateFontLib_Thread(0).start();
								dialog.dismiss();
							}

						});
				builder.setCancelable(true);
				builder.setNegativeButton(
						"从其他地方选择固件",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface arg0,int arg1) {
								SDFileExplorer.FileType=1;
								Intent intent = new Intent(ConsoleActivity.this,SDFileExplorer.class);
								//startActivity(intent);
								startActivityForResult(intent,0);
								Log.e("quck2", "finish->从其他地方选择固件  ");

							}
						});
				builder.setPositiveButton(R.string.cancel, null);
				builder.show();




			}
		});
		ButtonUpdateVersion.setEnabled(false);
		ButtonUpdateFontLib.setEnabled(false);

		Toast.makeText(getApplicationContext(), "apk version:3.0.4a",Toast.LENGTH_SHORT).show();
		HdxUtil.SetPrinterPower(1);

		sendCommand(0x1B,0x23,0x23,0x45,0x43,0x41,0x54,0x31);
		sendCommand(0x1B,0x23,0x23,0x73,0x70,0x6c,0x76,speed);
		sendCommand(0x1B,0x36,0x0 );

	}
	@Override
	protected void onDataReceived(final byte[] buffer, final int size,final int n)
	{
		int i;
		String strTemp;
		if(Status_Start_Falg == true)
		{
			for (i = 0; i < size; i++)
			{
				Status_Buffer[getStatus_Buffer_Index()]=buffer[i];
				setStatus_Buffer_Index(1+i);
			}
		}

		if (ConsoleActivity.ver_start_falg == true) {
			for (i = 0; i < size; i++) {
				ConsoleActivity.strVer.append(String.format("%c",(char) buffer[i]));
			}

		}
		/*
		 * 	public static boolean flow_start_falg = false;
		byte [] flow_buffer=new byte[300];

		 * */

		StringBuilder str = new StringBuilder();
		StringBuilder strBuild = new StringBuilder();
		for (i = 0; i < size; i++) {
			if(flow_start_falg == true)
			{
				if( (buffer[i] ==0x13) || ( buffer[i] ==0x11)  )
				{
					flow_buffer[0]= buffer[i];

				}
			}
			str.append(String.format(" %x", buffer[i]));
			strBuild.append(String.format("%c", (char) buffer[i]));
		}
		Log.e(TAG, "onReceivedC= " + strBuild.toString());
		Log.e(TAG, "onReceivedx= " + str.toString());

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("quck3", "finish->浠庡叾浠栧湴鏂归€夋嫨鍥轰欢 -->3 "+ requestCode +"  "+resultCode);
		if (resultCode != 1)
		{
			Log.e("quck3", "finish->  resultCode != 1 ");
			return ;
		}
		if(SDFileExplorer.FileType ==0 )
		{
			handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
			new UpdateFWThread(1).start();
		}
		else
		{
			handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
			new UpdateFontLib_Thread(1).start();
		}


	}
	int getStatus_Buffer_Index()
	{
		return Status_Buffer_Index;

	}
	void setStatus_Buffer_Index(int v)
	{
		Status_Buffer_Index=v;
	}

	void Close_Button()
	{

		ButtonCodeDemo.setEnabled(false);
		ButtonImageDemo.setEnabled(false);
		ButtonGetVersion.setEnabled(false);
		ButtonCharacterDemo.setEnabled(false);
		ButtonUpdateVersion.setEnabled(false);
		ButtonUpdateFontLib.setEnabled(false);
	}
	byte Get_Printer_Status()
	{
		Status_Buffer[0]=0;
		Status_Buffer[1]=0;
		Status_Start_Falg = true;
		setStatus_Buffer_Index(0);
		sendCommand(0x1b,0x76);
		Log.i(TAG,"Get_Printer_Status->0x1b,0x76");
		Time_Check_Start();

		while(true)
		{
			if(getStatus_Buffer_Index()>0)
			{

				Status_Start_Falg = false;
				Log.e(TAG,"Get_Printer_Status :"+Status_Buffer[0]);
				return Status_Buffer[0] ;
			}
			if(TimeIsOver(5))
			{
				Status_Start_Falg = false;
				Log.e(TAG,"Get_Printer_Status->TIME OVER:"+Status_Buffer[0]);
				return (byte)0xff;

			}
			sleep(50);
		}


	}

	void PrinterPowerOnAndWaitReady()
	{

		//Status_Buffer_Index=0;
		//Status_Start_Falg = true;
		HdxUtil.SetPrinterPower(1);
		sleep(500);
	}
	void PrinterPowerOff()
	{
		//HdxUtil.SetPrinterPower(0);
	}

	void Wait_Printer_Ready()
	{/*
		byte status;

		while(true)
		{
			status = Get_Printer_Status() ;
			if(status== 0xff)
			{
				Log.e(TAG," time is out");
				return ;

			}

			if( (status & HDX_ST_WORK)>0 )
			{

				Log.d(TAG,"printer is busy");
			}
			else
			{
				Log.d(TAG," printer is ready");
				return;

			}
			sleep(50);
		}*/
	}
	//返回真, 有纸, 返回假 没有纸
	boolean  Printer_Is_Normal()
	{
		byte status;


		status = Get_Printer_Status() ;

		if(status== 0xff)
		{
			Log.e(TAG,"huck time is out");
			Error_State="huck unkown err";
			return  false;

		}

		if( (status & HDX_ST_NO_PAPER1 )>0 )
		{

			Log.d(TAG,"huck is not paper");
			Error_State=getResources().getString(R.string.IsOutOfPaper);
			return false;
		}
		else if( (status & HDX_ST_HOT )>0 )
		{
			Log.d(TAG,"huck is too hot");
			Error_State=getResources().getString(R.string.PrinterNotNormal1);
			return false;
		}
		else
		{
			Log.d(TAG," huck is ready");
			return true;
		}


	}
	//判断打印机装好纸 ,如果有 ,返回真,否者返回假
	boolean Warning_When_Not_Normal()
	{


			/*handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
			if(  Printer_Is_Normal() )
			{

				Log.i(TAG,"quck_Is_Normal ok");
				return true;
			}
			else
			{
				handler.sendMessage(handler.obtainMessage(SHOW_FONT_UPTAE_INFO, 1, 0, Error_State));
				Log.d(TAG," quck_Is not_Paper");
				return false;

			}*/
		return true;

	}
	/*
	 * 	public static boolean flow_start_falg = false;
	byte [] flow_buffer=new byte[300];

	 * */

	void flow_begin()
	{

		flow_start_falg = true;
		flow_buffer[0]=  0x0;
		Log.i(TAG,"flow_begin ");

	}
	void flow_end()
	{

		flow_start_falg = false;
		flow_buffer[0]=  0x0;
		Log.i(TAG,"flow_end ");
	}


	boolean  flow_check_and_Wait(int timeout)
	{


		boolean flag=false;

		Time_Check_Start();

		while(true)
		{
			sleep(5);
			if(flow_buffer[0]== 0)
			{
				return true;
				//flow_start_falg = false;
				//Log.e(TAG,"Get flow ready" );
				//return true ;
			}
			sleep(50);
			if(flow_buffer[0]== 0x13)//暂停标志
			{

				if(flag ==false )
				{
					flag=true;
					Log.e(TAG,"Get flow 13" );
				}

				continue;
				//flow_start_falg = false;

				//return true ;
			}

			if(flow_buffer[0]== 0x11)
			{

				Log.e(TAG,"Get flow 11" );
				flow_buffer[0]=  0x0;
				return true;
				//flow_start_falg = false;
				//Log.e(TAG,"Get flow ready" );
				//return true ;
			}


			if(timeout !=0)
			{
				if(TimeIsOver(timeout))
				{

					Log.e(TAG,"Get_Printer flow timeout");
					return false;

				}

			}

			sleep(50);
		}


	}

	private class BmpThread extends Thread {
		public BmpThread() {
		}

		public void run() {
			super.run();
			//PrinterPowerOff();
			PrinterPowerOnAndWaitReady();
			/*if(!Warning_When_Not_Normal())
			{
				PrinterPowerOff();
				return;
			}*/
			//Wait_Printer_Ready();
			//ConsoleActivity.this.sleep(1000);
			lock.acquire();


			try {
				Resources r = getResources();
				// 以数据流的方式读取资源
				InputStream is = r.openRawResource(R.drawable.test222);
				BitmapDrawable bmpDraw = new BitmapDrawable(is);
				Bitmap bmp = bmpDraw.getBitmap();
				//PrintBmp(10, bmp);
				//PrintQuckBmp(1400,25,"We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.TT",StaticLayout.Alignment.ALIGN_NORMAL);
				//PrintQuckBmp();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//Wait_Printer_Ready();
				lock.release();
				//ConsoleActivity.this.sleep(1000);
				//HdxUtil.SetPrinterPower(0);

			}
			try {
				Resources r = getResources();
				// 以数据流的方式读取资源
				InputStream is = r.openRawResource(R.drawable.test2);
				BitmapDrawable bmpDraw = new BitmapDrawable(is);
				Bitmap bmp = bmpDraw.getBitmap();
				PrintBmp(0, bmp);
				//PrintQuckBmp(1400,25,"We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.TT",StaticLayout.Alignment.ALIGN_NORMAL);
				//PrintQuckBmp();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//Wait_Printer_Ready();
				//lock.release();
				//ConsoleActivity.this.sleep(1000);
				//HdxUtil.SetPrinterPower(0);

			}

			handler.sendMessage(handler
					.obtainMessage(ENABLE_BUTTON, 1, 0, null));
		}
	}

	private class GetVersionThread extends Thread {
		int type =0;
		public GetVersionThread(int type) {
			this.type=type;
		}

		public void run() {
			super.run();

			ConsoleActivity.this.sleep(500);
			lock.acquire();
			try {

				ConsoleActivity.strVer = new StringBuilder();
				ConsoleActivity.ver_start_falg = true;
				if(type == 0)
				{
					byte[] start2 = { 0x1D, 0x67, 0x66 };
					mOutputStream.write(start2);
				}
				else
				{
					byte[] start2 = { 0x1D, 0x67, 0x33 };
					mOutputStream.write(start2);
				}


			} catch (Exception e) {
				Log.e(TAG, "quck =" + "here1");
				e.printStackTrace();
			} finally {
				lock.release();
				ConsoleActivity.this.sleep(500);
				// HdxUtil.SetPrinterPower(0);
			}

		}
	}


	private class Busser extends Thread {

		public Busser( ) {

		}

		public void run() {
			super.run();
			HdxUtil.EnableBuzze(1);
			ConsoleActivity.this.sleep(500);
			HdxUtil.EnableBuzze(0);



		}
	}


	void Time_Check_Start() {
		time.setToNow(); // ȡ��ϵͳʱ�䡣
		TimeSecond = time.second;


	}

	boolean TimeIsOver(int second) {

		time.setToNow(); // ȡ��ϵͳʱ�䡣
		int t = time.second;
		if (t < TimeSecond) {
			t += 60;
		}

		if (t - TimeSecond > second) {
			return true;
		}
		return false;
	}

	// get current fw version
	public boolean get_fw_version() {
		HdxUtil.SetPrinterPower(0);
		ConsoleActivity.this.sleep(100);
		HdxUtil.SetPrinterPower(1);
		ConsoleActivity.this.sleep(800);

		//ConsoleActivity.strVer = new StringBuilder();
		//ConsoleActivity.ver_start_falg = true;
		byte[] start3 = { 0x1B, 0x23, 0x56 };

		try {
			mOutputStream.write(start3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*
		byte[] start2 = { 0x1D, 0x67, 0x66 };
		try {
			mOutputStream.write(start2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		ConsoleActivity.this.sleep(800);
		strVer = new StringBuilder("900");
		oldVer = new StringBuilder("900");
		return true ;
	}


	// get current fw version
	public boolean get_Language()
	{
		new GetVersionThread(1).start();
		Time_Check_Start();
		Log.i(TAG," get_Language  "  );
		String strTemp;
		int i;
		while (true)
		{
			if (TimeIsOver(3))
			{
				Log.e(TAG, " faild ,TimeIsOver " );
				return false;

			}
			ConsoleActivity.this.sleep(10);
			strTemp = strVer.toString().trim();
			if (strTemp.length() >= 10)
			{
				i = strTemp.indexOf(':');//i = strTemp.indexOf(".bin");
				if (i == -1)
				{

					Log.e(TAG, " faild ,onDataReceivee= "+ strTemp.length());
					//return false;
				}
				else
				{

					strTemp = strTemp.substring(i + 2).trim();
					strVer = new StringBuilder(strTemp);
					ConsoleActivity.ver_start_falg = false;
					Log.e(TAG, " ok ,onDataReceivet= "+ strVer.toString() );
					try {
						i = Integer.parseInt(strVer.toString());
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;

				}

			}



		}

	}
	void int2ByteAtr(int pData, byte sumBuf[]) {
		for (int ix = 0; ix < 4; ++ix) {
			int offset = ix * 8;
			sumBuf[ix] = (byte) ((pData >> offset) & 0xff);
		}

	}

	// 4�ֽ����
	void Get_Buf_Sum(byte dataBuf[], int dataLen, byte sumBuf[]) {

		int i;
		long Sum = 0;
		// byte[] byteNum = new byte[8];
		long temp;

		for (i = 0; i < dataLen; i++) {
			if (dataBuf[i] < 0) {
				temp = dataBuf[i] & 0x7f;
				temp |= 0x80L;

			} else {
				temp = dataBuf[i];
			}
			Sum += temp;
			temp = dataBuf[i];

		}

		for (int ix = 0; ix < 4; ++ix) {
			int offset = ix * 8;
			sumBuf[ix] = (byte) ((Sum >> offset) & 0xff);
		}

	}

	private class UpdateFWThread extends Thread {
		int type;
		public UpdateFWThread(int type) {
			this.type=type;
		}

		public void run() {

			byte[] start2 = { 0x1B, 0x23, 0x23, 0x55, 0x50, 0x50, 0x47 };
			int temp;
			super.run();

			HdxUtil.SetPrinterPower(1);
			ConsoleActivity.this.sleep(500);

			lock.acquire();

			Message message = new Message();
			handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS, 1, 0,getResources().getString(R.string.itpw)  ));
			try {
				if(type == 0 )
				{
					SendLongDataToUart(BinFileNum,start2,100,50);
				}
				else
				{
					SendLongDataToUart(BinFile,start2,100,50);
				}
				Log.e("quck2", "all data have send!!  ");
				sleep(3000);
				get_fw_version();
				message = new Message();
				message.what = UPDATE_FW;
				handler.sendMessage(message);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				ConsoleActivity.this.sleep(200);
				lock.release();
				// HdxUtil.SetPrinterPower(0);
			}

			handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0, null));

		}
	}

	private class UpdateFontLib_Thread extends Thread
	{
		int type;
		public UpdateFontLib_Thread( int type)
		{
			this.type=type;
		}


		public void run() {
			super.run();
			HdxUtil.SetPrinterPower(1);

			ConsoleActivity.this.sleep(500);

			//,0x1B,0x23,0x23,0x55 ,0x50 ,0x46 ,0x54 checksum len X1 X2 ... Xlen
			byte[] cmdHead= {0x1B,0x23,0x23,0x55 ,0x50 ,0x46 ,0x54};
			lock.acquire();
			handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1, 0,null));
			handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS, 1, 0,getResources().getString(R.string.itpw2)));
			ConsoleActivity.this.sleep(1000);
			if(type ==0)
			{
				SendLongDataToUart(BinFileNum,cmdHead,30*1000,40);
			}
			else
			{
				SendLongDataToUart(BinFile,cmdHead,30*1000,40);
			}

			//lock.release();
			handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS, 1, 0,null));
			handler.sendMessage(handler.obtainMessage(SHOW_FONT_UPTAE_INFO, 1, 0,getResources().getString(R.string.itpw4)));
			handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));

		}
	}

	public void SendLongDataToUart(int fileID,byte [] command_head,int delay_time,int delay_time2 )
	{
		byte[] byteNum = new byte[4];
		byte[] byteNumCrc = new byte[4];
		byte[] byteNumLen = new byte[4];
		int i;
		int temp;
		Log.e(TAG,"  TEST_quck2");
		flow_begin();
		try {
			Resources r =getResources();;
			InputStream is = r.openRawResource(fileID);
			int count = is.available();
			byte[] b = new byte[count];
			is.read(b);
			byte SendBuf[] = new byte[count  +1023];
			Arrays.fill(SendBuf,(byte)0);

			Log.e("quck2", " read file is .available()= "+ count  );
			//get command HEAD


			//get crc
			Get_Buf_Sum(b,count,byteNum);// 17	01 7E 00   CRC
			System.arraycopy(byteNum,0,byteNumCrc,0,4);
			Log.e("quck2", "crc0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "crc1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "crc2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "crc3  "+ String.format("0x%02x", byteNum[3] )  );


			//get len
			int2ByteAtr(count,byteNum); //58 54 01 00	LEN
			System.arraycopy(byteNum,0, byteNumLen,0,4);
			Log.e("quck2", "len0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "len1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "len2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "len3  "+ String.format("0x%02x", byteNum[3] )  );

			//send command_head
			mOutputStream.write(command_head);
			//send crc
			mOutputStream.write(byteNumCrc);
			//send len
			mOutputStream.write(byteNumLen);
			//send bin file
			System.arraycopy(b,0,SendBuf,0, count);
			temp= (count +63)/64;
			byte[] databuf= new byte[64];
			sleep(delay_time);
			for(i=0;i<temp;i++)
			{
				System.arraycopy(SendBuf,i*64,databuf,0,64);

				//if((i%2) == 0)
				{
					//sleep(delay_time2);

				}
				Log.e("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");
				iProgress=((i+1)*100)/temp;
				handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
				mOutputStream.write(databuf);
				flow_check_and_Wait(10);
				//sleep(delay_time2);

			}

			Log.e("quck2", "all data have send!!  "   );
			sleep(3000);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConsoleActivity.this.sleep(200);
			flow_end();
			//HdxUtil.SetPrinterPower(0);
		}

		handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));
	}

	public void SendRawFileToUart(int fileID,byte [] command_head,int delay_time,int delay_time2 )
	{
		byte[] byteNum = new byte[4];
		byte[] byteNumCrc = new byte[4];
		byte[] byteNumLen = new byte[4];
		int i;
		int temp;
		Log.e(TAG,"  TEST_quck2");
		flow_begin();
		try {
			Resources r =getResources();;
			InputStream is = r.openRawResource(fileID);
			int count = is.available();
			byte[] b = new byte[count];
			is.read(b);
			byte SendBuf[] = new byte[count  +1023];
			Arrays.fill(SendBuf,(byte)0);

			Log.e("quck2", " read file is .available()= "+ count  );
			//get command HEAD


			//get crc
			Get_Buf_Sum(b,count,byteNum);// 17	01 7E 00   CRC
			System.arraycopy(byteNum,0,byteNumCrc,0,4);
			Log.e("quck2", "crc0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "crc1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "crc2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "crc3  "+ String.format("0x%02x", byteNum[3] )  );


			//get len
			int2ByteAtr(count,byteNum); //58 54 01 00	LEN
			System.arraycopy(byteNum,0, byteNumLen,0,4);
			Log.e("quck2", "len0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "len1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "len2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "len3  "+ String.format("0x%02x", byteNum[3] )  );

			//send command_head
			//mOutputStream.write(command_head);
			//send crc
			//mOutputStream.write(byteNumCrc);
			//send len
			//mOutputStream.write(byteNumLen);
			//send bin file
			System.arraycopy(b,0,SendBuf,0, count);
			temp= (count +63)/64;
			byte[] databuf= new byte[64];
			sleep(delay_time);
			for(i=0;i<temp;i++)
			{
				System.arraycopy(SendBuf,i*64,databuf,0,64);

				//if((i%2) == 0)
				{
					//sleep(delay_time2);

				}
				Log.e("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");
				iProgress=((i+1)*100)/temp;
				//handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
				mOutputStream.write(databuf);
				flow_check_and_Wait(10);
				//sleep(delay_time2);

			}

			Log.e("quck2", "all data have send!!  "   );
			sleep(3000);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConsoleActivity.this.sleep(200);
			flow_end();
			//HdxUtil.SetPrinterPower(0);
		}

		//handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));
	}


	public void SendFileToUart(File file,byte [] command_head,int delay_time,int delay_time2 )
	{
		byte[] byteNum = new byte[4];
		byte[] byteNumCrc = new byte[4];
		byte[] byteNumLen = new byte[4];
		int i;
		int temp;
		FileInputStream is ;
		flow_begin();
		try {

			is  = new FileInputStream(file);
			int count = is.available();
			byte[] b = new byte[count];
			is.read(b);
			byte SendBuf[] = new byte[count  +1023];
			Arrays.fill(SendBuf,(byte)0);

			Log.e("quck2", " read file is .available()= "+ count  );
			//get command HEAD
			//get crc
			Get_Buf_Sum(b,count,byteNum);// 17	01 7E 00   CRC
			System.arraycopy(byteNum,0,byteNumCrc,0,4);
			Log.e("quck2", "crc0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "crc1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "crc2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "crc3  "+ String.format("0x%02x", byteNum[3] )  );


			//get len
			int2ByteAtr(count,byteNum); //58 54 01 00	LEN
			System.arraycopy(byteNum,0, byteNumLen,0,4);
			Log.e("quck2", "len0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "len1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "len2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "len3  "+ String.format("0x%02x", byteNum[3] )  );

			//send command_head
			//mOutputStream.write(command_head);
			//send crc
			//mOutputStream.write(byteNumCrc);
			//send len
			//mOutputStream.write(byteNumLen);
			//send bin file
			System.arraycopy(b,0,SendBuf,0, count);
			temp= (count +63)/64;
			byte[] databuf= new byte[64];
			//sleep(delay_time);
			for(i=0;i<temp;i++)
			{
				System.arraycopy(SendBuf,i*64,databuf,0,64);

				//if((i%2) == 0)
				{
					//sleep(delay_time2);

				}
				Log.e("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");
				iProgress=((i+1)*100)/temp;
				handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
				mOutputStream.write(databuf);
				flow_check_and_Wait(10);

			}

			Log.e("quck2", "all data have send!!  "   );
			sleep(3000);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConsoleActivity.this.sleep(200);
			flow_end();
			//HdxUtil.SetPrinterPower(0);
		}

		//handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));
	}



	public void SendLongDataToUart(File file,byte [] command_head,int delay_time,int delay_time2 )
	{
		byte[] byteNum = new byte[4];
		byte[] byteNumCrc = new byte[4];
		byte[] byteNumLen = new byte[4];
		int i;
		int temp;
		FileInputStream is ;
		flow_begin();
		try {

			is  = new FileInputStream(file);
			int count = is.available();
			byte[] b = new byte[count];
			is.read(b);
			byte SendBuf[] = new byte[count  +1023];
			Arrays.fill(SendBuf,(byte)0);

			Log.e("quck2", " read file is .available()= "+ count  );
			//get command HEAD
			//get crc
			Get_Buf_Sum(b,count,byteNum);// 17	01 7E 00   CRC
			System.arraycopy(byteNum,0,byteNumCrc,0,4);
			Log.e("quck2", "crc0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "crc1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "crc2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "crc3  "+ String.format("0x%02x", byteNum[3] )  );


			//get len
			int2ByteAtr(count,byteNum); //58 54 01 00	LEN
			System.arraycopy(byteNum,0, byteNumLen,0,4);
			Log.e("quck2", "len0  "+ String.format("0x%02x", byteNum[0] )  );
			Log.e("quck2", "len1  "+ String.format("0x%02x", byteNum[1] )	);
			Log.e("quck2", "len2  "+ String.format("0x%02x", byteNum[2] )	);
			Log.e("quck2", "len3  "+ String.format("0x%02x", byteNum[3] )  );

			//send command_head
			mOutputStream.write(command_head);
			//send crc
			mOutputStream.write(byteNumCrc);
			//send len
			mOutputStream.write(byteNumLen);
			//send bin file
			System.arraycopy(b,0,SendBuf,0, count);
			temp= (count +63)/64;
			byte[] databuf= new byte[64];
			sleep(delay_time);
			for(i=0;i<temp;i++)
			{
				System.arraycopy(SendBuf,i*64,databuf,0,64);

				//if((i%2) == 0)
				{
					//sleep(delay_time2);

				}
				Log.e("quck2", " updating ffont finish:"  +((i+1)*100)/temp +"%");
				iProgress=((i+1)*100)/temp;
				handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
				mOutputStream.write(databuf);
				flow_check_and_Wait(10);

			}

			Log.e("quck2", "all data have send!!  "   );
			sleep(3000);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConsoleActivity.this.sleep(200);
			flow_end();
			//HdxUtil.SetPrinterPower(0);
		}

		handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0,null));
	}
	private class WriteThread extends Thread {
		int  action_code;

		public WriteThread(int  code) {
			action_code = code;
		}

		public void run() {
			super.run();
			PrinterPowerOnAndWaitReady();
			if(!Warning_When_Not_Normal())
			{
				PrinterPowerOff();
				return;
			}

			lock.acquire();
			try {

				//Wait_Printer_Ready();
				switch(action_code)
				{
					case 0:
						sendCommand(0x1b, 0x61, 1);
						sendCharacterDemo();
						sendCommand(0x0a);
						sendCommand(0x1d,0x56,0x42,0x20);
						sendCommand(0x1d, 0x56, 0x30);
						Log.e("quck2", " print char test"   );
						break;
					case 1:
						Log.e("quck2", "Print Code test  "   );
						while (true )
						{

							sendCodeDemo();
						}


					default:
						sendCharacterDemo2();
						break;
				}
				// ConsoleActivity.this.sleep(14000);

			} finally {
				Wait_Printer_Ready();
				lock.release();
				PrinterPowerOff();
				handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1,0, null));
			}

		}
	}

	private class InitThread extends Thread {
		int  action_code;

		public InitThread(int  code) {
			action_code = code;
		}

		public void run() {
			super.run();
			//PrinterPowerOnAndWaitReady();
			lock.acquire();
			try {

				switch(action_code)
				{

					case 3:
						Init_Data_When_Start();

						break;
					default:
						break;
				}
				//ConsoleActivity.this.sleep(4000);

			} finally {

				lock.release();


			}

		}
	}
	void Init_Data_When_Start()
	{

		handler.sendMessage(handler.obtainMessage(DISABLE_BUTTON, 1,0, null));
		handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS, 1, 0,getResources().getString(R.string.itpw3)));
		//PrinterPowerOnAndWaitReady();

		int i =15;// Integer.parseInt(strVer.toString());
		String[] city=getResources().getStringArray(R.array.language);
		Printer_Info=getResources().getString(R.string.CurrentLanguageis);
		if(!city[i].isEmpty())
		{

			Printer_Info +=city[i];
			Printer_Info +="\n";
		}
		else
		{
			Printer_Info="";
			Printer_Info +="\n";
		}
		iProgress=3;
		handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
		String str;
		if(get_fw_version())
		{

			str=getResources().getString(R.string.currentFWV);
			str +=strVer.toString();
			str +="\n";
			strVer=new StringBuilder(str);
		}
		if(Warning_When_Not_Normal())
		{
			//if(get_fw_version())
			{

				//str=getResources().getString(R.string.currentFWV);
				///str +=strVer.toString();
				//str +="\n";
				//str2  =getResources().getString(R.string.HavePaper);
				//	str2 +="\n";
				//strVer=new StringBuilder(str);

				//handler.sendMessage(handler.obtainMessage(SHOW_PRINTER_INFO_WHEN_INIT, 1,0, null));
				//iProgress=50;
				//handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
				//handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS, 1, 0,null));
				//handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1,0, null));
			}
		}


		//iProgress=50;
		//handler.sendMessage(handler.obtainMessage(REFRESH_PROGRESS, 1, 0,null));
		handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS, 1, 0,null));
		//handler.sendMessage(handler.obtainMessage(SHOW_FONT_UPTAE_INFO, 1, 0,getResources().getString(R.string.IsOutOfPaper)));
		handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1,0, null));
		handler.sendMessage(handler.obtainMessage(SHOW_PRINTER_INFO_WHEN_INIT, 1,0, null));




		//PrinterPowerOff();

	}



	void test_qr5()
	{


		//code 128
		//sendCommand(0x1d,0x6b,0x49,0x05,0x31,0x32,0x33,0x34,0x35);


		//sendCommand(29 ,40 ,107 ,03, 0, 49, 67,16);
		sendCommand( 0x1D,0x28,0x6B,0x0D,0x00,0x31,0x50,0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30 );

		sendCommand(0x1d,0x28,0x6b,0x03,0x00,0x31,0x51,0x30);
		sendCommand(0x1b,0x4a,0x30);   //li
	}



	void print_qr_str(String str)
	{

		byte [] data =str.getBytes();
		//byte [] test={0x6D};
		int qr_len=data.length+3;
	/* (71+3+80+150)%256 */

		byte [] qr_command={0x1d, 0x28, 0x6b,1, 0, 0x31, 0x50,	0x30};
		qr_command[4]=  (byte)((qr_len &0xff00)>>8);
		qr_command[3]= (byte)((qr_len &0xff));
		Log.e(TAG, "qr_len:"+Integer.toHexString(qr_len) +" ,h: " +Integer.toHexString(qr_command[4])+" ,L:"+Integer.toHexString(qr_command[3]));

		//byte [] SendBuf =new byte[data.length+qr_command.length];

		//System.arraycopy(qr_command,0,SendBuf,0,qr_command.length);
		//System.arraycopy(data,0,SendBuf,qr_command.length,data.length);
		try {
			mOutputStream.write(qr_command);
			mOutputStream.write(data);
			//mOutputStream.write(SendBuf);
			//mOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(0x1d, 0x28, 0x6b, 0x03, 0x00, 0x31, 0x51, 0x30);
	}
	private void sendCodeDemo() {


		test_qr5();
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed

		sendTest('1','1','1');
		sendTest('2','2','3');
		sendString("1234");
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
	}
	void sendTest(int...command ) {
		int LH,LL;
		sendCommand( 0x1d,0x68,162  );
		sendCommand(0x1d, 0x45, 0x43, 0x1);
		LH= (command.length>>8);
		LL= command.length&0xff;
		sendCommand( 0x1d,0x6b,0x49,LL);
		sendCommand(command);


	}

	void sendString(String command) {
		int LH,LL;
		sendCommand( 0x1d,0x68,162  );



		sendCommand(0x1d, 0x45, 0x43, 0x1);




		LH= (command.length()>>8);



		LH=LH;


		LL= command.length()&0xff;


		sendCommand( 0x1d,0x6b,0x49,LL);



		sendCommand(command.getBytes());


	}

	private void sendCommand(byte... command) {
		try {
			for (int i = 0; i < command.length; i++) {
				mOutputStream.write(command[i]);
				// Log.e(TAG,"command["+i+"] = "+Integer.toHexString(command[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// / sleep(1);
	}
	private void sendCommand(int... command) {
		try {
			for (int i = 0; i < command.length; i++) {
				mOutputStream.write(command[i]);
				// Log.e(TAG,"command["+i+"] = "+Integer.toHexString(command[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// / sleep(1);
	}

	private void sendCharacterDemo() {

		FirebaseCrash.log("checking sucess" + jsonArray);
		//sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x0e ); // taiwan
		//sendCommand(0x1b, 0x61, 49);
		try {
			JSONArray array = new JSONArray(jsonArray);
			JSONObject b = array.getJSONObject(0);
			String func   = b.getString("func");
			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = df.format(c.getTime());
			try {
				if ("fuelstdailysumm".equals(func)){
					String companyname	= " "+b.getString("companyname");
					String companyaddr 	= " "+b.getString("companyaddr");
					String saleheading	= " SALES DAILY SUMMARY";
					String separator	= " ---------------------";
					String sales		= " Sales";
					String volume		= " Fuel Volume";
					String username		= " Fuel Attendant:"+b.getString("username");
					String station		= " Station     :" 	+b.getString("station");
					String summ_amt		= " Total Sales :"	+b.getString("summ_amt");
					String summ_cash	= " Cash Sales  :"	+b.getString("summ_cash");
					String summ_mpesa	= " Mpesa Sales :"	+b.getString("summ_mpesa");
					String summ_volume	= " Total Vol(l):"	+b.getString("summ_volume");
					String summ_petrol	= " Petrol Vol  :"	+b.getString("summ_petrol");
					String summ_diesel	= " Diesel Vol  :"	+b.getString("summ_diesel");

					mOutputStream.write(companyname.getBytes());sendCommand(0x0a);
					mOutputStream.write("P.O. Box 69483-00400 ".getBytes());sendCommand(0x0a);
					mOutputStream.write(companyaddr.getBytes());sendCommand(0x0a);
					String d = " "+formattedDate;
					sendCommand(0x0a);
					sendCommand(0x1b, 0x61, 0);
					mOutputStream.write(saleheading.getBytes());sendCommand(0x0a);
					mOutputStream.write(d.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(username.getBytes());sendCommand(0x0a);
					mOutputStream.write(station.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(sales.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_amt.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_cash.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_mpesa.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(volume.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_volume.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_petrol.getBytes());sendCommand(0x0a);
					mOutputStream.write(summ_diesel.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write("  Terms and Conditions apply ".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(" info@nuclearinvestments.com".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					sendCommand(0x0a);
				}

				if ("fuelstsale".equals(func)){
					String companyname	= " "+b.getString("companyname");
					String companyaddr 	= " "+b.getString("companyaddr");
					String saleheading	= " SALE RECEIPT";
					String station		= " Station      : " +b.getString("station");
					String receipt		= " Receipt#     : " +b.getString("receipt");
					String vehregno		= " Vehicle Reg# : " +b.getString("vehregno");
					String amount		= " Sale amount  : " +b.getString("amount");
					String volume		= " Volume (l)   : " +b.getString("volume");
					String ftype 		= " Fuel type    : " +b.getString("ftype");
					String pmethod		= " Payment mthd : " +b.getString("pmethod");
					String username 	= " Served by    : " +b.getString("username");

					mOutputStream.write(companyname.getBytes());sendCommand(0x0a);
					mOutputStream.write("P.O. Box 69483-00400 ".getBytes());sendCommand(0x0a);
					mOutputStream.write(companyaddr.getBytes());sendCommand(0x0a);
					String d = " "+formattedDate;
					sendCommand(0x0a);
					sendCommand(0x1b, 0x61, 0);
					mOutputStream.write(saleheading.getBytes());sendCommand(0x0a);
					mOutputStream.write(d.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(station.getBytes());sendCommand(0x0a);
					mOutputStream.write(receipt.getBytes());sendCommand(0x0a);
					mOutputStream.write(vehregno.getBytes());sendCommand(0x0a);
					mOutputStream.write(amount.getBytes());sendCommand(0x0a);
					mOutputStream.write(volume.getBytes());sendCommand(0x0a);
					mOutputStream.write(ftype.getBytes());sendCommand(0x0a);
					mOutputStream.write(pmethod.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(username.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write("  Terms and Conditions apply ".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(" info@nuclearinvestments.com".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					sendCommand(0x0a);
				}
				if ("sum".equals(func)) {
					String cname 	= "   "+b.getString("busname");
					String caddress = "  "+b.getString("busaddress");

					String[] firstname = b.getString("firstname").split(",");
					String[] lastname  = b.getString("lastname").split(",");
					String namess = "";
					for(int cc = 0;cc < firstname.length;cc++){
						namess += ","+firstname[cc]+" "+lastname[cc];
					}

					String names = namess.substring(1);

					String[] idn = b.getString("idn").split(",");
					String[] mobile = b.getString("mobile").split(",");
					String ticket      = " Ticket no   : "+b.getString("ticket");
					//String name        = " Name        : "+names;
					String passname    = " Name        : "+b.getString("passname");
					//String idno      = " Id no       : "+b.getString("idn");
					//String mobiles   = " Mobile      : "+b.getString("mobile");
					String selectedbus = " Bus no      : "+b.getString("selectedbus");
					//String seat        = " Seat no     : "+b.getString("seat");
					//String dater       = " Travel date : "+b.getString("dater");
					String source      = " From        : "+b.getString("source");
					String destination = " To          : "+b.getString("destination");
					String total       = " Fare        : "+b.getString("total");
					String agent       = " Booked by   : "+b.getString("agent");
					mOutputStream.write(cname.getBytes());sendCommand(0x0a);
					mOutputStream.write("P.O.Box 69483-00400 ".getBytes());sendCommand(0x0a);
					mOutputStream.write(caddress.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					String d = " "+formattedDate;
					sendCommand(0x1b, 0x61, 0);
					mOutputStream.write(d.getBytes());sendCommand(0x0a);
					mOutputStream.write(ticket.getBytes());sendCommand(0x0a);
					mOutputStream.write(passname.getBytes());sendCommand(0x0a);
					//mOutputStream.write(name.getBytes());sendCommand(0x0a);
					//mOutputStream.write(idno.getBytes());sendCommand(0x0a);
					//mOutputStream.write(mobiles.getBytes());sendCommand(0x0a);
					mOutputStream.write(selectedbus.getBytes());sendCommand(0x0a);
					//mOutputStream.write(seat.getBytes());sendCommand(0x0a);
					//mOutputStream.write(dater.getBytes());sendCommand(0x0a);
					mOutputStream.write(source.getBytes());sendCommand(0x0a);
					mOutputStream.write(destination.getBytes());sendCommand(0x0a);
					mOutputStream.write(total.getBytes());sendCommand(0x0a);
					mOutputStream.write(agent.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write("  Terms and Conditions apply ".getBytes());sendCommand(0x0a);
					mOutputStream.write("       Not transferable ".getBytes());sendCommand(0x0a);
					mOutputStream.write(" info@nuclearinvestments.com".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					sendCommand(0x0a);
				}
				if ("booksum".equals(func)){
					String cname 	= "   "+b.getString("busname");
					String caddress = "  "+b.getString("busaddress");
					String agent =" Agent      :"+b.getString("agent");
					String nob   =" Bookings   :"+b.getString("nob");
					String tfc   =" Collection :"+b.getString("tfc");

					mOutputStream.write(cname.getBytes());sendCommand(0x0a);
					mOutputStream.write("P.O.Box 69483-00400 ".getBytes());sendCommand(0x0a);
					mOutputStream.write(caddress.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					String d = " "+formattedDate;
					sendCommand(0x1b, 0x61, 0);
					mOutputStream.write(d.getBytes());sendCommand(0x0a);
					mOutputStream.write(agent.getBytes());sendCommand(0x0a);
					mOutputStream.write(nob.getBytes());sendCommand(0x0a);
					mOutputStream.write(tfc.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write("  Terms and Conditions apply ".getBytes());sendCommand(0x0a);
					mOutputStream.write("     Powered by AVTTMS ".getBytes());sendCommand(0x0a);
					mOutputStream.write(" info@nuclearinvestments.com".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					sendCommand(0x0a);
				}
				if ("dreceipt".equals(func)){
					String cname 	= "   "+b.getString("busname");
					String caddress = "  "+b.getString("busaddress");
					String deliverynum	= " Delivery#  :"+b.getString("deliverynum");
					String vehowner		= " Client name:"+b.getString("vehowner");
					String selbus		= " Bus no      : "+b.getString("selbus");
					String source		= " From        : "+b.getString("source");
					String destination	= " To          : "+b.getString("destination");
					String passnum		= " No passenger: "+b.getString("passnum");
					String payheader	= " Payment details";
					String grossamt		= " Gross amount    : "+b.getString("grossamt");
					String separator	= " ------------------------------";
                    String deliveryinfo = "   DELIVERY RECEIPT";
					String servcharge	= " Service charge  : "+b.getString("servcharge");
					String othercharge	= " Other charge    : "+b.getString("othercharge");
					String loan			= " Loan amount     : "+b.getString("loan");
					String insurance	= " Insurance amount: "+b.getString("insurance");
					String totdeduct	= " Total deduction : "+b.getString("totdeduct");
					String netamt		= " Net amount      : "+b.getString("netamt");
					String agentname	= " Delivery by : "+b.getString("agentname");
					mOutputStream.write(cname.getBytes());sendCommand(0x0a);
					mOutputStream.write("P.O. Box 69483-00400 ".getBytes());sendCommand(0x0a);
					mOutputStream.write(caddress.getBytes());sendCommand(0x0a);
					String d = " "+formattedDate;
                    sendCommand(0x0a);
                    mOutputStream.write(deliveryinfo.getBytes());sendCommand(0x0a);
                    sendCommand(0x1b, 0x61, 0);
					mOutputStream.write(d.getBytes());sendCommand(0x0a);
					mOutputStream.write(deliverynum.getBytes());sendCommand(0x0a);
					mOutputStream.write(vehowner.getBytes());sendCommand(0x0a);
					mOutputStream.write(selbus.getBytes());sendCommand(0x0a);
					mOutputStream.write(source.getBytes());sendCommand(0x0a);
					mOutputStream.write(destination.getBytes());sendCommand(0x0a);
					mOutputStream.write(passnum.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(payheader.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(grossamt.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(servcharge.getBytes());sendCommand(0x0a);
					mOutputStream.write(othercharge.getBytes());sendCommand(0x0a);
					mOutputStream.write(loan.getBytes());sendCommand(0x0a);
					mOutputStream.write(insurance.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(totdeduct.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(netamt.getBytes());sendCommand(0x0a);
					mOutputStream.write(separator.getBytes());sendCommand(0x0a);
					mOutputStream.write(agentname.getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write("  Terms and Conditions apply ".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					mOutputStream.write(" info@nuclearinvestments.com".getBytes());sendCommand(0x0a);
					sendCommand(0x0a);
					sendCommand(0x0a);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException ie){
				FirebaseCrash.log("bug" + ie);
			}
		} catch (JSONException e) {
			FirebaseCrash.log("1" + e);
			e.printStackTrace();
		}

		/*Log.e(TAG, "#########sendCharacterDemo##########");//,0x1B,0x23,0x46

		sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x0e ); // taiwan
		sendCommand(0x1b, 0x61, 49);
		try {
			mOutputStream.write("撥出受固定撥號限制".getBytes("Big5"));
			mOutputStream.write("目前無法連上這個網路".getBytes("Big5"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(0x0a);

		try {
			PrinterPowerOff();
			PrinterPowerOnAndWaitReady();
			PrinterPowerOnAndWaitReady();
			sendCommand(0x1B,0x23,0x23,0x73,0x70,0x6c,0x76,0x00);
			sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 39); //  阿拉伯语
			mOutputStream.write("يضصثقثقغنهفهخغعفهخغتخهتنميبتسينمبتسيمنبت".getBytes("cp864"));
			mOutputStream.write("يضصثقثقغنهفهخغعفهخغتخهتنميبتسينمبتسيمنبت".getBytes("cp1256"));
			mOutputStream.write("يضصثقثقغنهفهخغعفهخغتخهتنميبتسينمبتسيمنبت".getBytes("cp1256"));
			// PrinterPowerOff();
			//	PrinterPowerOnAndWaitReady();
			//	PrinterPowerOnAndWaitReady();

			//SendRawFileToUart(R.raw.alabo,new byte[0],0,0);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x0f); // china
			sendCommand(0x1D, 0x21, 0x01); // double height
			sendCommand(0x1b, 0x61, 1);
			mOutputStream.write("倍高命令   double Dom test".getBytes("cp936"));
			sendCommand(0x0a);
			sendCommand(0x1D, 0x21, 0x00); // cancel double height
			mOutputStream.write("取消倍高命令取消倍高命 cancel double height".getBytes("cp936"));
			sendCommand(0x0a);
			sendCommand(0x1D, 0x21, 0x10); // double width
			mOutputStream.write("倍宽命令  double width ".getBytes("cp936"));
			sendCommand(0x0a);
			sendCommand(0x1D, 0x21, 0x00); // cancel double width
			mOutputStream.write("取消倍宽命令取消倍宽命令取消倍宽命令 cancel double width".getBytes("cp936"));
			sendCommand(0x0a);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mOutputStream.write("english test".getBytes());
			sendCommand(0x0a);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x20); // thailand

		try {
			mOutputStream.write("แต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้าหากเธอไม่สามารถช่วยพี่ช"
					.getBytes("cp874"));
			int size,i;
			String strd="แต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้";
			byte []buffer  =strd.getBytes("cp874");
			size=buffer.length;
			StringBuilder str = new StringBuilder();
			StringBuilder strBuild = new StringBuilder();
			for (i = 0; i < size; i++) {

				str.append(String.format("%02x", buffer[i]));
				strBuild.append(String.format("%c", (char) buffer[i]));
			}
			Log.e(TAG, "oxxxC= " + strBuild.toString());
			Log.e(TAG, "oxxxX= " + str.toString());

			sendCommand(0x0a);
			//40个泰文
			mOutputStream.write("แต่ถ้าหากเธอแต่ถ้าหากเธอแต่ถ้าหากเธอแต่ถ้าหากเธอ".getBytes("cp874"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(0x0a);

		sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x22); // russia
		try {
			mOutputStream
					.write("У этого сайта проблемы с сертификатом безопасности."
							.getBytes("CP1251"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(0x0a);
		sleep(200);
		sendCommand(0x1B, 0x23, 0x23, 0x53, 0x4C, 0x41, 0x4E, 0x0f); // china
		sleep(200);
		sendCommand(0x1D, 0x42, 0x1 ); //使能反白
		try {
			mOutputStream.write("反白打印测试反白打印测试反白打印测试反白打印测试反白打印测试反白打印测试反白打印测试反白打印测试 ".getBytes("cp936"));
			sendCommand(0x0a);
			//mOutputStream.write("反白打印测试   ".getBytes("cp936"));
			sendCommand(0x0a);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendCommand(0x1D, 0x42, 0x0 ); //关闭反白
*/
		ConsoleActivity.this.finish();
	}
	private void sendCharacterDemo2() {


		try {
			sendCommand(0x1D, 0x21, 0x00); // cancel double width
			sendCommand(0x1B,0x23,0x23,0x73,0x70,0x6c,0x76,speed);


			mOutputStream.write("We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.We challenged ourselves to create a visual language for our users that synthesizes the classic principles of good design with the innovation and possibility of technology and science. This is material design. This spec is a living document that will be updated as we continue to develop the tenets and specifics of material design.".getBytes());
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x1D,0x56,0x1 );
			sendCommand(0x1D, 0x21, 0x00); // cancel double width
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
			sendCommand(0x0a);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}
	private void sleep(int ms) {
		// Log.d(TAG,"start sleep "+ms);
		try {
			java.lang.Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Log.d(TAG,"end sleep "+ms);
	}

	public void PrintBmp(int startx, Bitmap bitmap) throws IOException {
		// byte[] start1 = { 0x0d,0x0a};

		/*
		byte[] start2 = { 0x1D, 0x67, 0x66 };
		try {
			mOutputStream.write(start2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		sendCommand(0x1b, 0x4a, 0x30);
		sendCommand(0x1b, 0x4a, 0x30);


		byte[] start2 = { 0x1D, 0x76, 0x30, 0x30, 0x00, 0x00, 0x01, 0x00 };

		int width = bitmap.getWidth() + startx;
		int height = bitmap.getHeight();
		Bitmap.Config m =bitmap.getConfig();
		// 332  272  ARGB_8888
		Log.e(TAG,"width:  "+width+" height :"+height+"   m:"+ m);
		if (width > 384)
			width = 384;
		int tmp = (width + 7) / 8;
		byte[] data = new byte[tmp];
		byte xL = (byte) (tmp % 256);
		byte xH = (byte) (tmp / 256);
		start2[4] = xL;
		start2[5] = xH;
		start2[6] = (byte) (height % 256);
		;
		start2[7] = (byte) (height / 256);
		;
		flow_begin();
		mOutputStream.write(start2);

		for (int i = 0; i < height; i++) {

			for (int x = 0; x < tmp; x++)
				data[x] = 0;
			for (int x = startx; x < width; x++) {
				int pixel = bitmap.getPixel(x - startx, i);
				if (Color.red(pixel) == 0 || Color.green(pixel) == 0
						|| Color.blue(pixel) == 0) {
					// 高位在左，所以使用128 右移
					data[x / 8] += 128 >> (x % 8);// (byte) (128 >> (y % 8));
				}
			}

			while ((printer_status & 0x13) != 0) {
				Log.e(TAG, "printer_status=" + printer_status);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
			//flow_check_and_Wait(10);
			mOutputStream.write(data);

			/*
			 * try { Thread.sleep(5); } catch (InterruptedException e) { }
			 */
		}
		flow_end();
	}

	public void PrintBmp(int startx, Bitmap bitmap,int ddd) throws IOException {



		byte[] start2 = { 0x1D, 0x76, 0x30, 0x30, 0x00, 0x00, 0x01, 0x00 };

		int width = bitmap.getWidth() + startx;
		int height = bitmap.getHeight();
		Bitmap.Config m =bitmap.getConfig();
		// 332  272  ARGB_8888
		Log.e(TAG,"width:  "+width+" height :"+height+"   m:"+ m);
		if (width > 384)
			width = 384;
		int tmp = (width + 7) / 8;
		byte[] data = new byte[tmp];
		byte xL = (byte) (tmp % 256);
		byte xH = (byte) (tmp / 256);
		start2[4] = xL;
		start2[5] = xH;
		start2[6] = (byte) (height % 256);
		;
		start2[7] = (byte) (height / 256);
		;
		flow_begin();
		mOutputStream.write(start2);

		for (int i = 0; i < height; i++) {

			for (int x = 0; x < tmp; x++)
				data[x] = 0;
			for (int x = startx; x < width; x++) {
				int pixel = bitmap.getPixel(x - startx, i);
				if (Color.red(pixel) == 0 || Color.green(pixel) == 0
						|| Color.blue(pixel) == 0) {
					// 高位在左，所以使用128 右移
					data[x / 8] += 128 >> (x % 8);// (byte) (128 >> (y % 8));
				}
			}

			while ((printer_status & 0x13) != 0) {
				Log.e(TAG, "printer_status=" + printer_status);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
			flow_check_and_Wait(10);
			mOutputStream.write(data);


		}
		flow_end();
	}

	public void PrintQuckBmp() throws IOException {


		lcd_width=384;
		//lcd_height=1600;
		lcd_height=800;
		mBitmap = Bitmap.createBitmap(lcd_width,lcd_height, Bitmap.Config.valueOf("ARGB_8888"));

		mCanvas = new Canvas(mBitmap);
		mCanvas.drawColor(Color.WHITE);
		drawSubLcd();

		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
		sendCommand(0x1b, 0x4a, 0x30); // line feed
	}
	public void PrintQuckBmp(int h,int Fontsize,String str,StaticLayout.Alignment mode) throws IOException {


		lcd_width=384;
		//lcd_height=1600;
		lcd_height=h;
		mBitmap = Bitmap.createBitmap(lcd_width,lcd_height, Bitmap.Config.valueOf("ARGB_8888"));

		mCanvas = new Canvas(mBitmap);
		mCanvas.drawColor(Color.WHITE);
		drawSubLcd(str,Fontsize,mode );

	}

	static int mCountPrint=0;
	private void drawSubLcd() throws IOException{
		Paint mp = new Paint();
		// mp.setTypeface(Typeface.DEFAULT );

		mp.setTypeface(Typeface.MONOSPACE );
		TextPaint p = new TextPaint (mp);

		p.setAntiAlias(false);
		p.setTextSize(25);
		p.setARGB(0xff, 0x0, 0x0, 0x0);
		// p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		// String str = "好德芯\n尊敬的客户，您本次交费成功，详细信息如下：\n交费工号 \n得得sss  vdc";
		String str2= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str3= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str4= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str5= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str6= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str7= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str8= String.format("(%02X)", mCountPrint);
		mCountPrint++;
		String str9= String.format("(%02X)", mCountPrint);
		mCountPrint++;


		String str ="multiple language testing   \n";
		str =  str+  str2+"Chinese : 我喜欢你我喜欢你我喜欢你我喜欢你 \n";
		str =  str+  str3+"Traditional :目前無法連上這個網路     \n";
		str =  str+  str4+"Thai:  แต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้าหากเธอไม่ แต่ถ้าหากเธอไม่สามารถช่วยพี่ชแต่ถ้าหากเธอไม่ \n";
		str =  str+  str5+"Arab : يضصثقثقغنهفهخغعفهخغتخهتنميبتسينمبتسيمنبتيضصثقثقغنهفهخغعفهخغتخهتنميبتسينمبتسيمنبت\n";
		str =  str + str6+"Japanese:  私はあなたが好き私はあなたが好き 私はあなたが好き私はあなたが好き\n";
		str =  str + str7+"Korean :  나 는 당신 나 는 당신 나 는 당신 나 는 당신 나 는 당신 나 는 당신\n";
		str =  str + str8+"French :  Je vous aime Je vous aime\n";
		str =  str + str9+"Russian :  Ты мне нравишься Ты мне нравишься\n";
		//str = str + "Spanish :  Me gustas, quiero darle a UN chico, volviste rápido   \n";
		//str = str + "Portuguese :  código postal, Mas por via avião custa muito\n";

		StaticLayout layout = new StaticLayout(str,
				p,lcd_width, StaticLayout.Alignment.ALIGN_NORMAL,1.0F,0.0F,true);

		layout.draw(mCanvas);

		Log.d("sublcd", "start display");


		PrintBmp(00, mBitmap);


		Log.d("sublcd", "end display");
	}

	private void drawSubLcd( String txt,int FontSize,StaticLayout.Alignment mode) throws IOException{
		Paint mp = new Paint();

		mp.setTypeface(Typeface.MONOSPACE );
		TextPaint p = new TextPaint (mp);

		p.setAntiAlias(false);
		p.setTextSize(FontSize);
		p.setARGB(0xff, 0x0, 0x0, 0x0);


		String str =txt ;

		StaticLayout layout = new StaticLayout(str,
				p,lcd_width,mode,1.0F,0.0F,true);

		layout.draw(mCanvas);



		PrintBmp(00, mBitmap,0);


	}

	protected void onDestroy()
	{
		super.onDestroy();
		stop = true;
		//PrinterPowerOff();
		Log.e(TAG, "onDestroy"  );

	}
	int test22[ ] = { 0X00,0X01,0X87,0X00,0XF0,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X40,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X60,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XF0,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0XF8,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0XF8,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0F,
			0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X01,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X03,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X01,0XC0,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X01,0XC1,0XE0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X07,0X81,0XC1,0XE0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X01,
			0X80,0X00,0X00,0X00,0X0F,0XC9,0XE1,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X3F,
			0X83,0X80,0X00,0X00,0X00,0X18,0XE9,0XE0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X77,0XC3,0XA4,0X00,0X7C,0X7F,0X90,0X65,0XFF,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X68,0X67,0XB4,0XE9,0XBE,0X40,0X91,0X75,0XFF,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X77,0XEF,0X9C,0XFC,0XFF,0X7F,0X93,0X75,0XE7,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X01,0XFF,0XE9,0XBC,0X01,0XF3,0X5B,0X93,0X75,0XE3,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X03,0XBE,0X5F,0XBC,0XFC,0XFE,0X5C,0X97,0X74,0XFF,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X99,0XD9,0XBC,0XF5,0XE0,0X5C,0X9D,0X77,0XBE,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X1A,0XDF,0XBC,0XDD,0XEC,0XFC,0X9F,0X77,0X18,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1A,0XDF,0XBC,0XF5,0XFE,0X8C,0X1F,0X77,0XD8,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XDF,0XBC,0XB5,0XFA,0X9D,0XDF,0X76,
			0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,
			0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,
			0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,
			0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,
			0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,
			0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,
			0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1B,
			0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X1B,0XD7,0XBC,0XB5,0XFA,0X9D,0XD9,0X76,0X58,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X7F,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFE,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X84,0X84,0X40,0X40,0X13,0X12,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X40,0X04,0X20,0X20,0X13,
			0X02,0X10,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X08,0X00,0X40,0X00,
			0X20,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X18,0X80,0X03,0X00,
			0X06,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X08,0X80,0X1F,
			0XE0,0X0F,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X01,0XB0,
			0X36,0X20,0X15,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X39,
			0X80,0X0C,0XC0,0X03,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X09,0XF0,0X13,0X80,0X0E,0XF0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X09,0XB0,0X07,0X80,0X22,0X20,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X0B,0X20,0X18,0X70,0X1F,0XE0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X0D,0X20,0X08,0X40,0X0E,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X1A,0X20,0X08,0X40,0X12,0X60,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X04,0X60,0X0F,0XC0,0X22,0X20,0X7F,0XE6,0X07,0X1F,0XF8,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X7F,0XE6,0X07,0X1F,
			0XF8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X40,0X6E,0XF3,
			0X10,0X18,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X27,0XE8,0X00,0X00,0X5F,0X66,
			0X30,0X17,0XD8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X3F,0XED,0X00,0X00,0X5F,
			0X66,0X37,0X17,0XD8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0XB7,0X87,0X80,0X00,
			0X5F,0X66,0X37,0X17,0XD8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0XDF,0X87,0X80,
			0X00,0X5F,0X6C,0XDF,0X17,0XD8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0F,0XAB,0XE2,
			0XA0,0X00,0X4F,0X6E,0XDF,0X97,0XD8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0E,0X9F,
			0XDD,0X60,0X00,0X40,0X66,0XDB,0X90,0X18,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1A,
			0X1F,0XD2,0XF0,0X00,0X7F,0XED,0XB6,0X9F,0XF8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X1D,0X07,0XE7,0XF0,0X00,0X7F,0XEF,0XFC,0X9F,0XF8,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X1B,0X3E,0XF0,0XB8,0X00,0X00,0X07,0XF8,0X80,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X35,0XBF,0XD1,0X78,0X00,0X5B,0X67,0XBB,0X86,0X20,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X3A,0X9E,0X0D,0X78,0X00,0X3E,0XCD,0XF0,0X90,0X18,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X31,0X7F,0X14,0X78,0X00,0X3E,0XCD,0XF0,0XF0,0X18,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X3D,0XBF,0X29,0XF8,0X00,0X07,0X7E,0X36,0X7C,0XF8,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X1F,0X7F,0XFF,0XF8,0X00,0X43,0X1C,0X1E,0XD0,0X30,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X1F,0XFD,0XFF,0XF0,0X00,0X43,0XFC,0X7F,0X99,
			0X38,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0E,0X38,0XFE,0XF0,0X00,0X00,0XE0,0XFB,
			0X9D,0XB8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0E,0XF8,0XFC,0X60,0X00,0X3B,0X11,
			0XDE,0XF1,0X98,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X04,0XF0,0X60,0X70,0X00,0X7B,
			0XF3,0XDE,0XFD,0XF8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X04,0X60,0X00,0X60,0X00,
			0X43,0XE6,0X06,0X6C,0XF8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X02,0X00,0X00,0X40,
			0X00,0X3B,0X16,0X38,0XF1,0XA0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X02,0X00,0X00,
			0X40,0X00,0X7B,0X76,0XFE,0XFF,0XA0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X40,0X40,0X00,0X58,0X76,0XFE,0XFF,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X07,0X80,0X80,0X00,0X00,0X0D,0X83,0X87,0XF8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X01,0X00,0X00,0X80,0X00,0X7F,0XE0,0X1B,0X96,0X38,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X7F,0XE0,0X1B,0X96,0X38,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X1F,0XE0,0X00,0X00,0X40,0X66,0XF8,0X87,0XB8,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X87,0X80,0X00,0X00,0X5F,0X6C,0XF8,0XFE,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X01,0XC3,0X00,0X00,0X00,0X5F,0X6E,0XFB,0XFE,0XC0,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X01,0XE0,0X00,0X00,0X00,0X5F,0X66,0X3B,0X86,0XC0,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X01,0XF0,0X00,0X00,0X00,0X5F,0X6E,0X1E,0X6F,0X98,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0XF8,0X20,0X00,0X00,0X4F,0X6F,0XBE,0XEF,
			0XB8,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0F,0XFF,0XC0,0X00,0X00,0X40,0X67,0XBE,
			0XE7,0XA0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X3F,0XFF,0X84,0X00,0X00,0X7F,0XEE,
			0XFB,0XEC,0X38,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X3F,0XFF,0X86,0X00,0X00,0X7F,
			0XEE,0X7B,0XEC,0X38,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XFF,0XFF,0XFF,0XFF,0XFF,
			0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFE,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X01,0X02,0X08,0X82,0X00,0X05,0X0A,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X4A,0X4A,0X84,0X80,0X0F,0XCE,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X05,0X05,0X8E,0X8B,0X08,0X07,0X5F,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X05,0X43,0X0E,0XC6,0X00,0X07,0X4F,0X40,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X07,0X4F,0XCC,0X8C,0X00,0X07,0X4B,0XC0,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X06,0XC4,0X8C,0XC4,0X08,0X09,0X0E,0X40,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X04,0X80,0X04,0X00,0X01,0X02,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,
			0XFF,0XFF,0XFF,0XFF,0XFE,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X04,
			0X87,0X81,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X04,
			0X86,0X04,0X88,0X80,0X08,0XC4,0X4E,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X0E,0X0B,0X84,0X8A,0X88,0X18,0X55,0X22,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X02,0XC0,0X44,0X0E,0XC0,0X09,0X50,0XE0,0X40,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X02,0X00,0X04,0X8C,0XC0,0X3D,0XD4,0X40,0X40,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X0E,0X00,0X00,0X8C,0X48,0X08,0X4C,0X8E,0X40,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X03,0XC0,0X01,0X00,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X05,0X02,0X08,0X09,
			0X00,0X0F,0XCB,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0XCA,0XCA,
			0XCB,0X00,0X02,0X9F,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X05,0X47,
			0X0A,0X4B,0XC8,0X09,0X0B,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X05,
			0X47,0XCA,0X4B,0X00,0X0B,0X0B,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X05,0X47,0X8E,0X0F,0X08,0X09,0X9B,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X02,0X84,0X83,0XC7,0XC0,0X09,0X04,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X02,0X05,0X0A,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0F,0XC7,0X4F,0X42,0X00,0X09,
			0X48,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0X8F,0X87,0X44,0X88,
			0X01,0X49,0X20,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0X87,0X87,0X42,
			0X00,0X09,0X48,0XE0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0X84,0X8F,
			0X4F,0X80,0X01,0X48,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X8B,
			0X8B,0X42,0X08,0X1D,0X08,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X82,0X42,0X40,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFE,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X05,0X05,0X07,0XC7,0XC0,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0C,0X85,0X0B,0X08,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0XCC,0X83,0X08,0X08,0X00,0X00,
			0X60,0X02,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0D,0X43,0XCB,0X48,0X00,0X00,
			0X00,0X60,0X01,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X07,0X80,0X8B,0X48,0X08,
			0X00,0X0A,0X40,0X01,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0D,0X41,0X8B,0X48,
			0X80,0X00,0X7F,0XC0,0X02,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X01,0XAD,0XA0,0X06,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XFF,0XFF,0XFF,
			0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFE,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0XFB,0XC0,0X18,0XC0,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X03,0X6F,0X80,0X30,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XF1,0XE0,0X00,0X60,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X01,0XC3,0XC0,0X0C,0X30,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X03,0X99,0X8E,0X0C,0X30,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X05,0X05,0X02,0X07,0X80,0X01,0XFA,0XE0,0X18,0X60,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X04,0X85,0X02,0X07,0X80,0X01,0XBF,0X80,0X30,0X40,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X05,0XCF,0XC2,0X0F,0X88,0X03,0XF6,0X80,0X7C,0XE0,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0D,0X4B,0XC3,0X0A,0X80,0X06,0X40,0X80,0XC7,
			0X9C,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X06,0X80,0X84,0X8A,0X80,0X0C,0X00,0X81,
			0X03,0X06,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X05,0XC0,0X88,0XC6,0XC8,0X10,0X00,
			0X84,0X06,0X03,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X80,0X00,0X01,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X01,0X00,0X00,0X01,0X02,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X05,0X4D,0XC4,0X8F,0XCA,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X0B,0XC8,0X84,0X86,0X89,0X80,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X0B,0X8B,0XC2,0X05,0X83,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X0B,0X8A,0X82,0X02,0X0F,0XC0,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X0B,0X8A,0X83,0X87,0X02,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X0F,0XC0,0X0C,0X4F,0XC2,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,
			0XFF,0XFF,0XFF,0XFF,0XFE,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,0XFF,
			0XFF,0XFE,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
			0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,
	};

}
