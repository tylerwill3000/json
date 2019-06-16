package com.github.tylersharpe.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tylersharpe.json.util.TypeToken
import com.google.gson.Gson

class Benchmarks {

  static final int ITERATIONS = 10

  @SuppressWarnings("unused")
  static class Photo {
    int albumId
    int id
    String title
    URL url
    URL thumbnailUrl
  }

  static void main(String[] args) {
    def json = new Json()
    def gson = new Gson()
    def mapper = new ObjectMapper()

    def listOfPhotosType = new TypeToken<List<Photo>>(){}.type

    List<Photo> photos = (List<Photo>) json.parse(new URL("https://jsonplaceholder.typicode.com/photos"), listOfPhotosType)
    byte[] photosBytes = json.serialize(photos).bytes
    Closure<InputStream> newPhotosStream = { new ByteArrayInputStream(photosBytes) }

    println "Working with ${photos.size()} photos"

    println "READ\nJackson | GSON | JSON"
    ITERATIONS.times {
      long jacksonRead = time { mapper.readValue(newPhotosStream(), new TypeReference<List<Photo>>(){}) }
      long gsonRead = time { gson.fromJson(new InputStreamReader(newPhotosStream()), listOfPhotosType) }
      long jsonRead = time { json.parse(newPhotosStream(), listOfPhotosType) }

      println String.format("%-7d | %-4d | %d", jacksonRead, gsonRead, jsonRead)
    }

    println()
    println "WRITE\nJackson | GSON | JSON"
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
