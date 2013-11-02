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

		// ����һ��SensorManager����ȡϵͳ�Ĵ���������
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// ѡȡ���ٶȸ�Ӧ��
		int sensorType = Sensor.TYPE_ACCELEROMETER;
		sm.registerListener(myAccelerometerListener,
				sm.getDefaultSensor(sensorType),
				SensorManager.SENSOR_DELAY_NORMAL);		
	}

	final SensorEventListener myAccelerometerListener = new SensorEventListener() {

		// ��дonSensorChanged����
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
					Log.i(TAG, "д�ļ�");
				}
				//Log.i(TAG, "onSensorChanged");
				// ͼ�����Ѿ���������ֵ�ĺ���
				// Log.i(TAG, "\n heading " + X_lateral);
				// Log.i(TAG, "\n pitch " + Y_longitudinal);
				// Log.i(TAG, "\n roll " + Z_vertical);
			}
		}

		// ��дonAccuracyChanged����
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
		 * �ܹؼ��Ĳ��֣�ע�⣬˵���ĵ����ᵽ����ʹactivity���ɼ���ʱ�򣬸�Ӧ����Ȼ������Ĺ��������Ե�ʱ����Է��֣�û��������ˢ��Ƶ��
		 * Ҳ��ǳ��ߣ�����һ��Ҫ��onPause�����йرմ����������򽲺ķ��û������������ܲ�����
		 */
		sm.unregisterListener(myAccelerometerListener);
		super.onPause();
	}

}
