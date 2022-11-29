import com.example.elevator_app.CCoperation
import com.example.elevator_app.zoning
import java.util.concurrent.ThreadLocalRandom

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
        for (i: Int in floor..ceiling) {
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
    var next_floor = 0 //엘리베이터가 지나갈 층, collective control에서는 현재층+1 또는 현재층-1
    var arrive_time = -1 //엘리베이터가 next_floor에 도착하는 시간
}

// 상수
//ALGORITHM 변수 후보: COLLECTIVE_CONTROL, algorithm2,  ZONING
val ALGORITHM = "ZONING" //사용하는 알고리즘
val IS_SIMULATION = true //시뮬레이션 중인지, 실제 사용자가 사용하는 경우인지 구분
val SIMULATION_DURATION = 604800 //시뮬레이션 지속 시간 604800초는 1주일 debug code
val MAXIMUM_NUMBER = 24 // 엘리베이터 정원
val OPEN_TIME = 4 // 엘리베이터의 문이 열리는 시간
val CLOSE_TIME = 4  // 엘리베이터의 문이 닫히는 시간
val MOVETIME = 1 // 1층을 이동하는 시간
val CONGESTION = 4 // 30초마다 생성되는 승객의 수 (자연수)
val FLOOR_FLOOR = 0 //제일 낮은 층 : 1층
val CEILING_FLOOR = 50 //제일 높은 층 : 50층


//변수
var now: Long = 0
var sum_passenger: Long = 0 // 모든 승객 수의 합
var sum_waiting: Long = 0 // 모든 승객들이 기다린 시간의 합
var sum_distance: Long = 0 // 엘리베이터의 총 이동 거리
var average_waiting: Long = 0 // 모든 승객들이 기다린 시간의 평균
var longest_waiting: Long = 0 // 가장 오래 기다린 승객이 기다린 시간
var average_distance: Long = 0 // 엘리베이터의 총 이동 거리/옮긴 승객

//Elevator 2개, Floor 1개 생성
var elevator1 = Elevator()
var elevator2 = Elevator()
var floor = Floor(FLOOR_FLOOR, CEILING_FLOOR)

//실제 유저의 입력을 받고 생성한다.
class real_user_create : Thread() {
    public override fun run() {
        while (!Thread.interrupted()) {
            var real_user_input = readLine()!!
            var arr = real_user_input.split(" ")
            var real_user = User(now.toInt(), arr[1].toInt() - 1, arr[2].toInt() - 1)
            real_user.userid = arr[0]
            println("userid : " + real_user.userid)
            println("생성 시간 : " + real_user.create_time)
            println("출발 층 : " + (real_user.ride_floor + 1))
            println("도착 층 : " + (real_user.quit_floor + 1))
            floor.user_list[real_user.ride_floor].add(real_user)
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
        ride_rand = 0
        quit_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR)
    }
    //n to 1
    else if (rand == 2) {
        ride_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR)
        quit_rand = 0
    }
    // n to m
    else if (rand == 3) {
        ride_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR)
        quit_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR)
        while (ride_rand == quit_rand)
            quit_rand = ThreadLocalRandom.current().nextInt(FLOOR_FLOOR + 1, CEILING_FLOOR)
    } else {
        println("난수 생성 에러 : " + rand)
    }

    //User를 생성하고 탑승층에 User를 넣는다.
    var newuser = User(now.toInt(), ride_rand, quit_rand)
    floor.user_list[newuser.ride_floor].add(newuser)
}

fun main() {
    //Zoning 알고리즘일 때, 초기 엘리베이터의 시작 위치를 세팅
    if (ALGORITHM == "ZONING") {
        elevator1.next_floor = 0
        elevator2.next_floor = CEILING_FLOOR / 2 as Int
    }

    //실제 사용자의 입력을 기다리는 thread 생성
    val myThread = real_user_create()
    myThread.start()

    //시뮬레이션이 실행된다.
    while (true) {
        var begin = System.nanoTime()

        //30초에 한번 CONGESTION만큼의 승객을 생성한다.
        //승객의 3분의 1은 1층에서 다른 층으로, 3분의 1은 다른 층에서 1층으로, 3분의 1은 1층이 아닌 층에서 1층이 아닌 층으로 이동하도록 생성한다.
        if (now.toInt() % 30 == 0) {
            for (i: Int in 1..CONGESTION)
                create_user(now.toInt())
        }

        //10000초에 한번 층마다 대기 중인 승객 (생성시간, 출발 층, 도착 층)을 콘솔창에 보여준다.
        if (now.toInt() % 10000 == 0) {
            println()
            println("\n" + "현재 시간 : " + now.toString())
            for (i in floor.user_list) {
                if (i.size > 0) {
                    print("\n" + (i[0].ride_floor + 1).toString() + "층 : ")
                }
                for (j in i) {
                    if (j.userid != "default")  // 실제 사용자
                        print("(" + j.userid + "," + j.create_time.toString() + "," + (j.ride_floor + 1).toString() + "," + (j.quit_floor + 1).toString() + ")" + "\t")
                    else    //  가상 사용자
                        print("(" + j.create_time.toString() + "," + (j.ride_floor + 1).toString() + "," + (j.quit_floor + 1).toString() + ")" + "\t")
                }
            }

            println("")
            print("엘리베이터 1 위치 ${elevator1.next_floor}층 : ")
            for (i in elevator1.up_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
                else
                    print("(${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
            }

            for (i in elevator1.down_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
                else
                    print("(${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
            }
            println("")

            print("엘리베이터 2 위치 ${elevator2.next_floor}층 : ")
            for (i in elevator2.up_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
                else
                    print("(${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
            }
            for (i in elevator2.down_user_list) {
                if (i.userid != "default")
                    print("(${i.userid},${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
                else
                    print("(${i.create_time},${i.ride_floor + 1},${i.quit_floor + 1})\t")
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
            algorithm2(elevator1)
            // 엘리베이터 2 동작
            algorithm2(elevator2)
        } else if (ALGORITHM == "ZONING") { // Zoning Algorithm
            // 엘리베이터 1 동작
            zoning(elevator1, 1)    // 1층부터 (최상층 / 2) 층
            // 엘리베이터 2 동작
            zoning(elevator2, 2)    // (최상층 / 2) + 1 층부터 최상층
        }


        //시뮬레이션이 아니라 실제 사용자를 대상으로 실행 중이라면 1초의 지연시간을 둔다.
        while (IS_SIMULATION == false)
            if (System.nanoTime() - begin > 100000000)
                break


        //debug code
        if (now.toInt() < SIMULATION_DURATION)
            now++
        else
            break
    }


    myThread.interrupt()

    //성능평가 결과 출력
    println("\n성능평가 결과")
    println("옮긴 승객 : $sum_passenger")
    println(sum_waiting)
    println("엘리베이터 동작 효율 : ${sum_passenger.toDouble() / sum_distance.toDouble()}")
    println("평균 사용시간 : ${sum_waiting.toDouble() / sum_passenger.toDouble()}")
    println("가장 오래 기다린 승객의 사용시간 : $longest_waiting")
}


