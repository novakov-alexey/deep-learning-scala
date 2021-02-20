package ml.network

object api extends GradientClippingApi with ActivationFuncApi with LossApi with MetricApi:  
  final type SimpleGD = ml.network.SimpleGD
  export ml.network.Dense
  export ml.network.optimizers.given
  export ml.network.Weight
  export ml.network.Sequential
  import ml.network.RandomGen.given