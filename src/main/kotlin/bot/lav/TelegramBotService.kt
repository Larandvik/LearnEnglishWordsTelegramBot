package bot.lav

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService {

    companion object {
        const val LEARN_WORDS = "learn_words_clicked"
        const val STATISTICS = "statistics_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        const val URL_API_TELEGRAM = "http://api.telegram.org/bot"
    }

    fun sendMessage(botToken: String, chatId: Int, text: String): String {
        val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8)
        println(encoded)
        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getUpdates(botToken: String, updateId: Int): String {
        val urlGetUpdates = "$URL_API_TELEGRAM$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(botToken: String, chatId: Int): String {
        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage"
        val sendMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "Изучать слова",
            					"callback_data": "learn_words_clicked"
            				},
            				{
            					"text": "Статистика",
            					"callback_data": "statistics_clicked"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(botToken: String, chatId: Int, question: Question): String {
        val urlSendQuestion = "$URL_API_TELEGRAM$botToken/sendMessage"
        val variantsButtons = question.variants.mapIndexed { index, word ->
            """
            {
                "text": "${word.translate}",
                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + index}"
            }
            """
        }.joinToString(",")

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

        println(sendQuestionBody)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: Int) {
        if (trainer.question == null) {
            sendMessage(botToken, chatId, "Вы выучили все слова в базе")
        } else {
            trainer.getNextQuestion()?.let { sendQuestion(botToken, chatId, it) }
        }
    }
}