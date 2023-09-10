package com.github.tylerwilliams.json.util;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Reader capable of reading until a given stop sequence / set of stop characters
 */
public class StopSequenceReader implements Closeable {

    private Reader reader;
    private int lastIndexRead = -1;
    private boolean endOfStreamReached;
    private Deque<Character> backReadQueue = new ArrayDeque<>();

    public StopSequenceReader(Reader reader) {
        this.reader = reader;
    }

    public void munchWhitespace() throws IOException {
        while (true) {
            char next = nextCharacter();
            if (!Character.isWhitespace(next)) {
                backRead(next);
                break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public char peekNextUpcomingOnly() throws IOException {
        if (backReadQueue.isEmpty()) {
            peek();
        }
        return backReadQueue.peekFirst();
    }

    public char peek() throws IOException {
        char peeked = doRead();
        backRead(peeked);
        return peeked;
    }

    public String readUntil(Set<Character> stops, boolean consumeStop) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean escaping = false;

        while (true) {
            char next = nextCharacter();
            if (endOfStreamReached) {
                throw new EOFException("Encountered end of stream while reading for one of " + stops);
            }
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

    public String readUntil(String stopSequence) throws IOException {
        if (stopSequence == null || stopSequence.isEmpty()) {
            throw new IllegalArgumentException("Stop sequence must be >= 1 character");
        }

        StringBuilder result = new StringBuilder();

        int nextMatchIndex = 0;
        while (true) {
            char next = nextCharacter();
            if (endOfStreamReached) {
                throw new EOFException("Encountered end of stream while reading for sequence '" + stopSequence + "'");
            }
            result.append(next);

            if (next == stopSequence.charAt(nextMatchIndex)) {
                nextMatchIndex++;
                if (nextMatchIndex == stopSequence.length()) {
                    return result.toString();
                }
            } else {
                nextMatchIndex = next == stopSequence.charAt(0) ? 1 : 0;
            }
        }
    }

    public char nextCharacter() throws IOException {
        return backReadQueue.isEmpty() ? doRead() : backReadQueue.poll();
    }

    public int getLastIndexRead() {
        return lastIndexRead - backReadQueue.size();
    }

    private void backRead(char aChar) {
        backReadQueue.offer(aChar);
    }

    private char doRead() throws IOException {
        if (endOfStreamReached) {
            return (char) -1;
        }
        int next = reader.read();
        if (next == -1) {
            endOfStreamReached = true;
        }
        lastIndexRead++;
        return (char) next;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
