package de.hcl.synchronize.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * the IniFile extends the IniReader for file functionality. It also implements
 * write functionality.
 * 
 * @author sebastian
 */
public class IniFile extends IniReader {

	/**
	 * the ini file
	 */
	private File file;

	public IniFile(File file) throws FileNotFoundException, IOException {
		super(new FileInputStream(file));
		this.file = file;
	}

	public void setPropertyString(String section, String key, String value) {
		Map<String, String> map = sections.get(section.toLowerCase());
		if (map == null) {
			map = new HashMap<String, String>();
			sections.put(section.toLowerCase(), map);
		}
		if (value != null)
			map.put(key, value);
		else
			map.remove(key);

	}

	public void setPropertyInt(String section, String key, int value) {
		setPropertyString(section, key, value + "");
	}

	public void setPropertyBool(String section, String key, boolean value) {
		setPropertyString(section, key, value + "");
	}

	public void writeFile() throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		for (String sectionName : sections.keySet()) {
			fileWriter.write("[" + sectionName + "]\n");
			Map<String, String> section = sections.get(sectionName);
			for (String key : section.keySet()) {
				String value = section.get(key);
				fileWriter.write("\t" + key + " = " + value + "\n");
			}
		}
		fileWriter.close();
	}

	public Set<String> getKeySet(String section) {
		if (!section.contains(section))
			return null;
		return sections.get(section).keySet();
	}

	public static void main(String args[]) {
		try {
			File file = new File("/home/sebastian/temp/config.ini");
			if (!file.exists()) {
				file.createNewFile();
				IniFile iniFile = new IniFile(file);

				iniFile.setPropertyString("BastisDokumente", "Inspiron1",
						"/home/sebastian/temp/cl1");
				iniFile.setPropertyString("BastisDokumente", "Inspiron2",
						"/home/sebastian/temp/cl1");
				iniFile.setPropertyString("Musik", "baestynator",
						"/media/baestynator/Musik");

				iniFile.writeFile();
			} else {
				IniFile iniFile = new IniFile(file);
				System.out.println(iniFile.getSections());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
