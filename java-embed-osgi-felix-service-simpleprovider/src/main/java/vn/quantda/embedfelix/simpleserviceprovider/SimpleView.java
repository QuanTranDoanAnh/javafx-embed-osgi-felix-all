package vn.quantda.embedfelix.simpleserviceprovider;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import vn.quantda.embedfelix.service.ViewService;

public class SimpleView implements ViewService {

	@Override
	public String getName() {
		return "Simple View";
	}

	@Override
	public Parent getContentPane() {
		Label label = new Label("The label");
		label.setPadding(new Insets(5,10,0,0));
		TextField text = new TextField();
		
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(50, 50, 50, 50));
		hbox.getChildren().add(label);
		hbox.getChildren().add(text);
		return hbox;
	}

}
