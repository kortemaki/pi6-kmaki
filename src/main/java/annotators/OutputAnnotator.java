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

import org.apache.uima.UimaContext;
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
import util.CheckMethod;
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
	Method getN;
	
	CheckMethod hasN1;
	CheckMethod hasNamePAt1;
	CheckMethod[] isPAt1;
	
	CheckMethod hasN5;
	CheckMethod hasNamePAt5;
	CheckMethod[] isPAt5;
	
	CheckMethod hasNameRR;
	CheckMethod[] isRR;
	
	CheckMethod hasNameAP;
	CheckMethod[] isAP;
	
	@Override
	public void initialize(UimaContext aContext)
	{	
		
		System.out.print("Initializing Output Annotator... ");
		getName = TypeUtils.instantiateMethod(PAtN.class, "getMetricName");
        getN = TypeUtils.instantiateMethod(PAtN.class, "getN");
        
        hasN1 = new CheckMethod(getN, 1);
        hasNamePAt1 = new CheckMethod(getName, "Precision at 1.");
        isPAt1 = new CheckMethod[]{hasN1, hasNamePAt1};
        
        hasN5 = new CheckMethod(getN, 5);
        hasNamePAt5 = new CheckMethod(getName, "Precision at 5.");
        isPAt5 = new CheckMethod[]{hasN5, hasNamePAt5};
        
        hasNameRR = new CheckMethod(getName, "Reciprocal rank.");
        isRR = new CheckMethod[]{hasNameRR};
        
        hasNameAP = new CheckMethod(getName, "AveragePrecision");
        isAP = new CheckMethod[]{hasNameAP};
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
	        
			System.out.println("    Finalizing output for document " + question.getId() + ".");
	        
	        ///////////////////////////////////////////
	        // Extract performance metrics of interest
	        FSList metrics = performance.getMetrics();
	        
	        if(isPAt1 == null)
	        {
	        	System.out.println("**********************NULL isPAt1************************");
	        }
	        if(isPAt5 == null)
	        {
	        	System.out.println("**********************NULL isPAt5************************");
	        }
	        if(isRR == null)
	        {
	        	System.out.println("***********************NULL isRR*************************");
	        }
	        if(isAP == null)
	        {
	        	System.out.println("***********************NULL isAP*************************");
	        }
	        // Precision@1
	        PAtN pat1 = ((PAtN) TypeUtils.getFromFSList(metrics,PAtN.class,hasN1));
	        float pAt1 = (float) 0;
	        if(pat1 != null)
	        {
	        	pAt1 = pat1.getValue();
	            System.out.println("      Finished p@1 output for document " + question.getId() + ".");
	        }
	        else
	        {
	        	System.out.println("******Could not find p@1 output for document " + question.getId() + ".");
	        }
	        
	        //Precision@5 
	        PAtN pat5 = ((PAtN) TypeUtils.getFromFSList(metrics,PAtN.class,isPAt5));
	        float pAt5 = (float) 0;
	        if(pat5 != null)
	        {
	        	pAt5 = pat5.getValue();
	            System.out.println("      Finished p@5 output for document " + question.getId() + ".");
	        }
	        else
	        {
	        	System.out.println("******Could not find p@5 output for document " + question.getId() + ".");
	        }
	        
	        //Reciprocal Rank
	        float rr = ((Metric) TypeUtils.getFromFSList(metrics,Metric.class,isRR)).getValue();
	        System.out.println("      Finished rr output for document " + question.getId() + ".");
	        
	        //Average Precision
	        float ap = ((Metric) TypeUtils.getFromFSList(metrics,Metric.class,isAP)).getValue();
	        System.out.println("      Finished ap output for document " + question.getId() + ".");
	        
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
			
			System.out.println("Finished output for document " + question.getId() + ".");
		}
	}
}
