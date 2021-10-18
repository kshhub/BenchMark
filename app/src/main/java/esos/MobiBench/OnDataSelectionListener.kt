package esos.MobiBench

import android.view.View
import android.widget.AdapterView


/**
 * Interface that is called when an item is selected in DataListView
 *
 * @author Mike
 */
interface OnDataSelectionListener {
    /**
     * Method that is called when an item is selected in DataListView
     *
     * @param parent Parent View
     * @param v Target View
     * @param row Row Index
     * @param column Column Index
     * @param id ID for the View
     */
    fun onDataSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long)
}