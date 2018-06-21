package com.scaiz.vertx.deploy.verticle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.tools.JavaFileObject.Kind;

class JavaSourceContext {

  private final String className;
  private final File sourceRoot;

  JavaSourceContext(File file) {
    String packageName = parsePackage(file);

    File rootDirectory = file.getParentFile();

    if (packageName != null) {
      String[] pathTokens = packageName.split("\\.");
      for (int i = pathTokens.length - 1; i >= 0; i--) {
        String token = pathTokens[i];
        if (!token.equals(rootDirectory.getName())) {
          throw new RuntimeException(
              "Package structure does not match directory structure: " + token
                  + " != " + rootDirectory.getName());
        }
        rootDirectory = rootDirectory.getParentFile();
      }
    }

    sourceRoot = rootDirectory;

    String fileName = file.getName();
    String className = fileName.substring(0, fileName.length()
        - Kind.SOURCE.extension.length());
    if (packageName != null) {
      className = packageName + '.' + className;
    }
    this.className = className;

  }


  File getSourceRoot() {
    return sourceRoot;
  }

  String getClassName() {
    return className;
  }

  private static String parsePackage(File file) {
    try {
      String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      // http://stackoverflow.com/questions/1657066/java-regular-expression-finding-comments-in-code
      source = source.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 " );
      for (String line : source.split("\\r?\\n")) {
        line = line.trim();
        if (!line.isEmpty()) {
          int idx = line.indexOf("package ");
          if (idx != -1) {
            return line.substring(line.indexOf(' ', idx), line.indexOf(';', idx)).trim();
          }
          return null; // Package definition must be on the first non-comment line
        }
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
