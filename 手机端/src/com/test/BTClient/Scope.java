package com.test.BTClient;

import java.util.LinkedList;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("DrawAllocation")
public class Scope extends ImageView{
	
	private Queue<Integer> QueueBuffer = new LinkedList<Integer>();
	private final int pointMap_xLength = 1080;
	private final int pointMap_yLength = 800;
	private final int xAxis_Length = (int)(1080*0.8);
	private final int yAxis_Length = (int)(xAxis_Length*0.8);
	private final int baseX = 20;
	private final int baseY = pointMap_yLength-20;
	private Bitmap pointMap = Bitmap.createBitmap(pointMap_xLength,pointMap_yLength, Config.ARGB_8888) ;

	
	private String peakValueText = null;
	private String averageValueText = null;
	public Scope(Context context) {
		
		super(context);
		pointMap_Init();
	}

	public Scope(Context context, AttributeSet attrs)
	{
		super(context,attrs);
		pointMap_Init();
	}
	
	 @Override
	protected void onDraw(Canvas canvas) {
		 	super.onDraw(canvas);
		 	Paint paint1 = new Paint();
		 	paint1.setColor(Color.WHITE);
		 	canvas.drawBitmap(pointMap,0,0,paint1);
	        canvas.save();
	    }
	 
	public void inputData(int data)
	{
		if(QueueBuffer.size()>=xAxis_Length)
		    QueueBuffer.remove();
		QueueBuffer.add(data);
		refreshDisplay();
	}
	private void pointMap_Init()
	{
		Canvas canva = new Canvas(pointMap);
		Paint paint1 = new Paint();
		paint1.setColor(Color.WHITE);
		paint1.setTextSize(25);
        canva.drawLine((float)baseX,(float)baseY,(float) (baseX+xAxis_Length),(float)baseY,paint1);
        canva.drawLine((float)baseX,(float)baseY,(float)baseX,(float) (baseY-yAxis_Length),paint1);
        canva.drawText("T",(float) (baseX+xAxis_Length),(float) (baseY), paint1);
        canva.drawText("3.3V",(float)baseX,(float) (baseY-yAxis_Length), paint1);
        canva.save();
	}
	private void refreshDisplay()
	{
		pointMap.eraseColor(Color.TRANSPARENT);
		pointMap_Init();
		Canvas canva = new Canvas(pointMap);
		Paint paint = new Paint();
		paint.setColor(Color.CYAN);
		int x = 0; int PeakValue = QueueBuffer.peek();
		int Sum = 0;
		for(Integer k :QueueBuffer)
		{
			if(k>PeakValue) PeakValue =k;
			canva.drawPoint(x+baseX, baseY-(int)((k)*yAxis_Length/4096), paint);
			Sum += k;
			x++;
		}
		canva.save();
		peakValueText = String.format("·åÖµ:%1$3.3f",(PeakValue*3.3/4096))+'V';
		averageValueText = String.format("¾ùÖµ:%1$3.3f",((Sum/x)*3.3/4096))+'V';
	}
	public void EmptyQueue()
	{
		QueueBuffer.clear();
	}
	public String getPeakValueString()
	{
		return peakValueText;
	}
	public String getAverageValueString()
	{
		return averageValueText;
	}
}
