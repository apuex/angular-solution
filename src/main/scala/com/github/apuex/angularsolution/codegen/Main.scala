package com.github.apuex.angularsolution.codegen

object Main extends App {
  if(args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "from-model" => FromModel.main(args.drop(1))
      case "generate-component" => GenerateAll.main(args.drop(1))
      case c =>
        println(s"unknown command '${c}'")
    }
  }
}

