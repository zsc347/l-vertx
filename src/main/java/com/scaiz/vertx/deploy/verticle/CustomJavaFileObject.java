package com.scaiz.vertx.deploy.verticle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

public class CustomJavaFileObject implements JavaFileObject {

  private final String binaryName;
  private final Kind kind;
  private final URI uri;

  public CustomJavaFileObject(URI uri, Kind kind, String binaryName) {
    this.uri = uri;
    this.kind = kind;
    this.binaryName = binaryName;

  }

  public String binaryName() {
    return binaryName;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    String name = simpleName + kind.extension;
    return (name.equals(toUri().getPath()) || toUri().getPath()
        .endsWith('/' + name)) && kind.equals(getKind());
  }

  @Override
  public NestingKind getNestingKind() {
    return null;
  }

  @Override
  public Modifier getAccessLevel() {
    return null;
  }

  @Override
  public URI toUri() {
    return uri;
  }

  @Override
  public String getName() {
    return toUri().getPath();
  }

  @Override
  public InputStream openInputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Writer openWriter() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLastModified() {
    return 0L;
  }

  @Override
  public boolean delete() {
    return false;
  }

  @Override
  public String toString() {
    return getClass().getName() + '[' + toUri() + ']';
  }
}
