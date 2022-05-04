package com.srcmaxim.repository

import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.min

class Cleaner(
    private val kvDatabase: ConcurrentHashMap<String, String>,
    private val bus: EventBus
) {

    private val expireAtStore = ConcurrentSkipListMap<Long, String>()

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            while (true) {
                cleanerRoutine()
            }
        }
    }

    fun addExpire(expireAt: Long, key: String) {
        expireAtStore[expireAt] = key
    }

    private suspend fun cleanerRoutine() {
        val time = System.currentTimeMillis()
        removeExpired(time)
        sleepToNextDuration()
    }

    private fun removeExpired(time: Long) {
        val expired = expireAtStore.headMap(time)
        for (ttlKey in expired) {
            kvDatabase.remove(ttlKey.value)
            expireAtStore.remove(ttlKey.key)
            bus.publish("invalidateShortToOriginUrl", ttlKey.value)
        }
    }

    private suspend fun sleepToNextDuration() {
        val expireAtMinDuration = 100L
        val firstEntry = expireAtStore.firstEntry()
        if (firstEntry != null) {
            val expireAt = firstEntry.key
            var sleep = expireAt - System.currentTimeMillis()
            sleep = min(sleep, expireAtMinDuration)
            delay(sleep)
        } else {
            // ignore, no value in set
            delay(expireAtMinDuration)
        }
    }
}
