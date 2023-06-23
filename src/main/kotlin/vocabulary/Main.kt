package vocabulary

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(line[0], line[1], line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

    while (true) {
        when (readln()) {
            "1" -> println("Учить слова")
            "2" -> {
                println("Статистика:")
                val learnedWords = dictionary.count { it.correctAnswersCount!! >= 3 }
                val percentLearnedWords = ((dictionary.count() * learnedWords.toDouble()) / 100)
                println("Выучено $learnedWords из ${dictionary.count()} слов | $percentLearnedWords%")
            }

            "0" -> return
            else -> println("введите 1, 2 или 0")
        }
    }
}