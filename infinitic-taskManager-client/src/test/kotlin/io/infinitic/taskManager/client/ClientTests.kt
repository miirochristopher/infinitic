package io.infinitic.taskManager.client

import io.infinitic.messaging.api.dispatcher.Dispatcher
import io.infinitic.taskManager.common.data.TaskId
import io.infinitic.taskManager.common.data.TaskInput
import io.infinitic.taskManager.common.data.TaskMeta
import io.infinitic.taskManager.common.data.TaskName
import io.infinitic.taskManager.common.data.TaskOptions
import io.infinitic.taskManager.common.messages.DispatchTask
import io.infinitic.taskManager.common.messages.ForTaskEngineMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot

class ClientTests : StringSpec({
    val dispatcher = mockk<Dispatcher>()
    val slot = slot<ForTaskEngineMessage>()
    coEvery { dispatcher.toTaskEngine(capture(slot)) } just Runs
    val client = Client(dispatcher)

    beforeTest {
        slot.clear()
    }

    "Should be able to dispatch method without parameter" {
        // when
        val task = client.dispatchTask<FakeTask> { m1() }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured
        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf())
        )
    }

    "Should be able to dispatch a method with a primitive as parameter" {
        // when
        val task = client.dispatchTask<FakeTask> {
            m1(0)
        }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured
        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(0),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf(Int::class.java.name))
        )
    }

    "Should be able to dispatch a method with null as parameter" {
        // when
        val task = client.dispatchTask<FakeTask> {
            m1(null)
        }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured
        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(null),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf(String::class.java.name))
        )
    }

    "Should be able to dispatch a method with multiple definition" {
        // when
        val task = client.dispatchTask<FakeTask> { m1("a") }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured
        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput("a"),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf(String::class.java.name))
        )
    }

    "Should be able to dispatch a method with multiple parameters" {
        // when
        val task = client.dispatchTask<FakeTask> { m1(0, "a") }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured
        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(0, "a"),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf(Int::class.java.name, String::class.java.name))
        )
    }

    "Should be able to dispatch a method with an interface as parameter" {
        // when
        val fake = FakeClass()
        val task = client.dispatchTask<FakeTask> { m1(fake) }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured

        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(fake),
            taskName = TaskName("${FakeTask::class.java.name}::m1"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf(FakeInterface::class.java.name))
        )
    }

    "Should be able to dispatch a method with a primitive as return value" {
        // when
        val task = client.dispatchTask<FakeTask> { m2() }
        // then
        slot.isCaptured shouldBe true
        val msg = slot.captured

        msg shouldBe DispatchTask(
            taskId = task.taskId,
            taskInput = TaskInput(),
            taskName = TaskName("${FakeTask::class.java.name}::m2"),
            taskOptions = TaskOptions(),
            taskMeta = TaskMeta().withParameterTypes(listOf())
        )
    }

    // TODO: add tests for cancel method

    // TODO: add tests for retry method

    // TODO: add tests for options

    // TODO: add tests for meta

    // TODO: add tests for error cases
})

private interface FakeInterface
private data class FakeClass(val i: Int ? = 0) : FakeInterface

private interface FakeTask {
    fun m1()
    fun m1(i: Int): String
    fun m1(str: String?): Any?
    fun m1(p1: Int, p2: String): String
    fun m1(id: FakeInterface): TaskId
    fun m2(): Boolean
}