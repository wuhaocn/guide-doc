package org.coral.test.loader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

public class ClassLoaderTest {

	public static void main(String[] args) {
		final String classPath = "E:/share/";
		final String className = "cn.itest.Person";
		final String fileName = className.replace(".", "/") + ".class";

		File f = new File(classPath, fileName);
		ClassFileObserver cfo = new ClassFileObserver(f.getAbsolutePath());

		cfo.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				try {

					Object[] loadTimes = (Object[]) arg;
					System.out.println(loadTimes[0] + " <---> " + loadTimes[1]);// 新旧时间对比

					Class<?> loadClass = ClassHotLoader.get(classPath)
							.loadClass(className);
					Object person = loadClass.newInstance();
					Method sayHelloMethod = loadClass.getMethod("sayHello");
					sayHelloMethod.invoke(person);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		cfo.startObserve();
	}
}