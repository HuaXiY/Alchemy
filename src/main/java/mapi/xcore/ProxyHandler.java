package mapi.xcore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;

public class ProxyHandler implements InvocationHandler {
	private final Interpreter interpreter = new Interpreter();
	private final String expression;
	private final List<String> largs;
	private final ICommandSender sender;
	public ProxyHandler(ICommandSender sender, List<String> largs, String expression){
		this.sender = sender;
		this.largs = largs;
		this.expression = expression;
	}
	public static Object bind(ICommandSender sender, Class<?> c, String args, String expression){
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{c},
				new ProxyHandler(sender, args != null ? args.indexOf('$') != -1 ? 
						Arrays.asList(args.split("$")) : Oneself.execute(new ArrayList<String>(), l -> l.add(args)) : null, expression));
	}	

	public Object invoke(Object proxy , Method method , final Object[] args) throws Throwable {
		if(largs != null && args != null){
			final Container<Integer> c_index = new Container<Integer>(0);
			largs.forEach(s -> interpreter.var.put(s, args[c_index.i]));
		}
		return interpreter.calculate(sender, expression);
	}
}
