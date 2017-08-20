package mapi.xcore;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import index.alchemy.core.debug.AlchemyRuntimeException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

@SuppressWarnings({ "unchecked", "serial", "unused" })
public class Interpreter {
	public static final Interpreter interpreter;
	public static final Map<String, Class<?>> cls = new HashMap<String, Class<?>>();
	static {
		cls.put("byte", byte.class);
		cls.put("short", short.class);
		cls.put("int", int.class);
		cls.put("long", long.class);
		cls.put("float", float.class);
		cls.put("double", double.class);
		cls.put("char", char.class);
		cls.put("boolean", boolean.class);
		cls.put("Object", Object.class);
		cls.put("String", String.class);
		cls.put("Class", Class.class);
		cls.put("System", System.class);
		cls.put("Thread", Thread.class);
		cls.put("Runnable", Runnable.class);
	}
	final Map<String, Object> var = new HashMap<String, Object>();
	final class DList<T> extends LinkedList<T> {
		Double getD(int index){
			Object obj = super.get(index);
			if(obj instanceof Number)return ((Number) obj).doubleValue();
			else {
				Object r = ((Pointer<?>) obj).get();
				if(r == null)return 0.0;
				else if(r.getClass() == Double.class)return (Double) r;
				else if(r instanceof Number)return ((Number) r).doubleValue();
				else return 0.0;
			}
		}
	}
	public final Object basicsCalculate(ICommandSender sender, String expression, List<Class<?>> lc) throws InterpretationException {
		if(expression.charAt(0) == '-')expression = "0" + expression;
		Container<Class<?>> c_cls = new Container<Class<?>>();
		Container<Boolean> c_cb = new Container<Boolean>(true);
		DList<Object> ld = new DList<Object>();
		LinkedList<String> ls = new LinkedList<String>();
		char lastC = 0, ca[] = expression.toCharArray();
		int last = 0, index = 0, a = 0, b = 0;
		boolean state = true, strMode = false;
		for(; index < ca.length; index++){
			char c = ca[index], lastChar = lastC;
			lastC = c;
			if(strMode){
				if(c == '"' && lastChar != '\\')strMode = false;
				else continue;
			} else switch(c){
				case '"':
					strMode = true;
				continue;
				case '(':
					a++;
				continue;
				case ')':
					a--;
				break;
			}
			if(a != 0 && state)continue;
			else if(b != 0){
				if(c == '[')b++;
				else if(c == ']'){
					b--;
					if(b == 0){
						ld.add(new String(ca, last, index - last));
						last = index + 1;
					}
				}
				continue;
			}
			String str = String.valueOf(c); 
			if(state){
				if(str.matches("[^$\\.0-9a-zA-Z_\"\\(\\)]")){
					String add = new String(ca, last, index - last);
					if(add.length() == 0){
						state = false;
						continue;
					}
					if(add.charAt(0) == '(')ld.add(basicsCalculate(sender, add.substring(1, add.length() - 1), null));
					else try {
						if(Tool.Strings.contains(add, 'L'))ld.add(Long.valueOf(add));
						else ld.add(Double.valueOf(add));
					} catch(Exception e){ld.add(add);}
					state = false;
					last = index;
				}
			} else if(str.matches("[$\\.0-9a-zA-Z_\"\\(]")){
				ls.add(new String(ca, last, index - last - a));
				state = true;
				last = index - a;
			}
			if(b == 0 && c == '['){
				b++;
				last++;
				ls.add("[");
				state = true;
			}
		}
		String add = new String(ca, last, index - last);
		if(add.length() > 0)if(!state)ls.add(add);
		else if(add.charAt(0) == '(')ld.add(basicsCalculate(sender, add.substring(1, add.length() - 1), null));
		else try {
			if(Tool.Strings.contains(add, 'L'))ld.add(Long.valueOf(add));
			else ld.add(Double.valueOf(add));}
		catch(Exception e){ld.add(add);}
		Container<Integer> c_index = new Container<Integer>(0);
		Tool.Collections.copy(ls).forEach(s -> {
			if(s.length() > 1){
				char c = s.charAt(s.length() - 1);
				if(c == '@' || c == '!' || c == '^'){
					ls.remove(s);
					ls.add(c_index.i, s = s.substring(0, s.length() - 1));
					ls.add(++c_index.i, String.valueOf(c));
				}
			}
			if(s.length() > 2){
				String c = s.substring(0, 2);
				if(c.equals("++") || c.equals("--")){
					ls.remove(s);
					ls.add(c_index.i, c);
					ls.add(++c_index.i, s.substring(2));
				}
			}
			c_index.i++;
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 1)return;
			Object result = null;
			switch(s){
				case ":":
					Object i = ld.get(c_index.i);
					Object next = ld.get(c_index.i + 1);
					try {
						Class<?> c;
						if(c_cb.i){
							if(i instanceof Class){
								c = (Class<?>) i;
							} else {
								c = cls.get(i);
								if(c == null)c = Class.forName((String) i);
							}
							if ((c == char.class || c == Character.class) && next instanceof Number)
								result = (char) ((Number) next).intValue();
							else if(!c.isAssignableFrom(next.getClass()) && Tool.Objects.getPrimitiveMapping(c) != next.getClass())
								if(next instanceof Number)result = Tool.Numbers.get(c, (Number) next);
								else throw new InterpretationException(next.getClass().getName() + " can not be labeled as " + c.getName() + ".");
							if (lc != null)
								lc.add(c);
							c_cb.i = false;
							c_cls.i = c;
						}
					} catch(Exception e){err(sender, e);}
					ld.remove(c_index.i.intValue());
					if(result != null){
						ld.remove(c_index.i.intValue());
						ld.add(c_index.i, result);
					}
				break;
				case "@":
					i = ld.get(c_index.i);
					String argsname = Tool.Strings.get((String) i, "(.*?)\\("), args = Tool.Strings.get((String) i, "\\((.*)\\)");
					boolean argsflag = true, argss = false;
					List<Object> argslo = new LinkedList<Object>();
					List<Class<?>> argslc = new LinkedList<Class<?>>();
					if(argss = args.length() > 0)try {
						char lastChar = 0, argsca[] = args.toCharArray();
						int k = 0, argsstart = 0, argsindex = 0;
						for(char argsc : argsca){
							if(argsflag){
								if(argsc == '"' && lastChar != '\\')argsflag = false;
							} else switch(argsc){
								case '(':
									k++;
								break;
								case ')':
									k--;
								break;
								case ',':
									if(k == 0){
										argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), argslc));
										argsstart = argsindex + 1;
									}
								break;
								case '"':
									argsflag = true;
								break;
							}
							argsindex++;
						}
						argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), argslc));
					} catch(Exception e){err(sender, e);}
					try {
						Class<?> c = cls.get(argsname);
						if(c == null)c = Class.forName(argsname);
						result = Oneself.execute(c.getConstructor(argss ? argslc.toArray(new Class<?>[argslc.size()]) : new Class<?>[0]),
								n -> n.setAccessible(true)).newInstance(argss ? argslo.toArray() : new Object[0]);
					} catch(Exception e){err(sender, e);}
					ld.remove(c_index.i.intValue());
					ld.add(c_index.i, result);
				break;
				case "`":
					String p_args = (String) ld.get(c_index.i), p_expression = (String) ld.get(c_index.i + 1);
					int l = p_args.indexOf('$');
					String p_cn = l != -1 ? p_args.substring(0, l) : p_args;
					Class<?> p_c = cls.get(p_cn);
					if(p_c == null)try {p_c = Class.forName(p_cn);}
					catch(Exception e){throw new RuntimeException(e);}
					result = ProxyHandler.bind(sender, p_c, l != -1 ? p_args.substring(l + 1) : null, p_expression.substring(2, p_expression.length() - 1));
					ld.remove(c_index.i.intValue());
					ld.remove(c_index.i.intValue());
					ld.add(c_index.i, result);
					if(lc != null && c_cb.i){
						lc.add(p_c);
						c_cb.i = false;
					}
				break;
				default:
					c_index.i++;
				return;
			}
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			boolean flag = true;
			Object result = null, i = ld.get(c_index.i), next = ld.get(c_index.i + 1);
			if(s.equals("[")){
				boolean argsflag = false;
				List<Object> argslo = new LinkedList<Object>();
				try {
					char lastChar = 0, argsca[] = ((String) next).toCharArray();
					int k = 0, argsstart = 0, argsindex = 0;
					for(char argsc : argsca){
						if(argsflag){
							if(argsc == '"' && lastChar != '\\')argsflag = false;
						} else switch(argsc){
							case '(':
								k++;
							break;
							case ')':
								k--;
							break;
							case ',':
								if(k == 0){
									argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), null));
									argsstart = argsindex + 1;
								}
							break;
							case '"':
								argsflag = true;
							break;
						}
						argsindex++;
					}
					argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), null));
				} catch(Exception e){err(sender, e);}
				final Object obj;
				final int arg = ((Number) argslo.get(0)).intValue();
				if(i.getClass() == String.class){
					if(argslo.size() == 1)result = ((String) i).charAt(arg);
					else if(argslo.size() == 2)result = ((String) i).substring(arg, ((Number) argslo.get(1)).intValue());
				} else if((obj = ((Pointer<?>) i).get()).getClass().isArray()){
					result = new Pointer<>(() -> Array.get(obj, arg), o -> Array.set(obj, arg, o), obj.getClass().getComponentType());
				} else if(obj instanceof List)result = new Pointer<>(() -> ((List<Object>) obj).get(arg), o -> ((List<Object>) obj).set(arg, o));
			} else {
				Class<?> c = null;
				Object obj;
				if(i.getClass() == String.class){
					if(((String) i).charAt(0) == '"')obj = ((String) i).substring(1, ((String) i).length() - 1);
					else {
						obj = var.get(i);
						if(obj != null){
							c = obj.getClass();
						}
						else {
							c = cls.get(i);
							if(c == null)try {c = Class.forName((String) i);} catch(Exception e){err(sender, e);}
							if(c == null)flag = false;
						}
					}
				} else if(i.getClass() == Pointer.class){
					obj = ((Pointer<?>) i).get();
					c = obj == null ? ((Pointer<?>) i).getType() : obj.getClass();
				} else {
					obj = i;
					c = obj.getClass();
				}
				if(flag){
					switch(s){
						case "->":
							String name = (String) next;
							result = obj == null ? Pointer.as(c, name) : Pointer.as(obj, name);
						break;
						case "=>":
							String argsname = Tool.Strings.get((String) next, "(.*?)\\("), args = Tool.Strings.get((String) next, "\\((.*)\\)");
							boolean argsflag = false, argss = false;
							List<Object> argslo = new LinkedList<Object>();
							List<Class<?>> argslc = new LinkedList<Class<?>>();
							if(argss = args.length() > 0)try {
								char lastChar = 0, argsca[] = args.toCharArray();
								int k = 0, argsstart = 0, argsindex = 0;
								for(char argsc : argsca){
									if(argsflag){
										if(argsc == '"' && lastChar != '\\')argsflag = false;
									} else switch(argsc){
										case '(':
											k++;
										break;
										case ')':
											k--;
										break;
										case ',':
											if(k == 0){
												argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), argslc));
												argsstart = argsindex + 1;
											}
										break;
										case '"':
											argsflag = true;
										break;
									}
									argsindex++;
								}
								argslo.add(basicsCalculate(sender, new String(argsca, argsstart, argsindex - argsstart), argslc));
							} catch(Exception e){err(sender, e);}
							while(c != null)try {
								result = Oneself.execute(c.getDeclaredMethod(argsname, argss ? argslc.toArray(new Class<?>[argslc.size()]) : new Class<?>[0]),
										m -> m.setAccessible(true), m -> {
											if(lc == null || !c_cb.i)return;
											lc.add(m.getReturnType());
											c_cb.i = false;
								}).invoke(obj, argss ? argslo.toArray() : new Object[0]);
								break;
							} catch (Exception e){
								c = c.getSuperclass();
							}
						break;
						default:
							c_index.i++;
						return;
					}
				}
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			if(flag)ld.add(c_index.i, result);
			ls.remove(s);
		});
		List<Object> tlo = ld.stream().map(o -> {
			if(o != null && o.getClass() == String.class){
				String str = (String) o;
				if(str.charAt(0) == '"')return str.substring(1, str.length() - 1);
				else return new Pointer<Object>(() -> Interpreter.this.var.get(str), obj -> Interpreter.this.var.put(str, obj));
			} else return o;
		}).collect(Collectors.toList());
		ld.clear();
		ld.addAll(tlo);
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 1)return;
			switch(s){
				case "++":
					Tool.Numbers.add((Pointer<Number>) ld.get(c_index.i), 1);
				break;
				case "--":
					Tool.Numbers.add((Pointer<Number>) ld.get(c_index.i), -1);
				break;
				default:
					c_index.i++;
				return;
			}
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 1)return;
			double result = 0, i = ld.getD(c_index.i);
			switch(s){
				case "~":
					result = ~(int) i;
				break;
				case "!":
					result = i > 0 ? 0 : 1;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "*":
					result = i * next;
				break;
				case "/":
					result = i / next;
				break;
				case "%":
					result = i % next;
				break;
				case "^<":
					result = Math.pow(i, next);
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "+":
					result = i + next;
				break;
				case "-":
					result = i - next;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			int result = 0, i = ld.getD(c_index.i).intValue(), next = ld.getD(c_index.i + 1).intValue();
			switch(s){
				case "<<":
					result = i << next;
					break;
				case ">>":
					result = i >> next;
				break;
				case ">>>":
					result = i >>> next;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "<":
					result = i < next ? 1 : 0;
					break;
				case ">":
					result = i > next ? 1 : 0;
				break;
				case "<=":
					result = i <= next ? 1 : 0;
				break;
				case ">=":
					result = i >= next ? 1 : 0;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "==":
					result = i == next ? 1 : 0;
				break;
				case "!=":
					result = i >= next ? 1 : 0;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			int result = 0, i = ld.getD(c_index.i).intValue(), next = ld.getD(c_index.i + 1).intValue();
			switch(s){
				case "&":
					result = i & next;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			int result = 0, i = ld.getD(c_index.i).intValue(), next = ld.getD(c_index.i + 1).intValue();
			switch(s){
				case "|":
					result = i | next;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;	
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			int result = 0, i = ld.getD(c_index.i).intValue(), next = ld.getD(c_index.i + 1).intValue();
			switch(s){
				case "^":
					result = i ^ next;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "&&":
					result = i > 0 && next > 0 ? 1 : 0;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "||":
					result = i > 0 || next > 0 ? 1 : 0;
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 3)return;
			if(s.equals(":"))return;
			double result = 0, i = ld.getD(c_index.i), next = ld.getD(c_index.i + 1);
			switch(s){
				case "?":
					result = i > 0 ? next : ld.getD(c_index.i + 2);
				break;
				default:
					c_index.i++;
				return;
			}
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.remove(c_index.i.intValue());
			ld.add(c_index.i, result);
			ls.remove(s);
			ls.remove(":");
		});
		c_index.i = 0;
		Collections.reverse(ls);
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			double result = 0, i = ld.getD(ld.size() - 2), next = ld.getD(ld.size() - 1);
			switch(s){
				case "=":
					var.put((String) ld.get(ld.size() - 2), result = next);
				break;
				case "+=":
					var.put((String) ld.get(ld.size() - 2), result = i + next);
				break;
				case "-=":
					var.put((String) ld.get(ld.size() - 2), result = i - next);
				break;
				case "*=":
					var.put((String) ld.get(ld.size() - 2), result = i * next);
				break;
				case "/=":
					var.put((String) ld.get(ld.size() - 2), result = i / next);
				break;
				case "%=":
					var.put((String) ld.get(ld.size() - 2), result = i % next);
				break;
				case "&=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i & (int) next);
				break;
				case "|=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i | (int) next);
				break;
				case "^=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i ^ (int) next);
				break;
				case "<<=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i << (int) next);
				break;
				case ">>=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i >> (int) next);
				break;
				case ">>>=":
					var.put((String) ld.get(ld.size() - 2), result = (int) i >>> (int) next);
				break;
				default:return;
			}
			ld.removeLast();
			ld.removeLast();
			ld.addLast(result);
			ls.remove(s);
		});
		c_index.i = 0;
		Tool.Collections.copy(ls).forEach(s -> {
			if(ld.size() < 2)return;
			Object obj = ld.get(ld.size() - 2);
			if (!(obj instanceof Pointer))
				return;
			Pointer<?> i = (Pointer<?>) obj;
			Object next = ld.get(ld.size() - 1);
			if(next instanceof Number){
				if(next.getClass() == Long.class)Tool.Numbers.set(i, (long) next);
				else Tool.Numbers.set(i, ((Number) next).doubleValue());
			} else i.set(next);
			ld.remove(ld.size() - 2);
			ls.remove(s);
		});
		if(ls.size() > 0)throw new InterpretationException("The symbol " + ls + " can not be explained.");
		try {
			Object result = ld.get(0);
			if(result != null && result.getClass() == Pointer.class)result = ((Pointer<?>) result).get();
			if(lc != null && c_cb.i)lc.add(result == null ? Object.class : result.getClass());
			return result;
		} catch(Exception e){
			err(sender, e);
			return null;
		}
	}
	public Object calculate(ICommandSender sender, String expression) throws InterpretationException {
//		sender.addChatMessage(new TextComponentString(" > " + expression));
		if(expression.charAt(0) == '#'){
			expression = expression.substring(1);
			String[] sa = expression.split("->");
			if(sa.length < 2)throw new InterpretationException("define args < 2.");
			try {cls.put(sa[0], Class.forName(sa[1]));}
			catch(Exception e){err(sender, e);}
//			sender.addChatMessage(new TextComponentString(" < #define " + sa[0] + "->Class<" + sa[1] + ">"));
			return null;
		}
		return basicsCalculate(sender, expression, null);
//		return Oneself.execute(basicsCalculate(sender, expression, null), result -> 
//		sender.addChatMessage(new TextComponentString(" < " + Tool.Objects.isNullOr(result, ""))));
	}
	static {
		interpreter = new Interpreter();
	}
	public static final Object todo(ICommandSender sender, String expression){
		try {
			return interpreter.calculate(sender, expression);
		} catch (Throwable e) {
			err(sender, e);
			return null;
		}
	}
	public static final void err(ICommandSender sender, Throwable e){
		String s = e.getClass().getName();
		String message = e.getLocalizedMessage();
//		sender.addChatMessage(new TextComponentString(" ! " + (message != null ? s + "\n ^ " + message.replace("\n", "\n ^ ") : s)));
//		sender.addChatMessage(new TextComponentString(AlchemyRuntimeException.getStringFormThrowable(e)));
	}
}
