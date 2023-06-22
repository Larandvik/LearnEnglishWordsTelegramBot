package vocabulary

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(line[0], line[1],null ?: 0 )
        dictionary.add(word)
    }

    for (word in dictionary) {
        println(word)
    }
}