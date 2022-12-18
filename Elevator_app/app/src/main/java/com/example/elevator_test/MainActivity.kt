package com.example.elevator_test

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import main
import now
import real_user_quit_floor
import real_user_ride_floor
import estimatedTimeOfArrival
import recommededElevator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        thread(start = true) {
                main()
        }


    thread(start = true) {
            while(true) {

                runOnUiThread {
                    var textView=findViewById<TextView>(R.id.tv_now)
                    textView.text =now.toString()
                }
                Thread.sleep(1000)
            }
        }

        //예약하기 버튼을 클릭한 경우
        btn_reserve.setOnClickListener {
            //출발층과 도착층을 입력한 경우
            if(!et_startfloor.text.isEmpty() && !et_endfloor.text.isEmpty()){
                //출발층과 도착층을 넣어 mainactivity2에 전달
                real_user_ride_floor=et_startfloor.text.toString().toInt()
                real_user_quit_floor=et_endfloor.text.toString().toInt()
                val intent= Intent(this,MainActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
            //출발층이나 도착층을 입력하지 않은 경우
            else{
                val builder=AlertDialog.Builder(this)
                builder.setMessage("출발층과 도착층 모두 입력해주세요.")
                    .setPositiveButton("확인",DialogInterface.OnClickListener{
                            dialogInterface, i -> dialogInterface.dismiss()
                    })
                    .create()
                    .show()
            }
        }


        //하단 메뉴(예약, 상태, 성능평가)를 클릭한 경우->각각의 페이지로 이동
        btn_bottom_register.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        btn_bottom_status.setOnClickListener {
            val intent=Intent(this,StatusActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        btn_bottom_result.setOnClickListener {
            val intent=Intent(this,ResultActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }


    }
}