package mapi.xcore;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.sun.javafx.application.LauncherImpl;
import com.sun.javafx.application.PlatformImpl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
public class ActivityTreeViewSample extends Application {
	//private Node rootIcon;
	public JFXPanel jfxp = new JFXPanel();
	public static TreeItem<String> init = new TreeItem<String>();
	public static Stage stage;
	private static volatile boolean start;
 
	public static void start() {
		new Thread(() -> LauncherImpl.launchApplication(ActivityTreeViewSample.class, new String[0])).start();
	}
	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		primaryStage.setTitle("Activity Object");
		primaryStage.show();
		start = true;
	}
	public static void setNowObject(Object obj){
		setNowObject(obj, null);
	}
	public static synchronized void setNowObject(Object obj, Field f){
		if (!start)
			start();
		while (!start)
			Animation.sleepSeconds(1);
		PlatformImpl.startup(() -> {});
		Platform.runLater(() -> {
			TreeItem<String> rootItem = new TreeItem<String>((f == null ? "" : f.getName()) + obj.getClass().getName() + " : " + obj);
			TreeView<String> tree = new TreeView<String> (rootItem);
			rootItem.setExpanded(true);
			try {
				addChildren(obj, obj, f, null, rootItem);
			} catch(Exception e){e.printStackTrace();}
			StackPane root = new StackPane();
			root.getChildren().add(tree);
			stage.setScene(new Scene(root));
		});
	}
	public static boolean check(Object obj, Field f, Class<?> clazz, TreeItem<String> rootItem){
		if(obj == null){
			TreeItem<String> item;
			if(f != null)item = new TreeItem<String>(f.getType().getName() + " : " + f.getName() + " -> null");
			else if(clazz != null)item = new TreeItem<String>(clazz.getName());
			else item = new TreeItem<String>("null");
			rootItem.getChildren().add(item);
			return true;
		}
		return false;
	}
	public static void addChildren(Object src, Object obj, Field f, Class<?> clazz, TreeItem<String> rootItem) throws Exception {
		if(check(obj, f, clazz, rootItem))return;
		if(Tool.Objects.isSimple(obj.getClass()) && src != obj){
 			TreeItem<String> val;
 			if(f != null)val = new TreeItem<String>(f.getType().getName() + " : " + f.getName() + " -> " + Tool.Objects.isNullOr(obj, "null"));
 			else if(clazz != null)val = new TreeItem<String>(clazz.getName() + " -> " + Tool.Objects.isNullOr(obj, "null"));
 			else val = new TreeItem<String>(obj.getClass().getName() + " -> " + Tool.Objects.isNullOr(obj, "null"));
 			rootItem.getChildren().add(val);
 			return;
 		}
		for (Field field : Tool.Fields.getAll(obj.getClass())) {
			if(Modifier.isStatic(field.getModifiers()))continue;
			field.setAccessible(true);
			TreeItem<String> item;
			if(field.getType().isArray()){
				Container<EventHandler<TreeModificationEvent<String>>> handler = new Container<EventHandler<TreeModificationEvent<String>>>();
				item = new TreeItem<String>(field.getType().getName() + " : " + field.getName());
				item.addEventHandler(TreeItem.branchExpandedEvent(),
						handler.i = e -> Platform.runLater(() -> {
							item.getChildren().remove(init);
							try {
								addChildrenArray(src, field.get(obj), field, null, item);
							} catch(Exception ex){ex.printStackTrace();}
							item.removeEventHandler(TreeItem.branchExpandedEvent(), handler.i);
						}));
				item.getChildren().add(init);
				rootItem.getChildren().add(item);
			} else {
				Object object = field.get(obj);
				if(object == null)addChildren(src, null, field, null, rootItem);
				else if(Tool.Objects.isSimple(object.getClass()))addChildren(src, object, field, null, rootItem);
				else {
					Container<EventHandler<TreeModificationEvent<String>>> handler = new Container<EventHandler<TreeModificationEvent<String>>>();
					item = new TreeItem<String>(field.getType().getName() + " : " + field.getName());
					item.addEventHandler(TreeItem.branchExpandedEvent(),
							handler.i = e -> Platform.runLater(() -> {
								item.getChildren().remove(init);
								try {
									addChildren(src, object, field, null, item);
								} catch(Exception ex){ex.printStackTrace();}
								item.removeEventHandler(TreeItem.branchExpandedEvent(), handler.i);
							}));
					item.getChildren().add(init);
					rootItem.getChildren().add(item);
				}
			}
		}
	}
	public static void addChildrenArray(Object src, Object arr, Field f, Class<?> clazz, TreeItem<String> rootItem) throws Exception {
		if(check(arr, f, clazz, rootItem))return;
		for(int i = 0, len = Array.getLength(arr); i < len; i++)addChildren(src, Array.get(arr, i), null, arr.getClass().getComponentType(), rootItem);
	}
}
