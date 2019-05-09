package cpscala.TSolver.Model.Constraint.IPConstraint

import java.util.concurrent.Callable

import cpscala.TSolver.Model.Constraint.SConstraint.Propagator
import cpscala.TSolver.Model.Variable.PVar

abstract class IPPropagator extends Propagator[PVar] with Callable[Boolean] {



  def propagate(): Boolean

}
