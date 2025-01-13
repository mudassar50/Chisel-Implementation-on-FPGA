package examples

import chisel3._
import chisel3.util._

class LCM(val w: Int) extends Module {
  val io = IO(new Bundle {
    val in1 = Flipped(Valid(UInt(w.W)))
    val in2 = Flipped(Valid(UInt(w.W)))
    val out = Decoupled(UInt(w.W))
  })

  val x = Reg(UInt(w.W))
  val y = Reg(UInt(w.W))
  val a = Reg(UInt(w.W))
  val b = Reg(UInt(w.W))

  // Use chisel3.util.Enum to define the states
  val s_idle :: s_dataIn :: s_gcdComp :: s_lcmComp :: Nil = Enum(4)
  val state = RegInit(s_idle)

  // State transition logic
  state := MuxCase(state, Seq(
    ((state === s_idle) && io.in1.valid && io.in2.valid) -> s_dataIn,
    (state === s_dataIn) -> s_gcdComp,
    ((state === s_gcdComp) && (x === y)) -> s_lcmComp,
    ((state === s_lcmComp) && io.out.ready) -> s_idle
  ))

  // Data input handling
  when(state === s_dataIn) {
    x := io.in1.bits
    y := io.in2.bits
    a := io.in1.bits
    b := io.in2.bits
  }

  // GCD computation
  when(state === s_gcdComp) {
    when(x > y) {
      x := x - y
    } .otherwise {
      y := y - x
    }
  }

  // Output assignment: LCM = (a * b) / GCD
  io.out.bits := a * b / x
  io.out.valid := state === s_lcmComp
}
object LCM extends App {
  chisel3.Driver.execute(args, () => new LCM(8))
}
