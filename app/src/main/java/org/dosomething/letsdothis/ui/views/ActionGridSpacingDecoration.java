package org.dosomething.letsdothis.ui.views;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.dosomething.letsdothis.R;
import org.dosomething.letsdothis.ui.views.typeface.CustomTextView;

/**
 * Created by toidiu on 5/6/15.
 */
public class ActionGridSpacingDecoration extends RecyclerView.ItemDecoration
{

    public static final int GRID_SPACING = R.dimen.padding_small;
    private static      int eightDp      = 0;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        Resources r = view.getResources();
        if(view instanceof CustomTextView)
        {
            outRect.bottom = getPxFromEightDp(r);
        }
        if(view instanceof ReportBackImageView)
        {
            outRect.bottom = getPxFromEightDp(r);
            outRect.right = getPxFromEightDp(r);
            outRect.left = getPxFromEightDp(r);

            int childPosition = parent.getChildPosition(view);
            if(childPosition % 2 == 0)
            {
                outRect.right = (getPxFromEightDp(r) / 2);
            }
            else
            {
                outRect.left = (getPxFromEightDp(r) / 2);
            }
        }
    }

    private int getPxFromEightDp(Resources r)
    {
        if(eightDp == 0)
        {
            eightDp = r.getDimensionPixelSize(GRID_SPACING);
        }
        return eightDp;
    }

}
