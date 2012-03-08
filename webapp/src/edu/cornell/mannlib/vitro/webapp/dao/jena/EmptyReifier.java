package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collections;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class EmptyReifier implements Reifier {

    private Graph g;
    
    public EmptyReifier(Graph g) {
        this.g = g;
    }
    
    @Override
    public Triple getTriple(Node arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExtendedIterator<Node> allNodes() {
        return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
    }

    @Override
    public ExtendedIterator<Node> allNodes(Triple arg0) {
        return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public ExtendedIterator<Triple> find(TripleMatch arg0) {
        return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
    }

    @Override
    public ExtendedIterator<Triple> findEither(TripleMatch arg0, boolean arg1) {
        return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
    }

    @Override
    public ExtendedIterator<Triple> findExposed(TripleMatch arg0) {
        return WrappedIterator.create(Collections.EMPTY_LIST.iterator());
    }

    @Override
    public Graph getParentGraph() {
        return g;
    }

    @Override
    public ReificationStyle getStyle() {
        return ReificationStyle.Minimal;
    }

    @Override
    public boolean handledAdd(Triple arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handledRemove(Triple arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasTriple(Node arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasTriple(Triple arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node reifyAs(Node arg0, Triple arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(Triple arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void remove(Node arg0, Triple arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

}