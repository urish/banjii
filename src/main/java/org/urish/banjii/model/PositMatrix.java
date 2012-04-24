package org.urish.banjii.model;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class PositMatrix {
	private Vector3 translation;
	private Matrix3 rotation;

	public PositMatrix(Vector3 translation, Matrix3 rotation) {
		super();
		this.translation = translation;
		this.rotation = rotation;
	}

	public PositMatrix(PositMatrix source) {
		super();
		this.translation = new Vector3(source.getTranslation());
		this.rotation = new Matrix3(source.getRotation());
	}
	
	public static PositMatrix load(double[] values) {
		Matrix3 rotation = new Matrix3(values[0], values[1], values[2], values[4], values[5], values[6], values[8],
				values[9], values[10]);
		Vector3 translation = new Vector3(values[3], values[7], values[11]);
		return new PositMatrix(translation, rotation);
	}

	public ReadOnlyVector3 getTranslation() {
		return translation;
	}

	public void setTranslation(Vector3 translation) {
		this.translation = translation;
	}

	public ReadOnlyMatrix3 getRotation() {
		return rotation;
	}

	public void setRotation(Matrix3 rotation) {
		this.rotation = rotation;
	}

	public void addTranslation(Vector3 source) {
		translation.addLocal(source);
	}

	public void multiplyTranslation(double amount) {
		translation.multiplyLocal(amount);
	}

	public Transform toTransform() {
		Transform result = new Transform();
		result.setTranslation(translation);
		result.setRotation(rotation);
		return result;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " [\n Translation : " + translation + "\n Rotation: " + rotation + "\n]";
	}
}
