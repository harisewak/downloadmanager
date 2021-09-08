package com.harisewak.downloader.other

const val REQUEST_ID = "request_id"
const val REASON = "failure_reason"
const val MESSAGE = "message"
const val BUFFER_SIZE = 4096
const val RETRY_INTERVAL_MS = 3000L

const val REASON_CANCELLED = 11
const val REASON_RESPONSE_NOT_OK = 12
const val REASON_NETWORK_FAILURE = 13
const val REASON_DATABASE_ERROR = 14
const val REASON_STORAGE_ERROR = 15