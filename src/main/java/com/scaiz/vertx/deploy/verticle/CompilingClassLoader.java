package com.scaiz.vertx.deploy.verticle;

import com.scaiz.vertx.json.Json;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class CompilingClassLoader extends ClassLoader {

  private static final String JAVA_COMPILER_OPTIONS_PROP_NAME = "vertx.javaCompilerOptions";
  private static List<String> COMPILER_OPTIONS;


  private final JavaSourceContext javaSourceContext;
  private final MemoryFileManager fileManager;

  static {
    String props = System.getProperty(JAVA_COMPILER_OPTIONS_PROP_NAME);
    if (props != null) {
      String[] arr = props.split(",");
      List<String> compilerPros = new ArrayList<>();
      for (String prop : arr) {
        compilerPros.add(prop.trim());
      }
      COMPILER_OPTIONS = Collections.unmodifiableList(compilerPros);
    } else {
      COMPILER_OPTIONS = Collections.emptyList();
    }
  }

  public CompilingClassLoader(ClassLoader parent, String sourceName) {
    super(parent);
    URL resource = getResource(sourceName);
    if (resource == null) {
      throw new RuntimeException("Resource not found " + sourceName);
    }
    File sourceFile;
    try {
      sourceFile = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Failed to decode " + e.getMessage());
    }

    if (!sourceFile.canRead()) {
      throw new RuntimeException("File not found: "
          + sourceFile.getAbsolutePath()
          + " current dir is " + new File(".").getAbsolutePath());
    }

    this.javaSourceContext = new JavaSourceContext(sourceFile);

    try {
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
      JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
      if (javaCompiler == null) {
        throw new RuntimeException(
            "unable to detect java compiler, make sure you are using a JDK not a JRE!");
      }

      StandardJavaFileManager standardFileManager = javaCompiler
          .getStandardFileManager(null, null, null);
      standardFileManager.setLocation(StandardLocation.SOURCE_PATH,
          Collections.singleton(javaSourceContext.getSourceRoot()));
      fileManager = new MemoryFileManager(parent, standardFileManager);

      JavaFileObject javaFile = standardFileManager.getJavaFileForInput(
          StandardLocation.SOURCE_PATH, resolveMainClassName(), Kind.SOURCE);
      JavaCompiler.CompilationTask task = javaCompiler.getTask(
          null, fileManager, diagnostics, COMPILER_OPTIONS,
          null, Collections.singleton(javaFile));
      boolean valid = task.call();
      if (valid) {
        System.out.println(Json.encode(diagnostics));
      } else {
        System.err.println(Json.encode(diagnostics));
        throw new RuntimeException("Compilation failed");
      }
    } catch (Exception e) {
      throw new RuntimeException("Compilation failed", e);
    }
  }

  private String resolveMainClassName() {
    return javaSourceContext.getClassName();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] byteCode = getClassBytes(name);
    if (byteCode == null) {
      throw new ClassNotFoundException();
    }
    return defineClass(name, byteCode, 0, byteCode.length);
  }

  private byte[] getClassBytes(String name) {
    return fileManager.getCompiledClass(name);
  }
}
