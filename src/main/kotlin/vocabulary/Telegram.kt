package vocabulary

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        val botService = TelegramBotService()

        Thread.sleep(2000)
        val updates: String = botService.getUpdates(botToken, updateId)
        println(updates)

        val stringUpdateIdRegex = "\"update_id\":(.+),".toRegex()
        val matchResultUpdateId = stringUpdateIdRegex.find(updates)
        val groupsUpdateId = matchResultUpdateId?.groups
        val updateIdString = groupsUpdateId?.get(1)?.value ?: continue

        val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value ?: continue

        val stringChatIdRegex = "\"chat\":\\{\"id\":(.+),\"f".toRegex()
        val matchResultChatId = stringChatIdRegex.find(updates)
        val groupsChatId = matchResultChatId?.groups
        val chatIdString = groupsChatId?.get(1)?.value ?: continue
        val chatId = chatIdString.toInt()

        botService.sendMessage(botToken, chatId, text)

        updateId = updateIdString.toInt() + 1
    }
}


