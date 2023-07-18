package bot.lav

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0

    val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer()
    val statistics_clicked = trainer.getStatistics().toString()
    val question = trainer.getNextQuestion()

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
        if (data?.lowercase() == "statistics_clicked") {
            botService.sendMessage(botToken, chatId, statistics_clicked)
        }
        if (data?.lowercase() == "learn_words_clicked") {
            if (question == null) {
                botService.sendMessage(botToken, chatId, "Вы выучили все слова в базе")
            } else {
                botService.sendQuestion(botToken, chatId, question)
            }
        }
        if (data?.lowercase().toString().startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            val userAnswer = data.toString().substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswer)) {
                botService.sendMessage(botToken, chatId, "Правильно")
            } else {
                botService.sendMessage(botToken, chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} " +
                            "- ${trainer.question?.correctAnswer?.translate}")
            }
            botService.checkNextQuestionAndSend(trainer, botToken, chatId)
        }
    }
}


