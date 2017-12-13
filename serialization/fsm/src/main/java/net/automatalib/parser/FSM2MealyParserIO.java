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

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Parse a Mealy machine from an FSM source, with straightforward edge semantics.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public final class FSM2MealyParserIO<I, O> extends FSM2MealyParser<I, O> {

    /**
     * Constructs a new FSM2MealyParserIO. Use one of the static parse() methods to actually parse an FSM source.
     *
     * @param reader the Reader.
     * @param inputParser the input parser (see {@link #inputParser}).
     * @param outputParser the output parser (similar to {@code inputParser}.
     */
    private FSM2MealyParserIO(Reader reader, Function<String, I> inputParser, Function<String, O> outputParser) {
        super(reader, inputParser, outputParser);
    }

    /**
     * We do not need to parse data definitions.
     */
    @Override
    protected void parseDataDefinition() {

    }

    /**
     * Do not need to check them either.
     */
    @Override
    protected void checkDataDefinitions() {

    }

    /**
     * Add a state for a line.
     */
    @Override
    protected void parseStateVector() {
        getStates().add(getPartLineNumber());
    }

    /**
     * Skip any checks.
     */
    @Override
    protected void checkStateVectors() {

    }

    /**
     * Parse a transition.
     *
     * @throws FSMParseException when the transition is illegal.
     * @throws IOException
     */
    @Override
    protected void parseTransition() throws FSMParseException, IOException {
        try {

            // check we will read a state index
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the source state index
            int from = Integer.valueOf(getStreamTokenizer().sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(from)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, from), getStreamTokenizer());
            }

            // check we will read a state index
            if (getStreamTokenizer().nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMParseException(EXPECT_NUMBER, getStreamTokenizer());
            }

            // read the target state index
            int to = Integer.valueOf(getStreamTokenizer().sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(to)) {
                throw new FSMParseException(String.format(NO_SUCH_STATE, to), getStreamTokenizer());
            }

            // check we will read an input edge label
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the input, and convert the input string to actual input
            final I input = getInputParser().apply(getStreamTokenizer().sval);

            // add it to the set of inputs
            getInputs().add(input);

            // check we will read an output edge label
            if (getStreamTokenizer().nextToken() != '"') {
                throw new FSMParseException(EXPECT_STRING, getStreamTokenizer());
            }

            // read the output, and convert the output string to actual output
            final O output = getOutputParser().apply(getStreamTokenizer().sval);

            // create the Mealy machine transition
            final Pair<O, Integer> prev = getTransitions().put(Pair.make(from, input), Pair.make(output, to));

            // check for non-determinism
            if (prev != null) {
                throw new FSMParseException(String.format(NON_DETERMINISM_DETECTED, prev), getStreamTokenizer());
            }
        } catch (NumberFormatException nfe) {
            throw new FSMParseException(nfe, getStreamTokenizer());
        }
    }

    /**
     * Create the initial state.
     *
     * @throws FSMParseException when there is no, or more than one initial state.
     */
    @Override
    protected void checkTransitions() throws FSMParseException {
        final Set<Integer> initialStates = new HashSet(getStates());
        for (Pair<O, Integer> target: getTransitions().values()) initialStates.remove(target.getSecond());
        if (initialStates.size() > 1) throw new FSMParseException(String.format(INITIAL_STATES, initialStates));
        if (initialStates.isEmpty()) throw new FSMParseException(INITIAL_STATE);
        setInitialState(initialStates.iterator().next());
    }

    public static <I, O> CompactMealy parse(Reader reader, Function<String, I> inputParser, Function<String, O> outputParser) throws
            IOException, FSMParseException {
        return new FSM2MealyParserIO(reader, inputParser, outputParser).parseMealy();
    }

    public static <I, O> CompactMealy parse(File file, Function<String, I> inputParser, Function<String, O> outputParser) throws
            IOException, FSMParseException {
        return parse(new FileReader(file), inputParser, outputParser);
    }

    public static <I, O> CompactMealy parse(String string, Function<String, I> inputParser, Function<String, O> outputParser) throws
            IOException, FSMParseException {
        return parse(new StringReader(string), inputParser, outputParser);
    }

    public static <I, O> CompactMealy parse(InputStream inputStream, Function<String, I> inputParser, Function<String, O> outputParser)
            throws IOException, FSMParseException {
        return parse(new InputStreamReader(inputStream), inputParser, outputParser);
    }

    public static <E> CompactMealy parse(Reader reader, Function<String, E> edgeParser) throws IOException, FSMParseException {
        return parse(reader, edgeParser, edgeParser);
    }

    public static <E> CompactMealy parse(File file, Function<String, E> edgeParser) throws IOException, FSMParseException {
        return parse(file, edgeParser, edgeParser);
    }

    public static <E> CompactMealy parse(String string, Function<String, E> edgeParser) throws IOException, FSMParseException {
        return parse(string, edgeParser, edgeParser);
    }

    public static <E> CompactMealy parse(InputStream inputStream, Function<String, E> edgeParser) throws IOException, FSMParseException {
        return parse(inputStream, edgeParser, edgeParser);
    }
}
