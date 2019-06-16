package com.github.tylersharpe.json.util

import spock.lang.Specification
import spock.lang.Unroll

class StopSequenceReaderSpec extends Specification {

  @Unroll
  def 'read until "#stopSequence" found in "#input" should produce "#result"'() {
    expect:
    createReader(input).readUntil(stopSequence) == result

    where:
    input         | stopSequence | result
    'hello world' | 'lo'         | 'hello'
    'hello world' | 'ld'         | 'hello world'
    'hello world' | 'w'          | 'hello w'
    'hello world' | 'h'          | 'h'
    'hello world' | 'd'          | 'hello world'
  }

  def 'when stop sequence is not found, exception is thrown'() {
    when:
    createReader('hello world').readUntil('x')

    then:
    thrown EOFException
  }

  def 'peeking multiple times will advance the reader, but peeked characters will still be read via nextCharacter() calls'() {
    given:
    def reader = createReader('hello world')

    expect:
    reader.peek() == (char) 'h'
    reader.peek() == (char) 'e'
    reader.nextCharacter() == (char) 'h'
    reader.nextCharacter() == (char) 'e'
  }

  def 'peek next upcoming will repeatedly return the next upcoming character only'() {
    given:
    def reader = createReader('hello world')

    expect:
    reader.peekNextUpcomingOnly() == (char) 'h'
    reader.peekNextUpcomingOnly() == (char) 'h'
    reader.nextCharacter() == (char) 'h'
  }

  def 'munching whitespace multiple times in a row will not advance the reader past the next non-whitespace character'() {
    given:
    def reader = createReader('  hello world')

    when:
    reader.munchWhitespace()
    reader.munchWhitespace()

    then:
    reader.nextCharacter() == (char) 'h'
  }

  private static StopSequenceReader createReader(String input) {
    Reader reader = new InputStreamReader(new ByteArrayInputStream(input.bytes))
    return new StopSequenceReader(reader)
  }

}
