package org.urish.banjii.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class RootResource {
	@GET
	public String test() {
		return "OK";
	}
}
