package org.inventivetalent.gw2rpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

// https://github.com/Drant/GW2Navi/blob/master/src/GW2Navi/GPS.java
public class MumbleLink extends Thread {

	// GPS data format
	private final int     MEM_MAP_SIZE = 5460;
	private final String  MEM_MAP_NAME = "MumbleLink";
	private final long    REFRESH_RATE = 2000;
	private final HANDLE  sharedFile;
	private final Pointer sharedMemory;

	int     uiVersion       = 0;
	int     uiTick          = 0;
	float[] fAvatarPosition = new float[0];
	float[] fAvatarFront    = new float[0];
	float[] fAvatarTop      = new float[0];
	float[] fCameraPosition = new float[0];
	float[] fCameraFront    = new float[0];
	float[] fCameraTop      = new float[0];
	char[]  identity        = new char[0];
	char[]  gameName        = new char[0];
	int     context_len     = 0;
	byte[]  context         = new byte[0];

	public boolean active = true;

	// Constructor
	public MumbleLink() {
		sharedFile = Kernel32.INSTANCE.CreateFileMapping(
				WinBase.INVALID_HANDLE_VALUE, null, WinNT.PAGE_EXECUTE_READWRITE, 0, MEM_MAP_SIZE, MEM_MAP_NAME);
		sharedMemory = Kernel32.INSTANCE.MapViewOfFile(
				sharedFile, WinNT.SECTION_MAP_READ, 0, 0, MEM_MAP_SIZE);
		System.out.println("sharedMemory: " + sharedMemory);
	}

	@Override
	public void run() {
		try {
			while (active && this.sharedMemory != null) {
				uiVersion = this.sharedMemory.getInt(0);
				uiTick = this.sharedMemory.getInt(4);
				fAvatarPosition = this.sharedMemory.getFloatArray(8, 3);
				fAvatarFront = this.sharedMemory.getFloatArray(20, 3);
				fAvatarTop = this.sharedMemory.getFloatArray(32, 3);
				gameName = this.sharedMemory.getCharArray(44, 256);
				fCameraPosition = this.sharedMemory.getFloatArray(556, 3);
				fCameraFront = this.sharedMemory.getFloatArray(568, 3);
				fCameraTop = this.sharedMemory.getFloatArray(580, 3);
				identity = this.sharedMemory.getCharArray(592, 256);
				context_len = this.sharedMemory.getInt(1104);
				context = this.sharedMemory.getByteArray(1108, 256);
//				System.out.println("uiVersion: " + uiVersion);
//				System.out.println("uiTick: " + uiTick);
//				System.out.println("fAvatarPosition: " + Arrays.toString(fAvatarPosition));
//				System.out.println("fAvatarFront: " + Arrays.toString(fAvatarFront));
//				System.out.println("fAvatarTop: " + Arrays.toString(fAvatarTop));
//				System.out.println("gameName: " + (new String(gameName)).trim());
//				System.out.println("fCameraPosition: " + Arrays.toString(fCameraPosition));
//				System.out.println("fCameraFront: " + Arrays.toString(fCameraFront));
//				System.out.println("fCameraTop: " + Arrays.toString(fCameraTop));
//				System.out.println("identity: " + (new String(identity)).trim());
//				System.out.println("context_len: " + context_len);
//				System.out.println("context: " + Arrays.toString(context));
//				System.out.println("#####################################################");

				Thread.sleep(REFRESH_RATE);
			}
		} catch (InterruptedException ex) {
			System.out.println("MumbleLinnk thread sleep error.");
		}
	}

	public String getIdentity() {
		return sanitizeIdentity(new String(identity)).trim();
	}

	public JsonObject getIdentityJson() {
		return new JsonParser().parse(getIdentity()).getAsJsonObject();
	}

	/**
	 * Retrieved JSON may sometimes be untrimmed, extracts only the outermost {} part.
	 *
	 * @param pString JSON to sanitize.
	 * @return sanitized JSON string.
	 */
	public String sanitizeIdentity(String pString) {
		String s = pString;
		int bgn = s.indexOf("{");
		int end = s.indexOf("}");
		if (bgn == -1 || end == -1) {
			return "null";
		}
		return s.substring(bgn, end + 1);
	}

	/**
	 * Calling this before terminating the program will prevent an access violation.
	 */
	public void kill() {
		active = false;
	}

}
