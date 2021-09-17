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

package io.infinitic.common.workflows.data.commands

import com.fasterxml.jackson.annotation.JsonProperty
import io.infinitic.common.data.MillisDuration
import io.infinitic.common.data.MillisInstant
import io.infinitic.common.data.methods.MethodName
import io.infinitic.common.data.methods.MethodParameterTypes
import io.infinitic.common.data.methods.MethodParameters
import io.infinitic.common.serDe.SerializedData
import io.infinitic.common.tasks.data.TaskMeta
import io.infinitic.common.tasks.data.TaskName
import io.infinitic.common.tasks.data.TaskOptions
import io.infinitic.common.tasks.data.TaskTag
import io.infinitic.common.workflows.data.channels.ChannelEventFilter
import io.infinitic.common.workflows.data.channels.ChannelName
import io.infinitic.common.workflows.data.channels.ChannelSignal
import io.infinitic.common.workflows.data.channels.ChannelSignalType
import io.infinitic.common.workflows.data.workflows.WorkflowMeta
import io.infinitic.common.workflows.data.workflows.WorkflowName
import io.infinitic.common.workflows.data.workflows.WorkflowOptions
import io.infinitic.common.workflows.data.workflows.WorkflowTag
import kotlinx.serialization.Serializable

@Serializable
sealed class Command {
    open fun hash() = CommandHash(SerializedData.from(this).hash())
}

/**
 * Commands are asynchronously processed
 */

@Serializable
data class DispatchTask(
    val taskName: TaskName,
    val methodName: MethodName,
    val methodParameterTypes: MethodParameterTypes,
    val methodParameters: MethodParameters,
    val taskTags: Set<TaskTag>,
    val taskMeta: TaskMeta,
    val taskOptions: TaskOptions
) : Command()

@Serializable
data class DispatchChildWorkflow(
    val childWorkflowName: WorkflowName,
    val childMethodName: MethodName,
    val childMethodParameterTypes: MethodParameterTypes,
    val childMethodParameters: MethodParameters,
    val workflowTags: Set<WorkflowTag>,
    val workflowMeta: WorkflowMeta,
    val workflowOptions: WorkflowOptions
) : Command()

@Serializable
object StartAsync : Command() {
    override fun equals(other: Any?) = javaClass == other?.javaClass
}

@Serializable
data class EndAsync(
    @JsonProperty("output")
    val asyncReturnValue: CommandReturnValue
) : Command()

@Serializable
object StartInlineTask : Command() {
    // as we can not define a data class without parameter, we add manually the equals func
    override fun equals(other: Any?) = javaClass == other?.javaClass
}

@Serializable
data class EndInlineTask(
    @JsonProperty("output")
    val inlineTaskReturnValue: CommandReturnValue
) : Command()

@Serializable
data class StartDurationTimer(
    val duration: MillisDuration
) : Command()

@Serializable
data class StartInstantTimer(
    val instant: MillisInstant
) : Command()

@Serializable
data class ReceiveInChannel(
    val channelName: ChannelName,
    val channelSignalType: ChannelSignalType?,
    val channelEventFilter: ChannelEventFilter?
) : Command()

@Serializable
data class SendToChannel(
    val channelName: ChannelName,
    val channelSignal: ChannelSignal,
    val channelSignalTypes: Set<ChannelSignalType>
) : Command()
