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
import jetbrains.buildServer.agent.duplicates.DuplicatesReporter;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.XMLReader;

import java.io.File;
import java.util.Map;

/**
 * User: vbedrosova
 * Date: 22.01.11
 * Time: 15:18
 */
public interface ParseParameters {
  boolean isVerbose();

  @NotNull
  BuildProgressLogger getThreadLogger();

  @NotNull
  BuildProgressLogger getInternalizingThreadLogger();

  @NotNull
  InspectionReporter getInspectionReporter();

  @NotNull
  DuplicatesReporter getDuplicatesReporter();

  @NotNull
  Map<String, String> getParameters();

  @NotNull
  XMLReader getXmlReader();

  @NotNull
  String getType();

  @NotNull
  File getCheckoutDir();

  @NotNull
  File getTempDir();
}