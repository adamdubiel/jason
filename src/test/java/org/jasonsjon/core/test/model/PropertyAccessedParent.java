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
package org.jasonsjon.core.test.model;

import org.jasonjson.core.AccessStrategyType;
import org.jasonjson.core.JasonAccess;

/**
 *
 * @author Adam Dubiel
 */
@JasonAccess(strategy = AccessStrategyType.PROPERTY)
public class PropertyAccessedParent {

    private String name;

    public PropertyAccessedParent(String name) {
        this.name = name;
    }

    public String getGetterName() {
        return name;
    }
}
