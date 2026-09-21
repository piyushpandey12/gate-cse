package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary

/**
 * Clean mathematical formula formatter that converts raw LaTeX notation into clear,
 * beautiful mathematical layout using Unicode mathematical symbols, superscripts,
 * subscripts, and highlighted variables.
 */
object FormulaFormatter {

    fun formatLatexToReadable(raw: String): String {
        var text = raw.trim()

        // Replace common LaTeX math commands with elegant unicode math
        text = text.replace("\\sum_{i=1}^m", "∑ (from i=1 to m)")
        text = text.replace("\\sum_i", "∑_i")
        text = text.replace("\\sum", "∑")
        text = text.replace("\\prod", "∏")
        text = text.replace("\\cdot", " · ")
        text = text.replace("\\approx", " ≈ ")
        text = text.replace("\\gg", " ≫ ")
        text = text.replace("\\ll", " ≪ ")
        text = text.replace("\\le", " ≤ ")
        text = text.replace("\\ge", " ≥ ")
        text = text.replace("\\neq", " ≠ ")
        text = text.replace("\\times", " × ")
        text = text.replace("\\implies", " ⟹ ")
        text = text.replace("\\to", " → ")
        text = text.replace("\\in", " ∈ ")
        text = text.replace("\\notin", " ∉ ")
        text = text.replace("\\infty", " ∞ ")
        text = text.replace("\\eta", "η")
        text = text.replace("\\theta", "θ")
        text = text.replace("\\Theta", "Θ")
        text = text.replace("\\Omega", "Ω")
        text = text.replace("\\alpha", "α")
        text = text.replace("\\beta", "β")
        text = text.replace("\\lambda", "λ")
        text = text.replace("\\mu", "μ")
        text = text.replace("\\sigma", "σ")
        text = text.replace("\\delta", "δ")
        text = text.replace("\\Delta", "Δ")
        text = text.replace("\\quad", "   ")
        text = text.replace("\\qquad", "      ")
        text = text.replace("\\text{", "")
        text = text.replace("\\log_2", "log₂")
        text = text.replace("\\log_b", "log_b")
        text = text.replace("\\log", "log")
        text = text.replace("\\sqrt", "√")

        // Handle fractions: \frac{A}{B} -> (A) / (B)
        val fracRegex = Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")
        var fracMatched = fracRegex.find(text)
        while (fracMatched != null) {
            val num = fracMatched.groupValues[1]
            val den = fracMatched.groupValues[2]
            text = text.replace(fracMatched.value, "(${num.trim()}) / (${den.trim()})")
            fracMatched = fracRegex.find(text)
        }

        // Handle subscripts like R_{min} -> R_min, T_{prop} -> T_prop
        text = text.replace(Regex("""_\{([^{}]+)\}""")) { match ->
            "_${match.groupValues[1]}"
        }

        // Handle superscripts like ^k or ^{k}
        text = text.replace(Regex("""\^\{([^{}]+)\}""")) { match ->
            toSuperscript(match.groupValues[1])
        }
        text = text.replace(Regex("""\^([0-9nkm])""")) { match ->
            toSuperscript(match.groupValues[1])
        }

        // Clean remaining braces
        text = text.replace("{", "").replace("}", "")

        // Clean double backslashes or remaining lone backslashes
        text = text.replace("\\", "")

        // Normalize multiple spaces
        text = text.replace(Regex("""\s{2,}"""), " ")

        return text.trim()
    }

    private fun toSuperscript(str: String): String {
        val map = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
            'n' to 'ⁿ', 'i' to 'ⁱ'
        )
        val sb = StringBuilder()
        for (ch in str) {
            sb.append(map[ch] ?: "^$ch")
        }
        return sb.toString()
    }
}

/**
 * Beautiful Math Formula Display Composable
 * Renders mathematical formulas in high-contrast card styling with formatted mathematical symbols.
 */
@Composable
fun MathFormulaView(
    rawFormula: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val formatted = remember(rawFormula) {
        FormulaFormatter.formatLatexToReadable(rawFormula)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Horizontally scrollable row in case formula is wide
            val scrollState = rememberScrollState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        lineHeight = 24.sp
                    )
                )
            }
        }
    }
}
