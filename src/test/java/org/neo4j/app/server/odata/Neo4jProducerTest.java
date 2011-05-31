/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.app.server.odata;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.core4j.Func;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.ImpermanentGraphDatabase;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.rest.DocsGenerator;
import org.neo4j.test.GraphDescription;
import org.neo4j.test.GraphDescription.Graph;
import org.neo4j.test.TestData.Title;
import org.neo4j.test.GraphHolder;
import org.neo4j.test.TestData;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.CrossDomainResourceConfig;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.resources.ODataResourceConfig;
import org.odata4j.producer.server.JerseyServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class Neo4jProducerTest implements GraphHolder
{

    private static final String NEO4J_ODATA = "Neo4jOdata";
    private static final String HELLO_WORLD = "HelloWorld";
    private String ENDPOINT = "http://localhost:8887/"+NEO4J_ODATA+"/";
    private JerseyServer server;

    public @Rule
    TestData<Map<String, Node>> data = TestData.producedThrough( GraphDescription.createGraphFor(
            this, true ) );
    
    public @Rule
    TestData<DocsGenerator> gen = TestData.producedThrough( DocsGenerator.PRODUCER );
    private static GraphDatabaseService graphdb;
    
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Documented("OData HelloWorld")
    @Title("Title")
    @Test
    @Graph({"I know you"})
    public void testBootup()
    {


        final InMemoryProducer producer = new InMemoryProducer(
                NEO4J_ODATA );

        
        producer.register( Entry.class, String.class, HELLO_WORLD,
                new Func<Iterable<Entry>>()
                {
                    public Iterable<Entry> apply()
                    {
                        HashMap<String, String>nodes = new HashMap<String, String>();
                        for(Entry<String, Node> entry : data.get().entrySet()) {
                            nodes.put( entry.getKey(), entry.getValue().toString() );
                        }
                        return (Iterable<Entry>) (Object) nodes.entrySet();
                    }
                }, "Key" );

        
        ODataProducerProvider.setInstance( producer );
        Map<String, String> params = new HashMap<String, String>();
        params.put( "charset", "utf-8" );
        String response = gen.get()
        .expectedStatus( Status.OK.getStatusCode() )
        .expectedType( new MediaType("application", "atom+xml", params ) )
        .get( ENDPOINT + HELLO_WORLD)
        .entity();
        System.out.println(response);
        assertTrue(response.contains( "you" ));
    }


    @Before
    public void startServer()
    {
        
        server = new JerseyServer( ENDPOINT );
        server.addAppResourceClasses( new ODataResourceConfig().getClasses() );
        server.addRootResourceClasses( new CrossDomainResourceConfig().getClasses() );
        server.addJerseyRequestFilter( LoggingFilter.class ); // log all
                                                              // requests
        server.start();

    }
    
    @After
    public void stopServer() {
        server.stop();
    }
    
    @Override
    public GraphDatabaseService graphdb()
    {
        return graphdb;
    }
    
    @BeforeClass
    public static void startDatabase()
    {
        graphdb = new ImpermanentGraphDatabase("target/db"+System.currentTimeMillis());
        
    }
}
