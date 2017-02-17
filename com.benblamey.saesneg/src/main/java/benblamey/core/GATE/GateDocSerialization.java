package benblamey.core.GATE;

import com.benblamey.core.BinarySerializer;
import gate.Document;
import gate.util.GateException;
import java.io.IOException;

public class GateDocSerialization {

    /**
     * @param args
     * @throws GateException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws GateException, ClassNotFoundException, IOException {

        String dir = "C:/work/data/output/Participant 1/";

        Document loadGATEDoc = GateUtils2.loadGATEDoc(dir + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.xml");

        BinarySerializer.writeToFile(dir + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.xml.bin", loadGATEDoc);
        Document tahDah = (Document) BinarySerializer.readObjectFromFile(dir + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.xml.bin");

        GateUtils2.exportGATEtoXML(tahDah, dir + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.roundtriptest.xml");

    }

}
