// RosterTableModelTest.java

package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.*;

/**
 * Tests for the roster.swing.RosterTableModel class.
 *
 * @author	Bob Jacobsen     Copyright (C) 2009
 * @version     $Revision: 1.3 $
 */
public class RosterTableModelTest extends TestCase {

    public void testTableLength() throws Exception {
        RosterTableModel t = new RosterTableModel();
        
        Assert.assertEquals(NENTRIES, t.getRowCount());
    }

    public void testTableWidth() throws Exception {
        RosterTableModel t = new RosterTableModel();
        
        // hard-coded value is number of columns expected
        Assert.assertEquals(4, t.getColumnCount());
    }

    public void testColumnName() throws Exception {
        RosterTableModel t = new RosterTableModel();
        
        Assert.assertEquals("Road Number", t.getColumnName(1));
    }

    public void testGetValueAt() {
        RosterTableModel t = new RosterTableModel();
        
        Assert.assertEquals("id 1", t.getValueAt(0,0));    
        Assert.assertEquals("431", t.getValueAt(0,1));    
        Assert.assertEquals("SP", t.getValueAt(0,2));    
        Assert.assertEquals("Athearn", t.getValueAt(0,3));    
        Assert.assertEquals("id 2", t.getValueAt(1,0));    
        Assert.assertEquals("431", t.getValueAt(1,1));    
        Assert.assertEquals("SP", t.getValueAt(1,2));    
        Assert.assertEquals("Athearn", t.getValueAt(1,3));    
        Assert.assertEquals("id 3", t.getValueAt(2,0));    
        Assert.assertEquals("431", t.getValueAt(2,1));    
        Assert.assertEquals("SP", t.getValueAt(2,2));    
        Assert.assertEquals("Athearn", t.getValueAt(2,3));    
    }
    
    // create a standard test roster
    
    static int NENTRIES = 3;
    static int NKEYS = 4;
    
    public void setUp() {
        apps.tests.Log4JFixture.setUp();

        // Reset Roster instance to ensure empty
        Roster.resetInstance();
        new Roster(){
            { _instance = this; }
        };

        // first entry
        Element e;
        RosterEntry r;
        
        e = new org.jdom.Element("locomotive")
            .setAttribute("id","id 1")
            .setAttribute("fileName","file here")
            .setAttribute("roadNumber","431")
            .setAttribute("roadName","SP")
            .setAttribute("mfg","Athearn")
            .setAttribute("dccAddress","1234")
            .addContent(new org.jdom.Element("decoder")
                        .setAttribute("family","91")
                        .setAttribute("model","33")
                        )
            .addContent(new org.jdom.Element("locoaddress")
                .addContent(new org.jdom.Element("dcclocoaddress")
                        .setAttribute("number","12")
                        .setAttribute("longaddress","yes")
                        )
                )
            ; // end create element

        r = new RosterEntry(e){
                     protected void warnShortLong(String s){}
             };
        Roster.instance().addEntry(r);
        r.putAttribute("key a", "value 1");
        
        e = new org.jdom.Element("locomotive")
            .setAttribute("id","id 2")
            .setAttribute("fileName","file here")
            .setAttribute("roadNumber","431")
            .setAttribute("roadName","SP")
            .setAttribute("mfg","Athearn")
            .addContent(new org.jdom.Element("decoder")
                        .setAttribute("family","91")
                        .setAttribute("model","33")
                        )
            .addContent(new org.jdom.Element("locoaddress")
                .addContent(new org.jdom.Element("dcclocoaddress")
                        .setAttribute("number","12")
                        .setAttribute("longaddress","yes")
                        )
                )
            ; // end create element

        r = new RosterEntry(e){
                   protected void warnShortLong(String s){}
             };
        Roster.instance().addEntry(r);
        r.putAttribute("key a", "value 11");
        r.putAttribute("key b", "value 12");
        r.putAttribute("key c", "value 13");
        r.putAttribute("key d", "value 14");

        e = new org.jdom.Element("locomotive")
            .setAttribute("id","id 3")
            .setAttribute("fileName","file here")
            .setAttribute("roadNumber","431")
            .setAttribute("roadName","SP")
            .setAttribute("mfg","Athearn")
            .addContent(new org.jdom.Element("decoder")
                        .setAttribute("family","91")
                        .setAttribute("model","33")
                        )
            .addContent(new org.jdom.Element("locoaddress")
                .addContent(new org.jdom.Element("dcclocoaddress")
                        .setAttribute("number","12")
                        .setAttribute("longaddress","yes")
                        )
                )
            ; // end create element

        r = new RosterEntry(e){
                    protected void warnShortLong(String s){}
             };
        Roster.instance().addEntry(r);

    }

    // from here down is testing infrastructure

    public RosterTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RosterTableModelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RosterTableModelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
