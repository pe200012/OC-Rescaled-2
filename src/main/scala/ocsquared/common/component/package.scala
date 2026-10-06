package ocsquared.common

import scala.language.implicitConversions

package object component {
  implicit def result(args: Any*): Array[AnyRef] = ocsquared.util.ResultWrapper.result(args*)
}
