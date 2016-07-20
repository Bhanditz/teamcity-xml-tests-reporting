/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.xmlReportPlugin.parsers.nUnit;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.xmlReportPlugin.Parser;
import jetbrains.buildServer.xmlReportPlugin.ParsingException;
import jetbrains.buildServer.xmlReportPlugin.ParsingResult;
import jetbrains.buildServer.xmlReportPlugin.tests.TestParsingResult;
import jetbrains.buildServer.xmlReportPlugin.tests.TestReporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


class NUnitReportParser implements Parser {
  public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(NUnitReportParser.class);

  @NotNull
  private final TestReporter myTestReporter;

  private int myTestsToSkip;
  private int myLoggedTests;

  private int myLoggedSuites;

  @Nullable
  private ParsingException myParsingException;

  @Nullable
  private String mySuite;

  public NUnitReportParser(@NotNull TestReporter testReporter) {
    myTestReporter = testReporter;
  }

  public boolean parse(@NotNull final File file, @Nullable final ParsingResult prevResult) throws ParsingException {
    if (prevResult != null) {
      myTestsToSkip = ((TestParsingResult) prevResult).getTests();
    }
    try {
      new NUnitXmlReportParser(new NUnitXmlReportParser.Callback() {
        public void suiteFound(@Nullable final String suiteName) {
          if (mySuite != null) {
            LOG.warn("Suite " + mySuite + " was not closed");
          }

          if (suiteName == null) {
            myTestReporter.warning("File " + file + " contains unnamed suite");
            return;
          }

          myTestReporter.openTestSuite(suiteName);
          ++myLoggedSuites;
          mySuite = suiteName;
        }

        public void suiteFinished(@Nullable final String suiteName) {
          if (mySuite == null || !mySuite.equals(suiteName)) {
            LOG.warn("Failed to log suite finish for not-opened suite " + suiteName);
            return;
          }
          myTestReporter.closeTestSuite();
          mySuite = null;
        }

        public void testFound(@NotNull final TestData testData) {
          try {
            if (testSkipped()) return;

            final String testName = testData.getName();

            if (testName == null) {
              myTestReporter.warning("File " + file + " contains unnamed test");
              return;
            }

            myTestReporter.openTest(testName);
            final String message = testData.getMessage();
            if (testData.isIgnored()) {
              myTestReporter.testIgnored(StringUtil.emptyIfNull(message));
            } else if (testData.isSuccess()) {
              if (!StringUtil.isEmptyOrSpaces(message)) {
                myTestReporter.testStdOutput(message);
              }
            } else if (message != null) {
              myTestReporter.testFail(message, testData.getFailureStackTrace());
            }
            myTestReporter.closeTest(testData.getDuration());
          } finally {
            ++myLoggedTests;
          }
        }

        @Override
        public void failure(@NotNull final String msg) {
          myTestReporter.failure(file + ": " + msg);
        }

        @Override
        public void warning(@NotNull final String msg) {
          myTestReporter.warning(file + ": " + msg);
        }

        @Override
        public void message(@NotNull final String msg) {
          myTestReporter.info(file + ": " + msg);
        }
      }).parse(file);
      return true;
    } catch (IOException e) {
      myParsingException = new ParsingException(e);
      if (mySuite != null) myTestReporter.closeTestSuite();
      LOG.debug("Couldn't completely parse " + file
                + " report, exception occurred: " + e + ", " + myLoggedTests + " tests logged");
    }

    return false;
  }

  public ParsingResult getParsingResult() {
    return new TestParsingResult(myLoggedSuites, (myLoggedTests > myTestsToSkip) ? myLoggedTests : myTestsToSkip, myParsingException);
  }

  private boolean testSkipped() {
    return myLoggedTests < myTestsToSkip;
  }
}