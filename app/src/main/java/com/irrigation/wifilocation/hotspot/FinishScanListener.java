package com.irrigation.wifilocation.hotspot;

import java.util.ArrayList;


public interface FinishScanListener {
	/**
	 * Interface called when the scan method finishes. Network operations should not execute on UI thread
	 * @param clients
	 */
	public void onFinishScan(ArrayList<ClientScanResult> clients);
}
