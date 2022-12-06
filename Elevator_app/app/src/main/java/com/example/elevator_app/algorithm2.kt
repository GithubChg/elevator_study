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

//개발한 알고리즘의 경우
fun algorithm2(another_priority:Int,elevator: Elevator) {

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

        //만약 엘리베이터에 올라가는 승객이 없고, 내려가는 승객이 없고, 최종 목적지에 도달했다면 방향을 STOP으로 바꾼다.
        if(elevator.up_user_list.size==0 && elevator.down_user_list.size==0 && elevator.next_floor==elevator.final_floor)
            elevator.direction="STOP"


        //엘리베이터가 위로 가는 중인 경우
        if (elevator.direction == "UP") {
            if(elevator.up_user_list.size==0){ //올라가는 승객이 없는경우
                if (elevator.next_floor==elevator.priority_floor){//계산한 우선순위 층에 도착한 경우->결과(priority_floor와 final_floor비교)에 따라 올라가는 승객만 or 내려가는 승객만 태워야 함
                    if(elevator.priority_floor<elevator.final_floor){//우선순위층<최종목적지층 이므로 올라가는 승객만 태워야 함
                        var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                        for(j in floor.user_list[elevator.next_floor]){
                            next_floor_list.add(j)
                        }
                        var flag = false

                        //next_floor에서 올라가는 승객이 있다면 flag를 true로 바꿈
                        for (i in next_floor_list){
                            if(i.quit_floor>i.ride_floor){
                                flag=true
                            }
                        }

                        var temp_list=ArrayList<Int>() //엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용

                        //next_floor에서 올라가는 승객이 있는 경우
                        if (flag) {
                            i = 0
                            var num = elevator.up_user_list.size + elevator.down_user_list.size
                            //정원초과 되기 전까지 next_floor에서 up_user_list로 삽입한다.
                            while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                                if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor && next_floor_list[i].quit_floor<=elevator.final_floor) {
                                    var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                    var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                    var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                    var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                    //floor.user_list[elevator.next_floor].removeAt(i)
                                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                    temp_user.userid = temp_userid
                                    elevator.up_user_list.add(temp_user)
                                    temp_list.add(i)
                                }
                                num = elevator.up_user_list.size + elevator.down_user_list.size
                                i++
                            }
                            //엘리베이터에 탑승한 승객(elevator.up_user_list)을 floor.user_list에서 삭제
                            var k=0
                            for(j in temp_list){
                                floor.user_list[elevator.next_floor].removeAt(j-k) //삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                                k++
                            }
                            elevator.next_floor++
                            elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                        }
                        else{//next_floor에서 올라가는 승객이 없다면 방향을 STOP으로 변경
                            elevator.direction="STOP"
                        }
                    }
                    else{//우선순위층>최종목적지층 이므로 내려가는 승객만 태워야 함
                        var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                        for(j in floor.user_list[elevator.next_floor]){
                            next_floor_list.add(j)
                        }

                        var flag = false

                        //next_floor에서 내려가는 승객이 있다면 flag를 true로 바꿈
                        for (i in next_floor_list){
                            if(i.quit_floor<i.ride_floor){
                                flag=true
                            }
                        }

                        var temp_list=ArrayList<Int>()//엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용

                        //next_floor에서 내려가는 승객이 있는 경우
                        if (flag) {
                            i = 0
                            var num = elevator.up_user_list.size + elevator.down_user_list.size
                            //정원초과 되기 전까지 next_floor에서 down_user_list로 삽입한다.
                            while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                                if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor && next_floor_list[i].quit_floor>=elevator.final_floor) {
                                    var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                    var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                    var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                    var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                    //floor.user_list[elevator.next_floor].removeAt(i)
                                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                    temp_user.userid = temp_userid
                                    elevator.down_user_list.add(temp_user)
                                    temp_list.add(i)
                                }
                                num = elevator.up_user_list.size + elevator.down_user_list.size
                                i++
                            }
                            //엘리베이터에 탑승한 승객(elevator.down_user_list)을 floor.user_list에서 삭제
                            var k=0
                            for(j in temp_list){
                                floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                                k++
                            }
                            elevator.next_floor--
                            elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                            elevator.direction="DOWN" //올라가는 승객이 없고 내려가야 하는 승객이 있으므로 방향을 바꿈
                        }
                        else{//next_floor에서 내려가는 승객이 없다면 방향을 STOP으로 변경
                            elevator.direction="STOP"
                        }

                    }

                }
                else{//엘리베이터에 탑승한 승객이 없고, 우선순위 층에 도달 안했으면 승객을 태우지 않고 그냥 올라감
                    elevator.next_floor++
                    elevator.arrive_time += MOVETIME
                    if (is_open)
                        elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                }
            }
            else if(elevator.down_user_list.size!=0){//방향이 UP이고, 올라가는 승객이 있고, 내려가는 승객이 있는 경우 error-엘리베이터에는 같은 방향의 승객만 있어야 하므로 error
                print("\nERROR\n")
            }
            else{//엘리베이터에 올라가는 승객이 있는 경우
                var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                var temp_list=ArrayList<Int>()//엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용
                for(j in floor.user_list[elevator.next_floor]){
                    next_floor_list.add(j)
                }

                var flag = false

                //next_floor에서 탑승할 수 있는 승객이 있다면 flag를 true로 바꿈
                //이때, 탑승할 수 있는 승객=올라가야함(UP방향이고, 이미 올라가는 승객이 탑승한 상태이므로) && 엘리베이터의 최종 목적지보다 같거나 낮은 층으로 가야함
                if(elevator.priority_floor<elevator.final_floor){
                    for (i in next_floor_list){
                        if(i.quit_floor>i.ride_floor && i.quit_floor<=elevator.final_floor)
                            flag=true
                    }

                    //탑승할 수 있는 승객이 있는 경우
                    if(flag){
                        i = 0
                        var num = elevator.up_user_list.size + elevator.down_user_list.size
                        //정원초과 되기 전까지 next_floor에서 탑승할 수 있는 승객만 up_user_list로 삽입한다.
                        while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                            if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor && next_floor_list[i].quit_floor<=elevator.final_floor) {
                                var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                //floor.user_list[elevator.next_floor].removeAt(i)
                                var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                temp_user.userid = temp_userid
                                elevator.up_user_list.add(temp_user)
                                temp_list.add(i)
                            }
                            num = elevator.up_user_list.size + elevator.down_user_list.size
                            i++
                        }
                        //엘리베이터에 탑승한 승객(elevator.up_user_list)을 floor.user_list에서 삭제
                        var k=0
                        for(j in temp_list){
                            floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                            k++
                        }
                        elevator.next_floor++
                        elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                    }
                    else{//탑승할 수 있는 승객이 없다면 승객을 태우지 않고 그냥 올라감
                        elevator.next_floor++
                        elevator.arrive_time += MOVETIME
                        if (is_open)
                            elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                    }
                }
                else{
                    print("\nUP ERROR\n")
                }

            }
        }
        //엘리베이터가 아래로 가는 중인 경우
        else if (elevator.direction == "DOWN") {
            if(elevator.down_user_list.size==0){//내려가는 승객이 없는 경우
                if (elevator.next_floor==elevator.priority_floor){//계산한 우선순위 층에 도착한 경우->결과(priority_floor와 final_floor비교)에 따라 올라가는 승객만 or 내려가는 승객만 태워야 함
                    if(elevator.priority_floor<elevator.final_floor){//우선순위층<최종목적지층 이므로 올라가는 승객만 태워야 함
                        var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                        for(j in floor.user_list[elevator.next_floor]){
                            next_floor_list.add(j)
                        }
                        var flag = false

                        //next_floor에서 올라가는 승객이 있다면 flag를 true로 바꿈
                        for (i in next_floor_list){
                            if(i.quit_floor>i.ride_floor){
                                flag=true
                            }
                        }

                        var temp_list=ArrayList<Int>()//엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용

                        //next_floor에서 올라가는 승객이 있는 경우
                        if (flag) {
                            i = 0
                            var num = elevator.up_user_list.size + elevator.down_user_list.size
                            //정원초과 되기 전까지 next_floor에서 up_user_list로 삽입한다.
                            while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                                if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor && next_floor_list[i].quit_floor<=elevator.final_floor) {
                                    var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                    var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                    var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                    var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                    //floor.user_list[elevator.next_floor].removeAt(i)
                                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                    temp_user.userid = temp_userid
                                    elevator.up_user_list.add(temp_user)
                                    temp_list.add(i)
                                }
                                num = elevator.up_user_list.size + elevator.down_user_list.size
                                i++
                            }
                            //엘리베이터에 탑승한 승객(elevator.up_user_list)을 floor.user_list에서 삭제
                            var k=0
                            for(j in temp_list){
                                floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                                k++
                            }
                            elevator.next_floor++
                            elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                            elevator.direction="UP"//내려가는 승객이 없고 올라가야 하는 승객이 있으므로 방향을 바꿈
                        }
                        else{//next_floor에서 올라가는 승객이 없다면 방향을 STOP으로 변경
                            elevator.direction="STOP"
                        }
                    }
                    else{//우선순위층>최종목적지층 이므로 내려가는 승객만 태워야 함
                        var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                        for(j in floor.user_list[elevator.next_floor]){
                            next_floor_list.add(j)
                        }
                        var flag = false

                        //next_floor에서 내려가는 승객이 있다면 flag를 true로 바꿈
                        for (i in next_floor_list){
                            if(i.quit_floor<i.ride_floor){
                                flag=true
                            }
                        }

                        var temp_list=ArrayList<Int>()//엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용

                        //next_floor에서 내려가는 승객이 있는 경우
                        if (flag) {
                            i = 0
                            var num = elevator.up_user_list.size + elevator.down_user_list.size
                            //정원초과 되기 전까지 next_floor에서 down_user_list로 삽입한다.
                            while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                                if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor && next_floor_list[i].quit_floor>=elevator.final_floor) {
                                    var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                    var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                    var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                    var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                    //floor.user_list[elevator.next_floor].removeAt(i)
                                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                    temp_user.userid = temp_userid
                                    elevator.down_user_list.add(temp_user)
                                    temp_list.add(i)
                                }
                                num = elevator.up_user_list.size + elevator.down_user_list.size
                                i++
                            }
                            //엘리베이터에 탑승한 승객(elevator.down_user_list)을 floor.user_list에서 삭제
                            var k=0
                            for(j in temp_list){
                                floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                                k++
                            }
                            elevator.next_floor--
                            elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                            elevator.direction="DOWN" //방향 유지
                        }
                        else{//next_floor에서 내려가는 승객이 없다면 방향을 STOP으로 변경
                            elevator.direction="STOP"
                        }

                    }

                }
                else{//엘리베이터에 탑승한 승객이 없고, 우선순위 층에 도달 안했으면 승객을 태우지 않고 그냥 올라감
                    elevator.next_floor--
                    elevator.arrive_time += MOVETIME
                    if (is_open)
                        elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                }
            }
            else if(elevator.up_user_list.size!=0){//방향이 DOWN이고, 올라가는 승객이 있고, 내려가는 승객이 있는 경우 error-엘리베이터에는 같은 방향의 승객만 있어야 하므로 error
                print("\nERROR\n")
            }
            else{//엘리베이터에 내려가는 승객이 있는 경우
                var next_floor_list =ArrayList<User>()//next_floor에 있는 승객을 저장할 리스트
                var temp_list=ArrayList<Int>()//엘리베이터에 탑승한 승객의 인덱스 저장 번호->floor.user_list를 삭제하는데 사용
                for(j in floor.user_list[elevator.next_floor]){
                    next_floor_list.add(j)
                }

                var flag = false

                //next_floor에서 탑승할 수 있는 승객이 있다면 flag를 true로 바꿈
                //이때, 탑승할 수 있는 승객=내려가야함(DOWN방향이고, 이미 내려가는 승객이 탑승한 상태이므로) && 엘리베이터의 최종 목적지보다 같거나 높은 층으로 가야함
                if(elevator.priority_floor>elevator.final_floor){
                    for (i in next_floor_list){
                        if(i.quit_floor<i.ride_floor && i.quit_floor>=elevator.final_floor)
                            flag=true
                    }

                    //탑승할 수 있는 승객이 있는 경우
                    if(flag){
                        i = 0
                        var num = elevator.up_user_list.size + elevator.down_user_list.size
                        //정원초과 되기 전까지 next_floor에서 탑승할 수 있는 승객만 down_user_list에 삽입한다.
                        while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                            if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor && next_floor_list[i].quit_floor<=elevator.final_floor) {
                                var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                                var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                                var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                                var temp_userid = floor.user_list[elevator.next_floor][i].userid
                                //floor.user_list[elevator.next_floor].removeAt(i)
                                var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                                temp_user.userid = temp_userid
                                elevator.down_user_list.add(temp_user)
                                temp_list.add(i)
                            }
                            num = elevator.up_user_list.size + elevator.down_user_list.size
                            i++
                        }
                        //엘리베이터에 탑승한 승객(elevator.down_user_list)을 floor.user_list에서 삭제
                        var k=0
                        for(j in temp_list){
                            floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                            k++
                        }
                        elevator.next_floor--
                        elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                    }
                    else{//탑승할 수 있는 승객이 없다면 승객을 태우지 않고 그냥 올라감
                        elevator.next_floor--
                        elevator.arrive_time += MOVETIME
                        if (is_open)
                            elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                    }
                }
                else{
                    print("\nDOWN ERROR\n")
                }

            }

        }
        //엘리베이터가 정지한 경우
        else if (elevator.direction == "STOP") {

            var priorityList= calculpriority(floor.user_list)
            var priorityfloor=0 //우선순위로 정해진 층=현재 가려는 층, 1~50 : 위로가는 1~50층, 51~100 : 아래로 가는 1~50층

            //다른 엘리베이터의 우선순위와 겹치지 않으려면 다른 엘리베이터의 우선순위를 제거함
            //이때, 제거한 후에 우선순위 리스트가 0이면 다시 계산
            if(another_priority!=0){
                priorityList.remove(another_priority)
            }
            if(priorityList.size==0){
                priorityList= calculpriority2(floor.user_list)
                if(priorityList.size==0){//엘리베이터 우선순위를 다시 계산하였을 때 0이면 승객이 아예 없는것임
                    return
                }
            }

            var near_floor=-1

            if(elevator.next_floor in priorityList){
                near_floor=elevator.next_floor
                priorityfloor=elevator.next_floor
                elevator.priority_direction=elevator.next_floor
            }
            else if(elevator.next_floor+50 in priorityList){
                near_floor=elevator.next_floor
                priorityfloor=elevator.next_floor+50
                elevator.priority_direction=elevator.next_floor+50
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

            elevator.priority_floor=near_floor//우선순위층
            elevator.priority_direction=priorityfloor//우선순위층 + 방향 (올라가면 그대로, 내려가면 +50)

            //엘리베이터의 최종 목적지를 설정
            if(priorityfloor>50){
                var temp=50
                for(i in floor.user_list[near_floor]){
                    if(i.quit_floor<temp)
                        temp=i.quit_floor
                }
                elevator.final_floor=temp
            }
            else{
                var temp=1
                for(i in floor.user_list[near_floor]){
                    if(i.quit_floor>temp)
                        temp=i.quit_floor
                }
                elevator.final_floor=temp
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
                var next_floor_list =ArrayList<User>()
                var temp_list=ArrayList<Int>()
                for(j in floor.user_list[elevator.next_floor]){
                    next_floor_list.add(j)
                }
                i = 0
                var num = elevator.up_user_list.size + elevator.down_user_list.size

                //정원초과 되기 전까지 next_floor에서 down_user_list로 삽입한다.
                if(priorityfloor>50){//50보다 크면 내려가는 방향이 우선
                    while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                        if (next_floor_list[i].quit_floor < next_floor_list[i].ride_floor) {
                            var temp_create_time = floor.user_list[elevator.next_floor][i].create_time
                            var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                            var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                            var temp_userid = floor.user_list[elevator.next_floor][i].userid
                            //floor.user_list[elevator.next_floor].removeAt(i)
                            var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                            temp_user.userid = temp_userid
                            elevator.down_user_list.add(temp_user)
                            temp_list.add(i)
                        }
                        num = elevator.up_user_list.size + elevator.down_user_list.size
                        i++
                    }
                    //엘리베이터에 탑승한 승객(elevator.up_user_list)을 floor.user_list에서 삭제
                    var k=0
                    for(j in temp_list){
                        floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                        k++
                    }
                }
                else{//50보다 작으므로 올라가는 방향이 우선
                    while ((i < next_floor_list.size) && (num < MAXIMUM_NUMBER)) {
                        if (next_floor_list[i].quit_floor > next_floor_list[i].ride_floor) {
                            var temp_create_time =floor.user_list[elevator.next_floor][i].create_time
                            var temp_ride_floor = floor.user_list[elevator.next_floor][i].ride_floor
                            var temp_quit_floor = floor.user_list[elevator.next_floor][i].quit_floor
                            var temp_userid = floor.user_list[elevator.next_floor][i].userid
                            //floor.user_list[elevator.next_floor].removeAt(i)
                            var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                            temp_user.userid = temp_userid
                            elevator.up_user_list.add(temp_user)
                            temp_list.add(i)
                        }
                        num = elevator.up_user_list.size + elevator.down_user_list.size
                        i++
                    }
                    //엘리베이터에 탑승한 승객(elevator.up_user_list)을 floor.user_list에서 삭제
                    var k=0
                    for(j in temp_list){
                        floor.user_list[elevator.next_floor].removeAt(j-k)//삭제할수록 인덱스 번호가 줄어드므로 k를 활용
                        k++
                    }
                }

                // 엘리베이터 내부에 내려가는 승객의 수가 있는지 확인
                if (elevator.down_user_list.size != 0) {
                    elevator.direction = "DOWN"
                    elevator.next_floor--
                    elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                }
                // 엘리베이터 내부에 올라가는 승객의 수가 있는지 확인
                else if (elevator.up_user_list.size != 0) {
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
fun calculpriority(userlist: MutableList<MutableList<User>>):ArrayList<Int>{
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
    return priorityfloor
}

//두번째 우선순위를 계산하는 함수
fun calculpriority2(userlist: MutableList<MutableList<User>>):ArrayList<Int>{
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
    var max= arr.indices.map { i:Int->arr[i] }.maxOrNull() //우선순위 배열에서 가장 큰 값
    for (i in 0..100){
        if(arr[i]==max){
            arr[i]=0.0
        }
    }
    max=arr.indices.map { i:Int->arr[i] }.maxOrNull()
    for (i in 0..100){
        if(arr[i]==max){
            priorityfloor.add(i)
        }
    }

    return priorityfloor
}