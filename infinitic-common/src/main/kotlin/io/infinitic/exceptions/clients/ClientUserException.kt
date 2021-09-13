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

package io.infinitic.exceptions.clients

import io.infinitic.exceptions.UserException
import io.infinitic.workflows.SendChannel

sealed class ClientUserException(
    msg: String,
    help: String,
) : UserException("$msg.\n$help")

class NotAStubException(
    name: String,
    async: String
) : ClientUserException(
    msg = "First parameter of client.$async function should be a stub",
    help = "Make sure to provide the stub returned by client.newTask($name) or client.newWorkflow($name) function"
)

class NotAnInterfaceException(
    method: String,
    action: String
) : ClientUserException(
    msg = "When using client.$action function, $method must be a method from an interface",
    help = "Make sure to provide a method defined from an interface, not from an actual instance."
)

class CanNotApplyOnChannelException(
    action: String
) : ClientUserException(
    msg = "First parameter of client.$action should be the stub of an existing task or workflow",
    help = "Make sure to provide the stub returned by client.getTask or client.getWorkflow function"
)

class CanNotApplyOnNewTaskStubException(
    name: String,
    action: String
) : ClientUserException(
    msg = "You can not `$action` on the stub of a new task",
    help = "Please target an existing $name task using `client.getTask()` "
)

class CanNotApplyOnNewWorkflowStubException(
    name: String,
    action: String
) : ClientUserException(
    msg = "You can not `$action` on the stub of a new workflow",
    help = "Please target an existing $name workflow using `client.getWorkflow()` "
)

class SuspendMethodNotSupportedException(
    klass: String,
    method: String
) : ClientUserException(
    msg = "method \"$method\" in class \"$klass\" is a suspend function",
    help = "Suspend functions are not supporteds"
)

class ChannelUsedOnNewWorkflowException(
    workflow: String
) : ClientUserException(
    msg = "Channels can only be used for an existing instance of $workflow workflow",
    help = "Make sure you target a running workflow, by providing and id when defining your workflow stub"
)

class UnknownMethodInSendChannelException(
    workflow: String,
    channel: String,
    method: String
) : ClientUserException(
    msg = "Unknown method $method used on channel $channel in $workflow",
    help = "Make sure to use the ${SendChannel<*>::send.name} method"
)

class CanNotAwaitStubPerTag(
    klass: String
) : ClientUserException(
    msg = "You can not await a task or workflow ($klass) based on a tag",
    help = "Please target the task or workflow per id"
)
