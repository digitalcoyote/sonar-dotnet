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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSystemCoverageFileValidatorTest {
  @Rule
  public LogTester logTester = new LogTester();

  @Test
  // FIXME change name
  public void if_projectBaseDir_not_found_log_warn_do_not_replace() {
    FileSystem fs = mock(FileSystem.class);
    FilePredicates filePredicates = mock(FilePredicates.class);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    when(filePredicates.hasRelativePath(argumentCaptor.capture())).thenReturn(mock(FilePredicate.class));
    when(fs.predicates()).thenReturn(filePredicates);
    when(fs.inputFiles(any())).thenReturn(Collections.emptyList());

    Configuration mockConfig = mock(Configuration.class);

    FileSystemCoverageFileValidator sut = new FileSystemCoverageFileValidator(mockConfig,"key", fs);
    Optional<InputFile> result = sut.getFilesByRelativePath("/_/some/path/file.cs");

    assertThat(result).isEmpty();
    List<String> logs = logTester.logs(LoggerLevel.DEBUG);
    //assertThat(logs).hasSize(3);
    assertThat(argumentCaptor.getValue()).isEqualTo("some/path/file.cs");
  }

  @Test
  public void if_projectBaseDir_returns_windows_path_with_drive_replaces_base_dir_and_slash() {
    FileSystem fs = mock(FileSystem.class);
    FilePredicates filePredicates = mock(FilePredicates.class);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    when(filePredicates.hasRelativePath(argumentCaptor.capture())).thenReturn(mock(FilePredicate.class));
    when(fs.predicates()).thenReturn(filePredicates);

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(URI.create("a"));
    when(fs.inputFiles(any())).thenReturn(Arrays.asList(inputFile));

    Configuration mockConfig = mock(Configuration.class);

    FileSystemCoverageFileValidator sut = new FileSystemCoverageFileValidator(mockConfig,"key", fs);
    //Iterable<InputFile> result = sut.getFilesByRelativePath("C:\\_\\some\\path\\file.cs");
    Optional<InputFile> result = sut.getFilesByRelativePath("C:\\_\\some\\path\\file.cs");

    //assertThat(result).contains(inputFile);
    assertThat(argumentCaptor.getValue()).isEqualTo("some/path/file.cs");
  }
}