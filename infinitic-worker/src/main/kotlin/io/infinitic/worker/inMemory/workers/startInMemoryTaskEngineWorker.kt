/**
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined
 * below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the
 * License will not include, and the License does not grant to you, the right to
 * Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights
 * granted to you under the License to provide to third parties, for a fee or
 * other consideration (including without limitation fees for hosting or
 * consulting/ support services related to the Software), a product or service
 * whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also
 * include this Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */

package io.infinitic.worker.inMemory

import io.infinitic.common.storage.keyValue.KeyValueStorage
import io.infinitic.monitoring.perName.engine.transport.MonitoringPerNameMessageToProcess
import io.infinitic.tasks.engine.storage.events.NoTaskEventStorage
import io.infinitic.tasks.engine.storage.states.TaskStateKeyValueStorage
import io.infinitic.tasks.engine.transport.TaskEngineInput
import io.infinitic.tasks.engine.transport.TaskEngineMessageToProcess
import io.infinitic.tasks.engine.worker.startTaskEngine
import io.infinitic.tasks.executor.transport.TaskExecutorMessageToProcess
import io.infinitic.worker.inMemory.transport.InMemoryTaskEngineOutput
import io.infinitic.workflows.engine.transport.WorkflowEngineMessageToProcess
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private const val N_WORKERS = 10

fun CoroutineScope.startInMemoryTaskEngineWorker(
    keyValueStorage: KeyValueStorage,
    taskEngineCommandsChannel: Channel<TaskEngineMessageToProcess>,
    taskEngineEventsChannel: Channel<TaskEngineMessageToProcess>,
    taskEngineResultsChannel: Channel<TaskEngineMessageToProcess>,
    taskExecutorChannel: Channel<TaskExecutorMessageToProcess>,
    monitoringPerNameChannel: Channel<MonitoringPerNameMessageToProcess>,
    workflowEngineEventsChannel: Channel<WorkflowEngineMessageToProcess>
) = launch {

    launch(CoroutineName("task-engine-message-acknowledger")) {
        for (result in taskEngineResultsChannel) {
            println("TASK_ENGINE: ${result.message}")
            // no message acknowledging for inMemory implementation
            if (result.exception != null) {
                println(result.exception)
            }
        }
    }

    // Starting Task Engine

    startTaskEngine(
        "task-engine",
        TaskStateKeyValueStorage(keyValueStorage),
        NoTaskEventStorage(),
        TaskEngineInput(
            taskEngineCommandsChannel,
            taskEngineEventsChannel,
            taskEngineResultsChannel
        ),
        InMemoryTaskEngineOutput(
            this,
            taskEngineEventsChannel,
            taskExecutorChannel,
            monitoringPerNameChannel,
            workflowEngineEventsChannel
        )
    )
}