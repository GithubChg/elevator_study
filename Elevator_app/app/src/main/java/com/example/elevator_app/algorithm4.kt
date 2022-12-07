package com.example.elevator_app

import CEILING_FLOOR
import CLOSE_TIME
import Elevator
import FLOOR_FLOOR
import MAXIMUM_NUMBER
import MOVETIME
import OPEN_TIME
import User
import floor
import longest_waiting
import now
import sum_distance
import sum_passenger
import sum_waiting
import kotlin.system.exitProcess

//Collective control의 엘리베이터 동작함수+우선순위 반영
fun algorithm4(elevator: Elevator) {

    var is_open = false
    //엘리베이터가 next_floor에 도착한 경우
    if (elevator.arrive_time <= now) {
        sum_distance++
        //도착한 승객들을 모두 내리고 성능평가지표를 수정한다.
        var i = 0
        while (i < elevator.up_user_list.size) {
            if (elevator.up_user_list[i].quit_floor == elevator.next_floor) {
                sum_passenger++
                sum_waiting += now - elevator.up_user_list[i].create_time
                if (longest_waiting < now - elevator.up_user_list[i].create_time)
                    longest_waiting = now - elevator.up_user_list[i].create_time
                elevator.up_user_list.removeAt(i)
                i--
                is_open = true
            }
            i++
            if (i >= elevator.up_user_list.size)
                break;
        }
        i = 0
        while (i < elevator.down_user_list.size) {
            if (elevator.down_user_list[i].quit_floor == elevator.next_floor) {
                sum_passenger++
                sum_waiting += now - elevator.down_user_list[i].create_time
                if (longest_waiting < now - elevator.down_user_list[i].create_time)
                    longest_waiting = now - elevator.down_user_list[i].create_time
                elevator.down_user_list.removeAt(i)
                i--
                is_open = true
            }
            i++
            if (i >= elevator.down_user_list.size)
                break;
        }

        //엘리베이터가 위로 가는 중인 경우
        if (elevator.direction == "UP") {
            //next_floor에서 위로 가는 요청이 있는 경우, 정원 한도 내에서 태운다.
            var next_floor_list = floor.user_list[elevator.next_floor]
            i = 0
            var flag = false

            // 올라가는 승객인지 확인
            while (i < next_floor_list.size) {
                if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor)
                    flag = true
                i++
            }

            //next_floor에서 위로 가는 요청이 있는 경우
            if (flag) {
                i = 0
                var num = elevator.up_user_list.size + elevator.down_user_list.size
                //정원초과 되기 전까지 next_floor에서 승객을 제거하여 up_user_list로 삽입한다.
                while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                    if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor) {
                        var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                        var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                        var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                        var temp_userid = floor.user_list[elevator.next_floor][i].userid
                        floor.user_list[elevator.next_floor].removeAt(i)
                        var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                        temp_user.userid = temp_userid
                        elevator.up_user_list.add(temp_user)
                    }
                    num = elevator.up_user_list.size + elevator.down_user_list.size
                    i++
                }
                elevator.next_floor++
                elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
            }
            //next_floor에서 위로 가는 요청이 없는 경우
            else {
                flag = false
                //next_floor+1층에서 ceiling_floor 사이에 요청이 있음
                for (i: Int in elevator.next_floor + 1..CEILING_FLOOR)
                    for (j in floor.user_list[i])
                        flag = true
                //엘리베이터에 next_floor+1층 이상으로 가는 승객이 있음
                for (i in elevator.up_user_list)
                    if (i.quit_floor > elevator.next_floor)
                        flag = true

                //next_floor+1층에서 ceiling_floor 사이에 요청이 있거나,
                //엘리베이터에 next_floor+1층 이상으로 가는 승객이 있는 경우
                if (flag) {
                    elevator.next_floor++
                    elevator.arrive_time += MOVETIME
                    if (is_open)
                        elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                }
                //next_floor+1층에서 ceiling_floor 사이에 요청이 없고,
                //엘리베이터에 next_floor+1층 이상으로 가는 승객이 없는 경우 -> 방향 바꿈
                else {
                    flag = false
                    for (i in floor.user_list[elevator.next_floor])
                        if (i.ride_floor > i.quit_floor)
                            flag = true

                    //next_floor에 아래로 가는 요청이 있는 경우, 승객을 엘리베이터에 태움
                    if (flag) {
                        elevator.direction = "DOWN"

                        i = 0
                        var num = elevator.up_user_list.size + elevator.down_user_list.size
                        while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                            if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor) {
                                var temp_create_time =
                                    floor.user_list[elevator.next_floor][i].create_time
                                var temp_ride_floor =
                                    floor.user_list[elevator.next_floor][i].ride_floor
                                var temp_quit_floor =
                                    floor.user_list[elevator.next_floor][i].quit_floor
                                var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                floor.user_list[elevator.next_floor].removeAt(i)
                                var temp_user =
                                    User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                temp_user.userid = temp_userid
                                elevator.down_user_list.add(temp_user)
                            }
                            num = elevator.up_user_list.size + elevator.down_user_list.size
                            i++
                        }
                        elevator.next_floor--
                        elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                    }
                    // next_floor에서 아래로 가는 요청이 없는 경우, 그 다음 층에 요청이 있는지 확인
                    else {
                        flag = false
                        //1층에서 next_floor-1층 사이에 요청이 있음
                        for (i: Int in FLOOR_FLOOR until elevator.next_floor)
                            for (j in floor.user_list[i])
                                flag = true
                        //엘리베이터에 next_floor-1층 이하로 가는 승객이 있는 경우
                        for (i in elevator.down_user_list)
                            if (i.quit_floor < elevator.next_floor)
                                flag = true

                        //1층에서 next_floor-1층 사이에 요청이 있거나,
                        //엘리베이터에 next_floor-1 층 이하로 가는 승객이 있는 경우
                        if (flag) {
                            elevator.direction = "DOWN"
                            elevator.next_floor--
                            elevator.arrive_time += MOVETIME
                            if (is_open)
                                elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                        }
                        //0층에서 next_floor-1층 사이에 요청이 없고,
                        //엘리베이터에 next_floor-1 층 이하로 가는 승객이 없는 경우
                        else {
                            //if((elevator.up_user_list.size==0)&&(elevator.down_user_list.size==0)&&())

                            elevator.direction = "STOP"
                        }
                    }
                }
            }
        }
        //엘리베이터가 아래로 가는 중인 경우
        else if (elevator.direction == "DOWN") {
            //next_floor에서 아래로 가는 요청이 있는 경우, 정원 한도 내에서 태운다.
            var next_floor_list = floor.user_list[elevator.next_floor]
            i = 0
            var flag = false

            while (i < next_floor_list.size) {
                if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor)
                    flag = true
                i++
            }

            //next_floor에서 아래로 가는 요청이 있는 경우
            if (flag) {
                i = 0
                var num = elevator.up_user_list.size + elevator.down_user_list.size
                //정원초과 되기 전까지 next_floor에서 승객을 제거하여 down_user_list로 삽입한다.
                while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                    if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor) {
                        var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                        var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                        var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                        var temp_userid = floor.user_list[elevator.next_floor][i].userid
                        floor.user_list[elevator.next_floor].removeAt(i)
                        var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                        temp_user.userid = temp_userid
                        elevator.down_user_list.add(temp_user)
                    }
                    num = elevator.up_user_list.size + elevator.down_user_list.size
                    i++
                }
                elevator.next_floor--
                elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
            }
            //next_floor에서 아래로 가는 요청이 없는 경우
            else {
                flag = false
                //1층에서 next_floor-1 사이에 요청이 있음
                for (i: Int in FLOOR_FLOOR until elevator.next_floor)
                    for (j in floor.user_list[i])
                        flag = true
                //엘리베이터에 next_floor-1층 이하로 가는 승객이 있음
                for (i in elevator.down_user_list)
                    if (i.quit_floor < elevator.next_floor)
                        flag = true

                //1층에서 next_floor-1층 사이에 요청이 있거나,
                //엘리베이터에 next_floor-1층 이하로 가는 승객이 있는 경우
                if (flag) {
                    elevator.next_floor--
                    elevator.arrive_time += MOVETIME
                    if (is_open)
                        elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                }
                //1층에서 next_floor-1층 사이에 요청이 없고,
                //엘리베이터에 next_floor-1층 이하로 가는 승객이 없는 경우
                else {
                    flag = false
                    for (i in floor.user_list[elevator.next_floor])
                        if (i.ride_floor < i.quit_floor)
                            flag = true

                    //next_floor에 위로 가는 요청이 있는 경우
                    if (flag) {
                        elevator.direction = "UP"

                        i = 0
                        var num = elevator.up_user_list.size + elevator.down_user_list.size
                        while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                            if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor) {
                                var temp_create_time =
                                    floor.user_list[elevator.next_floor][i].create_time
                                var temp_ride_floor =
                                    floor.user_list[elevator.next_floor][i].ride_floor
                                var temp_quit_floor =
                                    floor.user_list[elevator.next_floor][i].quit_floor
                                var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                floor.user_list[elevator.next_floor].removeAt(i)
                                var temp_user =
                                    User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                temp_user.userid = temp_userid
                                elevator.up_user_list.add(temp_user)
                            }
                            num = elevator.up_user_list.size + elevator.down_user_list.size
                            i++
                        }
                        elevator.next_floor++
                        elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                    }
                    // next_floor에서 위로 가는 요청이 없는 경우
                    else {
                        flag = false
                        //next_floor+1층에서 ceiling층 사이에 요청이 있음
                        for (i: Int in elevator.next_floor + 1..CEILING_FLOOR)
                            for (j in floor.user_list[i])
                                flag = true
                        //엘리베이터에 next_floor+1층 이상으로 가는 승객이 있는 경우
                        for (i in elevator.up_user_list)
                            if (i.quit_floor > elevator.next_floor)
                                flag = true

                        //next_floor+1층에서 ceiling층 사이에 요청이 있거나,
                        //엘리베이터에 next_floor+1층 이상으로 가는 승객이 있는 경우
                        if (flag) {
                            elevator.direction = "UP"
                            elevator.next_floor++
                            elevator.arrive_time += MOVETIME
                            if (is_open)
                                elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                        }
                        //next_floor+1층에서 ceiling층 사이에 요청이 있거나,
                        //엘리베이터에 next_floor+1층 이상으로 가는 승객이 있는 경우
                        else {
                            elevator.direction = "STOP"
                        }
                    }
                }
            }
        }
        //엘리베이터가 정지한 경우
        else if (elevator.direction == "STOP") {
            //우선순위를 계산한다.-List의 size가 0인 경우 승객이 없다는 의미
            var priorityList=calculpriority1(floor.user_list)
            print("\npriorityList : "+priorityList+"\n")
            var priorityfloor=0 //우선순위로 정해진 층=현재 가려는 층, 1~50 : 위로가는 1~50층, 51~100 : 아래로 가는 1~50층

            var near_floor=-1

            if(elevator.next_floor in priorityList){
                near_floor=elevator.next_floor
                priorityfloor=elevator.next_floor
            }
            else if(elevator.next_floor+50 in priorityList){
                near_floor=elevator.next_floor-50
                priorityfloor=elevator.next_floor+50
            }
            else{
                var temp_dif=50 //최대 차이가 나는 경우+1값
                //우선순위 층과 현재 층의 차이를 계산하여 최소가 되는 층을 찾기 만일, 동일한 층이 두개라면 낮은층, 올라가는 방향 우선으로
                for(i in priorityList){
                    if(i<51){
                        var temp=if(i<elevator.next_floor) elevator.next_floor-i else i-elevator.next_floor
                        if(temp<temp_dif){
                            temp_dif=temp
                            near_floor=i
                            priorityfloor=i
                        }
                    }
                    else{
                        var temp=if((i-50)<elevator.next_floor) elevator.next_floor-(i-50) else (i-50)-elevator.next_floor
                        if(temp<temp_dif){
                            temp_dif=temp
                            near_floor=i-50
                            priorityfloor=i
                        }
                    }

                }
            }



            //가까운 승객이 현재 엘리베이터가 있는 곳보다 높은 층인 경우
            if ((near_floor != -1) && (near_floor > elevator.next_floor)) {
                elevator.direction = "UP"
                elevator.next_floor++
                elevator.arrive_time += MOVETIME
                if (is_open)
                    elevator.arrive_time += OPEN_TIME + CLOSE_TIME
            }
            //가까운 승객이 현재 엘리베이터가 있는 곳보다 낮은 층인 경우
            else if ((near_floor != -1) && (near_floor < elevator.next_floor)) {
                elevator.direction = "DOWN"
                elevator.next_floor--
                elevator.arrive_time += MOVETIME
                if (is_open)
                    elevator.arrive_time += OPEN_TIME + CLOSE_TIME
            }
            // 가까운 승객이 엘리베이터가 서 있는 층에 존재하는 경우
            else if (near_floor == elevator.next_floor) {
                var next_floor_list = floor.user_list[elevator.next_floor]
                i = 0
                var num = elevator.up_user_list.size + elevator.down_user_list.size
                //정원초과 되기 전까지 next_floor에서 승객을 제거하여 down_user_list로 삽입한다.
                while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                    if(priorityfloor>50) {// 내려가는 사람 먼저 챙긴다
                        if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor) {
                            var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                            var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                            var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                            var temp_userid = floor.user_list[elevator.next_floor][i].userid
                            floor.user_list[elevator.next_floor].removeAt(i)
                            var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                            temp_user.userid = temp_userid
                            elevator.down_user_list.add(temp_user)
                        }
                        num = elevator.up_user_list.size + elevator.down_user_list.size
                        i++
                    }
                }

                // 엘리베이터 내부에 내려가는 승객의 수가 있는지 확인
                if (elevator.down_user_list.size != 0) {
                    elevator.direction = "DOWN"
                    elevator.next_floor--
                    elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                }
                // 내려가려는 승객이 없으면 해당 층에서 올라가려는 승객을 태움
                else if (elevator.down_user_list.size == 0) {
                    i = 0
                    var num = elevator.up_user_list.size + elevator.down_user_list.size
                    //정원초과 되기 전까지 next_floor에서 승객을 제거하여 up_user_list로 삽입한다.
                    while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                        if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor) {
                            var temp_create_time =
                                floor.user_list[elevator.next_floor][i].create_time
                            var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                            var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                            var temp_userid = floor.user_list[elevator.next_floor][i].userid
                            floor.user_list[elevator.next_floor].removeAt(i)
                            var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                            temp_user.userid = temp_userid
                            elevator.up_user_list.add(temp_user)
                        }
                        num = elevator.up_user_list.size + elevator.down_user_list.size
                        i++
                    }

                    elevator.direction = "UP"
                    elevator.next_floor++
                    elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME

                } else {
                    println("stop error!")
                }
            }
        } else {
            println("direction error!")
        }
    }
}

/*
우선순위를 결정하는 함수 : 대기하고 있는 승객 수/승객이 이동하는데 걸리는 시간의 합
50개의 층, 위 아래의 두가지 방향이므로 총 100가지의 경우가 존재함
해당 함수는 시작할 때와 엘레베이터가 최종 목적지에 도착하면 실행
입력 : 승객의 리스트
출력 : 우선순위가 높은 층의 리스트
 */
fun calculpriority1(userlist: MutableList<MutableList<User>>):ArrayList<Int>{
    var priorityfloor= ArrayList<Int>()//우선순위가 가장 높은 층의 리스트
    var temp=0

    //우선순위를 결정하는 크기가 102인 배열, 위로 가는 기준으로 1~50층, 아래로 가는 기준으로 1~50층 순서
    //1~50 : 1~50층 상향, 51~100 : 1~50층 하향
    var arr=Array(102,{0.0})
    for(i in userlist){
        var temp1=0.0//해당 층의 올라가는 승객의 이동시간의 합
        var temp2=0.0//해당 층의 내려가는 승객의 이동시간의 합
        var count1=0.0//해당 층에서 올라가는 승객의 수
        var count2=0.0//해당 층에서 내려가는 승객의 수
        for(j in i){
            if(j.ride_floor<j.quit_floor){//타는층<내리는층이므로 올라가는 승객
                temp1+=(j.quit_floor-j.ride_floor)
                count1+=1
            }
            else{//타는층>내리는층이므로 내려가는 승객
                temp2+=(j.ride_floor-j.quit_floor)
                count2+=1
            }
        }
        //계산된 우선순위값 배열에 저장하기
        if(count1==0.0){
            arr[temp]= 0.0
        } else{
            arr[temp]= count1/temp1
        }
        if(count2==0.0){
            arr[temp+50]= 0.0
        }else{
            arr[temp+50]= count2/temp2
        }
        temp+=1
    }

    //우선순위 배열에서 가장 큰 값을 구한 후, 해당하는 층을 반환 리스트에 삽입
    val max= arr.indices.map { i:Int->arr[i] }.maxOrNull() //우선순위 배열에서 가장 큰 값
    for (i in 0..99){
        if(arr[i]==max){
            priorityfloor.add(i)
        }
    }
    //print("\n*priorityfloor"+priorityfloor)
    return priorityfloor
}