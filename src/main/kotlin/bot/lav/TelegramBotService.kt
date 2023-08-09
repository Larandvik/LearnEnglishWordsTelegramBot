package bot.lav

import kotlinx.serialization.encodeToString
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json

class TelegramBotService {

    companion object {
        const val LEARN_WORDS_CLICKED = "learn_words_clicked"
        const val STATISTICS_CLICKED = "statistics_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        const val RESET_CLICKED = "reset_clicked"
        const val URL_API_TELEGRAM = "https://api.telegram.org/bot"
    }

    fun sendMessage(json: Json, botToken: String, chatId: Long, message: String): String {
        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getUpdates(botToken: String, updateId: Long): String {
        val urlGetUpdates = "$URL_API_TELEGRAM$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(json: Json, botToken: String, chatId: Long): String {
        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(listOf(
                    InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_CLICKED),
                    InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED),
                ),
                listOf(
                    InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                )
                    )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(json: Json, botToken: String, chatId: Long, question: Question): String {
        val urlSendQuestion = "$URL_API_TELEGRAM$botToken/sendMessage"
        val variantsButtons = question.variants.mapIndexed { index: Int, word: Word ->
            """
            {
                "text": "${word.translate}",
                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + index}"
            }
            """
        }.joinToString(",")
        println(variantsButtons)

        val sendQuestionBody = """
            {
            	"chat_id": $chatId,
            	"text": "${question.correctAnswer.questionWord}",
            	"reply_markup": {
            		"inline_keyboard": [
                    [
            			$variantsButtons
            		]
                ]
            }
        }
        """.trimIndent()

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.questionWord,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed {index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index}"
                    )
                })
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, botToken: String, chatId: Long) {
        if (trainer.getNextQuestion() == null) {
            sendMessage(json, botToken, chatId, "Вы выучили все слова в базе")
        } else {
            trainer.getNextQuestion()?.let { sendQuestion(json, botToken, chatId, it) }
        }
    }
}