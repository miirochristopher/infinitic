package com.zenaton.workflowengine.interfaces

import com.zenaton.commons.data.DateTime

interface MessageInterface {
    var sentAt: DateTime?
    fun getStateId(): String
}
