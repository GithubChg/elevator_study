import com.example.elevator_app.CCoperation
import com.example.elevator_app.algorithm2
import com.example.elevator_app.algorithm4
import com.example.elevator_app.zoning
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.system.exitProcess

//사용자 클래스
class User(var create_time: Int, var ride_floor: Int, var quit_floor: Int) {
    //var create_time : Int=-1 // 승객 생성시간
    //var ride_floor : Int=-1 // 승객 출발층
    //var quit_floor : Int=-1 // 승객 도착층
    var userid: String = "default" // 실제 사용자가 사용하는 userid
    var target_elevator: Int = -1 //어느 엘리베이터에 탑승하는지
    var depart_time: Int = -1 //승객 탑승시간 (예상)
    var arrive_time: Int = -1 // 승객 도착층 도착시간 (예상)
    var isTransfer: Boolean = false // Zoning 알고리즘에서 승객이 환승을 했는지 여부를 알려주는 변수
}

//층 클래스
// floor 층부터 ceiling층까지 모든 층에 대기 중인 승객의 리스트가 존재한다
class Floor(var floor: Int, var ceiling: Int) {
    var user_list = mutableListOf<MutableList<User>>()

    init {
        //floor층부터 ceiling층까지 list 추가
        for (i: Int in floor..ceiling + 1) {    // 인덱싱 쉽게하기 위해 51개의 list 만듦. floor.user_list[0]은 비어있음
            var floor_list = mutableListOf<User>() //한 층에 들어갈 승객의 list
            user_list.add(floor_list)
        }
    }
}

// 엘리베이터 클래스
// collective control이 아닌 알고리즘에 적용할 때는 다른 엘리베이터 클래스를 만들거나
// 기존 엘리베이터 클래스를 상속 받아야할 듯 (추가되어야하는 속성이 있음)
class Elevator() {
    var up_user_list = mutableListOf<User>() //위로 이동하고자 하는 탑승객
    var down_user_list = mutableListOf<User>() //아래로 이동하고자 하는 탑승객
    var direction = "STOP" //엘리베이터의 현재 이동방향, "STOP", "UP", "DOWN" 가능
    var next_floor = 1 //엘리베이터가 지나갈 층, collective control에서는 현재층+1 또는 현재층-1
    var arrive_time = -1 //엘리베이터가 next_floor에 도착하는 시간

    var priority_floor = 0 //우선순위 층
    var final_floor = 0 //최종 목적지
    var priority_direction = 0//우선순위 층+방향(올라가면 우선순위 층 그대로, 내려가면 우선순위 층 +50)
}

// 상수
//ALGORITHM 변수 후보: COLLECTIVE_CONTROL, ALGORITHM2, ZONING, ALGORITHM4
var ALGORITHM = "COLLECTIVE_CONTROL" //사용하는 알고리즘
var IS_SIMULATION = false //시뮬레이션 중인지, 실제 사용자가 사용하는 경우인지 구분
val SIMULATION_DURATION = 604800 //시뮬레이션 지속 시간 604800초는 1주일 debug code
val MAXIMUM_NUMBER = 24 // 엘리베이터 정원
val OPEN_TIME = 4 // 엘리베이터의 문이 열리는 시간
val CLOSE_TIME = 4  // 엘리베이터의 문이 닫히는 시간
val MOVETIME = 1 // 1층을 이동하는 시간
var CONGESTION = 4 // 30초마다 생성되는 승객의 수 (자연수)
val FLOOR_FLOOR = 1 //제일 낮은 층 : 1층
val CEILING_FLOOR = 50 //제일 높은 층 : 50층


//변수
var now: Long = 0
var sum_passenger: Long = 0 // 모든 승객 수의 합
var sum_waiting: Long = 0 // 모든 승객들이 기다린 시간의 합
var sum_distance: Long = 0 // 엘리베이터의 총 이동 거리
var average_waiting: Long = 0 // 모든 승객들이 기다린 시간의 평균
var longest_waiting: Long = 0 // 가장 오래 기다린 승객이 기다린 시간
var average_distance: Long = 0 // 엘리베이터의 총 이동 거리/옮긴 승객
var estimatedTimeOfArrival: Int = 0 //엘리베이터 도착 예정 시간
var recommededElevator: String = "" // 도착 예정 시간을 기반으로 한 엘리베이터 추천

//Elevator 2개, Floor 1개 생성
var elevator1 = Elevator()
var elevator2 = Elevator()
var floor = Floor(FLOOR_FLOOR, CEILING_FLOOR)

//실제 사용자에 대한 변수 생성
var real_user_ride_floor=-1
var real_user_quit_floor=-1
var flag_ride=false

var end=false //시뮬레이션이 종료되었는지 확인
var simulation_start=false //시뮬레이션을 시작하는지 여부
var debug=false //디버그 모드인지 여부, 디버그 모드라면 시간이 10배 빨리 가고, 콘솔창에 결과가 출력됨
    
//실제 유저의 입력을 받고 생성한다.
class real_user_create : Thread() {
    public override fun run() {
        var ele1EstimateTimeOfArrival: Int = 0
        var ele2EstimateTimeOfArrival: Int = 0

        while (!Thread.interrupted()) {
            if((real_user_ride_floor>0)&&(real_user_quit_floor>0))
            {
                var real_user = User(now.toInt(), real_user_ride_floor, real_user_quit_floor)
                real_user.userid = "real_user1"

                // 탑승 예정 시간과 추천 엘리베이터 구하기
                if (ALGORITHM == "ZONING") {
                    calEstimateedTimeOfArrivalInZoning(real_user)
                }
                else if (ALGORITHM != "ALGORITHM2"){
                    ele1EstimateTimeOfArrival = calEstimateedTimeOfArrival(elevator1, real_user)
                    ele2EstimateTimeOfArrival = calEstimateedTimeOfArrival(elevator2, real_user)

                    if (ele1EstimateTimeOfArrival > ele2EstimateTimeOfArrival) {
                        estimatedTimeOfArrival = ele2EstimateTimeOfArrival
                        recommededElevator = "ELEVATOR2"
                    } else {
                        estimatedTimeOfArrival = ele1EstimateTimeOfArrival
                        recommededElevator = "ELEVATOR1"
                    }
                }


                println("userid : " + real_user.userid)
                println("생성 시간 : " + real_user.create_time)
                println("출발 층 : " + (real_user.ride_floor)) // 입력 범위: 1 ~ 50
                println("도착 층 : " + (real_user.quit_floor)) // 입력 범위: 1 ~ 50
                if (ALGORITHM != "ALGORITHM2") {
                    println("탑승까지 남은 시간 : $estimatedTimeOfArrival")
                    println("추천 엘리베이터 : $recommededElevator")
                }
                floor.user_list[real_user.ride_floor].add(real_user)
                estimatedTimeOfArrival+=now.toInt()

                break
            }

        }
    }
}

//승객을 생성하고 Floor로 넣는 함수
fun create_user(create_time: Int) {
    var rand = ThreadLocalRandom.current().nextInt(1, 4)
    var ride_rand = -1
    var quit_rand = -1

    //승객의 3분의 1은 1층에서 다른 층으로, 3분의 1은 다른 층에서 1층으로, 3분의 1은 1층이 아닌 층에서 1층이 아닌 층으로 이동하도록 생성한다.
    //1 to n
    if (rand == 1) {
        ride_rand = 1
        quit_rand = ThreadLocalRandom.current()
            .nextInt(
                FLOOR_FLOOR + 1,
                CEILING_FLOOR + 1
            ) // FLOOR_FLOOR <= quit_rand <= CEILING_FLOOR
    }
    //n to 1
    else if (rand == 2) {
        ride_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR + 1)
        quit_rand = 1
    }
    // n to m
    else if (rand == 3) {
        ride_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR, CEILING_FLOOR)
        quit_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR, CEILING_FLOOR)
        while (ride_rand == quit_rand)
            quit_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR, CEILING_FLOOR)
    } else {
        println("난수 생성 에러 : " + rand)
    }

    //User를 생성하고 탑승층에 User를 넣는다.
    var newuser = User(now.toInt(), ride_rand, quit_rand)
    floor.user_list[newuser.ride_floor].add(newuser)
}

// 해당 엘리베이터가 방향을 계속 유지했을 때, 유저와 마주치는지 여부를 반환하는 함수
fun isCrossed(elevator: Elevator, userRide: Int, userQuit: Int): Boolean {
    var userDirection: String = ""

    if (userRide < userQuit) userDirection = "UP"
    else userDirection = "DOWN"


    if (userDirection == "UP" && elevator.direction == "UP") {
        return userRide > elevator.next_floor
    } else if (userDirection == "UP" && elevator.direction == "DOWN") {
        return false
    } else if (userDirection == "DOWN" && elevator.direction == "UP") {
        return false
    } else if (userDirection == "DOWN" && elevator.direction == "DOWN") {
        return userRide < elevator.next_floor
    }
    return exitProcess(1000)

}

fun calEstimateedTimeOfArrival(elevator: Elevator, real_user: User): Int {
    var userDirection: String = ""
    var arriveTime: Int = 0

    // 사용자가 이동하는 방향을 확인
    userDirection = if (real_user.quit_floor > real_user.ride_floor) "UP"
    else "DOWN"

    // 사용자와 엘리베이터가 같은 방향
    if (userDirection == elevator.direction) {
        //엘리베이터가 사용자를 안지나침
        // 도착 예정시간 = only 사용자에게 도달하는 시간
        if (isCrossed(
                elevator,
                real_user.ride_floor,
                real_user.quit_floor
            )
        ) {
            arriveTime =
                abs(elevator.next_floor - real_user.ride_floor)
        }
        // 지나침
        // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 끝 층에서 끝 층으로 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
        else {
            if (elevator.direction == "UP") {
                arriveTime =
                    (CEILING_FLOOR - elevator.next_floor) + (CEILING_FLOOR - FLOOR_FLOOR) + (real_user.ride_floor - FLOOR_FLOOR)
            } else if (elevator.direction == "DOWN") {
                arriveTime =
                    (elevator.next_floor - FLOOR_FLOOR) + (CEILING_FLOOR - FLOOR_FLOOR) + (CEILING_FLOOR - real_user.ride_floor)
            }
        }
    }
    //사용자와 엘리베이터가 다른 방향
    // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
    else {
        if (elevator.direction == "UP") {
            arriveTime =
                (CEILING_FLOOR - elevator.next_floor) + (CEILING_FLOOR - real_user.ride_floor)
        } else if (elevator.direction == "DOWN") {
            arriveTime =
                (elevator.next_floor - FLOOR_FLOOR) + (real_user.ride_floor - FLOOR_FLOOR)
        }
    }
    return arriveTime
}

fun calEstimateedTimeOfArrivalInZoning(real_user: User) {
    var ele1EstimateTimeOfArrival: Int = 0
    var ele2EstimateTimeOfArrival: Int = 0
    var userDirection: String = ""

    // 사용자가 탑승하는 층수에 따라 추천 엘리베이터를 결정
    if (real_user.ride_floor >= FLOOR_FLOOR && real_user.ride_floor <= CEILING_FLOOR / 2)
        recommededElevator = "ELEVATOR1"
    else if (real_user.ride_floor >= CEILING_FLOOR / 2 && real_user.ride_floor <= CEILING_FLOOR)
        recommededElevator = "ELEVATOR2"

    // 사용자가 이동하는 방향을 확인
    userDirection = if (real_user.quit_floor > real_user.ride_floor) "UP"
    else "DOWN"

    // 25층 미만에서 타는 경우
    if (real_user.ride_floor < CEILING_FLOOR / 2) {
        // 내리는 층을 보고 환승하는지 확인
        if (real_user.quit_floor > CEILING_FLOOR / 2) real_user.isTransfer = true
        // 사용자와 엘리베이터가 같은 방향
        if (userDirection == elevator1.direction) {
            //엘리베이터가 사용자를 안지나침
            // 도착 예정시간 = only 사용자에게 도달하는 시간
            if (isCrossed(
                    elevator1,
                    real_user.ride_floor,
                    real_user.quit_floor
                )
            ) {
                ele1EstimateTimeOfArrival =
                    abs(elevator1.next_floor - real_user.ride_floor)
            }
            // 지나침
            // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 끝 층에서 끝 층으로 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
            else {
                if (elevator1.direction == "UP") {
                    ele1EstimateTimeOfArrival =
                        (CEILING_FLOOR / 2 - elevator1.next_floor) + (CEILING_FLOOR / 2 - FLOOR_FLOOR) + (real_user.ride_floor - FLOOR_FLOOR)
                } else if (elevator1.direction == "DOWN") {
                    ele1EstimateTimeOfArrival =
                        (elevator1.next_floor - FLOOR_FLOOR) + (CEILING_FLOOR / 2 - FLOOR_FLOOR) + (CEILING_FLOOR / 2 - real_user.ride_floor)
                }
            }
        }
        //사용자와 엘리베이터가 다른 방향
        // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
        else {
            if (elevator1.direction == "UP") {
                ele1EstimateTimeOfArrival =
                    (CEILING_FLOOR / 2 - elevator1.next_floor) + (CEILING_FLOOR / 2 - real_user.ride_floor)
            } else if (elevator1.direction == "DOWN") {
                ele1EstimateTimeOfArrival =
                    (elevator1.next_floor - FLOOR_FLOOR) + (real_user.ride_floor - FLOOR_FLOOR)
            }
        }
        // 사용자가 환승하는 경우, 다른 구역 엘리베이터가 현재 층에서 환승 층까지가는데 걸리는 시간을 더해줌
        if (real_user.isTransfer) {
            ele1EstimateTimeOfArrival += elevator2.next_floor - CEILING_FLOOR / 2
        }
    }
    //25층 초과에서 타는 경우
    else if (real_user.ride_floor > CEILING_FLOOR / 2) {
        // 내리는 층을 보고 환승하는지 확인
        if (real_user.quit_floor < CEILING_FLOOR / 2) real_user.isTransfer = true
        // 사용자와 엘리베이터가 같은 방향
        if (userDirection == elevator2.direction) {
            //엘리베이터가 사용자를 안지나침
            // 도착 예정시간 = only 사용자에게 도달하는 시간
            if (isCrossed(
                    elevator2,
                    real_user.ride_floor,
                    real_user.quit_floor
                )
            ) {
                ele2EstimateTimeOfArrival =
                    abs(elevator2.next_floor - real_user.ride_floor)
            }
            // 지나침
            // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 끝 층에서 끝 층으로 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
            else {
                if (elevator2.direction == "UP") {
                    ele2EstimateTimeOfArrival =
                        (CEILING_FLOOR - elevator2.next_floor) + (CEILING_FLOOR - CEILING_FLOOR / 2) + (real_user.ride_floor - CEILING_FLOOR / 2)
                } else if (elevator2.direction == "DOWN") {
                    ele2EstimateTimeOfArrival =
                        (elevator2.next_floor - CEILING_FLOOR / 2) + (CEILING_FLOOR - CEILING_FLOOR / 2) + (CEILING_FLOOR - real_user.ride_floor)
                }
            }
        }
        //사용자와 엘리베이터가 다른 방향
        // 도착 예정시간 = 현재 엘리베이터 방향에서 끝 층까지 이동하는 데 걸리는 시간 + 사용자에게 도달하는 시간
        else {
            if (elevator2.direction == "UP") {
                ele2EstimateTimeOfArrival =
                    (CEILING_FLOOR - elevator2.next_floor) + (CEILING_FLOOR - real_user.ride_floor)
            } else if (elevator2.direction == "DOWN") {
                ele2EstimateTimeOfArrival =
                    (elevator2.next_floor - CEILING_FLOOR / 2) + (real_user.ride_floor - CEILING_FLOOR / 2)
            }
        }
        // 사용자가 환승하는 경우, 다른 구역 엘리베이터가 현재 층에서 환승 층까지가는데 걸리는 시간을 더해줌
        if (real_user.isTransfer) {
            ele2EstimateTimeOfArrival += CEILING_FLOOR / 2 - elevator1.next_floor
        }
    }
    // 25층에서 타는 경우
    else if (real_user.ride_floor == CEILING_FLOOR / 2) {
        if (real_user.quit_floor < CEILING_FLOOR / 2) {
            ele1EstimateTimeOfArrival = real_user.ride_floor - elevator1.next_floor
        } else if (real_user.quit_floor > CEILING_FLOOR / 2) {
            ele2EstimateTimeOfArrival = elevator2.next_floor - real_user.ride_floor
        }
    }

    if (real_user.ride_floor >= FLOOR_FLOOR && real_user.ride_floor <= CEILING_FLOOR / 2) {
        estimatedTimeOfArrival = ele1EstimateTimeOfArrival
    } else if (real_user.ride_floor >= CEILING_FLOOR / 2 && real_user.ride_floor <= CEILING_FLOOR) {
        estimatedTimeOfArrival = ele2EstimateTimeOfArrival
    }

}

fun main() {
    //시뮬레이션인 경우 입력받은 값으로 세팅한다.
    Thread.sleep(1000)

    //실제 사용자의 입력을 기다리는 thread 생성
    val myThread = real_user_create()
    myThread.start()

    if (ALGORITHM == "ZONING") {
        elevator2.next_floor = CEILING_FLOOR / 2 as Int
    }
    
    //시뮬레이션이 실행된다.
    while (true) {
        var begin = System.nanoTime()

        //만약 승객이 엘리베이터에 탑승한 경우 에상 출발시간과 예상 엘리베이터를 실제값으로 변경한다.
        for (i in elevator1.up_user_list){
            if ((i.userid != "default")&&(!flag_ride)){
                estimatedTimeOfArrival=now.toInt()
                recommededElevator = "ELEVATOR1"
                flag_ride=true
            }
        }
        for (i in elevator1.down_user_list){
            if ((i.userid != "default")&&(!flag_ride)){
                estimatedTimeOfArrival=now.toInt()
                recommededElevator = "ELEVATOR1"
                flag_ride=true
            }
        }
        for (i in elevator2.up_user_list){
            if ((i.userid != "default")&&(!flag_ride)){
                estimatedTimeOfArrival=now.toInt()
                recommededElevator = "ELEVATOR2"
                flag_ride=true
            }
        }
        for (i in elevator2.down_user_list){
            if ((i.userid != "default")&&(!flag_ride)){
                estimatedTimeOfArrival=now.toInt()
                recommededElevator = "ELEVATOR2"
                flag_ride=true
            }
        }

        //30초에 한번 CONGESTION만큼의 승객을 생성한다.
        //승객의 3분의 1은 1층에서 다른 층으로, 3분의 1은 다른 층에서 1층으로, 3분의 1은 1층이 아닌 층에서 1층이 아닌 층으로 이동하도록 생성한다.
        if (now.toInt() % 30 == 0) {
            for (i: Int in 1..CONGESTION)
                create_user(now.toInt())
        }

        //10000초에 한번 층마다 대기 중인 승객 (생성시간, 출발 층, 도착 층)을 콘솔창에 보여준다.
        if ((debug)&&(now.toInt() % 100 == 0)) {
            println()
            println("\n현재 시간 : $now")
            for (i in floor.user_list) {
                if (i.size > 0) {
                    print("\n" + (i[0].ride_floor).toString() + "층 : ")
                }
                for (j in i) {
                    if (j.userid != "default")  // 실제 사용자
                        print("(" + j.userid + "," + j.create_time.toString() + "," + (j.ride_floor).toString() + "," + (j.quit_floor).toString() + ")" + "\t")
                    else    //  가상 사용자
                        print("(" + j.create_time.toString() + "," + (j.ride_floor).toString() + "," + (j.quit_floor).toString() + ")" +"\t")
                }
            }

            println("")
            print("엘리베이터 1 위치 ${elevator1.next_floor}층 (방향: ${elevator1.direction}): ")
            for (i in elevator1.up_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor},${i.quit_floor})\t")
                else
                    print("(${i.create_time},${i.ride_floor},${i.quit_floor})\t")
            }

            for (i in elevator1.down_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor},${i.quit_floor})\t")
                else
                    print("(${i.create_time},${i.ride_floor},${i.quit_floor})\t")
            }
            println("")

            print("엘리베이터 2 위치 ${elevator2.next_floor}층 (방향: ${elevator2.direction}): ")
            for (i in elevator2.up_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor},${i.quit_floor})\t")
                else
                    print("(${i.create_time},${i.ride_floor},${i.quit_floor})\t")
            }
            for (i in elevator2.down_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor},${i.quit_floor})\t")
                else
                    print("(${i.create_time},${i.ride_floor},${i.quit_floor})\t")
            }
            println("")

        }

        //알고리즘의 종류에 따른 엘리베이터 운영 (멈추기, 내리기, 태우기)
        //각 알고리즘 수행 결과 엘리베이터 한 층 씩 이동
        if (ALGORITHM == "COLLECTIVE_CONTROL") {
            //엘리베이터 1 동작
            CCoperation(elevator1)
            //엘리베이터 2 동작
            CCoperation(elevator2)
        } else if (ALGORITHM == "ALGORITHM2") {
            // 엘리베이터 1 동작
            algorithm2(elevator2.priority_direction, elevator1)
            //algorithm2(elevator1)
            // 엘리베이터 2 동작
            algorithm2(elevator1.priority_direction, elevator2)
            //algorithm2(elevator2)
        } else if (ALGORITHM == "ZONING") { // Zoning Algorithm
            // 엘리베이터 1 동작
            zoning(elevator1, 1)    // 1층부터 (최상층 / 2) 층
            // 엘리베이터 2 동작
            zoning(elevator2, 2)    // (최상층 / 2) + 1 층부터 최상층
        } else if (ALGORITHM == "ALGORITHM4") {
            algorithm4(elevator1)
            algorithm4(elevator2)
        }


        //시뮬레이션이 아니라 실제 사용자를 대상으로 실행 중이라면 1초의 지연시간을 둔다.
        while (!IS_SIMULATION)
            if (System.nanoTime() - begin > 100000000)
                break





        //debug code
        if (now.toInt() < SIMULATION_DURATION)
            now++
        else
            break

        if((!IS_SIMULATION)&&(simulation_start))
            return
    }


    myThread.interrupt()

    end=true
    //성능평가 결과 출력
    println("\n$ALGORITHM Algorithm 성능평가 결과")
    println("옮긴 승객 : $sum_passenger 명")
    println("총 기다린 시간: $sum_waiting")
    println("엘리베이터 동작 효율 : ${(sum_passenger.toDouble() / sum_distance.toDouble()) * 100}%")
    println("평균 사용시간 : ${sum_waiting.toDouble() / sum_passenger.toDouble()}")
    println("가장 오래 기다린 승객의 사용시간 : $longest_waiting")

    Thread.sleep(10000000)
}


