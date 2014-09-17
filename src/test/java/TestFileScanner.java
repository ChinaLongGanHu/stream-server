import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;

public class TestFileScanner {

	
	public static void main(String[] args) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try {
			
			
			
			System.out.println(FilenameUtils.getFullPath("logback.xml"));
			
			URL url = classLoader.getResource("logback.xml");
			
			System.out.println(url);
			
			Enumeration<URL> urls = classLoader.getResources("logback.xml");

			while (urls.hasMoreElements()) {
				
				URL packageURL = urls.nextElement();
				
				System.out.println(packageURL);
				
//				if (packageURL.getProtocol().equals("jar")) {
//					String jarFileName;
//					JarFile jf;
//					Enumeration<JarEntry> jarEntries;
//					// build jar file name, then loop through zipped entries
//					try {
//
//						jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
//
//						jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
//
//						Logger.logger.info("scan jar file:" + jarFileName);
//
//						jf = new JarFile(jarFileName);
//
//						jarEntries = jf.entries();
//
//						while (jarEntries.hasMoreElements()) {
//							JarEntry jarentry = jarEntries.nextElement();
//
//							String entryName = jarentry.getName();
//
////							Logger.logger.info("entry:" + entryName);
//
//							if (entryName.endsWith(".class")) {
//								String className = entryName.replace("/", ".").replace(".class", "");
//
//								className = className.replace("/", ".");
//
//								if (className.contains("$"))
//									continue;
//
//								if (!className.startsWith(packageName))
//									continue;
//
////								try {
//
////									Method method = Class.forName(className).getMethod("main", String[].class);
//
////									if (method != null)
//								names.add(className);
//
////								} catch (Exception e) {
//								//
////								}
//							}
//						}
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//
//				} else {
//					File folder = new File(packageURL.getPath());
//
//					scanFolder(packageName, folder, names);
//				}
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
