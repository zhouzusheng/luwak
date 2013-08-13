package uk.co.flax.luwak.presearcher;/*
 * Copyright (c) 2013 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.AbstractAnalysisFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.lucene.analysis.util.AbstractAnalysisFactory.LUCENE_MATCH_VERSION_PARAM;

public class TermsEnumBooleanQuery {

    private static final Map<String, String> STD_VERSION_PARAMS = new HashMap<>();
    static {
        STD_VERSION_PARAMS.put(LUCENE_MATCH_VERSION_PARAM, Version.LUCENE_50.toString());
    }

    private static final TokenFilterFactory PASS_THROUGH_FILTER_FACTORY
            = new StandardFilterFactory(STD_VERSION_PARAMS);

    public static Query createFrom(AtomicReader reader) throws IOException {
        return createFrom(reader, PASS_THROUGH_FILTER_FACTORY);
    }

    public static Query createFrom(AtomicReader reader, TokenFilterFactory tfFactory) throws IOException {

        BooleanQuery bq = new BooleanQuery();

        for (String field : reader.fields()) {
            TermsEnum te = reader.terms(field).iterator(null);
            TokenStream ts = new DuplicateRemovalTokenFilter(
                    tfFactory.create(new TermsEnumTokenStream(te)));
            final CharTermAttribute cht = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                bq.add(new TermQuery(new Term(field, cht.toString())), BooleanClause.Occur.SHOULD);
            }
        }

        return bq;
    }
}
