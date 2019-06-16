package com.github.tylersharpe.json;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * {@link WhitespaceWriter} which will indent entries at a given depth by a given number from space characters
 */
public class PrettyWhitespaceWriter implements WhitespaceWriter {

  private String indent;

  public PrettyWhitespaceWriter() {
    this(2);
  }

  @SuppressWarnings("WeakerAccess")
  public PrettyWhitespaceWriter(int indentSpaces) {
    this.indent = IntStream.range(0, indentSpaces).mapToObj(__ -> " ").collect(joining());
  }

  @Override
  public void writeItemSeparator(Writer writer, int depth) throws IOException {
    writer.append("\n");
    for (int i = 1; i <= depth; i++) {
      writer.append(indent);
    }
  }

  @Override
  public void writeColon(Writer writer) throws IOException {
    writer.append(": ");
  }

}
