package org.urish.banjii.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.urish.banjii.Scene;
import org.urish.banjii.api.CameraListener;

@Path("/")
public class RootResource {
	@GET
	public String test() {
		return "API is working!";
	}
	
	@POST
	@Path("/cameras/{id}")
	public void updateCamera(@PathParam("id") int cameraId, @FormParam("marker") int markerId, @FormParam("x") double x,
			@FormParam("y") double y, @FormParam("z") double z, @FormParam("matrix") String matrix) {
		CameraListener listener = Scene.instance;
		String[] splitMatrix = matrix.split(",");
		double[] dblMatrix = new double[splitMatrix.length];
		for (int i = 0; i < dblMatrix.length; i++) {
			dblMatrix[i] = Double.valueOf(splitMatrix[i]);
		}
		if (listener != null) {
			listener.onCameraMovement(cameraId, markerId, x, y, z, dblMatrix);
		}
	}
}
