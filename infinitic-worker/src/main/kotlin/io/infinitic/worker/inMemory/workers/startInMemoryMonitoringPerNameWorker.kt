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
import io.infinitic.monitoring.global.engine.transport.MonitoringGlobalMessageToProcess
import io.infinitic.monitoring.perName.engine.storage.MonitoringPerNameStateKeyValueStorage
import io.infinitic.monitoring.perName.engine.transport.MonitoringPerNameInput
import io.infinitic.monitoring.perName.engine.transport.MonitoringPerNameMessageToProcess
import io.infinitic.monitoring.perName.engine.worker.startMonitoringPerNameEngine
import io.infinitic.worker.inMemory.transport.InMemoryMonitoringPerNameOutput
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private const val N_WORKERS = 10

fun CoroutineScope.startInMemoryMonitoringPerNameWorker(
    keyValueStorage: KeyValueStorage,
    monitoringPerNameChannel: Channel<MonitoringPerNameMessageToProcess>,
    monitoringPerNameResultsChannel: Channel<MonitoringPerNameMessageToProcess>,
    monitoringGlobalChannel: Channel<MonitoringGlobalMessageToProcess>
) = launch {

    launch(CoroutineName("monitoring-per-name-message-acknowledger")) {
        for (result in monitoringPerNameResultsChannel) {
            println("MONITORING_PER_NAME: ${result.message}")
            // no message acknowledging for inMemory implementation
            if (result.exception != null) {
                println(result.exception)
            }
        }
    }

    startMonitoringPerNameEngine(
        "monitoring-per-name-engine",
        MonitoringPerNameStateKeyValueStorage(keyValueStorage),
        MonitoringPerNameInput(
            monitoringPerNameChannel,
            monitoringPerNameResultsChannel
        ),
        InMemoryMonitoringPerNameOutput(
            this,
            monitoringGlobalChannel
        )
    )
}