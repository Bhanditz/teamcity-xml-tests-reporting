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

import jetbrains.buildServer.agent.duplicates.DuplicatesReporter;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionReporterListener;
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo;
import jetbrains.buildServer.duplicator.DuplicateInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public final class TestUtil {
  static public String readFile(@NotNull final File file, boolean unifyFileSeparators) throws IOException {
    final FileInputStream inputStream = new FileInputStream(file);
    try {
      final BufferedInputStream bis = new BufferedInputStream(inputStream);
      final byte[] bytes = new byte[(int) file.length()];
      bis.read(bytes);
      bis.close();

      String line = new String(bytes);
      if (unifyFileSeparators) {
        line = line.replace("/", File.separator).replace("\\", File.separator);
      }
      return line;
    }
    finally {
      inputStream.close();
    }
  }

  static public List<String> readFileToList(@NotNull final File file) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final List<String> lines = new ArrayList<String>();
    String line = reader.readLine();
    while (line != null) {
      lines.add(line.replace("/", File.separator).replace("\\", File.separator));
      line = reader.readLine();
    }
    Collections.sort(lines);
    return lines;
  }

  public static String getTestDataPath(final String fileName, final String folderName) throws FileNotFoundException {
    return getTestDataFile(fileName, folderName).getPath();
  }

  public static String getAbsoluteTestDataPath(@Nullable final String fileName, @NotNull final String folderName) throws FileNotFoundException {
    return getTestDataFile(fileName, folderName).getAbsolutePath();
  }

  public static File getTestDataFile(final String fileName, final String folderName) throws FileNotFoundException {
    final String relativeFileName = "testData" + (folderName != null ? File.separator + folderName : "") + (fileName != null ? File.separator + fileName : "");
    final File file1 = new File(relativeFileName);
    if (file1.exists()) {
      return file1;
    }
    final File file2 = new File("tests" + File.separator + relativeFileName);
    if (file2.exists()) {
      return file2;
    }
    final File file3 = new File("svnrepo" + File.separator + "xml-tests-reporting" + File.separator + "tests" + File.separator + relativeFileName);
    if (file3.exists()) {
      return file3;
    }
    throw new FileNotFoundException(file1.getAbsolutePath() + ", " + file2.getAbsolutePath() + " or file " + file3.getAbsolutePath() + " should exist.");
  }

  public static String getRelativePath(final File f, final File base) {
    if (f.getAbsolutePath().startsWith(base.getAbsolutePath())) {
      return f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);  //+1 for truncating trasiling "/"
    }
    return f.getAbsolutePath();
  }

  public static InspectionReporter createInspectionReporter(final StringBuilder results) {
    return new InspectionReporter() {
      public void reportInspection(@NotNull final InspectionInstance inspection) {
        results.append(inspection.toString()).append("\n");
      }

      public void reportInspectionType(@NotNull final InspectionTypeInfo inspectionType) {
        results.append(inspectionType.toString()).append("\n");
      }

      public void markBuildAsInspectionsBuild() {
      }

      public void flush() {
      }

      public void addListener(@NotNull final InspectionReporterListener listener) {
      }
    };
  }

  public static DuplicatesReporter createDuplicatesReporter(final StringBuilder results) {
    return new DuplicatesReporter() {
      public void startDuplicates() {
      }

      public void addDuplicate(@NotNull DuplicateInfo duplicate) {
        results.append("[Cost: ").append(duplicate.getCost());
        results.append(" Density: ").append(duplicate.getDensity());
        results.append(" Hash: ").append(duplicate.getHash()).append("\n");
        for (final DuplicateInfo.Fragment fragment : duplicate.getFragments()) {
          results.append("[File: ").append(fragment.getFile());
          results.append("Hash: ").append(fragment.getHash());
          results.append("Offset: ").append(fragment.getOffsetInfo());
          results.append("]\n");
        }
        results.append("]\n\n");
      }

      public void addDuplicates(@NotNull Collection<DuplicateInfo> duplicates) {
        for (final DuplicateInfo duplicate : duplicates) {
          addDuplicate(duplicate);
        }
      }

      public void finishDuplicates() {
      }
    };
  }

  public static InspectionReporter createInspectionReporter(Mockery context) {
    final InspectionReporter reporter = context.mock(InspectionReporter.class);
    context.checking(new Expectations() {
      {
        ignoring(reporter);
      }
    });
    return reporter;
  }

  public static DuplicatesReporter createDuplicatesReporter(Mockery context) {
    final DuplicatesReporter reporter = context.mock(DuplicatesReporter.class);
    context.checking(new Expectations() {
      {
        ignoring(reporter);
      }
    });
    return reporter;
  }


}
