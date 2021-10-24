package esos.MobiBench

import android.app.AlertDialog
import android.app.TabActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class TabMain : TabActivity() {
    // for update version check
    private var mResult: String? = "1"
    private var sVersion = 0
    private var sMyVersion = 0
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var db_index = 0
    private var root_flag = false
    private val set: Setting = Setting()
    private var CB_SW: CheckBox? = null
    private var CB_SR: CheckBox? = null
    private var CB_RW: CheckBox? = null
    private var CB_RR: CheckBox? = null
    private var CB_INSERT: CheckBox? = null
    private var CB_UPDATE: CheckBox? = null
    private var CB_DELETE: CheckBox? = null
    private var et_threadnum: EditText? = null
    private var et_filesize_w: EditText? = null
    private var et_filesize_r: EditText? = null

    //private EditText et_io_size = null;
    private var et_transaction: EditText? = null
    private var sp_partition: Spinner? = null
    private var sp_file_sync: Spinner? = null
    private var sp_io_size: Spinner? = null
    private var sp_sql_sync: Spinner? = null
    private var sp_journal: Spinner? = null
    private var m_exe: MobiBenchExe? = null
    private var tv_progress_txt: TextView? = null
    private var tv_progress_per: TextView? = null
    private var TV_free_space: TextView? = null
    private var mFlag = false // using App stop button
    private var result: Cursor? = null
    private var db_date: String? = null
    private val arr = ArrayList<String>()
    private var aa: ArrayAdapter<String>? = null
    private val tmpExpName = arrayOf(
        "Seq.Write",
        "Seq.read",
        "Rand.Write",
        "Rand.Read",
        "SQLite.Insert",
        "SQLite.Update",
        "SQLite.Delete"
    )

    //   ProgressTrd thread = null;
    var mb_thread: MobiBenchExe? = null
    private var con: Context? = null

    /* For Animation*/
    var anidrawable: AnimationDrawable? = null
    var image: ImageView? = null
    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what <= 100) {
                prBar!!.progress = msg.what
                tv_progress_per!!.text = "" + msg.what + "%"
            } else if (msg.what == 999) {
                tv_progress_txt!!.text = msg.obj as String
            } else if (msg.what == 666) {
                mFlag = false
            } else if (msg.what == 444) {
                //while(image.getId() != R.drawable.bg_mea_middle06 ){
                anidrawable!!.stop()
                //}
                image!!.setImageResource(R.drawable.bg_mea)
                if (msg.arg1 == 1) {
                    print_error(1)
                }
                Log.d(DEBUG_TAG, "[JWGOM] join start")
                try {
                    mb_thread!!.join()
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                Log.d(DEBUG_TAG, "[JWGOM] join end")
                btn_clk_check = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DEBUG_TAG, "**********onCreate")
        if (m_exe == null) {
            m_exe = MobiBenchExe()
            m_exe!!.LoadEngine()
            m_exe!!.SetStoragePath(this.filesDir.toString())
        }
        // For Database
        dbAdapter = NotesDbAdapter(this)
        dbAdapter!!.open()

        /* For tab layout setting */
        val tabHost = tabHost
        LayoutInflater.from(this).inflate(R.layout.tabmain, tabHost.tabContentView, true)
        tabHost.addTab(
            tabHost.newTabSpec("measure")
                .setIndicator("", resources.getDrawable(R.drawable.tab_mea))
                .setContent(R.id.measure)
        )
        tabHost.addTab(
            tabHost.newTabSpec("history")
                .setIndicator("", resources.getDrawable(R.drawable.tab_history))
                .setContent(R.id.history)
        )
        for(tab in 0 until tabHost.tabWidget.childCount){
            tabHost.tabWidget.getChildAt(tab).setBackgroundColor(Color.parseColor("#B5E61D"))
        }
        tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#CDDF94"))
        tabHost.setOnTabChangedListener {
            if(tabHost.currentTab == 0){
                tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#CDDF94"))
                tabHost.tabWidget.getChildAt(1).setBackgroundColor(Color.parseColor("#B5E61D"))
            }else{
                tabHost.tabWidget.getChildAt(1).setBackgroundColor(Color.parseColor("#CDDF94"))
                tabHost.tabWidget.getChildAt(0).setBackgroundColor(Color.parseColor("#B5E61D"))
            }
        }

        /* For Animation*/
        image = findViewById<View>(R.id.position_mea) as ImageView
        image!!.setBackgroundResource(R.drawable.aniimage)
        anidrawable = image!!.background as AnimationDrawable

        //image.post(new StartAni());


        /* Preference Control */prefs = getSharedPreferences("Setting", MODE_PRIVATE)
        root_flag = prefs!!.getBoolean("init_flag", true)
        editor = prefs!!.edit()

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */
        var pi: PackageInfo? = null
        try {
            pi = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {

            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        val verSion = pi!!.versionName
        val verCode = pi.versionCode
        Log.d(DEBUG_TAG, "versionName is $verSion")
        Log.d(DEBUG_TAG, "versionCode is $verCode")

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */db_index = prefs!!.getInt("database_index", 0) // data base indexing
        result = null
        var db_data: String? = null
        val arr = ArrayList<String>()
        Log.d(DEBUG_TAG, "**********onCreate before jwgom  1")
        if (db_index != 0) {
            for (i in 0 until db_index) {
                result = dbAdapter!!.fetchNote(i.toLong()) // 횟수 제한
                result!!.moveToFirst()
                db_data = "  " + result!!.getString(2) + "(" + result!!.getString(1) + ")"
                arr.add(db_data)
            }
            result!!.close()
        }
        val list = findViewById<View>(R.id.ListView01) as ListView
        aa = ArrayAdapter(this, R.layout.history_listitem, arr)
        list.adapter = aa // ListView에 ArrayAdapter 설정
        list.onItemClickListener =
            OnItemClickListener { a, v, position, id ->
                result = dbAdapter!!.fetchNote(position.toLong())
                result!!.moveToFirst()
                //for(int i=0; i < 7; i++) {
                Log.d(DEBUG_TAG, "Start cursor position " + result!!.position)


                // clear values
                result_start = 0
                DialogActivity.ResultDate = null
                for (i in 0..6) {
                    DialogActivity.bHasResult[i] = 0
                    DialogActivity.ResultCPU_act[i] = null
                    DialogActivity.ResultCPU_iow[i] = null
                    DialogActivity.ResultCPU_idl[i] = null
                    DialogActivity.ResultCS_tot[i] = null
                    DialogActivity.ResultCS_vol[i] = null
                    DialogActivity.ResultThrp[i] = null
                    DialogActivity.ResultExpName[i] = null
                    DialogActivity.ResultType[i] = null
                }
                DialogActivity.ResultDate = result!!.getString(1)
                while (!result!!.isAfterLast) {
                    Log.d(
                        DEBUG_TAG,
                        "Create DialogActivity (position/result_start/expname) " + result!!.position + " " + result_start + " " + result!!.getString(
                            10
                        )
                    )
                    if (result!!.getString(10) == tmpExpName[0]) { // seq write
                        result_start = 0
                    } else if (result!!.getString(10) == tmpExpName[1]) {
                        result_start = 1
                    } else if (result!!.getString(10) == tmpExpName[2]) {
                        result_start = 2
                    } else if (result!!.getString(10) == tmpExpName[3]) {
                        result_start = 3
                    } else if (result!!.getString(10) == tmpExpName[4]) {
                        result_start = 4
                    } else if (result!!.getString(10) == tmpExpName[5]) {
                        result_start = 5
                    } else if (result!!.getString(10) == tmpExpName[6]) {
                        result_start = 6
                    }
                    DialogActivity.bHasResult[result_start] = result!!.getInt(3)
                    DialogActivity.ResultCPU_act[result_start] = result!!.getString(4)
                    DialogActivity.ResultCPU_iow[result_start] = result!!.getString(5)
                    DialogActivity.ResultCPU_idl[result_start] = result!!.getString(6)
                    DialogActivity.ResultCS_tot[result_start] = result!!.getString(7)
                    DialogActivity.ResultCS_vol[result_start] = result!!.getString(8)
                    DialogActivity.ResultThrp[result_start] = result!!.getString(9)
                    DialogActivity.ResultExpName[result_start] = result!!.getString(10)
                    DialogActivity.ResultType[result_start] = result!!.getString(2)
                    result!!.moveToNext()
                }
                val intent = Intent(this@TabMain, DialogActivity::class.java)
                DialogActivity.check_using_db = 0
                startActivity(intent)
            }

        /*jwgom
		 *
		 * ***************************************************************************************************************************************************
		 * */Log.d(DEBUG_TAG, "**********onCreate after jwgom")

        /* spinner define (total 5 spinner) */sp_partition =
            findViewById<View>(R.id.sp_partition) as Spinner
        sp_file_sync = findViewById<View>(R.id.sp_file_sync) as Spinner
        sp_io_size = findViewById<View>(R.id.sp_io_size) as Spinner
        sp_sql_sync = findViewById<View>(R.id.sp_sql_sync) as Spinner
        sp_journal = findViewById<View>(R.id.sp_journal) as Spinner
        val ad_partition: ArrayAdapter<*>
        ad_partition = if (StorageOptions.b_2nd_sdcard === true) {
            ArrayAdapter.createFromResource(this, R.array.partition, android.R.layout.simple_spinner_item)
        } else {
            ArrayAdapter.createFromResource(this, R.array.partition2, android.R.layout.simple_spinner_item)
        }
        val ad_file_sync: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.filesyncmode, R.layout.spinner_item)
        val ad_io_size: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.iosize, R.layout.spinner_item)
        val ad_sql_sync: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.sqlsyncmode, R.layout.spinner_item)
        val ad_journal: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(this, R.array.journalmode, R.layout.spinner_item)
        //
        ad_partition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_file_sync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_io_size.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_sql_sync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ad_journal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_partition!!.adapter = ad_partition
        sp_file_sync!!.adapter = ad_file_sync
        sp_io_size!!.adapter = ad_io_size
        sp_sql_sync!!.adapter = ad_sql_sync
        sp_journal!!.adapter = ad_journal
        et_threadnum = findViewById<View>(R.id.threadnum) as EditText
        et_filesize_w = findViewById<View>(R.id.filesize_w) as EditText
        et_filesize_r = findViewById<View>(R.id.filesize_r) as EditText
        //et_io_size = (EditText)findViewById(R.id.io_size);
        et_transaction = findViewById<View>(R.id.transcation) as EditText
        CB_SW = findViewById<View>(R.id.cb_sw) as CheckBox
        CB_SR = findViewById<View>(R.id.cb_sr) as CheckBox
        CB_RW = findViewById<View>(R.id.cb_rw) as CheckBox
        CB_RR = findViewById<View>(R.id.cb_rr) as CheckBox
        CB_INSERT = findViewById<View>(R.id.cb_insert) as CheckBox
        CB_UPDATE = findViewById<View>(R.id.cb_update) as CheckBox
        CB_DELETE = findViewById<View>(R.id.cb_delete) as CheckBox
        tv_progress_txt = findViewById<View>(R.id.progress_text) as TextView
        tv_progress_per = findViewById<View>(R.id.progress_per) as TextView
        prBar = findViewById<View>(R.id.progress) as ProgressBar
        prBar!!.progress = 0


        /* First Warning message control */if (root_flag) {
            set_default()
            startActivityForResult(Intent(this@TabMain, First::class.java), 0)
        } else {
            load_init()

            // for version check and update this app.
            try {
                val url = URL("http://mobibench.dothome.co.kr/mobibench_ver.html")
                val conn = url.openConnection() as HttpURLConnection
                if (conn != null) {
                    conn.connectTimeout = 10000
                    conn.useCaches = false
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val br = BufferedReader(
                            InputStreamReader(conn.inputStream)
                        )
                        var bVersion = false
                        while (true) {
                            val line = br.readLine()
                            Log.d(DEBUG_TAG, "line$line")
                            if (bVersion) {
                                mResult = line
                                Log.d(
                                    DEBUG_TAG,
                                    "get mResult$mResult"
                                )
                                break
                            }
                            if (line == "version:") {
                                bVersion = true
                            }
                            if (line == null) {
                                break
                            }
                        }
                        br.close()
                    }
                    conn.disconnect()
                }
            } catch (e: Exception) {
            }
            sVersion = mResult!!.toInt()
            try {
                val i_tmp = this.packageManager.getPackageInfo(this.packageName, 0)
                sMyVersion = i_tmp.versionCode
            } catch (e: PackageManager.NameNotFoundException) {
            }
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            var ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val isWifiAvail = ni!!.isAvailable
            val isWifiConn = ni.isConnected
            ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            val isMobileAvail = ni!!.isAvailable
            val isMobileConn = ni.isConnected
            val status = """
                WiFi
                Avail = $isWifiAvail
                Conn = $isWifiConn
                Mobile
                Avail = $isMobileAvail
                Conn = $isMobileConn
                
                """.trimIndent()
            if (isWifiConn || isMobileConn) {
                if (sVersion != sMyVersion) {
                    Log.d(DEBUG_TAG, "update sVersion is $sVersion")
                    Log.d(
                        DEBUG_TAG,
                        "update sMyVersion is $sMyVersion"
                    )
                    val alert = AlertDialog.Builder(this)
                        .setTitle("Notice.")
                        .setMessage("Your Mobibench is not the latest version. Do you want to update your app?")
                        .setCancelable(true)
                        .setPositiveButton(
                            "Update"
                        ) { dialog, whichButton ->
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=esos.MobiBench")
                            )
                            startActivity(intent)
                        }
                        .setNegativeButton(
                            "Later"
                        ) { dialog, whichButton -> }
                        .show()
                }
            }
        }
        var target_path: String? = null
        when (set.get_target_partition()) {
            0 -> target_path = Environment.getDataDirectory().path
            1 -> target_path = Environment.getExternalStorageDirectory().path
            2 -> target_path = MobiBenchExe.sdcard_2nd_path
        }
        free_space = StorageOptions.getAvailableSize(target_path!!)
        free_suffix = StorageOptions.formatSize(free_space)
        TV_free_space = findViewById<View>(R.id.freespace) as TextView
        TV_free_space!!.text = "(" + free_suffix + " free)"

        // Activity가 실행 중인 동안 화면을 밝게 유지합니다.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /* Image button listener*/findViewById<View>(R.id.btn_execute).setOnClickListener(
            mClickListener
        )
        findViewById<View>(R.id.btn_all).setOnClickListener(mClickListener)

        /* ******************* */
        /*   Spinner Control   */
        /* ******************* */
        // Partition spinner
        sp_partition!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                var target_path: String? = null
                when (position) {
                    0 -> {
                        editor!!.putInt("p_target_partition", 0)
                        set.set_target_partition(0)
                        target_path = Environment.getDataDirectory().path
                    }
                    1 -> {
                        editor!!.putInt("p_target_partition", 1)
                        set.set_target_partition(1)
                        target_path = Environment.getExternalStorageDirectory().path
                    }
                    2 -> {
                        editor!!.putInt("p_target_partition", 2)
                        set.set_target_partition(2)
                        target_path = m_exe!!.rt_sd() // m_exe.sdcard_2nd_path
                    }
                }
                editor!!.commit()
                free_space = StorageOptions.getAvailableSize(target_path!!)
                free_suffix = StorageOptions.formatSize(free_space)
                TV_free_space!!.text = "(" + free_suffix + " free)"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        // File synchronization spinner
        sp_file_sync!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        editor!!.putInt("p_file_sync_mode", 0)
                        set.set_file_sync_mode(0)
                    }
                    1 -> {
                        editor!!.putInt("p_file_sync_mode", 1)
                        set.set_file_sync_mode(1)
                    }
                    2 -> {
                        editor!!.putInt("p_file_sync_mode", 2)
                        set.set_file_sync_mode(2)
                    }
                    3 -> {
                        editor!!.putInt("p_file_sync_mode", 3)
                        set.set_file_sync_mode(3)
                    }
                    4 -> {
                        editor!!.putInt("p_file_sync_mode", 4)
                        set.set_file_sync_mode(4)
                    }
                    5 -> {
                        editor!!.putInt("p_file_sync_mode", 5)
                        set.set_file_sync_mode(5)
                    }
                    6 -> {
                        editor!!.putInt("p_file_sync_mode", 6)
                        set.set_file_sync_mode(6)
                    }
                    7 -> {
                        editor!!.putInt("p_file_sync_mode", 7)
                        set.set_file_sync_mode(7)
                    }
                }
                editor!!.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        // IO size spinner
        sp_io_size!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        editor!!.putInt("p_io_size", 0)
                        set.set_io_size(0)
                    }
                    1 -> {
                        editor!!.putInt("p_io_size", 1)
                        set.set_io_size(1)
                    }
                    2 -> {
                        editor!!.putInt("p_io_size", 2)
                        set.set_io_size(2)
                    }
                    3 -> {
                        editor!!.putInt("p_io_size", 3)
                        set.set_io_size(3)
                    }
                    4 -> {
                        editor!!.putInt("p_io_size", 4)
                        set.set_io_size(4)
                    }
                }
                editor!!.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        // SQLite synchronization spinner
        sp_sql_sync!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        editor!!.putInt("p_sql_sync_mode", 0)
                        set.set_sql_sync_mode(0)
                    }
                    1 -> {
                        editor!!.putInt("p_sql_sync_mode", 1)
                        set.set_sql_sync_mode(1)
                    }
                    2 -> {
                        editor!!.putInt("p_sql_sync_mode", 2)
                        set.set_sql_sync_mode(2)
                    }
                }
                editor!!.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        // SQL journaling spinner
        sp_journal!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        editor!!.putInt("p_journal_mode", 0)
                        set.set_journal_mode(0)
                    }
                    1 -> {
                        editor!!.putInt("p_journal_mode", 1)
                        set.set_journal_mode(1)
                    }
                    2 -> {
                        editor!!.putInt("p_journal_mode", 2)
                        set.set_journal_mode(2)
                    }
                    3 -> {
                        editor!!.putInt("p_journal_mode", 3)
                        set.set_journal_mode(3)
                    }
                    4 -> {
                        editor!!.putInt("p_journal_mode", 4)
                        set.set_journal_mode(4)
                    }
                    5 -> {
                        editor!!.putInt("p_journal_mode", 5)
                        set.set_journal_mode(5)
                    }
                }
                editor!!.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }


        /* ******************* */
        /*  Check box control  */
        /* ******************* */
        // Sequential Write Check box
        CB_SW = findViewById<View>(R.id.cb_sw) as CheckBox
        CB_SW!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_seq_write(true)
            } else {
                set.set_seq_write(false)
            }
        }

        // Sequential Read Check box
        CB_SR = findViewById<View>(R.id.cb_sr) as CheckBox
        CB_SR!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_seq_read(true)
            } else {
                set.set_seq_read(false)
            }
        }
        // Random Write Check box
        CB_RW = findViewById<View>(R.id.cb_rw) as CheckBox
        CB_RW!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_ran_write(true)
            } else {
                set.set_ran_write(false)
            }
        }

        // Random Read Check box
        CB_RR = findViewById<View>(R.id.cb_rr) as CheckBox
        CB_RR!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_ran_read(true)
            } else {
                set.set_ran_read(false)
            }
        }

        // SQLite Insert Check box
        CB_INSERT = findViewById<View>(R.id.cb_insert) as CheckBox
        CB_INSERT!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_insert(true)
            } else {
                set.set_insert(false)
            }
        }

        // Sequential Write Check box
        CB_UPDATE = findViewById<View>(R.id.cb_update) as CheckBox
        CB_UPDATE!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_update(true)
            } else {
                set.set_update(false)
            }
        }

        // Sequential Write Check box
        CB_DELETE = findViewById<View>(R.id.cb_delete) as CheckBox
        CB_DELETE!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true) {
                set.set_delete(true)
            } else {
                set.set_delete(false)
            }
        }


        //        mb_thread.setDaemon(true);
        con = this
        Log.d(DEBUG_TAG, "**********onCreate complete")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mFlag) {
                Toast.makeText(
                    this@TabMain,
                    "Press the \"Back button\" again to exit",
                    Toast.LENGTH_SHORT
                ).show()
                mFlag = true
                mHandler.sendEmptyMessageDelayed(666, 2000)
                return false
            } else {
                setResult(RESULT_CANCELED)
                storeValue()
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun startMobibenchExe(type: Int) {
        if (btn_clk_check == false) {
            print_error(0)
        } else {
            image!!.post(StartAni())
            btn_clk_check = false
            Log.d(DEBUG_TAG, "[TM] BTN_CLICK:TRUE" + "[" + btn_clk_check + "]")
            storeValue()
            if (et_filesize_w!!.text.toString()
                    .toInt() >= free_space / 1024 / 1024 || et_filesize_r!!.text.toString()
                    .toInt() >= free_space / 1024 / 1024
            ) {
                print_error(2)
                btn_clk_check = true
                return
            }
            set_configuration()
            DialogActivity.g_def = check_default()
            DialogActivity.ClearResult(dbAdapter)
            m_exe!!.setMobiBenchExe(type)
            print_exp(type)
            mb_thread = MobiBenchExe(con, mHandler)
            mb_thread!!.start()
        }
    }

    /* Image button listener*/
    var mClickListener = View.OnClickListener { v ->
        //		Intent intent;
        when (v.id) {
            R.id.btn_execute -> {
                set_default()
                load_init()
            }
            R.id.btn_all -> startMobibenchExe(0)
        }
    }

    /* Load : stored in preferences */ /* Load preferenced values */
    private fun load_init() {
        if (StorageOptions.b_2nd_sdcard === false && prefs!!.getInt("p_target_partition", 0) == 2) {
            sp_partition!!.setSelection(0)
        } else {
            sp_partition!!.setSelection(prefs!!.getInt("p_target_partition", 0))
        }
        et_threadnum!!.setText(prefs!!.getInt("p_threadnum", 1).toString())
        et_filesize_w!!.setText(prefs!!.getInt("p_filesize_w", 10).toString())
        et_filesize_r!!.setText(prefs!!.getInt("p_filesize_r", 32).toString())
        //et_io_size.setText(String.valueOf(prefs.getInt("p_io_size", 4)));
        sp_io_size!!.setSelection(prefs!!.getInt("p_io_size", 0))
        sp_file_sync!!.setSelection(prefs!!.getInt("p_file_sync_mode", 3))
        et_transaction!!.setText(prefs!!.getInt("p_transaction", 1).toString())
        sp_sql_sync!!.setSelection(prefs!!.getInt("p_sql_sync_mode", 0))
        sp_journal!!.setSelection(prefs!!.getInt("p_journal_mode", 0))
        if (StorageOptions.b_2nd_sdcard === false && prefs!!.getInt("p_target_partition", 0) == 2) {
            set.set_target_partition(0)
        } else {
            set.set_target_partition(prefs!!.getInt("p_target_partition", 0))
        }
        set.set_thread_num(prefs!!.getInt("p_threadnum", 1))
        set.set_filesize_write(prefs!!.getInt("p_filesize_w", 10))
        set.set_filesize_read(prefs!!.getInt("p_filesize_r", 32))
        set.set_io_size(prefs!!.getInt("p_io_size", 0))
        set.set_file_sync_mode(prefs!!.getInt("p_file_sync_mode", 3))
        set.set_transaction_num(prefs!!.getInt("p_transaction", 1))
        set.set_sql_sync_mode(prefs!!.getInt("p_sql_sync_mode", 0))
        set.set_journal_mode(prefs!!.getInt("p_journal_mode", 0))

        /* Check box setting */Log.d(DEBUG_TAG, "[JWGOM] start")
        CB_SW!!.isChecked = prefs!!.getBoolean("p_cb_sw", false)
        CB_SR!!.isChecked = prefs!!.getBoolean("p_cb_sr", false)
        CB_RW!!.isChecked = prefs!!.getBoolean("p_cb_rw", false)
        CB_RR!!.isChecked = prefs!!.getBoolean("p_cb_rr", false)
        CB_INSERT!!.isChecked = prefs!!.getBoolean("p_cb_insert", false)
        CB_UPDATE!!.isChecked = prefs!!.getBoolean("p_cb_update", false)
        CB_DELETE!!.isChecked = prefs!!.getBoolean("p_cb_delete", false)
        Log.d(DEBUG_TAG, "[JWGOM] end1")
        set.set_seq_write(prefs!!.getBoolean("p_cb_sw", false))
        set.set_seq_read(prefs!!.getBoolean("p_cb_sr", false))
        set.set_ran_write(prefs!!.getBoolean("p_cb_rw", false))
        set.set_ran_read(prefs!!.getBoolean("p_cb_rr", false))
        set.set_insert(prefs!!.getBoolean("p_cb_insert", false))
        set.set_update(prefs!!.getBoolean("p_cb_update", false))
        set.set_delete(prefs!!.getBoolean("p_cb_delete", false))
        set.set_cb_count(prefs!!.getInt("p_cb_count", 0))
        //Toast.makeText(this, "load init cb count "+ set.get_cb_count() , Toast.LENGTH_SHORT).show();
        Log.d(DEBUG_TAG, "[JWGOM] end")
        //print_values();
    }

    /* Store values : To the preference */
    fun storeValue() {
        set.set_thread_num(et_threadnum!!.text.toString().toInt())
        editor!!.putInt("p_threadnum", et_threadnum!!.text.toString().toInt())

        //	if(Integer.parseInt(et_filesize_w.getText().toString()) >= free_space/1024/1024)
        if (et_filesize_w!!.text.toString().toInt() < free_space / 1024 / 1024) {
            set.set_filesize_write(et_filesize_w!!.text.toString().toInt())
            editor!!.putInt("p_filesize_w", et_filesize_w!!.text.toString().toInt())
        } else {
            Log.d(DEBUG_TAG, "[JWGOM] storeValue() -> file size write is not saved")
        }
        if (et_filesize_r!!.text.toString().toInt() < free_space / 1024 / 1024) {
            set.set_filesize_read(et_filesize_r!!.text.toString().toInt())
            editor!!.putInt("p_filesize_r", et_filesize_r!!.text.toString().toInt())
        }
        //set.set_io_size(Integer.parseInt(et_io_size.getText().toString()));
        //editor.putInt("p_io_size", Integer.parseInt(et_io_size.getText().toString()));
        set.set_transaction_num(et_transaction!!.text.toString().toInt())
        editor!!.putInt("p_transaction", et_transaction!!.text.toString().toInt())

        /* Store : Checkbox */set.set_seq_write(CB_SW!!.isChecked)
        set.set_seq_read(CB_SR!!.isChecked)
        set.set_ran_write(CB_RW!!.isChecked)
        set.set_ran_read(CB_RR!!.isChecked)
        set.set_insert(CB_INSERT!!.isChecked)
        set.set_update(CB_UPDATE!!.isChecked)
        set.set_delete(CB_DELETE!!.isChecked)
        editor!!.putBoolean("p_cb_sw", CB_SW!!.isChecked)
        editor!!.putBoolean("p_cb_sr", CB_SR!!.isChecked)
        editor!!.putBoolean("p_cb_rw", CB_RW!!.isChecked)
        editor!!.putBoolean("p_cb_rr", CB_RR!!.isChecked)
        editor!!.putBoolean("p_cb_insert", CB_INSERT!!.isChecked)
        editor!!.putBoolean("p_cb_update", CB_UPDATE!!.isChecked)
        editor!!.putBoolean("p_cb_delete", CB_DELETE!!.isChecked)


        //Toast.makeText(this, "store : cb count "+ set.get_cb_count() , Toast.LENGTH_SHORT).show();
        //print_values();
        editor!!.commit()
    }

    fun set_default() {
        editor!!.putInt("p_target_partition", 0)
        set.set_target_partition(0)
        editor!!.putInt("p_threadnum", 1)
        set.set_thread_num(1)
        editor!!.putInt("p_filesize_w", 10)
        set.set_filesize_write(10)
        editor!!.putInt("p_filesize_r", 32)
        set.set_filesize_read(32)
        editor!!.putInt("p_io_size", 0)
        set.set_io_size(0)
        editor!!.putInt("p_file_sync_mode", 3)
        set.set_file_sync_mode(0)
        editor!!.putInt("p_transaction", 100)
        set.set_transaction_num(100)
        editor!!.putInt("p_sql_sync_mode", 2)
        set.set_sql_sync_mode(1)
        editor!!.putInt("p_journal_mode", 1)
        set.set_journal_mode(1)
        editor!!.putInt("p_cb_count", 0)
        set.set_cb_count(0)

        /* Checkbox */set.set_seq_write(false)
        set.set_seq_read(false)
        set.set_ran_write(false)
        set.set_ran_read(false)
        set.set_insert(false)
        set.set_update(false)
        set.set_delete(false)
        editor!!.putBoolean("p_cb_sw", false)
        editor!!.putBoolean("p_cb_sr", false)
        editor!!.putBoolean("p_cb_rw", false)
        editor!!.putBoolean("p_cb_rr", false)
        editor!!.putBoolean("p_cb_insert", false)
        editor!!.putBoolean("p_cb_update", false)
        editor!!.putBoolean("p_cb_delete", false)
        editor!!.commit()
    }

    fun print_error(type: Int) {
        when (type) {
            0 -> Toast.makeText(this, "MobiBench working..", Toast.LENGTH_SHORT).show()
            1 -> Toast.makeText(this, "Benchmark engin exited with error", Toast.LENGTH_LONG).show()
            2 -> Toast.makeText(
                this,
                "The file size must be less than the free space.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun print_exp(flag: Int) {
        when (flag) {
            0 -> Toast.makeText(this, "Start Benchmark : File, SQlite", Toast.LENGTH_SHORT).show()
            1 -> Toast.makeText(this, "Start Benchmark : File", Toast.LENGTH_SHORT).show()
            2 -> Toast.makeText(this, "Start Benchmark : SQlite", Toast.LENGTH_SHORT).show()
            3 -> Toast.makeText(this, "Start Benchmark : Customized set", Toast.LENGTH_SHORT).show()
            4 -> Toast.makeText(this, "Nothing selected. Check \"Setting tab\"", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        db_index = prefs!!.getInt("database_index", 0) // data base indexing
        arr.clear()
        for (i in 0 until db_index) {
            result = dbAdapter!!.fetchNote(i.toLong()) // 횟수 제한
            result!!.moveToFirst()
            db_date = " " + result!!.getString(2) + "  ( " + result!!.getString(1) + " )"
            arr.add(db_date!!)
            //aa.notifyDataSetChanged();
        }
        val list = findViewById<View>(R.id.ListView01) as ListView
        aa = ArrayAdapter( // ListView에 할당할 ArrayAdapter 생성
            this, R.layout.history_listitem, arr
        ) // 여기에 앞에서 만든 ArrayList를 사용한다
        list.adapter = aa // ListView에 ArrayAdapter 설정
    }


    fun set_configuration() {
        DialogActivity.g_partition = sp_partition!!.selectedItem.toString()
        DialogActivity.g_thread = et_threadnum!!.text.toString()
        DialogActivity.g_file_size_w = et_filesize_w!!.text.toString()
        DialogActivity.g_file_size_r = et_filesize_r!!.text.toString()
        DialogActivity.g_io_size = sp_io_size!!.selectedItem.toString()
        DialogActivity.g_file_mode = sp_file_sync!!.selectedItem.toString()
        DialogActivity.g_transaction_mode = et_transaction!!.text.toString()
        DialogActivity.g_sqlite_mode = sp_sql_sync!!.selectedItem.toString()
        DialogActivity.g_sqlite_journal = sp_journal!!.selectedItem.toString()

        /*
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_partition);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_thread);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_file_size_w);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_file_size_r);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_io_size);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_file_mode);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_transaction_mode);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_sqlite_mode);
		Log.d(DEBUG_TAG, "[JWGOM] Configuration value " + DialogActivity.g_sqlite_journal);
		 */return
    }

    fun check_default(): String {
        var tmp_result = "1"
        if (sp_partition!!.selectedItemPosition != 0) tmp_result = "0"
        //Log.d(DEBUG_TAG, "[JWGOM] ------------sp_partition " + (sp_partition.getSelectedItemPosition() == 0) + "[" + tmp_result + "]");
        if (et_threadnum!!.text.toString() != "1") tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] ------------et_threadnum " + et_threadnum.getText().toString().equals("1") + "[" + tmp_result + "]");
        if (et_filesize_w!!.text.toString() != "10") tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] -----------et_filesize_w " + et_filesize_w.getText().toString().equals("10") + "[" + tmp_result + "]");
        if (et_filesize_r!!.text.toString() != "32") tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] -----------et_filesize_r " + et_filesize_r.getText().toString().equals("32") + "[" + tmp_result + "]");
        if (sp_io_size!!.selectedItemPosition != 0) tmp_result = "0"
        //Log.d(DEBUG_TAG, "[JWGOM] --------------sp_io_size " + (sp_io_size.getSelectedItemPosition() == 0) + "[" + tmp_result + "]");
        if (sp_file_sync!!.selectedItemPosition != 3) tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] ------------sp_file_sync " + (sp_file_sync.getSelectedItemPosition() == 3) + "[" + tmp_result + "]");
        if (et_transaction!!.text.toString() != "100") tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] ----------et_transaction " + et_transaction.getText().toString().equals("100") + "[" + tmp_result + "]");
        if (sp_sql_sync!!.selectedItemPosition != 2) tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] -------------sp_sql_sync " + (sp_sql_sync.getSelectedItemPosition() == 2) + "[" + tmp_result + "]");
        if (sp_journal!!.selectedItemPosition != 1) tmp_result = "0"
        //	Log.d(DEBUG_TAG, "[JWGOM] -------------sp_journal " + (sp_journal.getSelectedItemPosition() == 1) + "[" + tmp_result + "]");


        //Log.d(DEBUG_TAG, "[JWGOM] ------------ Return value is : " +tmp_result);
        return tmp_result
    }

    internal inner class StartAni : Runnable {
        override fun run() {
            anidrawable!!.start()
        }

        fun stop() {}
    }

    companion object {
        private var result_start = 0

        /*jwgom*/
        private const val DEBUG_TAG = "progress bar"
        var prBar: ProgressBar? = null
        private var btn_clk_check = true
        private var free_space: Long = 0
        private var free_suffix: String? = null
        var g_animation = true
        const val PROGRESS_DIALOG = 0

        // For Database
        var dbAdapter: NotesDbAdapter? = null
        fun getMD5Hash(s: String): String? {
            var m: MessageDigest? = null
            var hash: String? = null
            try {
                m = MessageDigest.getInstance("MD5")
                m.update(s.toByteArray(), 0, s.length)
                hash = BigInteger(1, m.digest()).toString(16)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return hash
        }
    }
}