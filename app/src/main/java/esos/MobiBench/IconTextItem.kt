package esos.MobiBench

import android.graphics.drawable.Drawable


class IconTextItem {
    /**
     * Get icon
     *
     * @return
     */
    /**
     * Set icon
     *
     * @param icon
     */
    /**
     * Icon
     */
    var icon: Drawable
    /**
     * Get data array
     *
     * @return
     */
    /**
     * Set data array
     *
     * @param obj
     */
    /**
     * Data array
     */
    var data: Array<String?>?
    /**
     * True if this item is selectable
     */
    /**
     * Set selectable flag
     */
    /**
     * True if this item is selectable
     */
    var isSelectable = true

    /**
     * Initialize with icon and data array
     *
     * @param icon
     * @param obj
     */
    constructor(icon: Drawable, obj: Array<String?>?) {
        this.icon = icon
        data = obj
    }

    /**
     * Initialize with icon and strings
     */
    constructor(
        icon: Drawable,
        obj01: String?,
        obj02: String?,
        obj03: String?,
        obj04: String?,
        obj05: String?,
        obj06: String?,
        obj07: String?
    ) {
        this.icon = icon
        data = arrayOfNulls(7)
        data!![0] = obj01
        data!![1] = obj02
        data!![2] = obj03
        data!![3] = obj04
        data!![4] = obj05
        data!![5] = obj06
        data!![6] = obj07
    }

    /**
     * Get data
     */
    fun getData(index: Int): String? {
        return if (data == null || index >= data!!.size) {
            null
        } else data!![index]
    }

    /**
     * Compare with the input object
     *
     * @param other
     * @return
     */
    operator fun compareTo(other: IconTextItem): Int {
        if (data != null) {
            val otherData = other.data
            if (data!!.size == otherData!!.size) {
                for (i in data!!.indices) {
                    if (data!![i] != otherData[i]) {
                        return -1
                    }
                }
            } else {
                return -1
            }
        } else {
            throw IllegalArgumentException()
        }
        return 0
    }
}