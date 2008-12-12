/*
 * Copyright 2008 JetBrains s.r.o.
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

package jetbrains.buildServer.testReportParserPlugin;

import jetbrains.buildServer.agent.*;
import static jetbrains.buildServer.testReportParserPlugin.TestReportParserPluginUtil.isOutputVerbose;
import static jetbrains.buildServer.testReportParserPlugin.TestReportParserPluginUtil.isTestReportParsingEnabled;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class TestReportParserPlugin extends AgentLifeCycleAdapter implements DataProcessor {
  public static final String TEST_REPORT_DIR_PROPERTY = "testReportParsing.reportDirs";

  private static final String DATA_PROCESSOR_ID = "junit";
  private static final String DATA_PROCESSOR_VERBOSE_ARGUMENT = "verbose";

  private TestReportDirectoryWatcher myDirectoryWatcher;
  private TestReportProcessor myReportProcessor;
  private TestReportLogger myLogger;

  private boolean myTestReportParsingEnabled = false;
  private boolean myVerboseOutput = false;
  private long myBuildStartTime;
  private File myRunnerWorkingDir;

  private volatile boolean myStopped;

  public TestReportParserPlugin(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
    agentDispatcher.addListener(this);
  }

  public void buildStarted(@NotNull AgentRunningBuild agentRunningBuild) {
    myStopped = false;
    myBuildStartTime = new Date().getTime();
  }

  public void beforeRunnerStart(@NotNull AgentRunningBuild agentRunningBuild) {
    myRunnerWorkingDir = agentRunningBuild.getWorkingDirectory();

    final Map<String, String> runnerParameters = agentRunningBuild.getRunnerParameters();

    myTestReportParsingEnabled = isTestReportParsingEnabled(runnerParameters);
    myVerboseOutput = isOutputVerbose(runnerParameters);

    obtainLogger(agentRunningBuild);

    if (!myTestReportParsingEnabled) {
      return;
    }

    final String dirProperty = runnerParameters.get(TEST_REPORT_DIR_PROPERTY);
    final List<File> reportDirs = getReportDirsFromDirProperty(dirProperty, myRunnerWorkingDir);

    if (reportDirs.size() == 0) {
      System.out.println(reportDirs.size());
      myLogger.warning("no report directories specified.");
    }

    startReportProcessing(reportDirs);
  }

  private void startReportProcessing(List<File> reportDirs) {
    final LinkedBlockingQueue<File> reportsQueue = new LinkedBlockingQueue<File>();
    myDirectoryWatcher = new TestReportDirectoryWatcher(this, reportDirs, reportsQueue);
    myReportProcessor = new TestReportProcessor(this, reportsQueue, myDirectoryWatcher);

    myDirectoryWatcher.start();
    myReportProcessor.start();
  }

  private void obtainLogger(AgentRunningBuild agentRunningBuild) {
    final BuildProgressLogger logger = agentRunningBuild.getBuildLogger();
    if (logger instanceof BaseServerLoggerFacade) {
      myLogger = new TestReportLogger((BaseServerLoggerFacade) logger, myVerboseOutput);
    } else {
      // not expected
    }
  }

  //dirs are not supposed to contain ';' in their path, as it is separator
  private static List<File> getReportDirsFromDirProperty(String dirProperty, final File workingDir) {
    if (dirProperty == null) {
      return Collections.emptyList();
    }

    final String separator = ";";
    final List<File> dirs = new ArrayList<File>();

    if (!dirProperty.endsWith(separator)) {
      dirProperty += separator;
    }

    int from = 0;
    int to = dirProperty.indexOf(separator);

    while (to != -1) {
      dirs.add(FileUtil.resolvePath(workingDir, dirProperty.substring(from, to)));
      from = to + 1;
      to = dirProperty.indexOf(separator, from);
    }
    return dirs;
  }

  public void beforeBuildFinish(@NotNull BuildFinishedStatus buildFinishedStatus) {
    myStopped = true;

    if (!myTestReportParsingEnabled) {
      return;
    }

    switch (buildFinishedStatus) {
      case INTERRUPTED:
        myLogger.warning("build interrupted, plugin may not finish it's work.");
      case FINISHED_SUCCESS:
      case FINISHED_FAILED:
        synchronized (myReportProcessor) {
          try {
            myReportProcessor.join();
          } catch (InterruptedException e) {
            myLogger.debugToAgentLog("plugin thread interrupted.");
          }
        }
        myDirectoryWatcher.logDirectoryTotals();
        break;
    }
  }

  public TestReportLogger getLogger() {
    return myLogger;
  }

  public long getBuildStartTime() {
    return myBuildStartTime;
  }

  public boolean isStopped() {
    return myStopped;
  }

  //"##teamcity[importData id='junit' file='somedir']" service messsage activates watching "somedir" directory
  //"##teamcity[importData id='junit' file='somedir' verbose='true']" does the same and sets output verbose 
  public void processData(@NotNull File file, Map<String, String> arguments) throws Exception {
    final List<File> reportDirs = new ArrayList<File>();
    reportDirs.add(file);

    if (arguments.containsKey(DATA_PROCESSOR_VERBOSE_ARGUMENT)) {
      myVerboseOutput = Boolean.parseBoolean(arguments.get(DATA_PROCESSOR_VERBOSE_ARGUMENT));
    } else {
      myVerboseOutput = false;
    }

    myLogger.setVerboseOutput(myVerboseOutput);

    if (!myTestReportParsingEnabled) {
      myTestReportParsingEnabled = true;
      startReportProcessing(reportDirs);
    } else {
      myDirectoryWatcher.addDirectories(reportDirs);
    }
  }

  @NotNull
  public String getId() {
    return DATA_PROCESSOR_ID;
  }
}