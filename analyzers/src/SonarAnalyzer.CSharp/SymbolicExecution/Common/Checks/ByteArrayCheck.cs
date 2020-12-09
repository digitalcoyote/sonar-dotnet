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

using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using SonarAnalyzer.Helpers;
using SonarAnalyzer.SymbolicExecution.Common.Constraints;

namespace SonarAnalyzer.SymbolicExecution.Common.Checks
{
    internal sealed class ByteArrayCheck : ExplodedGraphCheck
    {
        public ByteArrayCheck(AbstractExplodedGraph explodedGraph) : base(explodedGraph)
        {
        }

        public override ProgramState PostProcessInstruction(ProgramPoint programPoint, ProgramState programState) =>
            programPoint.CurrentInstruction switch
            {
                ArrayCreationExpressionSyntax arrayCreation => ArrayCreationPostProcess(arrayCreation, programState),
                InvocationExpressionSyntax invocation => AssignmentExpressionPostProcess(invocation, programState),
                _ => programState
            };

        private ProgramState ArrayCreationPostProcess(ArrayCreationExpressionSyntax arrayCreation, ProgramState programState) =>
            semanticModel.GetTypeInfo(arrayCreation).Type.Is(KnownType.System_Byte_Array) && programState.HasValue
                ? programState.SetConstraint(programState.PeekValue(), ByteArraySymbolicValueConstraint.Constant)
                : programState;

        private ProgramState AssignmentExpressionPostProcess(InvocationExpressionSyntax invocation, ProgramState programState) =>
            IsSanitizer(invocation, semanticModel)
            && semanticModel.GetSymbolInfo(invocation.ArgumentList.Arguments[0].Expression).Symbol is {} symbol
            && symbol.GetSymbolType().Is(KnownType.System_Byte_Array)
            && symbol.HasConstraint(ByteArraySymbolicValueConstraint.Constant, programState)
                ? programState.SetConstraint(programState.GetSymbolValue(symbol), ByteArraySymbolicValueConstraint.Modified)
                : programState;

        private static bool IsSanitizer(InvocationExpressionSyntax invocation, SemanticModel semanticModel) =>
            invocation.Expression is MemberAccessExpressionSyntax memberAccessExpressionSyntax
            && (memberAccessExpressionSyntax.NameIs("GetBytes") || memberAccessExpressionSyntax.NameIs("GetNonZeroBytes"))
            && semanticModel.GetSymbolInfo(invocation).Symbol is {} symbol
            && symbol.ContainingType.IsAny(KnownType.System_Security_Cryptography_RNGCryptoServiceProvider,
                                           KnownType.System_Security_Cryptography_RandomNumberGenerator);
    }
}
