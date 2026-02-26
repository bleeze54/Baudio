package tools

fun randomString() : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')+"="+"+"+"*"+"$"+"%"+"^"+"?"+"!"+"-"+"&"+"ç"+"@"+" "
    //return (1..length)
    return (1..150)
        .map { allowedChars.random() }
        .joinToString("")
}