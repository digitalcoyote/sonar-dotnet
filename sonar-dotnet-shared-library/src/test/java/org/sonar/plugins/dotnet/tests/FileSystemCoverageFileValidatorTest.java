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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.LogTester;

import static org.assertj.core.api.Assertions.assertThat;
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

    Configuration mockConfig = mock(Configuration.class);

    FileSystemCoverageFileValidator sut = new FileSystemCoverageFileValidator(mockConfig,"key", fs);
    sut.isSupportedRelative("/_/some/path/file.cs");

    assertThat(argumentCaptor.getValue()).isEqualTo("some/path/file.cs");
  }

  @Test
  public void if_projectBaseDir_returns_windows_path_with_drive_replaces_base_dir_and_slash() {
    FileSystem fs = mock(FileSystem.class);
    FilePredicates filePredicates = mock(FilePredicates.class);

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    when(filePredicates.hasRelativePath(argumentCaptor.capture())).thenReturn(mock(FilePredicate.class));
    when(fs.predicates()).thenReturn(filePredicates);

    Configuration mockConfig = mock(Configuration.class);

    FileSystemCoverageFileValidator sut = new FileSystemCoverageFileValidator(mockConfig,"key", fs);
    sut.isSupportedRelative("C:\\_\\some\\path\\file.cs");

    assertThat(argumentCaptor.getValue()).isEqualTo("some\\path\\file.cs");
  }
}