package vocabulary

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
) {
    override fun toString(): String {
        return "$original - $translate, правильных ответов = $correctAnswersCount"
    }
}