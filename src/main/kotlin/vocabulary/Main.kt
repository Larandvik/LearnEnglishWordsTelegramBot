package vocabulary

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val count = if (line[2].isNotEmpty()) line[2].toInt() else 0
        val word = Word(line[0], line[1], count)
        dictionary.add(word)
    }

    for (word in dictionary) {
        println(word)
    }
}