package com.example.gs.voicetest;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;

public final class FileUtils {

	private static final String TAG = FileUtils.class.getSimpleName();
	private static FileUtils fileUtils = new FileUtils();
	public interface FileUtilsCallBack{
		void callback();
	}
	private FileUtils() {
	}

	public static FileUtils getInstance() {
		return fileUtils;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//建立一个MIME类型与文件后缀名的匹配表
	public static final String[][] MIME_MapTable={
			//{后缀名，    MIME类型}
			{".3gp",    "video/3gpp"},
			{".apk",    "application/vnd.android.package-archive"},
			{".asf",    "video/x-ms-asf"},
			{".avi",    "video/x-msvideo"},
			{".bin",    "application/octet-stream"},
			{".bmp",      "image/bmp"},
			{".c",        "text/plain"},
			{".class",    "application/octet-stream"},
			{".conf",    "text/plain"},
			{".cpp",    "text/plain"},
			{".doc",    "application/msword"},
			{".exe",    "application/octet-stream"},
			{".gif",    "image/gif"},
			{".gtar",    "application/x-gtar"},
			{".gz",        "application/x-gzip"},
			{".h",        "text/plain"},
			{".htm",    "text/html"},
			{".html",    "text/html"},
			{".jar",    "application/java-archive"},
			{".java",    "text/plain"},
			{".jpeg",    "image/jpeg"},
			{".jpg",    "image/jpeg"},
			{".js",        "application/x-javascript"},
			{".log",    "text/plain"},
			{".m3u",    "audio/x-mpegurl"},
			{".m4a",    "audio/mp4a-latm"},
			{".m4b",    "audio/mp4a-latm"},
			{".m4p",    "audio/mp4a-latm"},
			{".m4u",    "video/vnd.mpegurl"},
			{".m4v",    "video/x-m4v"},
			{".mov",    "video/quicktime"},
			{".mp2",    "audio/x-mpeg"},
			{".mp3",    "audio/x-mpeg"},
			{".mp4",    "video/mp4"},
			{".mpc",    "application/vnd.mpohun.certificate"},
			{".mpe",    "video/mpeg"},
			{".mpeg",    "video/mpeg"},
			{".mpg",    "video/mpeg"},
			{".mpg4",    "video/mp4"},
			{".mpga",    "audio/mpeg"},
			{".msg",    "application/vnd.ms-outlook"},
			{".ogg",    "audio/ogg"},
			{".pdf",    "application/pdf"},
			{".png",    "image/png"},
			{".pps",    "application/vnd.ms-powerpoint"},
			{".ppt",    "application/vnd.ms-powerpoint"},
			{".prop",    "text/plain"},
			{".rar",    "application/x-rar-compressed"},
			{".rc",        "text/plain"},
			{".rmvb",    "audio/x-pn-realaudio"},
			{".rtf",    "application/rtf"},
			{".sh",        "text/plain"},
			{".tar",    "application/x-tar"},
			{".tgz",    "application/x-compressed"},
			{".txt",    "text/plain"},
			{".wav",    "audio/x-wav"},
			{".wma",    "audio/x-ms-wma"},
			{".wmv",    "audio/x-ms-wmv"},
			{".wps",    "application/vnd.ms-works"},
			//{".xml",    "text/xml"},
			{".xml",    "text/plain"},
			{".z",        "application/x-compress"},
			{".zip",    "application/zip"},
			{"",        "*/*"}
	};
	public static void write(Context context, String fileName, String content)
	{
		if( content == null )	content = "";

		try
		{
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write( content.getBytes() );

			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// 删除 /data/data/PACKAGE_NAME/files 目录下 文件
	public static Boolean delete_(String filePath) {
		if (!filePath.equals("")) {
			File file = new File(filePath);
			if (file.isFile()) {
				try {
					file.delete();
					return true;
				} catch (SecurityException se) {
					se.printStackTrace();
					return false;
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * 读取文本文件
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String read(Context context, String fileName )
	{
		try
		{
			FileInputStream in = context.openFileInput(fileName);
			return readInStream(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}

	private static String readInStream(FileInputStream inStream)
	{
		try
		{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int length = -1;
			while((length = inStream.read(buffer)) != -1 )
			{
				outStream.write(buffer, 0, length);
			}

			outStream.close();
			inStream.close();
			return outStream.toString();
		}
		catch (IOException e)
		{
			Log.i("FileTest", e.getMessage());
		}
		return null;
	}

	public static File createFile(String folderPath, String fileName )
	{
		File destDir = new File(folderPath);
		if (!destDir.exists())
		{
			destDir.mkdirs();
		}
		return new File(folderPath,  fileName + fileName );
	}

	/**
	 * 向手机写图片
	 * @param buffer
	 * @param folder
	 * @param fileName
	 * @return
	 */
	public static boolean writeFile(byte[] buffer, String folder, String fileName )
	{
		boolean writeSucc = false;

		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

		String folderPath = "";
		if( sdCardExist )
		{
			folderPath = Environment.getExternalStorageDirectory() + File.separator +  folder + File.separator;
		}
		else
		{
			writeSucc =false;
		}

		File fileDir = new File(folderPath);
		if(!fileDir.exists())
		{
			fileDir.mkdirs();
		}

		File file = new File( folderPath + fileName );
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream( file );
			out.write(buffer);
			writeSucc = true;
		}
		catch (Exception e)
		{
//			Toast.makeText(WoApplication.getContext(),"没有获取到地址", Toast.LENGTH_SHORT).show();
//			Config.Log(TAG," *** 没有获取到地址");
			e.printStackTrace();
		}
		finally
		{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return writeSucc;
	}

	public static boolean writeFile( byte[] buffer, String path )
	{
		boolean writeSucc = false;


		File file = new File( path );
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream( file );
			out.write(buffer);
			writeSucc = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return writeSucc;
	}

	public static boolean writeFile(byte[] buffer, String path , FileUtilsCallBack callBack)
	{
		boolean writeSucc = false;


		File file = new File( path );
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream( file );
			out.write(buffer);
			writeSucc = true;
		}
		catch (Exception e)
		{
			callBack.callback();
			e.printStackTrace();
		}
		finally
		{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return writeSucc;
	}

	public static String getFileName1(String filePath) throws Exception {
		if (TextUtils.isEmpty(filePath)) {
			return "";
		}
		String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1,
				filePath.length());
//		if (Config.DEBUG) {
//			Config.Log("chenchaozheng", "FileUtils getFileName " + fileName);
//		}
		return fileName;
	}
	public String getFileName(String filePath) throws Exception {
		if (TextUtils.isEmpty(filePath)) {
			return "";
		}
		String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1,
				filePath.length());
//		if (Config.DEBUG) {
//			Config.Log("chenchaozheng", "FileUtils getFileName " + fileName);
//		}
		return fileName;
	}
	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath){
		if(TextUtils.isEmpty(filePath)){
			return "";
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator)+1,point);
	}

	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName )
	{
		if( TextUtils.isEmpty(fileName) )	return "";

		int point = fileName.lastIndexOf( '.' );
		return fileName.substring(point).toLowerCase();
	}

	public static String getFileType(String format) {
		String type="*/*";
		if(TextUtils.isEmpty(format)) return type;
		for(int i=0;i<MIME_MapTable.length;i++){
			if(format.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	/**
	 * 获取文件大小
	 * @param filePath
	 * @return
	 */
	public static long getFileSize( String filePath )
	{
		long size = 0;

		File file = new File( filePath );
		if(file!=null && file.exists())
		{
			size = file.length();
		}
		return size;
	}

	/**
	 * 获取文件大小
	 * @param size 字节
	 * @return
	 */
	public static String getFileSize(long size)
	{
		if (size <= 0)	return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float)size / 1024;
		if (temp >= 1024)
		{
			return df.format(temp / 1024) + "M";
		}
		else
		{
			return df.format(temp) + "K";
		}
	}

	/**
	 * 获取文件MD5
	 * @param file
	 * @return
	 */
	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	/**
	 * 转换文件大小
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public String formatFileSize(long fileS) {
		DecimalFormat format = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = format.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = format.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = format.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = format.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取目录文件大小
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); //递归调用继续统计
			}
		}
		return dirSize;
	}

	public long getFileList(File dir){
		long count = 0;
		File[] files = dir.listFiles();
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);//递归
				count--;
			}
		}
		return count;
	}

	public static byte[] toBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) != -1)
		{
			out.write(ch);
		}
		byte buffer[]=out.toByteArray();
		out.close();
		return buffer;
	}

	/**
	 * 检查sdcard文件是否存在
	 * @param name
	 * @return
	 */
	public static boolean checkFileExists(String name) {
		boolean status;
		if (!name.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + name);
			status = newPath.exists();
		} else {
			status = false;
		}
		return status;
	}

	public static boolean checkFileExistsInSdcard(File file) {
		return file.exists();
	}

	/**
	 * 检查sdcard是否存在，true为存在
	 * @return
	 */
	public static boolean checkSdcardIsPlugIn() {
		if(!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			return false;
		}
		return true;
	}

	/**
	 * 计算SD卡的剩余空间
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace() {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks * blockSize / 1024;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return (freeSpace);
	}

	/**
	 * 新建目录
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		boolean status;
		if (!directoryName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + directoryName);
			status = newPath.mkdir();
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 检查是否安装SD卡
	 * @return
	 */
	public static boolean checkSaveLocationExists() {
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除目录(包括：目录里的所有文件)
	 * @param fileName
	 * @return
	 */
	public static boolean deleteDirectory(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isDirectory()) {
				String[] listfile = newPath.list();
				// delete all files within the specified directory and then
				// delete the directory
				try {
					for (int i = 0; i < listfile.length; i++) {
						File deletedFile = new File(newPath.toString() + "/"
								+ listfile[i].toString());
						deletedFile.delete();
					}
					newPath.delete();
//					Log.i("DirectoryManager deleteDirectory", fileName);
					status = true;
				} catch (Exception e) {
					e.printStackTrace();
					status = false;
				}

			} else
				status = false;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除文件
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()) {
				try {
//					Log.i("DirectoryManager deleteFile", fileName);
					newPath.delete();
					status = true;
				} catch (SecurityException se) {
					se.printStackTrace();
					status = false;
				}
			} else
				status = false;
		} else
			status = false;
		return status;
	}

	/**
	 * 检查sdcard是否装载
	 * @return
	 */
	public static boolean checkSDCard() {

		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) ? true : false;
	}

	/**
	 * 获取sdcard路径
	 * @return
	 */
	public static String getSDCardRoot() {
		if (checkSDCard()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();

		} else {

			return null;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////



	/**
	 * 获取SDCARD根路径
	 *
	 * @return
	 */
	public StringBuffer getRootDir() throws Exception {
		return new StringBuffer().append(Environment
				.getExternalStorageDirectory());
	}

	public StringBuffer getExternalFileAbsoluteDir(Context mContext,
                                                   String folderName, String fileName) throws Exception {
		StringBuffer stringBuffer = getRootDir().append(File.separator);
		stringBuffer.append(getExternalFilesDir(mContext, folderName));
		if (fileName != null) {
			if (0 == fileName.indexOf(File.separator)) {
				stringBuffer.append(fileName);
			} else {
				stringBuffer.append(File.separator);
				stringBuffer.append(fileName);
			}
		}
//		if (Config.DEBUG)
//			Config.Log("chenchaozheng", "FileUtils getExternalFileAbsoluteDir " + stringBuffer.toString());
		return stringBuffer;
	}

	public File createExternalFile(Context mContext, String folderName,
                                   String fileName) throws Exception {
		File file = new File(getExternalFileAbsoluteDir(mContext, folderName,
				fileName).toString());
		if (!file.exists())
			createExternalFilesDir(getExternalFilesDir(mContext, folderName));
		return file;
	}

	/**
	 * 获取SDCARD上应用存储路径
	 *
	 * @param mContext
	 * @param folderName
	 * @return
	 */
	private StringBuffer getExternalFilesDir(Context mContext, String folderName)
			throws Exception {
		String packageName = mContext.getPackageName();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Android").append(File.separator).append("data")
				.append(File.separator).append(packageName);
		if (folderName != null) {
			if (0 == folderName.indexOf(File.separator)) {
				stringBuffer.append(folderName);
			} else {
				stringBuffer.append(File.separator);
				stringBuffer.append(folderName);
			}
		}
//		if (Config.DEBUG)
//			Config.Log("chenchaozheng", "FileUtils getExternalFilesDir " + stringBuffer.toString());
		return stringBuffer;
	}

	public String createExternalFilesDir(StringBuffer stringBuffer)
			throws Exception {
		String[] paths = stringBuffer.toString().split(File.separator);
		stringBuffer.delete(0, stringBuffer.length());
		File file = null;
		for (String path : paths) {
			stringBuffer.append(path).append(File.separator);
			file = new File(getRootDir().append(File.separator)
					.append(stringBuffer).toString());
			if (!file.exists()) {
				file.mkdir();
//				if (Config.DEBUG)
//					Config.Log("chenchaozheng", "FileUtils createExternalFilesDir create Dir "
//							+ file.getAbsolutePath());
			}
		}
		return file.getAbsolutePath();
	}

	public boolean isExternalFileExist(Context mContext, String folderName,
                                       String fileName) throws Exception {
		File file = new File(getExternalFileAbsoluteDir(mContext, folderName,
				fileName).toString());
		if (file.exists())
			return true;
		else
			return false;
	}

	public void deleteExternalFile(Context mContext, String folderName,
                                   String fileName) throws Exception {
		String fileDir = getExternalFileAbsoluteDir(mContext, folderName,
				fileName).toString();
		File file = new File(fileDir);
		if (file.exists()) {
			file.delete();
//			if (Config.DEBUG)
//				Config.Log("chenchaozheng", "FileUtils deleteExternalFile " + fileDir);
		}
	}

	public void deleteExternalFilesDir(Context mContext, String folderName)
			throws Exception {
		String folderDir = getExternalFilesDir(mContext, folderName).toString();
		File externalFilesDir = new File(folderDir);
		deleteExternalFilesDir(externalFilesDir);
//		if (Config.DEBUG)
//			Config.Log("chenchaozheng", "FileUtils deleteExternalFilesDir " + folderDir);
	}

	public void deleteExternalFilesDir(File externalFilesDir) throws Exception {
		if (externalFilesDir.isDirectory()) {
			File[] files = externalFilesDir.listFiles();
			for (File file : files) {
				deleteExternalFilesDir(file);
			}
			externalFilesDir.delete();
		} else {
			externalFilesDir.delete();
		}
	}

	public File writeToExternalFilesDir(Context mContext, String folderName,
                                        String fileName, byte[] bytes) throws Exception {
		OutputStream os = null;
		String fileDir = getExternalFileAbsoluteDir(mContext, folderName,
				fileName).toString();
		File file = new File(fileDir);
		try {
			if (!file.exists())
				createExternalFilesDir(getExternalFilesDir(mContext, folderName));
			os = new FileOutputStream(file);
			os.write(bytes);
			os.flush();
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public File writeToExternalFilesDir(Context mContext, String folderName,
                                        String fileName, InputStream is) throws Exception {
		OutputStream os = null;
		String fileDir = getExternalFileAbsoluteDir(mContext, folderName,
				fileName).toString();
//		if (Config.DEBUG)
//			Config.Log("chenchaozheng", "FileUtils writeToExternalFilesDir " + fileDir);
		File file = new File(fileDir);
		try {
			if (!file.exists())
				createExternalFilesDir(getExternalFilesDir(mContext, folderName));
			os = new FileOutputStream(file);
			byte[] buffer = new byte[4 * 1024];
			while (is.read(buffer) != -1) {
				os.write(buffer);
			}
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}


	private long getFileSize(File file) {
		long size = 0;
		File flist[] = file.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	public String getFileSize(Context mContext) {
		StringBuffer stringBuffer;
		try {
			stringBuffer = FileUtils.getInstance().getRootDir()
					.append(File.separator);
			String packageName = mContext.getPackageName();
			stringBuffer.append("Android").append(File.separator).append("data")
					.append(File.separator).append(packageName);
			File file = new File(stringBuffer.toString());
			return formatFileSize(getFileSize(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}

	public static void writeStringToFile(String data, File file) {
		OutputStream output = null;
		try {
			if(file.exists()) {
				file.delete();
			}
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			output = new BufferedOutputStream(new FileOutputStream(file, false));
			byte bytes[] = data.getBytes("UTF-8");
			output.write(bytes, 0, bytes.length);
			output.close();
			output.flush();
			output = null;
		} catch (Exception e) {
		} finally {
			closeQuietly(output);
		}
	}

	public static String readStringFromFile(File file) {
		String result = "";
		byte[] data = null;
		InputStream input = null;
		try {
			if(file.exists() &&
					file.isFile()) {
				input = new FileInputStream(file);
				data = convertInputStreamToBytes(input);
			}
			if(data != null) {
				result = new String(data, "UTF-8");
			}
		} catch (Exception e) {
		} finally {
			closeQuietly(input);
		}
		return result;
	}

	private static final int BUFFER_SIZE = 8192;

	public static byte[] convertInputStreamToBytes(InputStream in) {
		byte[] data = null;
		if(in == null)
			return data;
		ByteArrayOutputStream out = null;
		try {
			byte buffer[] = new byte[BUFFER_SIZE];
			int n;
			out = new ByteArrayOutputStream();
			while ((n = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
				out.write(buffer, 0, n);
			}
			data = out.toByteArray();
		} catch(Exception e) {
		} finally {
			closeQuietly(out);
		}
		return data;
	}

	public static void closeQuietly(Closeable res) {
		if(res == null) {
			return;
		}
		try {
			res.close();
		} catch (Exception e) {
		}
	}

	public static String concatPath(String head, String tail) {
		StringBuilder builder = new StringBuilder();
		builder.append(head);
		if(!head.endsWith(File.separator)) {
			builder.append(File.separator);
		}
		builder.append(tail);
		return builder.toString();
	}

}
