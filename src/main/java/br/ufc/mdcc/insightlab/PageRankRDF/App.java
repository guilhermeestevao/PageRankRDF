package br.ufc.mdcc.insightlab.PageRankRDF;


import org.la4j.Matrix;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;
import br.ufc.mdcc.insightlab.PageRankRDF.validator.AllowedFormats;
import br.ufc.mdcc.insightlab.PageRankRDF.validator.FileCanBeOpenedValidator;
import br.ufc.mdcc.insightlab.PageRankRDF.validator.FileExistValidator;
import br.ufc.mdcc.insightlab.PageRankRDF.validator.PositiveDouble;
import br.ufc.mdcc.insightlab.PageRankRDF.validator.ZeroOneDouble;

/**
 * Hello world!
 *
 */
public class App {

	@Parameter(names={"--iteration", "-I"},validateWith = PositiveInteger.class, description = "specifying the number are performed by PageRank")
	private int numberOfIterations = 40;
	@Parameter(names={"--start-value", "-S"}, validateWith = PositiveDouble.class,  description = "specifying the start value for the PageRank computation")
	private Double startValue = 0.1;
	@Parameter(names={"--dumping", "-D"}, validateWith = ZeroOneDouble.class, description = "specifying the dumping factor for the PageRank computation")
	private Double dampingFactor = 0.85;
	@Parameter(names={"--string"}, description = "option to compute the page rank for strings or not")
	private Boolean string = false;
	@Parameter(names = "--help", help = true, description = "displays the list of possible parameters")
	private boolean help = false;
	@Parameter(names={"--input", "-in"}, required = true, validateWith = FileExistValidator.class, description = "specify a file in some RDF format or in HDT")
	private String input;
	@Parameter(names={"--output", "-out"}, required = true, validateWith = FileCanBeOpenedValidator.class, description = "specify the file where the pagerank scores are stored")
	private String output;
	@Parameter(names={"--format", "-f"}, validateWith = AllowedFormats.class, description = "specify the output format for the PageRank scores, either \"tsv\" or \"nt\"")
	private String outputFormat = "nt";

	public static void main( String[] args )
	{
		App app = new App();
		JCommander jCommander = JCommander.newBuilder()
				.addObject(app)
				.build();
		jCommander.setProgramName("java -jar pagerank.jar -in file -out pagerank");
		try {
			jCommander.parse(args);
			app.run();
		} catch (ParameterException e) {
			e.printStackTrace();
			System.out.println("This is a command line tool for PageRank computation over RDF graphs");
			System.out.println("Example usage: java -jar pagerank.jar -in file -out pagerank");
			System.out.println();
			e.usage();
			System.out.println();
			System.out.println("Note: This program only uses the (directed) link structure of an RDF graph (It ignores predicates and graph names. Duplicate A->B links will be counted twice.)");
			System.out.println("Authors: Andreas Thalhammer (http://andreas.thalhammer.bayern) && Dennis Diefenbach (dennis.diefenbach@univ-st-etienne.fr) ");
			System.out.println("Version: 1.0");
			System.exit(1);
		}
	}

	public void run() {

		long startTime = System.nanoTime();
		if (input.endsWith(".hdt")){
			PageRankHDT pr = new PageRankHDT(input, dampingFactor, numberOfIterations);
			INDArray result = pr.compute();
			
			for(int i = 0; i <result.rows(); i++ ){
				System.out.println();
				for(int j = 0; j <result.columns(); j++){
					double value = result.getDouble(i, j);
					System.out.print(" "+value);
				}



			}
			
			
		}
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("The computation took "+estimatedTime);

		System.exit(1);
	}
}
