package com.ws.womansafety.luncher;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ws.womansafety.RegisterActivity;
import com.ws.womansafety.R;


public class LauncherActivity extends AppCompatActivity {


    private TextView mAppVersionTextView;
    ImageView slidingButton;
    String TAG="Click";
    private float initialX;
    private float dWidth;

    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.layout_enroll).setOnClickListener(onClickListener);
        findViewById(R.id.layout_login_rbk).setOnClickListener(onClickListener);

        slidingButton=findViewById(R.id.img_swap);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        dWidth = displayMetrics.widthPixels-displayMetrics.widthPixels/10;
      //  slidingButton.setOnTouchListener(getButtonTouchListener());

        findViewById(R.id.layout_login).setOnTouchListener(getButtonTouchListener());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getSupportActionBar().hide();

    }

   View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_enroll:
                   gotoSingUp(2);
                    break;
                case R.id.layout_login_rbk:
                    gotoSingUp(1);
                    break;
            }
        }
    };

    private void gotoSingUp(int type){
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);

    }

    private View.OnTouchListener getButtonTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (initialX == 0) {
                            initialX = slidingButton.getX();
                        }
                        if (event.getX() > initialX + slidingButton.getWidth() / 2 &&
                                event.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
                            Log.d(TAG, "sliding Right");
                        }

                        if (event.getX() < initialX + slidingButton.getWidth() / 2 &&
                                event.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
                            Log.d(TAG, "sliding left");
                        }

                        if (event.getX() + slidingButton.getWidth() / 2 > getWidth() &&
                                slidingButton.getX() + slidingButton.getWidth() / 2 < getWidth() + 100) {
                            Log.d(TAG, "stop at right");
                            slidingButton.setX(getWidth() - slidingButton.getWidth());
                        }


                        if (event.getX() + slidingButton.getWidth() / 2 < getWidth() && slidingButton.getX() < 4) {
                            slidingButton.setX(0);
                        }

                        if (event.getX() < slidingButton.getWidth() / 2 &&
                                slidingButton.getX() > 0) {
                            slidingButton.setX(initialX);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if(initialX!=0) {
                            if (slidingButton.getX()  > getWidth() * 0.61) {
                                gotoSingUp(1);
                            } else if (slidingButton.getX()  < getWidth() * 0.25) {
                                gotoSingUp(2);
                            } else {
                                moveToCenter();
                            }


                            return true;
                        }

                }

                return false;
            }
        };

    }

    private float getWidth() {
return dWidth;
    }

    private void moveToCenter() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(slidingButton.getX(), 0);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(initialX);
            }
        });
        positionAnimator.setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(positionAnimator);
        animatorSet.start();
    }


}
