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

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ScannerSide
public class FileSystemCoverageFileValidator implements CoverageFileValidator {
  private static final Logger LOG = Loggers.get(FileSystemCoverageFileValidator.class);
  private FileSystem fileSystem;
  private String languageKey;

  public FileSystemCoverageFileValidator(Configuration configuration, String languageKey, FileSystem fileSystem) {
    this.languageKey = languageKey;
    this.fileSystem = fileSystem;
  }

  public boolean isSupportedAbsolute(String absolutePath) {
    return fileSystem.hasFiles(
      fileSystem.predicates().and(
        fileSystem.predicates().hasAbsolutePath(absolutePath),
        fileSystem.predicates().hasLanguage(languageKey)));
  }

  public boolean isSupportedRelative(String filePath) {
    String absolutePath;
    LOG.debug("Will verify if '{}' is supported", filePath);
    if (isDeterministicSourcePath(filePath)) {
      absolutePath = replaceDeterministicSourcePath(filePath);
      LOG.debug("isDeterministicSourcePath true, replaced {} with {}", filePath, absolutePath);
    } else {
      absolutePath = filePath;
      LOG.debug("isDeterministicSourcePath false, kept {}", filePath);
    }
    return fileSystem.hasFiles(
      fileSystem.predicates().and(
        fileSystem.predicates().hasRelativePath(absolutePath),
        fileSystem.predicates().hasLanguage(languageKey)));
  }

  private boolean isDeterministicSourcePath(String filePath) {
    // FIXME - I'd like to use File.separator, however it won't work with UTs (it is OS dependant)
    return filePath.startsWith("/_/") || filePath.contains("C:\\_\\");
  }

  private String replaceDeterministicSourcePath(String filePath) {
    if (filePath.startsWith("/_/")) {
      filePath = filePath.replaceFirst("/_/", "");
    } else {
      // FIXME for `drive:\\_\\` , I should use a regular expression
      filePath = filePath.replace("C:\\_\\", "");
    }
    return filePath;
  }

}