# Seahorse Customs Operations for the SLR platform

- PythonModelTransformation: Allows to execute custom ML models in Python

## Seahorse SDK usage example

### Getting started
You can create a JAR with the following steps:

1. Run `sbt assembly`. This produces a JAR in `target/scala-2.11` directory.
2. Put this JAR in `$SEAHORSE/jars`, where `$SEAHORSE` is the directory with
docker-compose.yml or Vagrantfile (depending whether you run Docker or Vagrant).
3. Restart Seahorse (By either `stop`ping and `start`ing `docker-compose` or `halt`ing and `up`ing `vagrant`).
4. Operations are now visible in Seahorse Workflow Editor.

### Operation examples

#### Identity Operation
This is a simple example designed to show a minimum set of steps needed to create an operation. You can find full source
in [IdentityOperation.scala](src/main/scala/io/deepsense/sdk/example/IdentityOperation.scala).

First, we need to annotate the operation so that Seahorse knows that it should be registered in the operation catalogue.
```scala
  @Register
```
We extend `DOperation1To1[DataFrame, DataFrame]`
because our operation takes one `DataFrame` as input and return one `DataFrame` on the output.
```scala
  final class IdentityOperation
      extends DOperation1To1[DataFrame, DataFrame]
```

Version 4 UUID used to uniquely identify this operation.
After changes in operation are made (e.g. name is changed), `id` should not be changed – it is used by Seahorse to
recognize it as the same operation as previously.
```scala
    override val id: Id = "e9990168-daf7-44c6-8e0c-fbc50456fbec"
```
Next, we define some information for the user.
```scala
    override val name: String = "Identity"
    override val description: String = "Passes DataFrame along."
```
Now for the "core logic" of our operation. In our case we simply return the
`DataFrame` that was passed as an argument.
```scala
    override protected def execute(input: DataFrame)(context: ExecutionContext): DataFrame = input
```
Finally, we declare operation's parameters. Our operations does not have any, so empty `Array` is returned.
```scala
    override def params = Array.empty
```

#### Random Split
This example is slightly more advanced and more practical. We implement `RandomSplit`, a simpler version of
`ai.deepsense.deeplang.doperations.Split`, which splits DataFrame in two with given ratio. You can find full source
in [RandomSplit.scala](src/main/scala/io/deepsense/sdk/example/RandomSplit.scala).


We register our class, as before. This operation takes one `DataFrame` and returns two `DataFrame`s,
so operation type is `DOperation1To2`.
We also extend `Params` – a mixin helpful when declaring operation's parameters.
```scala
  extends DOperation1To2[DataFrame, DataFrame, DataFrame] with Params
```

After declaring `id`, `name` and `description`, we declare first of user-definable parameters, `splitRatio`
```scala
  val splitRatio = NumericParam(
    name = "split ratio",
    description = "Percentage of rows that should end up in the first output DataFrame.",
```

It should be a number in \[0, 1\] interval...
```scala
    validator = RangeValidator(0.0, 1.0, beginIncluded = true, endIncluded = true))
```

... and is equal to 0.5 by default.
If we did not provide the default, user would get a warning while trying to use this operation.
```scala
  setDefault(splitRatio, 0.5)
```

We can also provide standard getters and setters. It is optional, can be useful in tests however.
```
  def setSplitRatio(value: Double): this.type = set(splitRatio, value)
  def getSplitRatio: Double = $(splitRatio)
```

Similarly, we give user ability to provide a seed for a pseudorandom number generator.
```scala
  val seed = NumericParam(
    name = "seed",
    description = "Seed for pseudo random number generator",
    validator = RangeValidator(Int.MinValue, Int.MaxValue, step = Some(1.0))
  )
  setDefault(seed, 0.0)
```

Now, we declare which parameters this operation has, together with order in which they will be shown to user.
`splitRatio` is more important, so it goes first.
`declareParams` will additionally check that we did not miss any parameter.
```scala
  override val params = declareParams(splitRatio, seed)
```

Execution is pretty straightforward. Schema of resulting DataFrames is the same as the input DataFrame.
We split `DataFrame` into two `DataFrame`s,
using underlying Spark `randomSplit` function and wrapping them back in DataFrames.
```scala
  override protected def execute(
      df: DataFrame)(
      context: ExecutionContext): (DataFrame, DataFrame) = {
    val Array(f1: RDD[Row], f2: RDD[Row]) =
      df.sparkDataFrame.rdd.randomSplit(Array(getSplitRatio, 1.0 - getSplitRatio), getSeed)
    val schema = df.sparkDataFrame.schema
    val dataFrame1 = context.dataFrameBuilder.buildDataFrame(schema, f1)
    val dataFrame2 = context.dataFrameBuilder.buildDataFrame(schema, f2)
    (dataFrame1, dataFrame2)
  }
```

Finally, we add inference, which is mainly used to deduce the output schema. It can be also used to validate that the
input schema is correct.

In our case, we simply say that schemas of both resulting `DataFrame`s are the same as schema of input `DataFrame`
and we return no warnings.
```scala
  override protected def inferKnowledge(knowledge: DKnowledge[DataFrame])(context: InferContext)
      : ((DKnowledge[DataFrame], DKnowledge[DataFrame]), InferenceWarnings) = {
    ((knowledge, knowledge), InferenceWarnings.empty)
  }
```
