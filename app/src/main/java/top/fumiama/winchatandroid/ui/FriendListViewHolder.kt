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
    open inner class RecyclerViewAdapter(private val clickLine: (Int, Array<String>, View)->Unit, private val longClickLine: (Int, Array<String>, View)->Unit):
        RecyclerView.Adapter<FriendListViewHolder>() {
        private var listIDs: List<Int>? = null
        // getKeys by user
        open fun getKeys(): List<Int>? = null
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
            Log.d("MyMain", "Bind open at $position")
            Thread{
                listIDs?.apply {
                    if (position < size) {
                        val id = get(position)
                        val data = getValue(id)!!
                        holder.itemView.apply {
                            mainWeakReference?.get()?.runOnUiThread {
                                ta.visibility = View.VISIBLE
                                lwclast.visibility = View.GONE
                                fftt.text = id.toString()
                                tn.text = data[0]
                                ta.text = data[1]
                                fftc.text = data[2]
                                setOnClickListener {
                                    clickLine(id, data, this)
                                }
                                setOnLongClickListener {
                                    mainWeakReference?.get()?.apply {
                                        cm?.apply {
                                            setPrimaryClip(ClipData.newPlainText(mainWeakReference?.get()?.getString(R.string.app_name)?:"WinChatAndroid", "$id"))
                                            runOnUiThread {
                                                Toast.makeText(context, "已复制ID", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    longClickLine(id, data, this)
                                    return@setOnLongClickListener true
                                }
                            }
                        }
                    }
                }
            }.start()
        }

        override fun getItemCount() = listIDs?.size?:0

        fun refresh() = Thread{
            listIDs = getKeys()
            mainWeakReference?.get()?.runOnUiThread { notifyDataSetChanged() }
        }.start()

        fun add(id:Int) = Thread{
            listIDs?.apply {
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
            listIDs?.apply {
                for(i in 0 until size) {
                    if(this[i] == id) {
                        val newList = List(size-1) {
                            return@List when {
                                it < i -> this[i]
                                else-> this[i+1]
                            }
                        }
                        listIDs = newList
                        mainWeakReference?.get()?.runOnUiThread {
                            notifyItemRemoved(i)
                        }
                        return
                    }
                }
            }
        }

        fun replace(id: Int) {
            listIDs?.apply {
                for(i in 0 until size) {
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
