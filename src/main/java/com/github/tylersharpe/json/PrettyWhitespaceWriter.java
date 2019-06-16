package com.github.tylersharpe.json;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * {@link WhitespaceWriter} which will indent entries at a given depth by a given number from space characters
 */
public class PrettyWhitespaceWriter implements WhitespaceWriter {

  private static final Map<Integer, PrettyWhitespaceWriter> CACHE = new HashMap<>();

  private String indent;

  private PrettyWhitespaceWriter(int indentSpaces) {
    this.indent = IntStream.range(0, indentSpaces).mapToObj(__ -> " ").collect(joining());
  }

  public static PrettyWhitespaceWriter withIndent(int indent) {
    return CACHE.computeIfAbsent(indent, PrettyWhitespaceWriter::new);
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
