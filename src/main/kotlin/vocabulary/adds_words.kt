package vocabulary

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    for (word in wordsFile.readLines()) {
        println(word)
    }
}