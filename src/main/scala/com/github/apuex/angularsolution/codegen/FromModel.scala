package com.github.apuex.angularsolution.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.codegen.ModelLoader
import com.github.apuex.springbootsolution.codegen.ModelUtils._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TypeConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._

import scala.xml.{Node, Text}

object FromModel extends App {
  val model = ModelLoader(args(0)).xml
  val modelName = model.attribute("name").asInstanceOf[Some[Text]].get.data
  val projectRoot = s"${System.getProperty("project.root", "target/generated")}"
  val projectDir = s"${projectRoot}/${cToShell(modelName)}/${cToShell(modelName)}-frontend"
  val srcDir = s"${projectDir}/src/app"

  new File(srcDir).mkdirs()

  model.child.filter(x => x.label == "entity")
    .foreach(x => {
      moduleForEntity(x)
    })

  def moduleForEntity(entity: Node): Unit = {
    val entityName = entity.\@("name")
    val moduleDir = s"${srcDir}/${cToShell(entityName)}"
    new File(moduleDir).mkdirs()

    messageForModule(entity, entityName, moduleDir)
    serviceForModule(entity, entityName, moduleDir)
    tsForModule(entity, entityName, moduleDir)
    routingForModule(entity, entityName, moduleDir)
    componentForEntity(entity, entityName, moduleDir)
  }

  def messageForModule(entity: Node, entityName: String, moduleDir: String): Unit = {
    val printWriter = new PrintWriter(s"${moduleDir}/${cToShell(entityName)}.ts", "utf-8")

    val columns = persistentColumnsExtended(model, entity)
      .map(f => (f.\@("no"), f.\@("name"), f.\@("type")))
    val pkColumns = primaryKeyColumns(model, entity)
      .map(x => x.\@("name"))
      .toSet

    val template =
      s"""// ${cToShell(entityName)}.ts: 100% generated, do not edit.
         |${messageDependencies(columns.map(f => f._3).filter(f => !isTypeScriptType(f)).toSet)}
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

  def messageDependencies(types: Set[String]): String = {
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
      s"""// ${cToShell(entityName)}.service.ts: 100% generated, do not edit.
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

  def importMessageForModule(entity: Node, entityName: String): Seq[String] = {
    val symbols: String = messages(entity, entityName)
    persistentColumnsExtended(model, entity)
      .map(f => f.\@("type"))
      .filter(f => !isTypeScriptType(f))
      .toSet[String]
      .flatMap(f => importMessageForModule(entityFor(model, f), f))
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
      s"""// ${cToShell(entityName)}.module.ts: 100% generated, do not edit.
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
      s"""// ${cToShell(entityName)}-routing.module.ts: 100% generated, do not edit.
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

    val template =
      s"""/* ${cToShell(entityName)}.component.css: 100% generated, do not edit. */
         |
       """.stripMargin

    printWriter.print(template)
    printWriter.close()
  }

  def htmlForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.component.html", "utf-8")

    val template =
      s"""<!-- ${cToShell(entityName)}.component.html: 100% generated, do not edit. -->
         |<form [formGroup]="${cToCamel(entityName)}Form" (ngSubmit)="submit(${cToCamel(entityName)}Form.value)">
         |  ${indent(fieldsForForm(entity, entityName), 2)}
         |  <button type="submit" class="btn btn-lg btn-primary btn-block" i18n>OK</button>
         |  <button class="btn btn-lg btn-secondary btn-block" i18n>Cancel</button>
         |</form>
       """.stripMargin

    printWriter.print(template)
    printWriter.close()

  }

  def fieldsForForm(entity: Node, entityName: String): String = {
    persistentColumnsExtended(model, entity)
      .map(f => (f.\@("no"), f.\@("name"), f.\@("type"), f.\@("length"), f.\@("required")))
      .sortWith((x, y) => x._1 < y._1)
      .map(f => fieldForForm(f._2, f._3, f._4, f._5))
      .reduce((x, y) => s"${x}\n${y}")
  }

  def fieldForForm(name: String, _type: String, length: String, required: String): String = {
    s"""${labelForField(name, _type, length, required)}
      |${inputForField(name, _type, length, required)}""".stripMargin
  }

  def inputForField(name: String, _type: String, length: String, required: String): String = _type match {
    case "bool" => checkboxForField(name, _type, length, required)
    case "short" => textForField(name, _type, length, required)
    case "byte" => textForField(name, _type, length, required)
    case "int" => textForField(name, _type, length, required)
    case "identity" => textForField(name, _type, length, required)
    case "long" => textForField(name, _type, length, required)
    case "decimal" => textForField(name, _type, length, required)
    case "string" => if(length.toInt > 256) textAreaForField(name, _type, length, required) else textForField(name, _type, length, required)
    case "timestamp" => datetimeForField(name, _type, length, required)
    case "float" => textForField(name, _type, length, required)
    case "double" => textForField(name, _type, length, required)
    case "blob" => textForField(name, _type, length, required)
    case _ => if(isEnum(model, _type)) selectForField(name, _type, length, required) else textForField(name, _type, length, required)
  }

  def tsForComponent(entity: Node, entityName: String, componentDir: String): Unit = {
    val printWriter = new PrintWriter(s"${componentDir}/${cToShell(entityName)}.component.ts", "utf-8")

    val template =
      s"""// ${cToShell(entityName)}.component.ts: 100% generated, do not edit.
         |import { Component, OnInit } from '@angular/core';
         |import { ${cToPascal(entityName)}Service } from '../${cToShell(entityName)}.service';
         |
         |@Component({
         |  selector: '${cToShell(entityName)}',
         |  templateUrl: './${cToShell(entityName)}.component.html',
         |  styleUrls: ['./${cToShell(entityName)}.component.css']
         |})
         |export class ${cToPascal(entityName)}Component implements OnInit {
         |
         |  constructor(public ${cToCamel(entityName)}Service: ${cToPascal(entityName)}Service) {}
         |
         |  ngOnInit() {
         |  }
         |
         |}
         |
       """.stripMargin

    printWriter.print(template)
    printWriter.close()
  }

  def labelForField(name: String, _type: String, length: String, required: String): String = {
    val asterisk = if("true".equalsIgnoreCase(required)) "*" else ""

    s"""<label for="${cToCamel(name)}" class="sr-only" ngbButtonLabel i18n>${cToCamel(name)}${asterisk}:</label>""".stripMargin
  }

  def textForField(name: String, _type: String, length: String, required: String): String = {
      s"""<input type="text" id="${cToCamel(name)}" ngbButton formControlName="${cToCamel(name)}" aria-describedby="${cToCamel(name)}Help" i18n-placeholder placeholder="input${cToPascal(name)}" class="form-control">""".stripMargin
  }

  def checkboxForField(name: String, _type: String, length: String, required: String): String = {
    s"""<input type="checkbox" id="${cToCamel(name)}" formControlName="${cToCamel(name)}" value="${cToCamel(name)}" aria-describedby="${cToCamel(name)}Help" i18n-placeholder placeholder="input${cToPascal(name)}" class="form-control"><span i18n>${cToCamel(name)}</span>""".stripMargin
  }

  def textAreaForField(name: String, _type: String, length: String, required: String): String = {
    s"""<textarea id="${cToCamel(name)}" formControlName="${cToCamel(name)}" aria-describedby="${cToCamel(name)}Help" i18n-placeholder placeholder="input${cToPascal(name)}" class="md-textarea form-control">
       |</textarea>""".stripMargin
  }

  def selectForField(name: String, _type: String, length: String, required: String): String = {
    val prelude = s"""<select id="${cToCamel(name)}" ngbButton formControlName="${cToCamel(name)}" aria-describedby="${cToCamel(name)}Help" i18n-placeholder placeholder="input${cToPascal(name)}" class="form-control">""".stripMargin
    val options = entityFor(model, _type).child
      .filter(p => p.label == "row")
      .map(f => (f.\@("id"), f.\@("name")))
      .map(f => s"""<option value="${f._1}" i18n>${f._2}</option>""".stripMargin)
      .reduce((x, y) => "%s\n%s".format(x, y))
    val end = "</select>"

    "%s\n%s\n%s".format(prelude, indent(options, 2, true), end)
  }

  def datetimeForField(name: String, _type: String, length: String, required: String): String = {
    textForField(name, _type, length, required)
  }

}
