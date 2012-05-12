package org.urish.banjii;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.urish.banjii.connector.CintieConnector;
import org.urish.banjii.model.PlayerManager;
import org.urish.banjii.server.UDPService;

import com.ardor3d.example.ExampleBase;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class BanjiiMain {
	private static File createTempDirectory() throws IOException {
		File temp = File.createTempFile("temp", String.valueOf(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return temp;
	}

	public static void loadEmbededLGWGL() throws IOException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		File tempDir = createTempDirectory();
		String[] dllNames = new String[] { "OpenAL64.dll", "OpenAL32.dll", "lwjgl64.dll", "lwjgl.dll" };

		for (String dllName : dllNames) {
			InputStream libStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(dllName);
			File tempFile = new File(tempDir, dllName);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			IOUtils.copy(libStream, outputStream);
			outputStream.close();
			libStream.close();
			tempFile.deleteOnExit();

		}

		System.setProperty("java.library.path", tempDir.getPath());
		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		fieldSysPath.set(null, null);
	}

	public static Server startJettyServer(int port) throws Exception {
		Server server = new Server(port);
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.addServlet(new ServletHolder(new ServletContainer(new PackagesResourceConfig(
				"org.urish.banjii.resources"))), "/api/*");
		server.setHandler(handler);
		server.start();
		return server;
	}

	public static void main(String[] args) throws Exception {
		loadEmbededLGWGL();
		new CintieConnector(PlayerManager.instance);
		new UDPService(1280).start();
		startJettyServer(1280);
		ExampleBase.start(Scene.class);
	}
}
