package org.ucb.appp1.scheduler

actual class LogScheduler actual constructor(context: Any?) {
    actual fun schedulePeriodicalUpload() {
        println("WorkManager no está disponible en iOS")
    }
}
