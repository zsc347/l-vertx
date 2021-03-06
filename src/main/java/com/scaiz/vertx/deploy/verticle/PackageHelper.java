package com.scaiz.vertx.deploy.verticle;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

class PackageHelper {

  private final static String CLASS_FILE = ".class";

  private final ClassLoader classLoader;

  PackageHelper(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  List<JavaFileObject> find(String packageName) throws IOException {
    String javaPackageName = packageName.replaceAll("\\.", "/");
    List<JavaFileObject> result = new ArrayList<>();

    Enumeration<URL> urlEnumeration = classLoader.getResources(javaPackageName);
    while (urlEnumeration.hasMoreElements()) {
      URL resource = urlEnumeration.nextElement();
      File directory;
      try {
        directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException("Failed to decode " + e.getMessage());
      }

      if (directory.isDirectory()) {
        result.addAll(browseDir(packageName, directory));
      } else {
        result.addAll(browseJar(resource));
      }
    }
    return result;
  }

  private List<JavaFileObject> browseDir(String packageName,
      File directory) {
    List<JavaFileObject> result = new ArrayList<>();
    for (File childFile : directory.listFiles()) {
      if (childFile.isFile() && childFile.getName().endsWith(CLASS_FILE)) {
        String binaryName = packageName + "." + childFile.getName()
            .replaceAll(CLASS_FILE + "$", "");
        result.add(
            new CustomJavaFileObject(childFile.toURI(), Kind.CLASS, binaryName));
      }
    }
    return result;
  }

  private static List<JavaFileObject> browseJar(URL packageFolderURL) {
    List<JavaFileObject> result = new ArrayList<>();
    try {
      String jarUri = packageFolderURL.toExternalForm().split("!")[0];
      JarURLConnection jarConn = (JarURLConnection) packageFolderURL
          .openConnection();
      String rootEntryName = jarConn.getEntryName();

      int rootEnd = rootEntryName.length() + 1;

      Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
      while (entryEnum.hasMoreElements()) {
        JarEntry jarEntry = entryEnum.nextElement();
        String name = jarEntry.getName();
        if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1
            && name.endsWith(CLASS_FILE)) {
          String binaryName = name.replaceAll("/", ".")
              .replaceAll(CLASS_FILE + "$", "");
          result.add(new CustomJavaFileObject(URI.create(jarUri + "!/" + name),
              Kind.CLASS, binaryName));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(packageFolderURL + " is not a JAR file", e);
    }
    return result;
  }

}
