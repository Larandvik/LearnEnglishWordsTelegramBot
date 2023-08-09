package bot.lav

import bot.lav.TelegramBotService.Companion.CALLBACK_DATA_ANSWER_PREFIX
import bot.lav.TelegramBotService.Companion.LEARN_WORDS_CLICKED
import bot.lav.TelegramBotService.Companion.RESET_CLICKED
import bot.lav.TelegramBotService.Companion.STATISTICS_CLICKED
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: Callback_query? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class Callback_query(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        val botService = TelegramBotService()
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(botToken, lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, botToken, trainers, botService) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    json: Json,
    botToken: String,
    trainers: HashMap<Long, LearnWordsTrainer>,
    botService: TelegramBotService,
) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }
    val statisticsClicked = trainer.getStatistics().toString()

    if (message?.lowercase() == "/start") {
        botService.sendMenu(json, botToken, chatId)
    }
    if (data?.lowercase() == STATISTICS_CLICKED) {
        botService.sendMessage(json, botToken, chatId, statisticsClicked)
        botService.sendMenu(json, botToken, chatId)
    }
    if (data?.lowercase() == LEARN_WORDS_CLICKED) {
        botService.checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }
    if (data?.lowercase().toString().startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
        val userAnswer = data.toString().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(userAnswer)) {
            botService.sendMessage(json, botToken, chatId, "Правильно")
        } else {
            botService.sendMessage(
                json,
                botToken,
                chatId,
                "Не правильно: ${trainer.question?.correctAnswer?.questionWord} " +
                        "- ${trainer.question?.correctAnswer?.translate}"
            )
        }
        botService.checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }

    if (data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        botService.sendMessage(json, botToken, chatId, "Прогресс сброшен")

    }
}


