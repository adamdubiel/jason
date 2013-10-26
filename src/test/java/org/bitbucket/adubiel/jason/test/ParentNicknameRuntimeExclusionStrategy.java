/*
 * Copyright 2013 Adam Dubiel, Przemek Hertel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.adubiel.jason.test;

import com.google.gson.RuntimeExclusionStrategy;
import com.google.gson.reflect.TypeToken;
import org.bitbucket.adubiel.jason.test.model.Parent;

/**
 *
 * @author Adam Dubiel
 */
public class ParentNicknameRuntimeExclusionStrategy implements RuntimeExclusionStrategy {

    @Override
    public boolean skipField(TypeToken<?> token, String fieldName) {
        if (token.getType().equals(Parent.class) && fieldName.equals("nickName")) {
            return true;
        }
        return false;
    }
}
