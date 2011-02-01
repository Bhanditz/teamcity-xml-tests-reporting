package jetbrains.buildServer.xmlReportPlugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * User: vbedrosova
 * Date: 16.12.10
 * Time: 18:06
 */
public class ParseReportCommand implements Runnable {
  @NotNull
  private final File myFile;

  @NotNull
  private final ParseParameters myParameters;

  @NotNull
  private final FilesState myFilesState;

  @NotNull
  private final Map<File, ParsingResult> myPrevResults;

  @NotNull
  private final ParserFactory myParserFactory;

  public ParseReportCommand(@NotNull File file,
                            @NotNull ParseParameters parameters,
                            @NotNull FilesState filesState,
                            @NotNull Map<File, ParsingResult> prevResults,
                            @NotNull ParserFactory parserFactory) {
    myFile = file;
    myParameters = parameters;
    myFilesState = filesState;
    myPrevResults = prevResults;
    myParserFactory = parserFactory;
  }

  public void run() {
    final Parser parser = myParserFactory.createParser(myParameters);

    boolean finished;
    try {
      finished = parser.parse(myFile, myPrevResults.get(myFile));
    } catch (ParsingException e) {
      finished = true;
      logFailedToParse(e);
    }

    final ParsingResult parsingResult = parser.getParsingResult();
    assert parsingResult != null;

    if (finished) { // file processed
      parsingResult.logAsFileResult(myFile, myParameters);
      myPrevResults.remove(myFile);
      myFilesState.setFileProcessed(myFile, parsingResult);
    } else {
      //todo: log file not processed
      myPrevResults.put(myFile, parsingResult);
      myFilesState.removeFile(myFile);
    }
  }

  private void logFailedToParse(@NotNull ParsingException e) {
    LoggingUtils.logFailedToParse(myFile, myParameters.getType(), e.getCause(), myParameters.getThreadLogger());
  }
}