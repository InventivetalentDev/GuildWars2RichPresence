package org.inventivetalent.gw2rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// https://stackoverflow.com/questions/19005410/check-if-some-exe-program-is-running-on-the-windows
public class ProcessChecker {

	boolean isProcessRunning(String name) {
		String line;
		String pidInfo = "";

		try {
			Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				pidInfo += line;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return pidInfo.contains(name);
	}

}
