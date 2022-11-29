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


// Zoning algorithm
// section 값이 1일 때 -> 1층 부터 중간 층까지 이동하는 엘리베이터
// section 값이 2일 때 -> 중간 층+1층 부터 최상층까지 이동하는 엘리베이터
fun zoning(elevator: Elevator, section: Int) {
    var floorFloor: Int = 0
    var ceilingFloor: Int = 0

    if (section == 1) { // 하위 층 엘리베이터
        floorFloor = FLOOR_FLOOR
        ceilingFloor = CEILING_FLOOR / 2 as Int
    } else if (section == 2) { // 상위 층 엘리베이터
        floorFloor = CEILING_FLOOR / 2 as Int
        ceilingFloor = CEILING_FLOOR
    }

    var is_open = false
    //엘리베이터가 next_floor에 도착한 경우
    if (elevator.arrive_time <= now) {
        sum_distance++
        //도착한 승객들을 모두 내리고 성능평가지표를 수정한다.
        var i = 0
        while (i < elevator.up_user_list.size) {
            //승객이 내리고자 하는 층에 도착한 경우
            if (elevator.up_user_list[i].quit_floor == elevator.next_floor) {
                sum_passenger++
                // 환승을 했던 승객이라면 총 대기 시간에 10을 추가로 더해줌
                sum_waiting += if (elevator.up_user_list[i].isTransfer) {
                    (now - elevator.up_user_list[i].create_time) + 10
                } else {
                    now - elevator.up_user_list[i].create_time
                }
                if (longest_waiting < now - elevator.up_user_list[i].create_time)
                    longest_waiting = now - elevator.up_user_list[i].create_time
                elevator.up_user_list.removeAt(i)
                i--
                is_open = true
            }
            // 천장 층 도착 +
            // 올라가려는 승객이 25층 초과인 경우, 승객의 ride_floor를 25로 수정해서 다른 엘리베이터로 환승시킴
            if (elevator.next_floor == ceilingFloor) {
                if (elevator.up_user_list[i].quit_floor >= ceilingFloor) {
                    elevator.up_user_list[i].ride_floor = ceilingFloor;

                    var temp_create_time = elevator.up_user_list[i].create_time
                    var temp_ride_floor = elevator.up_user_list[i].ride_floor
                    var temp_quit_floor = elevator.up_user_list[i].quit_floor
                    var temp_userid = elevator.up_user_list[i].userid
                    elevator.up_user_list.removeAt(i)
                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                    temp_user.userid = temp_userid
                    temp_user.isTransfer = true
                    floor.user_list[temp_user.ride_floor].add(temp_user)
                    i--
                    is_open = true
                }
                elevator.direction = "DOWN" //천장층 도착 시, 강제로 엘리베이터의 방향 바꿈
            }
            i++
            if (i >= elevator.up_user_list.size)
                break;
        }

        i = 0
        while (i < elevator.down_user_list.size) {
            if (elevator.down_user_list[i].quit_floor == elevator.next_floor) {
                sum_passenger++
                // 환승을 했던 승객이라면 총 대기 시간에 10을 추가로 더해줌
                sum_waiting += if (elevator.down_user_list[i].isTransfer) {
                    (now - elevator.down_user_list[i].create_time) + 10
                } else {
                    now - elevator.down_user_list[i].create_time
                }
                if (longest_waiting < now - elevator.down_user_list[i].create_time)
                    longest_waiting = now - elevator.down_user_list[i].create_time
                elevator.down_user_list.removeAt(i)
                i--
                is_open = true
            }
            // 바닥층 도착 +
            // 내려가려는 승객이 25층 미만인 경우, ride_floor를 25로 수정해서 다른 엘리베이터로 환승시킴
            if (elevator.next_floor == floorFloor) {
                if (elevator.down_user_list[i].quit_floor <= floorFloor) {
                    elevator.down_user_list[i].ride_floor = floorFloor;

                    var temp_create_time = elevator.down_user_list[i].create_time
                    var temp_ride_floor = elevator.down_user_list[i].ride_floor
                    var temp_quit_floor = elevator.down_user_list[i].quit_floor
                    var temp_userid = elevator.down_user_list[i].userid
                    elevator.down_user_list.removeAt(i)
                    var temp_user = User(temp_create_time, temp_ride_floor, temp_quit_floor)
                    temp_user.userid = temp_userid
                    temp_user.isTransfer = true
                    floor.user_list[temp_user.ride_floor].add(temp_user)
                    i--
                    is_open = true
                }
                elevator.direction = "UP"
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
            if (flag == true) {
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
                for (i: Int in elevator.next_floor + 1..ceilingFloor)
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
                        //0층에서 next_floor-1층 사이에 요청이 있음
                        for (i: Int in floorFloor..elevator.next_floor - 1)
                            for (j in floor.user_list[i])
                                flag = true
                        //엘리베이터에 next_floor-1층 이하로 가는 승객이 있는 경우
                        for (i in elevator.down_user_list)
                            if (i.quit_floor < elevator.next_floor)
                                flag = true

                        //0층에서 next_floor-1층 사이에 요청이 있거나,
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
            if (flag == true) {
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
                //0층에서 next_floor-1 사이에 요청이 있음
                for (i: Int in 0..elevator.next_floor - 1)
                    for (j in floor.user_list[i])
                        flag = true
                //엘리베이터에 next_floor-1층 이하로 가는 승객이 있음
                for (i in elevator.down_user_list)
                    if (i.quit_floor < elevator.next_floor)
                        flag = true

                //0층에서 next_floor-1층 사이에 요청이 있거나,
                //엘리베이터에 next_floor-1층 이하로 가는 승객이 있는 경우
                if (flag) {
                    elevator.next_floor--
                    elevator.arrive_time += MOVETIME
                    if (is_open)
                        elevator.arrive_time += OPEN_TIME + CLOSE_TIME
                }
                //0층에서 next_floor-1층 사이에 요청이 없고,
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
                        for (i: Int in elevator.next_floor + 1..ceilingFloor)
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
            //가장 가까운 층에서의 요청을 찾는다
            var call_floor: MutableList<Int> = mutableListOf();


            for (i in floorFloor..ceilingFloor)
                if (floor.user_list[i].size != 0)
                    call_floor.add(i)


            var x = elevator.next_floor
            var y = elevator.next_floor
            var near_floor = -1
            while ((x >= floorFloor) || (y <= ceilingFloor)) {
                if (call_floor.contains(x)) {
                    near_floor = x
                    break
                }

                if (call_floor.contains(y)) {
                    near_floor = y
                    break
                }
                x--
                y++
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
            } else if (near_floor == elevator.next_floor) {
                var next_floor_list = floor.user_list[elevator.next_floor]
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


                if (elevator.down_user_list.size != 0) {
                    elevator.direction = "DOWN"
                    elevator.next_floor--
                    elevator.arrive_time += OPEN_TIME + MOVETIME + CLOSE_TIME
                } else if (elevator.down_user_list.size == 0) {
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