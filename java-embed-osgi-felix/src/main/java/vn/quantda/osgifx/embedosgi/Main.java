package vn.quantda.osgifx.embedosgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import vn.quantda.embedfelix.service.ViewService;

public class Main extends Application {

	private static final int CUSTOM_BUNDLE_LEVEL = 60;
	private static final int SYSTEM_BUNDLE_LEVEL = 10;
	private static final String SYSTEM_BUNDLES_DIR = "system-bundles";
	private static final String CUSTOM_BUNDLES_DIR = "bundles";
	private static final String HOST_LIBS_DIR = "libs";
	private static final String HOST_FRAMEWORD_DIR = "framework";
	private static Framework framework = null;
	private static Map<String, String> configMap;
	
	private ViewManager viewManager;

	public static void main(String[] args) throws InvalidSyntaxException {
		addShutdownHook();
		framework = createFramework();
		BundleContext bundleContext = framework.getBundleContext();
		ServiceReference<?>[] refs = (ServiceReference<?>[]) bundleContext.getServiceReferences(ViewService.class.getName(), null);
		for(int i=0; i < refs.length; i++ ) {
			System.out.println("Service Ref: " + refs[i].toString() + " - Service: " + bundleContext.getService(refs[i]));
			ViewService view = (ViewService) bundleContext.getService(refs[i]);
			System.out.println("Service:" + view.getName());
		}
		Bundle[] bundles = bundleContext.getBundles();
		for(int i = 0; i < bundles.length ; i++ ) {
			Bundle bundle = bundles[i];
			System.out.println("Bundle: " + bundle.getSymbolicName() + " - State: " + bundle.getState());
		}
		System.out.println("Felix fileinstall: " + bundleContext.getProperty("felix.fileinstall.dir").toString());
		launch(args);
	}

	private static Framework createFramework() {
		try {
			addLibsClasspath();
			buildConfigMap();
			exportHostPackagesOsgi();
			framework = initializeFramework();
			addSystemBundles();
			addCustomBundles();
		} catch (Exception e) {
			System.err.println("Error starting framework: " + e);
			e.printStackTrace();
		}
		return framework;
	}

	private static void buildConfigMap() {
		System.setProperty("felix.fileinstall.noInitialDelay", "true");
	    System.setProperty("felix.fileinstall.poll", "1000");
	    
		configMap = new HashMap<>();
		configMap = System.getProperties().entrySet().stream()
				.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));

		configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		// Read startup properties
		Properties prop = new Properties();
		InputStream input = null;
		try {
			File etcDir = new File("etc");
			if (! etcDir.exists()) {
				etcDir.mkdir();
			}
			input = new FileInputStream("etc/startup.properties");

			// load a properties file
			prop.load(input);
			
			// get the property value and print it out
			configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, prop.getProperty("javafx-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			File autoDeployDir = new File("deploy");
			if (! autoDeployDir.exists()) {
				autoDeployDir.mkdir();
			}
			/*System.setProperty("felix.fileinstall.dir", autoDeployDir.toURI().toURL().toString());
			configMap.put("felix.fileinstall.dir", autoDeployDir.toURI().toURL().toString());*/
			//System.setProperty("felix.fileinstall.dir", System.getProperty("user.home") + File.separator + "osgi-deploydir");
			configMap.put("felix.fileinstall.dir", autoDeployDir.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("Cannot init auto-deploy folder");
		}
		
	}
	private static void exportHostPackagesOsgi() throws Exception {
		exportHostPackagesOsgi(HOST_FRAMEWORD_DIR);
		exportHostPackagesOsgi(HOST_LIBS_DIR);
		
	}

	private static void exportHostPackagesOsgi(String path) throws Exception {
		File hostLibsDir = new File(path);
		if (hostLibsDir == null || ! hostLibsDir.exists()) {
			return;
		}
		File[] files = hostLibsDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if (file.getName().toLowerCase().endsWith(".jar")) {
					return true;
				}
				return false;
			}
		});
		Arrays.sort(files);
		if (files.length == 0) {
			return;
		}
		List<String> exportPackageNames = new ArrayList<>();
		for (int i=0; i < files.length; i++ ) {
			JarFile jar = new JarFile(files[i]);
			Manifest mf = jar.getManifest();
			Attributes jarAttributes = mf.getMainAttributes();
			String bundleSymbolicName = jarAttributes.getValue("Bundle-SymbolicName");
			String bundleExportPackageAttribute = jarAttributes.getValue("Export-Package");
			if (bundleSymbolicName != null && bundleExportPackageAttribute != null && ! bundleExportPackageAttribute.isEmpty() ) {
				exportPackageNames.add(bundleExportPackageAttribute);
			}
			jar.close();
		}
		if ( ! exportPackageNames.isEmpty() ) {
			String exportPackageCombined = exportPackageNames.stream().map(x -> x).collect(Collectors.joining(","));
			String oldExportPackageCombined = configMap.get(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA);
			if (oldExportPackageCombined != null) {
				exportPackageCombined = oldExportPackageCombined + "," + exportPackageCombined;
			} 
			configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exportPackageCombined);
		}
		
		
	}

	private static void addCustomBundles() throws Exception {
		addBundles(CUSTOM_BUNDLES_DIR, CUSTOM_BUNDLE_LEVEL);
	}

	private static void addSystemBundles() throws Exception {
		addBundles(SYSTEM_BUNDLES_DIR, SYSTEM_BUNDLE_LEVEL);
	}

	private static void addBundles(String bundleDirPath, int bundleStartLevel) throws Exception {
		// Create, configure, and start an OSGi framework instance
		// using the ServiceLoader to get a factory.
		File bundleDir = new File(bundleDirPath);
		if (!bundleDir.exists()) {
			bundleDir.mkdir();
		}
		File[] files = bundleDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.getName().toLowerCase().endsWith(".jar")) {
					return true;
				}
				return false;
			}
		});
		Arrays.sort(files);
		List<File> jars = new ArrayList<File>();
		jars.addAll(Arrays.asList(files));
		if (framework != null && framework.getState() == Bundle.ACTIVE && !jars.isEmpty()) {
			List<Bundle> bundleList = new ArrayList<>();
			BundleContext bundleContext = framework.getBundleContext();
			for (int i = 0; i < jars.size(); i++) {
				File jar = jars.get(i);
				Bundle bundle = bundleContext.installBundle(jar.toURI().toURL().toString());
				//bundle.adapt(BundleStartLevel.class).setStartLevel(bundleStartLevel);
				bundleList.add(bundle);
			}
			for (int i = 0; i < bundleList.size(); i++) {
				((Bundle) bundleList.get(i)).start();
			}
		}
	}

	private static Framework initializeFramework() throws Exception {
		framework = getFrameworkFactory().newFramework(configMap);
		framework.init();
		BundleListener listener = new BundleListenerImpl();
		framework.getBundleContext().addBundleListener(listener);
		framework.start();
		//FrameworkStartLevel sl = framework.adapt(FrameworkStartLevel.class);
        //sl.setInitialBundleStartLevel(CUSTOM_BUNDLE_LEVEL);
		return framework;

	}

	private static FrameworkFactory getFrameworkFactory() throws Exception {
		URL url = Main.class.getClassLoader()
				.getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if (url != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();
					if ((s.length() > 0) && (s.charAt(0) != '#')) {
						return (FrameworkFactory) Class.forName(s).newInstance();
					}
				}
			} finally {

			}
		}
		throw new Exception("Could not find framework factory");
	}
	
	private static void addLibsClasspath() throws Exception {
		addLibsClasspath(HOST_FRAMEWORD_DIR);
		addLibsClasspath(HOST_LIBS_DIR);
	}

	private static void addLibsClasspath(String path) throws Exception {
		File f = new File(path);
		if (f != null && f.exists()) {
			URL url = f.toURI().toURL();
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class<URLClassLoader> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[] { url });
		}

	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if (framework != null) {
						framework.stop();
						framework.waitForStop(0);
					}
				} catch (Exception e) {
					System.err.println("Error stopping framework: " + e);
				}
			}
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();
		
		primaryStage.setX(bounds.getMinX());
		primaryStage.setY(bounds.getMinY());
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());
		primaryStage.setTitle("Ui Application");
		
		viewManager = new ViewManager(framework.getBundleContext());
		framework.getBundleContext().addServiceListener(viewManager);
		primaryStage.setScene(viewManager.getScene());
		
		primaryStage.show();
	}
	
	@Override
	public void stop() throws Exception {
		try {
            framework.getBundleContext().getBundle(0).stop();
            super.stop();
        } catch (BundleException ex) {
        	ex.printStackTrace();
        }
	}

}
