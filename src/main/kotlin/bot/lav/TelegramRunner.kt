package bot.lav

import bot.lav.TelegramBotService.Companion.CALLBACK_DATA_ANSWER_PREFIX
import bot.lav.TelegramBotService.Companion.LEARN_WORDS
import bot.lav.TelegramBotService.Companion.STATISTICS

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0

    val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer()
    val statistics_clicked = trainer.getStatistics().toString()

    while (true) {
        val botService = TelegramBotService()

        Thread.sleep(2000)
        val updates: String = botService.getUpdates(botToken, lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1

        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "/start") {
            botService.sendMenu(botToken, chatId)
        }
        if (data?.lowercase() == STATISTICS) {
            botService.sendMessage(botToken, chatId, statistics_clicked)
            botService.sendMenu(botToken, chatId)
        }
        if (data?.lowercase() == LEARN_WORDS) {
            botService.checkNextQuestionAndSend(trainer, botToken, chatId)
        }
        if (data?.lowercase().toString().startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            val userAnswer = data.toString().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswer)) {
                botService.sendMessage(botToken, chatId, "Правильно")
            } else {
                botService.sendMessage(
                    botToken,
                    chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} " + "- ${trainer.question?.correctAnswer?.translate}"
                )
            }
            botService.checkNextQuestionAndSend(trainer, botToken, chatId)
        }
    }
}


