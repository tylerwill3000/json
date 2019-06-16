package com.github.tylersharpe.json;

import java.io.IOException;
import java.io.Writer;

public interface WhitespaceWriter {

  void writeItemSeparator(Writer writer, int depth) throws IOException;

  void writeColon(Writer writer) throws IOException;

  WhitespaceWriter NO_WHITESPACE = new WhitespaceWriter() {
    @Override
    public void writeItemSeparator(Writer writer, int depth) {
    }

    @Override
    public void writeColon(Writer writer) throws IOException {
      writer.append(':');
    }
  };

}
