package com.example.elevator_test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import estimatedTimeOfArrival
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.activity_status.*
import kotlinx.android.synthetic.main.activity_status.btn_bottom_register
import kotlinx.android.synthetic.main.activity_status.btn_bottom_result
import kotlinx.android.synthetic.main.activity_status.btn_bottom_status
import main
import now
import kotlin.concurrent.thread

class StatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)



        thread(start = true) {
            while(true) {

                runOnUiThread {
                    var textView=findViewById<TextView>(R.id.tv_now)
                    textView.text =now.toString()
                }
                Thread.sleep(1000)
            }
        }



        //1번 엘리베이터 조회를 누른 경우 statusactivity에 1을 전달
        //2번 엘리베이터 조회를 누른 경우 statusactivity에 2를 전달
        btn_status_1.setOnClickListener {
            val intent=Intent(this,StatusActivity2::class.java)
            intent.putExtra("elevatorNum","1")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        btn_status_2.setOnClickListener {
            val intent=Intent(this,StatusActivity2::class.java)
            intent.putExtra("elevatorNum","2")
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
    }
}