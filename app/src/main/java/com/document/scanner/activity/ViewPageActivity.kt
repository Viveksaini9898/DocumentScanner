package com.document.scanner.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.document.scanner.R
import com.document.scanner.adapter.ViewFrameAdapter
import com.document.scanner.constants.*
import com.document.scanner.data.Document
import com.document.scanner.data.Frame
import com.document.scanner.databinding.ActivityViewFramesBinding
import com.document.scanner.extension.viewBinding
import com.document.scanner.extension.visibility
import com.document.scanner.viewmodel.ViewPageActivityViewModel
import com.google.android.material.navigation.NavigationBarView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import net.gotev.speech.*


class ViewPageActivity : BaseActivity<ActivityViewFramesBinding, ViewPageActivityViewModel>(),
    SpeechDelegate,
    ViewPager.OnPageChangeListener {

    override val viewModel: ViewPageActivityViewModel by viewModels()

    private val docId by lazy { intent.getStringExtra(INTENT_DOCUMENT_ID) }
    private var viewFrameAdapter: ViewFrameAdapter? = null
    var document: Document? = null


    fun onCreate() = with(viewBinding) {


        if (docId == null) {
            Toast.makeText(this@ViewPageActivity, "Error", Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        /*   val colors = intArrayOf(
               ContextCompat.getColor(this@ViewPageActivity, android.R.color.black),
               ContextCompat.getColor(this@ViewPageActivity, android.R.color.darker_gray),
               ContextCompat.getColor(this@ViewPageActivity, android.R.color.black),
               ContextCompat.getColor(this@ViewPageActivity, android.R.color.holo_orange_dark),
               ContextCompat.getColor(this@ViewPageActivity, android.R.color.holo_red_dark)
           )
           progress.setColors(colors)*/

        viewModel.getDocument(docId).observe(this@ViewPageActivity) {
            titleTv.text = it.name
        }

        titleTv.setOnClickListener {
            showRenameDialog(this@ViewPageActivity, document!!)
        }

        back.setOnClickListener {
            finish()
        }

        savePdf.setOnClickListener {
            Intent(Intent.ACTION_CREATE_DOCUMENT).let {
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.type = "application/pdf"
                it.putExtra(Intent.EXTRA_TITLE, document?.name)
                exportResultLauncher.launch(it)
            }
        }

        viewFrameAdapter = ViewFrameAdapter(this@ViewPageActivity, ArrayList())
        viewModel.currentIndex = intent.getIntExtra(
            INTENT_FRAME_POSITION,
            0
        )

        viewPager.adapter = viewFrameAdapter
        viewPager.addOnPageChangeListener(this@ViewPageActivity)
        loadData()

        cropLayout.setOnClickListener {
            val cropIntent = Intent(this@ViewPageActivity, CropActivity::class.java)
            cropIntent.putExtra(
                INTENT_SOURCE_PATH,
                viewFrameAdapter?.get(getCurrentIndex())?.uri
            )
            cropIntent.putExtra(
                INTENT_CROPPED_PATH,
                viewFrameAdapter?.get(getCurrentIndex())?.croppedUri
            )
            cropIntent.putExtra(
                INTENT_FRAME_POSITION,
                getCurrentIndex()
            )
            cropIntent.putExtra(
                INTENT_ANGLE,
                viewFrameAdapter?.get(getCurrentIndex())?.angle
            )
            cropIntent.putExtra(
                INTENT_VIEW_FRAME,
                INTENT_VIEW_FRAME
            )
            cropResultLauncher.launch(cropIntent)
        }

        ocr.setOnClickListener {
            viewBinding.progress.visibility(true)
            Toast.makeText(this@ViewPageActivity, "Detecting Text. Please wait", Toast.LENGTH_SHORT)
                .show()
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val bitmap =
                BitmapFactory.decodeFile(viewFrameAdapter?.get(getCurrentIndex())?.croppedUri)
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { text: Text ->
                    viewBinding.progress.visibility(false)
                    showNoteDialog(
                        "Detected Text",
                        "",
                        text.text,
                        viewFrameAdapter?.get(getCurrentIndex())!!
                    )
                }
                .addOnFailureListener {
                    viewBinding.progress.visibility(false)
                    Toast.makeText(
                        this@ViewPageActivity,
                        "ERROR: Could not detect text",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        modify.setOnClickListener {
            val intent = Intent(this@ViewPageActivity, EditActivity::class.java)
            intent.putExtra(INTENT_DOCUMENT_ID, docId)
            intent.putExtra(
                INTENT_FRAME_POSITION,
                viewFrameAdapter?.get(getCurrentIndex())?.id
            )
            modifyResultLauncher.launch(intent)
        }

        speak.setOnClickListener {
            if (Speech.getInstance().isSpeaking) {
                Speech.getInstance().stopTextToSpeech()
            } else {
                progress.visibility(true)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val bitmap =
                    BitmapFactory.decodeFile(viewFrameAdapter?.get(getCurrentIndex())?.croppedUri)
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { text: Text ->
                        onSpeakClick(text)
                    }
                    .addOnFailureListener {
                        progress.visibility(false)
                        Toast.makeText(
                            this@ViewPageActivity,
                            "ERROR: Could not detect text",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        note.setOnClickListener {
            showNoteDialog(
                "Note",
                "Write something beautiful here! This note will be saved alongside the scanned copy",
                viewFrameAdapter?.get(getCurrentIndex())?.note,
                viewFrameAdapter?.get(getCurrentIndex())!!
            )
        }
        reorder.setOnClickListener {
            val i = Intent(this@ViewPageActivity, ListFramesActivity::class.java)
            i.putExtra(INTENT_DOCUMENT_ID, docId)
            i.putExtra(INTENT_VIEW_FRAME, INTENT_VIEW_FRAME)
            reorderResultLauncher.launch(i)
        }
    }


    private fun loadData() {
        viewBinding.progress.visibility(true)
        viewBinding.viewPager.adapter = viewFrameAdapter
        viewModel.frames(docId).observe(this) { frames ->
            viewFrameAdapter?.setFrames(frames)
            viewFrameAdapter?.notifyDataSetChanged()
            viewBinding.viewPager.currentItem = viewModel.currentIndex
            viewBinding.progress.visibility(false)

        }
    }

    private fun onSpeakClick(textToSpeech: Text) {
        if (textToSpeech.text.trim().isEmpty()) {
            Toast.makeText(this, "something wrong", Toast.LENGTH_LONG).show()
            return
        }
        Speech.getInstance()
            .say(textToSpeech.text.trim(), object : TextToSpeechCallback {
                override fun onStart() {
                    viewBinding.progress.visibility(false)
                    Toast.makeText(this@ViewPageActivity, "TTS onStart", Toast.LENGTH_SHORT).show()
                }

                override fun onCompleted() {
                    Toast.makeText(this@ViewPageActivity, "TTS onCompleted", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError() {
                    viewBinding.progress.visibility(false)
                    Toast.makeText(this@ViewPageActivity, "TTS onError", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /* private fun onRecordAudioPermissionGranted() {
         try {
             Speech.getInstance().stopTextToSpeech()
             Speech.getInstance().startListening(viewBinding.progress, this)
         } catch (exc: SpeechRecognitionNotAvailable) {
             Toast.makeText(this@ViewPageActivity, "Speech Recognition Not Available", Toast.LENGTH_SHORT).show()
         } catch (exc: GoogleVoiceTypingDisabledException) {
         }
     }
 */
    override fun onResume() {
        super.onResume()
        //viewModel.processUnprocessedFrames(docId!!,this)
    }


    private fun showRenameDialog(context: Context, document: Document) {
        val frameLayout = FrameLayout(context)
        val editText = EditText(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(50, 12, 50, 12)
            }
            setText(document.name)
        }
        frameLayout.addView(editText)
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Rename")
            setView(frameLayout)
            setNegativeButton("Cancel", null)
            setPositiveButton("Save") { it: DialogInterface?, _: Int ->
                document.name = editText.text.toString()
                viewModel.updateDocument(document)
                viewBinding.titleTv.text = editText.text.toString()
                it?.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }
    }


    private var cropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val frame =
                    viewFrameAdapter?.get(result.data?.getIntExtra(INTENT_FRAME_POSITION, 0)!!)
                        ?.apply {
                            uri = result.data?.getStringExtra(INTENT_SOURCE_PATH)!!
                            croppedUri = result.data?.getStringExtra(INTENT_CROPPED_PATH)
                            angle = result.data?.getIntExtra(INTENT_ANGLE, 0)!!
                            editedUri = null
                        }
                viewModel.updateFrame(frame!!)
                loadData()
            }
        }

    private var modifyResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val frame = viewFrameAdapter?.get(result.data?.getIntExtra(INTENT_FRAME_POSITION, 0)!!)
                Task(frame!!).execute()
            }
        }

    private var reorderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
               loadData()
            }
        }

    private fun getCurrentIndex(): Int {
        return viewBinding.viewPager.currentItem
    }

  inner class Task(var frame: Frame) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void?) {
            return viewModel.updateFrame(frame)
        }

        override fun onPreExecute() {
            super.onPreExecute()
           viewBinding.progress.visibility(true)
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            viewBinding.progress.visibility(false)
            loadData()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            /*  R.id.menu_rename -> {
                  showFrameRenameDialog(this, viewFrameAdapter.get(getCurrentIndex()))
              }
              R.id.menu_delete -> {
                  showFrameDeleteDialog(this, viewFrameAdapter.get(getCurrentIndex()))
              }*/
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFrameRenameDialog(activity: Activity, frame: Frame) {
        val frameLayout = FrameLayout(activity)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(50, 12, 50, 12)
        }
        val editText = EditText(activity).apply {
            layoutParams = params
            hint = "Frame Name"
            setText(frame.name)
        }
        frameLayout.addView(editText)
        AlertDialog.Builder(activity).apply {
            setTitle("Rename")
            setView(frameLayout)
            setNegativeButton("Cancel", null)
            setPositiveButton("Save") { _: DialogInterface?, _: Int ->
                frame.name = editText.text.toString()
                viewModel.updateFrame(frame)
            }
            create().show()
        }
    }

    private fun showFrameDeleteDialog(activity: Activity?, frame: Frame) {
        AlertDialog.Builder(activity).apply {
            setTitle("Confirm Delete")
            setMessage("Are you sure you want to delete this frame? You won't be able to recover this frame later")
            setNegativeButton("Cancel", null)
            setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFrame(frame)
            }
            create().show()
        }
    }

    @SuppressLint("InflateParams")
    private fun showNoteDialog(name: String?, hint: String?, note: String?, frame: Frame) {
        layoutInflater.inflate(R.layout.dialog_note, null).apply {
            val alertDialog = AlertDialog.Builder(this@ViewPageActivity)
                .setView(this)
                .create().apply {
                    window?.setBackgroundDrawableResource(android.R.color.transparent)
                    show()
                }
            val etNote = findViewById<EditText>(R.id.et_note).apply {
                this.hint = hint
                setText(note)
            }
            findViewById<TextView>(R.id.title).text = name
            findViewById<TextView>(R.id.tv_save).setOnClickListener {
                frame.note = etNote.text.toString()
                viewModel.updateFrame(frame)
                alertDialog.dismiss()
            }
            findViewById<TextView>(R.id.tv_cancel).setOnClickListener { alertDialog.dismiss() }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // required
    }

    override fun onPageSelected(position: Int) {
        viewModel.currentIndex = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        // required
    }

    override val viewBinding: ActivityViewFramesBinding by viewBinding(ActivityViewFramesBinding::inflate)

    override fun onLoadData() {
        viewModel.getDocument(docId).observe(this@ViewPageActivity) {
            document = it
        }
    }

    override fun onResult(result: ActivityResult, requestCode: Int) {
    }

    override fun onReady() {
        onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Speech.getInstance().isSpeaking) {
            Speech.getInstance().stopTextToSpeech()
        }
    }

    /* override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<String?>,
         grantResults: IntArray
     ) {
         if (requestCode != PERMISSIONS_REQUEST) {
             super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         } else {
             if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 // permission was granted, yay!
                 onRecordAudioPermissionGranted()
             } else {
                 Toast.makeText(this, "Permission required", Toast.LENGTH_LONG)
                     .show()
             }
         }
     }*/

    override fun onStartOfSpeech() {

    }

    override fun onSpeechRmsChanged(value: Float) {
    }

    override fun onSpeechPartialResults(results: MutableList<String>?) {
    }

    override fun onSpeechResult(result: String?) {
    }

    private var exportResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.exportPdf(uri,docId!!)
                }
            }
        }

    companion object {
        private const val PERMISSIONS_REQUEST = 1

    }
}