package ru.skillbranch.mappoints_vesion2.ui.custom.behavior

import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import ru.skillbranch.mappoints_vesion2.ui.custom.AppPanel

class AppPanelBahavior : CoordinatorLayout.Behavior<AppPanel>() {

    private var topbound = 0
    private var bottombound = 0
    private var interceptorEvents = false
    private lateinit var dragHelper: ViewDragHelper

    override fun onLayoutChild(parent: CoordinatorLayout, child: AppPanel, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        if (!::dragHelper.isInitialized) initialize(parent, child)
        return true
    }

    private fun initialize(parent: CoordinatorLayout, child: AppPanel) {
        dragHelper = ViewDragHelper.create(parent, 1f, DragHelperCallback())
        topbound = parent.height - child.height
        bottombound = parent.height - child.minHeight
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppPanel, ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> interceptorEvents = parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> interceptorEvents = false
        }
        return if (interceptorEvents) dragHelper.shouldInterceptTouchEvent(ev)
        else false
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: AppPanel, ev: MotionEvent): Boolean {
        if (::dragHelper.isInitialized) {
            dragHelper.processTouchEvent(ev)
        }
        return true
    }

    inner class DragHelperCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child is AppPanel
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return bottombound - topbound
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return MathUtils.clamp(top, topbound, bottombound)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val needClose = yvel > 0

            val startSettling = dragHelper.settleCapturedViewAt(0, if (needClose) bottombound else topbound)

            if (startSettling) {
                ViewCompat.postOnAnimation(releasedChild, SettleRunnable(releasedChild))
            }
        }

    }

    private inner class SettleRunnable(private val view : View) : Runnable {
        override fun run() {
            if (dragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(view, this)
            }
        }

    }

}