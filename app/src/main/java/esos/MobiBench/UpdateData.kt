package esos.MobiBench

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*


class UpdateData : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DEBUG, "create updatadata ")

        //HttpPostData("10","10","10","10","10","10","10");
        Log.d(DEBUG, "fin")
    }

    fun HttpPostData(
        seq_w: String?,
        seq_r: String?,
        ran_w: String?,
        ran_r: String?,
        sq_in: String?,
        sq_up: String?,
        sq_del: String?,
        sn: String?,
        c_partition: String?,
        c_thread: String?,
        c_file_size_w: String?,
        c_file_size_r: String?,
        c_io_size: String?,
        c_file_mode: String?,
        c_tran: String?,
        c_sqlite_mode: String?,
        c_sqlite_journal: String?,
        c_filesystem: String?,
        def: String?
    ) {
        try {
            val url = URL(
                "http://mobibench.dothome.co.kr/insert_data.php"
            ) // URL
            val http = url.openConnection() as HttpURLConnection // 접속
            http.defaultUseCaches = false
            http.doInput = true
            http.doOutput = true
            http.requestMethod = "POST"
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded")
            val buffer = StringBuffer()
            val model = Build.MODEL
            val android_ver = "Android " + Build.VERSION.RELEASE
            val kernel_ver = System.getProperty("os.name") + System.getProperty("os.version")

            //total memory
            var totalmem = "unknown"
            val m_scanner = Scanner(File("/proc/meminfo"))
            while (m_scanner.hasNext()) {
                val line = m_scanner.nextLine()
                val lineElements =
                    line.replace("\\p{Space}".toRegex(), "").split(":").toTypedArray()
                if (lineElements[0].contentEquals("MemTotal")) {
                    totalmem = lineElements[1]
                    break
                }
            }

            //eMMC chip number
            var emmc_num = "unknown"
            val e_scanner = Scanner(File("/sys/class/block/mmcblk0/device/cid"))
            emmc_num = e_scanner.nextLine()
            buffer.append("model").append("=").append(model).append("&")
            buffer.append("android_ver").append("=").append(android_ver).append("&")
            buffer.append("seq_w").append("=").append(seq_w).append("&")
            buffer.append("seq_r").append("=").append(seq_r).append("&")
            buffer.append("ran_w").append("=").append(ran_w).append("&")
            buffer.append("ran_r").append("=").append(ran_r).append("&")
            buffer.append("sq_in").append("=").append(sq_in).append("&")
            buffer.append("sq_up").append("=").append(sq_up).append("&")
            buffer.append("sq_del").append("=").append(sq_del).append("&")
            buffer.append("sn").append("=").append(sn).append("&") //임시
            buffer.append("c_partition").append("=").append(c_partition).append("&")
            buffer.append("c_thread").append("=").append(c_thread).append("&")
            buffer.append("c_file_size_w").append("=").append(c_file_size_w).append("&")
            buffer.append("c_file_size_r").append("=").append(c_file_size_r).append("&")
            buffer.append("c_io_size").append("=").append(c_io_size).append("&")
            buffer.append("c_file_mode").append("=").append(c_file_mode).append("&")
            buffer.append("c_tran").append("=").append(c_tran).append("&")
            buffer.append("c_sqlite_mode").append("=").append(c_sqlite_mode).append("&")
            buffer.append("c_sqlite_journal").append("=").append(c_sqlite_journal).append("&")
            buffer.append("c_filesystem").append("=").append(c_filesystem).append("&")
            buffer.append("def").append("=").append(def).append("&")
            buffer.append("kernel_ver").append("=").append(kernel_ver).append("&")
            buffer.append("totalmem").append("=").append(totalmem).append("&")
            buffer.append("emmc_num").append("=").append(emmc_num)
            val outStream = OutputStreamWriter(
                http.outputStream, "EUC-KR"
            )
            val writer = PrintWriter(outStream)
            writer.write(buffer.toString())
            writer.flush()

            // --------------------------
            // 서버에서 전송받기
            // --------------------------
            val tmp = InputStreamReader(
                http.inputStream, "EUC-KR"
            )
            val reader = BufferedReader(tmp)
            val builder = StringBuilder()
            var str: String
            while (reader.readLine().also { str = it } != null) { // 서버에서 라인단위로 보내줄 것이므로
                //Log.d(DEBUG, "[url] str : " + str);	// 라인단위로 읽는다
                builder.append(
                    """
                    $str
                    
                    """.trimIndent()
                ) // View에 표시하기 위해 라인 구분자 추가
            }
        } catch (e: MalformedURLException) {
            //
        } catch (e: IOException) {
            //
        } // try
    } // HttpPostData

    companion object {
        private const val DEBUG = "net_access"
    }
} // Activity
