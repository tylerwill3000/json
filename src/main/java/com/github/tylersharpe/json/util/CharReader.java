package com.github.tylersharpe.json.util;

import java.io.*;
import java.util.Set;

public class CharReader implements Closeable {

  private static final char NULL_CHAR = '\u0000';

  private Reader reader;
  private int lastIndexRead = -1;
  private char peekChar = NULL_CHAR;

  public CharReader(Reader reader) {
    this.reader = reader;
  }

  public void munchWhitespace() throws IOException {
    while (true) {
      char next = nextCharacter();
      if (!Character.isWhitespace(next)) {
        backRead(next);
        return;
      }
    }
  }

  public char peek() throws IOException {
    char peeked = nextCharacter();
    backRead(peeked);
    return peeked;
  }

  public String readUntil(Set<Character> stops, boolean consumeStop) throws IOException {
    StringBuilder result = new StringBuilder();
    boolean escaping = false;

    while (true) {
      char next = nextCharacter();
      if (stops.contains(next) && !escaping) {
        if (!consumeStop) {
          backRead(next);
        }
        return result.toString();
      }

      escaping = next == '\\';

      if (!escaping) {
        result.append(next);
      }
    }
  }

  public char nextCharacter() throws IOException {
    int next;
    if (peekChar != NULL_CHAR) {
      next = peekChar;
      peekChar = NULL_CHAR;
    } else {
      next = reader.read();
      lastIndexRead++;
    }

    if (next == -1) {
      throw new EOFException("Encountered premature end of stream");
    }

    return (char) next;
  }

  public int getLastIndexRead() {
    return lastIndexRead;
  }

  private void backRead(char aChar) {
    peekChar = aChar;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

}
