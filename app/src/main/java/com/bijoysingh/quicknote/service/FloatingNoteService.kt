package com.bijoysingh.quicknote.service

import android.app.Activity
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity.NOTE_ID
import com.bijoysingh.quicknote.database.Note
import com.bsk.floatingbubblelib.FloatingBubbleConfig
import com.bsk.floatingbubblelib.FloatingBubblePermissions
import com.bsk.floatingbubblelib.FloatingBubbleService
import com.github.bijoysingh.starter.util.TextUtils

/**
 * The floating not service
 * Created by bijoy on 3/29/17.
 */

class FloatingNoteService : FloatingBubbleService() {

  private var note: Note? = null
  private lateinit var title: TextView
  private lateinit var description: TextView
  private lateinit var timestamp: TextView
  private lateinit var panel: View

  override fun getConfig(): FloatingBubbleConfig {
    return FloatingBubbleConfig.Builder()
        .bubbleIcon(ContextCompat.getDrawable(context, R.drawable.app_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(
            context,
            com.bsk.floatingbubblelib.R.drawable.close_default_icon))
        .bubbleIconDp(72)
        .removeBubbleIconDp(72)
        .paddingDp(8)
        .borderRadiusDp(4)
        .physicsEnabled(true)
        .expandableColor(-0x50506)
        .triangleColor(-0x50506)
        .gravity(Gravity.END)
        .expandableView(loadView())
        .removeBubbleAlpha(0.7f)
        .build()
  }

  override fun onGetIntent(intent: Intent): Boolean {
    note = null
    if (intent.hasExtra(NOTE_ID)) {
      note = Note.db(context).getByID(intent.getIntExtra(NOTE_ID, 0))
    }
    return note != null
  }

  private fun loadView(): View {
    val rootView = getInflater().inflate(R.layout.layout_add_note_overlay, null)

    title = rootView.findViewById<View>(R.id.title) as TextView
    description = rootView.findViewById<View>(R.id.description) as TextView
    timestamp = rootView.findViewById<View>(R.id.timestamp) as TextView


    val editButton = rootView.findViewById<View>(R.id.panel_edit_button) as ImageView
    editButton.setImageResource(R.drawable.ic_edit_white_48dp)
    editButton.setOnClickListener {
      try {
        note!!.edit(context)
      } catch (exception: Exception) {
        // Some issue
      }
      stopSelf()
    }

    val shareButton = rootView.findViewById<View>(R.id.panel_share_button) as ImageView
    shareButton.setImageResource(R.drawable.ic_share_white_48dp)
    shareButton.setOnClickListener {
      note!!.share(context)
      stopSelf()
    }

    val copyButton = rootView.findViewById<View>(R.id.panel_copy_button) as ImageView
    copyButton.visibility = View.VISIBLE
    copyButton.setOnClickListener {
      note!!.copy(context)
      setState(false)
    }

    panel = rootView.findViewById(R.id.panel_layout)
    panel.setBackgroundColor(note!!.color)

    setNote()
    return rootView
  }

  fun setNote() {
    if (note == null) {
      note = Note.gen()
    }

    val noteTitle = note!!.getTitle()
    val noteDescription = note!!.text
    title.text = noteTitle
    description.text = noteDescription
    timestamp.text = note!!.displayTimestamp

    title.visibility = if (TextUtils.isNullOrEmpty(noteTitle)) View.GONE else View.VISIBLE
    description.visibility = if (TextUtils.isNullOrEmpty(noteDescription)) View.GONE else View.VISIBLE
  }

  companion object {
    fun openNote(activity: Activity, note: Note?, finishOnOpen: Boolean) {
      if (FloatingBubblePermissions.requiresPermission(activity)) {
        FloatingBubblePermissions.startPermissionRequest(activity)
      } else {
        val intent = Intent(activity, FloatingNoteService::class.java)
        if (note != null) {
          intent.putExtra(NOTE_ID, note.uid)
        }
        activity.startService(intent)
        if (finishOnOpen) activity.finish()
      }
    }
  }
}
