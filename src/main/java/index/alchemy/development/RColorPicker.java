package index.alchemy.development;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Test;
import index.alchemy.util.JFXHelper;
import index.project.version.annotation.Omega;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Test
@Omega
@SideOnly(Side.CLIENT)
@Init(state = ModState.PREINITIALIZED, enable = false)
public class RColorPicker {
	
	public static ColorPicker color_picker;
	
	public static volatile float a, r, g, b;
	
	public static int getRGB() {
		return new java.awt.Color(RColorPicker.r, RColorPicker.g, RColorPicker.b).getRGB();
	}
	
	public static Color getColor() {
		return color_picker.getValue();
	}
	
	public static void init() {
		JFXHelper.runLater(() -> {
			Stage stage = new Stage();
			stage.setTitle("ColorPicker");
			Scene scene = new Scene(new HBox(20), 240, 30);
			HBox box = (HBox) scene.getRoot();
			box.setPadding(new Insets(5, 5, 5, 5));		
			color_picker = new ColorPicker();
			color_picker.setValue(Color.CORAL);
			color_picker.setOnAction(e -> {
				Color color = color_picker.getValue();
				a = 1 - (float) color.getOpacity();
				r = (float) color.getRed();
				g = (float) color.getGreen();
				b = (float) color.getBlue();
			});
			box.getChildren().addAll(color_picker);
			stage.setScene(scene);
			stage.show();
		});
	}
	
}