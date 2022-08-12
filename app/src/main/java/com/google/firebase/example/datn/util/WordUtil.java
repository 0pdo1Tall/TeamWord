/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.google.firebase.example.datn.util;

import android.text.TextUtils;

import com.google.firebase.example.datn.model.Word;

import java.util.Locale;
import java.util.Random;

public class WordUtil {

    private static final String TAG = "WordUtil";

    private static final String WORD_URL_FMT = "https://storage.googleapis.com/main-api-356704.appspot.com/pic_%d.jpg";

    private static final int MAX_IMAGE_NUM = 10;

    public static String getCleanWord(String s){
        return s.toLowerCase().replaceAll("\\s","");
    }

    //
    public static String getRandomImageUrl(Random random) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        return String.format(Locale.getDefault(), WORD_URL_FMT, id);
    }

    public static String getStringFirstCapital(String s){
        if(s.length() > 0 && !TextUtils.isEmpty(s.substring(0,1))){
            return s.substring(0,1).toUpperCase() + s.substring(1);
        }
        return s;
    }
}
