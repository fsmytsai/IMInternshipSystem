package MyMethod;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import tw.edu.nutc.iminternshipsystem.MainActivity;

/**
 * Created by user on 2017/9/11.
 */

public class MyRecyclerView extends RecyclerView {
    public View view;
    private MainActivity.MailListAdapter.ViewHolder viewHolder;
    public LinearLayout ll_MailBlockAll;
    private TextView tv_DeleteMail;
    private int lastX;
    private int lastY;
    public int deleteWidth;
    public Scroller scroller;
    public int state = 0;//state=0表示侧滑菜单没有显示，state=1表示侧滑菜单显示出来了
    public boolean isClosing = false;
    public boolean isSwiping = false;

    private int closingMoveTimes = 0;

    private MainActivity mainActivity;

    public MyRecyclerView(Context context) {
        super(context);
        scroller = new Scroller(context, new LinearInterpolator());
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context, new LinearInterpolator());
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scroller = new Scroller(context, new LinearInterpolator());
    }

    public void SetMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /*重写onTouchEvent()方法*/
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //顯示刪除框及在垃圾桶時直接返回
        if (mainActivity.mailType == 3 || mainActivity.isDeleting)
            return super.onTouchEvent(e);

        int x = (int) e.getX();//获得当前点击的X坐标
        int y = (int) e.getY();//获得当前点击的Y坐标
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("MyRecyclerViewLog", "ACTION_DOWN " + state + " IsClosing " + isClosing);
                if (state == 0) {//state=0表示菜单没被打开，state=1表示菜单被打开
                    //不知道為啥單擊會呼叫兩次ACTION_DOWN，避免覆蓋掉該關閉的ll_MailBlockAll而直接返回
                    if (isClosing) {
                        return super.onTouchEvent(e);
                    }
                    view = findChildViewUnder(x, y);//根据用户点击的坐标，找到RecyclerView下的子View，这里也就是每一个Item
                    //當RecyclerView沒塞滿畫面時，點擊空白處直接返回
                    if (view == null) {
                        //避免拖拉空白處也會移動到上一個觸碰的ll_MailBlockAll
                        viewHolder = null;
                        ll_MailBlockAll = null;
                        return super.onTouchEvent(e);
                    }
                    viewHolder = (MainActivity.MailListAdapter.ViewHolder) getChildViewHolder(view);//获得每一个Item的ViewHolder
                    ll_MailBlockAll = viewHolder.ll_MailBlockAll;//获得ViewHolder相应的布局
                    //點擊到Footer直接返回
                    if (ll_MailBlockAll == null) {
                        //避免拖拉Footer也會移動到上一個觸碰的ll_MailBlockAll
                        viewHolder = null;
                        ll_MailBlockAll = null;
                        return super.onTouchEvent(e);
                    }
                    tv_DeleteMail = viewHolder.tv_DeleteMail;//得到菜单栏里的控件，这里我们只有一个textview
                    deleteWidth = tv_DeleteMail.getWidth();//获得侧滑菜单的宽度
                } else if (state == 1) {
                    View view1 = findChildViewUnder(x, y);
                    //當側滑菜單已開啟且點擊空白處也要關閉
                    if (view1 == null) {
                        isClosing = true;
                        Log.e("MyRecyclerViewLog", "getScrollX " + ll_MailBlockAll.getScrollX() + " -deleteWidth " + -deleteWidth);
                        scroller.startScroll(ll_MailBlockAll.getScrollX(), 0, -deleteWidth, 0, 100);//弹性滑动
                        invalidate();
                        state = 0;
                        return super.onTouchEvent(e);
                    }
                    MainActivity.MailListAdapter.ViewHolder viewHolder1 = (MainActivity.MailListAdapter.ViewHolder) getChildViewHolder(view1);
                    //判断当前用户指向的Item是否为之前打开的那个Item
                    if (viewHolder.equals(viewHolder1)) {
                        break;
                    } else {
                        isClosing = true;
                        scroller.startScroll(ll_MailBlockAll.getScrollX(), 0, -deleteWidth, 0, 100);//弹性滑动
                        invalidate();
                        state = 0;
                        return true;   //加上这一句会好一些，
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //點到非ll_MailBlockAll的地方時，不允許拉動
                if (ll_MailBlockAll == null) {
                    Log.e("MyRecyclerViewLog", "ACTION_MOVE ll_MailBlockAll Null");
                    return super.onTouchEvent(e);
                }

                //避免關閉時再次拖動ll_MailBlockAll
                if (isClosing) {
                    Log.e("MyRecyclerViewLog", "ACTION_MOVE IsClosing");
                    //避免拖動後造成OnClick沒觸發，而造成isClosing = false;也沒觸發
                    closingMoveTimes++;
                    return super.onTouchEvent(e);
                }

                int scrollX = ll_MailBlockAll.getScrollX();  //获得用户在滑动后，View相对初始位置移动的距离
                int dx = lastX - x; //得到用户实时移动的举例（横向）
                int dy = lastY - y;
                //無視手指點擊時造成的些微Move及向右滑動(因為在DrawerLayout)
                if (dx < 10)
                    return super.onTouchEvent(e);//return true會造成卡頓

                Log.e("MyRecyclerViewLog", "dx = " + dx);

                //拖動時要避免Item的OnClick事件，且避免些微橫移造成的誤判
                if (dx > 20){
                    Log.e("MyRecyclerViewLog", "isSwiping become to true");
                    isSwiping = true;
                }

                if (Math.abs(dx) > Math.abs(dy)) {  //只要左右移动的举例比上下移动的距离大，就执行滑动菜单操作
                    if (scrollX + dx >= deleteWidth) {    //检测右边界
                        ll_MailBlockAll.scrollTo(deleteWidth, 0); //scrollTo()中的参数是指要“移动到的位置”
                        state = 1;
                        return super.onTouchEvent(e);    //表示已经消费这个事件，不必再传递了
                    } else if (scrollX + dx <= 0) {    //检测左边界
                        ll_MailBlockAll.scrollTo(0, 0);
                        state = 0;
                        return true;
                    }
                    ll_MailBlockAll.scrollBy(dx, 0);  //scrollBy()中的参数是指要“移动的距离（也就是像素的数量）”
                }
                break;
            case MotionEvent.ACTION_UP:
                //正在關閉(因為無須計算是要開啟或關閉)或是點擊空白處(在Down就關閉了)直接返回
                if (isClosing || ll_MailBlockAll == null) {
                    //避免誤判
                    isSwiping = false;
                    //如果正在關閉且點擊的是某Item(非Footer)，則先不要設置isClosing = false，以避免觸發Item的Onclick事件
                    View view1 = findChildViewUnder(x, y);
                    Log.e("MyRecyclerViewLog", "ClosingMoveTimes " + closingMoveTimes);
                    if (view1 != null) {
                        MainActivity.MailListAdapter.ViewHolder viewHolder1 = (MainActivity.MailListAdapter.ViewHolder) getChildViewHolder(view1);
                        if (viewHolder1.ll_MailBlockAll != null && closingMoveTimes < 8) {
                            Log.e("MyRecyclerViewLog", "ACTION_UP isClosing and click item");
                            closingMoveTimes = 0;
                            return super.onTouchEvent(e);
                        }
                    }
                    Log.e("MyRecyclerViewLog", "ACTION_UP isClosing to false");
                    isClosing = false;
                    closingMoveTimes = 0;
                    return super.onTouchEvent(e);
                }
                int deltaX = 0;
                int upScrollX = ll_MailBlockAll.getScrollX();//获得Item总共移动的距离
                if (upScrollX >= deleteWidth / 2) {  //如果显示超过一半，则弹性滑开
                    deltaX = deleteWidth - upScrollX;
                    state = 1;
                } else if (upScrollX < deleteWidth / 2) {//否则关闭

                    Log.e("MyRecyclerViewLog", "ACTION_UP Close");
                    deltaX = -upScrollX;//在startScroll()方法中，第三个参数小于0，表示向右滑。
                    state = 0;
                }
                Log.e("MyRecyclerViewLog", "upScrollX " + upScrollX + " deltaX " + deltaX);
                scroller.startScroll(upScrollX, 0, deltaX, 0, 100);//弹性滑动
                invalidate();
                break;
        }
        lastX = x;
        lastY = y;
        return super.onTouchEvent(e);//返回调用父类的方法，来处理我们没有处理的操作，比如上下滑动操作

    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset() && ll_MailBlockAll != null) {
            ll_MailBlockAll.scrollTo(scroller.getCurrX(), scroller.getCurrY());

            invalidate();
//            if(isClosing){
//                //避免關閉時，當手還沒抬起繼續拖拉造成無法觸發ACTION_UP使菜單卡在中間
//                viewHolder = null;
//                ll_MailBlockAll = null;
//            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchEvent(e);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchEvent(e);
                break;
            case MotionEvent.ACTION_UP:
                onTouchEvent(e);
                break;
        }
        return super.onInterceptTouchEvent(e);
    }
}
