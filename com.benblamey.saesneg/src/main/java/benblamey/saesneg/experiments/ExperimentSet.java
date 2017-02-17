package benblamey.saesneg.experiments;

import benblamey.core.MongoClientInstance;
import benblamey.core.json.JavaToJson;
import benblamey.saesneg.PipelineContext;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import edu.stanford.nlp.io.IOUtils;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A set of experiments, to compare variations in parameters or options.
 *
 * @author Ben Blamey blamey.ben@gmail.com
 *
 */
public class ExperimentSet implements IExperimentContainer {

    public String name = "expset";
    public final List<Experiment> exps = new ArrayList<Experiment>();
    final private String _outDirName;
    final private Date _startedTime;

    public ExperimentSet() {
        this("normal");
    }
    
    public ExperimentSet(String info) {
        _startedTime = new Date();
        // Set this once when we start - we might be several days.
        
        String hostname;
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
        
        _outDirName = "C:\\work\\data\\output\\PHD_DATA\\" + new SimpleDateFormat("yyyy-MM-dd\\HH.mm.ss").format(_startedTime) + "_" + name + "_"  + hostname + "_" + info;
        //new File(_outDirName).mkdirs();
    }

    /* (non-Javadoc)
	 * @see benblamey.saesneg.experiments.IExperimentContainer#getOutputDirectory()
     */
    @Override
    public String getOutputDirectoryWithTrailingSlash() {
        return _outDirName + "\\";
    }

    public void run() throws Exception {
        int i = 0;
        for (Experiment e : exps) {
            System.out.println("Running experiment " + i);
            e.run(); // Evaluates as original, then increments.
            i++;
        }
        writeClusteringResults();
    }

    private void writeClusteringResults() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(PipelineContext.getCurrentContext().getDataOutputDir() + "/clusteringAccuracy.txt"), "UTF-8"));
        for (Experiment exp : exps) {
            out.append("===========================");
            out.append(exp.Name);
            out.append(JavaToJson.toJSON(exp.Results));
            out.append("===========================");
        }
        out.close();
        
        IOUtils.writeStringToFileNoExceptions(generateResultsJson().toString(),
                this.getOutputDirectoryWithTrailingSlash() + "clusteringResults.json", 
                "UTF-8");
    }

    private void exportResultsToMongo() {
        BasicDBObject resultObject = generateResultsJson();
        DBCollection resultsColl = MongoClientInstance.getClientLocal().getDB("exp-results").getCollection("exps");
        resultsColl.insert(resultObject);
    }

    private BasicDBObject generateResultsJson() {
        //Gson gson = new Gson();
        BasicDBObject resultObject = new BasicDBObject();
        resultObject.put("name", name);
        resultObject.put("startedTime", _startedTime);
        List<BasicDBObject> experiments = new ArrayList<>();
        for (Experiment exp : exps) {
            BasicDBObject experiment = new BasicDBObject();
            experiment.put("name", exp.Name);
            //experiment.put("options", (BasicDBObject) JSON.parse(JavaToJson.toJSON(exp.Options)));
            experiment.put("results", (BasicDBObject) JSON.parse(JavaToJson.toJSON(exp.Results)));
            experiments.add(experiment);

        }
        resultObject.put("exps", experiments);
        return resultObject;
    }

    public void addExperiment(Experiment exp) {
        exps.add(exp);
    }

}
