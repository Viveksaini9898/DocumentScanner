package com.document.scanner.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.document.scanner.R
import com.document.scanner.activity.BaseActivity
import com.document.scanner.activity.MainActivity
import com.document.scanner.activity.ViewPageActivity
import com.document.scanner.constants.INTENT_DOCUMENT_ID
import com.document.scanner.data.Document
import com.document.scanner.utils.Utils
import com.document.scanner.viewmodel.MainActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

class DocumentsAdapter(
    private val activity: Activity,
    private var data: List<Document>,
    val viewModel: MainActivityViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var simpleDateFormat: SimpleDateFormat =
        SimpleDateFormat("dd MMM, yyyy hh:mm", Locale.getDefault())

    @SuppressLint("NotifyDataSetChanged")
    fun updateDocuments(documents: List<Document>) {
        data = documents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NormalViewHolder(layoutInflater.inflate(R.layout.row_document, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val document = data[position]
            (holder as NormalViewHolder).apply {
                title.text = document.name
                subtitle.text = simpleDateFormat.format(Date(document.dateTime))
                viewModel.getPageCount(document.id).observe(activity as MainActivity) { count ->
                    sheetNumber.text = String.format(
                        Locale.getDefault(),
                        "%d pages", count
                    )
                }
                itemView.setOnClickListener {
                    val intent = Intent(activity, ViewPageActivity::class.java)
                    intent.putExtra(INTENT_DOCUMENT_ID, document.id)
                    activity.startActivity(intent)
                }
                viewModel.getFirstFrameImagePath(document.id)?.observe(activity) { uri ->
                    Glide.with(activity).load(uri).downsample(DownsampleStrategy.AT_MOST)
                        .into(imageView)
                }

                delete.setOnClickListener {
                    viewModel.deleteDocument(document.id)
                }

            }


    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_NORMAL
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.iv_frame)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var subtitle: TextView = itemView.findViewById(R.id.tv_sub_title)
        var sheetNumber: TextView = itemView.findViewById(R.id.tv_number)
        var delete: ImageView = itemView.findViewById(R.id.delete)
    }

    companion object {
        private const val TYPE_NORMAL = 0
    }

}