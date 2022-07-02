package com.document.scanner.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.os.AsyncTask
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.document.scanner.adapter.ProgressFramesAdapter
import com.document.scanner.constants.*
import com.document.scanner.data.Frame
import com.document.scanner.databinding.ActivityListFramesBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.extension.visibility
import com.document.scanner.viewmodel.ListFrameActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ListFramesActivity : BaseActivity<ActivityListFramesBinding, ListFrameActivityViewModel>() {

    private var from: Int = 0
    private var to: Int = 0
    private var framesAdapter: ProgressFramesAdapter? = null
    override val viewModel: ListFrameActivityViewModel by viewModels()
    private val docId by lazy { intent.getStringExtra(INTENT_DOCUMENT_ID) }
    private val viewFrameIntent by lazy { intent.getStringExtra(INTENT_VIEW_FRAME) }


    override var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.exportPdf(docId!!, uri)
                }
            }
        }

    /*   override fun onOptionsItemSelected(item: MenuItem): Boolean {
           when (item.itemId) {
               R.id.menu_export_pdf -> {
                   viewModel.sendCreateFileIntent(
                       "application/pdf",
                       resultLauncher
                   )
               }
               R.id.menu_delete -> {
                   showConfirmDeleteDialog()
               }
               R.id.menu_rename -> {
                   showRenameDialog()
               }
               android.R.id.home -> {
                   finish()
               }
           }
           return super.onOptionsItemSelected(item)
       }*/


    private fun showRenameDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getDocument(docId!!).let { document ->
                val editText = EditText(application).apply {
                    setText(document.name)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(50, 12, 50, 12)
                    }
                }
                val frameLayout = FrameLayout(application).apply { addView(editText) }
                AlertDialog.Builder(this@ListFramesActivity).apply {
                    setTitle("Rename")
                    setView(frameLayout)
                    setNegativeButton("Cancel", null)
                    setPositiveButton("Save") { _: DialogInterface?, _: Int ->
                        document.name = editText.text.toString()
                        viewModel.updateDocument(document)
                    }
                    create().show()
                }
            }
        }
    }

    private fun showConfirmDeleteDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirm Delete")
            setMessage("Are you sure you want to delete this document. You won't be able to recover the document later!")
            setNegativeButton("Cancel", null)
            setPositiveButton("Delete") { _, _ ->
                viewModel.delete(docId!!)
                finish()
            }
            create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        // viewModel.processUnprocessedFrames(docId!!)
    }


    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    fun onCreate() = with(viewBinding) {

        titleTv.text = "Reorder"

        back.setOnClickListener { finish() }

        next.setOnClickListener {
            if (viewFrameIntent != null) {
                Intent().let {
                    it.putExtra(INTENT_DOCUMENT_ID, docId)
                    setResult(RESULT_OK, it)
                }
                finish()
            } else {
                val intent = Intent(this@ListFramesActivity, ViewPageActivity::class.java)
                intent.putExtra(INTENT_DOCUMENT_ID, docId)
                startActivity(intent)
            }
        }

        if (viewFrameIntent != null) {
            fab.visibility(false)
        }

        framesAdapter = ProgressFramesAdapter(this@ListFramesActivity, docId!!, ArrayList())

        rvFrames.apply {
            layoutManager = GridLayoutManager(this@ListFramesActivity, 2)
            adapter = framesAdapter
            setHasFixedSize(true)
        }


        loadData()

        val itemTouchHelper = ItemTouchHelper(getItemTouchHelperCallback())
        itemTouchHelper.attachToRecyclerView(rvFrames.apply {
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (framesAdapter?.isSwapped!!) {
                            framesAdapter?.isSwapped = false
                            swapRoomData(from,to)
                            framesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                false
            }
        })

        fab.setOnClickListener {
            startActivity(Intent(this@ListFramesActivity, ScanActivity::class.java).apply {
                putExtra(INTENT_DOCUMENT_ID, docId)
                finish()
            })
        }
    }

    private fun loadData() {
        viewModel.let {
            it.frames(docId!!).observe(this@ListFramesActivity) { newFrames ->
                framesAdapter?.apply {
                    if (frames.size == newFrames.size) {
                        frames = newFrames
                        for (i in frames.indices) {
                            notifyItemChanged(i)
                        }
                    } else {
                        frames = newFrames
                        notifyDataSetChanged()
                    }
                }
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
                from = viewHolder.adapterPosition
                to = target.adapterPosition
                framesAdapter?.swap(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //framesAdapter?.remove(viewHolder.adapterPosition);
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

    private fun swapRoomData(from: Int, to: Int) {
        val temp = framesAdapter?.frames?.get(from)?.index
        framesAdapter?.frames?.get(from)?.index = framesAdapter?.frames?.get(to)?.index!!
        framesAdapter?.frames?.get(to)?.index = temp!!
        viewModel.update(framesAdapter?.frames!!)
    }


    companion object {
        const val VIEW_PAGE_ACTIVITY = 101
    }

    override val viewBinding: ActivityListFramesBinding by viewBinding(ActivityListFramesBinding::inflate)


    override fun onLoadData() {
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
        onCreate()
    }
}