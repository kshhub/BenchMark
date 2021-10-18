package esos.MobiBench

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView


class DataListView : ListView {
    /**
     * DataAdapter for this instance
     */
    private val adapter: IconTextListAdapter? = null

    /**
     * Listener for data selection
     */
    private var selectionListener: OnDataSelectionListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    // jwgom add
    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
    }

    override fun setCacheColorHint(color: Int) {
        super.setCacheColorHint(color)
    }

    /**
     * set initial properties
     */
    private fun init() {
        // set OnItemClickListener for processing OnDataSelectionListener
        onItemClickListener = OnItemClickAdapter()
    }

    /**
     * set DataAdapter
     *
     * @param adapter
     */
    fun setAdapter(adapter: BaseAdapter?) {
        super.setAdapter(adapter)
    }

    /**
     * get DataAdapter
     *
     * @return
     */
    override fun getAdapter(): BaseAdapter? {
        return super.getAdapter() as BaseAdapter?
    }
    /**
     * get OnDataSelectionListener
     *
     * @return
     */
    /**
     * set OnDataSelectionListener
     *
     * @param listener
     */
    var onDataSelectionListener: OnDataSelectionListener?
        get() = selectionListener
        set(listener) {
            selectionListener = listener
        }

    internal inner class OnItemClickAdapter : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, v: View, position: Int, id: Long) {
            if (selectionListener == null) {
                return
            }

            // call the OnDataSelectionListener method
            selectionListener!!.onDataSelected(parent, v, position, id)
        }
    }
}