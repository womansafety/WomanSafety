package com.ws.womansafety.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class KeyBoardUtils {

 

    public static void hideKeyBoard(Activity context) {
        if (context != null) {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = context.getCurrentFocus();
            if (currentFocus != null) {
                inputManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (currentFocus instanceof EditText) {
                    ((EditText) currentFocus).setCursorVisible(false);
                    currentFocus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v != null) {
                                ((EditText) v).setCursorVisible(true);

                            }
                        }
                    });
                }

            }
        }
    }

}
