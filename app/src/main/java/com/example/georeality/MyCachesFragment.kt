package com.example.georeality

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
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


class MyCachesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var deleteIcon: Drawable
    private lateinit var colorDrawableBackground: ColorDrawable
    private var dataset = mutableListOf("Item 1", "Item 2", "Item 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_caches, container, false)
        viewManager = LinearLayoutManager(requireActivity())
<<<<<<< HEAD
        viewAdapter = RecyclerViewAdapter(dataset)
        deleteIcon = ContextCompat.getDrawable(requireActivity().baseContext, R.drawable.ic_delete)!!
        colorDrawableBackground = ColorDrawable(Color.parseColor("#ff0000"))

        recyclerView = view.findViewById<RecyclerView>(R.id.my_caches_recyclerView).apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                p0: RecyclerView,
                p1: RecyclerView.ViewHolder,
                p2: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (viewAdapter as RecyclerViewAdapter).removeItem(
                    viewHolder.adapterPosition,
                    viewHolder
                )
            }
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
        // Inflate the layout for this fragment
        return view
=======
        //viewAdapter = MyRe
        return inflater.inflate(R.layout.fragment_my_caches, container, false)
>>>>>>> eced22fa537efc1d37d678982b6ef6c21b663c67
    }
}

class RecyclerViewAdapter(private val dataset: MutableList<String>) : RecyclerView.Adapter<RecyclerViewAdapter.MainViewHolder>() {

    private var removedPosition: Int = 0
    private var removedItem: String = ""

    class MainViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.caches_textView)
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.MainViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.my_caches_recycler_view_item, viewGroup, false)
        return MainViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: MainViewHolder, position: Int) {
        viewHolder.title.text = dataset[position]
    }

    fun removeItem(position: Int, viewHolder: RecyclerView.ViewHolder) {
        removedItem = dataset[position]
        removedPosition = position

        dataset.removeAt(position)
        notifyItemRemoved(position)

        Snackbar.make(viewHolder.itemView, "$removedItem removed", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                dataset.add(removedPosition, removedItem)
                notifyItemInserted(removedPosition)

            }.show()
    }

    override fun getItemCount() = dataset.size
}