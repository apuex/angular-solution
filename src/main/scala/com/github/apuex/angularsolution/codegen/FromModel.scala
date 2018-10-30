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
      moduleForEntity(xml, x)
    })

  def moduleForEntity(model: Node, entity: Node): Unit = {
    val entityName = entity.\@("name")
    val moduleDir = s"${srcDir}/${cToShell(entityName)}"
    new File(moduleDir).mkdirs()

    messageForModule(model, entity, entityName, moduleDir)
    serviceForModule(entity, entityName, moduleDir)
    tsForModule(entity, entityName, moduleDir)
    routingForModule(entity, entityName, moduleDir)
    componentForEntity(entity, entityName, moduleDir)
  }

  def messageForModule(model: Node, entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.ts", "utf-8")

    val columns = persistentColumnsExtended(model, entity)
      .map(f => (f.\@("no"), f.\@("name"), f.\@("type")))
    val pkColumns = primaryKeyColumns(model, entity)
      .map(x => x.\@("name"))
      .toSet

    val template =
      s"""// ${cToPascal(entityName)}.ts: 100% generated, do not edit.
         |${messageDependencies(model, columns.map(f => f._3).filter(f => !isTypeScriptType(f)).toSet)}
         |
         |export class ${cToPascal(entityName)}Vo {
         |  ${indent(fields(columns), 2)};
         |}
         |
         |export class ${cToPascal(entityName)}ListVo {
         |  items: ${cToPascal(entityName)}Vo[];
         |}
         |
         |export class Create${cToPascal(entityName)}Cmd {
         |  ${indent(fields(columns), 2)};
         |}
         |
         |export class Update${cToPascal(entityName)}Cmd {
         |  ${indent(fields(columns), 2)};
         |}
         |
         |export class Retrieve${cToPascal(entityName)}Cmd {
         |  ${indent(fields(columns.filter(f => pkColumns.contains(f._2))), 2)};
         |}
         |
         |export class Delete${cToPascal(entityName)}Cmd {
         |  ${indent(fields(columns.filter(f => pkColumns.contains(f._2))), 2)};
         |}
         |
       """.stripMargin
    printWriter.print(template)
    printWriter.close()
  }

  def messageDependencies(model: Node, types: Set[String]): String = {
    val dep = types.map(f => (f, messages(entityFor(model, f), f)))
      .map(f => "import { %s } from '../%s/%s'".format(cToPascal(f._2), cToShell(f._1), cToShell(f._1)))
    if(dep.isEmpty) "" else dep.reduce((x, y) => "%s;\n%s".format(x, y))
  }

  def fields(columns: Seq[(String, String, String)]): String = {
    columns.map(f => "%s: %s".format(cToCamel(f._2), toTypeScriptType(f._3)))
      .reduce((x, y) => "%s;\n%s".format(x, y))
  }

  def serviceForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.service.ts", "utf-8")

    val template =
      s"""// ${cToPascal(entityName)}.service.ts: 100% generated, do not edit.
         |import { Injectable } from '@angular/core';
         |import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
         |
         |import { ${cToPascal(entityName)} } from './${cToShell(entityName)}'
         |
         |@Injectable({
         |  providedIn: 'root'
         |})
         |export class ${cToPascal(entityName)}Service {
         |  // TODO: add dependencies to constructor.
         |  constructor(private http: HttpClient) {}
         |
         |  // TODO: add service methods.
         |}
         |
       """.stripMargin
    printWriter.print(template)
    printWriter.close()
  }

  def importMessageForModule(model: Node, entity: Node, entityName: String): Seq[String] = {
    val symbols: String = messages(entity, entityName)
    persistentColumnsExtended(model, entity)
      .map(f => f.\@("type"))
      .filter(f => !isTypeScriptType(f))
      .toSet[String]
      .flatMap(f => importMessageForModule(model, entityFor(model, f), f))
      .toSeq ++ Seq(s"""import { ${symbols} } from './${cToShell(entityName)}'""")
  }

  private def messages(entity: Node, entityName: String): String = {
    val isEnum = "true".equalsIgnoreCase(entity.\@("enum"))
    ((if (isEnum) Seq(s"""${cToPascal(entityName)}""") else Seq()) ++ Seq(
      s"""${cToPascal(entityName)}Vo""",
      s"""${cToPascal(entityName)}ListVo""",
      s"""Create${cToPascal(entityName)}Cmd""",
      s"""Update${cToPascal(entityName)}Cmd""",
      s"""Retrieve${cToPascal(entityName)}Cmd""",
      s"""Delete${cToPascal(entityName)}Cmd"""
    )).reduce((x, y) => s"""${x}, ${y}""")
  }

  def tsForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.module.ts", "utf-8")

    val template =
      s"""
        |import { NgModule }       from '@angular/core';
        |import { CommonModule }   from '@angular/common';
        |import { FormsModule }    from '@angular/forms';
        |
        |import { ${cToPascal(entityName)}Component }    from './${cToShell(entityName)}/${cToShell(entityName)}.component';
        |import { ${cToPascal(entityName)}RoutingModule } from './${cToShell(entityName)}-routing.module';
        |
        |@NgModule({
        |  imports: [
        |    CommonModule,
        |    FormsModule,
        |    ${cToPascal(entityName)}RoutingModule
        |  ],
        |  declarations: [
        |    ${cToPascal(entityName)}Component
        |  ]
        |})
        |export class ${cToPascal(entityName)}Module {}
        |
      """.stripMargin
    printWriter.print(template)
    printWriter.close()
  }

  def routingForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}-routing.module.ts", "utf-8")

    val template =
      s"""
         |import { NgModule }             from '@angular/core';
         |import { RouterModule, Routes } from '@angular/router';
         |import { ${cToPascal(entityName)}Service }          from './${cToShell(entityName)}.service';
         |import { ${cToPascal(entityName)}Component }       from './${cToShell(entityName)}/${cToShell(entityName)}.component';
         |
         |const ${cToCamel(entityName)}Routes: Routes = [
         |  { path: '${cToShell(entityName)}', component: ${cToPascal(entityName)}Component }
         |];
         |
         |@NgModule({
         |  imports: [
         |    RouterModule.forChild(${cToCamel(entityName)}Routes)
         |  ],
         |  exports: [
         |    RouterModule
         |  ]
         |})
         |export class ${cToPascal(entityName)}RoutingModule {}
         |
       """.stripMargin
    printWriter.print(template)
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
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.component.css", "utf-8")
    printWriter.println()
    printWriter.close()
  }

  def htmlForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.component.html", "utf-8")
    printWriter.println()
    printWriter.close()

  }

  def tsForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.component.ts", "utf-8")
    printWriter.println()
    printWriter.close()
  }
}
