package vn.quantda.osgifx.embedosgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class BundleListenerImpl implements BundleListener {

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		String symbolicName = bundle.getSymbolicName();
		System.out.println("Bundle has event: " + symbolicName + " - State: " + bundle.getState()); 
	}

}
