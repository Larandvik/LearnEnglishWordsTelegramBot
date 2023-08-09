package bot.lav

import kotlinx.serialization.Serializable
import java.io.File
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException



class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4

) {

    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { it.correctAnswersCount >= 3 }.size
        val total = dictionary.size
        val percent = learned * 100 / total
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) +
                    learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }

            val dictionary = mutableListOf<Word>()
            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        for (word in dictionary) {
            wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount }
        saveDictionary()
    }
}

class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,


) {
    override fun toString(): String {
        return "Выучено $learned из $total слов | $percent%"
    }
}

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)
@Serializable
data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)