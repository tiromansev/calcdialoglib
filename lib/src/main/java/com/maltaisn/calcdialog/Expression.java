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

    Expression() {}

    void clear() {
        numbers.clear();
        operators.clear();
    }

    boolean isEmpty() {
        return numbers.size() == 0;
    }

    /**
     * Evaluate the expression and return the result.
     * @param priority     Whether to apply operation priority or not.
     * @param scale        Scale used for division.
     * @param roundingMode Rounding mode used for division.
     * @return The result.
     * @throws ArithmeticException if a division by zero occurred.
     */
    @NonNull
    BigDecimal evaluate(boolean priority, int scale, RoundingMode roundingMode) {
        if (numbers.size() == 1) return numbers.get(0);

        List<BigDecimal> nbs = new ArrayList<>(numbers);
        List<Operator> ops = new ArrayList<>(operators);

        if (nbs.size() != ops.size() + 1) {
            ops.remove(ops.size()-1);
        }

        if (priority) {
            // Evaluate products and quotients
            int i = 0;
            while (i < ops.size()) {
                Operator op = ops.get(i);
                if (op == Operator.MULTIPLY) {
                    ops.remove(i);
                    BigDecimal n1 = nbs.get(i);
                    BigDecimal n2 = nbs.remove(i + 1);
                    nbs.set(i, n1.multiply(n2));
                } else if (op == Operator.DIVIDE) {
                    ops.remove(i);
                    BigDecimal n1 = nbs.get(i);
                    BigDecimal n2 = nbs.remove(i + 1);
                    nbs.set(i, n1.divide(n2, scale, roundingMode));
                } else if (op == Operator.PERCENT) {
                    ops.remove(i);
                    BigDecimal n1 = nbs.get(i);
                    BigDecimal n2 = nbs.remove(i + 1);
                    BigDecimal percentValue = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                    nbs.set(i, n1.multiply(percentValue));
                } else {
                    i++;
                }
            }
        }

        // Evaluate the rest
        while (!ops.isEmpty()) {
            Operator op = ops.remove(0);
            boolean nextOpIsPercent = false;
            //if we have next operator = percent
            if (ops.size() - 1 > 1) {
                Operator nextOp = ops.get(0);
                nextOpIsPercent = nextOp == Operator.PERCENT;
                if (nextOpIsPercent) {
                    ops.remove(0);
                }
            }
            BigDecimal n1 = nbs.get(0);
            BigDecimal n2 = nbs.remove(1);
            BigDecimal n3 = nextOpIsPercent ? n1.multiply(n2.divide(BigDecimal.valueOf(100), scale, roundingMode)) : n2;
            if (op == Operator.ADD) {
                nbs.set(0, n1.add(n3));
            } else if (op == Operator.SUBTRACT) {
                nbs.set(0, n1.subtract(n3));
            } else if (op == Operator.MULTIPLY) {
                nbs.set(0, n1.multiply(n3));
            } else {
                nbs.set(0, n1.divide(n3, scale, roundingMode));
            }
        }

        return nbs.remove(0).stripTrailingZeros();
    }

    /**
     * Format the expression to a string.
     * @param nbFormat The format to use for formatting numbers.
     * @return The expression string.
     */
    String format(NumberFormat nbFormat) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            sb.append(nbFormat.format(numbers.get(i)));
            sb.append(' ');
            if (i < operators.size()) {
                sb.append(operators.get(i).symbol);
            }
            sb.append(' ');
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
