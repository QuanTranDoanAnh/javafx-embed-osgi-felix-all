package vn.quantda.embedfelix.yav;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import vn.quantda.embedfelix.service.ViewService;

public class YetAnotherView implements ViewService {

	@Override
	public String getName() {
		return "Yet Another View";
	}

	@Override
	public Parent getContentPane() {
		final TextArea textArea = new TextArea();
		textArea.setPrefRowCount(50);
		textArea.setPrefColumnCount(50);
		textArea.setPadding(new Insets(50, 50, 50, 50));
		return textArea;
	}

}
