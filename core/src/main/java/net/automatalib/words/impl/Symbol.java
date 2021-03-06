/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.words.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.EqualsAndHashCode;
import net.automatalib.words.abstractimpl.AbstractSymbol;

@ParametersAreNonnullByDefault
@EqualsAndHashCode(callSuper = false)
public class Symbol extends AbstractSymbol<Symbol> {

    @Nullable
    private final Object userObject;

    public Symbol(@Nullable Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public int compareTo(@Nonnull Symbol o) {
        return getId() - o.getId();
    }

    @Nullable
    public Object getUserObject() {
        return userObject;
    }

    @Override
    @Nonnull
    public String toString() {
        return String.valueOf(userObject);
    }

}
