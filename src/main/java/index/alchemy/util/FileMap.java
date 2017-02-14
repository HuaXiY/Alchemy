package index.alchemy.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class FileMap implements Map<String, File> {
	
	protected final File folder;
	
	protected FileMap(File folder) {
		this.folder = folder;
	}
	
	@Nullable
	public static FileMap newFileMap(File file) {
		return file.exists() ? file.isDirectory() ? new FileMap(file) : null : file.mkdirs() ? new FileMap(file) : null;
	}

	@Override
	public int size() {
		return (int) Arrays.stream(folder.listFiles()).filter(File::isFile).count();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object val) {
		return val instanceof File && get(((File) val).getName()) != null;
	}

	@Nullable
	@Override
	public File get(Object key) {
		for (File file : folder.listFiles())
			if (file.isFile() && file.getName().equals(key))
				return file;
		return null;
	}

	@Override
	public File put(String key, File val) {
		if (!val.getParent().equals(folder.getPath()) || !key.equals(val.getName()))
			return val;
		File result = get(key);
		if (result == null)
			try {
				val.createNewFile();
			} catch (IOException e) { e.printStackTrace(); }
		return result;
	}

	@Nullable
	@Override
	public File remove(Object key) {
		File result = get(key);
		return result.delete() ? result : null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends File> map) {
		map.forEach(this::put);
	}

	@Override
	public void clear() {
		values().forEach(File::delete);
	}

	@Override
	public Set<String> keySet() {
		return Arrays.stream(folder.listFiles()).filter(File::isFile).map(File::getName).collect(Collectors.toSet());
	}

	@Override
	public Collection<File> values() {
		return Arrays.stream(folder.listFiles()).filter(File::isFile).collect(Collectors.toSet());
	}

	@Override
	public Set<Map.Entry<String, File>> entrySet() {
		return Arrays.stream(folder.listFiles()).filter(File::isFile).map(FileMap::toEntry).collect(Collectors.toSet());
	}
	
	protected static Map.Entry<String, File> toEntry(File f) {
		return new Map.Entry<String, File>() {

			@Override
			public String getKey() {
				return f.getName();
			}

			@Override
			public File getValue() {
				return f;
			}

			@Override
			public File setValue(File val) {
				return val;
			}

		};
	}

}
