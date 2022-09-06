@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package it.vfsfitvnm.reordering

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListBeyondBoundsInfo
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class ReorderingState(
    val lazyListState: LazyListState,
    val coroutineScope: CoroutineScope,
    private val lastIndex: Int,
    internal val onDragStart: () -> Unit,
    internal val onDragEnd: (Int, Int) -> Unit,
    private val extraItemCount: Int
) {
    private lateinit var lazyListBeyondBoundsInfoInterval: LazyListBeyondBoundsInfo.Interval
    internal val lazyListBeyondBoundsInfo = LazyListBeyondBoundsInfo()
    internal val offset = Animatable(0, Int.VectorConverter)

    internal var draggingIndex by mutableStateOf(-1)
    internal var reachedIndex by mutableStateOf(-1)
    internal var draggingItemSize by mutableStateOf(0)

    lateinit var itemInfo: LazyListItemInfo

    private var previousItemSize = 0
    private var nextItemSize = 0

    private var overscrolled = 0

    internal var indexesToAnimate = mutableStateMapOf<Int, Animatable<Int, AnimationVector1D>>()
    private var animatablesPool: AnimatablesPool<Int, AnimationVector1D>? = null

    fun onDragStart(index: Int) {
        overscrolled = 0
        itemInfo = lazyListState.layoutInfo.visibleItemsInfo.find {
            it.index == index + extraItemCount
        }!!
        onDragStart.invoke()
        draggingIndex = index
        reachedIndex = index
        draggingItemSize = itemInfo.size

        nextItemSize = draggingItemSize
        previousItemSize = -draggingItemSize

        offset.updateBounds(
            lowerBound = -index * draggingItemSize,
            upperBound = (lastIndex - index) * draggingItemSize
        )

        lazyListBeyondBoundsInfoInterval =
            lazyListBeyondBoundsInfo.addInterval(index + extraItemCount, index + extraItemCount)

        val size =
            lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset

        animatablesPool = AnimatablesPool(size / draggingItemSize + 2, 0, Int.VectorConverter)
    }

    fun onDrag(change: PointerInputChange, dragAmount: Offset) {
        change.consume()

        val delta = when (lazyListState.layoutInfo.orientation) {
            Orientation.Vertical -> dragAmount.y
            Orientation.Horizontal -> dragAmount.x
        }.roundToInt()

        val targetOffset = offset.value + delta

        coroutineScope.launch {
            offset.snapTo(targetOffset)
        }

        if (targetOffset > nextItemSize) {
            if (reachedIndex < lastIndex) {
                reachedIndex += 1
                nextItemSize += draggingItemSize
                previousItemSize += draggingItemSize

                val indexToAnimate = reachedIndex - if (draggingIndex < reachedIndex) 0 else 1

                coroutineScope.launch {
                    val animatable = indexesToAnimate.getOrPut(indexToAnimate) {
                        animatablesPool?.acquire() ?: return@launch
                    }

                    if (draggingIndex < reachedIndex) {
                        animatable.snapTo(0)
                        animatable.animateTo(-draggingItemSize)
                    } else {
                        animatable.snapTo(draggingItemSize)
                        animatable.animateTo(0)
                    }

                    indexesToAnimate.remove(indexToAnimate)
                    animatablesPool?.release(animatable)
                }
            }
        } else if (targetOffset < previousItemSize) {
            if (reachedIndex > 0) {
                reachedIndex -= 1
                previousItemSize -= draggingItemSize
                nextItemSize -= draggingItemSize

                val indexToAnimate = reachedIndex + if (draggingIndex > reachedIndex) 0 else 1

                coroutineScope.launch {
                    val animatable = indexesToAnimate.getOrPut(indexToAnimate) {
                        animatablesPool?.acquire() ?: return@launch
                    }

                    if (draggingIndex > reachedIndex) {
                        animatable.snapTo(0)
                        animatable.animateTo(draggingItemSize)
                    } else {
                        animatable.snapTo(-draggingItemSize)
                        animatable.animateTo(0)
                    }
                    indexesToAnimate.remove(indexToAnimate)
                    animatablesPool?.release(animatable)
                }
            }
        } else {
            val offsetInViewPort = targetOffset + itemInfo.offset - overscrolled

            val topOverscroll = lazyListState.layoutInfo.viewportStartOffset +
                    lazyListState.layoutInfo.beforeContentPadding - offsetInViewPort

            val bottomOverscroll = lazyListState.layoutInfo.viewportEndOffset -
                    lazyListState.layoutInfo.afterContentPadding - offsetInViewPort - itemInfo.size

            if (topOverscroll > 0) {
                overscroll(topOverscroll)
            } else if (bottomOverscroll < 0) {
                overscroll(bottomOverscroll)
            }
        }
    }

    fun onDragEnd() {
        coroutineScope.launch {
            offset.animateTo((previousItemSize + nextItemSize) / 2)

            withContext(Dispatchers.Main) {
                onDragEnd.invoke(draggingIndex, reachedIndex)
            }

            if (areEquals()) {
                draggingIndex = -1
                reachedIndex = -1
                draggingItemSize = 0
                offset.snapTo(0)
            }

            lazyListBeyondBoundsInfo.removeInterval(lazyListBeyondBoundsInfoInterval)
            animatablesPool = null
        }
    }

    private fun overscroll(overscroll: Int) {
        lazyListState.dispatchRawDelta(-overscroll.toFloat())
        coroutineScope.launch {
            offset.snapTo(offset.value - overscroll)
        }
        overscrolled -= overscroll
    }

    private fun areEquals(): Boolean {
        return lazyListState.layoutInfo.visibleItemsInfo.find {
            it.index + extraItemCount == draggingIndex
        }?.key == lazyListState.layoutInfo.visibleItemsInfo.find {
            it.index + extraItemCount == reachedIndex
        }?.key
    }
}

@Composable
fun rememberReorderingState(
    lazyListState: LazyListState,
    key: Any,
    onDragEnd: (Int, Int) -> Unit,
    onDragStart: () -> Unit = {},
    extraItemCount: Int = 0
): ReorderingState {
    val coroutineScope = rememberCoroutineScope()

    return remember(key) {
        ReorderingState(
            lazyListState = lazyListState,
            coroutineScope = coroutineScope,
            lastIndex = if (key is List<*>) key.lastIndex else lazyListState.layoutInfo.totalItemsCount,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
            extraItemCount = extraItemCount,
        )
    }
}
