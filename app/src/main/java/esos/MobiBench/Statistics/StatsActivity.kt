package esos.MobiBench.Statistics

import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import esos.MobiBench.NotesDbAdapter
import esos.MobiBench.R

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        init()
    }

    private fun init(){

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        var barChart: BarChart = findViewById(R.id.Chart) // barChart 생성


        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(i){
                R.id.radioButton1 -> setStats(barChart, 0)
                R.id.radioButton2 -> setStats(barChart, 1)
                R.id.radioButton3 -> setStats(barChart, 2)
                R.id.radioButton4 -> setStats(barChart, 3)
            }
        }


    }

    private fun setStats(barChart:BarChart, type:Int){

        var dbAdapter = NotesDbAdapter(this)
        dbAdapter!!.open()

        val row:Int = dbAdapter.getNum()+1
        val col:Int = 10
        var multiIntArray: Array<IntArray> = Array<IntArray>(row) { IntArray(4) }
        var max:Float = 0f

        for(i in 0 until row){
            val strsample = dbAdapter!!.findthrp(i)
            val result = strsample.split("KB/s", "IOPS(4KB)"," ")
            for(j in 0 until col){
                if(j%3==0){
                    multiIntArray[i][j/3] = result[j].toInt()
                }
                Log.i(result[j],"i :"+i.toString()+" j :"+j.toString())
            }
        }

        val entries = ArrayList<BarEntry>()
        if(row>7) {
            for (i in row-7 until row) {
                entries.add(BarEntry((i - (row-7) + 1).toFloat(), multiIntArray[i][type].toFloat()))
                if (multiIntArray[i][type] > max) {
                    max = multiIntArray[i][type].toFloat()
                }
            }
        }else{
            for (i in 0 until row) {
                entries.add(BarEntry((i + 1).toFloat(), multiIntArray[i][type].toFloat()))
                if (multiIntArray[i][type] > max) {
                    max = multiIntArray[i][type].toFloat()
                }
            }
        }

        barChart.run {
            description.isEnabled = false // 차트 옆에 별도로 표기되는 description을 안보이게 설정 (false)
            setMaxVisibleValueCount(7) // 최대 보이는 그래프 개수를 7개로 지정
            setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
            setDrawBarShadow(false) //그래프의 그림자
            setDrawGridBackground(false)//격자구조 넣을건지
            axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                //axisMaximum = 101f //100 위치에 선을 그리기 위해 101f로 맥시멈값 설정
                axisMaximum = max
                axisMinimum = 0f // 최소값 0
                granularity = 50f // 50 단위마다 선을 그리려고 설정.
                setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                setDrawGridLines(true) //격자 라인 활용
                setDrawAxisLine(false) // 축 그리기 설정
                axisLineColor = ContextCompat.getColor(context,R.color.design_default_color_secondary_variant) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context,R.color.design_default_color_on_secondary) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(context,R.color.design_default_color_primary_dark) // 라벨 텍스트 컬러 설정
                textSize = 13f //라벨 텍스트 크기
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 1f // 1 단위만큼 간격 두기
                setDrawAxisLine(true) // 축 그림
                setDrawGridLines(false) // 격자
                textColor = ContextCompat.getColor(context,R.color.design_default_color_primary_dark) //라벨 색상
                textSize = 12f // 텍스트 크기
                valueFormatter = MyXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
            }
            axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
            setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
            animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
            legend.isEnabled = false //차트 범례 설정
        }

        var set = BarDataSet(entries,"DataSet") // 데이터셋 초기화
        set.color = ContextCompat.getColor(applicationContext!!,R.color.design_default_color_primary_dark) // 바 그래프 색 설정

        val dataSet :ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.3f //막대 너비 설정
        barChart.run {
            this.data = data //차트의 데이터를 data로 설정해줌.
            setFitBars(true)
            invalidate()
        }
    }

    inner class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("1st","2nd","3rd","4th","5th","6th","7th")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }
}