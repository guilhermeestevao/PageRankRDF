package br.ufc.mdcc.insightlab.PageRankRDF;

import java.io.IOException;
import java.math.BigDecimal;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;

public class PageRankHDT {

	private HDT hdt;
	private double[][] matrizTransicao;
	private double dampingFactor;
	private int numberOfIterations;

	public PageRankHDT(String hdtDump, double factor, int interations){
		load(hdtDump);
		matrizTransicao = Index.getInstance(hdt).getMatrix();
		dampingFactor = factor;
		numberOfIterations = interations;
	
	}
		
	public INDArray compute(){
		INDArray adja = Nd4j.create(matrizTransicao);
		INDArray tens = Nd4j.zeros(matrizTransicao.length, 1).addi(dampingFactor);
		
		return compute(adja, tens, numberOfIterations);
	}
	
	private INDArray compute(INDArray adjacencyMatrix, INDArray pr, int interation){
		if(interation == 0){
			return pr;
		}else{
			INDArray vector = adjacencyMatrix.mmul(pr);
			interation--;
			return compute(adjacencyMatrix, vector, interation);	
		}
	}
	
	private void load(String hdtDump){
        try {
            hdt = HDTManager.mapIndexedHDT(hdtDump, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
