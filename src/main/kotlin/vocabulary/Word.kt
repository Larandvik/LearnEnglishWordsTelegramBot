package vocabulary

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)