package org.coral.test.asm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyClassLouder extends ClassLoader {
	
	private String classpath;
	private String name;
	
	public MyClassLouder(String classpath, String name) {
		this.classpath = classpath;
		this.name = name;
	}
	
	public MyClassLouder(String classpath, String name, ClassLoader parent) {
		super(parent);
		this.classpath = classpath;
		this.name = name;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			byte[] bin = Files.readAllBytes(Paths.get(classpath + name.replace(".", "/") + ".class"));
			return defineClass(bin, 0, bin.length);
		} catch (IOException e) {
			throw new ClassNotFoundException();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		System.out.println("MyClassLoader finalize");
	}
	
	@Override
	public String toString() {
		return name;
	}
}