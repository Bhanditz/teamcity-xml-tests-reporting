/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.xmlReportPlugin;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * User: vbedrosova
 * Date: 12.01.11
 * Time: 14:46
 */
public class LoggingUtils {
  public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(XmlReportPlugin.class);

  public static void logInTarget(@NotNull String target,
                                 @NotNull Runnable activity,
                                 @NotNull BuildProgressLogger logger) {
    logger.targetStarted(target);
    activity.run();
    logger.targetFinished(target);
  }

  public static void message(@NotNull String message, @NotNull BuildProgressLogger logger) {
    LOG.info(message);
    logger.message(message);
  }

  public static void warn(@NotNull String message, @NotNull BuildProgressLogger logger) {
    LOG.warn(message);
    logger.warning(message);
  }

  public static void logError(@Nullable String error,
                              @Nullable Throwable throwable,
                              @NotNull BuildProgressLogger logger) {
    final String message =
        (error != null ? error : "")
      + (error != null && throwable != null ? ": " : "")
      + (throwable != null ? throwable.getMessage() : "");

    if (message.length() > 0) {
      logger.error(message);
      LOG.warn(message);
    }
    if (throwable != null) {
      LOG.warn(throwable.getMessage(), throwable);
    }
  }

  public static void logFailedToParse(@NotNull File file, @NotNull String type, @Nullable Throwable t, @NotNull BuildProgressLogger logger) {
    logError("Failed to parse " + file + " with " + getTypeDisplayName(type) + " parser", t, logger);
  }

  public static String getTypeDisplayName(@NotNull String type) {
    return XmlReportPluginUtil.SUPPORTED_REPORT_TYPES.get(type);
  }
}