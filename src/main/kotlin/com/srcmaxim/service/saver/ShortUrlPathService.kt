package com.srcmaxim.service.saver

import org.roaringbitmap.RoaringBitmap
import java.net.URI
import java.util.*
import java.util.random.RandomGenerator.SplittableGenerator
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ShortUrlPathService {

    private val base64Keys = RoaringBitmap()
    private val threadLocalRandom = getSplittableGenerator()
    private val maxBase64Path = 6

    fun getShortUrlPath(): URI {
        while (true) {
            val random = threadLocalRandom.get().nextInt()
            val bytes = byteArrayOf(
                random.ushr(24).toByte(),
                random.ushr(16).toByte(),
                random.ushr(8).toByte(),
                random.toByte()
            )
            if (base64Keys.checkedAdd(random)) {
                continue
            }
            val base64Path = Base64.getUrlEncoder().encodeToString(bytes)
            return URI.create(base64Path.substring(0, maxBase64Path))
        }
    }

    /**
     * Note on algorithm implementation:
     * 1. We do not need cryptography secure algorithm -- what we really need is
     * a very huge period* so our values won't repeat;
     * 2. Random generation of UUIDv4 happens in 4-8 rRandomPathGeneratorServiceeactive threads (depends on CPU),
     * so we know the exact amount of threads and we don't need huge state space;
     * 3. Linux uses /dev/random that implemented with ChaCha20 algorithm
     * and it has a period of 2^68 and statespace of 2^260;
     * Java uses SHA1PRNG that has period of 2^64;
     * For example L128X256MixRandom has a period of 2^128−1 and statespace of 2^384;
     * All these algorithms can pass random test TestU01;
     *
     * We can use algorithms:
     * - L64X128MixRandom, L64X256MixRandom, L64X1024MixRandom
     * - L128X128MixRandom, L128X256MixRandom, L128X1024MixRandom
     *
     * Notes*:
     * Period -- minimum amount of calls when values start repeating randomly;
     *
     * https://www.baeldung.com/java-secure-random
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/random/package-summary.html
     * https://stackoverflow.com/questions/4720822/what-is-the-best-pseudo-random-number-generator-as-of-today
     */
    private fun getSplittableGenerator(): ThreadLocal<SplittableGenerator> {
        val splittableGenerator = SplittableGenerator.of("L64X256MixRandom")
        return ThreadLocal.withInitial { splittableGenerator.split() }
    }
}
