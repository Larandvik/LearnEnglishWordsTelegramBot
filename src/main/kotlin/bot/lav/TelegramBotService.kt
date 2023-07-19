package bot.lav

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService {

    fun sendMessage(botToken: String, chatId: Int, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getUpdates(botToken: String, updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(botToken: String, chatId: Int): String {
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
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

    fun sendQuestion(botToken: String, chatId: Int, question: Question): String {
        val urlSendQuestion = "https://api.telegram.org/bot$botToken/sendMessage"
        val variantsButtons = question.variants.mapIndexed { index, word ->
            """
            {
                "text": "${word.translate}",
                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + index}
            }
            """
        }.joinToString(",")

        val sendQuestionBody = """
            {
            	"chat_id": $chatId,
            	"text": ${question.correctAnswer},
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

    }
}

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"