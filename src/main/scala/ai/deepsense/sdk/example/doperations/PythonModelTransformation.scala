package ai.deepsense.sdk.example.doperations

import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperations.TransformerAsOperation
import ai.deepsense.deeplang.refl.Register

import ai.deepsense.sdk.example.doperables.PythonModelTransformer

@Register
case class PythonModelTransformation() extends TransformerAsOperation[PythonModelTransformer] with OperationDocumentation {

  override val id: Id = "26bea2bb-2e16-4176-944b-cb630d85d343"
  override val name: String = "Python Model Transformation"
  override val description: String = "Creates a custom Python model transformation"

  override lazy val tTagTO_1: TypeTag[PythonModelTransformer] = typeTag

  override val since: Version = Version(1, 0, 0)
}

object PythonModelTransformation {
  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0
}
