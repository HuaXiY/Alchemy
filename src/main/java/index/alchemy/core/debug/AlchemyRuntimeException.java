package index.alchemy.core.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import index.alchemy.api.Alway;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.annotation.Change;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.util.Tool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class AlchemyRuntimeException extends RuntimeException {
	
	@Config(category = "runtime", comment = "Serious exceptions are ignored in the game.")
	private static boolean ignore_serious_exceptions = false;
	
	private AlchemyRuntimeException(Throwable t) {
		super(t);
	}
	
	public static void onException(Throwable t) {
		if (t instanceof AlchemyRuntimeException)
			return;
		
		final AlchemyRuntimeException e = new AlchemyRuntimeException(t);
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		final String error = sw.toString();
		
		AlchemyModLoader.logger.error(error);
		
		if (ignore_serious_exceptions)
			return;
		
		for (StackTraceElement element : e.getStackTrace())
			try {
				Class<?> clazz = Class.forName(element.getClassName());
				for (Method method : Tool.searchMethod(clazz, element.getMethodName()))
					if (checkAnnotation(clazz, method))
						break;
			} catch (ClassNotFoundException ex) {
				continue;
			}
		
		if (Alway.runOnClient()) {
			AlchemyEventSystem.addDelayedRunnable(new IPhaseRunnable() {
				@Override
				public void run(Phase phase) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiAlchemyRuntimeError(e, error));
				}
			}, 0);
		} else
			throw e;
		
	}
	
	private static boolean checkAnnotation(Class<?> clazz, Method method) {
		Unsafe unsafe = method.getAnnotation(Unsafe.class);
		if (unsafe != null) {
			unsafe(clazz.getName() + "#" + method.getName() + "()");
			unsafe(unsafe.value());
		}
		Change change = method.getAnnotation(Change.class);
		if (change != null && !change.value().equals(AlchemyConstants.MC_VERSION)) {
			change(clazz.getName() + "#" + method.getName() + "()");
			change(change.value() + "<=>" + AlchemyConstants.MC_VERSION);
		}
		return unsafe != null || change != null;
	}
	
	private static void unsafe(String msg) {
		AlchemyModLoader.logger.error("AlchemyRuntimeException: unsafe >>> " + msg);
	}
	
	private static void change(String msg) {
		AlchemyModLoader.logger.error("AlchemyRuntimeException: change >>> " + msg);
	}
	
	public static class GuiAlchemyRuntimeError extends GuiErrorScreen {
		
	    private Exception e;
	    private String error, msgs[];
	    
	    public GuiAlchemyRuntimeError(Exception e, String error) {
	        super(null, null);
	        this.e = e;
	        this.error = error;
	        msgs = error.split("\n");
	        msgs[0] = msgs[0].replace(msgs[0].charAt(msgs[0].length() - 1), ' ');
	        for (int i = 1; i < msgs.length; i++)
	        	msgs[i] = msgs[i].replace(msgs[i].charAt(0), ' ').substring(0, msgs[i].length() - 1);
	    }
	    
	    @Override
	    public void initGui() {
	        super.initGui();
	        buttonList.clear();
	        buttonList.add(new GuiButton(0, width / 2 - 200, height - 20, "复制"));
	        buttonList.add(new GuiButton(0, width / 2, height - 20, "上传"));
	    }

	    @Override
	    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	        drawDefaultBackground();
	        String title = "Alchemy-error";
	        drawString(fontRendererObj, title, (width - fontRendererObj.getStringWidth(title)) / 2, 30, 0xFFFFFF);
	        int offset = 40;
	        List<String> list = new LinkedList<String>();
	        for (String msg : msgs) {
	        	StringBuilder builder = new StringBuilder();
	        	int w = 0;
	        	boolean flag = false;
	        	for (char c : msg.toCharArray()) {
	        		w += fontRendererObj.getCharWidth(c);
	        		builder.append(c);
	        		if (w > width - 60) {
	        			w = 0;
	        			if (flag)
	        				list.add("    " + builder.toString());
	        			else {
	        				flag = true;
	        				list.add(builder.toString());
	        			}
	        			builder.replace(0, builder.length(), "");
	        		}
	        	}
	        	list.add(flag ? "    " + builder.toString() : builder.toString());
	        }
	        boolean flag = false;
	        for (String msg : list) {
	        	if ((offset += fontRendererObj.FONT_HEIGHT) < height - 30)
	        		drawString(fontRendererObj, msg, 30, offset, 0xFFFFFF);
	        	else {
	        		flag = true;
	        		break;
	        	}
	        }
	        if (flag)
	        	drawString(fontRendererObj, "...", 30, offset, 0xFFFFFF);
	        for (int i = 0; i < buttonList.size(); i++)
	        	buttonList.get(i).drawButton(this.mc, mouseX, mouseY);
	    }
	}
	
}