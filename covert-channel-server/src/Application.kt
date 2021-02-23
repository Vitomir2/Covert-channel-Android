package nl.utwente.ewi.scs.syssec

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.io.File
import kotlin.random.Random

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val staticfilesDir = File("static")
    require(staticfilesDir.exists()) { "Cannot find ${staticfilesDir.absolutePath}" }


    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/randomcat") {
            val files = staticfilesDir.listFiles()
            val ind = Random.nextInt(files.size)
            call.respondFile(files[ind])
        }

        post("/randomcat/{pin}") {
            val files = staticfilesDir.listFiles()
            val ind = Random.nextInt(files.size)
            call.respondFile(files[ind])

            val pin = call.parameters["pin"]
            println("The pin entered by the user is: $pin")
        }
    }
}

