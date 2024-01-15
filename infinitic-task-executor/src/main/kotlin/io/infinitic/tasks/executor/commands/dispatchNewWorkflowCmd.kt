/**
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including
 * without limitation fees for hosting or consulting/ support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also include this
 * Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */
package io.infinitic.tasks.executor.commands

import io.infinitic.common.data.MillisInstant
import io.infinitic.common.emitters.EmitterName
import io.infinitic.common.exceptions.thisShouldNotHappen
import io.infinitic.common.tasks.executors.errors.WorkflowMethodTimedOutError
import io.infinitic.common.transport.InfiniticProducer
import io.infinitic.common.workflows.data.commands.DispatchNewWorkflowCommand
import io.infinitic.common.workflows.data.commands.DispatchNewWorkflowPastCommand
import io.infinitic.common.workflows.data.methodRuns.WorkflowMethodId
import io.infinitic.common.workflows.data.workflows.WorkflowId
import io.infinitic.common.workflows.engine.messages.ChildMethodTimedOut
import io.infinitic.common.workflows.engine.messages.DispatchNewWorkflow
import io.infinitic.common.workflows.tags.messages.AddTagToWorkflow
import io.infinitic.common.workflows.tags.messages.DispatchWorkflowByCustomId
import io.infinitic.tasks.executor.TaskEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun CoroutineScope.dispatchNewWorkflowCmd(
  currentWorkflow: TaskEventHandler.CurrentWorkflow,
  pastCommand: DispatchNewWorkflowPastCommand,
  workflowTaskInstant: MillisInstant,
  producer: InfiniticProducer
) {
  val emitterName = EmitterName(producer.name)
  val command: DispatchNewWorkflowCommand = pastCommand.command
  val workflowId = WorkflowId.from(pastCommand.commandId)
  val workflowName = command.workflowName
  val methodName = command.methodName

  val customIds = command.workflowTags.filter { it.isCustomId() }

  when (customIds.size) {
    // no customId tag provided
    0 -> {
      // send workflow to workflow engine
      val dispatchWorkflow = DispatchNewWorkflow(
          workflowName = workflowName,
          workflowId = workflowId,
          methodName = methodName,
          methodParameters = command.methodParameters,
          methodParameterTypes = command.methodParameterTypes,
          workflowTags = command.workflowTags,
          workflowMeta = command.workflowMeta,
          parentWorkflowName = currentWorkflow.workflowName,
          parentWorkflowId = currentWorkflow.workflowId,
          parentWorkflowMethodId = currentWorkflow.workflowMethodId,
          clientWaiting = false,
          emitterName = emitterName,
          emittedAt = workflowTaskInstant,
      )
      launch { producer.sendToWorkflowCmd(dispatchWorkflow) }

      // add provided tags
      dispatchWorkflow.workflowTags.forEach {
        val addTagToWorkflow = AddTagToWorkflow(
            workflowName = dispatchWorkflow.workflowName,
            workflowTag = it,
            workflowId = workflowId,
            emitterName = emitterName,
            emittedAt = workflowTaskInstant,
        )
        launch { producer.sendToWorkflowTag(addTagToWorkflow) }
      }
    }

    1 -> {
      // send to workflow tag engine
      val dispatchWorkflowByCustomId = DispatchWorkflowByCustomId(
          workflowName = workflowName,
          workflowTag = customIds.first(),
          workflowId = workflowId,
          methodName = methodName,
          methodParameters = command.methodParameters,
          methodParameterTypes = command.methodParameterTypes,
          methodTimeout = command.methodTimeout,
          workflowTags = command.workflowTags,
          workflowMeta = command.workflowMeta,
          parentWorkflowName = currentWorkflow.workflowName,
          parentWorkflowId = currentWorkflow.workflowId,
          parentWorkflowMethodId = currentWorkflow.workflowMethodId,
          clientWaiting = false,
          emitterName = emitterName,
          emittedAt = workflowTaskInstant,
      )
      launch { producer.sendToWorkflowTag(dispatchWorkflowByCustomId) }
    }
    // this must be excluded from workflow task
    else -> thisShouldNotHappen()
  }

  // send global timeout if any
  val timeout = command.methodTimeout

  if (timeout != null) {
    val childMethodTimedOut = ChildMethodTimedOut(
        workflowId = currentWorkflow.workflowId,
        workflowName = currentWorkflow.workflowName,
        workflowMethodId = currentWorkflow.workflowMethodId,
        childMethodTimedOutError = WorkflowMethodTimedOutError(
            workflowName = workflowName,
            workflowId = workflowId,
            methodName = methodName,
            workflowMethodId = WorkflowMethodId.from(workflowId),
        ),
        emitterName = emitterName,
        emittedAt = workflowTaskInstant + timeout,
    )
    launch { producer.sendToWorkflowEngine(childMethodTimedOut, timeout) }
  }
}