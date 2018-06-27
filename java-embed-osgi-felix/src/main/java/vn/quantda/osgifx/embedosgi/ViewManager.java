package vn.quantda.osgifx.embedosgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import vn.quantda.embedfelix.service.ViewService;

public class ViewManager implements ServiceListener {
	private BorderPane mainBorderPane;
	private BundleContext bundleContext;
	private Scene scene;
	private ServiceReference<?>[] serviceReferences;
	private Menu viewMenu;
	private MenuBar menuBar;
	
	public ViewManager(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		try {
			serviceReferences = (ServiceReference<?>[]) this.bundleContext.getServiceReferences(ViewService.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initializeScene() {
		mainBorderPane = new BorderPane();
		createMenuBar();
		mainBorderPane.setTop(menuBar);
		
		scene = new Scene(new Group());
		scene.setRoot(mainBorderPane);
	}
	
	public Scene getScene() {
		if (scene == null) {
			initializeScene();
		}
		return this.scene;
	}

	private void createMenuBar() {
		menuBar = new MenuBar();
		viewMenu = new Menu("Views");
		menuBar.getMenus().add(viewMenu);
		
		if (this.serviceReferences != null && this.serviceReferences.length > 0) {
			for(int i=0; i < this.serviceReferences.length; i++) {
				ViewService view = (ViewService) bundleContext.getService(this.serviceReferences[i]);
				MenuItem item = new MenuItem(view.getName());
				item.setOnAction(event -> {
					Label label = new Label(view.getName());
					mainBorderPane.setLeft(label);
					mainBorderPane.setCenter(view.getContentPane());
				});
				viewMenu.getItems().add(item);
			}
		}
		
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		// If a dictionary service was registered, see if we
        // need one. If so, get a reference to it.
        if (event.getType() == ServiceEvent.REGISTERED)
        {
            // Get a reference to the service object.
            ServiceReference<?> serviceRef = event.getServiceReference();
            ViewService view = (ViewService) bundleContext.getService(serviceRef);
            if (view != null) {
            	MenuItem item = new MenuItem(view.getName());
            	item.setOnAction(actionEvent -> {
            		Label label = new Label(view.getName());
					mainBorderPane.setLeft(label);
					mainBorderPane.setCenter(view.getContentPane());
            	});
            	viewMenu.getItems().add(item);
            }
        }
		
	}

	

}
