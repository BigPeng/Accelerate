package com.example.accelerate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.os.Environment;

public class FileManger {
	private static final String PATH = "Accelerate";
	private static FileManger instance = null;

	private FileManger() {
	}

	public static FileManger getInstance() {
		if (instance == null)
			instance = new FileManger();
		return instance;
	}

	private String getSaveFilePath() {
		String basePath;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			basePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		else
			basePath = Environment.getDataDirectory().getAbsolutePath();
		String savePath = new File(basePath, PATH).getAbsolutePath();
		createDir(savePath);
		return savePath;
	}

	private void createDir(String path) {
		File file = new File(path);
		if (file.exists())
			return;
		file.mkdirs();
	}

	public boolean saveFile(String fileName, String text) {
		String path = getSaveFilePath();
		File file = new File(path, fileName);
		try {
			PrintWriter out = new PrintWriter(file);
			out.print(text);
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}

	public boolean append(String fileName, String data) {
		String path = getSaveFilePath();
		File file = new File(path, fileName);
		try {
			FileOutputStream fileOutStream = new FileOutputStream(file, true);// true表示追加
			byte[] buff = data.getBytes();
			fileOutStream.write(buff);
			fileOutStream.flush();
			fileOutStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 读取文件
	 * 
	 * @param fileName
	 * @return文件内容，当文件不存在时返回null
	 * @throws IOException
	 */
	public String readFile(String fileName) {
		String path = getSaveFilePath();
		File file = new File(path, fileName);
		if (!file.exists())
			return null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = in.readLine()) != null) {
				builder.append(line);
			}
			in.close();
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "0000000S";
		}

	}
}
