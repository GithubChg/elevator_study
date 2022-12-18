package com.example.elevator_test

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import elevator1
import elevator2
import estimatedTimeOfArrival
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.activity_status2.*
import kotlinx.android.synthetic.main.activity_status2.btn_bottom_register
import kotlinx.android.synthetic.main.activity_status2.btn_bottom_result
import kotlinx.android.synthetic.main.activity_status2.btn_bottom_status
import main
import now
import kotlin.concurrent.thread

class StatusActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status2)




        thread(start = true) {
            while(true) {

                runOnUiThread {
                    var textView=findViewById<TextView>(R.id.tv_now)
                    textView.text =now.toString()

                    //1번 엘리베이터일 때 실시간 데이터 갱신
                    if(intent.getStringExtra("elevatorNum")=="1"){
                        var tv_elevatorNumber=findViewById<TextView>(R.id.tv_elevatorNumber)
                        tv_elevatorNumber.text="엘리베이터 1"

                        var tv_elevatorPeople=findViewById<TextView>(R.id.tv_elevatorPeople)
                        tv_elevatorPeople.text =(elevator1.up_user_list.size+elevator1.down_user_list.size).toString()

                        var tv_elevatorDirection=findViewById<TextView>(R.id.tv_elevatorDirection)
                        tv_elevatorDirection.text =elevator1.direction

                        var tv_elevatorNow=findViewById<TextView>(R.id.tv_elevatorNow)
                        tv_elevatorNow.text =elevator1.next_floor.toString()
                    }
                    else if(intent.getStringExtra("elevatorNum")=="2"){
                        var tv_elevatorNumber=findViewById<TextView>(R.id.tv_elevatorNumber)
                        tv_elevatorNumber.text="엘리베이터 2"

                        var tv_elevatorPeople=findViewById<TextView>(R.id.tv_elevatorPeople)
                        tv_elevatorPeople.text =(elevator2.up_user_list.size+elevator2.down_user_list.size).toString()

                        var tv_elevatorDirection=findViewById<TextView>(R.id.tv_elevatorDirection)
                        tv_elevatorDirection.text =elevator2.direction

                        var tv_elevatorNow=findViewById<TextView>(R.id.tv_elevatorNow)
                        tv_elevatorNow.text =elevator2.next_floor.toString()
                    }

                }
                Thread.sleep(1000)
            }
        }


        //StatusActivity에서 전달받은 엘리베이터 번호
        val elevatorNum=intent.getStringExtra("elevatorNum")
        //엘리베이터 번호 출력하기
        if(elevatorNum=="1"){
            tv_elevatorNumber.text="엘리베이터 1"
        }
        else{
            tv_elevatorNumber.text="엘리베이터 2"
        }

        //TODO : 탑승인원, 방향, 현재층 출력하기
        //엘리베이터1 조회하기
        if(elevatorNum=="1"){
            val temp_people=elevator1.up_user_list.size+elevator1.down_user_list.size
            var temp_direction="멈춤"
            if(elevator1.direction=="STOP"){
                temp_direction="멈춤"
            }
            else if(elevator1.direction=="UP"){
                temp_direction="하강 ↓"
            }
            else if(elevator1.direction=="DOWN"){
                temp_direction="상승 ↑"
            }

            //TODO : 현재층은 어떻게..?

            tv_elevatorPeople.text=temp_people.toString()+"명"
            tv_elevatorDirection.text=temp_direction
        }
        else if(elevatorNum=="2"){
            val temp_people=elevator2.up_user_list.size+elevator2.down_user_list.size
            var temp_direction="정지"
            if(elevator2.direction=="STOP"){
                temp_direction="정지"
            }
            else if(elevator2.direction=="UP"){
                temp_direction="하강 ↓"
            }
            else if(elevator2.direction=="DOWN"){
                temp_direction="상승 ↑"
            }
            

            tv_elevatorPeople.text=temp_people.toString()+"명"
            tv_elevatorDirection.text=temp_direction
        }

        //뒤로가기를 클릭한 경우
        btn_back.setOnClickListener {
            val intent=Intent(this,StatusActivity::class.java)
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