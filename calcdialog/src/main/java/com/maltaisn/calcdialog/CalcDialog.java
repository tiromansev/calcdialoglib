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


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.math.BigDecimal;


/**
 * Dialog with calculator for entering and calculating a number.
 * All settings must be set before showing the dialog or unexpected behavior will occur.
 */
public class CalcDialog extends AppCompatDialogFragment {

    private static final String TAG = CalcDialog.class.getSimpleName();

    // Indexes of text elements in R.array.calc_dialog_btn_texts
    private static final int TEXT_INDEX_ADD = 10;
    private static final int TEXT_INDEX_SUB = 11;
    private static final int TEXT_INDEX_MUL = 12;
    private static final int TEXT_INDEX_DIV = 13;
    private static final int TEXT_INDEX_SIGN = 14;
    private static final int TEXT_INDEX_DEC_SEP = 15;
    private static final int TEXT_INDEX_EQUAL = 16;

    private Context context;
    private CalcPresenter presenter;

    private CalcSettings settings = new CalcSettings();

    private HorizontalScrollView expressionHsv;
    private TextView expressionTxv;
    private TextView valueTxv;
    private TextView decimalSepBtn;
    private TextView equalBtn;
    private TextView answerBtn;
    private TextView signBtn;

    private CharSequence[] btnTexts;
    private CharSequence[] errorMessages;
    private int[] maxDialogDimensions;
    private CalcDialogCallback calcDialogCallback;

    private int buttonBackgroundColor;
    private int buttonTextColor;

    public void setCalcDialogCallback(CalcDialogCallback calcDialogCallback) {
        this.calcDialogCallback = calcDialogCallback;
    }

    public void setButtonBackgroundColor(int buttonBackgroundColor) {
        this.buttonBackgroundColor = buttonBackgroundColor;
    }

    public void setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    ////////// LIFECYCLE METHODS //////////
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Wrap calculator dialog's theme to context
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.calcDialogStyle});
        int style = ta.getResourceId(0, R.style.CalcDialogStyle);
        ta.recycle();
        this.context = new ContextThemeWrapper(context, style);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Get strings
        TypedArray ta = context.obtainStyledAttributes(R.styleable.CalcDialog);
        btnTexts = ta.getTextArray(R.styleable.CalcDialog_calcButtonTexts);
        errorMessages = ta.getTextArray(R.styleable.CalcDialog_calcErrors);
        maxDialogDimensions = new int[]{
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxWidth, -1),
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxHeight, -1)
        };
        ta.recycle();
    }

    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle state) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_calc, null);

        // Value and expression views
        valueTxv = view.findViewById(R.id.calc_txv_value);

        expressionHsv = view.findViewById(R.id.calc_hsv_expression);
        expressionTxv = view.findViewById(R.id.calc_txv_expression);

        // Erase button
        CalcEraseButton eraseBtn = view.findViewById(R.id.calc_btn_erase);
        eraseBtn.setOnEraseListener(new CalcEraseButton.EraseListener() {
            @Override
            public void onErase() {
                presenter.onErasedOnce();
            }

            @Override
            public void onEraseAll() {
                presenter.onErasedAll();
            }
        });

        // Digit buttons
        for (int i = 0; i < 10; i++) {
            TextView digitBtn = view.findViewById(settings.numpadLayout.buttonIds[i]);
            digitBtn.setText(btnTexts[i]);

            final int digit = i;
            digitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onDigitBtnClicked(digit);
                }
            });
        }

        // Operator buttons
        final TextView addBtn = view.findViewById(R.id.calc_btn_add);
        final TextView subBtn = view.findViewById(R.id.calc_btn_sub);
        final TextView mulBtn = view.findViewById(R.id.calc_btn_mul);
        final TextView divBtn = view.findViewById(R.id.calc_btn_div);

        addBtn.setText(btnTexts[TEXT_INDEX_ADD]);
        subBtn.setText(btnTexts[TEXT_INDEX_SUB]);
        mulBtn.setText(btnTexts[TEXT_INDEX_MUL]);
        divBtn.setText(btnTexts[TEXT_INDEX_DIV]);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOperatorBtnClicked(Expression.Operator.ADD);
            }
        });
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOperatorBtnClicked(Expression.Operator.SUBTRACT);
            }
        });
        mulBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOperatorBtnClicked(Expression.Operator.MULTIPLY);
            }
        });
        divBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOperatorBtnClicked(Expression.Operator.DIVIDE);
            }
        });

        // Sign button: +/-
        signBtn = view.findViewById(R.id.calc_btn_sign);
        signBtn.setText(btnTexts[TEXT_INDEX_SIGN]);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSignBtnClicked();
            }
        });

        // Decimal separator button
        decimalSepBtn = view.findViewById(R.id.calc_btn_decimal);
        decimalSepBtn.setText(btnTexts[TEXT_INDEX_DEC_SEP]);
        decimalSepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onDecimalSepBtnClicked();
            }
        });

        // Equal button
        equalBtn = view.findViewById(R.id.calc_btn_equal);
        equalBtn.setText(btnTexts[TEXT_INDEX_EQUAL]);
        equalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEqualBtnClicked();
            }
        });

        // Answer button
        answerBtn = view.findViewById(R.id.calc_btn_answer);
        answerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAnswerBtnClicked();
            }
        });

        // Dialog buttons
        Button clearBtn = view.findViewById(R.id.calc_btn_clear);
        clearBtn.setBackgroundColor(buttonBackgroundColor);
        clearBtn.setTextColor(buttonTextColor);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onClearBtnClicked();
            }
        });

        Button cancelBtn = view.findViewById(R.id.calc_btn_cancel);
        cancelBtn.setBackgroundColor(buttonBackgroundColor);
        cancelBtn.setTextColor(buttonTextColor);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onCancelBtnClicked();
            }
        });

        Button okBtn = view.findViewById(R.id.calc_btn_ok);
        okBtn.setBackgroundColor(buttonBackgroundColor);
        okBtn.setTextColor(buttonTextColor);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOkBtnClicked();
            }
        });

        // Set up dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Get maximum dialog dimensions
                Rect fgPadding = new Rect();
                dialog.getWindow().getDecorView().getBackground().getPadding(fgPadding);
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int height = metrics.heightPixels - fgPadding.top - fgPadding.bottom;
                int width = metrics.widthPixels - fgPadding.top - fgPadding.bottom;

                // Set dialog's dimensions
                if (width > maxDialogDimensions[0]) width = maxDialogDimensions[0];
                if (height > maxDialogDimensions[1]) height = maxDialogDimensions[1];
                dialog.getWindow().setLayout(width, height);

                // Set dialog's content
                view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                dialog.setContentView(view);

                // Presenter
                presenter = new CalcPresenter();
                presenter.attach(CalcDialog.this, state);
            }
        });

        if (state != null) {
            settings = state.getParcelable("settings");
        }

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (presenter != null) {
            // On config change, presenter is detached before this is called
            presenter.onDismissed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        presenter.writeStateToBundle(state);
        state.putParcelable("settings", settings);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.detach();
        }

        presenter = null;
        context = null;
    }

    @Nullable
    private CalcDialogCallback getCallback() {
        if (calcDialogCallback != null) {
            return calcDialogCallback;
        }

        CalcDialogCallback cb = null;
        if (getParentFragment() != null) {
            try {
                cb = (CalcDialogCallback) getParentFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else if (getTargetFragment() != null) {
            try {
                cb = (CalcDialogCallback) getTargetFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else {
            // Caller was an activity
            try {
                cb = (CalcDialog.CalcDialogCallback) requireActivity();
            } catch (Exception e) {
                // Interface callback is not implemented in activity
            }
        }
        return cb;
    }

    /**
     * @return the calculator settings that can be changed.
     */
    public CalcSettings getSettings() {
        return settings;
    }

    ////////// VIEW METHODS //////////
    void exit() {
        dismissAllowingStateLoss();
    }

    void sendValueResult(BigDecimal value) {
        CalcDialogCallback cb = getCallback();
        if (cb != null) {
            cb.onValueEntered(settings.requestCode, value);
        }
    }

    void setExpressionVisible(boolean visible) {
        expressionHsv.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    void setAnswerBtnVisible(boolean visible) {
        answerBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        equalBtn.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
    }

    void setSignBtnVisible(boolean visible) {
        signBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    void setDecimalSepBtnEnabled(boolean enabled) {
        decimalSepBtn.setEnabled(enabled);
    }

    void updateExpression(@NonNull String text) {
        expressionTxv.setText(text);

        // Scroll to the end.
        expressionHsv.post(new Runnable() {
            @Override
            public void run() {
                expressionHsv.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    void updateCurrentValue(@Nullable String text) {
        valueTxv.setText(text);
    }

    void showErrorText(int error) {
        valueTxv.setText(errorMessages[error]);
    }

    void showAnswerText() {
        valueTxv.setText(R.string.calc_answer);
    }

    public interface CalcDialogCallback {
        /**
         * Called when the dialog's OK button is clicked.
         * @param value       value entered. May be null if no value was entered, in this case,
         *                    it should be interpreted as zero or absent value.
         * @param requestCode dialog request code from {@link CalcSettings#getRequestCode()}.
         */
        void onValueEntered(int requestCode, @Nullable BigDecimal value);
    }

}

