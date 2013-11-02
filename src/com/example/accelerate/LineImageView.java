package com.example.accelerate;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class LineImageView extends Activity {
	private static final String[] ACTIVITIES = { "其他", "走路", "跑步", "静坐", "上楼梯",
			"下楼梯", "乘车" };
	private static final String TAG = "sensor";
	private static final String DATA_FILENAME = "accelerate2.txt";
	private static final String ID_FILENAME = "userid.txt";
	private static final String USER_ID = getId();

	private static final int DELAY = SensorManager.SENSOR_DELAY_NORMAL;

	private long dataIndex = 0;// 该替换的数据的下标

	private static String user_activity = null;

	private String writeData = new String();
	private String head = new String();
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYSeries xSeries = new XYSeries("x");
	private XYSeries ySeries = new XYSeries("y");
	private XYSeries zSeries = new XYSeries("z");
	private XYSeriesRenderer xRenderer = new XYSeriesRenderer();// (类似于一条线对象)
	private XYSeriesRenderer yRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer zRenderer = new XYSeriesRenderer();

	private boolean STOPMEASURE = true;// 打开时默认关闭采集数据
	private boolean STOPAXIS = false;
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private GraphicalView view;
	private LinearLayout graphLayout;

	private SensorManager mSensorManager;
	private ShakeWakeupService mySerivece;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, ACTIVITIES);
		// 设置下拉列表风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		// 添加Spinner事件监听
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int groupId, long arg3) {
				user_activity = ACTIVITIES[groupId];
				arg0.setVisibility(View.VISIBLE);
				head = USER_ID + "," + groupId + ",";
				STOPMEASURE = true;// 设置时要暂停
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		final Button stopMeasureButton = (Button) this
				.findViewById(R.id.stopButton);
		stopMeasureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (user_activity == null) {// 选择了运动模式后才能开始采集数据
					Toast.makeText(LineImageView.this, "请您先选择运动模式!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				STOPMEASURE = !STOPMEASURE;
				if (STOPMEASURE) {
					stopMeasureButton.setText("继续");
					mSensorManager.unregisterListener(mySerivece);
				} else {
					stopMeasureButton.setText("暂停");
					Toast.makeText(LineImageView.this, user_activity,
							Toast.LENGTH_SHORT).show();
					mSensorManager.registerListener(mySerivece, mSensorManager
							.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), DELAY);

				}

			}
		});
		final Button stopAxisButton = (Button) this
				.findViewById(R.id.stopAxisButton);
		stopAxisButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				STOPAXIS = !STOPAXIS;
				if (STOPAXIS)
					stopAxisButton.setText("保持重力");
				else {
					stopAxisButton.setText("消除重力");
				}
			}
		});
		graphLayout = (LinearLayout) this.findViewById(R.id.graph);
		// 创建一个SensorManager来获取系统的传感器服务
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySerivece = new ShakeWakeupService();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);// 屏幕熄掉后依然运行
		registerReceiver(mySerivece.mReceiver, filter);
		lineView();
		graphLayout.addView(view);
	}

	// 折线图
	public void lineView() {
		mDataset.addSeries(xSeries);
		mDataset.addSeries(ySeries);
		mDataset.addSeries(zSeries);
		// 设置图表的X轴的当前方向
		mRenderer
				.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setXTitle("点");// 设置为X轴的标题
		mRenderer.setYTitle("加速度值");// 设置y轴的标题
		mRenderer.setAxisTitleTextSize(20);// 设置轴标题文本大小
		mRenderer.setChartTitle("加速度");// 设置图表标题
		mRenderer.setChartTitleTextSize(30);// 设置图表标题文字的大小
		mRenderer.setLabelsTextSize(18);// 设置标签的文字大小
		mRenderer.setLegendTextSize(20);// 设置图例文本大小
		mRenderer.setPointSize(1f);// 设置点的大小
		mRenderer.setYAxisMin(-15);// 设置y轴最小值是0
		mRenderer.setYAxisMax(15);
		mRenderer.setXAxisMax(60);
		mRenderer.setShowGrid(true);// 显示网格
		mRenderer.setMargins(new int[] { 20, 30, 15, 20 });// 设置视图位置

		xRenderer.setColor(Color.BLUE);// 设置颜色
		xRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		xRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		xRenderer.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(xRenderer);

		yRenderer.setColor(Color.GRAY);// 设置颜色
		yRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		yRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		yRenderer.setLineWidth(3);// 设置线宽

		mRenderer.addSeriesRenderer(yRenderer);

		zRenderer.setColor(Color.GREEN);// 设置颜色
		zRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		zRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		zRenderer.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(zRenderer);
		view = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer,
				0.2f);
		view.setBackgroundColor(Color.WHITE);
	}

	class ShakeWakeupService extends Service implements SensorEventListener {
		public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

		private SensorManager mSensorManager = null;
		private WakeLock mWakeLock = null;
		private float[] accValue;
		private float[] gravity;
		private float alpha = 0.8f;

		/*
		 * Register this as a sensor event listener.
		 */
		private void registerListener() {
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		/*
		 * Un-register this as a sensor event listener.
		 */
		private void unregisterListener() {
			if (mSensorManager != null)
				mSensorManager.unregisterListener(this);
		}

		public BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "onReceive(" + intent + ")");

				if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					return;
				}

				Runnable runnable = new Runnable() {
					public void run() {
						Log.i(TAG, "Runnable executing.");
						unregisterListener();
						registerListener();
					}
				};

				new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
			}
		};

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.i(TAG, "onAccuracyChanged().");
		}

		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// accelerometer = true;
				accValue = sensorEvent.values;
				StringBuilder builder = new StringBuilder();
				builder.append(head);
				builder.append(sensorEvent.timestamp);
				for (int i = 0; i < 3; i++) {
					builder.append(",");
					builder.append(accValue[i]);
				}
				Log.i(TAG, builder.toString());
				builder.append("\n");
				writeData += builder.toString();
				if (writeData.length() > 8000) {
					FileManger.getInstance().append(DATA_FILENAME, writeData);
					writeData = new String();
					Log.i(TAG, "写文件");
				}
				dataIndex++;
				if (STOPAXIS) {
					gravity[0] = alpha * gravity[0] + (1 - alpha)
							* sensorEvent.values[0];
					gravity[1] = alpha * gravity[1] + (1 - alpha)
							* sensorEvent.values[1];
					gravity[2] = alpha * gravity[2] + (1 - alpha)
							* sensorEvent.values[2];

					accValue[0] = sensorEvent.values[0] - gravity[0];
					accValue[1] = sensorEvent.values[1] - gravity[1];
					accValue[2] = sensorEvent.values[2] - gravity[2];
				}
				xSeries.add(dataIndex, accValue[0]);
				ySeries.add(dataIndex, accValue[1]);
				zSeries.add(dataIndex, accValue[2]);
				if (dataIndex > 500) {// 取消图中最多只保存500个
					xSeries.remove(0);
					ySeries.remove(0);
					zSeries.remove(0);
				}

				mRenderer.setXAxisMin(dataIndex - 55);
				mRenderer.setXAxisMax(dataIndex + 5);
				view.repaint();

			}

		}

		@Override
		public void onCreate() {
			super.onCreate();

			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

			PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = manager
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

			registerReceiver(mReceiver, new IntentFilter(
					Intent.ACTION_SCREEN_OFF));
		}

		@Override
		public void onDestroy() {
			unregisterReceiver(mReceiver);
			unregisterListener();
			mWakeLock.release();
			stopForeground(true);
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			super.onStartCommand(intent, flags, startId);

			startForeground(Process.myPid(), new Notification());
			registerListener();
			mWakeLock.acquire();
			return START_STICKY;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 设置菜单
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, 1, 1, R.string.action_settings);
		menu.add(0, 2, 2, R.string.action_settings);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() >= 1) {
			try {
				mSensorManager.unregisterListener(mySerivece);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			FileManger.getInstance().append(DATA_FILENAME, writeData);
			System.exit(0);
		}
		return super.onOptionsItemSelected(item);

	}

	private static String getId() {
		String id = FileManger.getInstance().readFile(ID_FILENAME);
		if (id == null) {
			Random r = new Random();
			Long idLong = r.nextLong();
			id = idLong.toString();
			FileManger.getInstance().saveFile(ID_FILENAME, id);
		}
		return id;
	}

}
