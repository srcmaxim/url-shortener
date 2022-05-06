package com.srcmaxim.repository

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import kotlin.math.min

class Cleaner(
    private val invalidateByKey: Consumer<String>
) {

    private val expireAtStore = ConcurrentSkipListMap<Long, CopyOnWriteArrayList<String>>()

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            while (true) {
                cleanerRoutine()
            }
        }
    }

    fun addExpire(ttl: Int, key: String) {
        val expireAt = ttl + System.currentTimeMillis()
        val keys = expireAtStore.computeIfAbsent(expireAt) { CopyOnWriteArrayList() }
        keys.add(key)
    }

    private suspend fun cleanerRoutine() {
        val time = System.currentTimeMillis()
        removeExpired(time)
        sleepToNextDuration(time)
    }

    private fun removeExpired(time: Long) {
        val expired = expireAtStore.headMap(time, true)
        for (ttlKey in expired) {
            ttlKey.value.forEach(invalidateByKey)
        }
        expired.clear()
    }

    private suspend fun sleepToNextDuration(time: Long) {
        val expireAtMinDuration = 100L
        val firstEntry = expireAtStore.firstEntry()
        if (firstEntry != null) {
            val expireAt = firstEntry.key
            var sleep = expireAt - time
            sleep = min(sleep, expireAtMinDuration)
            delay(sleep)
        } else {
            // ignore, no value in set
            delay(expireAtMinDuration)
        }
    }
}
