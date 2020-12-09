/*
 * SonarSource :: .NET :: Shared library
 * Copyright (C) 2014-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.dotnet.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.LogTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class FileSystemCoverageFileValidatorParameterizedTest {
  private boolean hasFiles;
  private boolean expectedResult;

  public FileSystemCoverageFileValidatorParameterizedTest(boolean hasFiles, boolean expectedResult) {
    this.hasFiles = hasFiles;
    this.expectedResult = expectedResult;
  }

  @Rule
  public LogTester logTester = new LogTester();

  @Parameterized.Parameters
  public static Collection input() {
    return Arrays.asList(new Object[][] {
        // hasFiles, expectedResult
        {false, false},
        {true, true},
      }
    );
  }

  @Test
  public void isSupported_returns_hasFiles_result() {
    // arrange
    FilePredicates predicatesFilePredicate = mock(FilePredicates.class);

    FileSystem fs = mock(FileSystem.class);
    when(fs.predicates()).thenReturn(predicatesFilePredicate);
    when(fs.hasFiles(any())).thenReturn(hasFiles);

    Configuration mockConfig = mock(Configuration.class);
    when(mockConfig.get("sonar.projectBaseDir")).thenReturn(Optional.of("/TEST_ROOT/"));

    FileSystemCoverageFileValidator sut = new FileSystemCoverageFileValidator(mockConfig,"key", fs);

    // act & assert
    assertThat(sut.isSupportedAbsolute("x")).isEqualTo(expectedResult);
    //assertThat(logTester.logs(LoggerLevel.INFO)).containsExactly("In case deterministic source paths are used, '/_/' will be replaced '/TEST_ROOT/' ('sonar.projectBaseDir')");
  }

}