package com.example.accelerate;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class Accelerator extends Activity {
	private static final String TAG = "sensor";
	private static final int INTERVAL = 200;
	private SensorManager sm;
	private Long lastTime = System.currentTimeMillis();
	private static String writeData = new String();
	private TextView xValueTV;
	private TextView yValueTV;
	private TextView zValueTV;
	private Float lastx = 9.8f;
	private Float lasty = 9.8f;
	private Float lastz = 9.8f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		xValueTV = (TextView) this.findViewById(R.id.xValue);
//		yValueTV = (TextView) this.findViewById(R.id.yValue);
//		zValueTV = (TextView) this.findViewById(R.id.zValue);

		// 创建一个SensorManager来获取系统的传感器服务
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 选取加速度感应器
		int sensorType = Sensor.TYPE_ACCELEROMETER;
		sm.registerListener(myAccelerometerListener,
				sm.getDefaultSensor(sensorType),
				SensorManager.SENSOR_DELAY_NORMAL);		
	}

	final SensorEventListener myAccelerometerListener = new SensorEventListener() {

		// 复写onSensorChanged方法
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER
					&& System.currentTimeMillis() - lastTime > INTERVAL) {
				lastTime = System.currentTimeMillis();
				Float X_lateral = sensorEvent.values[0];
				Float Y_longitudinal = sensorEvent.values[1];
				Float Z_vertical = sensorEvent.values[2];
				if (Math.abs(X_lateral - lastx) > 0.2) {
					lastx = X_lateral;
					xValueTV.setText(X_lateral.toString());
				}

				if (Math.abs(Y_longitudinal - lasty) > 0.2) {
					lasty = Y_longitudinal;
					yValueTV.setText(Y_longitudinal.toString());
				}
				if (Math.abs(Z_vertical - lastz) > 0.2) {
					lastz = Z_vertical;
					zValueTV.setText(Z_vertical.toString());
				}
				StringBuilder builder = new StringBuilder();
				builder.append(lastTime);
				builder.append(",");
				builder.append(X_lateral);
				builder.append(",");
				builder.append(Y_longitudinal);
				builder.append(",");
				builder.append(Z_vertical);
				builder.append("\n");
				writeData += builder.toString();
				if (writeData.length() > 1024) {
					FileManger.getInstance()
							.append("accelerate.txt", writeData);
					writeData = new String();
					Log.i(TAG, "写文件");
				}
				//Log.i(TAG, "onSensorChanged");
				// 图解中已经解释三个值的含义
				// Log.i(TAG, "\n heading " + X_lateral);
				// Log.i(TAG, "\n pitch " + Y_longitudinal);
				// Log.i(TAG, "\n roll " + Z_vertical);
			}
		}

		// 复写onAccuracyChanged方法
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.i(TAG, "onAccuracyChanged");
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onPause() {
		/*
		 * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
		 * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
		 */
		sm.unregisterListener(myAccelerometerListener);
		super.onPause();
	}

}
