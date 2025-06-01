package me.bottdev.lumencore.handlers

import kotlinx.coroutines.*
import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.messages.types.AckMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper


fun interface AckSuccessHandlerBlock<T : IMessageWrapper> {
    fun handle(wrapper: T, ackMessage: AckMessage)
}


fun interface AckTimeoutHandlerBlock<T : IMessageWrapper> {
    fun handle(wrapper: T)
}

fun interface AckResendHandlerBlock<T : IMessageWrapper> {
    fun handle(wrapper: T, iteration: Int)
}

class AckHandler {

    class Task<T : IMessageWrapper>(
        val io: IMessageIO,
        val wrappedMessage: T,
        val timeOut: Long = -1,
        var resendTimes: Int = 0,
        val onSuccess: AckSuccessHandlerBlock<T>? = null,
        val onTimeout: AckTimeoutHandlerBlock<T>? = null,
        val onResend: AckResendHandlerBlock<T>? = null,
    ) {

        private val maxResendTimes = resendTimes

        val canResend: Boolean
            get() = resendTimes > 0

        val deferredAckMessage = CompletableDeferred<AckMessage>()

        fun complete(ackMessage: AckMessage) {
            deferredAckMessage.complete(ackMessage)
        }

        fun success(ackMessage: AckMessage) {
            onSuccess?.handle(wrappedMessage, ackMessage)
        }

        fun timeout() {
            onTimeout?.handle(wrappedMessage)
        }

        fun resend() {
            resendTimes--
            io.sendWrapper(wrappedMessage)
            onResend?.handle(wrappedMessage, maxResendTimes - resendTimes)
        }

    }

    private val registeredTasks = mutableMapOf<String, Task<out IMessageWrapper>>()

    private fun isRegistered(id: String): Boolean =
        registeredTasks.containsKey(id)

    fun get(id: String): Task<*>? = registeredTasks[id]

    fun <T : IMessageWrapper> register(id: String, task: Task<T>) {
        if (isRegistered(id)) return
        registeredTasks[id] = task
        startTask(id, task)
    }

    private fun <T : IMessageWrapper> startTask(id: String, task: Task<T>) {

        val timeOut = task.timeOut

        if (timeOut <= 0) return

        CoroutineScope(Dispatchers.Default).launch {

            try {

                val ackMessage = withTimeout(timeOut) {
                    task.deferredAckMessage.await()
                }
                task.success(ackMessage)

            } catch (ex: TimeoutCancellationException) {

                if (task.canResend) {
                    task.resend()
                    startTask(id, task)

                } else {
                    task.timeout()
                    unregister(id)
                }

                cancel()

            }

        }

    }

    fun unregister(id: String) {
        registeredTasks.remove(id)
    }

    fun complete(ackMessage: AckMessage) {
        val id = ackMessage.messageId
        val task = get(id)
        task?.complete(ackMessage)
        unregister(id)
    }

}