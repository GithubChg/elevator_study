package com.example.elevator_test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import estimatedTimeOfArrival
import flag_ride
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.activity_main2.btn_bottom_register
import kotlinx.android.synthetic.main.activity_main2.btn_bottom_result
import kotlinx.android.synthetic.main.activity_main2.btn_bottom_status
import kotlin.concurrent.thread

import main
import now
import real_user_quit_floor
import real_user_ride_floor
import recommededElevator

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)



        thread(start = true) {
            while(true) {

                runOnUiThread {
                    var textView=findViewById<TextView>(R.id.tv_now)
                    textView.text =now.toString()


                    if(flag_ride) {
                        var e_time=findViewById<TextView>(R.id.tv_e_time)
                        e_time.text =estimatedTimeOfArrival.toString()+"초에 탑승 완료"

                        var e_elevator=findViewById<TextView>(R.id.tv_e_elevator)
                        e_elevator.text =recommededElevator.toString()+"에 탑승 완료"
                    }
                    else{
                        var e_time=findViewById<TextView>(R.id.tv_e_time)
                        e_time.text =estimatedTimeOfArrival.toString()+"초에 탑승 예정"

                        var e_elevator=findViewById<TextView>(R.id.tv_e_elevator)
                        e_elevator.text =recommededElevator.toString()+"에 탑승 예정"
                    }


                }
                Thread.sleep(1000)
            }
        }

        val startfloor=real_user_ride_floor.toString()//사용자가 입력한 출발층
        val endfloor=real_user_quit_floor.toString()//사용자가 입력한 도착층

        //화면에 출발층과 도착층을 출력
        tv_startfloor.setText(startfloor+"층")
        tv_endfloor.setText(endfloor+"층")


/*
        //뒤로가기 버튼을 클릭한 경우->예약하기 페이지로 이동
        //뒤로 가기 해도 다시 예약하지 못해서 그냥 뒤로 가기 버튼 없앴습니다.
        btn_back.setOnClickListener {
            val intent=Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
*/
        //하단 메뉴(예약, 상태, 성능평가)를 클릭한 경우->각각의 페이지로 이동
        btn_bottom_register.setOnClickListener {
            val intent=Intent(this,MainActivity2::class.java)
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