package br.ufc.mdcc.insightlab.PageRankRDF.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;

public class FileExistValidator implements IParameterValidator {
    public void validate(String name, String value) throws ParameterException {
        File file =  new File(value);
        if(!file.exists() || file.isDirectory()) {
            throw new ParameterException("Parameter " + name + " which should represent a file that exists, does not exist.");
        }
    }
}
