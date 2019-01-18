/*
 * Copyright 2014 Google Inc. All rights reserved.
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
package org.inferred.freebuilder.processor.util;

import org.inferred.freebuilder.processor.util.feature.FeatureSet;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

/** {@code SourceBuilder} which also handles package declaration and imports. */
public class CompilationUnitBuilder extends AbstractSourceBuilder<CompilationUnitBuilder> {

  private final StringBuilder source = new StringBuilder();
  private final ScopeHandler scopeHandler;
  private final BlockHandler blocks;
  private final SourceParser parser;

  /**
   * Returns a {@link CompilationUnitBuilder} for {@code classToWrite} using {@code features}. The
   * file preamble (package and imports) will be generated automatically, and {@code env} will be
   * inspected for potential import collisions.
   */
  public CompilationUnitBuilder(
      ProcessingEnvironment env,
      QualifiedName classToWrite,
      Collection<QualifiedName> implicitImports,
      FeatureSet features) {
    super(features);

    scopeHandler = new ScopeHandler(env.getElementUtils());
    scopeHandler.predeclareGeneratedType(classToWrite);
    for (QualifiedName implicitImport : implicitImports) {
      scopeHandler.predeclareGeneratedType(implicitImport);
    }

    blocks = new BlockHandler(source, scopeHandler);
    parser = new SourceParser(blocks);

    this.addLine("// Autogenerated code. Do not modify.")
        .addLine("package %s;", classToWrite.getPackage())
        .addLine("");
  }

  @Override
  protected CompilationUnitBuilder getThis() {
    return this;
  }

  @Override
  public CompilationUnitBuilder append(char c) {
    source.append(c);
    parser.parse(c);
    return this;
  }

  @Override
  protected TypeShortener getShortener() {
    return blocks.typeShortener();
  }

  @Override
  public Scope scope() {
    return blocks.scope();
  }

  @Override
  public String toString() {
    return blocks.toString();
  }
}
