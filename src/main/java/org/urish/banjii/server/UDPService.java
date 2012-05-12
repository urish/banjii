package org.urish.banjii.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.urish.banjii.model.CameraManager;

public class UDPService implements Runnable {
	private static final Logger logger = Logger.getLogger(UDPService.class.getName());

	private boolean terminated = false;
	private final CameraManager cameraManager = CameraManager.instance;
	private final DatagramSocket socket;

	public UDPService(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}

	public void run() {
		while (!terminated) {
			byte[] packetData = new byte[1576];
			DatagramPacket receivePacket = new DatagramPacket(packetData, packetData.length);
			try {
				socket.receive(receivePacket);
				byte[] incomingData = new byte[receivePacket.getLength()];
				System.arraycopy(packetData, receivePacket.getOffset(), incomingData, 0, receivePacket.getLength());
				String formData = new String(incomingData);
				processMessage(formData);
			} catch (IOException e) {
				logger.severe("UDP recv() failed: " + e.getMessage());
			}
		}
	}

	private void processMessage(String formData) {
		try {
			Map<String, String> decoded = decode(formData);
			int cameraId = Integer.valueOf(decoded.get("camera"));
			if (decoded.containsKey("marker")) {
				int markerId = Integer.valueOf(decoded.get("marker"));
				String[] matrixElements = decoded.get("matrix").split(",");
				double[] matrix = new double[matrixElements.length];
				for (int i = 0; i < matrix.length; i++) {
					matrix[i] = Double.valueOf(matrixElements[i]);
				}
				cameraManager.onMarkerMovement(cameraId, markerId, matrix);
			} else {
				cameraManager.updateCameraConnection(cameraId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Failed to process client message: <<" + formData + ">>, exception: " + e);
		}
	}

	private Map<String, String> decode(String formData) throws UnsupportedEncodingException {
		Map<String, String> result = new HashMap<String, String>();
		for (String element : formData.split("&")) {
			String[] parts = element.split("=", 2);
			result.put(URLDecoder.decode(parts[0], "utf-8"), URLDecoder.decode(parts[1], "utf-8"));
		}
		return result;
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
}
