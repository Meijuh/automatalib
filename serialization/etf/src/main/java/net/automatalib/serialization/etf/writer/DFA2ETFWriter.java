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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

import java.io.*;

/**
 * Write a DFA to ETF.
 *
 * @author Jeroen Meijer
 *
 * @param <S> the state type.
 * @param <I> the input type.
 */
public class DFA2ETFWriter<S, I> extends ETFWriter<I, DFA<S, I>> {

    /**
     * Constructs a new DFA2ETFWriter. Writing a DFA should be done with one of the static write methods.
     *
     * @param writer the Writer.
     */
    private DFA2ETFWriter(Writer writer) {
        super(writer);
    }

    /**
     * Writes the type of the edge. A DFA edge contains one label, named 'letter', of type 'letter.
     */
    @Override
    protected void writeEdge() {
        getPrintWriter().println("begin edge");
        getPrintWriter().println("letter:letter");
        getPrintWriter().println("end edge");
    }

    /**
     * Write DFA specific parts in the ETF.
     *
     *  - initial state,
     *  - the valuations for the state 'id',
     *  - the letters in the alphabet,
     *  - the transitions,
     *  - the state labels (rejecting/accepting),
     *  - the mapping from states to state labels.
     *
     * @param dfa the DFA to write.
     * @param inputs the alphabet.
     */
    @Override
    protected void writeETF(DFA<S, I> dfa, Alphabet<I> inputs) {
        // write the initial state
        getPrintWriter().println("begin init");
        getPrintWriter().printf("%d%n", dfa.stateIDs().getStateId(dfa.getInitialState()));
        getPrintWriter().println("end init");

        // write the valuations of the state ids
        getPrintWriter().println("begin sort id");
        for (S s : dfa.getStates()) getPrintWriter().printf("\"%s\"%n", s);
        getPrintWriter().println("end sort");

        // write the letters from the alphabet
        getPrintWriter().println("begin sort letter");
        for (I i : inputs) {
            getPrintWriter().print("\"");
            getPrintWriter().print(i);
            getPrintWriter().println("\"");
        }
        getPrintWriter().println("end sort");

        // write the transitions
        getPrintWriter().println("begin trans");
        for (S s : dfa.getStates()) {
            for (I i : inputs) {
                S t = dfa.getSuccessor(s, i);
                if (t != null) {
                    getPrintWriter().printf(
                            "%d/%d %d%n",
                            dfa.stateIDs().getStateId(s),
                            dfa.stateIDs().getStateId(t),
                            inputs.getSymbolIndex(i));
                }
            }
        }
        getPrintWriter().println("end trans");

        // write the two state label valuations
        getPrintWriter().println("begin sort label");
        getPrintWriter().println("\"reject\"");
        getPrintWriter().println("\"accept\"");
        getPrintWriter().println("end sort");

        // write the state labels for each state, e.g. whether it is accepting/rejecting.
        getPrintWriter().println("begin map label:label");
        for (S s : dfa.getStates()) {
            final int stateId = dfa.stateIDs().getStateId(s);
            getPrintWriter().printf("%d %d%n", stateId, dfa.isAccepting(s) ? 1 : 0);
        }
        getPrintWriter().println("end map");
    }

    public static void write(Writer writer, DFA dfa, Alphabet inputs) {
        new DFA2ETFWriter(writer).write(dfa, inputs);
    }

    public static void write(File file, DFA dfa, Alphabet inputs) throws IOException {
        write(new FileWriter(file), dfa, inputs);
    }

    public static void write(OutputStream outputStream, DFA dfa, Alphabet inputs) throws IOException {
        write(new OutputStreamWriter(outputStream), dfa, inputs);
    }
}
