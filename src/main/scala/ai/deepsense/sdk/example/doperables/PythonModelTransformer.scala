package ai.deepsense.sdk.example.doperables

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.OperationExecutionDispatcher.Result
import ai.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, StringParam}
import ai.deepsense.deeplang.doperables.CustomCodeTransformer

class PythonModelTransformer extends CustomCodeTransformer {

  val modelParams = StringParam(
    name = "Model Parameters",
    description = Some("Parameters to be used in the model as a JSON string")//,
//    validator = RangeValidator(Int.MinValue, Int.MaxValue, step = Some(1.0))
  )
  setDefault(modelParams, "{\"numTopics\": 5}")
  def setModelParams(value: String): this.type = set(modelParams, value)
  def getModelParams: String = $(modelParams)

  override lazy val codeParameter = CodeSnippetParam(
    name = "code",
    description = None,
    language = CodeSnippetLanguage(CodeSnippetLanguage.python)
  )
  setDefault(codeParameter -> "def transform(dataframe):\n    return dataframe")

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(modelParams, codeParameter)

  def getComposedCode(userCode: String): String = {
    s"""
       |import json
       |
       |PARAMS = json.loads(${getModelParams})
       |
       |$userCode
    """.stripMargin
  }

  override def isValid(context: ExecutionContext, code: String): Boolean =
    context.customCodeExecutor.isPythonValid(getComposedCode(code))

  override def runCode(context: ExecutionContext, code: String): Result =
    context.customCodeExecutor.runPython(getComposedCode(code))
}
