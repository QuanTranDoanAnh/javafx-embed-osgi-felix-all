package vn.quantda.embedfelix.yav;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import vn.quantda.embedfelix.service.ViewService;

public class YetAnotherViewActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Registering YetAnotherView");
		Hashtable<String, String> props = new Hashtable<>();
		props.put("ViewName", "YetAnotherView");
		context.registerService(ViewService.class.getName(), new YetAnotherView(), props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Unregistering YetAnotherView");

	}

}
