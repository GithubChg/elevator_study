package com.example.elevator_test

import ALGORITHM
import CONGESTION
import IS_SIMULATION
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import estimatedTimeOfArrival
import kotlinx.android.synthetic.main.activity_result.*
import main
import now
import simulation_start
import kotlin.concurrent.thread


class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        simulation_start=true



        var selectedAlgorithm="" //선택한 알고리즘 이름

        //알고리즘 드롭다운 메뉴 연결
        val algorithmList=arrayOf("COLLECTIVE_CONTROL","ZONING","ALGORITHM4")
        val adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,algorithmList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        algorithm_spinner.adapter=adapter

        //알고리즘 드롭다운 선택 값 받아오기
        algorithm_spinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedAlgorithm=algorithmList[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            }
        }


        btn_result.setOnClickListener { 
            //혼잡도를 입력하고 알고리즘을 선택한 경우 성능평가 계산
            if(!et_congestion.text.isEmpty() && selectedAlgorithm!=""){
                val intent=Intent(this,ResultActivity2::class.java)
                intent.putExtra("algorithm",selectedAlgorithm)
                intent.putExtra("congestion",et_congestion.text.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                //사용자가 입력한 혼잡도와 선택한 알고리즘 설정
                Thread.sleep(1000)

                thread(start = true) {
                    main()
                }

                ALGORITHM=selectedAlgorithm
                CONGESTION=et_congestion.text.toString().toInt()
                IS_SIMULATION=true

                startActivity(intent)
            }
            //혼잡도를 입력 안한 경우
            else if(et_congestion.text.isEmpty()){
                val builder= AlertDialog.Builder(this)
                builder.setMessage("혼잡도를 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener{
                            dialogInterface, i -> dialogInterface.dismiss()
                    })
                    .create()
                    .show()
            }
            //알고리즘을 선택 안한 경우-기본적으로 드롭다운 첫번째 메뉴임...
            else if(selectedAlgorithm==""){
                val builder=AlertDialog.Builder(this)
                builder.setMessage("알고리즘을 선택해주세요.")
                    .setPositiveButton("확인",DialogInterface.OnClickListener{
                            dialogInterface, i -> dialogInterface.dismiss()
                    })
                    .create()
                    .show()
            }
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