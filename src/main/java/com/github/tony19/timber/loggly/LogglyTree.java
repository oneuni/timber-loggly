/**
 * Copyright (C) 2015 Anthony K. Trinh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tony19.timber.loggly;

import com.github.tony19.loggly.LogglyClient;
import timber.log.Timber;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A <a href="https://github.com/JakeWharton/timber">Timber</a> tree that posts
 * log messages to <a href="http://loggly.com">Loggly</a>
 *
 * @author tony19@gmail.com
 */
public class LogglyTree extends Timber.HollowTree implements Timber.TaggedTree {

    private final LogglyClient loggly;
    private LogglyClient.Callback handler;

    /** Log severity level */
    private enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * Creates a  <a href="https://github.com/JakeWharton/timber">Timber</a>
     * tree for posting messages to <a href="http://loggly.com">Loggly</a>
     * @param token Loggly token from https://www.loggly.com/docs/customer-token-authentication-token/
     */
    public LogglyTree(String token) {
        loggly = new LogglyClient(token);

        // Setup an async callback
        // TODO: handle failed messages with N retries
        handler = new LogglyClient.Callback() {
            @Override
            public void success() {
                // XXX: Handle success
            }

            @Override
            public void failure(String error) {
                System.err.println("LogglyTree failed: " + error);
            }
        };
    }

    /**
     * Logs a message with {@code DEBUG} severity
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void d(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    /**
     * Logs a message and an associated throwable with {@code DEBUG} severity
     * @param t throwable to be logged
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void d(Throwable t, String message, Object... args) {
        log(Level.DEBUG, message, t, args);
    }

    /**
     * Logs a message with {@code INFO} severity
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void i(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Logs a message and an associated throwable with {@code INFO} severity
     * @param t throwable to be logged
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void i(Throwable t, String message, Object... args) {
        log(Level.INFO, message, t, args);
    }

    /**
     * Logs a message with {@code ERROR} severity
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void e(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    /**
     * Logs a message and an associated throwable with {@code ERROR} severity
     * @param t throwable to be logged
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void e(Throwable t, String message, Object... args) {
        log(Level.ERROR, message, t, args);
    }

    /**
     * Logs a message with {@code WARN} severity
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void w(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    /**
     * Logs a message and an associated throwable with {@code WARN} severity
     * @param t throwable to be logged
     * @param message message to be logged
     * @param args message formatting arguments
     */
    @Override
    public void w(Throwable t, String message, Object... args) {
        log(Level.WARN, message, t, args);
    }

    /**
     * Gets the JSON representation of a log event
     * @param level log severity level
     * @param message message to be logged
     * @param args message formatting arguments
     * @return JSON string
     */
    private String toJson(Level level, String message, Object... args) {
        return String.format("{\"level\": \"%1$s\", \"message\": \"%2$s\"}",
                            level,
                            String.format(message, args).replace("\"", "\\\""));
    }

    /**
     * Converts a {@code Throwable} into a string
     * http://stackoverflow.com/a/4812589/600838
     * @param t throwable to convert
     * @return string representation of the throwable
     */
    private String formatThrowable(Throwable t) {
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    /**
     * Gets the JSON representation of a log event
     * @param level log severity level
     * @param message message to be logged
     * @param args message formatting arguments
     * @return JSON string
     */
    private String toJson(Level level, String message, Throwable t, Object... args) {
        return String.format("{\"level\": \"%1$s\", \"message\": \"%2$s\", \"exception\": \"%3$s\"}",
            level,
            String.format(message, args).replace("\"", "\\\""),
            formatThrowable(t));
    }

    /**
     * Asynchronously sends a log event to Loggly
     * @param level log severity level
     * @param message message to be logged
     * @param t throwable
     * @param args message formatting arguments
     */
    private void log(Level level, String message, Throwable t, Object... args) {
        loggly.log(toJson(level, message, t, args), handler);
    }

    /**
     * Asynchronously sends a log event to Loggly
     * @param level log severity level
     * @param message message to be logged
     * @param args message formatting arguments
     */
    private void log(Level level, String message, Object... args) {
        loggly.log(toJson(level, message, args), handler);
    }

    /**
     * Sets the Loggly tag for all logs going forward. This differs from
     * the API of {@code Timber.TaggedTree} in that it's not a one-shot
     * tag.
     * @param tag desired tag or CSV of multiple tags; use empty string
     *            to clear tags
     */
    @Override
    public final void tag(String tag) {
        loggly.setTags(tag);
    }
}
