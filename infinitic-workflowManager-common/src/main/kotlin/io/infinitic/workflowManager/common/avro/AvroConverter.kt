package io.infinitic.workflowManager.common.avro

import io.infinitic.common.json.Json
import io.infinitic.workflowManager.common.data.methodRuns.MethodRun
import io.infinitic.workflowManager.common.data.workflowTasks.WorkflowTaskId
import io.infinitic.workflowManager.common.data.workflows.WorkflowMessageIndex
import io.infinitic.workflowManager.common.data.workflows.WorkflowId
import io.infinitic.workflowManager.common.data.workflows.WorkflowName
import io.infinitic.workflowManager.common.messages.CancelWorkflow
import io.infinitic.workflowManager.common.messages.ChildWorkflowCanceled
import io.infinitic.workflowManager.common.messages.ChildWorkflowCompleted
import io.infinitic.workflowManager.common.messages.WorkflowTaskCompleted
import io.infinitic.workflowManager.common.messages.WorkflowTaskDispatched
import io.infinitic.workflowManager.common.messages.TimerCompleted
import io.infinitic.workflowManager.common.messages.DispatchWorkflow
import io.infinitic.workflowManager.common.messages.ObjectReceived
import io.infinitic.workflowManager.common.messages.ForWorkflowEngineMessage
import io.infinitic.workflowManager.common.messages.Message
import io.infinitic.workflowManager.common.messages.TaskCanceled
import io.infinitic.workflowManager.common.messages.TaskCompleted
import io.infinitic.workflowManager.common.messages.TaskDispatched
import io.infinitic.workflowManager.common.messages.WorkflowCanceled
import io.infinitic.workflowManager.common.messages.WorkflowCompleted
import io.infinitic.workflowManager.common.data.states.WorkflowState
import io.infinitic.workflowManager.data.methodRuns.AvroMethodRun
import io.infinitic.workflowManager.messages.AvroCancelWorkflow
import io.infinitic.workflowManager.messages.AvroChildWorkflowCanceled
import io.infinitic.workflowManager.messages.AvroChildWorkflowCompleted
import io.infinitic.workflowManager.messages.AvroWorkflowTaskCompleted
import io.infinitic.workflowManager.messages.AvroWorkflowTaskDispatched
import io.infinitic.workflowManager.messages.AvroDelayCompleted
import io.infinitic.workflowManager.messages.AvroDispatchWorkflow
import io.infinitic.workflowManager.messages.AvroEventReceived
import io.infinitic.workflowManager.messages.AvroTaskCanceled
import io.infinitic.workflowManager.messages.AvroTaskCompleted
import io.infinitic.workflowManager.messages.AvroTaskDispatched
import io.infinitic.workflowManager.messages.AvroWorkflowCanceled
import io.infinitic.workflowManager.messages.AvroWorkflowCompleted
import io.infinitic.workflowManager.messages.envelopes.AvroEnvelopeForWorkflowEngine
import io.infinitic.workflowManager.messages.envelopes.AvroForWorkflowEngineMessageType
import io.infinitic.workflowManager.states.AvroWorkflowState
import org.apache.avro.specific.SpecificRecordBase

/**
 * This class does the mapping between avro-generated classes and classes actually used by our code
 */
object AvroConverter {

    /**
     *  State <-> Avro State
     */
    fun fromStorage(avro: AvroWorkflowState) = WorkflowState(
        workflowId = WorkflowId(avro.workflowId),
        parentWorkflowId = avro.parentWorkflowId?.let { WorkflowId(it) },
        workflowName = WorkflowName(avro.workflowName),
        workflowOptions = convertJson(avro.workflowOptions),
        currentWorkflowTaskId = avro.currentWorkflowTaskId?.let { WorkflowTaskId(it) },
        currentMessageIndex = WorkflowMessageIndex(avro.currentMessageIndex),
        currentMethodRuns = avro.currentMethodRuns.map { convertJson<MethodRun>(it) }.toMutableList(),
        currentProperties = convertJson(avro.currentProperties),
        propertyStore = convertJson(avro.propertyStore),
        bufferedMessages = avro.bufferedMessages.map { fromWorkflowEngine(it) }.toMutableList()
    )

    fun toStorage(state: WorkflowState) = AvroWorkflowState
        .newBuilder()
        .setWorkflowId("${state.workflowId}")
        .setParentWorkflowId(state.parentWorkflowId?.toString())
        .setWorkflowName("${state.workflowName}")
        .setWorkflowOptions(convertJson(state.workflowOptions))
        .setCurrentWorkflowTaskId("${state.currentWorkflowTaskId}")
        .setCurrentMessageIndex(convertJson(state.currentMessageIndex))
        .setCurrentMethodRuns(state.currentMethodRuns.map { convertJson<AvroMethodRun>(it) })
        .setCurrentProperties(convertJson(state.currentProperties))
        .setPropertyStore(convertJson(state.propertyStore))
        .setBufferedMessages(state.bufferedMessages.map { toWorkflowEngine(it) })
        .build()

    /**
     *  Avro message <-> Avro Envelope
     */
    fun addEnvelopeToWorkflowEngineMessage(message: SpecificRecordBase): AvroEnvelopeForWorkflowEngine {
        val builder = AvroEnvelopeForWorkflowEngine.newBuilder()
        when (message) {
            is AvroCancelWorkflow -> builder.apply {
                workflowId = message.workflowId
                avroCancelWorkflow = message
                type = AvroForWorkflowEngineMessageType.AvroCancelWorkflow
            }
            is AvroChildWorkflowCanceled -> builder.apply {
                workflowId = message.workflowId
                avroChildWorkflowCanceled = message
                type = AvroForWorkflowEngineMessageType.AvroChildWorkflowCanceled
            }
            is AvroChildWorkflowCompleted -> builder.apply {
                workflowId = message.workflowId
                avroChildWorkflowCompleted = message
                type = AvroForWorkflowEngineMessageType.AvroChildWorkflowCompleted
            }
            is AvroWorkflowTaskCompleted -> builder.apply {
                workflowId = message.workflowId
                avroWorkflowTaskCompleted = message
                type = AvroForWorkflowEngineMessageType.AvroWorkflowTaskCompleted
            }
            is AvroWorkflowTaskDispatched -> builder.apply {
                workflowId = message.workflowId
                avroWorkflowTaskDispatched = message
                type = AvroForWorkflowEngineMessageType.AvroWorkflowTaskDispatched
            }
            is AvroDelayCompleted -> builder.apply {
                workflowId = message.workflowId
                avroDelayCompleted = message
                type = AvroForWorkflowEngineMessageType.AvroDelayCompleted
            }
            is AvroDispatchWorkflow -> builder.apply {
                workflowId = message.workflowId
                avroDispatchWorkflow = message
                type = AvroForWorkflowEngineMessageType.AvroDispatchWorkflow
            }
            is AvroEventReceived -> builder.apply {
                workflowId = message.workflowId
                avroEventReceived = message
                type = AvroForWorkflowEngineMessageType.AvroEventReceived
            }
            is AvroTaskCanceled -> builder.apply {
                workflowId = message.workflowId
                avroTaskCanceled = message
                type = AvroForWorkflowEngineMessageType.AvroTaskCanceled
            }
            is AvroTaskCompleted -> builder.apply {
                workflowId = message.workflowId
                avroTaskCompleted = message
                type = AvroForWorkflowEngineMessageType.AvroTaskCompleted
            }
            is AvroTaskDispatched -> builder.apply {
                workflowId = message.workflowId
                avroTaskDispatched = message
                type = AvroForWorkflowEngineMessageType.AvroTaskDispatched
            }
            is AvroWorkflowCanceled -> builder.apply {
                workflowId = message.workflowId
                avroWorkflowCanceled = message
                type = AvroForWorkflowEngineMessageType.AvroWorkflowCanceled
            }
            is AvroWorkflowCompleted -> builder.apply {
                workflowId = message.workflowId
                avroWorkflowCompleted = message
                type = AvroForWorkflowEngineMessageType.AvroWorkflowCompleted
            }
            else -> throw RuntimeException("Unknown AvroWorkflowEngineMessage: ${message::class.qualifiedName}")
        }
        return builder.build()
    }

    fun removeEnvelopeFromWorkflowEngineMessage(input: AvroEnvelopeForWorkflowEngine): SpecificRecordBase = when (input.type) {
        AvroForWorkflowEngineMessageType.AvroCancelWorkflow -> input.avroCancelWorkflow
        AvroForWorkflowEngineMessageType.AvroChildWorkflowCanceled -> input.avroChildWorkflowCanceled
        AvroForWorkflowEngineMessageType.AvroChildWorkflowCompleted -> input.avroChildWorkflowCompleted
        AvroForWorkflowEngineMessageType.AvroWorkflowTaskCompleted -> input.avroWorkflowTaskCompleted
        AvroForWorkflowEngineMessageType.AvroWorkflowTaskDispatched -> input.avroWorkflowTaskDispatched
        AvroForWorkflowEngineMessageType.AvroDelayCompleted -> input.avroDelayCompleted
        AvroForWorkflowEngineMessageType.AvroDispatchWorkflow -> input.avroDispatchWorkflow
        AvroForWorkflowEngineMessageType.AvroEventReceived -> input.avroEventReceived
        AvroForWorkflowEngineMessageType.AvroTaskCanceled -> input.avroTaskCanceled
        AvroForWorkflowEngineMessageType.AvroTaskCompleted -> input.avroTaskCompleted
        AvroForWorkflowEngineMessageType.AvroTaskDispatched -> input.avroTaskDispatched
        AvroForWorkflowEngineMessageType.AvroWorkflowCanceled -> input.avroWorkflowCanceled
        AvroForWorkflowEngineMessageType.AvroWorkflowCompleted -> input.avroWorkflowCompleted
        null -> throw Exception("Null type in $input")
    }

    /**
     *  Message <-> Avro Envelope
     */

    fun toWorkflowEngine(message: ForWorkflowEngineMessage): AvroEnvelopeForWorkflowEngine =
        addEnvelopeToWorkflowEngineMessage(toAvroMessage(message))

    fun fromWorkflowEngine(avro: AvroEnvelopeForWorkflowEngine) =
        fromAvroMessage(removeEnvelopeFromWorkflowEngineMessage(avro)) as ForWorkflowEngineMessage

    /**
     *  Message <-> Avro Message
     */

    fun fromAvroMessage(avro: SpecificRecordBase): Message = when (avro) {
        is AvroCancelWorkflow -> fromAvroMessage(avro)
        is AvroChildWorkflowCanceled -> fromAvroMessage(avro)
        is AvroChildWorkflowCompleted -> fromAvroMessage(avro)
        is AvroWorkflowTaskCompleted -> fromAvroMessage(avro)
        is AvroWorkflowTaskDispatched -> fromAvroMessage(avro)
        is AvroDelayCompleted -> fromAvroMessage(avro)
        is AvroDispatchWorkflow -> fromAvroMessage(avro)
        is AvroEventReceived -> fromAvroMessage(avro)
        is AvroTaskCanceled -> fromAvroMessage(avro)
        is AvroTaskCompleted -> fromAvroMessage(avro)
        is AvroTaskDispatched -> fromAvroMessage(avro)
        is AvroWorkflowCanceled -> fromAvroMessage(avro)
        is AvroWorkflowCompleted -> fromAvroMessage(avro)
        else -> throw Exception("Unknown SpecificRecordBase: ${avro::class.qualifiedName}")
    }

    private fun fromAvroMessage(avro: AvroCancelWorkflow) = convertJson<CancelWorkflow>(avro)
    private fun fromAvroMessage(avro: AvroChildWorkflowCanceled) = convertJson<ChildWorkflowCanceled>(avro)
    private fun fromAvroMessage(avro: AvroChildWorkflowCompleted) = convertJson<ChildWorkflowCompleted>(avro)
    private fun fromAvroMessage(avro: AvroWorkflowTaskCompleted) = convertJson<WorkflowTaskCompleted>(avro)
    private fun fromAvroMessage(avro: AvroWorkflowTaskDispatched) = convertJson<WorkflowTaskDispatched>(avro)
    private fun fromAvroMessage(avro: AvroDelayCompleted) = convertJson<TimerCompleted>(avro)
    private fun fromAvroMessage(avro: AvroDispatchWorkflow) = convertJson<DispatchWorkflow>(avro)
    private fun fromAvroMessage(avro: AvroEventReceived) = convertJson<ObjectReceived>(avro)
    private fun fromAvroMessage(avro: AvroTaskCanceled) = convertJson<TaskCanceled>(avro)
    private fun fromAvroMessage(avro: AvroTaskCompleted) = convertJson<TaskCompleted>(avro)
    private fun fromAvroMessage(avro: AvroTaskDispatched) = convertJson<TaskDispatched>(avro)
    private fun fromAvroMessage(avro: AvroWorkflowCanceled) = convertJson<WorkflowCanceled>(avro)
    private fun fromAvroMessage(avro: AvroWorkflowCompleted) = convertJson<WorkflowCompleted>(avro)

    fun toAvroMessage(message: Message): SpecificRecordBase = when (message) {
        is CancelWorkflow -> toAvroMessage(message)
        is ChildWorkflowCanceled -> toAvroMessage(message)
        is ChildWorkflowCompleted -> toAvroMessage(message)
        is WorkflowTaskCompleted -> toAvroMessage(message)
        is WorkflowTaskDispatched -> toAvroMessage(message)
        is TimerCompleted -> toAvroMessage(message)
        is DispatchWorkflow -> toAvroMessage(message)
        is ObjectReceived -> toAvroMessage(message)
        is TaskCanceled -> toAvroMessage(message)
        is TaskCompleted -> toAvroMessage(message)
        is TaskDispatched -> toAvroMessage(message)
        is WorkflowCanceled -> toAvroMessage(message)
        is WorkflowCompleted -> toAvroMessage(message)
    }

    private fun toAvroMessage(message: CancelWorkflow) = convertJson<AvroCancelWorkflow>(message)
    private fun toAvroMessage(message: ChildWorkflowCanceled) = convertJson<AvroChildWorkflowCanceled>(message)
    private fun toAvroMessage(message: ChildWorkflowCompleted) = convertJson<AvroChildWorkflowCompleted>(message)
    private fun toAvroMessage(message: WorkflowTaskCompleted) = convertJson<AvroWorkflowTaskCompleted>(message)
    private fun toAvroMessage(message: WorkflowTaskDispatched) = convertJson<AvroWorkflowTaskDispatched>(message)
    private fun toAvroMessage(message: TimerCompleted) = convertJson<AvroDelayCompleted>(message)
    private fun toAvroMessage(message: DispatchWorkflow) = convertJson<AvroDispatchWorkflow>(message)
    private fun toAvroMessage(message: ObjectReceived) = convertJson<AvroEventReceived>(message)
    private fun toAvroMessage(message: TaskCanceled) = convertJson<AvroTaskCanceled>(message)
    private fun toAvroMessage(message: TaskCompleted) = convertJson<AvroTaskCompleted>(message)
    private fun toAvroMessage(message: TaskDispatched) = convertJson<AvroTaskDispatched>(message)
    private fun toAvroMessage(message: WorkflowCanceled) = convertJson<AvroWorkflowCanceled>(message)
    private fun toAvroMessage(message: WorkflowCompleted) = convertJson<AvroWorkflowCompleted>(message)

    /**
     *  Mapping function by Json serialization/deserialization
     */
    inline fun <reified T : Any> convertJson(from: Any?): T = Json.parse(Json.stringify(from))
}