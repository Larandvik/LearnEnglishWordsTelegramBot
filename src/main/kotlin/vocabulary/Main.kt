package vocabulary

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(line[0], line[1])
        dictionary.add(word)
    }
// оставил код, чтобы показать как обновил файл
//    val newWords = mutableListOf<String>()
//    for (word in dictionary) {
//        newWords.add("${word.original}|${word.translate}|${word.correctAnswersCount}")
//    }
//
//    wordsFile.writeText("")
//
//    for (word in newWords) {
//        wordsFile.appendText(word)
//        wordsFile.appendText("\n")
//    }

    for (word in dictionary) {
        println(word)
    }
}