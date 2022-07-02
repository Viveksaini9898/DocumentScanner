package com.document.scanner.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.document.scanner.R
import com.document.scanner.adapter.ProgressFramesAdapter
import com.document.scanner.constants.INTENT_DOCUMENT_ID
import com.document.scanner.constants.INTENT_URIS
import com.document.scanner.databinding.ActivityListFramesBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.viewmodel.ListFrameActivityViewModel

class GalleryListFramesActivity : BaseActivity<ActivityListFramesBinding, ListFrameActivityViewModel>() {


    private var framesAdapter: ProgressFramesAdapter? = null

    override var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel?.exportPdf(viewModel?.document?.id!!,uri)
                }
            }
        }


    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    fun onCreate() = with(viewBinding) {
        titleTv.text = "Reorder"

        back.setOnClickListener { finish() }

        next.setOnClickListener {
            val intent = Intent(this@GalleryListFramesActivity, ViewPageActivity::class.java)
            intent.putExtra(INTENT_DOCUMENT_ID, viewModel?.document?.id)
            startActivity(intent)
        }

        val sourcePaths = intent.getStringArrayListExtra(INTENT_URIS) ?: ArrayList()
        fab.visibility = View.GONE

         framesAdapter = ProgressFramesAdapter(this@GalleryListFramesActivity,
            viewModel?.document?.id!!, ArrayList())
        rvFrames.let {
            it.layoutManager = GridLayoutManager(this@GalleryListFramesActivity, 2)
            it.setHasFixedSize(true)
            it.adapter = framesAdapter
        }

        val itemTouchHelper = ItemTouchHelper(getItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(rvFrames.apply {
            layoutManager = GridLayoutManager(this@GalleryListFramesActivity, 2)
            adapter = framesAdapter
            setHasFixedSize(true)
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (framesAdapter?.isSwapped == true) {
                            framesAdapter?.isSwapped = false
                            viewModel?.update(framesAdapter?.frames!!)
                        }
                    }
                }
                false
            }
        })

        viewModel.let {
            it?.setup(sourcePaths)
            it?.frames?.observe(this@GalleryListFramesActivity) { frames ->
                framesAdapter?.frames = frames
                framesAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun getItemTouchHelperCallback(): ItemTouchHelper.Callback {
        return object : ItemTouchHelper.Callback() {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                framesAdapter?.swap(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                framesAdapter?.remove(viewHolder.adapterPosition);
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END
                )
            }
        }
    }

    override val viewBinding: ActivityListFramesBinding by viewBinding(ActivityListFramesBinding::inflate)
    override val viewModel: ListFrameActivityViewModel? by viewModels()

    override fun onLoadData() {
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
        onCreate()
    }
}