package de.hcl.synchronize.util;

/*
 * Copyright (c) 2002 Stefan Matthias Aust.  All Rights Reserved.
 *
 * You are granted the right to use this code in a) GPL based projects in 
 * which case this code shall be also protected by the GPL, or b) in other 
 * projects as long as you make all modifications or extensions to this 
 * code freely available, or c) make any other special agreement with the 
 * copyright holder.
 */
import java.io.*;
import java.util.*;

/**
 * This class can read properties files in Microsoft .ini file style and
 * provides an interface to read string, integer and boolean values. The .ini
 * files has the following structure:
 * 
 * <pre>
 * ; a comment
 * [section]
 * key=value
 * </pre>
 * 
 * @author Stefan Matthias Aust (sma@3plus4.de)
 * @version 1
 */
public class IniReader {

	protected Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();

	public IniReader(String pathname) throws FileNotFoundException, IOException {
		this(new FileReader(pathname));
	}

	public IniReader(InputStream input) throws FileNotFoundException,
			IOException {
		this(new InputStreamReader(input));
	}

	public IniReader(Reader input) throws FileNotFoundException, IOException {
		initialize(new BufferedReader(input));
	}

	private void initialize(BufferedReader r) throws IOException {
		String section = null, line;
		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.equals("") || line.startsWith(";")) {
				continue;
			}
			if (line.startsWith("[")) {
				if (!line.endsWith("]")) {
					throw new IOException("] expected in section header");
				}
				section = line.substring(1, line.length() - 1).toLowerCase();
			} else if (section == null) {
				throw new IOException("[section] header expected");
			} else {
				int index = line.indexOf('=');
				if (index < 0) {
					throw new IOException("key/value pair without =");
				}
				String key = line.substring(0, index).trim().toLowerCase();
				String value = line.substring(index + 1).trim();
				Map<String, String> map = sections.get(section);
				if (map == null) {
					sections.put(section, (map = new HashMap<String, String>()));
				}
				map.put(key, value);
			}
		}
	}

	public Set<String> getSections() {
		return sections.keySet();
	}

	public String getPropertyString(String section, String key,
			String defaultValue) {
		Map<String, String> map = sections.get(section.toLowerCase());
		if (map != null) {
			String value = (String) map.get(key.toLowerCase());
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	public int getPropertyInt(String section, String key, int defaultValue) {
		String s = getPropertyString(section, key, null);
		if (s != null) {
			return Integer.parseInt(s);
		}
		return defaultValue;
	}

	public boolean getPropertyBool(String section, String key,
			boolean defaultValue) {
		String s = getPropertyString(section, key, null);
		if (s != null) {
			return s.equalsIgnoreCase("true");
		}
		return defaultValue;
	}

	public static void main(String[] args) {
		try {
			IniReader v = new IniReader("d:\\text.ini");
			System.out.println("Land: "
					+ v.getPropertyString("section", "Land", "fehlt"));
			System.out.println("Stadt: "
					+ v.getPropertyString("section", "Stadt", "fehlt"));
			System.out.println("Fluss: "
					+ v.getPropertyString("section", "Fluss", "fehlt"));
			System.out.println("Bla Bla: "
					+ v.getPropertyString("section", "Bla Bla", "fehlt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeSection(String section) {
		sections.remove(section);
	}

}