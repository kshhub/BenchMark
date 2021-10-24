package esos.MobiBench

import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.*

object StorageOptions {
    private val mMounts = ArrayList<String>()
    private val mVold = ArrayList<String>()
    // var labels: Array<String>
    // var paths: Array<String>
    var count = 0
    var b_2nd_sdcard = false
    fun determineStorageOptions(): String? {
        readMountsFile()
        readVoldFile()
        compareMountsWithVold()
        testAndCleanMountsList()

        //setProperties();
        println("mobibench secondary sdcard path final: " + mMounts.toString())
        var ret: String? = null
        if (mMounts.size > 0) {
            b_2nd_sdcard = true
            ret = mMounts[0]
            mMounts.clear()
        }
        return ret
    }

    fun getAvailableSize(path: String): Long {
        println("Dir  : $path")
        val stat = StatFs(path)
        val block_size = stat.blockSize.toLong()
        val blocks = stat.availableBlocks.toLong()
        return block_size * blocks
    }

    fun formatSize(size: Long): String {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024
                }
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun GetFileSystemName(): String {
        var ret_str = "unknown"
        try {
            val scanner = Scanner(File("/proc/mounts"))
            while (scanner.hasNext()) {
                val line = scanner.nextLine()
                val lineElements = line.split(" ").toTypedArray()
                if (lineElements[1].contentEquals("/data")) {
                    ret_str = lineElements[2]
                    break
                }
            }
        } catch (e: Exception) {
            // Auto-generated catch block
            e.printStackTrace()
        }
        println("Filesystem Name: $ret_str")
        return ret_str
    }

    private fun readMountsFile() {
        /*
		 * Scan the /proc/mounts file and look for lines like this:
		 * /dev/block/vold/179:1 /mnt/sdcard vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015,fmask=0602,dmask=0602,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
		 *
		 * When one is found, split it into its elements
		 * and then pull out the path to the that mount point
		 * and add it to the arraylist
		 */

        // some mount files don't list the default
        // path first, so we add it here to
        // ensure that it is first in our list
        //  mMounts.add("/mnt/sdcard");
        try {
            val scanner = Scanner(File("/proc/mounts"))
            while (scanner.hasNext()) {
                val line = scanner.nextLine()
                if (line.startsWith("/dev/block/vold/")) {
                    val lineElements = line.split(" ").toTypedArray()
                    val element = lineElements[1]

                    // don't add the default mount path
                    // it's already in the list.
                    if (element != Environment.getExternalStorageDirectory().path) {
                        mMounts.add(element)
                        //System.out.println("mobibench secondary sdcard path mount: "+ element);
                    }
                }
            }
        } catch (e: Exception) {
            // Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun readVoldFile() {
        /*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1 /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 *
		 * When one is found, split it into its elements
		 * and then pull out the path to the that mount point
		 * and add it to the arraylist
		 */

        // some devices are missing the vold file entirely
        // so we add a path here to make sure the list always
        // includes the path to the first sdcard, whether real
        // or emulated.
        // mVold.add("/mnt/sdcard");
        try {
            val scanner = Scanner(File("/system/etc/vold.fstab"))
            while (scanner.hasNext()) {
                val line = scanner.nextLine()
                if (line.startsWith("dev_mount")) {
                    val lineElements = line.split(" ").toTypedArray()
                    var element = lineElements[2]
                    if (element.contains(":")) element = element.substring(0, element.indexOf(":"))

                    // don't add the default vold path
                    // it's already in the list.
                    if (element != Environment.getExternalStorageDirectory().path) {
                        mVold.add(element)
                        //System.out.println("mobibench secondary sdcard path vold: "+ element);
                    }
                }
            }
        } catch (e: Exception) {
            // Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun compareMountsWithVold() {
        /*
		 * Sometimes the two lists of mount points will be different.
		 * We only want those mount points that are in both list.
		 *
		 * Compare the two lists together and remove items that are not in both lists.
		 */
        var i = 0
        while (i < mMounts.size) {
            val mount = mMounts[i]
            if (!mVold.contains(mount)) mMounts.removeAt(i--)
            i++
        }

        // don't need this anymore, clear the vold list to reduce memory
        // use and to prepare it for the next time it's needed.
        mVold.clear()
    }

    private fun testAndCleanMountsList() {
        /*
		 * Now that we have a cleaned list of mount paths
		 * Test each one to make sure it's a valid and
		 * available path. If it is not, remove it from
		 * the list.
		 */
        var i = 0
        while (i < mMounts.size) {
            val mount = mMounts[i]
            val root = File(mount)
            if (!root.exists() || !root.isDirectory || !root.canWrite()) mMounts.removeAt(i--)
            i++
        }
    }
}