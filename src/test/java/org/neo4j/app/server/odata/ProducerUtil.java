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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.odata4j.producer.resources.CrossDomainResourceConfig;
import org.odata4j.producer.resources.ODataResourceConfig;
import org.odata4j.producer.server.JerseyServer;

import com.sun.jersey.api.container.filter.LoggingFilter;

public class ProducerUtil {

  public static void hostODataServer(String baseUri) {
    JerseyServer server = startODataServer(baseUri);
    System.out.println("Press any key to exit");
    readLine();
    server.stop();
  }

  public static JerseyServer startODataServer(String baseUri) {
    JerseyServer server = new JerseyServer(baseUri);
    server.addAppResourceClasses(new ODataResourceConfig().getClasses());
    server.addRootResourceClasses(new CrossDomainResourceConfig().getClasses());

    server.addJerseyRequestFilter(LoggingFilter.class); // log all requests

    // server.addHttpServerFilter(new WhitelistFilter("127.0.0.1","0:0:0:0:0:0:0:1%0")); // only allow local requests
    server.start();

    return server;

  }

  public static void readLine() {
    try {
      new BufferedReader(new InputStreamReader(System.in)).readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
