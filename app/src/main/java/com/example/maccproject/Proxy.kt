package com.example.maccproject

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Proxy {
    companion object{
        fun getUserData(googleId: String): JSONObject? {
            val name = "http://Gabbo.pythonanywhere.com/user/$googleId"
            val url = URL(name)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "GET"
                    if(responseCode == 404) return null
                    Log.v("GET", responseCode.toString())
                    Log.v("GET", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()
                    return JSONObject(getin)
                }
            }  catch (e: Exception) {
                //e.printStackTrace()
                Log.v("GET", e.toString())
                return null
            }
        }

        fun sendNewUser(name:String, email: String, googleId: String): JSONObject?{
            val uname = "http://Gabbo.pythonanywhere.com/user"
            val url = URL(uname)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")

                    try {
                        val out = OutputStreamWriter(outputStream)
                        out.write(JSONObject("{'name': '$name', 'email': '$email', 'googleId': '$googleId'}").toString())
                        out.flush()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    if(responseCode == 404) return null
                    Log.v("POST", responseCode.toString())
                    Log.v("POST", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()

                    return JSONObject(getin)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.v("POST", e.toString())
                return null
            }
        }

        fun addNickname(googleId: String, nickname: String): JSONObject? {
            val uname = "http://Gabbo.pythonanywhere.com/user/$googleId"
            val url = URL(uname)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "PUT"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    try {
                        val out = OutputStreamWriter(outputStream)
                        out.write(JSONObject("{'nickname': '$nickname'}").toString())
                        out.flush()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    Log.v("PUT", responseCode.toString())
                    Log.v("PUT", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()
                    return JSONObject(getin)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.v("PUT", e.toString())
                return null
            }
        }

        fun addScore(googleId: String, points: Int): JSONObject?{
            val uname = "http://Gabbo.pythonanywhere.com/leaderboard"
            val url = URL(uname)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    try {
                        val out = OutputStreamWriter(outputStream)
                        out.write(JSONObject("{'id': ${user?.id}, 'googleId': '${user?.googleId}', 'points': '$points'}").toString())
                        out.flush()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    Log.v("POST", responseCode.toString())
                    Log.v("POST", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()
                    return JSONObject(getin)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.v("POST", e.toString())
                return null
            }
        }

        fun getLeaderboard(): JSONArray? {
            val name = "http://Gabbo.pythonanywhere.com/leaderboard"
            val url = URL(name)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "GET"
                    if(responseCode == 404) return null
                    Log.v("GET", responseCode.toString())
                    Log.v("GET", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()
                    return JSONArray(getin)
                }
            }  catch (e: Exception) {
                e.printStackTrace()
                Log.v("GET", e.toString())
                return null
            }
        }

        fun getUserScore(googleId: String): JSONObject?{
            val name = "http://Gabbo.pythonanywhere.com/leaderboard/$googleId"
            val url = URL(name)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.run {
                    requestMethod = "GET"
                    if(responseCode == 404) return null
                    Log.v("GET", responseCode.toString())
                    Log.v("GET", responseMessage.toString())
                    val getin = InputStreamReader(inputStream).readText()
                    return JSONObject(getin)
                }
            }  catch (e: Exception) {
                //e.printStackTrace()
                Log.v("GET", e.toString())
                return null
            }
        }
    }
}