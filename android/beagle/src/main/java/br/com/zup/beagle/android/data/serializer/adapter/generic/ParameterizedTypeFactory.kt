/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

<<<<<<< HEAD:android/automated-tests/src/main/java/com/example/automated_tests/AppApplication.kt
package com.example.automated_tests

import android.app.Application

class AppApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        BeagleSetup().init(this)
=======
package br.com.zup.beagle.android.data.serializer.adapter.generic

import com.squareup.moshi.internal.Util
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ParameterizedTypeFactory {

    fun new(rawType: Type, vararg typeArguments: Type?): ParameterizedType? {
        require(typeArguments.isNotEmpty()) { "Missing type arguments for $rawType" }
        return Util.ParameterizedTypeImpl(null, rawType, *typeArguments)
>>>>>>> c69b1202d14378cc079d08e6ee8d1acd56fb260a:android/beagle/src/main/java/br/com/zup/beagle/android/data/serializer/adapter/generic/ParameterizedTypeFactory.kt
    }

}