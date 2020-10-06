package com.example.georeality

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//MyCachesFragment Class
class MyCachesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var deleteIcon: Drawable
    private lateinit var colorDrawableBackground: ColorDrawable
    private var user : FirebaseUser? = null
    private var audioMarkersList : MutableList<AudioMarker> = ArrayList()
    private var arMarkersList : MutableList<ARMarker> = ArrayList()
    private var markerList : MutableList<Any> = ArrayList()


    //onCreateView function
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_caches, container, false)
        viewManager = LinearLayoutManager(requireActivity())
        viewAdapter = RecyclerViewAdapter(markerList)
        deleteIcon = ContextCompat.getDrawable(requireActivity().baseContext, R.drawable.ic_delete)!!
        colorDrawableBackground = ColorDrawable(Color.parseColor("#ff0000"))

        recyclerView = view.findViewById<RecyclerView>(R.id.my_caches_recyclerView).apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            //onMove function
            override fun onMove(
                p0: RecyclerView,
                p1: RecyclerView.ViewHolder,
                p2: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            //onSwipe function
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (viewAdapter as RecyclerViewAdapter).removeItem(
                    viewHolder.adapterPosition,
                    viewHolder
                )
            }
            //onChildDraw function
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMarginVertical = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX > 0) {
                    colorDrawableBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMarginVertical, itemView.top + iconMarginVertical,
                        itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth, itemView.bottom - iconMarginVertical)
                } else {
                    colorDrawableBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMarginVertical - deleteIcon.intrinsicWidth, itemView.top + iconMarginVertical,
                        itemView.right - iconMarginVertical, itemView.bottom - iconMarginVertical)
                    deleteIcon.level = 0
                }

                colorDrawableBackground.draw(c)

                c.save()

                if (dX > 0)
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                deleteIcon.draw(c)

                c.restore()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        user = FirebaseAuth.getInstance().currentUser
        //Setup marker observers
        Database.dbViewModel!!.audioMarkers.observe(viewLifecycleOwner, {
            for (i in it.indices) {
                if (it[i].creator == user!!.email) {
                    audioMarkersList.add(it[i])
                    markerList.add(it[i])
                }
            }
            Log.d("AUDIOLIST", audioMarkersList.toString())
        })
        Database.dbViewModel!!.arMarkers.observe(viewLifecycleOwner, {
            for (i in it.indices) {
                if (it[i].creator == user!!.email) {
                    arMarkersList.add(it[i])
                    markerList.add(it[i])
                }
            }
            Log.d("ARLIST", arMarkersList.toString())
        })

        // Inflate the layout for this fragment
        return view
    }
}

//RecyclerViewAdapter class
class RecyclerViewAdapter(private val markerList : MutableList<Any>) : RecyclerView.Adapter<RecyclerViewAdapter.MainViewHolder>() {
    private var removedPosition: Int = 0
    private lateinit var removedItem: Any

    class MainViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.cachesTitle)
        val type: TextView = v.findViewById(R.id.cachesType)
        val latLon: TextView = v.findViewById(R.id.cachesLatLon)
    }

    //override onCreateViewHolder function
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.MainViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.my_caches_recycler_view_item, viewGroup, false)
        return MainViewHolder(v)
    }

    //Function to bind properties to layout
    override fun onBindViewHolder(viewHolder: MainViewHolder, position: Int) {
        if (markerList[position] is AudioMarker) {
            val audioMarker = markerList[position] as AudioMarker
            viewHolder.title.text = audioMarker.title
            viewHolder.type.text = "Audio"
            viewHolder.latLon.text = "Lat: ${audioMarker.latitude}, lon: ${audioMarker.longitude}"
        }
        else if (markerList[position] is ARMarker) {
            val arMarker = markerList[position] as ARMarker
            viewHolder.title.text = arMarker.title
            viewHolder.type.text = "AR"
            viewHolder.latLon.text = "Lat: ${arMarker.latitude}, lon: ${arMarker.longitude}"
        }
    }

    //Function to remove your own item from the recyclerview and database
    fun removeItem(position: Int, viewHolder: RecyclerView.ViewHolder) {
        removedItem = markerList[position]
        removedPosition = position

        //Remove item in UI
        markerList.removeAt(position)
        notifyItemRemoved(position)

        Snackbar.make(viewHolder.itemView, "$removedItem removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                //If undone, add to UI
                markerList.add(removedPosition, removedItem)
                notifyItemInserted(removedPosition)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar:Snackbar, event:Int) {
                    //Only remove item in database on Snackbar timeout
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        if (removedItem is AudioMarker) {
                            val newRemovedItem = removedItem as AudioMarker
                            newRemovedItem.audio_id?.let {
                                Database.dbViewModel!!.deleteMarker(it, "audio") }
                        } else if (removedItem is ARMarker) {
                            val newRemovedItem = removedItem as ARMarker
                            newRemovedItem.ar_id?.let {
                                Database.dbViewModel!!.deleteMarker(it, "ar") }
                        }
                    }
                }
            } )
            .show()
    }

    //Function to get item count
    override fun getItemCount() = markerList.size
}