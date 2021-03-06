package org.urish.banjii.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.urish.banjii.model.CameraManager;

@Path("/")
public class RootResource {
	private final CameraManager cameraManager = CameraManager.instance;
	
	@GET
	public String test() {
		return "API is working!";
	}
	
	@POST
	@Path("/updateConnection/{id}")
	public void updateConnection(@PathParam("id") int cameraId)
	{
		cameraManager.updateCameraConnection(cameraId);
	}
	
	@POST
	@Path("/cameras/{id}")
	public void updateCamera(@PathParam("id") int cameraId, @FormParam("marker") int markerId, @FormParam("matrix") String matrix) {
		String[] splitMatrix = matrix.split(",");
		double[] dblMatrix = new double[splitMatrix.length];
		for (int i = 0; i < dblMatrix.length; i++) {
			dblMatrix[i] = Double.valueOf(splitMatrix[i]);
		}
		cameraManager.onMarkerMovement(cameraId, markerId, dblMatrix);
	}
}
