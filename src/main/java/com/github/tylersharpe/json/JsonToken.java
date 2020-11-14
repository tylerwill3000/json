package com.github.tylersharpe.json;

import java.util.HashMap;
import java.util.Map;

public enum JsonToken {
    START_OBJECT('{'),
    END_OBJECT('}'),
    START_ARRAY('['),
    END_ARRAY(']'),
    QUOTE('"'),
    COMMA(','),
    COLON(':'),
    NULL('n'),
    TRUE('t'),
    FALSE('f'),
    NUMBER(null);

    private static final Map<Character, JsonToken> INDEX = new HashMap<>();

    static {
        for (JsonToken t : values()) {
            INDEX.put(t.character, t);
        }
    }

    char character;

    JsonToken(Character character) {
        if (character != null) {
            this.character = character;
        }
    }

    public static JsonToken fromCharacter(char theChar) {
        if (INDEX.containsKey(theChar)) {
            return INDEX.get(theChar);
        }
        if (theChar == '-' || theChar == '.' || Character.isDigit(theChar)) {
            return NUMBER;
        }
        return null;
    }

}
