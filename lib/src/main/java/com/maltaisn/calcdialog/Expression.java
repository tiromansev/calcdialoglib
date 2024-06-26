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

        if (nbs.size() != ops.size() + 1) {
            if (!ops.contains(Operator.PERCENT)) {
                //ops.remove(ops.size()-1);
            }
        }

        if (priority) {
            // Evaluate products and quotients
            int i = 0;
            while (i < ops.size()) {
                Operator op = ops.get(i);
                if (op == Operator.MULTIPLY) {

                    BigDecimal n1 = nbs.get(i);
                    BigDecimal n2 = nbs.remove(i + 1);

                    Operator nextOp = null;
                    if (i + 1 < ops.size()) {
                        nextOp = ops.get(i + 1);
                    }

                    ops.remove(i);

                    if (nextOp == Operator.PERCENT) {
                        BigDecimal result = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        nbs.set(i, n1.multiply(result));
                        ops.remove(nextOp);
                    } else {
                        nbs.set(i, n1.multiply(n2));
                    }

                } else if (op == Operator.DIVIDE) {
                    BigDecimal n1 = nbs.get(i);
                    BigDecimal n2 = nbs.remove(i + 1);

                    Operator nextOp = null;
                    if (i + 1 < ops.size()) {
                        nextOp = ops.get(i + 1);
                    }

                    ops.remove(i);

                    if (nextOp == Operator.PERCENT) {
                        BigDecimal result = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        nbs.set(i, n1.divide(result, scale, roundingMode));
                        ops.remove(nextOp);
                    } else {
                        nbs.set(i, n1.divide(n2, scale, roundingMode));
                    }
                } else if (op == Operator.PERCENT) {
                    if (i == 0) {
                        BigDecimal n1 = nbs.get(i);
                        BigDecimal result = n1.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        nbs.set(0, result);
                        ops.remove(i);
                        continue;
                    }

                    Operator prevOp = null;
                    Operator nextOp = null;
                    if (i - 1 >= 0 && i - 1 < ops.size()) {
                        prevOp = ops.get(i - 1);
                    }
                    if (i + 1 < ops.size()) {
                        nextOp = ops.get(i + 1);
                    }

                    if (nextOp == Operator.MULTIPLY || nextOp == Operator.DIVIDE) {
                        ops.remove(i);
                        BigDecimal n1 = nbs.get(i);
                        BigDecimal n2 = nbs.remove(i + 1);
                        BigDecimal result = n2.divide(BigDecimal.valueOf(100), scale, roundingMode);
                        if (nextOp == Operator.MULTIPLY) {
                            nbs.set(i, n1.multiply(result));
                        } else {
                            nbs.set(i, n1.divide(result, scale, roundingMode));
                        }
                        ops.remove(nextOp);
                    } else if (prevOp == Operator.MULTIPLY || prevOp == Operator.DIVIDE) {
                        ops.remove(i);
                        BigDecimal n1 = nbs.get(i - 1);
                        BigDecimal n2 = nbs.get(i);
                        if (prevOp == Operator.MULTIPLY) {
                            BigDecimal result = n1.multiply(n2).divide(BigDecimal.valueOf(100), scale, roundingMode);
                            nbs.set(i, n1.add(result));
                            ops.remove(prevOp);
                        } else {
                            BigDecimal result = n2.multiply(n1).divide(BigDecimal.valueOf(100), scale, roundingMode);
                            nbs.set(i, n1.add(result));
                            ops.remove(prevOp);
                        }
                    } else {
                        i++;
                    }

                } else {
                    i++;
                }
            }
        }

        // Evaluate the rest
        while (!ops.isEmpty()) {
            Operator op = ops.get(0);

            Operator nextOp = null;
            if (1 < ops.size()) {
                nextOp = ops.get(1);
            }

            ops.remove(0);

            BigDecimal n1 = nbs.get(0);
            BigDecimal n2 = nbs.get(1);

            nbs.remove(1);

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
