package com.github.tylerwilliams.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tylerwilliams.json.util.TypeToken
import com.google.gson.Gson

import java.lang.reflect.Type

class Benchmarks {

    static final int ITERATIONS = 15
    static final int MULTIPLIER = 10

    static final Json json = new Json()
    static final Gson gson = new Gson()
    static final ObjectMapper mapper = new ObjectMapper()

    static final Type listOfPhotosType = new TypeToken<List<Photo>>() {}.type

    @SuppressWarnings("unused")
    static class Photo {
        int albumId
        int id
        String title
        URL url
        URL thumbnailUrl
    }

    static void main(String[] args) {
        def photosUrl = new URL("https://jsonplaceholder.typicode.com/photos")
        List<Photo> photos = (List<Photo>) photosUrl.withInputStream { stream -> json.parse(stream, listOfPhotosType) }
        photos = (1..MULTIPLIER).collectMany { photos }
        byte[] photosBytes = json.serialize(photos).bytes
        Closure<InputStream> newPhotosStream = { new ByteArrayInputStream(photosBytes) }

        println "Working with ${photos.size()} photos"

        println "TYPED READ\nJackson | GSON | JSON"
        ITERATIONS.times {
            long jacksonRead = time { mapper.readValue(newPhotosStream(), new TypeReference<List<Photo>>() {}) }
            long gsonRead = time { gson.fromJson(new InputStreamReader(newPhotosStream()), listOfPhotosType) }
            long jsonRead = time { json.parse(newPhotosStream(), listOfPhotosType) }

            println String.format("%-7d | %-4d | %d", jacksonRead, gsonRead, jsonRead)
        }

        println "\nWRITE\nJackson | GSON | JSON"
        ITERATIONS.times {
            long jacksonWrite = time { mapper.writeValueAsString(photos) }
            long gsonWrite = time { gson.toJson(photos) }
            long jsonWrite = time { json.serialize(photos) }

            println String.format("%-7d | %-4d | %d", jacksonWrite, gsonWrite, jsonWrite)
        }
    }

    static long time(Closure action) {
        long start = System.currentTimeMillis()
        action()
        return System.currentTimeMillis() - start
    }

}
