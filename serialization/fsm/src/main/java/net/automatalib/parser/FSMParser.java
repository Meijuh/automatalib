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
import lombok.Setter;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_EOL;
import static java.io.StreamTokenizer.TT_WORD;
import static net.automatalib.parser.FSMParser.Part.DataDefinition;
import static net.automatalib.parser.FSMParser.Part.StateVectors;
import static net.automatalib.parser.FSMParser.Part.Transitions;

/**
 * This class provides methods to parse automata in FSM format.
 *
 * The FSM is parsed by means of a tokenizer (a grammar is not used).
 *
 * @see http://www.win.tue.nl/vis1/home/apretori/data/fsm.html
 *
 * @author Jeroen Meijer
 */
abstract class FSMParser<I> {

    /**
     * An enumeration for the three parts in the FSM file.
     */
    protected enum Part {DataDefinition, StateVectors, Transitions}

    // some messages for FSMParseExceptions.
    public static final String NO_SUCH_STATE = "state with number %d is undefined";
    public static final String NON_DETERMINISM_DETECTED = "non-determinism detected (previous value: %s)";
    public static final String INITIAL_STATES = "multiple initial states found: %s";
    public static final String INITIAL_STATE = "no initial state found";
    public static final String EXPECT_CHAR = "expected char '%c' not found";
    public static final String EXPECT_NUMBER = "number expected";
    public static final String EXPECT_IDENTIFIER= "expecting identifier";
    public static final String EXPECT_STRING = "expecting string";

    private final Reader reader;

    /**
     * The function that transforms strings in the FSM file to input.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Function<String, I> inputParser;

    /**
     * The current line that is being parsed in a specific part.
     */
    @Getter(AccessLevel.PROTECTED)
    private int partLineNumber;

    /**
     * The StreamTokenizer, that tokenizes tokens in the FSM file.
     */
    @Getter(AccessLevel.PROTECTED)
    private final StreamTokenizer streamTokenizer;

    /**
     * The set that contains all inputs that end up in the input alphabet.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Set<I> inputs = new HashSet();

    /**
     * The initial state.
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private Integer initialState = -1;

    /**
     * Constructs a new FSMParser and defines all possible tokens.
     *
     * @param reader the Reader
     * @param inputParser the Function that parses strings in the FSM file to input.
     */
    protected FSMParser(Reader reader, Function<String, I> inputParser) {
        this.inputParser = inputParser;
        this.reader = reader;
        streamTokenizer = new StreamTokenizer(reader);
        streamTokenizer.resetSyntax();
        streamTokenizer.wordChars('a', 'z');
        streamTokenizer.wordChars('A', 'Z');
        streamTokenizer.wordChars('-', '-');
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.wordChars('0', '9');
        streamTokenizer.wordChars(128 + 32, 255);
        streamTokenizer.whitespaceChars(0, ' ');
        streamTokenizer.quoteChar('"');
        streamTokenizer.eolIsSignificant(true);
        streamTokenizer.ordinaryChar('(');
        streamTokenizer.ordinaryChar(')');
    }

    /**
     * Parse a data definition.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void parseDataDefinition() throws FSMParseException, IOException;

    /**
     * Perform some actions after all data definitions have been parsed.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void checkDataDefinitions() throws FSMParseException, IOException;

    /**
     * Parse a state vector.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void parseStateVector() throws FSMParseException, IOException;

    /**
     * Perform some actions after all state vectors have been parsed.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void checkStateVectors() throws FSMParseException, IOException;

    /**
     * Parse a transition.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void parseTransition() throws FSMParseException, IOException;

    /**
     * Perform some actions after all transitions have been parsed.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected abstract void checkTransitions() throws FSMParseException, IOException;

    /**
     * Parsed the FSM file line-by-line.
     * At first this method expects to parse data definitions, and calls {@link #parseDataDefinition()} for each data
     * definition. After "---" is encountered {@link #checkDataDefinitions()} is called, and this method expects to
     * parse state vectors. The behavior is similar for state vectors and transitions.
     * For each line this method will increment {@link #partLineNumber}, and reset it when a new part in the FSM file
     * begins.
     *
     * Note that {@link StreamTokenizer} allows one to push back tokens. This is used whenever we have checked type
     * type of token we are going to read.
     *
     * @throws FSMParseException
     * @throws IOException
     */
    protected void parse() throws FSMParseException, IOException {
        FSMParser.Part part = DataDefinition;
        partLineNumber = 0;

        while(streamTokenizer.nextToken() != TT_EOF) {
            streamTokenizer.pushBack();
            switch (part) {
                case DataDefinition: {
                    if (streamTokenizer.nextToken() == TT_WORD && streamTokenizer.sval.equals("---")) {
                        // we entered the part with the state vectors
                        part = StateVectors;
                        partLineNumber = 0;
                        checkDataDefinitions();
                    } else {
                        streamTokenizer.pushBack();
                        parseDataDefinition();
                    }
                    break;
                }
                case StateVectors: {
                    if (streamTokenizer.nextToken() == TT_WORD && streamTokenizer.sval.equals("---")) {
                        // we entered the part with the transitions.
                        part = Transitions;
                        partLineNumber = 0;
                        checkStateVectors();
                    } else {
                        streamTokenizer.pushBack();
                        parseStateVector();
                    }
                    break;
                }
                case Transitions: {
                    parseTransition();
                    break;
                }
            }
            // consume all tokens until EOL is reached
            while (streamTokenizer.nextToken() != TT_EOL) {}
            partLineNumber++;
        }
        checkTransitions();
        reader.close();
    }
}
