package esos.MobiBench

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.isapanah.awesomespinner.AwesomeSpinner
import esos.MobiBench.databinding.ActivityUserBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator


class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding
    lateinit var adapter: UserAdapter

    var beginDay:Int = 0

    var begin:String = ""

    var totalTime:Int= 0
    var beginDate4TextView:String = ""
    var endDate4TextView:String = ""

    var mes:String = ""

    private val selectItems = arrayOf("1-Day","2-Day","3-Day","4-Day","5-Day","6-Day",
        "1-Week","2-Week","3-Week", "1-Month", "2-Month", "3-Month", "1-Year")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) //keyboard
        init()
    }

    private fun init(){

        initMeterialSpinner(binding.spinner)

        binding.buttonRun.setOnClickListener {

            beginDay = 0
            totalTime = 0
            beginDate4TextView = ""
            endDate4TextView = ""
            mes = ""
            initBeginDay()

            if(begin==""){
                Toast.makeText(this, "기간을 선택하세요.",Toast.LENGTH_SHORT).show()
            }else{
                showAppUsageStats(getAppUsageStats())
                initRecyclerView()
                initValue()
                calculateTotalTime()
                setText(binding.textViewBegin, binding.textViewEnd, binding.textViewTime)
            }
        }
        binding.buttonShare.setOnClickListener {
            if(mes!=""){
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/html"
                sharingIntent.putExtra(Intent.EXTRA_TEXT, mes)
                startActivity(Intent.createChooser(sharingIntent, "Share"))
            }else{
                Toast.makeText(this, "실행 버튼을 눌러 측정해주세요.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initValue(){
        totalTime = 0
        beginDate4TextView = ""
        endDate4TextView = ""
    }

    private fun initMeterialSpinner(spinner: AwesomeSpinner){
        val categoriesAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, selectItems)
        spinner.setAdapter(categoriesAdapter)
        spinner.setOnSpinnerItemClickListener { position, itemAtPosition ->
            when(position){
                0 -> {
                    begin = "1-Day"
                }
                1 -> {
                    begin = "2-Day"
                }
                2 -> {
                    begin = "3-Day"
                }
                3 -> {
                    begin = "4-Day"
                }
                4 -> {
                    begin = "5-Day"
                }
                5 -> {
                    begin = "6-Day"
                }
                6 -> {
                    begin = "1-Week"
                }
                7 -> {
                    begin = "2-Week"
                }
                8 -> {
                    begin = "3-Week"
                }
                9 -> {
                    begin = "1-Month"
                }
                10 -> {
                    begin = "2-Month"
                }
                11 -> {
                    begin = "3-Month"
                }
                12 -> {
                    begin = "1-Year"
                }
            }
        }

    }

    private fun initBeginDay(){
        when(begin){
            "1-Day" -> beginDay=1
            "2-Day" -> beginDay=2
            "3-Day" -> beginDay=3
            "4-Day" -> beginDay=4
            "5-Day" -> beginDay=5
            "6-Day" -> beginDay=6
            "1-Week" -> beginDay=7
            "2-Week" -> beginDay=14
            "3-Week" -> beginDay=21
            "1-Month" -> beginDay=30
            "2-Month" -> beginDay=60
            "3-Month" -> beginDay=90
            "1-Year" -> beginDay=365
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        adapter = UserAdapter(ArrayList<UserData>())
        mes = ""
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                val apppackname = appinfo.packageName
                var appicon:Drawable
                try{
                    appicon = packageManager.getApplicationIcon(appinfo.packageName)
                }catch (e: PackageManager.NameNotFoundException){
                    appicon = R.drawable.ic_baseline_android_24.toDrawable()
                }
                if (apptime.toString() != "0") {
                    adapter.items.add(UserData(apptime.toString() + " ms", apppackname, appicon))
                    mes = mes + "packagename: " + appinfo.packageName + " runtime: " + apptime.toString() + "ms" + "\n"
                }
            }
        }
        binding.recyclerView.adapter = adapter
    }

    // public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime)
    // intervalType = INTERVAL_BEST, INTERVAL_DAILY, INTERVAL_MONTHLY, INTERVAL_WEEKLY, INTERVAL_YEARLY

    private fun getAppUsageStats(): MutableList<UsageStats> {
        val calBegin = Calendar.getInstance()
        val calEnd = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        calBegin.add(Calendar.DATE, -beginDay)

        Log.d(TAG, "begin: ${df.format(calBegin.time)}")
        Log.d(TAG, "end: ${df.format(calEnd.time)}")

        beginDate4TextView = df.format(calBegin.time)
        endDate4TextView = df.format(calEnd.time)

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats:MutableList<UsageStats>
        if(begin.contains("Day")){
            queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
            )
            Log.d(TAG, "INTERVAL_DAILY: ${UsageStatsManager.INTERVAL_DAILY} Begin: ${calBegin.timeInMillis}")
        }else if(begin.contains("Week")){
            queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_WEEKLY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
            )
            Log.d(TAG, "INTERVAL_WEEKLY: ${UsageStatsManager.INTERVAL_WEEKLY} Begin: ${calBegin.timeInMillis}")
        }else if(begin.contains("Month")){
            queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_MONTHLY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
            )
            Log.d(TAG, "INTERVAL_MONTHLY: ${UsageStatsManager.INTERVAL_MONTHLY} Begin: ${calBegin.timeInMillis}")
        }else{
            queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_YEARLY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
            )
            Log.d(TAG, "INTERVAL_YEARLY: ${UsageStatsManager.INTERVAL_YEARLY} Begin: ${calBegin.timeInMillis}")
        }
        return queryUsageStats
    }

    private fun showAppUsageStats(usageStats: MutableList<UsageStats>) {
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })

        usageStats.forEach { it ->
            Log.d(TAG, "packageName: ${it.packageName}, lastTimeUsed: ${Date(it.lastTimeUsed)}, " +
                    "totalTimeInForeground: ${it.totalTimeInForeground}")
        }
    }
    // getPackageName : 앱 이름, getLastTimeUsed : 마지막으로 사용된 시간, getTotalInForeground : Foreground에서 실행된 전체 시간, getAppLaunchCount : 실행된 횟수

    private fun calculateTotalTime(){
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                if (apptime.toString() != "0") {
                    totalTime += apptime.toInt()
                }
            }
        }
    }

    private fun setText(textBegin: TextView, textEnd: TextView, textTime: TextView){
        textBegin.text = beginDate4TextView
        textEnd.text = endDate4TextView
        val h:Int = ((totalTime/1000)/60)/60
        val m:Int = ((totalTime/1000)/60)%60
        textTime.text = h.toString() + "  시간  " + m.toString() + "  분"
    }
}