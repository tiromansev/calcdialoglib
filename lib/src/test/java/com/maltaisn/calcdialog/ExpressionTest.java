/*
 * Copyright 2019 Nicolas Maltais
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

package com.maltaisn.calcdialog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExpressionTest {

    @Test
    public void twoSum() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("12.1"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("-24.8"));

        BigDecimal result = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result, new BigDecimal("-12.7"));
    }

    @Test
    public void sumAndMultiply() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("4"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("5"));

        BigDecimal result1 = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result1, new BigDecimal("35"));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(result2, new BigDecimal("23"));
    }

    @Test
    public void longExpression() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("-1"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("6"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("5"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("12"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("8"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("-10"));

        BigDecimal result1 = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result1, new BigDecimal("12.41666667"));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(result2, new BigDecimal("12.33333333"));
    }

    @Test
    public void stripTrailingZeroes() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("1"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("8"));

        BigDecimal result = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result, new BigDecimal("0.125"));
    }

    @Test
    public void percentTest() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("3"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("33"), result1);
    }

    @Test
    public void percentTest2() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("60").toPlainString(), result1.toPlainString());
    }

    @Test
    public void percentTest3() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("20"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("44"), result1);
    }

    @Test
    public void percentTest4() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("10"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("30"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("58.8"), result1);
    }

    @Test
    public void percentTest5() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("2"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("5.5"), result1);
    }

    @Test
    public void percentTest6() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("2"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("5.7"), result1);
    }

    @Test
    public void percentTest7() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("0.5"), result1);
    }

    @Test
    public void percentTest8() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("0.7"), result1);
    }


    @Test
    public void percentTest9() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("3"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("7"), result1);
    }

    @Test
    public void percentTest10() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("20"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("-4"), result1);
    }

    @Test
    public void percentTest11() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("10"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("30"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("-13.2"), result1);
    }

    @Test
    public void percentTest12() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("10"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("30"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("22.8"), result1);
    }

    @Test
    public void percentTest13() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("3"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("30").toPlainString(), result1.toPlainString());
    }

    @Test
    public void percentTest14() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("10"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("100").toPlainString(), result1.toPlainString());
    }

    @Test
    public void percentTest15() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("10"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("20"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("30"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("24").toPlainString(), result1.toPlainString());
    }

    @Test
    public void percentTest16() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("2"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("3"), result1);
    }

    @Test
    public void percentTest17() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("50"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("40"));
        expr.operators.add(Expression.Operator.PERCENT);
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("2"));

        BigDecimal result1 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1.2"), result1);
    }
}
