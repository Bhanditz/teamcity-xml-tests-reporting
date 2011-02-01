package jetbrains.buildServer.xmlReportPlugin.tests;

import jetbrains.buildServer.xmlReportPlugin.LoggingUtils;
import jetbrains.buildServer.xmlReportPlugin.ParseParameters;
import jetbrains.buildServer.xmlReportPlugin.ParsingResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * User: vbedrosova
 * Date: 22.01.11
 * Time: 18:33
 */
public class TestsParsingResult implements ParsingResult {
  private int mySuites;
  private int myTests;

  public TestsParsingResult(int suites, int tests) {
    mySuites = suites;
    myTests = tests;
  }

  public int getSuites() {
    return mySuites;
  }

  public int getTests() {
    return myTests;
  }

  public void accumulate(@NotNull ParsingResult parsingResult) {
    final TestsParsingResult testsParsingResult = (TestsParsingResult) parsingResult;
    mySuites += testsParsingResult.getSuites();
    myTests += testsParsingResult.getTests();
  }

  @NotNull
  public static TestsParsingResult createEmptyResult() {
    return new TestsParsingResult(0, 0);
  }

  public void logAsFileResult(@NotNull File file, @NotNull ParseParameters parameters) {
    final StringBuilder message = new StringBuilder(file.getAbsolutePath()).append(" report processed: ");

    message.append(mySuites).append(" suite").append(getEnding(mySuites));
    if (myTests > 0) {
      message.append(", ").append(myTests).append(" test").append(getEnding(myTests));
    }

    if (parameters.isVerbose()) {
      parameters.getThreadLogger().message(message.toString());
    }

    LoggingUtils.LOG.debug(message.toString());
  }

  @NotNull
  private static String getEnding(int number) {
    return (number == 1 ? "" : "s");
  }

  public void logAsTotalResult(@NotNull ParseParameters parameters) {}
}