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

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln()) {
            "1" -> {
                while (true) {
                    val unlearnedWords = dictionary.filter { it.correctAnswersCount < 3 }
                    if (unlearnedWords.isEmpty()) return println("Вы выучили все слова в базе")

                    unlearnedWords.shuffled()
                    val nextLearningWords = unlearnedWords.take(4)
                    val nextLearningWord = nextLearningWords.random()
                    val wordsForLearning = nextLearningWords.shuffled()

                    println(
                        """
                        ${nextLearningWord.original}
                        1. ${wordsForLearning[0].translate}
                        2. ${wordsForLearning[1].translate}
                        3. ${wordsForLearning[2].translate}
                        4. ${wordsForLearning[3].translate}
                        
                        0. Выход в меню
                        Введите номер правильного ответа или 0 для выхода в меню:
                    """.trimIndent()
                    )
                    when (readln()) {
                        "1" -> {
                            checkAnswer(wordsForLearning[0].translate, nextLearningWord)
                            saveDictionaryToFile(dictionary, wordsFile)
                        }

                        "2" -> {
                            checkAnswer(wordsForLearning[1].translate, nextLearningWord)
                            saveDictionaryToFile(dictionary, wordsFile)
                        }

                        "3" -> {
                            checkAnswer(wordsForLearning[2].translate, nextLearningWord)
                            saveDictionaryToFile(dictionary, wordsFile)
                        }

                        "4" -> {
                            checkAnswer(wordsForLearning[3].translate, nextLearningWord)
                            saveDictionaryToFile(dictionary, wordsFile)
                        }

                        "0" -> break
                        else -> continue
                    }
                }
            }

            "2" -> {
                println("Статистика:")
                val learnedWords = dictionary.count { it.correctAnswersCount >= 3 }
                val percentLearnedWords = ((dictionary.count() * learnedWords.toDouble()) / 100)
                println("Выучено $learnedWords из ${dictionary.count()} слов | $percentLearnedWords%")
            }

            "0" -> return
            else -> println("введите 1, 2 или 0")
        }
    }
}

fun saveDictionaryToFile(dictionary: MutableList<Word>, file: File) {
    file.writeText("")
    for (word in dictionary) {
        file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
    }
}

fun checkAnswer(userAnswer: String, nextLearningWord: Word) {
    if (nextLearningWord.translate == userAnswer) {
        nextLearningWord.correctAnswersCount++
        println("Правильно!\n")
    } else println("Неверно - ${nextLearningWord.original} - ${nextLearningWord.translate}\n")
}