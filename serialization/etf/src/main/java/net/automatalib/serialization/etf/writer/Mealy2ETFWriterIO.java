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
package net.automatalib.serialization.etf.writer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

import java.io.*;

/**
 * Write a Mealy machine with straightforward IO semantics.
 *
 * @author Jeroen Meijer
 *
 * @param <S> the state type
 * @param <I> the input type
 * @param <T> the transition type
 * @param <O> the output type
 */
public class Mealy2ETFWriterIO<S, I, T, O> extends ETFWriter<I, MealyMachine<S, I, T, O>> {

    private Mealy2ETFWriterIO(Writer writer) {
        super(writer);
    }

    /**
     * Write the edge type. An edge has two edge labels: input of type input, and output of type output.
     */
    @Override
    protected void writeEdge() {
        getPrintWriter().println("begin edge");
        getPrintWriter().println("input:input");
        getPrintWriter().println("output:output");
        getPrintWriter().println("end edge");
    }

    /**
     * Write ETF parts specific for Mealy machines with IO semantics.
     *
     * Writes:
     *  - the initial state,
     *  - the valuations for the state ids,
     *  - the transitions,
     *  - the input alphabet (for the input labels on edges),
     *  - the output alphabet (for the output labels on edges).
     *
     * @param mealy the Mealy machine to write.
     * @param inputs the alphabet.
     */
    @Override
    protected void writeETF(MealyMachine<S, I, T, O> mealy, Alphabet<I> inputs) {
        // write the initial state
        getPrintWriter().println("begin init");
        getPrintWriter().printf("%d%n", mealy.stateIDs().getStateId(mealy.getInitialState()));
        getPrintWriter().println("end init");

        // write the state ids
        getPrintWriter().println("begin sort id");
        for (S s : mealy.getStates()) getPrintWriter().printf("\"%s\"%n", s);
        getPrintWriter().println("end sort");

        // create a new bi-map that contains indices for the output alphabet
        final BiMap<O, Integer> outputIndices = HashBiMap.create();

        // write the transitions
        getPrintWriter().println("begin trans");
        for (S s : mealy.getStates()) {
            for (I i : inputs) {
                final T t = mealy.getTransition(s, i);
                if (t != null) {
                    final O o = mealy.getTransitionOutput(t);
                    outputIndices.computeIfAbsent(o, ii -> outputIndices.size());
                    final S n = mealy.getSuccessor(t);
                    getPrintWriter().printf("%s/%s %d %d%n",
                            mealy.stateIDs().getStateId(s),
                            mealy.stateIDs().getStateId(n),
                            inputs.getSymbolIndex(i),
                            outputIndices.get(o));
                }
            }
        }
        getPrintWriter().println("end trans");

        // write the letters in the input alphabet
        getPrintWriter().println("begin sort input");
        for (I i : inputs) getPrintWriter().printf("\"%s\"%n", i);
        getPrintWriter().println("end sort");

        // write the letters in the output alphabet
        getPrintWriter().println("begin sort output");
        for (int i = 0; i < outputIndices.size(); i++) {
            getPrintWriter().printf("\"%s\"%n", outputIndices.inverse().get(i));
        }
        getPrintWriter().println("end sort");
    }

    public static void write(Writer writer, MealyMachine mealy, Alphabet inputs) {
        new Mealy2ETFWriterIO(writer).write(mealy, inputs);
    }

    public static void write(File file, MealyMachine mealy, Alphabet inputs) throws IOException {
        write(new FileWriter(file), mealy, inputs);
    }

    public static void write(OutputStream outputStream, MealyMachine mealy, Alphabet inputs) {
        write(new OutputStreamWriter(outputStream), mealy, inputs);
    }
}
