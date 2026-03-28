package com.example.frontend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {

    private Paint paintFaible;
    private Paint paintMoyen;
    private Paint paintEleve;
    private Paint paintBorder;

    private float pctFaible = 0f;
    private float pctMoyen = 0f;
    private float pctEleve = 0f;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintFaible = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFaible.setColor(Color.parseColor("#F44336"));
        paintFaible.setStyle(Paint.Style.FILL);

        paintMoyen = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMoyen.setColor(Color.parseColor("#FFA726"));
        paintMoyen.setStyle(Paint.Style.FILL);

        paintEleve = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEleve.setColor(Color.parseColor("#4CAF50"));
        paintEleve.setStyle(Paint.Style.FILL);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(Color.parseColor("#1E2A3A"));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(4f);
    }

    public void setData(float faible, float moyen, float eleve) {
        this.pctFaible = faible;
        this.pctMoyen = moyen;
        this.pctEleve = eleve;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float padding = 8f;

        RectF oval = new RectF(padding, padding, width - padding, height - padding);

        float total = pctFaible + pctMoyen + pctEleve;

        if (total == 0f) {
            Paint paintEmpty = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintEmpty.setColor(Color.parseColor("#2A3B4C"));
            paintEmpty.setStyle(Paint.Style.FILL);
            canvas.drawOval(oval, paintEmpty);
            return;
        }

        float sweepFaible = 360f * pctFaible / total;
        float sweepMoyen  = 360f * pctMoyen  / total;
        float sweepEleve  = 360f * pctEleve  / total;

        float startAngle = -90f;

        if (sweepFaible > 0f) {
            canvas.drawArc(oval, startAngle, sweepFaible, true, paintFaible);
            canvas.drawArc(oval, startAngle, sweepFaible, true, paintBorder);
            startAngle += sweepFaible;
        }
        if (sweepMoyen > 0f) {
            canvas.drawArc(oval, startAngle, sweepMoyen, true, paintMoyen);
            canvas.drawArc(oval, startAngle, sweepMoyen, true, paintBorder);
            startAngle += sweepMoyen;
        }
        if (sweepEleve > 0f) {
            canvas.drawArc(oval, startAngle, sweepEleve, true, paintEleve);
            canvas.drawArc(oval, startAngle, sweepEleve, true, paintBorder);
        }
    }
}