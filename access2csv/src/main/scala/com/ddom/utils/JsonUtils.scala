package com.ddom.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.io.File

object JsonUtils {

  @transient lazy val instance: ObjectMapper = {
    initJacksonMapper()
  }

  //initialize json mapper
  private def initJacksonMapper(): ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def readFromFile[T](filePath:String, classType: Class[T]): T = {
    val deRec = instance.readValue(new File(filePath), classType)
    deRec
  }

  def readFromString[T](jsonString:String, classType: Class[T]): T = {
    val deRec = instance.readValue(jsonString, classType)
    deRec
  }
}
