package vn.quantda.embedfelix.simpleserviceprovider;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import vn.quantda.embedfelix.service.ViewService;

public class SimpleViewActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Registering SimpleView");
		Hashtable<String, String> props = new Hashtable<>();
		props.put("ViewName", "SimpleView");
		context.registerService(ViewService.class.getName(), new SimpleView(), props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Unregistering SimpleView");

	}

}
