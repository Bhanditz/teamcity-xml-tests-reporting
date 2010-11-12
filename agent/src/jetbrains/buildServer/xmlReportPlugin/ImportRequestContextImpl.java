/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import java.io.File;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

class ImportRequestContextImpl implements ImportRequestContext {
  @NotNull private final File myPath;
  @NotNull private final BuildProgressLogger myLogger;

  ImportRequestContextImpl(@NotNull final File path,
                           @NotNull final BuildProgressLogger logger) {
    myPath = path;
    myLogger = logger;
  }

  @NotNull
  public File getPath() {
    return myPath;
  }

  @NotNull
  public BuildProgressLogger getLogger() {
    return myLogger;
  }

}