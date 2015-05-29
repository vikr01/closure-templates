/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.jbcsrc.runtime;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.template.soy.base.internal.BaseUtils;

/**
 * Utilities for translating soy symbols to and from strings that are suitable for use in java class
 * files.  These utilities are shared between the compiler and the runtime system.
 */
public final class Names {
  private static final String CLASS_PREFIX = "com.google.template.soy.jbcsrc.gen.";

  private Names() {}

  /**
   * Translate a user controlled Soy name to a form that is safe to encode in a java class, method
   * or field name.
   *
   * <p>Soy identifiers are very simple, they are restricted to the following regex:
   * {@code [a-zA-Z_]([a-zA-Z_0-9])*}. So a template name is just one or more identifiers separated
   * by {@code .} characters.  To escape it, we simply replace all '.'s with '_'s, and all '_'s with
   * '__' and prefix with a package name.
   */
  public static String javaClassNameFromSoyTemplateName(String soyTemplate) {
    checkArgument(BaseUtils.isDottedIdentifier(soyTemplate),
        "%s is not a valid template name.", soyTemplate);
    return CLASS_PREFIX + soyTemplate;
  }

  /**
   * Translates a Java class name generated by {@link #javaClassNameFromSoyTemplateName}, back to
   * the original soy template name.
   */
  public static String soyTemplateNameFromJavaClassName(String javaClass) {
    if (!javaClass.startsWith(CLASS_PREFIX)) {
      throw new IllegalArgumentException("java class: " + javaClass
          + " is not a mangled soy template name");
    }
    return javaClass.substring(CLASS_PREFIX.length());
  }
}
