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
  private static final String PROJECT_BASE_DIR = "sonar.projectBaseDir";
  private static final Logger LOG = Loggers.get(FileSystemCoverageFileValidator.class);
  private String projectBaseDir;
  private FileSystem fileSystem;
  private String languageKey;

  public FileSystemCoverageFileValidator(Configuration configuration, String languageKey, FileSystem fileSystem) {
    this.projectBaseDir = configuration.get(PROJECT_BASE_DIR).orElse(null);
    this.languageKey = languageKey;
    this.fileSystem = fileSystem;
    if (projectBaseDir == null) {
      LOG.warn("Could not retrieve analysis parameter '{}'", PROJECT_BASE_DIR);
    } else {
      LOG.info("In case deterministic source paths are used, '/_/' will be replaced '{}' ('{}')",
        projectBaseDir, PROJECT_BASE_DIR);
    }
  }

  public boolean isSupported(String filePath) {
    String absolutePath;
    if (projectBaseDir != null && filePath.startsWith("/_/")) {
      filePath = filePath.replaceFirst("/_/", "");
      String separator;
      if (projectBaseDir.contains("\\")) {
        separator = "\\";
        filePath = filePath.replace('/', '\\');
      } else {
        separator = "/";
      }
      absolutePath = projectBaseDir + separator + filePath;
    } else {
      absolutePath = filePath;
    }
    return fileSystem.hasFiles(
      fileSystem.predicates().and(
        fileSystem.predicates().hasAbsolutePath(absolutePath),
        fileSystem.predicates().hasLanguage(languageKey)));
  }

}