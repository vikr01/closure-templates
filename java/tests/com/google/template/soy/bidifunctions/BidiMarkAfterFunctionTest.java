/*
 * Copyright 2009 Google Inc.
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

package com.google.template.soy.bidifunctions;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.template.soy.data.Dir;
import com.google.template.soy.data.SanitizedContents;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.internal.i18n.BidiGlobalDir;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.plugin.java.restricted.testing.SoyJavaSourceFunctionTester;
import com.google.template.soy.pysrc.restricted.PyExpr;
import com.google.template.soy.pysrc.restricted.PyStringExpr;
import com.google.template.soy.shared.SharedRestrictedTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for BidiMarkAfterFunction.
 *
 */
@RunWith(JUnit4.class)
public class BidiMarkAfterFunctionTest {

  @Test
  public void testComputeForJava() {
    // the java source version doesn't use the provider
    BidiMarkAfterFunction fn =
        new BidiMarkAfterFunction(
            () -> {
              throw new UnsupportedOperationException();
            });

    SoyJavaSourceFunctionTester tester =
        new SoyJavaSourceFunctionTester.Builder(fn).withBidiGlobalDir(BidiGlobalDir.LTR).build();

    assertThat(tester.callFunction(StringData.EMPTY_STRING)).isEqualTo("");
    assertThat(tester.callFunction(StringData.forValue("a"))).isEqualTo("");
    assertThat(tester.callFunction(StringData.forValue("\u05E0"))).isEqualTo("\u200E");

    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a"))).isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.LTR))).isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.NEUTRAL)))
        .isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.RTL)))
        .isEqualTo("\u200E");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0")))
        .isEqualTo("\u200E");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.RTL)))
        .isEqualTo("\u200E");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.NEUTRAL)))
        .isEqualTo("\u200E");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.LTR)))
        .isEqualTo("\u200E");

    tester =
        new SoyJavaSourceFunctionTester.Builder(fn).withBidiGlobalDir(BidiGlobalDir.RTL).build();
    assertThat(tester.callFunction(StringData.EMPTY_STRING)).isEqualTo("");
    assertThat(tester.callFunction(StringData.forValue("\u05E0"))).isEqualTo("");
    assertThat(tester.callFunction(StringData.forValue("a"))).isEqualTo("\u200F");

    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0"))).isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.RTL)))
        .isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.NEUTRAL)))
        .isEqualTo("");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("\u05E0", Dir.LTR)))
        .isEqualTo("\u200F");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a"))).isEqualTo("\u200F");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.LTR)))
        .isEqualTo("\u200F");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.NEUTRAL)))
        .isEqualTo("\u200F");
    assertThat(tester.callFunction(SanitizedContents.unsanitizedText("a", Dir.RTL)))
        .isEqualTo("\u200F");
  }

  @Test
  public void testComputeForJsSrc() {
    BidiMarkAfterFunction ltr = new BidiMarkAfterFunction(Suppliers.ofInstance(BidiGlobalDir.LTR));
    BidiMarkAfterFunction rtl = new BidiMarkAfterFunction(Suppliers.ofInstance(BidiGlobalDir.RTL));

    BidiMarkAfterFunction codeSnippet =
        new BidiMarkAfterFunction(
            SharedRestrictedTestUtils.BIDI_GLOBAL_DIR_FOR_JS_ISRTL_CODE_SNIPPET_SUPPLIER);

    JsExpr textExpr = new JsExpr("TEXT_JS_CODE", Integer.MAX_VALUE);
    assertThat(ltr.computeForJsSrc(ImmutableList.of(textExpr)))
        .isEqualTo(new JsExpr("soy.$$bidiMarkAfter(1, TEXT_JS_CODE)", Integer.MAX_VALUE));
    assertThat(codeSnippet.computeForJsSrc(ImmutableList.of(textExpr)))
        .isEqualTo(new JsExpr("soy.$$bidiMarkAfter(IS_RTL?-1:1, TEXT_JS_CODE)", Integer.MAX_VALUE));

    JsExpr isHtmlExpr = new JsExpr("IS_HTML_JS_CODE", Integer.MAX_VALUE);
    assertThat(rtl.computeForJsSrc(ImmutableList.of(textExpr, isHtmlExpr)))
        .isEqualTo(
            new JsExpr(
                "soy.$$bidiMarkAfter(-1, TEXT_JS_CODE, IS_HTML_JS_CODE)", Integer.MAX_VALUE));
    assertThat(codeSnippet.computeForJsSrc(ImmutableList.of(textExpr, isHtmlExpr)))
        .isEqualTo(
            new JsExpr(
                "soy.$$bidiMarkAfter(IS_RTL?-1:1, TEXT_JS_CODE, IS_HTML_JS_CODE)",
                Integer.MAX_VALUE));
  }

  @Test
  public void testComputeForPySrc() {
    BidiMarkAfterFunction codeSnippet =
        new BidiMarkAfterFunction(
            SharedRestrictedTestUtils.BIDI_GLOBAL_DIR_FOR_PY_ISRTL_CODE_SNIPPET_SUPPLIER);

    PyExpr textExpr = new PyStringExpr("'data'");
    assertThat(codeSnippet.computeForPySrc(ImmutableList.of(textExpr)).getText())
        .isEqualTo("bidi.mark_after(-1 if IS_RTL else 1, 'data')");

    PyExpr isHtmlExpr = new PyExpr("is_html", Integer.MAX_VALUE);
    assertThat(codeSnippet.computeForPySrc(ImmutableList.of(textExpr, isHtmlExpr)).getText())
        .isEqualTo("bidi.mark_after(-1 if IS_RTL else 1, 'data', is_html)");
  }
}
