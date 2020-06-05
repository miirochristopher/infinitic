package com.zenaton.jobManager.messages.interfaces

import com.zenaton.commons.data.DateTime
import com.zenaton.jobManager.data.JobName

interface ForWorkerMessage {
    val jobName: JobName
    val sentAt: DateTime
}
