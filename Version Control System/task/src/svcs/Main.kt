package svcs

import java.io.File
import java.security.MessageDigest

val separator: String = File.separator
val workingDirectory: String = System.getProperty ("user.dir")
val vcsDirectory = "${workingDirectory}${separator}vcs"
val configFilePath = "$vcsDirectory${separator}config.txt"
val indexFilePath = "$vcsDirectory${separator}index.txt"
val commitsDirectory = "$vcsDirectory${separator}commits"
val logFilePath = "$vcsDirectory${separator}log.txt"

const val helpText = "These are SVCS commands:\n" +
        "config     Get and set a username.\n" +
        "add        Add a file to the index.\n" +
        "log        Show commit logs.\n" +
        "commit     Save changes.\n" +
        "checkout   Restore a file."

fun hashFileContent(content: String): String {
    val digest = MessageDigest.getInstance("SHA-1").digest(content.toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun handleConfig(args: Array<String>) {
    val username = if (File(configFilePath).exists()) File(configFilePath).readText() else ""

    if (args.size == 1) {
        if (username == "") {
            println("Please, tell me who you are.")
        } else {
            println("The username is $username.")
        }
    } else {
        File(configFilePath).writeText(args[1])
        println("The username is ${args[1]}.")
    }
}

fun handleAdd(args: Array<String>) {
    val index = if (File(indexFilePath).exists()) File(indexFilePath).readLines() else emptyList()
    val fileNames = index.map { it.split(" ")[0] }
    val listOfFiles = File(workingDirectory).listFiles()!!.filter { it.isFile }.map { it.name }

    if (args.size == 1) {
        if (index.isEmpty()) {
            println("Add a file to the index.")
        } else {
            println("Tracked files:")
            for (fileName in fileNames) println(fileName)
        }
    } else {
        if (args[1] in listOfFiles) {
            if (args[1] in fileNames) {
                println("The file '${args[1]}' is already tracked.")
            } else {
                val path = "${workingDirectory}${separator}${args[1]}"
                val hash = hashFileContent(File(path).readText())
                File(indexFilePath).appendText("${args[1]} $hash\n")
                println("The file '${args[1]}' is tracked.")
            }
        } else {
            println("Can't find '${args[1]}'.")
        }
    }
}

fun handleLog() {
    val logContent = if (File(logFilePath).exists()) File(logFilePath).readText() else ""
    println(
        if (logContent == "") {
            "No commits yet."
        } else {
            logContent
        }
    )
}

fun createCommit(commitText: String) {
    val commitID = System.currentTimeMillis()
    val commitPath = "${commitsDirectory}${separator}$commitID"
    File(commitPath).mkdir()

    File(indexFilePath).writeText("")
    val listOfFiles = File(workingDirectory).listFiles()!!.filter { it.isFile }.map { it.name }
    for (fileName in listOfFiles) {
        val filePath = "${workingDirectory}${separator}$fileName"
        val content = File(filePath).readText()
        val hash = hashFileContent(content)
        File(indexFilePath).appendText("$fileName $hash\n")
        File(filePath).copyTo(File("$commitPath${separator}$fileName"))
    }

    val currentLog = if (File(logFilePath).exists()) File(logFilePath).readText() else ""
    val author = if (File(configFilePath).exists()) File(configFilePath).readText() else ""
    val newLog = "commit $commitID\n" +
            "Author: $author\n" +
            commitText

    if (currentLog == "") {
        File(logFilePath).writeText(newLog)
    } else {
        File(logFilePath).writeText("$newLog\n\n$currentLog")
    }
    println("Changes are committed.")
}

fun handleCommit(args: Array<String>) {
    if (args.size == 1) {
        println("Message was not passed.")
    } else {
        if (File(indexFilePath).exists()) {
            val index = File(indexFilePath).readLines()
            if (index.isEmpty()) {
                println("No files added to the index yet.")
            } else {
                val listOfCommits = File(commitsDirectory).list()
                if (listOfCommits!!.isEmpty()) {
                    createCommit(args[1])
                } else {
                    val fileNames = index.map { it.split(" ")[0] }
                    val hashes = index.map { it.split(" ")[1] }
                    var isThereChange = false
                    for (i in fileNames.indices) {
                        val path = "${workingDirectory}${separator}${fileNames[i]}"
                        val newHash = hashFileContent(File(path).readText())
                        if (hashes[i] != newHash) {
                            createCommit(args[1])
                            isThereChange = true
                            break
                        }
                    }
                    if (!isThereChange) println("Nothing to commit.")
                }
            }
        } else {
            println("No files added to the index yet.")
        }
    }
}

fun handleCheckout(args: Array<String>) {
    if (args.size == 1) {
        println("Commit id was not passed.")
    } else {
        val listOfCommits = File(commitsDirectory).list()
        if (listOfCommits!!.isEmpty() || args[1] !in listOfCommits) {
            println("Commit does not exist.")
        } else {
            val commitPath = "$commitsDirectory${separator}${args[1]}"
            File(commitPath).copyRecursively(File(workingDirectory), overwrite = true)
            println("Switched to commit ${args[1]}.")
        }
    }
}

fun main(args: Array<String>) {

    if (!File(vcsDirectory).exists()) File(vcsDirectory).mkdir()
    if (!File(commitsDirectory).exists()) File(commitsDirectory).mkdir()
    
    if (args.isEmpty()) {
        println(helpText)
    } else {
        when(args[0]) {
            "", "--help" -> println(helpText)
            "config" -> handleConfig(args)
            "add" -> handleAdd(args)
            "log" -> handleLog()
            "commit" -> handleCommit(args)
            "checkout" -> handleCheckout(args)
            else -> println("'${args[0]}' is not a SVCS command.")
        }
    }

}