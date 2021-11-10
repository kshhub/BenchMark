package esos.MobiBench

import android.R
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import esos.MobiBench.databinding.ActivityUserBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding
    lateinit var adapter: UserAdapter

    var beginDay:Int = 0
    var endDay:Int = 0

    var totalTime:Int= 0
    var beginDate4TextView:String = ""
    var endDate4TextView:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) //keyboard
    }

    private fun init(){

        binding.buttonRun.setOnClickListener {
            if(binding.editTextBegin.text.toString() == "" || binding.editTextBegin.text.toString()==null ||
                binding.editTextEnd.text.toString() == "" || binding.editTextEnd.text.toString()==null){
                Toast.makeText(this, "기간을 입력하세요.",Toast.LENGTH_SHORT).show()
            }else{
                if(beginDay<0 || beginDay>1000 || endDay<0 || endDay>1000){
                    Toast.makeText(this, "0 과 1000 사이의 값을 입력하세요.",Toast.LENGTH_SHORT).show()
                }else{
                    beginDay = binding.editTextBegin.text.toString().toInt()
                    endDay = binding.editTextEnd.text.toString().toInt()
                    showAppUsageStats(getAppUsageStats())
                    initRecyclerView()
                    initValue()
                    calculateTotalTime()
                    setText(binding.textViewBegin, binding.textViewEnd, binding.textViewTime)
                }
            }
        }
    }

    private fun initValue(){
        totalTime = 0
        beginDate4TextView = ""
        endDate4TextView = ""
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        adapter = UserAdapter(ArrayList<UserData>())
        val applist = getAppUsageStats()
        if(applist.size>0){
            for(appinfo in applist){
                val apptime = appinfo.totalTimeInForeground
                val apppackname = appinfo.packageName
                val appicon = packageManager.getApplicationIcon(appinfo.packageName)
                if (apptime.toString() != "0") {
                    adapter.items.add(UserData(apptime.toString() + " ms", apppackname, appicon))
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
        Log.d(TAG, "current: ${df.format(calBegin.time)}")

        calBegin.add(Calendar.DATE, -beginDay)

        calEnd.add(Calendar.DATE, -endDay)

        Log.d(TAG, "after: ${df.format(calBegin.time)}")

        beginDate4TextView = df.format(calBegin.time)
        endDate4TextView = df.format(calEnd.time)

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, calBegin.timeInMillis, calEnd.timeInMillis // 쿼리
        )
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