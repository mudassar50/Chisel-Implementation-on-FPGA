package examples

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class LCMTests(c: LCM) extends PeekPokeTester(c) {
  val inputs = List(
    (12, 15),  // LCM of 12 and 15 should be 60
    (7, 3),    // LCM of 7 and 3 should be 21
    (10, 5)    // LCM of 10 and 5 should be 10
  )
  val outputs = List(60, 21, 10)

  var i = 0
  do {
    // Poke the inputs (using 1 and 0 for true/false)
    poke(c.io.in1.valid, 1)
    poke(c.io.in2.valid, 1)
    poke(c.io.in1.bits, inputs(i)._1)
    poke(c.io.in2.bits, inputs(i)._2)

    // Trigger the computation
    poke(c.io.out.ready, 1)

    // Step through the clock cycles
    step(1)
    
    var valid = false
    do {
      valid = peek(c.io.out.valid) == 1
      step(1)
    } while (peek(c.io.out.valid) == 0 && i < 3)
    
    val lcmResult = peek(c.io.out.bits)
    println(s"LCM of ${inputs(i)._1} and ${inputs(i)._2} is: $lcmResult")
    
    // Expect the output to match the expected value
    expect(c.io.out.bits, outputs(i))

    i += 1
  } while (i < 3)

  if (t >= 100) fail
}

class LCMTester extends ChiselFlatSpec {
  behavior of "LCM"

  backends foreach {backend =>
    it should s"test the basic LCM circuit" in {
      Driver(() => new LCM(8), backend)((c) => new LCMTests(c)) should be (true)
    }
  }
}
