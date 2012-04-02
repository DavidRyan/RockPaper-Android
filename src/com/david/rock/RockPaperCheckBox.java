package com.david.rock;

import android.content.Context;
import android.view.View;
import android.widget.*;
import android.view.View.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.*;

public class RockPaperCheckBox extends CheckBox {

        private Paint paint;
        private final int MIN_WIDTH = 200;
        private final int MIN_HEIGHT = 50;
        final int DEFAULT_COLOR = Color.WHITE;

        int _color;


		public RockPaperCheckBox(Context context) {
			super(context);
		}

        public RockPaperCheckBox(Context context, AttributeSet attrs){
        super(context, attrs);
        }

        public RockPaperCheckBox(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        }

		protected void onDraw(Canvas canvas) {
			paint = new Paint();
			//paint.setColor(Color.GREEN);
            canvas.drawRect(5, 5, 5, 5, paint); 
           // canvas.drawColor(_color);
            super.onDraw(canvas);
			//canvas.drawText("Hello World", 5, 30, paint);
        }

	}

