/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.util

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import kotlin.io.println as ktPrintln

object Log {
    /** Priority constant for the println method; use Log.v. */
    const val VERBOSE: Int = 2

    /** Priority constant for the println method; use Log.d. */
    const val DEBUG: Int = 3

    /** Priority constant for the println method; use Log.i. */
    const val INFO: Int = 4

    /** Priority constant for the println method; use Log.w. */
    const val WARN: Int = 5

    /** Priority constant for the println method; use Log.e. */
    const val ERROR: Int = 6

    /** Priority constant for the println method. */
    const val ASSERT: Int = 7

    private const val MIN_LOG_LEVEL = INFO

    /** Send a [DEBUG] log message. */
    @JvmStatic
    fun d(tag: String?, msg: String): Int {
        return printlnImpl(DEBUG, tag, msg)
    }

    /** Send a [DEBUG] log message and log the exception. */
    @JvmStatic
    fun d(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(DEBUG, tag, msg, tr)
    }

    /** Send an [ERROR] log message. */
    @JvmStatic
    fun e(tag: String?, msg: String): Int {
        return printlnImpl(ERROR, tag, msg)
    }

    /** Send a [ERROR] log message and log the exception. */
    @JvmStatic
    fun e(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(ERROR, tag, msg, tr)
    }

    /** Handy function to get a loggable stack trace from a Throwable */
    @JvmStatic
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t: Throwable? = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw, false)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    /** Send an [INFO] log message. */
    @JvmStatic
    fun i(tag: String?, msg: String): Int {
        return printlnImpl(INFO, tag, msg)
    }

    /** Send a [INFO] log message and log the exception. */
    @JvmStatic
    fun i(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(INFO, tag, msg, tr)
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     */
    @JvmStatic
    fun isLoggable(tag: String?, level: Int): Boolean {
        return level >= MIN_LOG_LEVEL
    }

    /** Low-level logging call. */
    @JvmStatic
    fun println(priority: Int, tag: String?, msg: String): Int {
        return printlnImpl(priority, tag, msg)
    }

    /** Low-level logging call. */
    @JvmStatic
    private fun printlnImpl(priority: Int, tag: String?, msg: String?, tr: Throwable? = null): Int {
        if (isLoggable(tag, priority)) {
            ktPrintln("[$tag] $msg")
        }
        return 1
    }

    /** Send a [VERBOSE] log message. */
    @JvmStatic
    fun v(tag: String?, msg: String): Int {
        return printlnImpl(VERBOSE, tag, msg)
    }

    /** Send a [VERBOSE] log message and log the exception. */
    @JvmStatic
    fun v(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(VERBOSE, tag, msg, tr)
    }

    /** Send a [WARN] log message. */
    @JvmStatic
    fun w(tag: String?, msg: String): Int {
        return printlnImpl(WARN, tag, msg)
    }

    /** Send a [WARN] log message and log the exception. */
    @JvmStatic
    fun w(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(WARN, tag, msg, tr)
    }

    /** Send a [WARN] log message and log the exception. */
    @JvmStatic
    fun w(tag: String?, tr: Throwable?): Int {
        return printlnImpl(WARN, tag, null, tr)
    }

    /** What a Terrible Failure: Report a condition that should never happen. */
    @JvmStatic
    fun wtf(tag: String?, msg: String?): Int {
        return printlnImpl(ERROR, tag, msg)
    }

    /** What a Terrible Failure: Report an exception that should never happen. */
    @JvmStatic
    fun wtf(tag: String?, tr: Throwable): Int {
        return printlnImpl(ERROR, tag, null, tr)
    }

    /** What a Terrible Failure: Report an exception that should never happen. */
    @JvmStatic
    fun wtf(tag: String?, msg: String?, tr: Throwable?): Int {
        return printlnImpl(ERROR, tag, msg, tr)
    }
}
