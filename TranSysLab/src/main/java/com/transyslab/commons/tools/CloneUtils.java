/**
 *
 */
package com.transyslab.commons.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author yali
 *
 */
public class CloneUtils {
	public static <T extends Serializable> T clone(T obj) {
		T cloneObj = null;
		try {
			// д���ֽ���
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream obs = new ObjectOutputStream(out);
			obs.writeObject(obj);
			obs.close();

			// �����ڴ棬д��ԭʼ���������¶���
			ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(ios);
			// �������ɵ��¶���
			cloneObj = (T) ois.readObject();
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return cloneObj;
	}
}
