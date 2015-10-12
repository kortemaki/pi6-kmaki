/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package annotators;

import java.lang.reflect.Method;

import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

import type.Metric;
import type.OutputAnnotation;
import type.PAtN;
import type.Performance;
import type.Question;
import util.RTMethod;
import util.TypeUtils;
import util.TOPWrapper;

/**
 * A simple scoring annotator for PI3.
 * 
 * Expects each CAS to contain at least one NgramAnnotation.
 * Processes each NgramAnnotation by adding a corresponding AnswerScoringAnnotation to the CAS.
 * 
 * This annotator has no parameters and requires no initialization method.
 */

public class OutputAnnotator extends CasAnnotator_ImplBase {	
	
	Method getName;
	Method compField;
	RTMethod isMetric;
	RTMethod isPAtN;
	Method getN;
	RTMethod hasN1;
	RTMethod hasNamePAt1;
	RTMethod isPAt1;
	RTMethod hasN5;
	RTMethod hasNamePAt5;
	RTMethod isPAt5;
	RTMethod hasNameRR;
	RTMethod isRR;
	RTMethod hasNameAP;
	RTMethod isAP;
	
	public void initialize()
	{
		System.out.print("Initializing Output Annotator... ");
		getName = TypeUtils.instantiateMethod(PAtN.class, "getMetricName");
        compField = TypeUtils.instantiateMethod(TOPWrapper.class, "compareField", Method.class, Comparable.class);
        isMetric = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "assertClass", Class.class), Metric.class);
        isPAtN = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "assertClass", Class.class), PAtN.class);
        getN = TypeUtils.instantiateMethod(PAtN.class, "getN");
        hasN1 = new RTMethod(compField, getN, 1);
        hasNamePAt1 = new RTMethod(compField, getName, "Precision at 1.");
        isPAt1 = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "logicalAnd", RTMethod[].class), isPAtN, hasN1, hasNamePAt1);
        hasN5 = new RTMethod(compField, getN, 5);
        hasNamePAt5 = new RTMethod(compField, getName, "Precision at 5.");
        isPAt5 = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "logicalAnd", RTMethod[].class), isPAtN, hasN5, hasNamePAt5);
        hasNameRR = new RTMethod(compField, getName, "Reciprocal rank.");
        isRR = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "logicalAnd", RTMethod[].class), isMetric, hasNameRR);
        hasNameAP = new RTMethod(compField, getName, "Average precision.");
        isAP = new RTMethod(TypeUtils.instantiateMethod(TOPWrapper.class, "logicalAnd", RTMethod[].class), isMetric, hasNameAP);
        System.out.println("done.");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void process(CAS aCas) throws AnalysisEngineProcessException {
		
		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
		// Get the Scoring Annotations for each Test Element in the document
		FSIndex<Performance> performanceIndex = (FSIndex) (jcas.getAnnotationIndex(Performance.type));

		// Iterate over them in sequence
		for(Performance performance : performanceIndex)
		{				
	        Question question = performance.getTestElement().getQuestion();
	        
	        ///////////////////////////////////////////
	        // Extract performance metrics of interest
	        FSList metrics = performance.getMetrics();
	        
	        // Precision@1
	        float pAt1 = ((PAtN) TypeUtils.getFromFSList(metrics,isPAt1)).getValue();
	        
	        //Precision@5 
	        float pAt5 = ((PAtN) TypeUtils.getFromFSList(metrics,isPAt5)).getValue();
	        
	        //Reciprocal Rank
	        float rr = ((Metric) TypeUtils.getFromFSList(metrics,isRR)).getValue();
	        
	        //Average Precision
	        float ap = ((Metric) TypeUtils.getFromFSList(metrics,isAP)).getValue();
	        
	        String text = String.format("%s,%.3f,%.3f,%.3f,%.3f", question.getId(), pAt1, pAt5, rr, ap);	        
			
	        /**

	        // TODO: Calculate actual precision, recall and F1
	        double precision = 0.0;
	        double recall = 0.0;
	        double f1 = 0.0;
	        "%s,%d,%d,%d,%.3f,%.3f,%.3f\n", question.getId(), m.getTp(), m.getFn(), m.getFp(),
	          precision, recall, f1
	        */
	        
			OutputAnnotation output = new OutputAnnotation(jcas);
			output.setComponentId(this.getClass().getName());
			output.setBegin(performance.getBegin());
			output.setEnd(performance.getEnd());
			output.setOrig(performance.getTestElement());
			output.setText((performance.getTestElement()).getQuestion().getId());
			output.setOutput(text);
			output.addToIndexes();
		}
	}
}
