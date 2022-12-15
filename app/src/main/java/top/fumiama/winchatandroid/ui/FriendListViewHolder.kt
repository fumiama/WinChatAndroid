package top.fumiama.winchatandroid.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.line_friend.view.*
import top.fumiama.winchatandroid.MainActivity.Companion.mainWeakReference
import top.fumiama.winchatandroid.R

open class FriendListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open inner class RecyclerViewAdapter(private val clickLine: (Int, Array<String>, View)->Unit, private val longClickLine: (Int, Array<String>, View)->Boolean):
        RecyclerView.Adapter<FriendListViewHolder>() {
        private var listIDs: List<Int> = listOf()
        // getKeys by user
        open fun getKeys(): List<Int> = listOf()
        // getValue return [name, msg, count]
        open fun getValue(id: Int): Array<String>? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendListViewHolder {
            return FriendListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.line_friend, parent, false)
            )
        }

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun onBindViewHolder(holder: FriendListViewHolder, position: Int) {
            Thread{
                listIDs.apply {
                    if (position < size) {
                        val id = get(position)
                        val data = getValue(id)!!
                        holder.itemView.apply {
                            mainWeakReference?.get()?.runOnUiThread {
                                fftt.text = id.toString()
                                tn.text = data[0]
                                ta.text = data[1]
                                fftc.text = data[2]
                                lwclast.visibility = View.VISIBLE
                                Log.d("MyFLVH", "bind open at $position, id: ${fftt.text}, tn: ${tn.text}, ta: ${ta.text}, fftc: ${fftc.text}, lwclast.visible: ${lwclast.visibility == View.VISIBLE}")
                                setOnClickListener {
                                    clickLine(id, data, this)
                                }
                                setOnLongClickListener {
                                    return@setOnLongClickListener longClickLine(id, data, this)
                                }
                            }
                        }
                    } else if(position == size) {
                        Log.d("MyFLVH", "bind last")
                        holder.itemView.apply {
                            lwclast.visibility = View.GONE
                            tn.text = ""
                            ta.text = ""
                            isClickable = false
                            isFocusable = false
                        }
                    }
                }
            }.start()
        }

        override fun getItemCount() = listIDs.size + 1

        fun refresh() = Thread{
            listIDs = getKeys()
            mainWeakReference?.get()?.runOnUiThread { notifyDataSetChanged() }
        }.start()

        fun add(id:Int) = Thread{
            listIDs.apply {
                val newList = List(size+1) {
                    if(it == 0) return@List id
                    return@List this[it-1]
                }
                listIDs = newList
                mainWeakReference?.get()?.runOnUiThread {
                    notifyItemInserted(0)
                }
            }
        }.start()

        fun remove(id: Int) {
            listIDs.apply {
                for(i in indices) {
                    // Log.d("MyFLVH", "scanning to remove id $id @ $i, found ${this[i]}")
                    if(this[i] == id) {
                        val newList = List(size-1) {
                            return@List when {
                                it < i -> this[it]
                                else-> this[it+1]
                            }
                        }
                        listIDs = newList
                        Log.d("MyFLVH", "remove id $id @ $i")
                        mainWeakReference?.get()?.runOnUiThread {
                            notifyItemRemoved(i)
                        }
                        return
                    }
                }
            }
        }

        fun replace(id: Int) {
            listIDs.apply {
                for(i in indices) {
                    if(this[i] == id) {
                        mainWeakReference?.get()?.runOnUiThread {
                            notifyItemChanged(i)
                        }
                        return
                    }
                }
            }
        }
    }
}
