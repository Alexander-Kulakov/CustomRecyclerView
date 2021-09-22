package com.example.customrecyclerview

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.customrecyclerview.databinding.LayoutRecyclerviewBinding


class CustomRecyclerView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutRecyclerviewBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutRecyclerviewBinding.inflate(inflater, this, true)
        initAttrs()
    }

    private fun initAttrs() {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomRecyclerView, defStyleAttr, 0)

        val layoutManagerAttr = a.getInt(R.styleable.CustomRecyclerView_recyclerviewLayoutManager, 0)
        val columnCountAttr = a.getInt(R.styleable.CustomRecyclerView_spanCount, 1)
        val staggeredOrientationAttr = a.getInt(R.styleable.CustomRecyclerView_staggeredOrientation, 1)

        layoutManager = when(layoutManagerAttr) {
            1 -> GridLayoutManager(context, columnCountAttr)
            2 -> {
                val staggeredOrientation = when(staggeredOrientationAttr) {
                    1 -> StaggeredGridLayoutManager.VERTICAL
                    else -> StaggeredGridLayoutManager.HORIZONTAL
                }
                StaggeredGridLayoutManager(columnCountAttr, staggeredOrientation)
            }
            else -> LinearLayoutManager(context)
        }

        a.recycle()
    }

    var isResultEmpty: Boolean = false
        set(value) {
            field = value
            if(field) {
                binding.emptyResult.root.show()
                binding.error.root.hide()
                binding.progressBar.hide()
            }
            else binding.emptyResult.root.hide()
        }

    var isLoading: Boolean = false
        set(value) {
            field = value
            if(field) {
                binding.progressBar.show()
                binding.error.root.hide()
                binding.emptyResult.root.hide()
                binding.recyclerView.hide()
            }
            else {
                binding.progressBar.hide()
                binding.recyclerView.show()
                binding.swipeLayout.isRefreshing = false
            }
        }

    var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        set(value) {
            field = value
            binding.recyclerView.layoutManager = field
        }

    var errorMessage: String? = null
        set(value) {
            field = value
            if(field.isNullOrEmpty()) {
                binding.recyclerView.show()
                binding.error.root.hide()
            }
            else {
                binding.progressBar.hide()
                binding.recyclerView.hide()
                binding.emptyResult.root.hide()
                binding.error.root.show()
                binding.error.errorMessage.text = field
            }
        }

    var swipeToRefreshEnabled: Boolean = true
        set(value) {
            field = value
            if(!field) {
                binding.swipeLayout.isRefreshing = false
            }
            binding.swipeLayout.isEnabled = field
        }

    var retryHandler: () -> Unit = {}
        set(value) {
            field = value
            binding.swipeLayout.setOnRefreshListener {
                field.invoke()
            }
            binding.error.retryButton.setOnClickListener {
                field.invoke()
            }
        }

    var adapter: RecyclerView.Adapter<*>? = null
        set(value) {
            field = value
            binding.recyclerView.adapter = field
        }

    fun addSwipeToDelete(swipeHandler: (RecyclerView.ViewHolder) -> Unit) {
        val itemTouchHelperCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    swipeHandler(viewHolder)
                }

            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    fun prepareToSharedTransition(fragment: Fragment) {
        binding.recyclerView.apply {
            fragment.postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    fragment.startPostponedEnterTransition()
                    true
                }
        }
    }

    fun prepareToSharedTransition(activity: Activity) {
        binding.recyclerView.apply {
            activity.postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    activity.startPostponedEnterTransition()
                    true
                }
        }
    }

    fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }
}