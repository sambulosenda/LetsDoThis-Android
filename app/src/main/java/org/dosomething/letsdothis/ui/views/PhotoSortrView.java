package org.dosomething.letsdothis.ui.views;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.dosomething.letsdothis.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/27/13
 * Time: 12:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class PhotoSortrView extends View implements MultiTouchController.MultiTouchObjectCanvas<MultiTouchEntity>
{
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final  boolean mShowDebugInfo = false;
    private static final int     UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;
    public static final int mUIMode = UI_MODE_ROTATE;
    private Paint mLinePaintTouchPointCircle = new Paint();
    private ArrayList<MultiTouchEntity>            mImages              = new ArrayList<MultiTouchEntity>();
    private MultiTouchController<MultiTouchEntity> multiTouchController = new MultiTouchController<MultiTouchEntity>(
            this);
    private MultiTouchController.PointInfo         currTouchPoint       = new MultiTouchController.PointInfo();


    public PhotoSortrView(Context context)
    {
        this(context, null);
    }

    public PhotoSortrView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PhotoSortrView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int rawWidth = MeasureSpec.getSize(widthMeasureSpec);
        int rawHeight = MeasureSpec.getSize(heightMeasureSpec);

        int squareDimen;
        if(rawWidth > rawHeight)
        {
            squareDimen = rawHeight;
        }
        else
        {
            squareDimen = rawWidth;
        }
        this.setMeasuredDimension(rawWidth, rawHeight);
    }


    private void init()
    {
        setDrawingCacheEnabled(true);
        setBackgroundColor(getResources().getColor(R.color.black_70));

        mLinePaintTouchPointCircle.setColor(Color.YELLOW);
        mLinePaintTouchPointCircle.setStrokeWidth(5);
        mLinePaintTouchPointCircle.setStyle(Paint.Style.STROKE);
        mLinePaintTouchPointCircle.setAntiAlias(true);
    }

    /**
     * Called by activity's onResume() method to load the images
     */
    public void addImage(Drawable drawable)
    {
        float midX = (float) this.getMeasuredWidth() / 2.0f;
        float midY = (float) this.getMeasuredHeight() / 2.0f;

        ImageEntity entity = new ImageEntity(this, drawable, getContext().getResources());
        entity.load(getContext(), midX, midY);
        mImages.clear();
        mImages.add(entity);
    }

    /**
     * Called by activity's onPause() method to free memory used for loading the images
     */
    public void unloadImages()
    {
        for(MultiTouchEntity mImage : mImages)
        {
            mImage.unload();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        for(MultiTouchEntity mImage : mImages)
        {
            mImage.draw(canvas);
        }

        if(mShowDebugInfo)
        {
            drawMultitouchDebugMarks(canvas);
        }
    }

    public Bitmap takeSquareScreenshot(int imageSize) throws IOException
    {
        invalidate();
        System.gc();
        Bitmap drawingCache = getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(drawingCache, 0, 0, imageSize, imageSize);
        return bitmap;
    }


    public Bitmap takeScreenshot() throws IOException
    {
        invalidate();
        Bitmap drawingCache = getDrawingCache();
        return drawingCache;
    }

    private void drawMultitouchDebugMarks(Canvas canvas)
    {
        if(currTouchPoint.isDown())
        {
            float[] xs = currTouchPoint.getXs();
            float[] ys = currTouchPoint.getYs();
            float[] pressures = currTouchPoint.getPressures();
            int numPoints = Math.min(currTouchPoint.getNumTouchPoints(), 2);
            for(int i = 0; i < numPoints; i++)
            {
                canvas.drawCircle(xs[i], ys[i], 50 + pressures[i] * 80, mLinePaintTouchPointCircle);
            }
            if(numPoints == 2)
            {
                canvas.drawLine(xs[0], ys[0], xs[1], ys[1], mLinePaintTouchPointCircle);
            }
        }
    }

    /**
     * Pass touch events to the MT controller
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return multiTouchController.onTouchEvent(event);
    }

    /**
     * Get the image that is under the single-touch point, or return null (canceling the drag op) if none
     */
    public MultiTouchEntity getDraggableObjectAtPoint(MultiTouchController.PointInfo pt)
    {
        float x = pt.getX(), y = pt.getY();
        int n = mImages.size();
        for(int i = n - 1; i >= 0; i--)
        {
            ImageEntity im = (ImageEntity) mImages.get(i);
            if(im.containsPoint(x, y))
            {
                return im;
            }
        }
        return null;
    }

    /**
     * Select an object for dragging. Called whenever an object is found to be under the point (non-null is returned by getDraggableObjectAtPoint())
     * and a drag operation is starting. Called with null when drag op ends.
     */
    public void selectObject(MultiTouchEntity img, MultiTouchController.PointInfo touchPoint)
    {
        currTouchPoint.set(touchPoint);
        if(img != null)
        {
            // Move image to the top of the stack when selected
            mImages.remove(img);
            mImages.add(img);
        }
        else
        {
            // Called with img == null when drag stops.
        }
        invalidate();
    }

    /**
     * Get the current pagerPosition and scale of the selected image. Called whenever a drag starts or is reset.
     */
    public void getPositionAndScale(MultiTouchEntity img, MultiTouchController.PositionAndScale objPosAndScaleOut)
    {
        // FIXME affine-izem (and fix the fact that the anisotropic_scale part requires averaging the two scale factors)
        objPosAndScaleOut
                .set(img.getCenterX(), img.getCenterY(), (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                     (img.getScaleX() + img.getScaleY()) / 2,
                     (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(), img.getScaleY(),
                     (mUIMode & UI_MODE_ROTATE) != 0, img.getAngle());
    }

    /**
     * Set the pagerPosition and scale of the dragged/stretched image.
     */
    public boolean setPositionAndScale(MultiTouchEntity img, MultiTouchController.PositionAndScale newImgPosAndScale, MultiTouchController.PointInfo touchPoint)
    {
        currTouchPoint.set(touchPoint);
        boolean ok = img.setPos(newImgPosAndScale);
        if(ok)
        {
            invalidate();
        }
        return ok;
    }

    public boolean pointInObjectGrabArea(MultiTouchController.PointInfo pt, MultiTouchEntity img)
    {
        return false;
    }

    public void rotateImage()
    {
        for(MultiTouchEntity mImage : mImages)
        {
            float angle = mImage.getAngle();
            if(angle >= .75f)
            {
                mImage.setAngle(0f);
            }
            else
            {
                mImage.setAngle(angle + .25f);
            }
        }

        invalidate();
    }
}
