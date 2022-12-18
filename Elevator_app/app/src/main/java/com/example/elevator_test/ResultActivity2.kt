package com.example.elevator_test

import ALGORITHM
import CONGESTION
import IS_SIMULATION
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import average_distance
import average_waiting
import end
import estimatedTimeOfArrival
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.activity_result2.*
import kotlinx.android.synthetic.main.activity_result2.btn_bottom_register
import kotlinx.android.synthetic.main.activity_result2.btn_bottom_result
import kotlinx.android.synthetic.main.activity_result2.btn_bottom_status
import longest_waiting
import main
import sum_waiting
import main
import now
import sum_distance
import sum_passenger
import kotlin.concurrent.thread

class ResultActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result2)


        thread(start = true) {
            while(true) {
                runOnUiThread {
                    if(end){
                        var tv_algorithm=findViewById<TextView>(R.id.tv_algorithm)
                        tv_algorithm.text =ALGORITHM

                        var tv_average_waiting=findViewById<TextView>(R.id.tv_average_waiting)
                        tv_average_waiting.text =(sum_waiting.toDouble() / sum_passenger.toDouble()).toString()+"초"

                        var tv_longest_waiting=findViewById<TextView>(R.id.tv_longest_waiting)
                        tv_longest_waiting.text =longest_waiting.toString()

                        var tv_average_distance=findViewById<TextView>(R.id.tv_average_distance)
                        tv_average_distance.text =((sum_passenger.toDouble() / sum_distance.toDouble()) * 100).toString()
                    }
                }
                Thread.sleep(200)
            }
        }



//성능평가가 끝나면 예약이나 다른 걸 못해서 그냥 다 없앴습니다.
        //뒤로가기눌렀을때->ResultActivity로 이동
        /*
        btn_back.setOnClickListener {
            val intent=Intent(this,ResultActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        //하단 메뉴(예약, 상태, 성능평가)를 클릭한 경우->각각의 페이지로 이동
        btn_bottom_register.setOnClickListener {
            if(estimatedTimeOfArrival==0)
            {
                val intent= Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
            else
            {
                val intent= Intent(this,MainActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

        }
        btn_bottom_status.setOnClickListener {
            val intent= Intent(this,StatusActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        btn_bottom_result.setOnClickListener {
            val intent= Intent(this,ResultActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        */
    }
}