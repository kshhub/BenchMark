package esos.MobiBench

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.*


class IconTextListAdapter(private val mContext: Context) : BaseAdapter() {
    private var mItems: MutableList<IconTextItem> = ArrayList()
    fun addItem(it: IconTextItem) {
        mItems.add(it)
    }

    fun setListItems(lit: MutableList<IconTextItem>) {
        mItems = lit
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun getItem(position: Int): Any {
        return mItems[position]
    }

    fun areAllItemsSelectable(): Boolean {
        return false
    }

    fun isSelectable(position: Int): Boolean {
        return try {
            mItems[position].isSelectable
        } catch (ex: IndexOutOfBoundsException) {
            false
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemLastView: IconLastView
        if (convertView == null) {
            itemLastView = IconLastView(mContext, mItems[position])
        } else {
            itemLastView = convertView as IconLastView
            itemLastView.setIcon(mItems[position].icon)
            itemLastView.setText(0, mItems[position].getData(0))
            itemLastView.setText(1, mItems[position].getData(1))
            itemLastView.setText(2, mItems[position].getData(2))
            itemLastView.setText(3, mItems[position].getData(3))
            itemLastView.setText(4, mItems[position].getData(4))
            itemLastView.setText(5, mItems[position].getData(5))
            itemLastView.setText(6, mItems[position].getData(6))
        }
        return itemLastView
    }
}