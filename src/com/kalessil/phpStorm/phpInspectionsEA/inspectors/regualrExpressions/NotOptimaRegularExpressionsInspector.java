package com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.classesStrategy.ShortClassDefinitionStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy.AllowedModifierCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy.DeprecatedModifiersCheckStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy.UselessDollarEndOnlyModifierStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regualrExpressions.modifiersStrategy.UselessMultiLineModifierStrategy;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotOptimaRegularExpressionsInspector extends BasePhpInspection {

    @NotNull
    public String getShortName() {
        return "NotOptimaRegularExpressionsInspection";
    }

    private static HashSet<String> functions = null;
    private static HashSet<String> getFunctions() {
        if (null == functions) {
            functions = new HashSet<String>();

            functions.add("preg_filter");
            functions.add("preg_grep");
            functions.add("preg_match_all");
            functions.add("preg_match");
            functions.add("preg_replace_callback");
            functions.add("preg_replace");
            functions.add("preg_split");
        }

        return functions;
    }

    static private Pattern regexWithModifiers = null;
    static {
        regexWithModifiers = Pattern.compile("^\\/(.*)\\/([a-zA-Z]+)?$");
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String strFunctionName = reference.getName();
                if (StringUtil.isEmpty(strFunctionName) || !getFunctions().contains(strFunctionName)) {
                    return;
                }

                PsiElement[] params = reference.getParameters();
                if (params.length < 2 || !(params[0] instanceof StringLiteralExpression)) {
                    return;
                }

                String regex = ((StringLiteralExpression) params[0]).getContents();
                if (!StringUtil.isEmpty(regex)) {
                    Matcher regexMatcher = regexWithModifiers.matcher(regex);
                    if (regexMatcher.find()) {
                        String phpRegexPattern   = regexMatcher.group(1);
                        String phpRegexModifiers = regexMatcher.group(2);

                        checkCall(strFunctionName, (StringLiteralExpression) params[0], phpRegexPattern, phpRegexModifiers);
                    }
                }
            }

            private void checkCall (String strFunctionName, StringLiteralExpression target, String regex, String modifiers) {
                /**
                 * /no-az-chars/i => /no-az-chars/
                 * /no-dot-.-char/s => /no-dot-.-char/
                 *
                 * + /no-$/D => /no-$/
                 * + /no-^-or-$-occurrences/m => /no-^-or-$-occurrences/
                 * + /regexp/e => mark as deprecated, use preg_replace_callback instead
                 * + Check allowed PHP modifiers: eimsuxADJSUX
                 */
                DeprecatedModifiersCheckStrategy.apply(modifiers, target, holder);
                AllowedModifierCheckStrategy.apply(modifiers, target, holder);
                UselessMultiLineModifierStrategy.apply(modifiers, regex, target, holder);
                UselessDollarEndOnlyModifierStrategy.apply(modifiers, regex, target, holder);

                /**
                 * Simplification
                 *
                 * /^text/ => 0 === strpos(...) (match)
                 * /text/ => false !== strpos(...) (match) / str_replace (replace)
                 * /^text/i => 0 === stripos(...) (match)
                 * /text/i => false !== stripos(...) (match) / str_ireplace (replace)
                 * preg_quote => warning if second argument is not presented
                 */

                /** Classes:
                 * + [0-9] => \d
                 * + [^0-9] => \D
                 * + [:digit:] => \d
                 * + [:word:] => \w
                 * + [^\w] => \W
                 * + [^\s] => \S
                 */
                ShortClassDefinitionStrategy.apply(regex, target, holder);

                /**
                 * Optimizations
                 *
                 * [seq][seq]... => [seq]{N}
                 * [seq][seq]+ => [seq]{2,}
                 * [seq][seq]* => [seq]+
                 * [seq][seq]? => [seq]{1,2}
                 * /. * ···/, /···. * / => /···/ (remove leading and trailing .* without ^ or $, note: if no back-reference to \0)
                 * (...) => (?:...) (if there is no backreference)
                 */
            }
        };
    }
}
