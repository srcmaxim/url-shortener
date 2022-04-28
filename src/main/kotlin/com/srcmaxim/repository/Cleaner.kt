package com.srcmaxim.repository

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.max

class Cleaner(
    private val kvDatabase: ConcurrentHashMap<String, String>
) {

    private val expireAtStore = ConcurrentSkipListMap<Long, String>()

    init {
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
        }
    }

    private suspend fun sleepToNextDuration() {
        val expireAtMinDuration = 100L
        val firstEntry = expireAtStore.firstEntry()
        if (firstEntry != null) {
            val expireAt = firstEntry.key
            var sleep = expireAt - System.currentTimeMillis()
            sleep = max(sleep, expireAtMinDuration)
            delay(sleep)
        } else {
            // ignore, no value in set
            delay(expireAtMinDuration)
        }
    }
}
