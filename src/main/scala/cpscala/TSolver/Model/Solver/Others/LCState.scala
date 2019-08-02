package cpscala.TSolver.Model.Solver.Others

object LCState extends Enumeration {
  type LCState = Value
  val Idle = Value(0)
  val NeedStop = Value(1)
  val Running = Value(2)
  val Success = Value(3)
  val Fail = Value(4)
}
