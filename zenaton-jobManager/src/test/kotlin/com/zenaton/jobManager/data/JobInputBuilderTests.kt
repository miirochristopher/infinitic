package com.zenaton.jobManager.data

import com.zenaton.jobManager.states.AvroJobEngineState
import com.zenaton.jobManager.states.JobEngineState
import com.zenaton.jobManager.utils.TestFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

internal class JobInputBuilderTests : StringSpec({
    "JobInputBuilder should build correct JobInput" {
        // given
        val state = TestFactory.random(JobEngineState::class)
        val bytes = TestFactory.random(ByteArray::class)
        val buffer = TestFactory.random(ByteBuffer::class)
        val avro = TestFactory.random(AvroJobEngineState::class)
        // when
        val out = JobInput.builder()
            .add(null)
            .add(state)
            .add(bytes)
            .add(buffer)
            .add(avro)
            .build()
        // then
        val input = out.input
        input.size shouldBe 5
        input[0].isNull() shouldBe true
        input[1].get<JobEngineState>() shouldBe state
        input[2].getBytes() shouldBe bytes
        ByteBuffer.wrap(input[3].getBytes()) shouldBe buffer
        input[4].getAvro<AvroJobEngineState>() shouldBe avro
    }
})