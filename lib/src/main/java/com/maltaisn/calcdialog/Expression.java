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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

class Expression implements Parcelable {

    final List<BigDecimal> numbers = new ArrayList<>();
    final List<Operator> operators = new ArrayList<>();

    Expression() {
    }

    void clear() {
        numbers.clear();
        operators.clear();
    }

    boolean isEmpty() {
        return numbers.size() == 0;
    }

    /**
     * Evaluate the expression and return the result.
     *
     * @param priority     Whether to apply operation priority or not.
     * @param scale        Scale used for division.
     * @param roundingMode Rounding mode used for division.
     * @return The result.
     * @throws ArithmeticException if a division by zero occurred.
     */
    @NonNull
    BigDecimal evaluate(boolean priority, int scale, RoundingMode roundingMode) {
        if (numbers.size() == 1) {
            if (operators.size() == 1 && operators.get(0) == Operator.PERCENT) {
                return numbers.get(0).divide(BigDecimal.valueOf(100), scale, roundingMode);
            }
            return numbers.get(0);
        }

        List<BigDecimal> nbs = new ArrayList<>(numbers);
        List<Operator> ops = new ArrayList<>(operators);

        if (priority) {
            // Evaluate products and quotients.
            // i indexes operators, n indexes the left operand number. PERCENT is postfix
            // and has no operand slot of its own, so every percent passed over makes the
            // two indices diverge — numbers must always be addressed through n, not i.
            int i = 0;
            int n = 0;
            while (i < ops.size()) {
                Operator op = ops.get(i);
                if (op == Operator.MULTIPLY || op == Operator.DIVIDE) {
                    if (n + 1 >= nbs.size()) {
                        // Dangling operator without a right operand: drop it.
                        ops.remove(i);
                        continue;
                    }

                    BigDecimal n1 = nbs.get(n);
                    BigDecimal n2 = nbs.remove(n + 1);

                    boolean nextIsPercent = i + 1 < ops.size() && ops.get(i + 1) == Operator.PERCENT;
                    ops.remove(i);
                    if (nextIsPercent) {
                        n2 = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        ops.remove(i);
                    }

                    if (op == Operator.MULTIPLY) {
                        nbs.set(n, n1.multiply(n2));
                    } else {
                        nbs.set(n, n1.divide(n2, scale, roundingMode));
                    }

                } else if (op == Operator.PERCENT) {
                    if (i == 0) {
                        BigDecimal result = nbs.get(0).divide(BigDecimal.valueOf(100), scale, roundingMode);
                        nbs.set(0, result);
                        ops.remove(i);
                        continue;
                    }

                    Operator nextOp = null;
                    if (i + 1 < ops.size()) {
                        nextOp = ops.get(i + 1);
                    }

                    if ((nextOp == Operator.MULTIPLY || nextOp == Operator.DIVIDE) && n + 1 < nbs.size()) {
                        // "x % × y" / "x % ÷ y": fold the percent into the product/quotient.
                        ops.remove(i);
                        BigDecimal n1 = nbs.get(n);
                        BigDecimal n2 = nbs.remove(n + 1);
                        BigDecimal result = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        if (nextOp == Operator.MULTIPLY) {
                            nbs.set(n, n1.multiply(result));
                        } else {
                            nbs.set(n, n1.divide(result, scale, roundingMode));
                        }
                        // The × or ÷ has shifted into position i.
                        ops.remove(i);
                    } else {
                        // Additive percent ("a + b %"): resolved in the sequential pass below.
                        i++;
                    }

                } else {
                    // ADD / SUBTRACT: consumes one operand slot.
                    i++;
                    n++;
                }
            }
        }

        // Evaluate the rest
        while (!ops.isEmpty()) {
            Operator op = ops.remove(0);

            if (op == Operator.PERCENT) {
                // Leading or dangling percent: apply it to the accumulated value.
                nbs.set(0, nbs.get(0).divide(BigDecimal.valueOf(100), scale, roundingMode));
                continue;
            }

            if (nbs.size() < 2) {
                // Dangling operator without a right operand: nothing to apply.
                continue;
            }

            Operator nextOp = ops.isEmpty() ? null : ops.get(0);

            BigDecimal n1 = nbs.get(0);
            BigDecimal n2 = nbs.remove(1);

            if (op == Operator.ADD) {
                if (nextOp == Operator.PERCENT) {
                    BigDecimal result = n1.multiply(n2).divide(BigDecimal.valueOf(100), scale, roundingMode);
                    nbs.set(0, n1.add(result));
                    ops.remove(0);
                } else {
                    nbs.set(0, n1.add(n2));
                }

            } else if (op == Operator.SUBTRACT) {
                if (nextOp == Operator.PERCENT) {
                    BigDecimal result = n1.multiply(n2).divide(BigDecimal.valueOf(100), scale, roundingMode);
                    nbs.set(0, n1.subtract(result));
                    ops.remove(0);
                } else {
                    nbs.set(0, n1.subtract(n2));
                }
            } else if (op == Operator.MULTIPLY) {
                nbs.set(0, n1.multiply(n2));
            } else {
                nbs.set(0, n1.divide(n2, scale, roundingMode));
            }
        }

        return nbs.remove(0).stripTrailingZeros();
    }

    /**
     * Format the expression to a string.
     *
     * @param nbFormat The format to use for formatting numbers.
     * @return The expression string.
     */
    String format(NumberFormat nbFormat) {
        StringBuilder sb = new StringBuilder();

        int opsIndex = 0;
        for (int i = 0; i < numbers.size(); i++) {
            sb.append(nbFormat.format(numbers.get(i)));
            sb.append(' ');

            if (opsIndex < operators.size()) {
                Operator operator = operators.get(opsIndex);
                sb.append(operator.symbol);
                sb.append(' ');

                if (operator == Operator.PERCENT) {
                    int nextOpIndex = opsIndex + 1;
                    if (nextOpIndex < operators.size()) {
                        sb.append(operators.get(nextOpIndex).symbol);
                        sb.append(' ');
                        opsIndex++;
                    }
                }
                opsIndex++;
            }
        }
        if (sb.length() != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return format(NumberFormat.getInstance());
    }

    ////////// PARCELABLE //////////
    private Expression(Parcel in) {
        in.readList(numbers, BigDecimal.class.getClassLoader());
        in.readList(operators, Operator.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeList(numbers);
        out.writeList(operators);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Expression> CREATOR = new Creator<Expression>() {
        @Override
        public Expression createFromParcel(Parcel in) {
            return new Expression(in);
        }

        @Override
        public Expression[] newArray(int size) {
            return new Expression[size];
        }
    };

    enum Operator {
        ADD('+'), SUBTRACT('−'), MULTIPLY('×'), DIVIDE('÷'), PERCENT('%');

        char symbol;

        Operator(char symbol) {
            this.symbol = symbol;
        }
    }

}
