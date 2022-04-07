package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.system.exitProcess

object Cryptograph {
    private lateinit var inputFile: File
    private lateinit var outputFile: File
    private lateinit var image: BufferedImage
    private lateinit var message: ByteArray
    private lateinit var password: ByteArray

    fun run() {
        while (true) {
            println("Task (hide, show, exit):")
            when (val input = readln()) {
                "hide" -> hide()
                "show" -> show()
                "exit" -> {
                    println("Bye!")
                    terminateProcess()
                }
                else -> println("Wrong task: $input")
            }
        }
    }

    private fun show() {
        println("Input image file:")
        inputFile = File(readln())
        validateFiles()

        println("Password:")
        password = readln().encodeToByteArray()

        println("Message:")

        println(decryptMessage(decodeMessage()))

    }

    private fun decryptMessage(message: ByteArray): String {
        message.forEachIndexed { index, byte -> run {
            message[index] = (byte xor password[index % password.size])
            }
        }

        return message.map { it.toInt().toChar() }.joinToString("")
    }

    private fun decodeMessage(): ByteArray {
        val bitsList = mutableListOf<Int>()

        for (h in 0 until image.height) {
            for (w in 0 until image.width) {
                bitsList.add(Color(image.getRGB(w, h)).blue and 1)
            }
        }

        val bitString = bitsList.joinToString("")

        val messageBits = bitString.substring(0, bitString.indexOf("0".repeat(22) + "11"))

        return messageBits.chunked(8).map { it.toInt(2).toByte() }.toByteArray()
    }

    private fun terminateProcess() {
        exitProcess(0)
    }

    private fun hide() {
        println("Input image file:")
        inputFile = File(readLine()!!)
        println("Output image file:")
        outputFile = File(readLine()!!)

        if (!validateFiles()) return

        println("Message to hide:")
        message = readln().encodeToByteArray()

        println("Password:")
        password = readln().encodeToByteArray()

        if (!validateMessage()) return

        encryptMessage()

        hideMessage()

        saveImage()
    }

    private fun encryptMessage() {
        message.forEachIndexed { index, byte -> message[index] = (byte xor password[index % password.size]) }
        message = message.plus(0.toByte()).plus(0.toByte()).plus(3.toByte())
    }

    private fun validateMessage(): Boolean {
        if (message.plus(0.toByte()).plus(0.toByte()).plus(3.toByte()).size * 8 > image.width * image.height) {
            println("The input image is not large enough to hold this message.")
            return false
        }

        return true
    }

    private fun saveImage() {
        ImageIO.write(image, "png", outputFile)

        println("Message saved in ${outputFile.name} image.")
    }

    private fun hideMessage() {
        var i = 0

        for (h in 0 until image.height) {
            for (w in 0 until image.width) {

                if (i >= message.size * 8) return
                val pixel = Color(image.getRGB(w, h))
                val red = pixel.red
                val green = pixel.green
                val blue = pixel.blue
                val newPixel = Color(red, green, blue and 254 or getBit(message[i / 8].toInt(), 7 - i % 8) % 256)
                image.setRGB(w, h, newPixel.rgb)
                i++
            }
        }
    }

    private fun validateFiles(): Boolean {
        if (!inputFile.exists()) {
            println("Can't read input file!")
            return false
        }

        image = ImageIO.read(inputFile)
        return true
    }

    private fun getBit(value: Int, position: Int): Int {
        return (value shr position) and 1
    }
}

fun main() {
    Cryptograph.run()
}

