package eu.europeana.api2.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import static org.apache.commons.lang3.StringUtils.*;

public class TurtleRecordWriter implements AutoCloseable {
    private static final Logger LOG  = Logger.getLogger(TurtleRecordWriter.class);

    public static final int KB = 1024;
    public static final int BUFFER_SIZE = 32 * KB;

    private BufferedWriter bufferedWriter;
    private Map<String, String> map = new HashMap();

    public TurtleRecordWriter(OutputStream out) {
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)
                , BUFFER_SIZE);
        map.clear();
    }

    public void write(Model m ) throws IOException {
        writeHeader(m);
        ResIterator iter = m.listSubjectsWithProperty(RDF.type);
        try {
            while (iter.hasNext()) {
                writeResource(iter.next());
            }
        } finally {
            iter.close();
            bufferedWriter.flush();
        }
    }

    private void writeResource(Resource r) throws IOException {
        try {
            Map<String, Property> props = getProperties(r);
            int len = calcMaxLength(props.keySet());
            boolean first = true;
            bufferedWriter.newLine();
            writeValue(r);
            for (Map.Entry<String, Property> entry : props.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    bufferedWriter.append(" ;");
                }
                bufferedWriter.newLine();
                writePropertyDecl(entry.getKey(), len);
                writePropertyValues(r, entry.getValue());
            }
            bufferedWriter.append(" .");
            bufferedWriter.newLine();
        } catch (IOException e) {
            LOG.error("Error ocuured while writing resource " + r.getURI(), e);
        }
    }

    private void writeHeader(Model m) throws IOException {
        for (Map.Entry<String, String> entry : m.getNsPrefixMap().entrySet()) {
            String nPre = entry.getKey();
            String nURI = entry.getValue();
            String oURI = map.get(nPre);
            if (oURI == null || !oURI.equals(nURI)) {
                writePrefix(nPre, nURI);
            }
        }
    }

    private void writePrefix(String prefix, String ns) throws IOException {
        map.put(prefix, ns);
        bufferedWriter.append("@prefix ").append(prefix).append(": ");
        writeAsIRI(ns).append(" .");
        bufferedWriter.newLine();
    }

    private void writePropertyDecl(String qname, int length) throws IOException {
        bufferedWriter.append("\t").append(qname);
        for (int i = length - qname.length(); i > 0; i--) {
            bufferedWriter.append(" ");
        }
        bufferedWriter.append("  ");
    }

    private void writePropertyValues(Resource r, Property p) throws IOException {
        boolean first = true;
        StmtIterator iter = r.listProperties(p);
        try {
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    bufferedWriter.append(", ");
                }
                writeValue(iter.next().getObject());
            }
        } finally {
            iter.close();
        }
    }

    private void writeValue(RDFNode node) throws IOException {
        if (node.isResource()) {
            writeValue(node.asResource());
        } else {
            writeValue(node.asLiteral());
        }
    }

    private void writeValue(Resource r) throws IOException {
        String uri = r.getURI();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (!uri.startsWith(value)) {
                continue;
            }
            bufferedWriter.append(entry.getKey() + ":" + uri.substring(value.length()));
            return;
        }
        writeAsIRI(uri);
    }

    private void writeValue(Literal l) throws IOException {
        bufferedWriter.append('"');
        writeAsString(l.getString());
        bufferedWriter.append('"');
        String lang = l.getLanguage();
        if (!isEmpty(lang)) {
            bufferedWriter.append("@").append(lang);
            return;
        }
        RDFDatatype dt = l.getDatatype();
        if (hasDatatype(dt)) {
            writeDatatype(dt, l.getModel());
        }
    }

    private void escapeUnicode(char c) throws IOException {
        bufferedWriter.append("\\u");
        String s = Integer.toHexString(c).toUpperCase();
        for (int i = 4 - s.length(); i > 0; i--) {
            bufferedWriter.append('0');
        }
        bufferedWriter.append(s);
    }

    private BufferedWriter writeAsString(String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\t':
                case '\b':
                case '\n':
                case '\r':
                case '\f':
                case '\"':
                case '\\':
                    bufferedWriter.append('\\').append(c);
                    continue;
                default: // Do nothing
            }
            bufferedWriter.append(c);
        }
        return bufferedWriter;
    }

    private BufferedWriter writeDatatype(RDFDatatype dt
            , Model model) throws IOException {
        bufferedWriter.append("^^");
        String uri = dt.getURI();
        String prefix = model.getNsURIPrefix(XSD.NS);
        if (prefix != null && uri.startsWith(XSD.NS)) {
            bufferedWriter.append(prefix).append(":")
                    .append(uri.substring(XSD.NS.length()));
            return bufferedWriter;
        }
        return writeAsIRI(uri);
    }

    private BufferedWriter writeAsIRI(String str) throws IOException {
        bufferedWriter.append('<');
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c >= '\u0000' && c <= '\u0020') {
                escapeUnicode(c);
                continue;
            }
            switch (c) {
                case '<':
                case '>':
                case '"':
                case '{':
                case '}':
                case '|':
                case '^':
                case '`':
                case '\\':
                    escapeUnicode(c);
                    continue;
                default: // Do nothing
            }
            bufferedWriter.append(c);
        }
        bufferedWriter.append('>');
        return bufferedWriter;
    }

    private Map<String, Property> getProperties(Resource r) {
        Map<String, Property> ret = new TreeMap<>();
        StmtIterator iter = r.listProperties();
        try {
            while (iter.hasNext()) {
                Property p = iter.next().getPredicate();
                ret.put(RDF.type.equals(p) ? "a" : getPropertyRef(p), p);
            }
        } finally {
            iter.close();
        }
        return ret;
    }

    private String getPropertyRef(Resource r) {
        if (r == null) {
            return null;
        }
        String ns = r.getNameSpace();
        if (ns == null) {
            return "<" + r.getURI() + ">";
        }
        String p = r.getModel().getNsURIPrefix(ns);
        return (p == null ? r.getURI() : (p + ":" + r.getLocalName()));
    }

    private int calcMaxLength(Collection<String> col) {
        int len = 0;
        for (String value : col) {
            len = Math.max(len, value.length());
        }
        return len;
    }

    private boolean hasDatatype(RDFDatatype dt) {
        String uri = XSD.xstring.getURI();
        return ((dt != null) && !dt.getURI().equals(uri));
    }

    @Override
    public void close(){
        try {
             bufferedWriter.close();
        } catch (IOException e) {
            LOG.error("Error closing the buffer writer ", e);
        }
    }
}