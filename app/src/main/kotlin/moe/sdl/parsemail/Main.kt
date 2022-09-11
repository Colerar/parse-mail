package moe.sdl.parsemail

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import okio.buffer
import okio.source
import org.simplejavamail.api.email.Email
import org.simplejavamail.converter.EmailConverter
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = MainCmd().main(args)

class MainCmd : CliktCommand(name = "parse-mail") {
  val workDir by option("-D").file(mustExist = true, canBeFile = false)

  val inputDir by option("--input", "-i")
    .file(mustExist = true, canBeFile = false, mustBeReadable = true).default(File("."))

  val maxDepth by option("--depth", "-d").int().default(1)

  val outputFile by option("--output", "-o")
    .file(canBeDir = false, mustBeWritable = true).default(File("./result.csv"))

  val mailExtensions = listOf("eml", "msg")

  val parallelFileRead by option("--parallel-file-read", "--parallel", "-P").int().default(30)

  val print by option("--print", "-p").flag().help("Also print the result email summary")

  override fun run() = runBlocking {
    val limit = Semaphore(parallelFileRead)
    workDir?.also {
      System.setProperty("user.dir", it.normalize().absolutePath)
    }

    val summaries = Collections.synchronizedSet(mutableSetOf<MailSummary>())
    inputDir.walkTopDown().maxDepth(maxDepth).filter { file ->
      mailExtensions.any { file.extension.equals(it, ignoreCase = true) }
    }.asFlow().mapNotNull { file ->
      async {
        limit.withPermit {
          val bytes by lazy(LazyThreadSafetyMode.NONE) { file.source().buffer().use { it.readByteArray() } }
          when (file.extension.lowercase()) {
            "eml" -> EmailConverter.emlToEmail(bytes.inputStream())
            "msg" -> EmailConverter.outlookMsgToEmail(bytes.inputStream())
            else -> {
              logger.error { "Unexpected format with the file: ${file.toPath()}" }
              null
            }
          }?.toSummary()
        }
      }
    }.catch {
      logger.error(it) { "Failed to parse file" }
    }.collect {
      val summary = it.await()
      if (summary != null) summaries.add(summary)
    }

    if (summaries.isEmpty()) {
      logger.info { "No input files, exit..." }
      return@runBlocking
    }

    outputFile.parentFile?.mkdirs()
    if (outputFile.exists()) outputFile.delete()
    // write UTF-8 BOM for best office support
    outputFile.writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    csvWriter().openAsync(outputFile, append = true) {
      writeRow(
        "subject",
        "fromName",
        "fromEmail",
        "toName",
        "toEmail",
        "sentDateSecs",
      )
      summaries.forEach {
        if (print) {
          logger.info { "$it" }
        }
        writeRow(
          it.subject,
          it.fromName,
          it.fromEmail,
          it.toName,
          it.toEmail,
          it.sentDateSecs
        )
      }
    }

    logger.info { "Successfully write result csv to ${outputFile.toPath()}" }
  }
}

fun Email.toSummary() =
  MailSummary(
    subject = subject,
    fromName = fromRecipient?.name,
    fromEmail = fromRecipient?.address,
    toName = replyToRecipient?.name,
    toEmail = replyToRecipient?.address,
    sentDateSecs = sentDate?.toInstant()?.toEpochMilli()?.div(1000),
  )

@Serializable
data class MailSummary(
  val subject: String?,
  val fromName: String?,
  val fromEmail: String?,
  val toName: String?,
  val toEmail: String?,
  val sentDateSecs: Long?,
)
