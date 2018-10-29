package com.github.apuex.angularsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelLoader
import com.github.apuex.springbootsolution.codegen.ModelUtils._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TypeConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.{Node, Text}

object FromModel extends App {
  val xml = ModelLoader(args(0)).xml
  val modelName = xml.attribute("name").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-frontend"
  val srcDir = s"${projectDir}/src/app"

  new File(srcDir).mkdirs()

  xml.child.filter(x => x.label == "entity")
    .foreach(x => {
      moduleForEntity(x)
    })

  def moduleForEntity(entity: Node): Unit = {
    val entityName = entity.\@("name")
    val moduleDir = s"${srcDir}/${cToShell(entityName)}"
    new File(moduleDir).mkdirs()

    entityForModule(entity, entityName, moduleDir)
    serviceForModule(entity, entityName, moduleDir)
    tsForModule(entity, entityName, moduleDir)
    routingForModule(entity, entityName, moduleDir)
    componentForEntity(entity, entityName, moduleDir)
  }

  def entityForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def serviceForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.service.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def tsForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.module.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def routingForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}-routing.module.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def componentForEntity(entity: Node, entityName: String, moduleDir: String): Unit = {
    val componentDir = s"${srcDir}/${cToShell(entityName)}/${cToShell(entityName)}"
    new File(componentDir).mkdirs()

    cssForComponent(entity, entityName, componentDir)
    htmlForComponent(entity, entityName, componentDir)
    tsForComponent(entity, entityName, componentDir)
  }

  def cssForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.css", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def htmlForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.html", "utf-8")
    printWriter.println()
    printWriter.close()

  }

  def tsForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }
}
