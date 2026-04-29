package io.limao996.hoohoolib.utils

import android.util.Log

private fun makeLogMsg(vararg args: Any?) = args.joinToString("    ")

fun verboseLog(vararg args: Any?) = Log.v("PluginLog", makeLogMsg(*args))
fun debugLog(vararg args: Any?) = Log.d("PluginLog", makeLogMsg(*args))
fun infoLog(vararg args: Any?) = Log.i("PluginLog", makeLogMsg(*args))
fun warnLog(vararg args: Any?) = Log.w("PluginLog", makeLogMsg(*args))
fun errorLog(vararg args: Any?) = Log.e("PluginLog", makeLogMsg(*args))
fun errorLog(throwable: Throwable?, vararg args: Any?) =
    Log.e("PluginLog", makeLogMsg(*args), throwable)

fun fatalLog(vararg args: Any?) = Log.wtf("PluginLog", makeLogMsg(*args))