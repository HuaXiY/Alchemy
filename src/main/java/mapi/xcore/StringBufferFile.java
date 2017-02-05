package mapi.xcore;

import java.io.Closeable;
import java.io.PrintWriter;

public class StringBufferFile implements Closeable {
	private final StringBuilder sb;
	private final String path;
	private PrintWriter pw;
	public StringBufferFile(String path, boolean read){
		this.path = path;
		sb = new StringBuilder(read ? Tool.Strings.read(path) : "");
		pw = Tool.Strings.getPrintWriter(path);
		pw.print(sb);
		pw.flush();
	}
	public synchronized void close() {
		pw.close();
	}
	public synchronized PrintWriter getNewPrintWriter(){
		pw.close();
		return pw = Tool.Strings.getPrintWriter(path);
	}
	public synchronized int length(){
		return sb.length();
	}
	public synchronized StringBufferFile newLine() {
		pw.println();
		pw.flush(); 
		sb.append("\n");
		return this;
	}
	public synchronized StringBufferFile append(Object obj){
		pw.print(obj);
		pw.flush(); 
		sb.append(obj);
		return this;
	}
	public synchronized StringBufferFile append(String str){
		pw.print(str);
		pw.flush();
		sb.append(str);
		return this;
	}
	public synchronized StringBufferFile append(StringBuffer s){
		pw.print(s);
		pw.flush();
		sb.append(s);
		return this;
	}
	public synchronized StringBufferFile append(CharSequence s){
		pw.print(s);
		pw.flush();
		sb.append(s);
		return this;
	}
	public synchronized StringBufferFile append(CharSequence s, int start, int end){
		pw.print(s.subSequence(start, end));
		pw.flush();
		sb.append(s, start, end);
		return this;
	}
	public synchronized StringBufferFile append(char[] str){
		pw.print(str);
		pw.flush();
		sb.append(str);
		return this;
	}
	public synchronized StringBufferFile append(char[] str, int offset, int len){
		pw.write(str, offset, len);
		pw.flush();
		sb.append(str, offset, len);
		return this;
	}
	public synchronized StringBufferFile append(boolean b){
		pw.print(b);
		pw.flush();
		sb.append(b);
		return this;
	}
	public synchronized StringBufferFile append(char c){
		pw.print(c);
		pw.flush();
		sb.append(c);
		return this;
	}
	public synchronized StringBufferFile append(int i){
		pw.print(i);
		pw.flush();
		sb.append(i);
		return this;
	}

	public synchronized StringBufferFile append(long lng){
		pw.print(lng);
		pw.flush();
		sb.append(lng);
		return this;
	}
	public synchronized StringBufferFile append(float f){
		pw.print(f);
		pw.flush();
		sb.append(f);
		return this;
	}
	public synchronized StringBufferFile append(double d){
		pw.print(d);
		pw.flush();
		sb.append(d);
		return this;
	}
	public synchronized StringBufferFile appendCodePoint(int codePoint){
		pw.print((char) codePoint);
		pw.flush();
		sb.appendCodePoint(codePoint);
		return this;
	}
	public synchronized StringBufferFile delete(int start, int end){
		sb.delete(start, end);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile deleteCharAt(int index){
		sb.deleteCharAt(index);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile replace(int start, int end, String str){
		sb.replace(start, end, str);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int index, char[] str, int offset, int len){
		sb.insert(index, str, offset, len);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, Object obj){
		sb.insert(offset, obj);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, String str){
		sb.insert(offset, str);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, char[] str){
		sb.insert(offset, str);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int dstOffset, CharSequence s){
		sb.insert(dstOffset, s);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int dstOffset, CharSequence s, int start, int end){
		sb.insert(dstOffset, s, start, end);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, boolean b){
		sb.insert(offset, b);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, char c){
		sb.insert(offset, c);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, int i){
		sb.insert(offset, i);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, long l){
		sb.insert(offset, l);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, float f){
		sb.insert(offset, f);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile insert(int offset, double d){
		sb.insert(offset, d);
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	public synchronized StringBufferFile reverse(){
		sb.reverse();
		getNewPrintWriter().print(sb);
		pw.flush();
		return this;
	}
	@Override
	public synchronized String toString(){
		return sb.toString();
	}
	public synchronized int indexOf(String str){
		return sb.indexOf(str);
	}

	public synchronized int indexOf(String str, int fromIndex){
		return sb.indexOf(str, fromIndex);
	}

	public synchronized int lastIndexOf(String str){
		return sb.lastIndexOf(str);
	}
	public synchronized int lastIndexOf(String str, int fromIndex){
		return sb.lastIndexOf(str, fromIndex);
	}
}
