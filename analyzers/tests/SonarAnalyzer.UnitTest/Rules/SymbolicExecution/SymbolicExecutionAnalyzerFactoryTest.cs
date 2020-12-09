﻿/*
 * SonarAnalyzer for .NET
 * Copyright (C) 2015-2020 SonarSource SA
 * mailto: contact AT sonarsource DOT com
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

using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Threading;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.Diagnostics;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using SonarAnalyzer.Rules.SymbolicExecution;

namespace SonarAnalyzer.UnitTest.Rules.SymbolicExecution
{
    [TestClass]
    public class SymbolicExecutionAnalyzerFactoryTest
    {
        private const string EmptyNullableValueAccess = "S3655";
        private const string ObjectsShouldNotBeDisposedMoreThanOnce = "S3966";
        private const string PublicMethodArgumentsShouldBeCheckedForNull = "S3900";
        private const string EmptyCollectionsShouldNotBeEnumerated = "S4158";
        private const string ConditionEvaluatesToConstantBug = "S2583";
        private const string ConditionEvaluatesToConstantCodeSmell = "S2589";
        private const string InvalidCastToInterface = "S1944";
        private const string NullPointerDereference = "S2259";
        private const string RestrictDeserializedTypes = "S5773";
        private const string InitializationVectorShouldBeRandom = "S3329";
        private const string HashesShouldHaveUnpredictableSalt = "S2053";

        [TestMethod]
        public void SupportedDiagnostics_ReturnsSymbolicExecutionRuleDescriptors()
        {
            var sut = new SymbolicExecutionAnalyzerFactory();
            var supportedDiagnostics = sut.SupportedDiagnostics.Select(descriptor => descriptor.Id).ToList();

            CollectionAssert.AreEquivalent(supportedDiagnostics, new[]
            {
                EmptyNullableValueAccess,
                ObjectsShouldNotBeDisposedMoreThanOnce,
                PublicMethodArgumentsShouldBeCheckedForNull,
                EmptyCollectionsShouldNotBeEnumerated,
                ConditionEvaluatesToConstantBug,
                ConditionEvaluatesToConstantCodeSmell,
                InvalidCastToInterface,
                NullPointerDereference,
                RestrictDeserializedTypes,
                InitializationVectorShouldBeRandom,
                HashesShouldHaveUnpredictableSalt
            });
        }

        [DataTestMethod]
        [DataRow(ReportDiagnostic.Default)]
        [DataRow(ReportDiagnostic.Error)]
        [DataRow(ReportDiagnostic.Info)]
        [DataRow(ReportDiagnostic.Warn)]
        // According to the doc of Hidden: Non-visible to user. The diagnostic is reported to the IDE diagnostic engine, however.
        // https://docs.microsoft.com/en-us/visualstudio/code-quality/use-roslyn-analyzers?view=vs-2019#rule-severity
        [DataRow(ReportDiagnostic.Hidden)]
        public void GetEnabledAnalyzers_ReturnsDiagnostic_WhenEnabled(ReportDiagnostic reportDiagnostic)
        {
            var sut = new SymbolicExecutionAnalyzerFactory();
            var diagnostics = new Dictionary<string, ReportDiagnostic>
            {
                {EmptyNullableValueAccess, reportDiagnostic},
                {ObjectsShouldNotBeDisposedMoreThanOnce, ReportDiagnostic.Suppress},
                {EmptyCollectionsShouldNotBeEnumerated, ReportDiagnostic.Suppress},
                {ConditionEvaluatesToConstantBug, ReportDiagnostic.Suppress},
                {ConditionEvaluatesToConstantCodeSmell, ReportDiagnostic.Suppress},
                {InvalidCastToInterface, ReportDiagnostic.Suppress},
                {NullPointerDereference, ReportDiagnostic.Suppress},
                {InitializationVectorShouldBeRandom, ReportDiagnostic.Suppress},
                {RestrictDeserializedTypes, ReportDiagnostic.Suppress}
            }.ToImmutableDictionary();
            var context = CreateSyntaxNodeAnalysisContext(diagnostics);
            var analyzers = sut.GetEnabledAnalyzers(context).ToList();
            var enabledAnalyzers =
                analyzers
                    .SelectMany(analyzer => analyzer.SupportedDiagnostics.Select(descriptor => descriptor.Id))
                    .ToList();

            CollectionAssert.AreEquivalent(enabledAnalyzers, new[] {EmptyNullableValueAccess});
        }

        [TestMethod]
        public void GetEnabledAnalyzers_ReturnsEmptyList_WhenDiagnosticsAreDisabled()
        {
            var sut = new SymbolicExecutionAnalyzerFactory();
            var diagnostics = new Dictionary<string, ReportDiagnostic>
            {
                {EmptyNullableValueAccess, ReportDiagnostic.Suppress},
                {ObjectsShouldNotBeDisposedMoreThanOnce, ReportDiagnostic.Suppress},
                {EmptyCollectionsShouldNotBeEnumerated, ReportDiagnostic.Suppress},
                {ConditionEvaluatesToConstantBug, ReportDiagnostic.Suppress},
                {ConditionEvaluatesToConstantCodeSmell, ReportDiagnostic.Suppress},
                {InvalidCastToInterface, ReportDiagnostic.Suppress},
                {NullPointerDereference, ReportDiagnostic.Suppress},
                {InitializationVectorShouldBeRandom, ReportDiagnostic.Suppress},
                {RestrictDeserializedTypes, ReportDiagnostic.Suppress}
            }.ToImmutableDictionary();
            var context = CreateSyntaxNodeAnalysisContext(diagnostics);
            var analyzers = sut.GetEnabledAnalyzers(context).ToList();
            var enabledAnalyzers =
                analyzers
                    .SelectMany(analyzer => analyzer.SupportedDiagnostics.Select(descriptor => descriptor.Id))
                    .ToList();

            CollectionAssert.AreEquivalent(new List<string>(), enabledAnalyzers);
        }

        private static SyntaxNodeAnalysisContext CreateSyntaxNodeAnalysisContext(ImmutableDictionary<string, ReportDiagnostic> diagnostics)
        {
            var syntaxTree = CSharpSyntaxTree.ParseText(@"public class Empty { }", new CSharpParseOptions());

            return new SyntaxNodeAnalysisContext(syntaxTree.GetRoot(),
                CreateCSharpCompilation(diagnostics, syntaxTree).GetSemanticModel(syntaxTree),
                new AnalyzerOptions(ImmutableArray<AdditionalText>.Empty),
                diagnostic => { },
                diagnostic => true,
                CancellationToken.None);
        }

        private static CSharpCompilation CreateCSharpCompilation(ImmutableDictionary<string, ReportDiagnostic> diagnostics, SyntaxTree syntaxTree) =>
            CSharpCompilation
                .Create("Assembly.dll", new[] {syntaxTree}, null, new CSharpCompilationOptions(OutputKind.DynamicallyLinkedLibrary)
                .WithSpecificDiagnosticOptions(diagnostics));
    }
}
