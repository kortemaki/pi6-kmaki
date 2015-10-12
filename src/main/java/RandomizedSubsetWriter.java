import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import type.OutputAnnotation;
import util.RandomUtils;

/**
 * This CAS Consumer generates the report file with the method metrics
 */
public class RandomizedSubsetWriter extends CasConsumer_ImplBase {
  final String PARAM_OUTPUTDIR = "OutputDir";

  final String OUTPUT_FILENAME = "ErrorAnalysis.csv";

  File mOutputDir;

  @Override
  public void initialize() throws ResourceInitializationException {
    String mOutputDirStr = (String) getConfigParameterValue(PARAM_OUTPUTDIR);
    if (mOutputDirStr != null) {
      mOutputDir = new File(mOutputDirStr);
      if (!mOutputDir.exists()) {
        mOutputDir.mkdirs();
      }
    }
  }

  public void processCas(CAS arg0) throws ResourceProcessException {
    // Import the CAS as a aJCas
    JCas aJCas = null;
    File outputFile = null;
    PrintWriter writer = null;
    try {
      aJCas = arg0.getJCas();
      try {
        outputFile = new File(Paths.get(mOutputDir.getAbsolutePath(), OUTPUT_FILENAME).toString());
        outputFile.getParentFile().mkdirs();
        writer = new PrintWriter(outputFile);
      } catch (FileNotFoundException e) {
        System.out.printf("Output file could not be written: %s\n",
                Paths.get(mOutputDir.getAbsolutePath(), OUTPUT_FILENAME).toString());
        return;
      }

      writer.println("question_id,tp,fn,fp,precision,recall,f1");
      
      // Retrieve the questions for printout
      List<OutputAnnotation> allQuestions = new ArrayList<OutputAnnotation>();
      @SuppressWarnings({ "unchecked", "rawtypes" })
      FSIndex<OutputAnnotation> teIndex = (FSIndex) aJCas.getAnnotationIndex(OutputAnnotation.type);
      for(OutputAnnotation m : teIndex)
      {
    	  allQuestions.add(m);
      }
      List<OutputAnnotation> subsetOfQuestions = RandomUtils.getRandomSubset(allQuestions, 10);

      
      // TODO: Here one needs to sort the questions in ascending order of their question ID

      for (OutputAnnotation output : subsetOfQuestions) {
        writer.printf(output.getText());
      }
    } catch (CASException e) {
      try {
        throw new CollectionException(e);
      } catch (CollectionException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (writer != null)
        writer.close();
    }
  }
}
