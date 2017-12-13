/* Copyright (C) 2013-2017 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.parser;

import lombok.AccessLevel;
import lombok.Getter;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class FSM2MealyParser<I, O> extends FSMParser<I> {

    /**
     * A Function that transform strings from the FSM source to actual output.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Function<String, O> outputParser;

    /**
     * A map of transitions for the Mealy machine.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Map<Pair<Integer, I>, Pair<O, Integer>> transitions = new HashMap();

    /**
     * A set of states for the Mealy machine.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Set<Integer> states = new HashSet();

    /**
     * Constructs a new FSM2MealyParser.
     *
     * @param reader the reader
     * @param inputParser the input parser (see {@link #inputParser}).
     * @param outputParser the output parser (similar to {@code inputParser}).
     */
    protected FSM2MealyParser(Reader reader, Function<String, I> inputParser, Function<String, O> outputParser) {
        super(reader, inputParser);
        this.outputParser = outputParser;
    }

    /**
     * Constructs the actual {@link net.automatalib.automata.transout.MealyMachine}, using {@link #states}, and
     * {@link #transitions}.
     *
     * @return the Mealy machine defined in the FSM source.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected CompactMealy<I, O> parseMealy() throws FSMParseException, IOException {

        parse();

        // create the alphabet
        final Alphabet<I> alphabet = Alphabets.fromCollection(getInputs());

        // create a CompactMealy
        final CompactMealy<I, O> mealy = new CompactMealy(alphabet);

        // create a mapping states in the FSM source to states in the CompactMealy
        final Map<Integer, Integer> stateMap = new HashMap();

        // set the initial state
        mealy.setInitialState(stateMap.computeIfAbsent(getInitialState(), i -> mealy.addState()));

        // iterate over all transitions, add them to the CompactMealy
        for (Map.Entry<Pair<Integer, I>, Pair<O, Integer>> transition : getTransitions().entrySet()) {
            final Integer from = stateMap.computeIfAbsent(transition.getKey().getFirst(), i -> mealy.addState());
            final Integer to = stateMap.computeIfAbsent(transition.getValue().getSecond(), i -> mealy.addState());

            final I i = transition.getKey().getSecond();
            final O o = transition.getValue().getFirst();

            mealy.addTransition(from, i, to, o);
        }

        return mealy;
    }
}
